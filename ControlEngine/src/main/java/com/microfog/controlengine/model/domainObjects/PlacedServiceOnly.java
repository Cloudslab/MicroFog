package com.microfog.controlengine.model.domainObjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.List;

public class PlacedServiceOnly {
    String microserviceName;
    List<String> placedClusters = new ArrayList<>();

    @JsonCreator
    @JsonPropertyOrder({"microserviceName","placedClusters"})
    public PlacedServiceOnly(String microserviceName, List<String> placedClusters){
        this.microserviceName = microserviceName;
        this.placedClusters = placedClusters;
    }

    public String getMicroserviceName() {
        return microserviceName;
    }

    public List<String> getPlacedClusters() {
        return placedClusters;
    }

    public void addToPlacedCluster(String cluster){
        placedClusters.add(cluster);
    }
}
