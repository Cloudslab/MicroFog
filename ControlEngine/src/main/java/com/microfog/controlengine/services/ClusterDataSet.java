package com.microfog.controlengine.services;

import com.microfog.controlengine.model.domainObjects.ClusterData;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.groups.UniJoin;
import org.jboss.logging.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//used to aggregate results from multiple clusters
public class ClusterDataSet {
    private final static Logger LOGGER = Logger.getLogger(ClusterDataSet.class.getName());

    UniJoin.Builder<ClusterData> resultBuilder = Uni.join().builder();
    Map<ClusterDataResource,String> requestResources = new HashMap<>();

    public ClusterDataSet(){
    }

    public void addRequestToList(ClusterDataResource requestResource, String clusterName){
        requestResources.put(requestResource,clusterName);
    }

    public  Uni<List<ClusterData>> sendRequests(){
        for(ClusterDataResource requestResource:requestResources.keySet()){
                resultBuilder.add(requestResource.getClusterData(requestResources.get(requestResource)));
        }
        return resultBuilder.joinAll().andFailFast();
    }
}
