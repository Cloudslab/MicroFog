package com.microfog.controlengine.model.deployment;

import java.util.List;

public class Service extends ServiceLevelResource {
    String namespace;

    public Service(String yamlPath, String namespace, List<String> microserviceIdentifier){
        super(yamlPath,microserviceIdentifier);
        this.namespace = namespace;
    }

    public String getNamespace() {
        return namespace;
    }

}
