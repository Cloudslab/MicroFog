package com.microfog.controlengine.model.domainObjects;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class Cluster {

    String clusterName;
    Double latencyToCluster;
    Double bandwidthToCluster;
    Map<String, BigDecimal> resourceAvailability = new HashMap<>();
    ClusterType clusterType;

    public Cluster(String clusterName, ClusterType clusterType){
        this.clusterName = clusterName;
        this.clusterType = clusterType;
    }

    public void setBandwidthToCluster(Double bandwidthToCluster) {
        this.bandwidthToCluster = bandwidthToCluster;
    }

    public void setLatencyToCluster(Double latencyToCluster) {
        this.latencyToCluster = latencyToCluster;
    }

    public void setResourceAvailability(Map<String, BigDecimal> resourceAvailability) {
        this.resourceAvailability = resourceAvailability;
    }

    public Double getBandwidthToCluster() {
        return bandwidthToCluster;
    }

    public Double getLatencyToCluster() {
        return latencyToCluster;
    }

    public Map<String, BigDecimal> getResourceAvailability() {
        return resourceAvailability;
    }

    public String getClusterName() {
        return clusterName;
    }

    public ClusterType getClusterType() {
        return clusterType;
    }

    public enum ClusterType {
        CLOUD("cloud"),
        FOG("fog");

        public final String label;

        ClusterType(String label) {
            this.label = label;
        }
    }
}
