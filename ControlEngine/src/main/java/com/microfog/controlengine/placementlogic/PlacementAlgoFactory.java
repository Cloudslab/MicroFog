package com.microfog.controlengine.placementlogic;

import javax.inject.Singleton;

@Singleton
public class PlacementAlgoFactory {

    public PlacementAlgorithm get(String algoName) {
        switch(algoName){
            case "RESOURCE_AWARE":
                return new ResourceAwarePlacement();
            case "DISTRIBUTED_PLACEMENT":
                return new DistributedPlacement();
            default:
                    return null;
        }
    }
}
