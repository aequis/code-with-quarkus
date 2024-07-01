package org.acme.hibernate.orm.panache;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.preemptive;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.emptyString;
import static org.jboss.resteasy.reactive.RestResponse.StatusCode.*;

class FruitResourceTest {

    @Test
    public void testListAllFruits() {
        Response response = given()
                .when()
                .get("/fruits")
                .then()
                .statusCode(OK)
                .contentType("application/json")
                .extract().response();
        assertThat(response.jsonPath().getList("name")).containsExactlyInAnyOrder("Cherry", "Apple", "Banana");
    }

    @Test
    public void testPutFruit() {
        given()
                .when()
                .body("{\"name\": \"Pineapple\"}")
                .contentType("application/json")
                .put("/fruits/1")
                .then()
                .statusCode(OK)
                .body(
                    containsString("\"id\":"),
                    containsString("\"name\":\"Pineapple\""));
    }

    @Test
    public void testDeleteFruit() {
        given()
                .when()
                .delete("/fruits/1")
                .then()
                .statusCode(NO_CONTENT);
    }

    @Test
    public void testCreatePear() {
        given()
                .when()
                .body("{\"name\":\"Pear\"}")
                .contentType("application/json")
                .post("/fruits")
                .then()
                .statusCode(CREATED)
                .body(
                        containsString("\"id\":"),
                        containsString("\"name\":\"Pear\"")
                );
    }

    @Test
    public void testEntityNotFoundForDelete() {
        given()
                .when()
                .delete("/fruits/31535")
                .then()
                .statusCode(NOT_FOUND)
                .body(emptyString());
    }

    @Test
    public void testEntityNotFoundForUpdate() {
        given()
                .when()
                .body("{\"name\": \"Pineapple\"}")
                .put("/fruits/20359")
                .then()
                .statusCode(NOT_FOUND)
                .body(emptyString());
    }
}