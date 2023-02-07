//package com.microfog.scheduler;
//
//
//import io.quarkus.test.junit.QuarkusTest;
//import org.junit.jupiter.api.Test;
//
//import static io.restassured.RestAssured.given;
//import static org.hamcrest.CoreMatchers.is;
//
//@QuarkusTest
//public class PrProcessing {
//
//    @Test
//    public void prQueueUpdateBatchModeTest() {
//
//        given()
//                .when().get("/PR/home")
//                .then()
//                .statusCode(200)
//                .body(is("Hello from MicroFog Scheduler"));
//    }
//
//}