package com.microfog.controlengine.controllers;

import com.microfog.controlengine.model.deployment.IstioDestinationRule;
import com.microfog.controlengine.model.deployment.IstioVirtualService;
import io.fabric8.istio.api.networking.v1alpha3.*;
import io.fabric8.istio.client.DefaultIstioClient;
import io.fabric8.istio.client.IstioClient;
import io.fabric8.kubernetes.client.ConfigBuilder;
import org.jboss.logging.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class IstioResourceHandler {

    private static final Logger LOG = Logger.getLogger(IstioResourceHandler.class);

    IstioClient istioClient;

    @PostConstruct
    public void init() {
        ConfigBuilder config = new ConfigBuilder();
        istioClient = new DefaultIstioClient(config.build());
    }

    public boolean createDestinationRule(URL filepath, String namespace) {
        DestinationRule destinationRule = istioClient.v1alpha3().destinationRules().load(filepath).get();

        try {
            destinationRule = istioClient.v1alpha3().destinationRules().inNamespace(namespace).create(destinationRule);
            LOG.info("Created destination rule with name " + destinationRule.getMetadata().getName());
            return true;
        } catch (Exception e) {
            LOG.info("Exception occurred in destination rule creation: " + e.getLocalizedMessage());
            return false;
        }

    }

    public boolean createDestinationRule(URL filepath, String namespace, List<IstioDestinationRule.IstioSubset> subsets) {
        DestinationRule destinationRule = istioClient.v1alpha3().destinationRules().load(filepath).get();
        updateDesinationRule(destinationRule, subsets);
        try {
            destinationRule = istioClient.v1alpha3().destinationRules().inNamespace(namespace).resource(destinationRule).create();
            LOG.info("Created destination rule with name " + destinationRule.getMetadata().getName() + "with subsets : " +
                    destinationRule.getSpec().getSubsets().toString() + " - " + System.currentTimeMillis());
            return true;
        } catch (Exception e) {
            LOG.info("Exception occurred in destination rule creation: " + e.getLocalizedMessage());
            return false;
        }

    }


    public boolean createGateway(URL filepath, String namespace) {
        Gateway gateway = istioClient.v1alpha3().gateways().load(filepath).get();

        try {
            gateway = istioClient.v1alpha3().gateways().inNamespace(namespace).create(gateway);
            LOG.info("Created istio gateway with name " + gateway.getMetadata().getName() + " - " + System.currentTimeMillis());
            return true;
        } catch (Exception e) {
            LOG.info("Exception occurred istio gateway creation: " + e.getLocalizedMessage());
            return false;
        }

    }

    public boolean createVirtualService(URL filepath, String namespace) {
        VirtualService virtualService = istioClient.v1alpha3().virtualServices().load(filepath).get();

        try {
            virtualService = istioClient.v1alpha3().virtualServices().inNamespace(namespace).resource(virtualService).create();
            LOG.info("Created virtual service with name " + virtualService.getMetadata().getName());
            return true;
        } catch (Exception e) {
            LOG.info("Exception occurred in virtual service creation: " + e.getLocalizedMessage());
            return false;
        }
    }

    public boolean createVirtualService(URL filepath, String namespace, List<IstioVirtualService.Route> routes, IstioVirtualService.Protocols protocol) {
        VirtualService virtualService = istioClient.v1alpha3().virtualServices().load(filepath).get();

        try {
            updateVirtualService(routes, protocol, virtualService);
            virtualService = istioClient.v1alpha3().virtualServices().inNamespace(namespace).resource(virtualService).create();
            LOG.info("Created virtual service with name " + virtualService.getMetadata().getName() + "with routes : " +
                    virtualService.getSpec().getHttp().get(0).getRoute().toString()+ " - " + System.currentTimeMillis());
            return true;
        } catch (Exception e) {
            LOG.info("Exception occurred in virtual service creation: " + e.getLocalizedMessage());
            return false;
        }
    }

    private void updateVirtualService(List<IstioVirtualService.Route> routes, IstioVirtualService.Protocols protocol, VirtualService virtualService) {
        if (protocol.equals(IstioVirtualService.Protocols.HTTP)) {
            // match ustl level
            List<HTTPRoute> httpRoutes = virtualService.getSpec().getHttp();

            for (HTTPRoute httpRoute : httpRoutes) {
                List<HTTPRouteDestination> destinationRouteList = httpRoute.getRoute();
                // a destination without subsets is available in the yaml
                String host;
                PortSelector portSelector;
                HTTPRouteDestination httpRouteDestination = destinationRouteList.get(0);
                Destination destination = httpRouteDestination.getDestination();
                host = destination.getHost();
                portSelector = destination.getPort();

                for (int i = 0; i < routes.size(); i++) {
                    if (i == 0) {
                        destination.setSubset(routes.get(i).getDestinationSubset());
                        httpRouteDestination.setWeight(routes.get(i).getWeight());
                    } else {
                        HTTPRouteDestination destinationRoute = new HTTPRouteDestination();

                        Destination destinationi = new Destination(host, portSelector, routes.get(i).getDestinationSubset());

                        destinationRoute.setDestination(destinationi);

                        destinationRoute.setWeight(routes.get(i).getWeight());

                        destinationRouteList.add(destinationRoute);

                    }

                }


            }


//        List<HTTPRoute> httpRoutes = new ArrayList<>();
//        List<HTTPRouteDestination> destinationRouteList = new ArrayList<>();
//        //todo check why this is a list
//        HTTPRoute httpRoute = new HTTPRoute();
//        for (IstioVirtualService.Route r : routes) {
//            HTTPRouteDestination destinationRoute = new HTTPRouteDestination();
//
//            Destination destination = new Destination();
//            destination.setHost(r.getDestinationHost());
//            destination.setSubset(r.getDestinationSubset());
//
//            destinationRoute.setDestination(destination);
//
//            destinationRoute.setWeight(r.getWeight());
//
//            destinationRouteList.add(destinationRoute);
//
//        }
//        httpRoute.setRoute(destinationRouteList);
//        httpRoutes.add(httpRoute);
//        virtualService.getSpec().setHttp(httpRoutes);
        }

    }

    private void updateDesinationRule(DestinationRule destinationRule, List<IstioDestinationRule.IstioSubset> subsets) {
        List<Subset> subsetList = new ArrayList<>();
        for (IstioDestinationRule.IstioSubset subset : subsets) {
            Subset s = new Subset();
            s.setLabels(subset.getLabels());
            s.setName(subset.getSubsetName());

            subsetList.add(s);
        }
        destinationRule.getSpec().setSubsets(subsetList);
    }


}
