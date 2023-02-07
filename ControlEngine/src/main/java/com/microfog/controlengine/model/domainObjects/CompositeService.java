package com.microfog.controlengine.model.domainObjects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CompositeService implements Serializable {

    String compositeServiceId;
    List<Microservice> microservices = new ArrayList<>();
    List<List<Dataflow>> datapaths = new ArrayList<>();  // should be in proper order of data flow

    public CompositeService(String compositeServiceId, List<Microservice> microservices, List<List<Dataflow>> datapaths){
        this.compositeServiceId =compositeServiceId;
        this.microservices = microservices;
        this.datapaths = datapaths;
    }

    public String getCompositeServiceId() {
        return compositeServiceId;
    }

    public List<List<Dataflow>> getDatapaths() {
        return datapaths;
    }

    public List<Microservice> getMicroservices() {
        return microservices;
    }
}
