package com.microfog.controlengine.model.deployment;

import java.util.ArrayList;
import java.util.List;

public class IstioVirtualService extends ServiceLevelResource {
    String namespace;

    Protocols routeProtocol = null;  // tcp, tls, https
    List<Route> routes = new ArrayList<>();

    public IstioVirtualService(String yamlPath, String namespace, List<String> microserviceIdentifier){
        super(yamlPath,microserviceIdentifier);
        this.namespace = namespace;
    }

    public IstioVirtualService(String yamlPath, String namespace, Protocols routeProtocol, List<String> microserviceIdentifier){
        super(yamlPath,microserviceIdentifier);
        this.namespace = namespace;
        this.routeProtocol = routeProtocol;
    }

    public void setRouteProtocol(Protocols routeProtocol) {
        this.routeProtocol = routeProtocol;
    }

    public Protocols getRouteProtocol() {
        return routeProtocol;
    }

    public String getNamespace() {
        return namespace;
    }

    public void addRoute(String subset, Integer weight){
        Route route = new Route(subset,weight);
        routes.add(route);
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public class Route{
        String destinationSubset;
        Integer weight;

        public Route( String subset, Integer weight){
            this.destinationSubset = subset;
            this.weight = weight;
        }

        public Integer getWeight() {
            return weight;
        }


        public String getDestinationSubset() {
            return destinationSubset;
        }
    }

    public enum Protocols{
        HTTP("http"),
        TLS("tls"),
        TCP("tcp");

        public final String label;

        Protocols(String label) {
            this.label = label;
        }
    }



}
