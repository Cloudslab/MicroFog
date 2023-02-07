package com.microfog.controlengine.services;

import com.microfog.controlengine.model.domainObjects.ClusterData;
import io.smallrye.mutiny.Uni;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/MicroFog")
public interface ClusterDataService {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getClusterData")
    public Uni<ClusterData> getClusterData(@HeaderParam("cluster")String cluster);

}
