package com.microfog.controlengine.model.deployment;


import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Pod extends MicroserviceLevelResource {
    String namespace;

    Boolean setNodeName = false;
    List<String> nodeNames;

    Map<String,Map<String, BigDecimal>> requestedResourcesPerNode = new HashMap<>();

    public Pod(String yamlPath, String namespace, String microserviceIdentifier){
        super(yamlPath,microserviceIdentifier);
        this.namespace = namespace;
    }

    public Pod(String yamlPath, String namespace, String microserviceIdentifier, List<String> nodeNames){
        super(yamlPath,microserviceIdentifier);
        this.namespace = namespace;
        setNodeName = true;
        this.nodeNames = nodeNames;
    }

    public Pod(Pod resource) {
        super(resource.getYamlFile(),resource.getMicroserviceIdentifier());
        this.namespace = resource.getNamespace();
        setNodeName = resource.modifyNode();
        this.nodeNames = resource.getNodeNames();
    }

    public Pod(Pod resource, List<String> nodeNames) {
        super(resource.getYamlFile(),resource.getMicroserviceIdentifier());
        this.namespace = resource.getNamespace();
        setNodeName = true;
        this.nodeNames = nodeNames;
    }

    public String getNamespace() {
        return namespace;
    }


    public String getMicroserviceIdentifier() {
        return microserviceIdentifier;
    }

    public List<String> getNodeNames() {
        return nodeNames;
    }

    public Boolean modifyNode(){
        return setNodeName;
    }

    public void setNodeNames(List<String> nodeNames) {
        setNodeName = true;
        this.nodeNames = nodeNames;
        System.out.println("POD Nodenames " + this.nodeNames.toString());
    }

    public void addRequestedResources(String nodeName,ResourceType type,BigDecimal resourceAmount){
        if(!nodeNames.contains(nodeName))
            nodeNames.add(nodeName);
        if(requestedResourcesPerNode.containsKey(nodeName))
            requestedResourcesPerNode.get(nodeName).put(type.label,resourceAmount);
        else {
            Map<String, BigDecimal> m = new HashMap<>();
            m.put(type.label, resourceAmount);
            requestedResourcesPerNode.put(nodeName,m);
        }
    }

    public Map<String, BigDecimal> getRequestedResources(String node) {
        return requestedResourcesPerNode.get(node);
    }

    public enum ResourceType{
        CPU("cpu"),
        MEMORY("memory");

        public final String label;

        ResourceType(String label) {
            this.label = label;
        }
    }

}


