package com.microfog.controlengine.placementlogic;

import com.microfog.controlengine.model.domainObjects.*;
import org.eclipse.microprofile.config.ConfigProvider;
import org.graalvm.collections.Pair;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Adaptation of "Microservices-based IoT Application Placement within
 * Heterogeneous and Resource Constrained Fog Computing
 * Environments" by Samodha Pallewatta, Vassilis Kostakos and Rajkumar Buyya for the multi-cluster scenario
 */

public class DistributedPlacement extends PlacementAlgorithm {
    String version = ConfigProvider.getConfig().getValue("controlengine.placementalgo.version", String.class);
    String clusterName = ConfigProvider.getConfig().getValue("cluster.name", String.class);
    String clusterTier = ConfigProvider.getConfig().getValue("cluster.tier", String.class);

    @Override
    public PlacementOutPut run(List<PlacementRequest> prs, Map<String, Application> appInfo, List<FogDevice> inClusterDevices, Map<String, ClusterData> adjacentClusterData) {
        return super.run(prs, appInfo, inClusterDevices, adjacentClusterData);
    }

    @Override
    protected PlacementOutPut executePlacementLogic() {
        PlacementOutPut placementOutPut = new PlacementOutPut();
        /**
         * Centralised Placement Approach
         */
        if (version.equals("V1")) {
            for (PlacementRequest pr : placementRequests) {
                Application application = appInfo.get(pr.getApplicationId());
                List<String> accessClusters = pr.getEntryClusters();
                updateAccessClusters(pr, application.getApplicationId(), placementOutPut);

                // place root microservices in access clusters and continue from there to place to minimize latency.
                List<String> placedMicroservices = new ArrayList<>();
                for (PlacedMicroservice p : pr.getPlacedMicroservicesList()) {
                    //since this is not distributed placement, any microservices that are already placed would be FE
                    placedMicroservices.add(p.getMicroserviceName());
                }
                List<String> notPlacedInCluster = new ArrayList<>();
                checkRestrictions(notPlacedInCluster, pr);
                //to be placed within access clusters if possible
                List<Microservice> microservicesToPlace = getMicroservicesToPlace(application.getApplicationDAG(), placedMicroservices, notPlacedInCluster, application.getMicroservicesList());

                List<String> prioritisedClusters = accessClusters;

                while (!microservicesToPlace.isEmpty()) {
                    for (Microservice microservice : microservicesToPlace) {
                        int instancesToPlace = getInstancesToPlace(application, microservice, pr.placedMicroservicesList, pr.getQoSParameter(QoSParameter.THROUGHPUT));
                        System.out.println("Instances to place :" + instancesToPlace);
                        int index = -1;
                        String clusterToPlace = null;
                        while (instancesToPlace > 0) {
                            Pair<String, Integer> p = getDeviceToPlace(prioritisedClusters, clusterData, microservice, clusterToPlace, index);
                            index = p.getRight();
                            clusterToPlace = p.getLeft();
                            if (index != -1) {
                                System.out.println("PLACE " + microservice.getMicroserviceId() + " in " + clusterData.get(clusterToPlace).getFogDevices().get(index).getNodeName());
                                placeOnDevice(application.getApplicationId(), microservice, clusterToPlace, clusterData.get(clusterToPlace).getFogDevices().get(index), placementOutPut);
                                //will be updated later
                                updatePlacedMicroservice(pr, microservice, clusterToPlace, 1, instancesToPlace, new HashMap<>(), false);
                                instancesToPlace--;
                                if (instancesToPlace == 0) {
                                    placedMicroservices.add(microservice.getMicroserviceId());
                                }
                            } else {
                                instancesToPlace = 0;
                                notPlacedInCluster.add(microservice.getMicroserviceId());
                            }
                        }
                        //prs to send load balancing data
                        if (placementOutPut.getPlacement().containsKey(application.getApplicationId()) &&
                                placementOutPut.getPlacement().get(application.getApplicationId()).containsKey(microservice.getMicroserviceId()))
                            updateLoadBalancingData(pr, placementOutPut.getPlacement().get(application.getApplicationId()).get(microservice.getMicroserviceId()));
                        else {
                            System.out.println("ERROR PLACEMENT NOT COMPLETED");
                        }
                    }
                    microservicesToPlace = getMicroservicesToPlace(application.getApplicationDAG(), placedMicroservices, notPlacedInCluster, application.getMicroservicesList());
                }
                if (!notPlacedInCluster.isEmpty()) {
                    System.out.println("ERROR PLACEMENT NOT COMPLETED 2");
                } else
                    updateCompletedPrs(pr, placementOutPut);

            }

        }

        /**
         * Placement with horizontal capability. No of instances are decided based on the throughput requirement of the application
         */
        if (version.equals("V2")) {
            for (PlacementRequest pr : placementRequests) {
                Application application = appInfo.get(pr.getApplicationId());
                updateAccessClusters(pr, application.getApplicationId(), placementOutPut);

                //placement completed in this cluster or ones before this
                List<String> placementCompletedMicroservices = new ArrayList<>();
                List<String> partiallyCompletedMicroservices = new ArrayList<>();
                for (PlacedMicroservice p : pr.getPlacedMicroservicesList()) {
                    if (p.placementCompleted()) {
                        placementCompletedMicroservices.add(p.getMicroserviceName());
                        System.out.println("Completed microservices " + p.getMicroserviceName());
                    } else {
                        partiallyCompletedMicroservices.add(p.getMicroserviceName());
                        System.out.println("Partially completed microservices " + p.getMicroserviceName());
                    }
                }
                List<String> notPlacedInCluster = new ArrayList<>();
                checkRestrictions(notPlacedInCluster, pr);
                List<Microservice> microservicesToPlace = getMicroservicesToPlace(application.getApplicationDAG(), placementCompletedMicroservices, notPlacedInCluster, application.getMicroservicesList());
                System.out.println("Microservices to place " + microservicesToPlace);
                while (!microservicesToPlace.isEmpty()) {
                    System.out.println("inside while 1");
                    for (Microservice microservice : microservicesToPlace) {
                        System.out.println("inside while 2");
                        int instancesToPlace = getInstancesToPlace(application, microservice, pr.placedMicroservicesList, pr.getQoSParameter(QoSParameter.THROUGHPUT));
                        System.out.println("Instances to place :" + instancesToPlace);
                        int index = -1;
                        while (instancesToPlace > 0) {
                            System.out.println("inside while 3");
                            index = getDeviceToPlace(inClusterDevices, microservice, index);
                            if (index != -1) {
                                System.out.println("PLACE " + microservice.getMicroserviceId() + " in " + inClusterDevices.get(index).getNodeName());
                                placeOnDevice(application.getApplicationId(), microservice, clusterName, inClusterDevices.get(index), placementOutPut);
                                //will be updated later
                                updatePlacedMicroservice(pr, microservice, clusterName, 1, instancesToPlace, new HashMap<>(), false);
                                instancesToPlace--;
                                if (instancesToPlace == 0) {
                                    placementCompletedMicroservices.add(microservice.getMicroserviceId());
                                }
                            } else {
                                instancesToPlace = 0;
                                notPlacedInCluster.add(microservice.getMicroserviceId());
                            }
                        }
                        //prs to send load balancing data
                        if (placementOutPut.getPlacement().containsKey(application.getApplicationId()) &&
                                placementOutPut.getPlacement().get(application.getApplicationId()).containsKey(microservice.getMicroserviceId()))
                            updateLoadBalancingData(pr, placementOutPut.getPlacement().get(application.getApplicationId()).get(microservice.getMicroserviceId()));
                    }
                    microservicesToPlace = getMicroservicesToPlace(application.getApplicationDAG(), placementCompletedMicroservices, notPlacedInCluster, application.getMicroservicesList());
                }
                if (!notPlacedInCluster.isEmpty()) {
                    updateIncompletedPrs(pr, placementOutPut);
                } else
                    updateCompletedPrs(pr, placementOutPut);

            }
            return placementOutPut;
        }

        /**
         * V3 : Placement without horizontal scalability. No loadbalancing related functions
         * Config for loadblancing should be set to FALSE
         */
        if (version.equals("V3")) {
            for (PlacementRequest pr : placementRequests) {

                Application application = appInfo.get(pr.getApplicationId());
                updateAccessClusters(pr, application.getApplicationId(), placementOutPut);

                List<String> placedMicroservices = new ArrayList<>();
                for (PlacedMicroservice p : pr.getPlacedMicroservicesList()) {
                    placedMicroservices.add(p.getMicroserviceName());
                }
                List<String> notPlacedInCluster = new ArrayList<>();
                checkRestrictions(notPlacedInCluster, pr);
                List<Microservice> microservicesToPlace = getMicroservicesToPlace(application.getApplicationDAG(), placedMicroservices, notPlacedInCluster, application.getMicroservicesList());
                while (!microservicesToPlace.isEmpty()) {
                    for (Microservice microservice : microservicesToPlace) {
                        int instancesToPlace = 1;
                        if (pr.getQoSParameter(QoSParameter.THROUGHPUT) != null)
                            instancesToPlace = getInstancesToPlace(application, microservice, pr.placedMicroservicesList, pr.getQoSParameter(QoSParameter.THROUGHPUT));
                        int index = getDeviceToPlace(inClusterDevices, microservice, -1, instancesToPlace);
                        if (index != -1) {
                            System.out.println("PLACE " + microservice.getMicroserviceId() + " in " + inClusterDevices.get(index).getNodeName());
                            placeOnDevice(application.getApplicationId(), microservice, clusterName, inClusterDevices.get(index), placementOutPut);
                            //as a single instance is placed, load balancing information are irrelevant
                            updatePlacedMicroservice(pr, microservice, clusterName, instancesToPlace, instancesToPlace, new HashMap<>(), true);
                            placedMicroservices.add(microservice.getMicroserviceId());
                        } else {
                            notPlacedInCluster.add(microservice.getMicroserviceId());
                        }
                    }
                    microservicesToPlace = getMicroservicesToPlace(application.getApplicationDAG(), placedMicroservices, notPlacedInCluster, application.getMicroservicesList());
                }
                if (!notPlacedInCluster.isEmpty()) {
                    updateIncompletedPrs(pr, placementOutPut);
                } else
                    updateCompletedPrs(pr, placementOutPut);
            }
        }
        return placementOutPut;
    }

    private void checkRestrictions(List<String> notPlacedInCluster, PlacementRequest pr) {
        for(Restrictions r:pr.getPlacementRestrictions()){
                for(String eligibleTier:r.getEligibleTiers()){
                    if(!eligibleTier.equals(clusterTier) && !notPlacedInCluster.contains(r.getMicroserviceName()))
                        notPlacedInCluster.add(r.getMicroserviceName());
                }


                for(String eligibleCluster:r.getEligibleClusters()){
                    if(!eligibleCluster.equals(clusterName) && !notPlacedInCluster.contains(r.getMicroserviceName()))
                        notPlacedInCluster.add(r.getMicroserviceName());
                }

        }
    }

    private void updateLoadBalancingData(PlacementRequest pr, Placement placement) {
        for (String clusterName : placement.getDeviceMapping().keySet())
            pr.updateSubsetWeight(placement.getMicroserviceId(), placement.getDeviceMapping(clusterName));
    }

    private int getInstancesToPlace(Application application, Microservice microservice, List<PlacedMicroservice> placedMicroservicesList, Double throuhgput) {
        for (PlacedMicroservice m : placedMicroservicesList) {
            //instance count already calculated before
            if (m.getMicroserviceName().equals(microservice.getMicroserviceId())) {
                System.out.println("required : " + m.getRequiredInstanceCount() + "  place " + m.getPlacedInstancedCount());
                return m.getRequiredInstanceCount() - m.getPlacedInstancedCount();
            }
        }

        BigDecimal b = new BigDecimal(throuhgput);
        if (b != null)
            return b.divide(microservice.getThroughput(), 0, RoundingMode.CEILING).intValue();
        else
            return 1;
    }

    private Pair<String, Integer> getDeviceToPlace(List<String> prioritisedCluster, Map<String, ClusterData> deviceData, Microservice microservice, String clusterToTry, int indexToTry) {
        if (indexToTry >= 0 && compareForResourcesWOverhead(deviceData.get(clusterToTry).getFogDevices().get(indexToTry), microservice) == 1)
            return Pair.create(clusterToTry, indexToTry);
        else {
            String clusterName = null;
            int index = -1;
            List<String> clustersToCheck = prioritisedCluster; //ordered from fog to cloud
            List<String> checkedClusters = new ArrayList<>();
            while (index == -1 && !clustersToCheck.isEmpty()) {
                for (String cluster : clustersToCheck) {
                    clusterName = cluster;
                    sortFogDevices(deviceData.get(cluster).getFogDevices());
                    index = findLowerBoundDevice(deviceData.get(cluster).getFogDevices(), microservice, 0, deviceData.get(cluster).getFogDevices().size() - 1);
                    if (index != -1)
                        break;
                }
                if (index == -1) {
                    checkedClusters.addAll(clustersToCheck);
                    clustersToCheck = getUncheckedAdjacentCluster(clustersToCheck, checkedClusters, deviceData);
                }
            }
            System.out.println("PAIR CREATED : " + clusterName );
            return Pair.create(clusterName, index);
        }
    }

    private List<String> getUncheckedAdjacentCluster(List<String> clustersToCheck, List<String> checkedClusters, Map<String, ClusterData> deviceData) {
        List<String> fogclusters = new ArrayList<>();
        List<String> cloudclusters = new ArrayList<>();
        for (String c : clustersToCheck) {
            for (String fc : deviceData.get(c).getAdjacentFogClusters()) {
                if (!checkedClusters.contains(fc))
                    fogclusters.add(fc);
            }
            for (String cc : deviceData.get(c).getConnectedCloudClusters()) {
                if (!checkedClusters.contains(cc))
                    cloudclusters.add(cc);
            }
        }
        List<String> newClusterToCheck = new ArrayList<>();
        newClusterToCheck.addAll(fogclusters);
        newClusterToCheck.addAll(cloudclusters);
        return newClusterToCheck;
    }

    private int getDeviceToPlace(List<FogDevice> inClusterDevices, Microservice microservice, int indexToTry, int instancesToPlace) {
        //this is used for non load balancing scenario. So a single instance is placed where load balancing is not applicable. And resource requirements is
        // per microservice * instance count

        sortFogDevices(inClusterDevices);
        return findLowerBoundDevice(inClusterDevices, microservice, 0, inClusterDevices.size() - 1, instancesToPlace);

    }

    private int getDeviceToPlace(List<FogDevice> inClusterDevices, Microservice microservice, int indexToTry) {
        System.out.println("inside while 4");
        if (indexToTry >= 0 && compareForResourcesWOverhead(inClusterDevices.get(indexToTry), microservice) == 1)
            return indexToTry;
        else {
            sortFogDevices(inClusterDevices);
            return findLowerBoundDevice(inClusterDevices, microservice, 0, inClusterDevices.size() - 1);
        }
    }

    /**
     * Using the DAG find next eligible microservices (a microservice is eligible for placement if its all client microservices are placed
     * and the restrictions if any are not violated
     *
     * @param applicationDAG
     * @param placedMicroservices
     * @param notPlacedInCluster
     * @return
     */
    private List<Microservice> getMicroservicesToPlace(ApplicationDAG applicationDAG, List<String> placedMicroservices, List<String> notPlacedInCluster, List<Microservice> microservices) {
        List<Microservice> toPlace = new ArrayList<>();
        List<String> exclude = new ArrayList<>();
        exclude.addAll(placedMicroservices);
        exclude.addAll(notPlacedInCluster);
        for (Microservice m : microservices) {
            if (!exclude.contains(m.getMicroserviceId())) {
                List<Microservice> clientMicroservices = applicationDAG.getConsumerMicroservices(m);
                boolean allPlaced = true;
                for (Microservice client : clientMicroservices) {
                    if (placedMicroservices.contains(client.getMicroserviceId()))
                        continue;
                    else {
                        allPlaced = false;
                        for (Microservice consumed : applicationDAG.getConsumedMicroservices(m)) {
                            exclude.add(consumed.getMicroserviceId());
                        }
                    }
                }
                if (allPlaced)
                    toPlace.add(m);

            }
        }
        return toPlace;
    }

    private int findLowerBoundDevice(List<FogDevice> sortedFogDevices, Microservice microservice, int low, int high, int instanceCount) {
        int length = sortedFogDevices.size();
        System.out.println("Sorted Devices : " + sortedFogDevices.toString());
        int mid = (low + high) / 2;
        while (true) {
            FogDevice fogDevice = sortedFogDevices.get(mid);
            System.out.println("Mid Device - " + fogDevice.getNodeName());
            if (compareForResources(fogDevice, microservice, instanceCount) == 1) {
                System.out.println("ENOUGH RESOURCES FOR " + microservice.getMicroserviceId() + " in " + fogDevice.getNodeName());
                high = mid - 1;
                if (high < low)
                    return mid;
            } else {
                System.out.println("NOT ENOUGH RESOURCES FOR " + microservice.getMicroserviceId() + " in " + fogDevice.getNodeName());
                low = mid + 1;
                if (low > high) {
                    return (mid < length - 1 ? mid + 1 : -1);
                }
            }
            mid = (low + high) / 2;
        }
    }

    private int findLowerBoundDevice(List<FogDevice> sortedFogDevices, Microservice microservice, int low, int high) {
        int length = sortedFogDevices.size();
        System.out.println("Sorted Devices : " + sortedFogDevices.toString());
        int mid = (low + high) / 2;
        while (true) {
            FogDevice fogDevice = sortedFogDevices.get(mid);
            System.out.println("Mid Device - " + fogDevice.getNodeName());
            if (compareForResources(fogDevice, microservice) == 1) {
                System.out.println("ENOUGH RESOURCES FOR " + microservice.getMicroserviceId() + " in " + fogDevice.getNodeName());
                high = mid - 1;
                if (high < low)
                    return mid;
            } else {
                System.out.println("NOT ENOUGH RESOURCES FOR " + microservice.getMicroserviceId() + " in " + fogDevice.getNodeName());
                low = mid + 1;
                if (low > high) {
                    return (mid < length - 1 ? mid + 1 : -1);
                }
            }
            mid = (low + high) / 2;
        }
    }

    // comparison without considering istio proxy overhead
    private int compareForResourcesWOverhead(FogDevice fogDevice, Microservice microservice) {
        if (fogDevice.getRemainingCpu().compareTo(microservice.getCpu()) >= 0) {
            if (fogDevice.getRemainingMemory().compareTo(microservice.getRam()) >= 0) {
                return 1;
            }
            System.out.println("NOT ENOUGH MEMORY FOR " + microservice.getMicroserviceId() + " in " + fogDevice.getNodeName());
            return -1;
        }
        System.out.println("NOT ENOUGH CPU FOR " + microservice.getMicroserviceId() + " in " + fogDevice.getNodeName());
        return -1;
    }

    private int compareForResources(FogDevice fogDevice, Microservice microservice, int instanceCount) {
        BigDecimal i = new BigDecimal(instanceCount-1);
        if (fogDevice.getRemainingCpu().compareTo(microservice.getTotalCpu().add(microservice.getCpu().multiply(i))) >= 0) {
            if (fogDevice.getRemainingMemory().compareTo(microservice.getTotalRam().add(microservice.getRam().multiply(i))) >= 0) {
                return 1;
            }
            System.out.println("NOT ENOUGH MEMORY FOR " + microservice.getMicroserviceId() + " in " + fogDevice.getNodeName());
            return -1;
        }
        System.out.println("NOT ENOUGH CPU FOR " + microservice.getMicroserviceId() + " in " + fogDevice.getNodeName());
        return -1;
    }

    private int compareForResources(FogDevice fogDevice, Microservice microservice) {
        if (fogDevice.getRemainingCpu().compareTo(microservice.getTotalCpu()) >= 0) {
            if (fogDevice.getRemainingMemory().compareTo(microservice.getTotalRam()) >= 0) {
                return 1;
            }
            System.out.println("NOT ENOUGH MEMORY FOR " + microservice.getMicroserviceId() + " in " + fogDevice.getNodeName());
            return -1;
        }
        System.out.println("NOT ENOUGH CPU FOR " + microservice.getMicroserviceId() + " in " + fogDevice.getNodeName());
        return -1;
    }

    private void sortFogDevices(List<FogDevice> inClusterDevices) {
        inClusterDevices.sort(Comparator.comparing(FogDevice::getRemainingCpu)
                .thenComparing(FogDevice::getRemainingMemory));
    }
}
