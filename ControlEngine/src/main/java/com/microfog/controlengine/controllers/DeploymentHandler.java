package com.microfog.controlengine.controllers;

import com.microfog.controlengine.model.deployment.*;
import com.microfog.controlengine.model.domainObjects.*;
import com.microfog.controlengine.utils.Events;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class DeploymentHandler {

    private final static Logger LOGGER = Logger.getLogger(DeploymentHandler.class.getName());


//    @Inject
//    CommandQueue commandQueue;
    @Inject
    CommandHandler commandHandler;
    @Inject
    DataMapperMetadata dataMapperMetaData;
    @ConfigProperty(name = "controlengine.loadblancing.enabled")
    Boolean lbEnabled;

    public Uni<String> deployCommands(List<DeploymentInfo> deploymentInfo) {
        LOGGER.info(Events.DEPLOYMENT_OF_RECEIVED_COMMANDS_STARTED + " - " + System.currentTimeMillis());
        for (DeploymentInfo d : deploymentInfo) {
            Boolean lbUnavailable = d.getLbInfo().isEmpty();
            Application application = loadRelatedAppInfo(d.getApplicationId());

            List<Command> thisAppCommand = new ArrayList<>();
            if (!d.isOnlyLBrelated()) {
                List<String> gwCommandsPlaced = new ArrayList<>();
                thisAppCommand.addAll(application.getAppLevelCommand());

                boolean isEntrycluster = d.getEntryCluster();
                List<Placement> placements = d.getMicroservicesToDeploy();

                Map<String,Placement> mToPlacementMap = new HashMap<>();
                for(Placement placement:placements)
                    mToPlacementMap.put(placement.getMicroserviceId(),placement);

                for(Placement placement:placements){
                    String m = placement.getMicroserviceId();
                    Microservice microservice= application.getMicroservice(m);
                    thisAppCommand.addAll(getMicroserviceLevelCommands(microservice, placement.getDeviceMapping(commandHandler.getCurrentClusterName())));
                    //
                    if (!application.getIngressMicroservices().contains(microservice))
                        thisAppCommand.addAll(microservice.getCommandsSLevelNoLB());
                    else {
                        thisAppCommand.addAll(getServiceLevelForIngress(microservice, isEntrycluster,lbUnavailable));
                        gwCommandsPlaced.add(m);
                    }
                    // for connectivity with upstream services
                    thisAppCommand.addAll(getUpstreamCommands(microservice, application, mToPlacementMap));
                }
                for(Microservice m: application.getIngressMicroservices()){
                    if(!gwCommandsPlaced.contains(m.getMicroserviceId()))
                        thisAppCommand.addAll(getServiceLevelForIngress(m,isEntrycluster,lbUnavailable));
                }
                for(String m: d.getCompositionOnlyPlacements()){
                    thisAppCommand.addAll(application.getMicroservice(m).getCommandsSLevelNoLB());
                }
            }

            if(!lbUnavailable) {
                //load balancing related
                thisAppCommand.addAll(generateLoadBalancingRelatedCommands(d, application));
            }

            deploy(thisAppCommand);
//            commandQueue.addToQueue(thisAppCommand);
            LOGGER.info(Events.COMMAND_EXECUTION_RECEIVED_COMMANDS_COMPLETED + " - " + System.currentTimeMillis());
        }

        return Uni.createFrom().item("Deployment of commands completed for " + commandHandler.getCurrentClusterName());
    }

    private List<Command> getUpstreamCommands(Microservice m, Application application, Map<String, Placement> placementPerApp) {
        List<Command> serviceLevelCommands = new ArrayList<>();
        //get upstream microservices for "m"
        List<Microservice> upstreamM = application.getApplicationDAG().getConsumedMicroservices(m);
        for (Microservice um : upstreamM) {
            if (microServicePlacedInThisCluster(placementPerApp, um.getMicroserviceId()))
                continue;
            else
                serviceLevelCommands.addAll(um.getCommandsSLevelNoLB());
        }
        return serviceLevelCommands;
    }

    private boolean microServicePlacedInThisCluster(Map<String, Placement> placements, String microserviceId) {
        if (placements.containsKey(microserviceId) && !placements.get(microserviceId).getDeviceMapping().isEmpty())
            return true;

        return false;
    }

    private List<Command> getServiceLevelForIngress(Microservice m, Boolean isAccessCluster, boolean lbUnavailable) {
        List<Command> serviceLevelCommands = new ArrayList<>();
        for (Command c : m.getCommandsSLevelAll()) {
            LOGGER.info("Command test : " + c);
            if (c.getCommandType().equals(CommandTypeEnum.CREATE_GW)) {
                LOGGER.info("GW command");
                //todo what about virtual service
                if (isAccessCluster) {
                    LOGGER.info("Adding GW command");
                    serviceLevelCommands.add(c);
                }
            } else if (c.getCommandType().equals(CommandTypeEnum.CREATE_VS)) {
                if (lbUnavailable && !lbEnabled)
                    serviceLevelCommands.add(c);
            } else if (c.getCommandType().equals(CommandTypeEnum.CREATE_DR)) {
                //will be handled with loadblancing
                continue;
            } else
                serviceLevelCommands.add(c);
        }
        return serviceLevelCommands;
    }

    private List<Command> getMicroserviceLevelCommands(Microservice m, Map<String, Integer> nodeNames) {
        List<Command> microserviceLevelCommands = new ArrayList<>();
        List<Command> microserviceLevel1 = new ArrayList<>();
        List<Command> microserviceLevel2 = new ArrayList<>();
        List<Command> commandM = m.getCommandsMLevel();

        for (Command c : commandM) {
            if (c.getResourceType().equals(ResourceEnum.CONFIGMAP))
                microserviceLevel1.add(c);
            if (c.getResourceType().equals(ResourceEnum.POD)) {
                ((Pod) c.getResource()).setNodeNames(new ArrayList<>(nodeNames.keySet()));
                for (String node : nodeNames.keySet()) {
                    ((Pod) c.getResource()).addRequestedResources(node, Pod.ResourceType.CPU, m.getCpu().multiply(new BigDecimal(nodeNames.get(node))));
                    ((Pod) c.getResource()).addRequestedResources(node, Pod.ResourceType.MEMORY, m.getRam().multiply(new BigDecimal(nodeNames.get(node))));
                }
                microserviceLevel2.add(c);
            }
        }
        //configmaps should be deployed before the pods that use them
        microserviceLevelCommands.addAll(microserviceLevel1);
        microserviceLevelCommands.addAll(microserviceLevel2);

        return microserviceLevelCommands;
    }

    private List<Command> generateLoadBalancingRelatedCommands(DeploymentInfo d, Application app) {
        List<Command> commands = new ArrayList<>();
        for (String m : d.getLbInfo().keySet()){
            Microservice microservice = app.getMicroservice(m);
            commands.addAll(getLoadBalancingCommands(microservice,d.getLbInfo().get(m)));
        }
        return commands;
    }

    private List<Command> getLoadBalancingCommands(Microservice m, Map<String, Integer> lbWeights) {
        List<Command> lbCommands = new ArrayList<>();
        for (Command c : m.getCommandsSLevelLB()) {
            if (c.getResourceType().equals(ResourceEnum.VS)) {
                IstioVirtualService vs = (IstioVirtualService) c.getResource();
                for (String destSubset : lbWeights.keySet()) {
                    vs.addRoute(destSubset, lbWeights.get(destSubset));
                }
                lbCommands.add(c);
            } else if (c.getResourceType().equals(ResourceEnum.DR)) {
                IstioDestinationRule dr = (IstioDestinationRule) c.getResource();
                for (String subset : lbWeights.keySet()) {
                    Map<String, String> labels = new HashMap<>();
                    labels.put("nodeName", subset);
                    dr.addSubset(subset, labels);
                }
                lbCommands.add(c);
            }
        }
        return lbCommands;
    }

    private Application loadRelatedAppInfo(String appId) {
        return dataMapperMetaData.getObjectFromBucket(appId);
    }

    private void deploy(List<Command> commands) {
        for (Command c : commands) {
            commandHandler.executeCommand(c);
        }
    }

}
