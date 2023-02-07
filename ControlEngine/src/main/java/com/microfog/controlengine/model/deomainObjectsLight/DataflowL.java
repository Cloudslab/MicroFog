package com.microfog.controlengine.model.deomainObjectsLight;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.microfog.controlengine.model.domainObjects.Dataflow;

import java.io.Serializable;

public class DataflowL implements Serializable {
    String dataflowId;

    String startMicroservice;
    String endMicroservice;
    boolean invokationDirection = false;
    private double packetSize;
    private double instructionCount;
    private double rate;

    public DataflowL(Dataflow dataflow){
        this.startMicroservice = dataflow.getStartMicroservice().getMicroserviceId();
        this.endMicroservice = dataflow.getEndMicroservice().getMicroserviceId();
        this.invokationDirection = dataflow.isInvokationDirection();
        this.instructionCount = dataflow.getInstructionCount();
        this.rate = dataflow.getRate();
        this.packetSize = dataflow.getPacketSize();

        this.dataflowId = startMicroservice + "_" + endMicroservice;
    }

    @JsonCreator
    @JsonPropertyOrder({"dataflowId","startMicroservice","endMicroservice","invokationDirection","instructionCount","rate","packetSize"})
    public DataflowL(String dataflowId, String startMicroservice, String endMicroservice, Boolean invokationDirection,
                     int instructionCount, int rate, int packetSize){
        this.startMicroservice = startMicroservice;
        this.endMicroservice = endMicroservice;
        this.invokationDirection = invokationDirection;
        this.instructionCount = instructionCount;
        this.rate = rate;
        this.packetSize = packetSize;

        this.dataflowId = dataflowId;
    }


}
