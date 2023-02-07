package com.example;

import org.jboss.logging.Logger;

import java.util.*;

public class RequestProcessor {
    private final static Logger LOGGER = Logger.getLogger(RequestProcessor.class.getName());

    Request request;
    MicroserviceControlInfoInterface controlInfo;

    public RequestProcessor(Request request, MicroserviceControlInfoInterface microserviceControlInfo) {
        this.request = request;
        this.controlInfo = microserviceControlInfo;
    }


    public void processRequest() {
        int processingTime = request.getDataProcessingTime() * 1000000;
        for (long stop = System.nanoTime() + processingTime; stop > System.nanoTime(); ) {
            continue;
        }
    }

    public Reply processReceivedReply(Reply reply, MicroserviceControlInfoInterface.NextServiceData selectedMicroservice) {
        LOGGER.info("Rply received at: " + System.currentTimeMillis());
        int processingTime = reply.getDataProcessingTime() * 1000000;
        for (long stop = System.nanoTime() + processingTime; stop > System.nanoTime(); ) {
            continue;
        }

        return generateReturnReply(selectedMicroservice.repmsgsize(),selectedMicroservice.repprocesstime());
    }

    public boolean isLastProcess() {
        if (controlInfo.nextservices() == 0)
            return true;
        else
            return false;
    }

    public Reply getReply() {
        LOGGER.info("Generating direct reply");
        MicroserviceControlInfoInterface.DirectReply directReply = controlInfo.directreply().get();
        Random rd = new Random();
        byte[] arr = new byte[directReply.msgsize()];
        rd.nextBytes(arr);
        Reply reply = new Reply(arr, directReply.processingtime());
        return reply;
    }

    public Reply generateReturnReply(int msgSize, int processingTime) {
        Random rd = new Random();
        byte[] arr = new byte[msgSize];
        rd.nextBytes(arr);
        Reply reply = new Reply(arr, processingTime);
        return reply;
    }

    public Request generateNextRequest(int msgSize, int processingTime) {
        Random rd = new Random();
        byte[] arr = new byte[msgSize];
        rd.nextBytes(arr);
        Request request = new Request(arr, processingTime);
        return request;
    }

    public Reply callNextMicroservices(int currentLevel) {
        int numLevels = controlInfo.sync().size();
        int selectedCandidate = -1;
        //non aggregating services (can be single chains or POST)
        List<Integer> syncM = new ArrayList<>();
        if(controlInfo.sync().get(currentLevel).services().isPresent())
            syncM = controlInfo.sync().get(currentLevel).services().get();

        ReplySet replySet = new ReplySet();
        for(Integer i:syncM) {
            RequestResource requestResource = new RequestResource(controlInfo.nextservice().get().get(i).url());
            if(controlInfo.nextservice().get().get(i).type().equals("post"))
                requestResource.setPartOfAggregate(false);
            else {
                selectedCandidate = i;
                requestResource.setPartOfAggregate(true);
            }
            Request request = generateNextRequest(controlInfo.nextservice().get().get(i).reqmsgsize(),
                    controlInfo.nextservice().get().get(i).reqprocesstime());
            replySet.addRequestsToMap(requestResource,request);
        }
        //candidate set id, candidate microservice id
        Map<Integer,Integer> synsMCandidates = new HashMap<>();

        if(controlInfo.sync().get(currentLevel).candidateids().isPresent()) {
            List<Integer> candidateIds = controlInfo.sync().get(currentLevel).candidateids().get();
            for(int id:candidateIds) {
                int nextCandidateServiceIndex = -1;
                    if (controlInfo.candidateselection().get().equals("random")) {
                        nextCandidateServiceIndex = selectRandomMicroservice(id);
                    } else if (controlInfo.candidateselection().get().equals("weighted")) {
                        nextCandidateServiceIndex = selectWeightedMicroservice(id);
                    }
                    synsMCandidates.put(id,nextCandidateServiceIndex);
            }
        }


        if(controlInfo.sync().get(currentLevel).aggregatorid().isPresent()){
            MicroserviceControlInfoInterface.Aggregator aggregator = controlInfo.aggregators().get().get(controlInfo.sync().get(currentLevel).aggregatorid().get());
            List<Integer> synAg = new ArrayList<>();
            if(aggregator.services().isPresent())
                synAg = aggregator.services().get();
            if(aggregator.candidateids().isPresent()){
                for(int i: aggregator.candidateids().get()){
                    synAg.add(synsMCandidates.get(i));
                    synsMCandidates.remove(i);
                }
            }

            for(Integer i:synAg) {
                selectedCandidate = i;
                RequestResource requestResource = new RequestResource(controlInfo.nextservice().get().get(i).url());
                requestResource.setPartOfAggregate(true);
                Request request = generateNextRequest(controlInfo.nextservice().get().get(i).reqmsgsize(),
                        controlInfo.nextservice().get().get(i).reqprocesstime());
                replySet.addRequestsToMap(requestResource,request);
            }
        }


        for(int key:synsMCandidates.keySet()){
            RequestResource requestResource = new RequestResource(controlInfo.nextservice().get().get(synsMCandidates.get(key)).url());
            if(controlInfo.nextservice().get().get(synsMCandidates.get(key)).type().equals("post"))
                requestResource.setPartOfAggregate(false);
            else {
                requestResource.setPartOfAggregate(true);
                //just one in this case
                selectedCandidate = synsMCandidates.get(key);
            }
            Request request = generateNextRequest(controlInfo.nextservice().get().get(synsMCandidates.get(key)).reqmsgsize(),
                    controlInfo.nextservice().get().get(synsMCandidates.get(key)).reqprocesstime());
            replySet.addRequestsToMap(requestResource,request);
        }

        return processAggregatedReply(replySet.sendRequests().await().indefinitely(), currentLevel,selectedCandidate);
    }

    private Reply processAggregatedReply(List<Reply> replies, int currentLevel, int selectedCandidate) {
        processReceivedReply(replies.get(0),controlInfo.nextservice().get().get(selectedCandidate));
        if(controlInfo.sync().size()-1>currentLevel)
               return callNextMicroservices(currentLevel+1);
        else {
            Reply reply = generateReturnReply(controlInfo.nextservice().get().get(0).repmsgsize(),
                    controlInfo.nextservice().get().get(0).repprocesstime());
            return reply;
        }
    }

    private int selectWeightedMicroservice(Integer id) {
        int size = controlInfo.candidates().get().get(id).services().size();
        double totalWeight = 0.0;
        for (int i:controlInfo.candidates().get().get(id).services()) {
            totalWeight += controlInfo.nextservice().get().get(i).weight().get();
        }
        Random r = new Random();
        double randomVal = r.nextDouble()* totalWeight;
        double countWeight = 0.0;
        for (int i:controlInfo.candidates().get().get(id).services()) {
            countWeight += controlInfo.nextservice().get().get(i).weight().get();
            if(countWeight >= randomVal)
                return i;
        }
        return -1;
    }

    private int selectRandomMicroservice(Integer id) {
        int size = controlInfo.candidates().get().get(id).services().size();
        Random r = new Random();
        return controlInfo.candidates().get().get(id).services().get(r.nextInt(size));
    }

}
