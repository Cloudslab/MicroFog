package com.microfog.controlengine.model.deployment;

public class ConfigMap extends MicroserviceLevelResource{
    String namespace;

    public ConfigMap(String yamlPath, String namespace, String microserviceIdentifier){
        super(yamlPath,microserviceIdentifier);
        this.namespace = namespace;
    }

    public String getNamespace() {
        return namespace;
    }
}
