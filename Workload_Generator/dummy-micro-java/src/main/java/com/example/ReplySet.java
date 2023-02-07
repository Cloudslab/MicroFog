package com.example;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.groups.UniJoin;
import org.jboss.logging.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReplySet {
    private final static Logger LOGGER = Logger.getLogger(ReplySet.class.getName());

    UniJoin.Builder<Reply> resultBuilder = Uni.join().builder();
    UniJoin.Builder<String> resultBuilder2 = Uni.join().builder();
    Map<RequestResource,Request> requestResources = new HashMap<>();

    public ReplySet(){
    }


    public void addRequestsToMap(RequestResource requestResource, Request request){
        requestResources.put(requestResource,request);
    }

    public  Uni<List<Reply>> sendRequests(){

        for(RequestResource requestResource:requestResources.keySet()){
            if(requestResource.isPartOfAggregate())
            resultBuilder.add(requestResource.sendRequest(requestResources.get(requestResource)));
            else {
                new Thread(
                        ()->{
                            System.out.println(requestResource.sendPOSTRequest(requestResources.get(requestResource)));
                        }
                ).start();
            }
        }
        return resultBuilder.joinAll().andFailFast();
    }
}
