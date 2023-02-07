package com.microfog.controlengine.model.deomainObjectsLight;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.io.Serializable;
import java.math.BigDecimal;

public class MicroserviceL implements Serializable {
    String microserviceId;

    //resources
    BigDecimal ram;
    BigDecimal cpu;
    BigDecimal storage;
    BigDecimal throughput; //request rate supported by resources

    public static final String CPU = "cpu";
    public static final String RAM = "ram";
    public static final String STORAGE = "storage";
    public static final String THROUGHPUT = "throughput";

    @JsonCreator
    @JsonPropertyOrder({"microserviceId","ram","cpu","storage","throughput"})
    public MicroserviceL(String microserviceId,BigDecimal ram,BigDecimal cpu,BigDecimal storage,BigDecimal throughput){
        this.microserviceId = microserviceId;
        this.ram = ram;
        this.cpu = cpu;
        this.storage = storage;
        this.throughput = throughput;
    }

}
