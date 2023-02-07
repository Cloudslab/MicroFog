package com.microfog.controlengine.model.deployment;

public enum CommandLevelEnum {
    APP_LEVEL,   // namespaces, ingress access
    COMPOSITE_SERVICE_LEVEL,  // abstractions to enable creation of composite services and request routing (Service, Virtual Service, Gateway etc)
    MICROSERVICE_INSTANCE_LEVEL //pods
}
