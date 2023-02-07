package com.microfog.controlengine.services;

import com.microfog.controlengine.model.deomainObjectsLight.PlacementMetaData;
import com.microfog.controlengine.placementlogic.PlacementOutPut;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/Placement")
public interface ExternalAlgoService {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/executeAlgo")
    public PlacementOutPut executeAlgo(PlacementMetaData placementMetaData);

}
