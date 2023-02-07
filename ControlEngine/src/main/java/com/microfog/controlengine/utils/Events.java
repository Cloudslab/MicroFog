package com.microfog.controlengine.utils;

public enum Events {
    NEW_PR_RECEIVED("New PR Received"),
    PR_PROCESSING_BEGIN("PR Processing Begin"),
    RETRIEVE_META_DATA_START("Meta data retireval start"),
    RETRIEVE_OTHER_CLUSTER_DATA_BEGIN("Retrieve Other Cluster Device Data"),
    RETRIEVE_OTHER_CLUSTER_DATA_END("Other Cluster Device Data Received"),
    RETRIEVE_META_DATA_END("Meta data retireval start"),
    DEPLOY_COMMANDS_FORWARD("Commands to deploy"),

    LOAD_YAML_FILE_BEGIN("Load Yaml File - Start"),
    LOAD_YAML_FILE_COMPLETED("Load Yaml File - Edge"),
    PLACEMENT_ALGO_START(" Placement Algo Execution Started"),
    PLACEMENT_ALGO_COMPLETED("Placement Algo Completed"),
    DEPLOYMENT_STARTED("Deploying command creation started"),
//    DEPLOYMENT_COMMANDS_ADDED_TO_QUEUE("Deploying command execution completed"),
    LB_DEPLOYMENT_START("LB Deploying command execution STARTED"),
    LB_DEPLOYMENT_COMMANDS_ADDED_TO_QUEUE("LB Deploying command execution completed"),
    LB_COMMAND_FORWARDING_START("FOrwarding LB commands"),
    LB_COMMAND_EXECUTION_COMPLETED( "LB command replies received"),
    COMMAND_EXECUTION_COMPLETED_ALL_CLUSTER("forwarded commands completed"),
    COMMAND_EXECUTION_COMPLETED_THIS_CLUSTER("completed for this cluster in distributed mode"),
    PLACEMENT_COMPLETED("Placement completed"),
    DEPLOYMENT_OF_RECEIVED_COMMANDS_STARTED("Received commands deploying start"),
    COMMAND_EXECUTION_RECEIVED_COMMANDS_COMPLETED("Received commands deployed"),


    REQUEST_FORWARD("Request forward"),

    RETRIEVE_REDIS_DATA_BEGIN("Querying Redis database for app info"),
    RETRIEVE_REDIS_DATA_END("Querying Redis database for app info"),
    COMMAND_FORWARDING_START("Command Forwarding Started");

    public final String label;

    Events(String label) {
        this.label = label;
    }
}
