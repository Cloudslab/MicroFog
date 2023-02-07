//package com.microfog.scheduler;
//
//import com.microfog.scheduler.model.domainObjects.*;
//import io.quarkus.test.junit.QuarkusTest;
//import io.restassured.http.ContentType;
//import org.junit.jupiter.api.Test;
//
//import javax.inject.Inject;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
//import static io.restassured.RestAssured.given;
//import static org.hamcrest.CoreMatchers.is;
//
//@QuarkusTest
//public class TestAddPrAPI {
//    @Inject
//    public DataMapperMetadata dataMapper;
//
//    @Test
//    public void testHomeEndpoint() {
//        given()
//                .when().get("/PR/home")
//                .then()
//                .statusCode(200)
//                .body(is("Hello from MicroFog Scheduler"));
//    }
//
//    @Test
//    public void testPRsubmitComplete(){
//        generateApp("APP1");
//
//        List<String> entryClusters = new ArrayList<>();
//        entryClusters.add("Edge1");
//
//        PlacementRequest placementRequest = new PlacementRequest("APP1", new ArrayList<>(), entryClusters);
//
//        given()
//                .contentType(ContentType.JSON)
//                .body(placementRequest)
//                .when().post("/PR/addPR")
//                .then()
//                .statusCode(200)
//                .body(is("Placement Request Successfully Submitted"));
//
//        removeApp("APP1");
//    }
//
//    @Test
//    public void testPRsubmitAppIdEmpty(){
//        generateApp("APP1");
//
//        List<String> entryClusters = new ArrayList<>();
//        entryClusters.add("Edge1");
//
//        PlacementRequest placementRequest = new PlacementRequest("", new ArrayList<>(), entryClusters);
//
//        given()
//                .contentType(ContentType.JSON)
//                .body(placementRequest)
//                .when().post("/PR/addPR")
//                .then()
//                .statusCode(200)
//                .body(is("One or more required Fields of the placement request missing"));
//
//        removeApp("APP1");
//    }
//
//    @Test
//    public void testPRsubmitClustersEmpty(){
//        generateApp("APP1");
//
//        PlacementRequest placementRequest = new PlacementRequest("", new ArrayList<>(), new ArrayList<>());
//
//        given()
//                .contentType(ContentType.JSON)
//                .body(placementRequest)
//                .when().post("/PR/addPR")
//                .then()
//                .statusCode(200)
//                .body(is("One or more required Fields of the placement request missing"));
//
//        removeApp("APP1");
//    }
//
//    @Test
//    public void testPRsubmitUnavailableAppId(){
//        List<String> entryClusters = new ArrayList<>();
//        entryClusters.add("Edge1");
//
//        PlacementRequest placementRequest = new PlacementRequest("APP2", new ArrayList<>(), entryClusters);
//
//        given()
//                .contentType(ContentType.JSON)
//                .body(placementRequest)
//                .when().post("/PR/addPR")
//                .then()
//                .statusCode(200)
//                .body(is("Error Occurred when adding the Placement Request to the queue"));
//    }
//
//    @Test
//    public void testPRsubmitWrongMicroserviceId(){
//        generateApp("APP1");
//
//        List<String> entryClusters = new ArrayList<>();
//        entryClusters.add("Edge1");
//
//        List<String> placedMicroservices = new ArrayList<>();
//        placedMicroservices.add("WrongM");
//
//        PlacementRequest placementRequest = new PlacementRequest("APP1", placedMicroservices, entryClusters);
//
//        given()
//                .contentType(ContentType.JSON)
//                .body(placementRequest)
//                .when().post("/PR/addPR")
//                .then()
//                .statusCode(200)
//                .body(is("Error Occurred when adding the Placement Request to the queue"));
//
//        removeApp("APP1");
//    }
//
//
//    private void generateApp(String appId) {
//        /** application model
//         *  1. microservices (6)
//         *  2. Dataflows
//         *  3. composite services (4)
//         */
//        Microservice m1 = new Microservice(appId + "_microservice1");
//        Microservice m2 = new Microservice(appId + "_microservice2");
//        List<Microservice> microservices = Arrays.asList(new Microservice[]{m1,m2});
//
//        Dataflow m1_m2 = new Dataflow(m1,m2,true);
//        Dataflow m2_m1 = new Dataflow(m2,m1, false);
//
//        List<Dataflow> dataflows = Arrays.asList(new Dataflow[]{m1_m2, m2_m1});
//
//        CompositeService service1 = new CompositeService(appId + "_COMPS1",Arrays.asList(new Microservice[]{m1,m2}),
//                Arrays.asList(Arrays.asList(new Dataflow[]{m1_m2,m2_m1})));
//
//        List<CompositeService> compositeServices = Arrays.asList(new CompositeService[]{service1});
//
//        Application application = new Application(appId);
//        application.initModel(microservices, compositeServices, dataflows);
//
//        dataMapper.writeObjectAsBucket(appId,application);
//    }
//
//    private void removeApp(String app1) {
//        dataMapper.deleteByKey(app1, 1);
//    }
//
//
//}