package com.microfog.controlengine.model.deployment;

import java.io.Serializable;
import java.net.URL;

public class AppLevelResource implements Serializable {
    String yamlFile;
    String appId; // links the resource with application

    URL yamlUrl;

    public AppLevelResource(String yamlFile, String appId){
        this.yamlFile = yamlFile;
        this.appId = appId;
    }

    public String getYamlFile() {
        return yamlFile;
    }

    public String getAppId() {
        return appId;
    }

    public URL getYamlUrl() {
        return yamlUrl;
    }

    public void setYamlUrl(URL yamlUrl) {
        this.yamlUrl = yamlUrl;
    }
}
