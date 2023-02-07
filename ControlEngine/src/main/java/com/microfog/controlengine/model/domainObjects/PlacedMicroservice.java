package com.microfog.controlengine.model.domainObjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlacedMicroservice {
    String microserviceName;
    List<String> placedClusters = new ArrayList<>();

    //fog load balancing (this includes the nodeName and the microservice instance weigh (resources allocated/resources for base instance) to be used by the
    // load balancing policy.)
    Map<String, Integer> subsetWeights = new HashMap<>();

    int placedInstancedCount; // updated for init creation. then updated automatically one by one
    int requiredInstanceCount; //updated during the first instance placement

    boolean loadBalancingCompleted = false;

    @JsonCreator
    @JsonPropertyOrder({"microserviceName", "placedClusters", "placedInstancedCount", "tobePlacedInstanceCount", "subsetWeights", "loadBalancingCompleted"})
    public PlacedMicroservice(String microserviceName,
                              List<String> placedClusters,
                              int placedInstancedCount,
                              int tobePlacedInstanceCount,
                              Map<String, Integer> subsetWeights,
                              boolean loadBalancingCompleted) {

        this.microserviceName = microserviceName;
        this.placedClusters = placedClusters;
        this.placedInstancedCount = placedInstancedCount;
        this.requiredInstanceCount = tobePlacedInstanceCount;
        this.subsetWeights = subsetWeights;
        this.loadBalancingCompleted = loadBalancingCompleted;
    }

    public int getPlacedInstancedCount() {
        return placedInstancedCount;
    }

    public int getRequiredInstanceCount() {
        return requiredInstanceCount;
    }

    public List<String> getPlacedClusters() {
        return placedClusters;
    }

    public String getMicroserviceName() {
        return microserviceName;
    }

    public void setPlacedInstancedCount(int placedInstancedCount) {
        this.placedInstancedCount = placedInstancedCount;
    }

    public void addPlacedClusters(String placedCluster) {
        placedClusters.add(placedCluster);
    }

    public boolean placementCompleted() {
        if (requiredInstanceCount == placedInstancedCount)
            return true;
        else
            return false;
    }

    public void addClusterName(String clusterName) {
        if (!placedClusters.contains(clusterName))
            placedClusters.add(clusterName);
    }

    public void addInstancePlacement(String clusterName) {
        addClusterName(clusterName);
        placedInstancedCount++;
    }

    public void updateSubsetWeights(Map<String, Integer> subsetWeights) {
        if (this.subsetWeights.isEmpty())
            this.subsetWeights = new HashMap<>(subsetWeights);
        else{
            for(String label:subsetWeights.keySet())
                this.subsetWeights.put(label,subsetWeights.get(label));
        }
    }

    public boolean isLoadBalancingCompleted() {
        return loadBalancingCompleted;
    }

    public void setLoadBalancingCompleted(boolean loadBalancingCompleted) {
        this.loadBalancingCompleted = loadBalancingCompleted;
    }

    public Map<String, Integer> getSubsetWeights() {
        return subsetWeights;
    }
}
