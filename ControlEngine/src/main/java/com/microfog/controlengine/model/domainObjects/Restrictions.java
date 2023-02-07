package com.microfog.controlengine.model.domainObjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.List;

public class Restrictions {
    String microserviceName;
    // Tiers : Fog or Cloud, if no tiers defined microservice can be placed within any tier.
    // this can be further extended to limit to certain clusters based on data transmission restrictions for sensitive data, using eligibleClusters. If no clusters are defined
    // all clusters in that tier are eligible.
    List<String> eligibleTiers = new ArrayList<>();
    List<String> eligibleClusters = new ArrayList<>();

    public static final String CLOUD_TIER = "cloud";
    public static final String FOG_TIER = "fog";

    @JsonCreator
    @JsonPropertyOrder({"microserviceName", "eligibleTiers","eligibleClusters"})
    public Restrictions(String microserviceName,
                        List<String> eligibleTiers,
                        List<String> eligibleClusters){

        this.microserviceName = microserviceName;
        this.eligibleClusters = eligibleClusters;
        this.eligibleTiers = eligibleTiers;
    }

    public String getMicroserviceName() {
        return microserviceName;
    }

    public List<String> getEligibleClusters() {
        return eligibleClusters;
    }

    public List<String> getEligibleTiers() {
        return eligibleTiers;
    }
}
