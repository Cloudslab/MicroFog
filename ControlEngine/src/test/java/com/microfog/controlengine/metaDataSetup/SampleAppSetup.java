//package com.microfog.scheduler.metaDataSetup;
//
//import com.microfog.scheduler.DataMapper;
//import com.microfog.scheduler.KubeCommandHandler;
//import com.microfog.scheduler.model.domainObjects.FogDevice;
//import com.microfog.scheduler.model.kubernetesResources.*;
//import io.fabric8.kubernetes.api.model.Node;
//import io.quarkus.test.junit.QuarkusTest;
//import io.smallrye.common.constraint.Assert;
//import org.junit.jupiter.api.Test;
//
//import javax.inject.Inject;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
//@QuarkusTest
//public class SampleAppSetup {
//    @Inject
//    KubeCommandHandler kubeCommandHandler;
////    DataMapper dataMapper;
//
//    @Test
//    public void testCommandMetaDataAddDeploy() {
//        /**
//         * Sample application (Helloworld, contains 3 microservices -> Sleep, HelloWorld_V1 and Helloworld_V2
//         * Meta Data:
//         * Application ID : APP_HW
//         */
////        dataMapper.deleteByKey("APP_HW_Commands",DataMapper.LIST);
//         // create namespace: kubectl create namespace sample
//         // label namespace: kubectl label namespace sample istio-injection=enabled
//        Map<String, Node> nodes = kubeCommandHandler.getAllNodeData();
//        Map<String,String> nsLabels = new HashMap<>();
//        nsLabels.put("istio-injection","enabled");
//        Namespace ns = new Namespace("sample2", nsLabels);
//        Command command1 = new Command("APP_HW",CommandTypeEnum.CREATE_NS, CommandLevelEnum.APP_LEVEL,KubeResourceEnum.NS,ns);
//        Assert.assertTrue(kubeCommandHandler.executeCommand(command1));
//        // create service: kubectl apply -f <yaml path> -l service=<serviceName> -n <namesapce>
////        Service service = new Service("/Users/samodha/Practical/MicroFog/Schedule/src/main/java/com/microfog/scheduler/appResourceScripts/helloworld_service.yaml",ns.getName(),"HelloWorld_V1");
////        Command command2 = new Command(CommandTypeEnum.CREATE_S, KubeResourceEnum.SERVICE, service);
////
////        // create deployment for Helloworld V1: kubectl apply -f <yaml path for version 1> -n <namespace>
////        Deployment deployment = new Deployment("/Users/samodha/Practical/MicroFog/Schedule/src/main/java/com/microfog/scheduler/appResourceScripts/helloworld_deployment.yaml",ns.getName(),"HelloWorld_V1");
////        Command command3 = new Command(CommandTypeEnum.CREATE_M,KubeResourceEnum.DEPLOYMENT,deployment);
////
////        dataMapper.writeObjectAsList("APP_HW_Commands", command1);
////        dataMapper.writeObjectAsList("APP_HW_Commands", command2);
////        dataMapper.writeObjectAsList("APP_HW_Commands", command3);
////
////        List<Command> commands = dataMapper.getListObject("APP_HW_Commands");
////
////        for(Command c:commands) {
////            Assert.assertTrue(kubeCommandHandler.executeCommand(c));
////        }
////
////        dataMapper.deleteByKey("APP_HW_Commands",DataMapper.LIST);
//    }
//
//    @Test
//    public void testCommandMetaDataAddDeployToSelectedNode() {
//        /**
//         * Sample application (Helloworld, contains 3 microservices -> Sleep, HelloWorld_V1 and Helloworld_V2
//         * Meta Data:
//         * Application ID : APP_HW
//         */
////        dataMapper.deleteByKey("APP_HW_Commands",DataMapper.LIST);
////        // create namespace: kubectl create namespace sample
////        // label namespace: kubectl label namespace sample istio-injection=enabled
////        Map<String,String> nsLabels = new HashMap<>();
////        nsLabels.put("istio-injection","enabled");
////        Namespace ns = new Namespace("sample5", nsLabels);
////        Command command1 = new Command(CommandTypeEnum.CREATE_NS, KubeResourceEnum.NS,ns);
////
////        // create service: kubectl apply -f <yaml path> -l service=<serviceName> -n <namesapce>
////        Service service = new Service("/Users/samodha/Practical/MicroFog/Schedule/src/main/java/com/microfog/scheduler/appResourceScripts/helloworld_service.yaml",ns.getName(),"HelloWorld");
////        Command command2 = new Command(CommandTypeEnum.CREATE_S, KubeResourceEnum.SERVICE, service);
////
////        // create deployment for Helloworld V1: kubectl apply -f <yaml path for version 1> -n <namespace>
//////        Deployment deployment = new Deployment("/Users/samodha/Practical/MicroFog/Schedule/src/main/java/com/microfog/scheduler/appResourceScripts/helloworld_deployment.yaml",ns.getName(),"HelloWorld_V1");
//////        Command command3 = new Command(CommandTypeEnum.CREATE_M,KubeResourceEnum.DEPLOYMENT,deployment);
////
////        Pod pod1 = new Pod("/Users/samodha/Practical/MicroFog/Schedule/src/main/java/com/microfog/scheduler/appResourceScripts/helloworldV1.yaml",ns.getName(),"Helloworld_V1");
////        Command command3 = new Command(CommandTypeEnum.CREATE_M,KubeResourceEnum.POD,pod1);
////
////        Pod pod2 = new Pod("/Users/samodha/Practical/MicroFog/Schedule/src/main/java/com/microfog/scheduler/appResourceScripts/helloworldV2.yaml",ns.getName(),"Helloworld_V2");
////        Command command4 = new Command(CommandTypeEnum.CREATE_M,KubeResourceEnum.POD,pod2);
////
////        Service service2 = new Service("/Users/samodha/Practical/MicroFog/Schedule/src/main/java/com/microfog/scheduler/appResourceScripts/sleep_service.yaml",ns.getName(),"Sleep");
////        Command command5 = new Command(CommandTypeEnum.CREATE_S, KubeResourceEnum.SERVICE, service2);
////
////        Pod pod3 = new Pod("/Users/samodha/Practical/MicroFog/Schedule/src/main/java/com/microfog/scheduler/appResourceScripts/sleep_pod.yaml",ns.getName(),"Sleep");
////        Command command6 = new Command(CommandTypeEnum.CREATE_M,KubeResourceEnum.POD,pod3);
////
////        dataMapper.writeObjectAsList("APP_HW_Commands", command1);
////        dataMapper.writeObjectAsList("APP_HW_Commands", command2);
////        dataMapper.writeObjectAsList("APP_HW_Commands", command3);
////        dataMapper.writeObjectAsList("APP_HW_Commands", command4);
////        dataMapper.writeObjectAsList("APP_HW_Commands", command5);
////        dataMapper.writeObjectAsList("APP_HW_Commands", command6);
////
////        List<Command> commands = dataMapper.getListObject("APP_HW_Commands");
////
////        List<FogDevice> nodes = kubeCommandHandler.getAllNodes();
////        List<String> hostnames = nodes.stream().map(FogDevice::getNodeName).collect(Collectors.toList());
////        System.out.println(hostnames.get(0) + ", " + hostnames.get(1) + ", " + hostnames.get(2));
////        int i =0;
////        for(Command c:commands){
////            if(c.getResourceType()==KubeResourceEnum.POD){
////                if(hostnames.get(i).contains("control-plane"))
////                    hostnames.add(i,hostnames.get(2));
////                Pod pod = new Pod((Pod) c.getResource(),hostnames.get(i));
////                i++;
////                Assert.assertTrue(kubeCommandHandler.
////                        executeCommand(new Command(c.getCommandType(),c.getResourceType(),pod)));
////            }
////            else{
////                Assert.assertTrue(kubeCommandHandler.executeCommand(c));
////            }
////        }
////
////        dataMapper.deleteByKey("APP_HW_Commands",DataMapper.LIST);
////        dataMapper.disconnectRedisClient();
//    }
//
//
//
//}
