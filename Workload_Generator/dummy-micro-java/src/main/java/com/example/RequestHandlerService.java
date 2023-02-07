package com.example;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.eventbus.EventBus;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.sql.Time;

@Path("/DummyM")
public class RequestHandlerService {
    private final static Logger LOGGER = Logger.getLogger(RequestHandlerService.class.getName());

    @Inject
    EventBus bus;

    @Inject
    MicroserviceControlInfoInterface microserviceControlInfo;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/home")
    public String hello() {
        return "Hello from Dummy microservice";
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getResults")
    public Reply getResults(Request request) {
        Long t1 = System.currentTimeMillis();
        LOGGER.info("Request received for processing");
        RequestProcessor requestProcessor = new RequestProcessor(request, microserviceControlInfo);
        requestProcessor.processRequest();
        if(requestProcessor.isLastProcess()) {
            Reply reply = requestProcessor.getReply();
            Long t2 = System.currentTimeMillis();
            LOGGER.info("TOTAL TIME TO REPLY : " + (t2-t1));
            return reply;
        }
        else {
            Reply reply =  requestProcessor.callNextMicroservices(0);
            Long t2 = System.currentTimeMillis();
            LOGGER.info("TOTAL TIME TO REPLY : " + (t2-t1));
            return reply;
        }
    }

    @POST
    @Path("/addData")
    public String addData(Request request){
        LOGGER.info("Data received as POST operation");
        RequestProcessor requestProcessor = new RequestProcessor(request, microserviceControlInfo);
        requestProcessor.processRequest();
        return  "post request received";
    }


}