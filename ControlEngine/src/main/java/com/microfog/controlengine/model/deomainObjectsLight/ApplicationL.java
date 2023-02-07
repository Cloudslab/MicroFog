package com.microfog.controlengine.model.deomainObjectsLight;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.microfog.controlengine.model.domainObjects.QoSParameter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Application contains two types of data
 * 1. Application Model (microservices, services, dataflows, resource requirements for microservices, QoS requirements for services)
 * 2. Deployment Data (deployment commands at application level, at microservice level, at routing level)
 */
public class ApplicationL implements Serializable {
     String applicationId; //links to placement request

     // model
     List<MicroserviceL> microservicesList = new ArrayList<>();
     List<CompositeServiceL> compositeServices = new ArrayList<>();
     List<DataflowL> dataflows = new ArrayList<>();

     // qos
     List<QoSParameter> qoSParameters = new ArrayList<>();

     @JsonCreator
     @JsonPropertyOrder({"applicationId","microservicesList","compositeServices","dataflows","qoSParameters"})
     public ApplicationL(String applicationId, List<MicroserviceL> microservicesList, List<CompositeServiceL> compositeServices,
                         List<DataflowL> dataflows, List<QoSParameter> qoSParameters){
          this.applicationId = applicationId;
          this.microservicesList = microservicesList;
          this.compositeServices = compositeServices;
          this.dataflows = dataflows;
          this.qoSParameters = qoSParameters;
     }


}
