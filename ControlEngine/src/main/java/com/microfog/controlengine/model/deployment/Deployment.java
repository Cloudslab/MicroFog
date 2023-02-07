package com.microfog.controlengine.model.deployment;


public class Deployment extends MicroserviceLevelResource {
    String namespace;
    String microserviceIdentifier; //microserviceIdentifier links service and deployment together.

    public Deployment(String yamlPath, String namespace, String microserviceIdentifier) {
        super(yamlPath, microserviceIdentifier);
        this.namespace = namespace;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getMicroserviceIdentifier() {
        return microserviceIdentifier;
    }


}
