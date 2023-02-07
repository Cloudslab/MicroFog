package com.microfog.controlengine.model.domainObjects;

import java.io.Serializable;

public class Dataflow implements Serializable {
    Microservice startMicroservice;
    Microservice endMicroservice;
    boolean invokationDirection = false;
    private double packetSize;
    private double instructionCount;
    private double rate;

    public Dataflow(Microservice startMicroservice, Microservice endMicroservice, boolean invokationDirection){
        this.startMicroservice = startMicroservice;
        this.endMicroservice = endMicroservice;
        this.invokationDirection = invokationDirection;
    }

    public Dataflow(Microservice startMicroservice, Microservice endMicroservice, double packetSize, double instructionCount, double rate, boolean invokationDirection) {
        this.startMicroservice = startMicroservice;
        this.endMicroservice = endMicroservice;
        this.packetSize = packetSize;
        this.instructionCount = instructionCount;
        this.rate = rate;
        this.invokationDirection = invokationDirection;
    }

    public boolean isInvokationDirection() {
        return invokationDirection;
    }

    public double getInstructionCount() {
        return instructionCount;
    }

    public double getPacketSize() {
        return packetSize;
    }

    public double getRate() {
        return rate;
    }

    public Microservice getEndMicroservice() {
        return endMicroservice;
    }

    public Microservice getStartMicroservice() {
        return startMicroservice;
    }
}
