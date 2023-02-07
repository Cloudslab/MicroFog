package com.microfog.controlengine.model.deployment;

import java.io.Serializable;

public class Command implements Serializable {
    CommandTypeEnum commandType;
    ResourceEnum resourceType;
    CommandLevelEnum commandLevel;
    Object resource;

    public Command(CommandTypeEnum commandType, CommandLevelEnum commandLevel, ResourceEnum resourceType, Object resource){
        this.commandType = commandType;
        this.commandLevel = commandLevel;
        this.resourceType = resourceType;
        this.resource = resource;
    }

    public CommandTypeEnum getCommandType() {
        return commandType;
    }

    public ResourceEnum getResourceType() {
        return resourceType;
    }

    public CommandLevelEnum getCommandLevel() {
        return commandLevel;
    }

    public Object getResource() {
        return resource;
    }

}
