//package com.microfog.controlengine;
//
//import com.microfog.controlengine.controllers.KubernetesResourceHandler;
//import io.fabric8.kubernetes.api.model.Container;
//import io.fabric8.kubernetes.api.model.Node;
//import io.fabric8.kubernetes.api.model.Pod;
//import io.fabric8.kubernetes.client.KubernetesClient;
//import io.fabric8.kubernetes.client.KubernetesClientBuilder;
//import io.fabric8.kubernetes.client.dsl.Resource;
//import io.quarkus.test.junit.QuarkusTest;
//import org.junit.jupiter.api.Test;
//
//import javax.inject.Inject;
//import java.math.BigDecimal;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.logging.Handler;
//
//@QuarkusTest
//public class KubeAPITest {
//
//
//    @Test
//    public void testKubeHandler(){
//        KubernetesClient kubernetesClient = new KubernetesClientBuilder().build();
//        Resource<Node> nodeResource = kubernetesClient.nodes().withName("edge2-worker");
//        Map<String, Map<String,BigDecimal>> perNodeResources = new HashMap<>();
//        for (Pod item : kubernetesClient.pods().list().getItems()) {
//            String nodeName = item.getSpec().getNodeName();
//            for(Container container:item.getSpec().getContainers()){
//                BigDecimal cpu = container.getResources().getRequests().get("cpu").getNumericalAmount();
//                if(cpu !=null) {
//                    if (perNodeResources.containsKey(nodeName)) {
//                        if (perNodeResources.get(nodeName).containsKey("cpu"))
//                            perNodeResources.get(nodeName).get("cpu").add(cpu);
//                        else
//                            perNodeResources.get(nodeName).put("cpu", cpu);
//                    } else {
//                        Map<String, BigDecimal> m = new HashMap<>();
//                        m.put("cpu", cpu);
//                        perNodeResources.put(nodeName, m);
//                    }
//                }
//                BigDecimal memory = container.getResources().getRequests().get("memory").getNumericalAmount();
//                if(memory !=null) {
//                    if (perNodeResources.containsKey(nodeName)) {
//                        if (perNodeResources.get(nodeName).containsKey("memory"))
//                            perNodeResources.get(nodeName).get("memory").add(memory);
//                        else
//                            perNodeResources.get(nodeName).put("memory", memory);
//                    } else {
//                        Map<String, BigDecimal> m = new HashMap<>();
//                        m.put("memory", memory);
//                        perNodeResources.put(nodeName, m);
//                    }
//                }
//            }
//        }
//
//        System.out.println("");
//    }
//}
