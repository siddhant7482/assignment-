package com.example.taxi;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class TaxiBookingResourceTest {

    private String uniqueEmail(String base) {
        return base + System.currentTimeMillis() + "@example.com";
    }

    private String uniqueReg() {
        long x = System.currentTimeMillis() % 1000000L; // 6 digits
        return "T" + String.format("%06d", x); // 7 alphanum
    }

    @Test
    void createTaxi_duplicateRegistrationReturns409() {
        String reg = uniqueReg();
        var body = String.format("{\"registration\":\"%s\",\"seats\":4}", reg);
        given().contentType(ContentType.JSON).body(body)
        .when().post("/api/taxis")
        .then().statusCode(201).body("registration", equalTo(reg));

        given().contentType(ContentType.JSON).body(body)
        .when().post("/api/taxis")
        .then().statusCode(409);
    }

    @Test
    void bookingLifecycle_createListDelete_andDuplicatePrevention() {
        // Create Customer
        String email = uniqueEmail("dave");
        var custBody = String.format("{\"name\":\"Dave Test\",\"email\":\"%s\",\"phonenumber\":\"01234567890\"}", email);
        Long customerId =
                given().contentType(ContentType.JSON).body(custBody)
                .when().post("/api/customers")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");

        // Create Taxi
        String reg = uniqueReg();
        var taxiBody = String.format("{\"registration\":\"%s\",\"seats\":4}", reg);
        Long taxiId =
                given().contentType(ContentType.JSON).body(taxiBody)
                .when().post("/api/taxis")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");

        // Create Booking (future date)
        String date = LocalDate.now().plusDays(1).toString();
        var bookingBody = String.format("{\"customerId\":%d,\"taxiId\":%d,\"date\":\"%s\"}", customerId, taxiId, date);
        Long bookingId =
                given().contentType(ContentType.JSON).body(bookingBody)
                .when().post("/api/bookings")
                .then().statusCode(201)
                .body("customer.id", equalTo(customerId.intValue()))
                .body("taxi.id", equalTo(taxiId.intValue()))
                .body("date", equalTo(date))
                .extract().jsonPath().getLong("id");

        // Attempt duplicate booking for same taxi+date
        given().contentType(ContentType.JSON).body(bookingBody)
        .when().post("/api/bookings")
        .then().statusCode(409);

        // List bookings for the customer
        given()
        .when().get("/api/customers/" + customerId + "/bookings")
        .then().statusCode(200)
        .body("size()", greaterThanOrEqualTo(1))
        .body("find { it.id == " + bookingId + " }.taxi.registration", equalTo(reg))
        .body("find { it.id == " + bookingId + " }.date", equalTo(date));

        // Delete booking
        given().when().delete("/api/bookings/" + bookingId)
        .then().statusCode(204);

        // Deleting again should 404
        given().when().delete("/api/bookings/" + bookingId)
        .then().statusCode(404);
    }

    @Test
    void bookingInvalidDate_returns422() {
        // Create Customer and Taxi
        String email = uniqueEmail("eve");
        var custBody = String.format("{\"name\":\"Eve Test\",\"email\":\"%s\",\"phonenumber\":\"01234567890\"}", email);
        Long customerId = given().contentType(ContentType.JSON).body(custBody).when().post("/api/customers").then().statusCode(201).extract().jsonPath().getLong("id");

        String reg = uniqueReg();
        var taxiBody = String.format("{\"registration\":\"%s\",\"seats\":4}", reg);
        Long taxiId = given().contentType(ContentType.JSON).body(taxiBody).when().post("/api/taxis").then().statusCode(201).extract().jsonPath().getLong("id");

        // Date in the past
        String date = LocalDate.now().minusDays(1).toString();
        var bookingBody = String.format("{\"customerId\":%d,\"taxiId\":%d,\"date\":\"%s\"}", customerId, taxiId, date);

        given().contentType(ContentType.JSON).body(bookingBody)
        .when().post("/api/bookings")
        .then().statusCode(422);
    }
}