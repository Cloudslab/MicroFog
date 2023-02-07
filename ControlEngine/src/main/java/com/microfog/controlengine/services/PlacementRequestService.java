package com.microfog.controlengine.services;

import com.microfog.controlengine.model.domainObjects.PlacementRequest;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/MicroFog")
public interface PlacementRequestService {

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/addPR")
    public String addPR(@HeaderParam("cluster")String cluster, PlacementRequest placementRequest);

}
