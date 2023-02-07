package com.example;

import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.jboss.logging.Logger;

import java.net.URI;

public class RequestResource {
    private final static Logger LOGGER = Logger.getLogger(RequestResource.class.getName());
    private final  RequestService requestService;
    private boolean partOfAggregate;
    private String url;

    public RequestResource(String url){
        this.url = url;
        requestService = RestClientBuilder.newBuilder().baseUri(URI.create(url)).build(RequestService.class);
    }

    public Uni<Reply> sendRequest(Request request){
        LOGGER.info("Request sent to next microservice" + System.currentTimeMillis());
        return requestService.sendRequest(request);
    }

    public void setPartOfAggregate(boolean partOfAggregate) {
        this.partOfAggregate = partOfAggregate;
    }

    public boolean isPartOfAggregate() {
        return partOfAggregate;
    }

    public String sendPOSTRequest(Request request) {
        LOGGER.info("POST Request sent to next microservice" + System.currentTimeMillis() + "to URL" + url);
        return requestService.sendPOSTRequest(request);
    }
}
