package com.example.taxi;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class CustomerResourceTest {

    @Test
    void createCustomer_andList() {
        var body = "{" +
                "\"name\":\"Alice Smith\"," +
                "\"email\":\"alice@example.com\"," +
                "\"phonenumber\":\"01234567890\"}";

        given()
                .contentType(ContentType.JSON)
                .body(body)
        .when()
                .post("/api/customers")
        .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", equalTo("Alice Smith"))
                .body("email", equalTo("alice@example.com"))
                .body("phonenumber", equalTo("01234567890"));

        given()
        .when()
                .get("/api/customers")
        .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(1))
                .body("find { it.email == 'alice@example.com' }.name", equalTo("Alice Smith"));
    }

    @Test
    void duplicateEmail_returns409() {
        var body = "{" +
                "\"name\":\"Bob\"," +
                "\"email\":\"bob@example.com\"," +
                "\"phonenumber\":\"01234567890\"}";

        given().contentType(ContentType.JSON).body(body)
        .when().post("/api/customers")
        .then().statusCode(201);

        given().contentType(ContentType.JSON).body(body)
        .when().post("/api/customers")
        .then().statusCode(409);
    }

    @Test
    void invalidPhone_returns422() {
        var body = "{" +
                "\"name\":\"Carol\"," +
                "\"email\":\"carol@example.com\"," +
                "\"phonenumber\":\"1234567890\"}"; // does not start with 0 and not 11 digits

        given().contentType(ContentType.JSON).body(body)
        .when().post("/api/customers")
        .then().statusCode(422);
    }
}