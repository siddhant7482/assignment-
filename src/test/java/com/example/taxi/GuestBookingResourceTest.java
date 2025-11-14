package com.example.taxi;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class GuestBookingResourceTest {

    private String uniqueEmail(String prefix) {
        String suffix = String.valueOf(System.nanoTime());
        return prefix + suffix + "@example.com";
    }

    private String uniqueReg() {
        String suffix = String.valueOf(System.nanoTime());
        String alnum = suffix.replaceAll("[^A-Za-z0-9]", "");
        if (alnum.length() < 7) alnum = (alnum + "ABCDEFG").substring(0,7);
        return alnum.substring(0,7);
    }

    private Long ensureTaxi() {
        String reg = uniqueReg();
        var body = String.format("{\"registration\":\"%s\",\"seats\":4}", reg);
        return given().contentType(ContentType.JSON).body(body)
                .when().post("/api/taxis")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");
    }

    @Test
    void createGuestBooking_success() {
        Long taxiId = ensureTaxi();
        var req = new GuestBookingCreate();
        req.name = "Guest One";
        req.email = uniqueEmail("guest1");
        req.phonenumber = "01234567890";
        req.taxiId = taxiId;
        req.date = LocalDate.now().plusDays(20);

        given().contentType(ContentType.JSON).body(req)
        .when().post("/api/guest-bookings")
        .then().statusCode(201)
                .body("id", notNullValue())
                .body("customer.email", equalTo(req.email))
                .body("taxi.id", equalTo(taxiId.intValue()));
    }

    @Test
    void createGuestBooking_duplicateTaxiDate_rollsBackCustomer() {
        Long taxiId = ensureTaxi();
        String date = LocalDate.now().plusDays(30).toString();

        // First booking succeeds
        var req1 = new GuestBookingCreate();
        req1.name = "Guest Two";
        req1.email = uniqueEmail("guest2");
        req1.phonenumber = "01234567890";
        req1.taxiId = taxiId;
        req1.date = LocalDate.parse(date);

        Long bookingId = given().contentType(ContentType.JSON).body(req1)
                .when().post("/api/guest-bookings")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");

        // Second booking with same taxi/date should 409 and not persist customer
        var req2 = new GuestBookingCreate();
        req2.name = "Guest Three";
        req2.email = uniqueEmail("guest3");
        req2.phonenumber = "01234567890";
        req2.taxiId = taxiId;
        req2.date = LocalDate.parse(date);

        given().contentType(ContentType.JSON).body(req2)
        .when().post("/api/guest-bookings")
        .then().statusCode(409);

        // Verify customer with req2.email does NOT exist
        given()
        .when().get("/api/customers")
        .then().statusCode(200)
                .body("find { it.email == '" + req2.email + "' }", nullValue());

        // Cleanup booking
        given().when().delete("/api/bookings/" + bookingId).then().statusCode(204);
    }

    @Test
    void guestBooking_conflictExistingEmail() {
        // Create a baseline customer
        var custBody = "{" +
                "\"name\":\"Alice Conf\"," +
                "\"email\":\"alice-conf@example.com\"," +
                "\"phonenumber\":\"01234567890\"}";
        given().contentType(ContentType.JSON).body(custBody)
        .when().post("/api/customers")
        .then().statusCode(201);

        Long taxiId = ensureTaxi();

        var req = new GuestBookingCreate();
        req.name = "Guest Four";
        req.email = "alice-conf@example.com"; // duplicate email
        req.phonenumber = "01234567890";
        req.taxiId = taxiId;
        req.date = LocalDate.now().plusDays(10);

        given().contentType(ContentType.JSON).body(req)
        .when().post("/api/guest-bookings")
        .then().statusCode(409);
    }

    @Test
    void deleteCustomer_cascadesBookings() {
        Long taxiId = ensureTaxi();

        // Create customer
        String email = uniqueEmail("cascade");
        var custBody = String.format("{\"name\":\"Bob Cascade\",\"email\":\"%s\",\"phonenumber\":\"01234567890\"}", email);
        Long customerId = given().contentType(ContentType.JSON).body(custBody)
                .when().post("/api/customers")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");

        // Create booking
        String date = LocalDate.now().plusDays(40).toString();
        var bookingBody = String.format("{\"customerId\":%d,\"taxiId\":%d,\"date\":\"%s\"}", customerId, taxiId, date);
        Long bookingId = given().contentType(ContentType.JSON).body(bookingBody)
                .when().post("/api/bookings")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");

        // Delete customer; booking should be removed via cascade
        given().when().delete("/api/customers/" + customerId).then().statusCode(204);

        // Listing bookings for the deleted customer should be empty
        given()
        .when().get("/api/customers/" + customerId + "/bookings")
        .then().statusCode(200)
                .body("size()", equalTo(0));
    }

    @Test
    void deleteTaxi_cascadesBookings() {
        Long taxiId = ensureTaxi();

        // Create customer
        String email = uniqueEmail("cascadeT");
        var custBody = String.format("{\"name\":\"Carl Taxi\",\"email\":\"%s\",\"phonenumber\":\"01234567890\"}", email);
        Long customerId = given().contentType(ContentType.JSON).body(custBody)
                .when().post("/api/customers")
                .then().statusCode(201)
                .extract().jsonPath().getLong("id");

        // Create booking
        String date = LocalDate.now().plusDays(50).toString();
        var bookingBody = String.format("{\"customerId\":%d,\"taxiId\":%d,\"date\":\"%s\"}", customerId, taxiId, date);
        given().contentType(ContentType.JSON).body(bookingBody)
        .when().post("/api/bookings")
        .then().statusCode(201);

        // Delete taxi; booking should be removed via cascade
        given().when().delete("/api/taxis/" + taxiId).then().statusCode(204);

        // Listing bookings for the customer should be empty now
        given()
        .when().get("/api/customers/" + customerId + "/bookings")
        .then().statusCode(200)
                .body("size()", equalTo(0));
    }
}