package com.microfog.controlengine.model.domainObjects;

import com.microfog.controlengine.services.PlacementRequestResource;
import io.quarkus.scheduler.Scheduled;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logging.Logger;


import javax.inject.Singleton;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

@Singleton
public class ForwardRequestQueue {

    private final static Logger LOGGER = Logger.getLogger(ForwardRequestQueue.class.getName());
    String forwardUrl = ConfigProvider.getConfig().getValue("controlengine.forwardUrl", String.class);


    //todo double check concurrency for the queue;
    private static Queue<PlacementRequest> forwardPrQueue = new ConcurrentLinkedQueue<>();

    public synchronized boolean addToQueue(PlacementRequest placementRequest){
        return forwardPrQueue.add(placementRequest);
    }

    public synchronized void addToQueue(List<PlacementRequest> placementRequests){
        forwardPrQueue.addAll(placementRequests);
    }

    @Scheduled(every="0.001s")
    public void forwardRequest(){
//        System.out.println("FORWARD TIME : " + System.currentTimeMillis());
       while(!forwardPrQueue.isEmpty()){
           PlacementRequest pr = forwardPrQueue.poll();
           String nextClusterName = pr.getNextClusterToSend();
           PlacementRequestResource placementRequestResource = new PlacementRequestResource(forwardUrl);
           LOGGER.info("Incomplete PRs forwarded to : " + nextClusterName + " from  ");
           String reply = placementRequestResource.newPr(nextClusterName, pr);
           long t = System.currentTimeMillis();
           System.out.println("Reply to forwarded reply : " + reply + " - " + t);
       }
    }

}
