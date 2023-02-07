package com.microfog.controlengine.model.deomainObjectsLight;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.microfog.controlengine.model.domainObjects.CompositeService;
import com.microfog.controlengine.model.domainObjects.Dataflow;
import com.microfog.controlengine.model.domainObjects.Microservice;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CompositeServiceL implements Serializable {

    String compositeServiceId;
    List<String> microservices = new ArrayList<>();
    List<List<String>> datapaths = new ArrayList<>(); // dataflowids

    @JsonCreator
    @JsonPropertyOrder({"compositeServiceId","microservices","datapaths"})
    public CompositeServiceL(String compositeServiceId, List<String> microservices, List<List<String>> datapaths){
        this.compositeServiceId =compositeServiceId;
        this.microservices = microservices;
        this.datapaths = datapaths;
    }

    public CompositeServiceL(CompositeService c){
        this.compositeServiceId = c.getCompositeServiceId();
        for(Microservice m:c.getMicroservices()){
            microservices.add(m.getMicroserviceId());
        }
        for (List<Dataflow> dp:c.getDatapaths()){
            List<String> p = new ArrayList<>();
            for(Dataflow d:dp ){
                String dataflowId = d.getStartMicroservice().getMicroserviceId()+"_"+d.getEndMicroservice().getMicroserviceId();
                p.add(dataflowId);
            }
            datapaths.add(p);
        }
    }

    public String getCompositeServiceId() {
        return compositeServiceId;
    }
}
