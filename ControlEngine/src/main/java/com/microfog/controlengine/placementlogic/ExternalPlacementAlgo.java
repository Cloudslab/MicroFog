package com.microfog.controlengine.placementlogic;

import com.microfog.controlengine.model.domainObjects.*;
import com.microfog.controlengine.services.ExternalAlgoResource;
import com.microfog.controlengine.model.deomainObjectsLight.PlacementMetaData;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logging.Logger;


import java.util.List;
import java.util.Map;

public class ExternalPlacementAlgo extends PlacementAlgorithm{
    private final static Logger LOGGER = Logger.getLogger(ExternalPlacementAlgo.class.getName());

    String version = ConfigProvider.getConfig().getValue("controlengine.placementalgo.version", String.class);
    String algoUrl = ConfigProvider.getConfig().getValue("controlengine.placementalgourl", String.class);

    @Override
    public PlacementOutPut run(List<PlacementRequest> prs, Map<String, Application> appInfo, List<FogDevice> inClusterDevices, Map<String, ClusterData> adjacentClusterData) {
        return super.run(prs, appInfo, inClusterDevices, adjacentClusterData);
    }

    @Override
    protected PlacementOutPut executePlacementLogic() {
        PlacementOutPut placementOutPut = new PlacementOutPut();
        /**
         * call the external placement algo with version and other info. and get the output.
         */
        ExternalAlgoResource externalAlgoResource = new ExternalAlgoResource(algoUrl);
        LOGGER.info("Calling external placement algorithm ");
        PlacementMetaData placementMetaData = new PlacementMetaData(appInfoL,inClusterDevices, clusterData);
        placementOutPut = externalAlgoResource.executeAlgorithm(placementMetaData);
        LOGGER.info("Placement output received from external algo");

        return null;
    }








}
