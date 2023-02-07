package com.microfog.controlengine.placementlogic;

import com.microfog.controlengine.model.domainObjects.*;
import org.eclipse.microprofile.config.ConfigProvider;

import java.util.*;
import java.util.stream.Collectors;

/**
 * From "Resource Aware Placement of IoT Application Modules in Fog-Cloud Computing Paradigm" by Mohit Taneja, Alan Davy
 * Extended for a batch placement scenario with multiple applications
 * Extensions :
 * 1. To handle batch placement
 * 2. V1 : without scaling and load balancing
 * 3. V2 : with load balancing
 */

public class ResourceAwarePlacement extends PlacementAlgorithm{

    String version = ConfigProvider.getConfig().getValue("controlengine.placementalgo.version", String.class);
    String clusterName = ConfigProvider.getConfig().getValue("cluster.name", String.class);

    @Override
    public PlacementOutPut run(List<PlacementRequest> prs, Map<String, Application> appInfo, List<FogDevice> inClusterDevices, Map<String, ClusterData> adjacentClusterData) {
            return super.run(prs, appInfo, inClusterDevices, adjacentClusterData);
    }

    @Override
    protected PlacementOutPut executePlacementLogic() {
        PlacementOutPut placementOutPut = new PlacementOutPut();
        if(version.equals("V1")) {
            for (PlacementRequest pr : placementRequests) {
                Application application = appInfo.get(pr.getApplicationId());
                updateAccessClusters(pr,application.getApplicationId(), placementOutPut);
                // sort nodes in ascending order of resource
                sortFogDevices(inClusterDevices);
                // sort microservices in ascending order of resource requirements
                List<Microservice> sortedMicroservices = sortMicroservices(application.getMicroservicesList());
                List<Microservice> notPlacedInCluster = new ArrayList<>();
                int low = 0; int high = inClusterDevices.size()-1;
                for (Microservice microservice : sortedMicroservices) {

                    // find lower bound eligible device
                    int index = findLowerBoundDevice(inClusterDevices,microservice,low,high);
                    // assign module to device
                    if(index!=-1){
                        placeOnDevice(application.getApplicationId(), microservice,clusterName,inClusterDevices.get(index),placementOutPut);
                        //as a single instance is placed, load balancing information are irrelevant
                        updatePlacedMicroservice(pr, microservice, clusterName,1,1,new HashMap<>(),true);
                        sortFogDevices(inClusterDevices);
                        low = index;
                    }
                    else{
                        //todo place on cloud. Request is send to cloud cluster for placement
                        // PR should be added as incomplete
                        notPlacedInCluster.add(microservice);
                    }
                }
                if(!notPlacedInCluster.isEmpty()){
                    updateIncompletedPrs(pr, placementOutPut);
                }
                else
                    updateCompletedPrs(pr,placementOutPut);
            }
        }
        // v2 is a dummy placement created to test request forwarding
        else if(version.equals("V2")){
            for (PlacementRequest pr : placementRequests) {
                updateIncompletedPrs(pr, placementOutPut);
            }
        }
        // for testing purposes this discards all the placement requests
        else if(version.equals("V3")){
            return placementOutPut;
        }

        return placementOutPut;
    }



    private int findLowerBoundDevice(List<FogDevice> sortedFogDevices, Microservice microservice, int low, int high) {
        int length = sortedFogDevices.size();
        int mid = (low+high)/2;
        while (true){
            FogDevice fogDevice = sortedFogDevices.get(mid);
            if(compareForResources(fogDevice,microservice)==1){
                high = mid-1;
                if(high<low)
                    return mid;
            }
            else{
                //todo check this if this is true if cloud is not considered at this stage
                low = mid+1;
                if(low>high){
                    return (mid<length-1?mid+1:-1);
                }
            }
            mid = (low+high)/2;
        }
    }

    private int compareForResources(FogDevice fogDevice, Microservice microservice) {
        if(fogDevice.getRemainingCpu().compareTo(microservice.getTotalCpu())>=0 &&
               fogDevice.getRemainingMemory().compareTo(microservice.getTotalRam())>=0)
            return 1;
        return -1;
    }

    private void sortFogDevices(List<FogDevice> inClusterDevices) {
        inClusterDevices.sort(Comparator.comparing(FogDevice::getRemainingCpu)
                        .thenComparing(FogDevice::getRemainingMemory));
    }

    private List<Microservice> sortMicroservices(List<Microservice> microserviceList){
        return microserviceList.stream()
                .sorted(Comparator.comparing(Microservice::getTotalCpu)
                        .thenComparing(Microservice::getTotalRam))
                .collect(Collectors.toList());
    }



}
