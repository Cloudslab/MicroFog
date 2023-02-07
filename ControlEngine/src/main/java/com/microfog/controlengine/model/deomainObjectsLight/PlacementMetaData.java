package com.microfog.controlengine.model.deomainObjectsLight;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.microfog.controlengine.model.domainObjects.ClusterData;
import com.microfog.controlengine.model.domainObjects.FogDevice;

import java.util.List;
import java.util.Map;

public class PlacementMetaData {
    List<ApplicationL> appInfo;
    List<FogDevice> inClusterDevices;
    Map<String, ClusterData>  otherClusterDevices;

    @JsonCreator
    @JsonPropertyOrder({"appInfo","inClusterDevices","otherClusterDevices"})
    public PlacementMetaData(List<ApplicationL> appInfo,List<FogDevice> inClusterDevices,Map<String, ClusterData>  otherClusterDevices){
        this.appInfo = appInfo;
        this.inClusterDevices = inClusterDevices;
        this.otherClusterDevices = otherClusterDevices;
    }
}
