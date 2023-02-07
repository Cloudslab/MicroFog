package com.microfog.controlengine.model.deployment;

public class Rolebinding extends AppLevelResource {
    String namespace;
    Role relatedRole = null;

    public Rolebinding(String yamlFile, String appId, String namespace) {
        super(yamlFile, appId);
        this.namespace = namespace;
    }

    public Rolebinding(String yamlFile, String appId, String namespace,Role role) {
        super(yamlFile, appId);
        this.namespace = namespace;
        this.relatedRole = role;
    }

    public String getNamespace() {
        return namespace;
    }
}
