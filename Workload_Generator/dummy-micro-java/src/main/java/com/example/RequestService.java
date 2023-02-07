package com.example;

import io.smallrye.mutiny.Uni;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/DummyM")
public interface RequestService {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getResults")
    public Uni<Reply> sendRequest(Request request);

    @POST
    @Path("/addData")
    public String sendPOSTRequest(Request request);
}
