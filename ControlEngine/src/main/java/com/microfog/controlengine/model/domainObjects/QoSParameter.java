package com.microfog.controlengine.model.domainObjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.io.Serializable;

public class QoSParameter implements Serializable {

    public static String APP_LEVEL = "QoS App Level";
    public static String MICRO_LEVEL = "QoS Microservice Level";
    public static String SERVICE_LEVEL = "Qos Composite Service Level";

    public static String LATENCY = "latency";
    public static String BUDGET = "budget";
    public static String THROUGHPUT = "throughput";
    public static String AVAILABILITY = "availability";

    String qosLevel;
    String identifier; // appId, serviceId or microserviceId
    String parameter;
    double value;

    @JsonCreator
    @JsonPropertyOrder({"qosLevel", "identifier","parameter","value"})
    public QoSParameter(String qosLevel, String identifier, String parameter,  double value){
        this.qosLevel = qosLevel;
        this.identifier = identifier;
        this.parameter = parameter;
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public String getQosLevel() {
        return qosLevel;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getParameter() {
        return parameter;
    }
}
