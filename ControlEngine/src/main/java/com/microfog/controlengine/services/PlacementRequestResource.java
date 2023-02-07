package com.microfog.controlengine.services;

import com.microfog.controlengine.model.domainObjects.PlacementRequest;
import com.microfog.controlengine.utils.Events;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.jboss.logging.Logger;

import java.net.URI;


public class PlacementRequestResource {
    private final static Logger LOGGER = Logger.getLogger(PlacementRequestResource.class.getName());
    private  final PlacementRequestService placementRequestService;

    public PlacementRequestResource(String url){
        placementRequestService = RestClientBuilder.newBuilder()
                .baseUri(URI.create(url))
                .build(PlacementRequestService.class);
    }

    public String newPr(String clusterName, PlacementRequest placementRequest){
        LOGGER.info(Events.REQUEST_FORWARD + " - " + System.currentTimeMillis());
        return placementRequestService.addPR(clusterName, placementRequest);
    }
}
