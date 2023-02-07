package com.microfog.controlengine.model.domainObjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class PlacementRequest {
    @NotBlank
    public String applicationId;
    public String prId = null;
    public Timestamp createTime;
    public List<PlacedMicroservice> placedMicroservicesList = new ArrayList<>();
    //these are used for deployment of DR and VS for loadbalancing when microservices are deployed within not-adjacent clusters.
    public List<PlacedServiceOnly> deployedServiceLvlOnly = new ArrayList<>();
    @NotEmpty
    public List<String> entryClusters = new ArrayList<>(); // cluster names where ingress access is supported
    public List<Restrictions> placementRestrictions = new ArrayList<>();
    public List<QoSParameter> qoSParameters;

    public List<String> traversedClusters = new ArrayList<>();

    private String nextClusterToSend = ""; // used internally for forwarding purposes. But not at the API level.

    @JsonCreator
    @JsonPropertyOrder({"applicationId", "placedMicroservices","compositionOnlyPlacements","entryClusters","qosParameters","restrictions","traversedClusters"})
    public PlacementRequest(String applicationId, List<PlacedMicroservice> placedMicroservices,List<PlacedServiceOnly>  compositionOnlyPlacements,
                            List<String> entryClusters, List<QoSParameter> qosParameters,
                            List<Restrictions> restrictions, List<String> traversedClusters){
        this.applicationId = applicationId;
        this.placedMicroservicesList = placedMicroservices;
        this.deployedServiceLvlOnly = compositionOnlyPlacements;
        this.entryClusters = entryClusters;
        this.qoSParameters = qosParameters;
        this.placementRestrictions = restrictions;
        this.traversedClusters = traversedClusters;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public String getPrId() {
        if(prId == null)
            prId = applicationId;
        return prId;
    }

    public List<String> getEntryClusters() {
        return entryClusters;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public void addPlacedMicroservice(String microserviceId, String clusterName, int placed, int toBePlaced, Map<String,Integer> subsetWeights,
                                      boolean loadBalancingCompleted) {
        for(PlacedMicroservice m:placedMicroservicesList){
            if(m.microserviceName.equals(microserviceId)){
                m.addInstancePlacement(clusterName);
                return;
            }
        }
        List<String> clusters = new ArrayList<>();
        clusters.add(clusterName);
        PlacedMicroservice placedMicroservice = new PlacedMicroservice(microserviceId,clusters ,placed,toBePlaced, subsetWeights,loadBalancingCompleted);
        placedMicroservicesList.add(placedMicroservice);
    }

    public List<Restrictions> getPlacementRestrictions() {
        return placementRestrictions;
    }

    public List<PlacedMicroservice> getPlacedMicroservicesList() {
        return placedMicroservicesList;
    }

    public Double getQoSParameter(String param) {
        for(QoSParameter q:qoSParameters){
            if(q.getParameter().equals(param))
                return q.getValue();
        }
        return null;
    }

    public void updateSubsetWeight(String microserviceId, Map<String, Integer> deviceMapping) {
        for(PlacedMicroservice p:placedMicroservicesList){
            if(p.getMicroserviceName().equals(microserviceId))
                p.updateSubsetWeights(deviceMapping);
        }
    }

    public void setNextClusterToSend(String nextClusterToSend) {
        this.nextClusterToSend = nextClusterToSend;
    }

    public String getNextClusterToSend() {
        String tmp =  new String(nextClusterToSend);
        nextClusterToSend = "";
        return tmp;
    }

    public List<String> getTraversedClusters() {
        return traversedClusters;
    }

    public void addTraversedCluster(String clusterName) {
        traversedClusters.add(clusterName);
    }

    public void addSvcOnly(List<String> microservices, String clusterName){
        for(String m:microservices)
            addSvcOnly(m,clusterName);
    }

    public void addSvcOnly(String microservice, String clusterName){
        for(PlacedServiceOnly placedServiceOnly : deployedServiceLvlOnly){
            if(placedServiceOnly.microserviceName.equals(microservice)){
                placedServiceOnly.addToPlacedCluster(clusterName);
                return;
            }
        }
        List<String> placedClusters = new ArrayList<>();
        placedClusters.add(clusterName);
        PlacedServiceOnly placedServiceOnly = new PlacedServiceOnly(microservice,placedClusters);
        deployedServiceLvlOnly.add(placedServiceOnly);
    }
}
