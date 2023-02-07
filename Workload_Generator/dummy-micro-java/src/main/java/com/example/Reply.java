package com.example;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

public class Reply {

    byte[] message;
    int dataProcessingTime; // milliseconds

    @JsonCreator
    @JsonPropertyOrder({"message", "dataProcessingTime"})
    public Reply(byte[] message, int dataProcessingTime) {
        this.message = message;
        this.dataProcessingTime = dataProcessingTime;
    }

    public int getDataProcessingTime() {
        return dataProcessingTime;
    }

    public byte[] getMessage() {
        return message;
    }
}
