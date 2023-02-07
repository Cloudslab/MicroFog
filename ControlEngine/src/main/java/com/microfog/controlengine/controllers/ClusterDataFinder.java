package com.microfog.controlengine.controllers;

import com.microfog.controlengine.model.domainObjects.ClusterData;
import com.microfog.controlengine.model.domainObjects.FogDevice;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Singleton
public class ClusterDataFinder {

    @Inject
    CommandHandler commandHandler;

    String clusterName = ConfigProvider.getConfig().getValue("cluster.name", String.class);

    @ConfigProperty(name = "fog.adjacent.cluster")
    Optional<List<String>> adjacentEdgeCluster;

    @ConfigProperty(name = "cloud.adjacent.cluster")
    Optional<List<String>> cloudClusters;

    public ClusterData getResourceData(){
        List<FogDevice> inClusterDevices = new ArrayList<>();
        inClusterDevices = commandHandler.getAllNodes();
        List<String> fogClusters;
        List<String> connectedCloudClusters;
        if(!adjacentEdgeCluster.isPresent())
            fogClusters = new ArrayList<String>();
        else
            fogClusters = adjacentEdgeCluster.get();

        if(!cloudClusters.isPresent())
            connectedCloudClusters = new ArrayList<>();
        else
            connectedCloudClusters = cloudClusters.get();
        return new ClusterData(clusterName,inClusterDevices,fogClusters,connectedCloudClusters);
    }
}
