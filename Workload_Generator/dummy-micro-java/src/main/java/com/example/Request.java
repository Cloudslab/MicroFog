package com.example;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

public class Request {

    byte[] message;
    int dataProcessingTime; // milliseconds

    //todo
    // extend this to include data processing methods as well. (either a time or a particular function to call)

    @JsonCreator
    @JsonPropertyOrder({"message", "dataProcessingTime"})
    public Request(byte[] message, int dataProcessingTime){
        this.message = message;
        this.dataProcessingTime = dataProcessingTime;
    }

    public byte[] getMessage() {
        return message;
    }

    public int getDataProcessingTime() {
        return dataProcessingTime;
    }
}
