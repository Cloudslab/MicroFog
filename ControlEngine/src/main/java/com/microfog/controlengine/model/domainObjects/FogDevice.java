package com.microfog.controlengine.model.domainObjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class FogDevice {

    String nodeName;
    // memory in bytes and cpu in number of cores/vcpus
    Map<String,BigDecimal> resourcesTotal = new HashMap<>();
    Map<String,BigDecimal> resourcesUsed = new HashMap<>();
    Map<String,BigDecimal> resourcesAllocated = new HashMap<>();

    @JsonCreator
    @JsonPropertyOrder({"nodeName", "resourcesTotal","resourcesUsed","resourcesAllocated"})
    public FogDevice(String nodeName, Map<String, BigDecimal> resourcesTotal, Map<String, BigDecimal> resourcesUsed, Map<String, BigDecimal> resourcesAllocated){
        this.nodeName = nodeName;
        this.resourcesTotal = resourcesTotal;
        this.resourcesUsed = resourcesUsed;
        this.resourcesAllocated = resourcesAllocated;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public void setResourcesTotal(Map<String, BigDecimal> resourcesTotal) {
        this.resourcesTotal = resourcesTotal;
    }

    public void setResourcesUsed(Map<String, BigDecimal> resourcesUsed) {
        this.resourcesUsed = resourcesUsed;
    }

    @JsonIgnore
    public BigDecimal getRemainingCpu(){
        BigDecimal b1 = resourcesTotal.get("cpu").subtract(resourcesUsed.get("cpu"));
        BigDecimal b2 = resourcesTotal.get("cpu").subtract(resourcesAllocated.get("cpu"));
        System.out.println("TOTAL CPU " + resourcesTotal.get("cpu"));
        System.out.println("ALLOCATED CPU " + resourcesAllocated.get("cpu"));
        System.out.println("USED CPU " + resourcesUsed.get("cpu"));
        BigDecimal remaining = (b1.compareTo(b2) == 1) ? b2 : b1;
        System.out.println("REMAINING CPU " + remaining);
        return remaining;
    }

    @JsonIgnore
    public BigDecimal getRemainingMemory(){
        BigDecimal b1 = resourcesTotal.get("memory").subtract(resourcesUsed.get("memory"));
        BigDecimal b2 = resourcesTotal.get("memory").subtract(resourcesAllocated.get("memory"));

        BigDecimal remaining = (b1.compareTo(b2) == 1) ? b2 : b1;

        System.out.println("REMAINING MEMORY " + b2);
        return remaining;
    }

    //this is used to update CPU resources during placement calculations
    public boolean updateCpu(BigDecimal cpu){
        if(getRemainingCpu().compareTo(cpu)>=0) {
            resourcesUsed.put("cpu",resourcesUsed.get("cpu").add(cpu));
            resourcesAllocated.put("cpu",resourcesAllocated.get("cpu").add(cpu));
            return true;
        }
        return false;
    }

    public boolean updateRam(BigDecimal memory){
        if(getRemainingMemory().compareTo(memory)>=0) {
            resourcesUsed.put("memory",resourcesUsed.get("memory").add(memory));
            resourcesAllocated.put("memory",resourcesAllocated.get("memory").add(memory));
            return true;
        }
        return false;
    }

    public String getNodeName() {
        return nodeName;
    }

    public Map<String, BigDecimal> getResourcesTotal() {
        return resourcesTotal;
    }

    public Map<String, BigDecimal> getResourcesUsed() {
        return resourcesUsed;
    }

    public Map<String, BigDecimal> getResourcesAllocated() {
        return resourcesAllocated;
    }

}
