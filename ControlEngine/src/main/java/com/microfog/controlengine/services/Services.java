package com.microfog.controlengine.services;

import com.microfog.controlengine.controllers.ClusterDataFinder;
import com.microfog.controlengine.controllers.DeploymentHandler;
import com.microfog.controlengine.model.domainObjects.ClusterData;
import com.microfog.controlengine.model.domainObjects.DeploymentInfo;
import com.microfog.controlengine.model.domainObjects.PlacementRequest;
import com.microfog.controlengine.model.domainObjects.PrQueue;
import com.microfog.controlengine.utils.Events;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Set;

@Path("/MicroFog")
public class Services {

    private final static Logger LOGGER = Logger.getLogger(Services.class.getName());

    @Inject
    PrQueue prQueue;
    @Inject
    Validator validator;
    @Inject
    ClusterDataFinder clusterDataFinder;
    @Inject
    DeploymentHandler deploymentHandler;

    String clusterName = ConfigProvider.getConfig().getValue("cluster.name", String.class);
    @ConfigProperty(name = "controlengine.operationmode.primary")
    Boolean operationmode;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/home")
    public String hello(@HeaderParam("cluster") String clustername) {
        if(clusterName.equals(clustername))
        return "Hello from MicroFog Scheduler - " + clustername;
        else
            return "Hello from MicroFog Scheduler - " + clusterName + "( Error directing to requested cluster - " + clustername + ")";
    }

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/addPR")
    public String addPR(@HeaderParam("cluster") String clustername, PlacementRequest pr){
        long t = System.currentTimeMillis();
        LOGGER.info(Events.NEW_PR_RECEIVED.toString() + " - " + t + " - PR Id - "+ pr.getPrId() );
        if(!operationmode)
            return " Operation not supported in Central Mode";
        //header validation
        if(!clusterName.equals(clustername))
            return "Error with roting : PR received by - " + clusterName + "( Error directing to requested cluster - " + clustername + ")";
        //initial validations of the request
        Set<ConstraintViolation<PlacementRequest>> violations = validator.validate(pr);
        if(!violations.isEmpty())
            return "One or more required Fields of the placement request missing";
        boolean statue = prQueue.addToQueue(pr);
        return (statue==true)?"Placement Request Successfully Submitted ":"Error Occurred when adding the Placement Request to the queue";
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getClusterData")
    public ClusterData getClusterData(@HeaderParam("cluster") String clustername) {
        return clusterDataFinder.getResourceData();
    }

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/submitDeploymentInfo")
    public Uni<String> submitDeploymentInfo(@HeaderParam("cluster") String clustername, List<DeploymentInfo> deploymentInfo){
        return deploymentHandler.deployCommands(deploymentInfo);
    }




    //todo
    // to integrate placement algorithms
    // to redirect placement request to other clusters.

}