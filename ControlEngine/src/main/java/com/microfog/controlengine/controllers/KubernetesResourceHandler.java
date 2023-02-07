package com.microfog.controlengine.controllers;

import com.microfog.controlengine.utils.Events;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.metrics.v1beta1.NodeMetrics;
import io.fabric8.kubernetes.api.model.rbac.Role;
import io.fabric8.kubernetes.api.model.rbac.RoleBinding;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.PodResource;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.ServiceResource;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logging.Logger;

import javax.inject.Singleton;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Singleton
public class KubernetesResourceHandler {
    private static final Logger LOGGER = Logger.getLogger(KubernetesResourceHandler.class);

    KubernetesClient kubernetesClient = new KubernetesClientBuilder().build();

    public boolean createService(String filepath, String namespace) {
        File serviceYaml = new File(filepath);
        Service service = kubernetesClient.services().load(serviceYaml).get();
        try {
            service = kubernetesClient.services().inNamespace(namespace).create(service);
            LOGGER.info("Created service with name " + service.getMetadata().getName() + " - " + System.currentTimeMillis());
            return true;
        } catch (KubernetesClientException e) {
            LOGGER.info("Exception occurred in service creation: " + e.getLocalizedMessage());
            return false;
        }
    }

    public boolean createService(URL filepath, String namespace) {
        LOGGER.info(Events.LOAD_YAML_FILE_BEGIN + " - " + System.currentTimeMillis());
        ServiceResource<Service> serviceResource = kubernetesClient.services().load(filepath);
        LOGGER.info(Events.LOAD_YAML_FILE_COMPLETED + " - " + System.currentTimeMillis());
        Service service = serviceResource.get();
        try {
            service = kubernetesClient.services().inNamespace(namespace).resource(service).create();
            LOGGER.info("Created service with name " + service.getMetadata().getName()+ " - " + System.currentTimeMillis());
            return true;
        } catch (KubernetesClientException e) {
            LOGGER.info("Exception occurred in service creation: " + e.getLocalizedMessage());
            return false;
        }
    }

    public boolean createDeployment(String filepath, String namespace) {
        File deploymentYaml = new File(filepath);
        Deployment deployment = kubernetesClient.apps().deployments().load(deploymentYaml).get();
        try {
            deployment = kubernetesClient.apps().deployments().inNamespace(namespace).create(deployment);
            LOGGER.info("Created deployment with name " + deployment.getMetadata().getName()+ " - " + System.currentTimeMillis());
            return true;
        } catch (KubernetesClientException e) {
            LOGGER.info("Exception occurred in pod creation: " + e.getLocalizedMessage());
            return false;
        }
    }

    public boolean createDeployment(URL filepath, String namespace) {
        Deployment deployment = kubernetesClient.apps().deployments().load(filepath).get();
        try {
            deployment = kubernetesClient.apps().deployments().inNamespace(namespace).create(deployment);
            LOGGER.info("Created deployment with name " + deployment.getMetadata().getName()+ " - " + System.currentTimeMillis());
            return true;
        } catch (KubernetesClientException e) {
            LOGGER.info("Exception occurred in pod creation: " + e.getLocalizedMessage());
            return false;
        }
    }

    public boolean createPod(String filepath, String namespace, String nodeName) {
        File podYaml = new File(filepath);
        Pod pod = kubernetesClient.pods().load(podYaml).get();
        try {
            //set nodes
            pod.getSpec().setNodeName(nodeName);
            pod = kubernetesClient.pods().inNamespace(namespace).create(pod);
            LOGGER.info("Created pod with name " + pod.getMetadata().getName() + " on node " + pod.getSpec().getNodeName()
                    + " - " + System.currentTimeMillis());
            return true;
        } catch (KubernetesClientException e) {
            LOGGER.info("Exception occurred in pod creation: " + e.getLocalizedMessage());
            return false;
        }
    }

    // we assume that pod contains only one microservice at this point. No sidecars
    public boolean createPod(URL filepath, String namespace, String nodeName, Map<String, BigDecimal> requestedResources) {
        LOGGER.info("PASSED NODE NAME FOR POD " + nodeName);
        LOGGER.info(Events.LOAD_YAML_FILE_BEGIN + " - " + System.currentTimeMillis());
        PodResource podResource = kubernetesClient.pods().load(filepath);
        LOGGER.info(Events.LOAD_YAML_FILE_COMPLETED + " - " + System.currentTimeMillis());
        Pod pod = podResource.get();
        try {
            //set nodes
            pod.getSpec().setNodeName(nodeName);
            //set requests
            if (!requestedResources.isEmpty() && pod.getSpec().getContainers().size() == 1) {
                Map<String, Quantity> requestedQuantities = new HashMap<>();
                for (String rName : requestedResources.keySet())
                    requestedQuantities.put(rName, new Quantity(requestedResources.get(rName).toString()));

                if (pod.getSpec().getContainers().get(0).getResources() == null)
                    pod.getSpec().getContainers().get(0).setResources(new ResourceRequirements());
                pod.getSpec().getContainers().get(0).getResources().setRequests(requestedQuantities);
            }
            //update name to allow horizontal instances
            String name = pod.getMetadata().getName();
            System.out.println("POD NAME " + name);
            pod.getMetadata().setName(name+"-"+nodeName);

            //add node name as label to be used for load balancing purposes
            Map<String,String> labels = pod.getMetadata().getLabels();
            labels.put("nodeName",nodeName);
            pod.getMetadata().setLabels(labels);

//            kubernetesClient.pods().inNamespace(nodeName).withName(pod.getMetadata().getName()).create();
            pod = kubernetesClient.pods().inNamespace(namespace).resource(pod).create();

            LOGGER.info("Created pod with name " + pod.getMetadata().getName() + " on node " + pod.getSpec().getNodeName()
                    + "with label - nodeName - set to : " + pod.getMetadata().getLabels().get("nodeName")
                    + " - " + System.currentTimeMillis());
            return true;
        } catch (KubernetesClientException e) {
            LOGGER.info("Exception occurred in pod creation: " + e.getLocalizedMessage());
            return false;
        }
    }

    public boolean createPod(String filepath, String namespace) {
        File podYaml = new File(filepath);
        Pod pod = kubernetesClient.pods().load(podYaml).get();
        try {
            pod = kubernetesClient.pods().inNamespace(namespace).create(pod);
            LOGGER.info("Created pod with name " + pod.getMetadata().getName()+ " - " + System.currentTimeMillis());
            return true;
        } catch (KubernetesClientException e) {
            LOGGER.info("Exception occurred in pod creation: " + e.getLocalizedMessage());
            return false;
        }
    }

    public boolean createPod(URL filepath, String namespace) {
        Pod pod = kubernetesClient.pods().load(filepath).get();
        try {
            pod = kubernetesClient.pods().inNamespace(namespace).create(pod);
            LOGGER.info("Created pod with name " + pod.getMetadata().getName()+ " - " + System.currentTimeMillis());
            return true;
        } catch (KubernetesClientException e) {
            LOGGER.info("Exception occurred in pod creation: " + e.getLocalizedMessage());
            return false;
        }
    }

    public boolean createNameSpace(String namespace, Map<String, String> labels) {
        try {
//            if(kubernetesClient.namespaces().withName(namespace)!=null)
//                return true;

            // Creating namespace
            Namespace ns = new NamespaceBuilder().withNewMetadata().withName(namespace).addToLabels(labels).endMetadata()
                    .build();
            ns = kubernetesClient.namespaces().resource(ns).create();
            LOGGER.info("Created namespace:" + ns.getMetadata().getName()+ " - " + System.currentTimeMillis());
            return true;
        } catch (KubernetesClientException e) {
            LOGGER.info("Exception occurred in service creation: " + e.getLocalizedMessage());
            return false;
        }
    }

    public Map<String, Node> getClusterNodes() {
        Map<String, Node> nodeMap = kubernetesClient.nodes().list().getItems()
                .stream()
                .collect(Collectors.toMap(node -> node.getMetadata().getName(), Function.identity()));

        return nodeMap;

    }


    public Map<String, Map<String, Quantity>> getResourceUsage() {

        Map<String, NodeMetrics> nodeMetrics = kubernetesClient.top().nodes().metrics().getItems()
                .stream()
                .collect(Collectors.toMap(node -> node.getMetadata().getName(), Function.identity()));

        Map<String, Map<String, Quantity>> usageMap = new HashMap<>();

        for (String nodeName : nodeMetrics.keySet()) {
            usageMap.put(nodeName, nodeMetrics.get(nodeName).getUsage());
        }
        return usageMap;
    }

    public Map<String, Map<String, BigDecimal>> getAllocatedResources() {
        Map<String, Map<String, BigDecimal>> perNodeResources = new HashMap<>();
        for (Pod item : kubernetesClient.pods().inAnyNamespace().list().getItems()) {
            String nodeName = item.getSpec().getNodeName();
            for (Container container : item.getSpec().getContainers()) {
                BigDecimal cpu = null;
                if (container.getResources().getRequests().get("cpu") != null)
                    cpu = container.getResources().getRequests().get("cpu").getNumericalAmount();
                if (cpu != null) {
                    if (perNodeResources.containsKey(nodeName)) {
                        if (perNodeResources.get(nodeName).containsKey("cpu"))
                            perNodeResources.get(nodeName).put("cpu", perNodeResources.get(nodeName).get("cpu").add(cpu));
                        else
                            perNodeResources.get(nodeName).put("cpu", cpu);
                    } else {
                        Map<String, BigDecimal> m = new HashMap<>();
                        m.put("cpu", cpu);
                        perNodeResources.put(nodeName, m);
                    }
                }
                BigDecimal memory = null;
                if (container.getResources().getRequests().get("memory") != null)
                    memory = container.getResources().getRequests().get("memory").getNumericalAmount();
                if (memory != null) {
                    if (perNodeResources.containsKey(nodeName)) {
                        if (perNodeResources.get(nodeName).containsKey("memory"))
                            perNodeResources.get(nodeName).put("memory", perNodeResources.get(nodeName).get("memory").add(memory));
                        else
                            perNodeResources.get(nodeName).put("memory", memory);
                    } else {
                        Map<String, BigDecimal> m = new HashMap<>();
                        m.put("memory", memory);
                        perNodeResources.put(nodeName, m);
                    }
                }
            }
        }
        //set scale
        for (String key:perNodeResources.keySet()){
            for(String key2:perNodeResources.get(key).keySet()){
                perNodeResources.get(key).get(key2).setScale(2, RoundingMode.CEILING);
            }
        }
        return perNodeResources;
    }


    public boolean createRole(URL filepath, String namespace) {
        LOGGER.info(Events.LOAD_YAML_FILE_BEGIN + " - " + System.currentTimeMillis());
        Resource<Role> roleResource = kubernetesClient.rbac().roles().load(filepath);
        LOGGER.info(Events.LOAD_YAML_FILE_COMPLETED + " - " + System.currentTimeMillis());
        Role role = roleResource.get();
        try {
            role = kubernetesClient.rbac().roles().inNamespace(namespace).resource(role).create();
            LOGGER.info("Created role with name " + role.getMetadata().getName()+ " - " + System.currentTimeMillis());
            return true;
        } catch (KubernetesClientException e) {
            LOGGER.info("Exception occurred in role creation: " + e.getLocalizedMessage());
            return false;
        }
    }

    public boolean createRoleBinding(URL filepath, String namespace) {
        LOGGER.info(Events.LOAD_YAML_FILE_BEGIN + " - " + System.currentTimeMillis());
        Resource<RoleBinding> roleBindingResource = kubernetesClient.rbac().roleBindings().load(filepath);
        LOGGER.info(Events.LOAD_YAML_FILE_COMPLETED + " - " + System.currentTimeMillis());
        RoleBinding roleBinding =roleBindingResource.get();
        try {
            roleBinding = kubernetesClient.rbac().roleBindings().inNamespace(namespace).resource(roleBinding).create();
            LOGGER.info("Created rolebinding with name " + roleBinding.getMetadata().getName()+ " - " + System.currentTimeMillis());
            return true;
        } catch (KubernetesClientException e) {
            LOGGER.info("Exception occurred in rolebinding creation: " + e.getLocalizedMessage());
            return false;
        }
    }

    public String getClusterName() {
        return ConfigProvider.getConfig().getValue("cluster.name", String.class);
    }

    public boolean createConfigMap(URL yamlUrl, String namespace) {
        LOGGER.info(Events.LOAD_YAML_FILE_BEGIN + " - " + System.currentTimeMillis());
        Resource<ConfigMap> configMapResource = kubernetesClient.configMaps().load(yamlUrl);
        LOGGER.info(Events.LOAD_YAML_FILE_COMPLETED + " - " + System.currentTimeMillis());
        ConfigMap configMap = configMapResource.get();
        try {
            configMap = kubernetesClient.configMaps().inNamespace(namespace).resource(configMap).create();
            LOGGER.info("Created configmap with name " + configMap.getMetadata().getName()+ " - " + System.currentTimeMillis());
            return true;
        } catch (KubernetesClientException e) {
            LOGGER.info("Exception occurred in configmap creation: " + e.getLocalizedMessage());
            return false;
        }
    }
}
