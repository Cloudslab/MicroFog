package com.microfog.controlengine.placementlogic;

import com.microfog.controlengine.model.deomainObjectsLight.ApplicationL;
import com.microfog.controlengine.model.domainObjects.*;

import java.util.*;

public abstract class  PlacementAlgorithm {
    protected List<FogDevice> inClusterDevices = new ArrayList<>();
    protected Map<String, ClusterData> clusterData = new HashMap<>();
    protected List<PlacementRequest> placementRequests;
    protected Map<String, Application> appInfo = new HashMap<>();

    //only for external algorithms
    protected List<ApplicationL> appInfoL = new ArrayList<>();

    public PlacementOutPut run(List<PlacementRequest> prs, Map<String, Application> appInfo, List<FogDevice> inClusterDevices, Map<String, ClusterData> adjacentClusterData) {
        placementRequests = prs;
        
        this.inClusterDevices = inClusterDevices;

        this.appInfo = appInfo;

        this.clusterData = adjacentClusterData;
        
        return executePlacementLogic();

    }

    public PlacementOutPut run(List<PlacementRequest> prs, List<ApplicationL> appInfo, List<FogDevice> inClusterDevices, Map<String, ClusterData> adjacentClusterData) {
        placementRequests = prs;

        this.inClusterDevices = inClusterDevices;

        this.clusterData = adjacentClusterData;

        this.appInfoL = appInfo;

        return executePlacementLogic();

    }

    protected abstract PlacementOutPut executePlacementLogic();

    protected void updateCompletedPrs(PlacementRequest pr, PlacementOutPut placementOutPut) {
        placementOutPut.addCompletedPrs(pr);
    }

    protected void updateIncompletedPrs(PlacementRequest pr,PlacementOutPut placementOutPut) {
        placementOutPut.addIncompletedPrs(pr);
    }

    protected void updatePlacedMicroservice(PlacementRequest pr, Microservice microservice, String clusterName, int placed, int toBePlaced
            , Map<String,Integer> subsetWeights,
                                            boolean loadBalancingCompleted) {
        pr.addPlacedMicroservice(microservice.getMicroserviceId(),clusterName,placed,toBePlaced,subsetWeights,loadBalancingCompleted);
    }

    protected void updateAccessClusters(PlacementRequest pr, String application, PlacementOutPut placementOutPut) {
        placementOutPut.addAccessClusters(application,pr.getEntryClusters());
    }

    protected void placeOnDevice(String application, Microservice microservice, String clusterName, FogDevice fogDevice, PlacementOutPut placementOutput) {
        // add to map
        System.out.println("MAPPED :" + fogDevice.getNodeName() + " in " + clusterName);
        placementOutput.addToPlacement(application,microservice.getMicroserviceId(),clusterName,fogDevice.getNodeName());
        // update resources on device
        if(placementOutput.getPlacement().get(application).get(microservice.getMicroserviceId()).getDeviceMapping().get(clusterName).get(fogDevice.getNodeName())==1) {
            if (!fogDevice.updateCpu(microservice.getTotalCpu()))
                System.out.println("Error updating resources (cpu) : usage larger than allocatable resources");
            if (!fogDevice.updateRam(microservice.getTotalRam()))
                System.out.println("Error updating resources (memory) : usage larger than allocatable resources");
        }
        else{
            if (!fogDevice.updateCpu(microservice.getCpu()))
                System.out.println("Error updating resources (cpu) : usage larger than allocatable resources");
            if (!fogDevice.updateRam(microservice.getRam()))
                System.out.println("Error updating resources (memory) : usage larger than allocatable resources");
        }
    }

}
