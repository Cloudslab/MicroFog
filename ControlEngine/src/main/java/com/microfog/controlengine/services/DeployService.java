package com.microfog.controlengine.services;

import com.microfog.controlengine.model.domainObjects.DeploymentInfo;
import io.smallrye.mutiny.Uni;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/MicroFog")
public interface DeployService {

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/submitDeploymentInfo")
    public Uni<String> submitDeploymentInfo(@HeaderParam("cluster") String clustername, List<DeploymentInfo> deploymentInfo );

}
