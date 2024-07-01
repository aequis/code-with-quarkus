package org.acme.hibernate.orm.panache;

import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.jboss.resteasy.reactive.RestResponse.StatusCode.NO_CONTENT;

class FruitResourceTest {

    @Test
    public void testListAllFruits() {
        Response response = given()
                .when()
                .get("/fruits")
                .then()
                .statusCode(200)
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
                .statusCode(200)
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
}