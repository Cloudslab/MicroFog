package com.microfog.controlengine.requestForwardingLogic;

import com.microfog.controlengine.model.domainObjects.Cluster;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Singleton
public class ForwardingPolicy {

    public final static String TO_CLOUD = "to_cloud";
    public final static String TO_RAND_FOG = "to_rand_fog";

    @ConfigProperty(name = "controlengine.forwardpolicy")
    String policy;

    public String getNextCluster(List<Cluster> clusters, List<String> traversedClusters) {
        switch (policy) {
            case TO_CLOUD:
                return selectCloudCluster(clusters, traversedClusters);
            case TO_RAND_FOG:
                return selectFogCluster(clusters, traversedClusters);
            default:
                return null;
        }
    }

    private String selectFogCluster(List<Cluster> clusters, List<String> traversedClusters) {
        List<Cluster> fogClusters = new ArrayList<>();
        for (Cluster cluster : clusters) {
            if (traversedClusters.contains(cluster.getClusterName()))
                continue;
            switch (cluster.getClusterType()) {
                case FOG:
                    fogClusters.add(cluster);
                    break;
                case CLOUD:
                    break;
            }
        }
        if (!fogClusters.isEmpty())
            return selectRandomCluster(fogClusters);
        else
            return selectCloudCluster(clusters, traversedClusters);
    }

    private String selectCloudCluster(List<Cluster> clusters, List<String> traversedClusters) {
        List<Cluster> cloudClusters = new ArrayList<>();
        for (Cluster cluster : clusters) {
            if (traversedClusters.contains(cluster.getClusterName()))
                continue;
            switch (cluster.getClusterType()) {
                case CLOUD:
                    cloudClusters.add(cluster);
                    break;
                case FOG:
                    break;
            }
        }
        return selectRandomCluster(cloudClusters);
    }

    private String selectRandomCluster(List<Cluster> clusters) {
        Random random = new Random();
        System.out.println("POSIIBLE CLUSTERS = " + clusters.get(0).getClusterName());
        return clusters.get(random.nextInt(clusters.size())).getClusterName();
    }

}
