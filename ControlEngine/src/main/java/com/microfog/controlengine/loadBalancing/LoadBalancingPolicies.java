package com.microfog.controlengine.loadBalancing;

import com.microfog.controlengine.model.domainObjects.PlacedMicroservice;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class LoadBalancingPolicies {

    public final static String WEIGHTED_ROUND_ROBI = "weighted_round_robin";

    @ConfigProperty(name = "controlengine.loadblancing.policy")
    String policy;

    public Map<String, Integer> getWeights(PlacedMicroservice placement) {
        switch (policy) {
            case WEIGHTED_ROUND_ROBI:
                return generateWeightsForWRR(placement);
            default:
                return null;
        }
    }

    private Map<String, Integer> generateWeightsForWRR(PlacedMicroservice placement) {
        Map<String, Integer> lbWeights = new HashMap<>();

        Map<String, Integer> subsetInfo = placement.getSubsetWeights();
        System.out.println("Weights from placedMicroservice : " + subsetInfo.toString());
        int totalInstances = placement.getRequiredInstanceCount();

        int totalWeight = 100;
        int currentWeight = 0;
        int subsetIndex = 0;

        for (String subsetLabel : subsetInfo.keySet()) {
            int weight;
            if (subsetIndex == subsetInfo.size()) {
                weight = totalWeight - currentWeight;
            } else
                weight = (int) Math.round((((double) subsetInfo.get(subsetLabel) )/ totalInstances)*100);
            currentWeight += weight;
            subsetIndex++;
            lbWeights.put(subsetLabel, weight);
        }
        return lbWeights;
    }
}
