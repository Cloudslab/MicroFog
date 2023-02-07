package com.microfog.controlengine.services;

import com.microfog.controlengine.model.domainObjects.DeploymentInfo;
import com.microfog.controlengine.utils.Events;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.jboss.logging.Logger;

import java.net.URI;
import java.util.List;


public class DeployResource {
    private final static Logger LOGGER = Logger.getLogger(DeployResource.class.getName());
    private  final DeployService deployService;
    private String cluster;

    public DeployResource(String url,String cluster){
        this.cluster = cluster;
        deployService = RestClientBuilder.newBuilder()
                .baseUri(URI.create(url))
                .build(DeployService.class);
    }

    public Uni<String> submitDeploymentInfo(String clusterName, List<DeploymentInfo> deploymentInfo){
        LOGGER.info(Events.DEPLOY_COMMANDS_FORWARD + " - " + System.currentTimeMillis() + "to cluster" + clusterName);
        return deployService.submitDeploymentInfo(clusterName, deploymentInfo);
    }

    public String getCluster() {
        return cluster;
    }
}
