package com.example.agent;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasSize;

@QuarkusTest
public class TravelAgentResourceTest {

    @Test
    public void listEmptyAggregateBookings() {
        given()
                .when().get("/api/agent/bookings")
                .then()
                .statusCode(200)
                .body("$", hasSize(0));
    }
}