package com.microfog.controlengine.model.domainObjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.HashMap;
import java.util.Map;

public class Placement {
    String microserviceId;
    //   cluster -> <device, and scaling factor> . For centralised placement cluster information is important.
    Map<String, Map<String, Integer>> deviceMapping = new HashMap<>();

    @JsonCreator
    @JsonPropertyOrder({"microserviceId", "deviceMapping"})
    public Placement(String microserviceId, Map<String, Map<String, Integer>> deviceMapping) {
        System.out.println("DEVICE MAPPING WITH CONSTRUCT:" + deviceMapping.toString());
        this.microserviceId = microserviceId;
        this.deviceMapping = deviceMapping;
    }

    public Placement(String microserviceId) {
        this.microserviceId = microserviceId;
    }

    public String getMicroserviceId() {
        return microserviceId;
    }

    public Map<String, Map<String, Integer>> getDeviceMapping() {
        return new HashMap<>(deviceMapping);
    }

    public Map<String, Integer> getDeviceMapping(String clusterName) {
        System.out.println("DEVICE MAPPING:" + deviceMapping.toString());
        if (deviceMapping.containsKey(clusterName))
            return new HashMap<>(deviceMapping.get(clusterName));
        else
            return new HashMap<>();
    }

    public void addDeviceMapping(String clusterName, String device, Integer scalingFactor) {
        System.out.println("ADD DEVICE TO MAPPING : " + device + " - " + clusterName);
        if(!deviceMapping.containsKey(clusterName))
            deviceMapping.put(clusterName,new HashMap<>());
        deviceMapping.get(clusterName).put(device, scalingFactor);
    }

    public void addDeviceMapping(String clusterName,String device) {
        System.out.println("ADD DEVICE TO MAPPING : " + device + " - " + clusterName);
        if(!deviceMapping.containsKey(clusterName))
            deviceMapping.put(clusterName,new HashMap<>());

        if (deviceMapping.get(clusterName).containsKey(device))
            deviceMapping.get(clusterName).put(device, deviceMapping.get(clusterName).get(device) + 1);
        else
            deviceMapping.get(clusterName).put(device, 1);
    }

    public int getScalingFactor(String clustername, String device) {
        return deviceMapping.get(clustername).get(device);
    }

}
