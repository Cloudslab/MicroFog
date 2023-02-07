package com.microfog.controlengine.model.domainObjects;

import com.microfog.controlengine.controllers.DataMapperMetadata;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Singleton
public class PrQueue {

    private final static Logger LOGGER = Logger.getLogger(PrQueue.class.getName());

    int prCounter = 0;
    //todo double check concurrency for the queue;
    private static Queue<PlacementRequest> prQueue = new ConcurrentLinkedQueue<>();
    @ConfigProperty(name = "controlengine.placementmode.periodic")
    boolean isBatchPlacement;

    @Inject
    DataMapperMetadata dataMapper;

    public synchronized boolean addToQueue(PlacementRequest pr){
        /** domain specific validations of the PR
         * 1. application id is valid
         * 2. placed microservices are valid
         */
        Boolean valid = validatePR(pr);
        if(!valid)
            return valid;
        pr.setCreateTime(new Timestamp(System.currentTimeMillis()));
        return prQueue.add(pr);
    }

    private Boolean validatePR(PlacementRequest pr) {
        boolean validAppId = true;
        boolean validMicroserviceNames = true;

        String applicationId = pr.getApplicationId();
        List<PlacedMicroservice> placedMicroservices = pr.placedMicroservicesList;

        if(dataMapper.ifExists(applicationId)) {
            Application application = dataMapper.getObjectFromBucket(applicationId);
            for (PlacedMicroservice m:placedMicroservices){
                if(!application.getMicroserviceids().contains(m.microserviceName)){
                    validMicroserviceNames =false;
                }
                if(!validMicroserviceNames)
                    break;
            }
        }
        else {
            validAppId = false;
            LOGGER.info("PR validation failed :  no matching APP ID in redis");
        }

        if(!validAppId || !validMicroserviceNames)
            return false;
        else
            return true;
    }

    /**
     * if in Batch placement mode,
     * @return all placement requests in the queue, or else
     * @return the first one in the queue
     */
    public  synchronized List<PlacementRequest> getFromPrQueue(){
        List<PlacementRequest> prs = new ArrayList<>();
        synchronized (prQueue) {
            if(isBatchPlacement) {
//                LOGGER.info("PR Queue size before logic execution (Batch Placement): " + prQueue.size());
                while (!prQueue.isEmpty())
                    prs.add(prQueue.poll());
//                LOGGER.info("PR Queue size before logic execution (Batch Placement): " + prQueue.size());
            }
            else{
                if(!prQueue.isEmpty())
                    prs.add(prQueue.poll());
            }
        }
        return prs;
    }

    public int getPrQueueSize(){
        return prQueue.size();
    }
}
