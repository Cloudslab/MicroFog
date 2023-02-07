package com.microfog.controlengine.services;

import com.microfog.controlengine.controllers.DeploymentHandler;
import com.microfog.controlengine.model.domainObjects.DeploymentInfo;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.groups.UniJoin;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeployResourceDataSet {
    private final static Logger LOGGER = Logger.getLogger(DeployResourceDataSet.class.getName());

    String clusterName = ConfigProvider.getConfig().getValue("cluster.name", String.class);

    UniJoin.Builder<String> resultBuilder = Uni.join().builder();
    Map<DeployResource, List<DeploymentInfo>> requestResources = new HashMap<>();

    public void addRequestToList(DeployResource requestResource, List<DeploymentInfo> deploymentInfos) {
        requestResources.put(requestResource, deploymentInfos);
    }

    public Uni<List<String>> sendRequests(DeploymentHandler deploymentHandler) {
        List<DeploymentInfo> forCurrentCluster = new ArrayList<>();
        for (DeployResource requestResource : requestResources.keySet()) {
            if (!requestResource.getCluster().equals(clusterName)) {
                LOGGER.info("DEPLOY ON " + requestResource.getCluster() + " AND CLUSTER NAME IS" + clusterName);
                resultBuilder.add(requestResource.submitDeploymentInfo(requestResource.getCluster(), requestResources.get(requestResource)));
            }
            else {
                forCurrentCluster = requestResources.get(requestResource);
            }
        }
        System.out.println("Current cluster commands sent for execution : " +System.currentTimeMillis());
        resultBuilder.add(deploymentHandler.deployCommands(forCurrentCluster));
        return resultBuilder.joinAll().andFailFast();
    }

}
