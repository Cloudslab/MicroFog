package com.microfog.controlengine.model.deployment;

import java.util.List;

public class Gateway extends ServiceLevelResource {
    String namespace;

    public Gateway(String yamlPath, String namespace, List<String> microserviceIdentifier){
        super(yamlPath,microserviceIdentifier);
        this.namespace = namespace;
    }

    public String getNamespace() {
        return namespace;
    }

}
