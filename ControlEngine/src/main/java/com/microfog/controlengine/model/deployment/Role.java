package com.microfog.controlengine.model.deployment;

public class Role extends AppLevelResource {
    String namespace;

    public Role(String yamlFile, String appId, String namespace) {
        super(yamlFile, appId);
        this.namespace = namespace;
    }

    public String getNamespace() {
        return namespace;
    }
}
