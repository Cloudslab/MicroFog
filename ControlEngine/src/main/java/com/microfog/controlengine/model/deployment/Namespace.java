package com.microfog.controlengine.model.deployment;

import java.util.Map;

public class Namespace extends AppLevelResource {

    String name;
    Map<String,String> labels;

    public Namespace(String applicationId, String name, Map<String,String> labels){
        super(null, applicationId);
        this.name = name;
        this.labels = labels;
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getLabels() {
        return labels;
    }
}
