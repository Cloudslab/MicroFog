package com.microfog.controlengine.model.domainObjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.List;


/**
 * Contains
 * 1. Resource availability of nodes within the cluster
 * 2. Topology information (connected FogClusters and Cloud clusters)
 */
public class ClusterData {
    String clusterName;
    List<FogDevice> fogDevices = new ArrayList<>();
    List<String> adjacentFogClusters = new ArrayList<>();
    List<String> connectedCloudClusters = new ArrayList<>();

    @JsonCreator
    @JsonPropertyOrder({"clusterName","fogDevices","adjacentFogClusters","connectedCloudClusters"})
    public ClusterData(String clusterName, List<FogDevice> fogDevices, List<String> adjacentFogClusters, List<String> connectedCloudClusters){
        this.clusterName = clusterName;
        this.fogDevices = fogDevices;
        this.adjacentFogClusters = adjacentFogClusters;
        this.connectedCloudClusters = connectedCloudClusters;
    }

    public String getClusterName() {
        return clusterName;
    }

    public List<FogDevice> getFogDevices() {
        return fogDevices;
    }

    public List<String> getAdjacentFogClusters() {
        return adjacentFogClusters;
    }

    public List<String> getConnectedCloudClusters() {
        return connectedCloudClusters;
    }
}
