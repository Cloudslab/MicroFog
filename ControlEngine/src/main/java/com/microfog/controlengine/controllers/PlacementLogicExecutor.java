package com.microfog.controlengine.controllers;

import com.microfog.controlengine.loadBalancing.LoadBalancingPolicies;
import com.microfog.controlengine.model.deomainObjectsLight.ApplicationL;
import com.microfog.controlengine.model.deployment.*;
import com.microfog.controlengine.model.domainObjects.*;
import com.microfog.controlengine.placementlogic.PlacementAlgoFactory;
import com.microfog.controlengine.placementlogic.PlacementAlgorithm;
import com.microfog.controlengine.placementlogic.PlacementOutPut;
import com.microfog.controlengine.requestForwardingLogic.ForwardingPolicy;
import com.microfog.controlengine.services.*;
import com.microfog.controlengine.utils.Events;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class PlacementLogicExecutor implements Runnable {
    private final static Logger LOGGER = Logger.getLogger(PlacementLogicExecutor.class.getName());

    @Inject
    PrQueue prQueue;
    @Inject
    ForwardRequestQueue forwardRequestQueue;
    @Inject
    PlacementAlgoFactory placementAlgoFactory;
    @Inject
    CommandHandler commandHandler;
    @Inject
    DataMapperMetadata dataMapperMetaData;
    @Inject
    DeploymentHandler deploymentHandler;

    String algoName = ConfigProvider.getConfig().getValue("controlengine.placementalgo", String.class);
    String forwardUrl = ConfigProvider.getConfig().getValue("controlengine.forwardUrl", String.class);

    @ConfigProperty(name = "controlengine.placementalgotype.internal")
    Boolean internalAlgo;

    @ConfigProperty(name = "controlengine.operationmode.distributed")
    Boolean distributedPlacement;

    @ConfigProperty(name = "controlengine.operationmode.primary")
    Boolean isPrimaryCE;

    @ConfigProperty(name = "controlengine.placementmode.periodic")
    Boolean batchPlacement;

    @ConfigProperty(name = "controlengine.loadblancing.enabled")
    Boolean lbEnabled;


    PlacementAlgorithm placementAlgorithm;


    @Inject
    ForwardingPolicy forwardingPolicy;   //to the cloud, or to random cluster, to the closest cluster, a random cluster or even based on resource availability + distance

    @Inject
    LoadBalancingPolicies loadBalancingPolicies;

    @ConfigProperty(name = "fog.adjacent.cluster")
    Optional<List<String>> adjacentEdgeCluster;

    @ConfigProperty(name = "cloud.adjacent.cluster")
    Optional<List<String>> cloudClusters;


    String clusterName = ConfigProvider.getConfig().getValue("cluster.name", String.class);

    List<Cluster> connectedClusters = new ArrayList<>();

    @PostConstruct
    public void init() {
        placementAlgorithm = placementAlgoFactory.get(algoName);

        // adjacent cluster data {this can be extended to add more information}
        List<String> edgeClusterNames = adjacentEdgeCluster.orElse(Collections.emptyList()).stream().collect(Collectors.toList());
        for (String name : edgeClusterNames)
            connectedClusters.add(new Cluster(name, Cluster.ClusterType.FOG));

        List<String> cloudClusterNames = cloudClusters.orElse(Collections.emptyList()).stream().collect(Collectors.toList());
        for (String name : cloudClusterNames)
            connectedClusters.add(new Cluster(name, Cluster.ClusterType.CLOUD));

    }

    @Override
    public void run() {
        executePlacement();
    }

    public void executePlacement() {

        LOGGER.debug("Placement process invoked at time : " + LocalDateTime.now());
        List<PlacementRequest> prs = prQueue.getFromPrQueue();

        if (!prs.isEmpty()) {
            //run placement algorithm and get output: map of microservice to node name

            LOGGER.info(Events.PR_PROCESSING_BEGIN + " - Periodic Placement of PRs from PR ID - " + prs.get(0).prId + " - " + prs.get(prs.size() - 1).prId + " - " + System.currentTimeMillis());

            /**
             * 1. Load Cluster Details
             */
            LOGGER.info(Events.RETRIEVE_META_DATA_START + " - " + System.currentTimeMillis());
            List<FogDevice> inclusterDeviceData = loadDeviceData(commandHandler);

            Map<String, ClusterData> clusterData = new HashMap<>();
            ClusterData currentClusterData = new ClusterData(clusterName, inclusterDeviceData,
                    adjacentEdgeCluster.isPresent() ? adjacentEdgeCluster.get() : new ArrayList<>(),
                    cloudClusters.isPresent() ? cloudClusters.get() : new ArrayList<>());
            clusterData.put(clusterName, currentClusterData);

            if (!distributedPlacement && isPrimaryCE) {
                LOGGER.info(Events.RETRIEVE_OTHER_CLUSTER_DATA_BEGIN + " - " + System.currentTimeMillis());
                requestOtherClusterData(clusterData); // clustername -> cluster data

                LOGGER.info(Events.RETRIEVE_OTHER_CLUSTER_DATA_END + " - " + System.currentTimeMillis() + " - cluster data received from "
                        + clusterData.size() + " clusters"
                );
            }


            /**
             * 2. Load Application Details from Redis Database
             */
            LOGGER.info(Events.RETRIEVE_REDIS_DATA_BEGIN + " - " + System.currentTimeMillis());
            Map<String, Application> appInfo = loadRelatedAppInfo(prs, dataMapperMetaData);
            LOGGER.info(Events.RETRIEVE_REDIS_DATA_END + " - " + System.currentTimeMillis());

            LOGGER.info(Events.RETRIEVE_META_DATA_END + " - " + System.currentTimeMillis());

            /**
             * 3. Execute algorithm and Generate Placement Output
             */
            PlacementOutPut placementOutput;
            LOGGER.info(Events.PLACEMENT_ALGO_START + " - " + System.currentTimeMillis());
            if (internalAlgo)
                placementOutput = generatePlacement(prs, appInfo, inclusterDeviceData, clusterData);
            else {
                List<ApplicationL> appInfoL = generateAppDataLight(appInfo);
                placementOutput = generatePlacement(prs, appInfoL, inclusterDeviceData, clusterData);
            }
            LOGGER.info(Events.PLACEMENT_ALGO_COMPLETED + " - " + System.currentTimeMillis());


            /**
             * 4.1 Generate Kubernetes commands to deploy it
             * 4.2 Deploy Kubernete and Istio Commands
             * 4.3 If LoadBalancing is enabled deploy DR and VS for this cluster and send details for other clusters as well.
             */
            if (distributedPlacement) {

                LOGGER.info(Events.DEPLOYMENT_STARTED + " - " + System.currentTimeMillis());

                Map<String, Map<String, DeploymentInfo>> perClusterDeploymentInfo = new HashMap<>(); // per cluster -> application -> deployment data
                generateBasicDeploymentInfo(placementOutput, perClusterDeploymentInfo, appInfo);
                if (lbEnabled) {
                    generateLoadBalancingRelatedDeploymentInfo(prs, perClusterDeploymentInfo, appInfo);
                }

                handleIncompletedPRs(placementOutput);

                sendDeployInfoToClusters(perClusterDeploymentInfo);

                LOGGER.info(Events.COMMAND_EXECUTION_COMPLETED_THIS_CLUSTER + " - " + System.currentTimeMillis());

            } else {
                //deploy ones in current cluster. For rest send deployment commands (both kubernetes, istio) to relevant clusters for deployment)
                LOGGER.info(Events.DEPLOYMENT_STARTED + " - " + System.currentTimeMillis());
                Map<String, Map<String, DeploymentInfo>> perClusterDeploymentInfo = new HashMap<>(); // cluster -> <application,microservice deployment>


//                deploymentInfoForPlacedMicroservicesPerCluster(placementOutput, perClusterInfo);
//                //todo update rest of the stuff in deployment info
//                for(String application : placementOutput.getPlacement().keySet()){
//                    for(String cluster:placementOutput.GetAccessClusters(application)){
//                            perClusterInfo.get(cluster).get(application).setAccessCluster(true);
//                    }
//                }
                generateBasicDeploymentInfo(placementOutput, perClusterDeploymentInfo, appInfo);
                if (lbEnabled) {
                    generateLoadBalancingRelatedDeploymentInfo(prs, perClusterDeploymentInfo, appInfo);
                }

                LOGGER.info(Events.COMMAND_FORWARDING_START + " - " + System.currentTimeMillis());

                sendDeployInfoToClusters(perClusterDeploymentInfo);

            }

            LOGGER.info(Events.PLACEMENT_COMPLETED + " - " + System.currentTimeMillis());
        }

    }

    private void sendDeployInfoToClusters(Map<String, Map<String, DeploymentInfo>> perClusterInfo) {
        if (!perClusterInfo.isEmpty()) {
            DeployResourceDataSet deployResourceDataSet = new DeployResourceDataSet();
            for (String cluster : perClusterInfo.keySet()) {
                List<DeploymentInfo> deploymentInfos = new ArrayList<>();
                for (String m : perClusterInfo.get(cluster).keySet())
                    deploymentInfos.add(perClusterInfo.get(cluster).get(m));

                DeployResource deployResource = new DeployResource(forwardUrl, cluster);
                deployResourceDataSet.addRequestToList(deployResource, deploymentInfos);
            }

            List<String> reply = deployResourceDataSet.sendRequests(deploymentHandler).await().indefinitely();
            long t = System.currentTimeMillis();
            LOGGER.info("Replies " + reply.toString());
            LOGGER.info(Events.COMMAND_EXECUTION_COMPLETED_ALL_CLUSTER + " - " + t);
        }
    }

    private void generateBasicDeploymentInfo(PlacementOutPut placementOutput, Map<String, Map<String, DeploymentInfo>> perClusterInfo, Map<String, Application> appInfo) {
        LOGGER.info(Events.DEPLOYMENT_STARTED + " - " + System.currentTimeMillis());

        /**
         * For microservice instances placed within this cluster
         */
        deploymentInfoForPlacedMicroservicesPerCluster(placementOutput, perClusterInfo);
        //todo update rest of the stuff in deployment info
//        for(String application : placementOutput.getPlacement().keySet()){
//            for(String cluster:placementOutput.GetAccessClusters(application)){
//                if(cluster.equals(clusterName))
//                perClusterInfo.get(cluster).get(application).setAccessCluster(true);
//            }
//        }

        for (String applicationId : placementOutput.getPlacement().keySet()) {
            for (String cluster : placementOutput.GetAccessClusters(applicationId)) {
                if(distributedPlacement && !perClusterInfo.containsKey(cluster))
                    continue;
                else {
                    if(!perClusterInfo.containsKey(cluster))
                        perClusterInfo.put(cluster,new HashMap<>());
                    if(!perClusterInfo.get(cluster).containsKey(applicationId)){
                        perClusterInfo.get(cluster).put(applicationId, new DeploymentInfo(applicationId, false));
                    }
                    perClusterInfo.get(cluster).get(applicationId).setAccessCluster(true);
                }
            }
        }

        if (distributedPlacement) {
            for (PlacementRequest pr : placementOutput.getIncompletePRs()) {

                Application application = appInfo.get(pr.getApplicationId());

                // placed on this device
                Map<String, Placement> placementPerApp = new HashMap<>();
                if (placementOutput.getPlacement().containsKey(pr.getApplicationId()))
                    placementPerApp = placementOutput.getPlacement().get(pr.getApplicationId());

                List<String> incomplete = new ArrayList<>();
                for (PlacedMicroservice placedMicroservice : pr.placedMicroservicesList) {
                    if (!placedMicroservice.placementCompleted())
                        incomplete.add(placedMicroservice.getMicroserviceName());
                }

                //According to  the generated PlacmentOutput  no microservices can be placed in cluster and no microservices are placed in any cluster yet.
                // But this is the access cluster and requires GW and VS deployments
                if (pr.placedMicroservicesList.isEmpty() && pr.entryClusters.contains(clusterName)) {
                    if (!perClusterInfo.containsKey(clusterName))
                        perClusterInfo.put(clusterName, new HashMap<>());
                    if (!perClusterInfo.get(clusterName).containsKey(application.getApplicationId()))
                        perClusterInfo.get(clusterName).put(application.getApplicationId(), new DeploymentInfo(application.getApplicationId(), false));
                    perClusterInfo.get(clusterName).get(application.getApplicationId()).setAccessCluster(true);

                }


                for (PlacedMicroservice placedMicroservice : pr.placedMicroservicesList) {
                    if (microServicePlacedInThisCluster(placementPerApp, placedMicroservice.getMicroserviceName())) {
                        continue;
                    } else {
                        //For scenarios where multiple horizontal instance of a microservice needs to be placed
                        // if not placed in cluster and not completed -> then place service level resources of the microservice
                        //and also service level for all upstream microservices are required
                        //they are also store in PR for load balancing resource placement in the future
                        if (!placedMicroservice.placementCompleted()) {
                            String microservice = placedMicroservice.getMicroserviceName();
                            Microservice m = application.getMicroserviceById(microservice);
                            LOGGER.info("Getting service level commands for microservice " + m.getMicroserviceId());
                            //add microservice to deployment
                            if (!perClusterInfo.containsKey(clusterName))
                                perClusterInfo.put(clusterName, new HashMap<>());
                            if (!perClusterInfo.get(clusterName).containsKey(pr.getApplicationId()))
                                perClusterInfo.get(clusterName).put(pr.getApplicationId(), new DeploymentInfo(pr.getApplicationId(), false));
                            perClusterInfo.get(clusterName).get(pr.getApplicationId()).addAdditionaMForSLevel(microservice);
                            //
                            pr.addSvcOnly(microservice, clusterName);

                            //add upstream microservice to the deployment
                            List<String> um = getUpstreamMicroservices(m, application, placementPerApp);
                            perClusterInfo.get(clusterName).get(pr.getApplicationId()).addAdditionaMForSLevel(um);
                            //
                            pr.addSvcOnly( um, clusterName);
                        }
                        //For scenarios where no microservices are placed in this cluster and all placed microservices are complete, place service level resources of upstream uncompleted
                        else {
                            //add upstream microservice to the deployment
                            String microservice = placedMicroservice.getMicroserviceName();
                            Microservice m = application.getMicroserviceById(microservice);
                            List<String> um = getIncompleUpstreamM(m, application, placementPerApp, incomplete);
                            if (!perClusterInfo.containsKey(clusterName))
                                perClusterInfo.put(clusterName, new HashMap<>());
                            if (!perClusterInfo.get(clusterName).containsKey(pr.getApplicationId()))
                                perClusterInfo.get(clusterName).put(pr.getApplicationId(), new DeploymentInfo(pr.getApplicationId(), false));
                            perClusterInfo.get(clusterName).get(pr.getApplicationId()).addAdditionaMForSLevel(um);
                            //
                            pr.addSvcOnly(microservice,clusterName);
                        }
                    }
                }
            }
        }

    }

    private void deploymentInfoForPlacedMicroservicesPerCluster(PlacementOutPut placementOutput, Map<String, Map<String, DeploymentInfo>> perClusterInfo) {
        for (String application : placementOutput.getPlacement().keySet()) {
            for (String m : placementOutput.getPlacement().get(application).keySet()) {
                for (String cluster : placementOutput.getPlacement().get(application).get(m).getDeviceMapping().keySet()) {
                    LOGGER.info("Adding commands to cluster " + cluster);
                    Map<String, Map<String, Integer>> deviceMapping = new HashMap<>();
                    deviceMapping.put(cluster, placementOutput.getPlacement().get(application).get(m).getDeviceMapping(cluster));
                    Placement p = new Placement(m, deviceMapping);
                    if (!perClusterInfo.containsKey(cluster))
                        perClusterInfo.put(cluster, new HashMap<>());
                    if (!perClusterInfo.get(cluster).containsKey(application))
                        perClusterInfo.get(cluster).put(application, new DeploymentInfo(application, false));
                    perClusterInfo.get(cluster).get(application).addMicroservicesToDeploy(p);
                }
            }
        }
    }


    private void generateLoadBalancingRelatedDeploymentInfo(List<PlacementRequest> prs, Map<String, Map<String, DeploymentInfo>> perClusterInfo, Map<String, Application> appInfo) {
        for (PlacementRequest pr : prs) {
            List<PlacedMicroservice> placedMicroservicesList = pr.getPlacedMicroservicesList();
            System.out.println("PLACED FROM PR : " + placedMicroservicesList.toString());
            Application application = appInfo.get(pr.getApplicationId());
            List<Microservice> sortedMicroservices = application.getSortedDAG();
            Map<String, PlacedMicroservice> placedMmap = new HashMap<>();
            for (PlacedMicroservice m : placedMicroservicesList) {
                placedMmap.put(m.getMicroserviceName(), m);
            }

            // to establish communication between two microservices in non-adjacent clusters
            Map<String, PlacedServiceOnly> placedSmap = new HashMap<>();
            for (PlacedServiceOnly m : pr.deployedServiceLvlOnly) {
                placedSmap.put(m.getMicroserviceName(), m);
            }

            List<String> lbCompleted = new ArrayList<>();
            for (Microservice m : sortedMicroservices) {
                // if not part of the placed microservices, no need to deploy load balancing resources yet.
                if (!placedMmap.containsKey(m.getMicroserviceId()))
                    continue;
                PlacedMicroservice placedM = placedMmap.get(m.getMicroserviceId());
                if (placedM.isLoadBalancingCompleted())
                    lbCompleted.add(m.getMicroserviceId());
                else {
                    if (isLBcompleted(application.getApplicationDAG().getConsumerMicroservices(m), placedMmap, placedM)) {
                        //generate weights
                        Map<String, Integer> lbWeights = loadBalancingPolicies.getWeights(placedM);
                        lbCompleted.add(placedM.getMicroserviceName());
                        placedM.setLoadBalancingCompleted(true);

                        // get clusters to send this data (clusters of m and also of its consumer microservices)
                        List<String> clusters = new ArrayList<>();
                        clusters.addAll(placedM.getPlacedClusters());

                        //**********
                        if(placedSmap.containsKey(m.getMicroserviceId()))
                            clusters.addAll(placedSmap.get(m).getPlacedClusters());

                        for (Microservice cm : application.getApplicationDAG().getConsumerMicroservices(m)) {
                            System.out.println("here1");
                            for (String cluster : placedMmap.get(cm.getMicroserviceId()).getPlacedClusters()) {
                                System.out.println("here2 " + cm.getMicroserviceId());
                                if (!clusters.contains(cluster))
                                    clusters.add(cluster);
                            }
                        }
                        for (String cluster : clusters) {
                            if (!perClusterInfo.containsKey(cluster)) {
                                perClusterInfo.put(cluster, new HashMap<>());
                            }

                            if (!perClusterInfo.get(cluster).containsKey(application.getApplicationId())) {
                                DeploymentInfo deploymentInfo = new DeploymentInfo(application.getApplicationId(), true);
                                deploymentInfo.addLBinfo(m.getMicroserviceId(), lbWeights);
                                perClusterInfo.get(cluster).put(application.getApplicationId(), deploymentInfo);
                            } else {
                                perClusterInfo.get(cluster).get(application.getApplicationId()).addLBinfo(m.getMicroserviceId(), lbWeights);
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isLBcompleted(List<Microservice> consumerMicroservices, Map<String, PlacedMicroservice> placedMmap, PlacedMicroservice placedM) {
        if (!placedM.placementCompleted())
            return false;
        for (Microservice m : consumerMicroservices) {
            if (placedMmap.containsKey(m.getMicroserviceId()) && placedMmap.get(m.getMicroserviceId()).isLoadBalancingCompleted())
                continue;
            else
                return false;
        }
        return true;
    }

    private void logCompletePRs(List<PlacementRequest> completedPRs) {
        StringBuilder s = new StringBuilder();
        s.append("COMPLETED PR IDs ");
        for (PlacementRequest pr : completedPRs)
            s.append(" - " + pr.prId);
        LOGGER.info(s);
    }

    private void requestOtherClusterData(Map<String, ClusterData> clusterData) {
        if (!distributedPlacement && isPrimaryCE) {
            LOGGER.info(Events.RETRIEVE_OTHER_CLUSTER_DATA_BEGIN.toString() + " - " + System.currentTimeMillis());
            //used for sending asynchronous requests to clusters and aggregating the results before continuing
            ClusterDataSet clusterDataSet = new ClusterDataSet();
            for (Cluster cluster : connectedClusters) {
                ClusterDataResource clusterDataResource = new ClusterDataResource(forwardUrl);
                clusterDataSet.addRequestToList(clusterDataResource, cluster.getClusterName());
            }
            List<ClusterData> clusterDataList = clusterDataSet.sendRequests().await().indefinitely();
            LOGGER.info("Reply for cluster device data received from all clusters");

            for (ClusterData c : clusterDataList) {
                clusterData.put(c.getClusterName(), c);
            }

        }
    }

    private List<ApplicationL> generateAppDataLight(Map<String, Application> appInfo) {
        List<ApplicationL> applicationL = new ArrayList<>();
        for (String appId : appInfo.keySet()) {
            Application app = appInfo.get(appId);
            applicationL.add(new ApplicationL(app.getApplicationId(), app.getMicroservicesL(),
                    app.getCompositeServicesL(), app.getDataflowsL(), app.getQoSParameters()));
        }
        return applicationL;
    }

    private void handleIncompletedPRs(PlacementOutPut placementOutput) {
        //todo send incomplete prs to next cluster or cloud following a particular policy.
        for (PlacementRequest pr : placementOutput.getIncompletePRs()) {
            pr.addTraversedCluster(clusterName);
            String nextCluster =forwardingPolicy.getNextCluster(connectedClusters,pr.getTraversedClusters());
            if(nextCluster!=null) {
                pr.setNextClusterToSend(nextCluster);
                forwardRequestQueue.addToQueue(pr);
            }
            else
                LOGGER.info("No forwarding cluster selected");
        }
    }


    private List<Command> getCommands(PlacementOutPut placementOutput, Map<String, Application> appInfo) {
        List<Command> commands = new ArrayList<>();

        /** 1. prs where some microservices are placed
         * 1.1 Istio GW , VS , Svc if this is an access cluster or just the (VS and Svc)
         * 1.2 For the rest VS, DR, Svc should deployed for both own microservice and any upstream ones
         *        a. ingress access requires GW and VS attached to it
         *        b. DR and VS is required when loadbalancing rules are configured.
         */
        for (PlacementRequest pr : placementOutput.getCompletedPRs()) {
            Application application = appInfo.get(pr.getApplicationId());
            // placed on this device
            Map<String, Placement> placementPerApp = placementOutput.getPlacement().get(pr.getApplicationId());

            List<Command> appLevelcommands = application.getAppLevelCommand();
            List<Command> serviceLevelCommands = new ArrayList<>();
            List<Command> microserviceLevelCommands = new ArrayList<>();
            commands.addAll(appLevelcommands);

            // microservice level : pod for the microservice
            // composite service level : to maintain connections service definitions of the upstream services should be available as well
            for (String microservice : placementPerApp.keySet()) {
                Placement microservicePlacement = placementPerApp.get(microservice);
                Microservice m = application.getMicroservice(microservice);
                microserviceLevelCommands.addAll(getMicroserviceLevelCommands(m, microservicePlacement.getDeviceMapping(clusterName)));
                //
                if (!application.getIngressMicroservices().contains(m))
                    serviceLevelCommands.addAll(m.getCommandsSLevelNoLB());
                else {
                    serviceLevelCommands.addAll(getServiceLevelForIngress(m, placementOutput.GetAccessClusters(application.getApplicationId())));
                }
                // for connectivity with upstream services
                serviceLevelCommands.addAll(getUpstreamCommands(m, application, placementPerApp));

                //todo update VS and DR following load balancing policy

            }

            commands.addAll(serviceLevelCommands);
            commands.addAll(microserviceLevelCommands);
        }

        for (PlacementRequest pr : placementOutput.getIncompletePRs()) {

            Application application = appInfo.get(pr.getApplicationId());

            List<Command> appLevelcommands = application.getAppLevelCommand();
            List<Command> serviceLevelCommands = new ArrayList<>();
            List<Command> microserviceLevelCommands = new ArrayList<>();
            commands.addAll(appLevelcommands);

            // placed on this device
            Map<String, Placement> placementPerApp = new HashMap<>();
            if (placementOutput.getPlacement().containsKey(pr.getApplicationId()))
                placementPerApp = placementOutput.getPlacement().get(pr.getApplicationId());

            List<String> incomplete = new ArrayList<>();
            for (PlacedMicroservice placedMicroservice : pr.placedMicroservicesList) {
                if (!placedMicroservice.placementCompleted())
                    incomplete.add(placedMicroservice.getMicroserviceName());
            }
            for (PlacedMicroservice placedMicroservice : pr.placedMicroservicesList) {
                if (microServicePlacedInThisCluster(placementPerApp, placedMicroservice.getMicroserviceName())) {
                    String microservice = placedMicroservice.getMicroserviceName();
                    Microservice m = application.getMicroserviceById(microservice);

                    microserviceLevelCommands.addAll(getMicroserviceLevelCommands(m, placementPerApp.get(microservice).getDeviceMapping(clusterName)));

                    if (!application.getIngressMicroservices().contains(m))
                        serviceLevelCommands.addAll(m.getCommandsSLevelNoLB());
                    else {
                        serviceLevelCommands.addAll(getServiceLevelForIngress(m, placementOutput.GetAccessClusters(application.getApplicationId())));
                    }
                    // for connectivity with upstream services
                    serviceLevelCommands.addAll(getUpstreamCommands(m, application, placementPerApp));
                } else {
                    //if not placed in cluster and not completed -> then place service level resources of the microservice
                    //and also service level for all upstream
                    if (!placedMicroservice.placementCompleted()) {
                        String microservice = placedMicroservice.getMicroserviceName();
                        Microservice m = application.getMicroserviceById(microservice);
                        LOGGER.info("Getting service level commands for microservice " + m.getMicroserviceId());
                        if (!application.getIngressMicroservices().contains(m))
                            serviceLevelCommands.addAll(m.getCommandsSLevelNoLB());
                        else {
                            serviceLevelCommands.addAll(getServiceLevelForIngress(m, placementOutput.GetAccessClusters(application.getApplicationId())));
                        }
                        // for connectivity with upstream services
                        serviceLevelCommands.addAll(getUpstreamCommands(m, application, placementPerApp));
                    }
                    //if not placed in cluster and complete, place service level resources of upstream uncompleted
                    else {
                        String microservice = placedMicroservice.getMicroserviceName();
                        Microservice m = application.getMicroserviceById(microservice);
                        serviceLevelCommands.addAll(getIncompleUpstreamCommands(m, application, placementPerApp, incomplete));
                    }
                }
            }
            commands.addAll(serviceLevelCommands);
            commands.addAll(microserviceLevelCommands);
        }

        return commands;

    }

    private Placement getPlacement(List<Placement> placementPerApp, String microservice) {
        Placement placement = null;
        for (Placement p : placementPerApp) {
            if (p.getMicroserviceId() == microservice)
                placement = p;
        }
        return placement;
    }

    private List<String> getIncompleUpstreamM(Microservice m, Application application, Map<String, Placement> placementPerApp, List<String> incomplete) {
        List<String> microservices = new ArrayList<>();
        //get upstream microservices for "m"
        List<Microservice> upstreamM = application.getApplicationDAG().getConsumedMicroservices(m);
        for (Microservice um : upstreamM) {
            if (incomplete.contains(um.getMicroserviceId()))
                continue;
            else if (microServicePlacedInThisCluster(placementPerApp, um.getMicroserviceId()))
                continue;
            else
                microservices.add(um.getMicroserviceId());
        }
        return microservices;
    }

    private List<Command> getIncompleUpstreamCommands(Microservice m, Application application, Map<String, Placement> placementPerApp, List<String> incomplete) {
        List<Command> serviceLevelCommands = new ArrayList<>();
        //get upstream microservices for "m"
        List<Microservice> upstreamM = application.getApplicationDAG().getConsumedMicroservices(m);
        for (Microservice um : upstreamM) {
            if (incomplete.contains(um.getMicroserviceId()))
                continue;
            else if (microServicePlacedInThisCluster(placementPerApp, um.getMicroserviceId()))
                continue;
            else
                serviceLevelCommands.addAll(um.getCommandsSLevelNoLB());
        }
        return serviceLevelCommands;
    }

    private List<Command> getServiceLevelForIngress(Microservice m, List<String> accessClusters) {
        LOGGER.info("ROOT Microservice " + m.getMicroserviceId() + " accessClusters : " + accessClusters);
        List<Command> serviceLevelCommands = new ArrayList<>();
        for (Command c : m.getCommandsSLevelAll()) {
            LOGGER.info("Command test : " + c);
            if (c.getCommandType().equals(CommandTypeEnum.CREATE_GW)) {
                LOGGER.info("GW command");
                //todo what about virtual service
                if (accessClusters.contains(commandHandler.getCurrentClusterName())) {
                    LOGGER.info("Adding GW command");
                    serviceLevelCommands.add(c);
                }
            } else if (c.getCommandType().equals(CommandTypeEnum.CREATE_VS)) {
                if (!lbEnabled)
                    serviceLevelCommands.add(c);
            } else if (c.getCommandType().equals(CommandTypeEnum.CREATE_DR)) {
                //will be handled with loadblancing
                continue;
            } else
                serviceLevelCommands.add(c);
        }
        return serviceLevelCommands;
    }

    private List<String> getUpstreamMicroservices(Microservice m, Application application, Map<String, Placement> placementPerApp) {
        List<String> microservices = new ArrayList<>();
        //get upstream microservices for "m"
        List<Microservice> upstreamM = application.getApplicationDAG().getConsumedMicroservices(m);
        for (Microservice um : upstreamM) {
            if (microServicePlacedInThisCluster(placementPerApp, um.getMicroserviceId()))
                continue;
            else
                microservices.add(um.getMicroserviceId());
        }
        return microservices;
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

    private PlacementOutPut generatePlacement(List<PlacementRequest> prs, Map<String, Application> appInfo, List<FogDevice> inClusterDevices, Map<String, ClusterData> adjacentClusterData) {
        // map of microservice to node name
        return placementAlgorithm.run(prs, appInfo, inClusterDevices, adjacentClusterData);
    }

    private PlacementOutPut generatePlacement(List<PlacementRequest> prs, List<ApplicationL> appInfo, List<FogDevice> inClusterDevices, Map<String, ClusterData> adjacentClusterData) {
        // map of microservice to node name
        return placementAlgorithm.run(prs, appInfo, inClusterDevices, adjacentClusterData);
    }



    // this is for incomplete prs
    public enum ResourceDeploymentLevel {
        ROOT_WITH_INGRESS("root_with_ingress"),
        ROOT_WITHOUT_INGRESS("root_without_ingress"),
        NON_ROOT("non_root");

        public final String label;

        ResourceDeploymentLevel(String label) {
            this.label = label;
        }
    }

    private Map<String, Application> loadRelatedAppInfo(List<PlacementRequest> prs, DataMapperMetadata dataMapper) {
        Map<String, Application> appInfo = new HashMap<>();
        for (PlacementRequest pr : prs) {
            Application application = dataMapper.getObjectFromBucket(pr.getApplicationId());
            appInfo.put(application.getApplicationId(), application);
        }
        return appInfo;
    }

    private List<FogDevice> loadDeviceData(CommandHandler kubeCommandHandler) {
        List<FogDevice> inClusterDevices = new ArrayList<>();
        inClusterDevices = kubeCommandHandler.getAllNodes();
        return inClusterDevices;
    }

}
