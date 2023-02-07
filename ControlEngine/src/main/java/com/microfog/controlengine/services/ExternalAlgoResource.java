package com.microfog.controlengine.services;

import com.microfog.controlengine.model.deomainObjectsLight.PlacementMetaData;
import com.microfog.controlengine.placementlogic.PlacementOutPut;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.jboss.logging.Logger;

import java.net.URI;


public class ExternalAlgoResource {
    private final static Logger LOGGER = Logger.getLogger(ExternalAlgoResource.class.getName());
    private  final ExternalAlgoService externalAlgoService;

    public ExternalAlgoResource(String url){
        externalAlgoService = RestClientBuilder.newBuilder()
                .baseUri(URI.create(url))
                .build(ExternalAlgoService.class);
    }

    public PlacementOutPut executeAlgorithm(PlacementMetaData placementMetaData){
        LOGGER.info("Placement Request Forward to next cluster" + System.currentTimeMillis());
        return externalAlgoService.executeAlgo(placementMetaData);
    }
}
