package com.microfog.controlengine.services;

import com.microfog.controlengine.model.domainObjects.ClusterData;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.jboss.logging.Logger;

import java.net.URI;


public class ClusterDataResource {
    private final static Logger LOGGER = Logger.getLogger(ClusterDataResource.class.getName());
    private  final ClusterDataService clusterDataService;

    public ClusterDataResource(String url){
        clusterDataService = RestClientBuilder.newBuilder()
                .baseUri(URI.create(url))
                .build(ClusterDataService.class);
    }

    public Uni<ClusterData> getClusterData(String clusterName){
        LOGGER.info("Resource Request sent to next cluster" + System.currentTimeMillis());
        return clusterDataService.getClusterData(clusterName);
    }
}
