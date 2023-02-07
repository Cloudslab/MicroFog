package com.microfog.controlengine.model.deployment;

import java.io.Serializable;
import java.net.URL;
import java.util.List;

public class ServiceLevelResource implements Serializable {
    String yamlFile;
    List<String> microserviceIdentifier; //microserviceIdentifier links service and deployment together. Can be mutiple microservice instances (eg:versions)

    URL yamlUrl; // url for minio object

    public ServiceLevelResource(String yamlFile, List<String> microserviceIdentifier){
        this.yamlFile = yamlFile;
        this.microserviceIdentifier = microserviceIdentifier;
    }

    public List<String> getMicroserviceIdentifier() {
        return microserviceIdentifier;
    }

    public String getYamlFile() {
        return yamlFile;
    }

    public URL getYamlUrl() {
        return yamlUrl;
    }

    public void setYamlUrl(URL yamlUrl) {
        this.yamlUrl = yamlUrl;
    }
}
