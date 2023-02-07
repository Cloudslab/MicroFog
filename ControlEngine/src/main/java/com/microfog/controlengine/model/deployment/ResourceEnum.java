package com.microfog.controlengine.model.deployment;

public enum ResourceEnum {
    NS("ns"),
    SERVICE("service"),
    DEPLOYMENT("deployment"),
    POD("pod"),
    ROLE("role"),
    ROLEBINDING("rolebinding"),
    CONFIGMAP("configmap"),

    GW("istio_gateway"),
    VS("virtual_service"),
    DR("destination_rules");

    public final String label;

    ResourceEnum(String label) {
        this.label = label;
    }
}
