package com.microfog.controlengine.controllers;

import com.microfog.controlengine.model.domainObjects.FogDevice;
import com.microfog.controlengine.model.deployment.*;
import io.fabric8.kubernetes.api.model.Node;
import io.fabric8.kubernetes.api.model.Quantity;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class CommandHandler {

    @Inject
    KubernetesResourceHandler kubernetesResourceHandler;

    @Inject
    IstioResourceHandler istioResourceHandler;

    public boolean executeCommand(Command command) {
        switch (command.getCommandType()) {
            case CREATE_NS:
                switch (command.getResourceType()) {
                    case NS:
                        Namespace ns = (Namespace) command.getResource();
                        return kubernetesResourceHandler.createNameSpace(ns.getName(), ns.getLabels());
                }
            case CREATE_CONFIGMAP:
                switch (command.getResourceType()) {
                    case CONFIGMAP:
                        ConfigMap configMap = (ConfigMap) command.getResource();
                        return kubernetesResourceHandler.createConfigMap(configMap.getYamlUrl(), configMap.getNamespace());

                }
            case CREATE_PERMISSIONS:
                switch (command.getResourceType()) {
                    case ROLE:
                        Role role = (Role) command.getResource();
                        return kubernetesResourceHandler.createRole(role.getYamlUrl(), role.getNamespace());
                    case ROLEBINDING:
                        Rolebinding rolebinding = (Rolebinding) command.getResource();
                        return kubernetesResourceHandler.createRoleBinding(rolebinding.getYamlUrl(), rolebinding.getNamespace());
                }
            case CREATE_S:
                switch (command.getResourceType()) {
                    case SERVICE:
                        Service service = (Service) command.getResource();
                        return kubernetesResourceHandler.createService(service.getYamlUrl(), service.getNamespace());
                }
            case CREATE_M:
                switch (command.getResourceType()) {
                    case DEPLOYMENT:
                        Deployment deployment = (Deployment) command.getResource();
                        return kubernetesResourceHandler.createDeployment(deployment.getYamlUrl(), deployment.getNamespace());
                    case POD:
                        Pod pod = (Pod) command.getResource();
                        if (pod.modifyNode()) {
                            Boolean status = true;
                            System.out.println("POD GET NODE NAMES : " + pod.getNodeNames().toString());
                            for (String nodeName : pod.getNodeNames()) {
                                System.out.println("POD SLECTED NODE NAME : " + nodeName );
                                status = status && kubernetesResourceHandler.createPod(pod.getYamlUrl(), pod.getNamespace(), nodeName, pod.getRequestedResources(nodeName));
                            }
                            return status;
                        } else
                            return kubernetesResourceHandler.createPod(pod.getYamlUrl(), pod.getNamespace());
                }
            case CREATE_VS:
                switch (command.getResourceType()) {
                    case VS:
                        IstioVirtualService vs = (IstioVirtualService) command.getResource();
//                        if (vs.getRouteProtocol() != null && !vs.getRoutes().isEmpty())
                            return istioResourceHandler.createVirtualService(vs.getYamlUrl(), vs.getNamespace(), vs.getRoutes(), vs.getRouteProtocol());
//                        return istioResourceHandler.createVirtualService(vs.getYamlUrl(), vs.getNamespace());
                }
            case CREATE_GW:
                switch (command.getResourceType()) {
                    case GW:
                        Gateway gw = (Gateway) command.getResource();
                        return istioResourceHandler.createGateway(gw.getYamlUrl(), gw.getNamespace());
                }
            case CREATE_DR:
                switch (command.getResourceType()) {
                    case DR:
                        IstioDestinationRule dr = (IstioDestinationRule) command.getResource();
//                        if (!dr.getSubsets().isEmpty())
                            return istioResourceHandler.createDestinationRule(dr.getYamlUrl(), dr.getNamespace(), dr.getSubsets());
//                        return istioResourceHandler.createDestinationRule(dr.getYamlUrl(), dr.getNamespace());
                }

            default:
                return false;
        }
    }

    public Map<String, Node> getAllNodeData() {
        return kubernetesResourceHandler.getClusterNodes();
    }

    public List<FogDevice> getAllNodes() {
        Map<String, Node> nodeData = kubernetesResourceHandler.getClusterNodes();
        Map<String, Map<String, Quantity>> nodeUsage = kubernetesResourceHandler.getResourceUsage();
        Map<String, Map<String, BigDecimal>> allocatedResources = kubernetesResourceHandler.getAllocatedResources();
        System.out.println("MAP of Allocated Resources : " + allocatedResources);

        List<FogDevice> nodesInCluster = new ArrayList<>();

        for (String nodeName : nodeData.keySet()) {
            Node node = nodeData.get(nodeName);

            Map<String, BigDecimal> resourcesTotal = new HashMap<>();
            Map<String, BigDecimal> resourcesUsed = new HashMap<>();
            Map<String, BigDecimal> resourcesAllocated = new HashMap<>();
            if (allocatedResources.containsKey(nodeName))
                resourcesAllocated = allocatedResources.get(nodeName); // requested by pods.
            if (!resourcesAllocated.containsKey("cpu"))
                resourcesAllocated.put("cpu", new BigDecimal(0.00).setScale(2));
            if (!resourcesAllocated.containsKey("memory"))
                resourcesAllocated.put("memory", new BigDecimal(0.00).setScale(2));

            //allocatable for pods: total amount (capacity)
            Quantity cpuAloc = node.getStatus().getAllocatable().get("cpu");
            Quantity memoryAloc = node.getStatus().getAllocatable().get("memory");
            resourcesTotal.put("cpu", Quantity.getAmountInBytes(cpuAloc).setScale(2, RoundingMode.FLOOR));
            resourcesTotal.put("memory", Quantity.getAmountInBytes(memoryAloc).setScale(2, RoundingMode.FLOOR));

            //current used amount
            Quantity cpuUsed = nodeUsage.get(nodeName).get("cpu");
            Quantity memoryUsed = nodeUsage.get(nodeName).get("memory");
            resourcesUsed.put("cpu", Quantity.getAmountInBytes(cpuUsed).setScale(2, RoundingMode.CEILING));
            resourcesUsed.put("memory", Quantity.getAmountInBytes(memoryUsed).setScale(2, RoundingMode.CEILING));

            FogDevice fogDevice = new FogDevice(nodeName, resourcesTotal, resourcesUsed, resourcesAllocated);
            nodesInCluster.add(fogDevice);

        }

        return nodesInCluster;
    }

    public String getCurrentClusterName() {
        return kubernetesResourceHandler.getClusterName();
    }

}
