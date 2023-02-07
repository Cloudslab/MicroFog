package com.microfog.controlengine.model.deployment;

import java.io.Serializable;
import java.net.URL;

public class MicroserviceLevelResource implements Serializable {
    String yamlFile;
    String microserviceIdentifier; //microserviceIdentifier links service and deployment together.

    URL yamlUrl; // url string

    public MicroserviceLevelResource(String yamlFile, String microserviceIdentifier){
        this.yamlFile = yamlFile;
        this.microserviceIdentifier = microserviceIdentifier;
    }

    public String getMicroserviceIdentifier() {
        return microserviceIdentifier;
    }

    public String getYamlFile() {
        return yamlFile;
    }

    public void setYamlUrl(URL yamlUrl) {
        this.yamlUrl = yamlUrl;
    }

    public URL getYamlUrl() {
        return yamlUrl;
    }
}
