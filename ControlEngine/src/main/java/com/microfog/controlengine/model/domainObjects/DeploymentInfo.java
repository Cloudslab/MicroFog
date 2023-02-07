package com.microfog.controlengine.model.domainObjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//per app deployment info
public class DeploymentInfo {
    String applicationId;
    List<Placement> microservicesToDeploy = new ArrayList<>(); //microservicesPlacedWithinCluster -> this is only used in centralised placement mode
    Map<String, Map<String,Integer>> lbInfo = new HashMap<>(); // per microservice -> subset labels and weights.
    List<String> compositionOnlyPlacements = new ArrayList<>(); //during distributed placement used to makeuser communication is established
                                                          // across clusters even if microservices are not deployed in this cluster.
    Boolean entryCluster = false; // this is also used in centralised placement
    Boolean onlyLBrelated;

    @JsonCreator
    @JsonPropertyOrder({"applicationId", "microservices","lbInfo","entryCluster","onlyLBrelated","compositionOnlyPlacements"})
    public DeploymentInfo(String applicationId, List<Placement> microservices, Map<String, Map<String,Integer>> lbInfo,
                          Boolean entryCluster,Boolean onlyLBrelated,List<String> compositionOnlyPlacements ){
        this.applicationId = applicationId;
        this.microservicesToDeploy = microservices;
        this.lbInfo = lbInfo;
        this.entryCluster = entryCluster;
        this.onlyLBrelated = onlyLBrelated;
        this.compositionOnlyPlacements = compositionOnlyPlacements;
    }

    public DeploymentInfo(String appId, boolean onlyLBrelated){
        this.applicationId = appId;
        this.onlyLBrelated = onlyLBrelated;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public Boolean getEntryCluster() {
        return entryCluster;
    }

    public List<Placement> getMicroservicesToDeploy() {
        return microservicesToDeploy;
    }

    public Map<String, Map<String, Integer>> getLbInfo() {
        return lbInfo;
    }

    public void addLBinfo(String microserviceId, Map<String, Integer> lbWeights){
        System.out.println( "LB weight added : " + microserviceId + " : " +lbWeights.toString());
        lbInfo.put(microserviceId,lbWeights);
    }

    public Boolean isOnlyLBrelated() {
        return onlyLBrelated;
    }

    public void addMicroservicesToDeploy(Placement p) {
        microservicesToDeploy.add(p);
    }

    public void setAccessCluster(boolean b) {
        entryCluster = true;
    }

    public void addAdditionaMForSLevel(String microservice){
        compositionOnlyPlacements.add(microservice);
    }

    public void addAdditionaMForSLevel(List<String> microservice){
        compositionOnlyPlacements.addAll(microservice);
    }

    public List<String> getCompositionOnlyPlacements() {
        return compositionOnlyPlacements;
    }
}
