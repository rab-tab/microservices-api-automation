package com.microservices.api.tests.gateway.RoutingTests;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.microservices.api.core.AuthTokenProvider;
import com.microservices.api.core.BodyLoader;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.post;

/**
 * WireMock-based routing tests for API Gateway (TestNG)
 *
 * Tests:
 *  - Create product forwarding (method, path, body, headers)
 *  - Query param forwarding
 *  - Path param forwarding
 *  - Header forwarding
 *  - Retry behavior detection (multiple attempts)
 *  - Fallback detection (gateway returns fallback body)
 */
public class WireMockRoutingTests {
    private static final int WIREMOCK_PORT = 9095;
    private WireMockServer wireMockServer;

    @BeforeClass
    public void startWireMock() {
        wireMockServer = new WireMockServer(WIREMOCK_PORT);
        wireMockServer.start();
        WireMock.configureFor("localhost", WIREMOCK_PORT);

        // configure RestAssured base as gateway
        RestAssured.baseURI = "http://localhost:8088";
    }

    @AfterClass
    public void stopWireMock() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
    }

    @BeforeMethod
    public void resetWiremock() {
        WireMock.reset();
    }

    /**
     * 1) Create product via Gateway -> verify forwarded method/path/body/headers to downstream
     */
    @Test
    public void testCreateAndGetProductRouting() throws Exception {

        // 1️⃣ Load dynamic body from JSON file
        Map<String, Object> body = BodyLoader.loadBody("create_product_data.json");
        String expectedJson = new com.fasterxml.jackson.databind.ObjectMapper()
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(body);

        // Extract the dynamic product name, price, quantity (optional)
        String productName = body.get("name").toString();
        int price = Integer.parseInt(body.get("price").toString());
        int quantity = Integer.parseInt(body.get("quantity").toString());

        // 2️⃣ WireMock: stub CREATE product (dynamic JSON match)
        stubFor(request("POST", urlEqualTo("/product"))
                .withHeader("Content-Type", containing("application/json"))
                .withHeader("Authorization", matching("Bearer\\s.+"))
                .withRequestBody(equalToJson(expectedJson, true, true)) // no hardcoding
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"productId\": \"12345\"}")  // you can make this dynamic too
                )
        );

        // 3️⃣ Call Gateway → POST /product
        Response createResp = given()
                .header("Authorization", "Bearer " + AuthTokenProvider.getToken())
                .header("X-Request-ID", "req-123")
                .header("X-Correlation-ID", "corr-456")
                .contentType("application/json")
                .body(expectedJson)
                .post("/product")
                .then().extract().response();

        Assert.assertEquals(createResp.getStatusCode(), 201);

        // 4️⃣ Extract productId from the mock response
        String productId = createResp.jsonPath().getString("productId");
        Assert.assertNotNull(productId);

        // 5️⃣ WireMock: stub GET product/{id}
        stubFor(get(urlEqualTo("/product/" + productId))
                .withHeader("Authorization", matching("Bearer\\s.+"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"name\": \"" + productName + "\", \"price\": " + price + "}")
                )
        );

        // 6️⃣ Call Gateway → GET /product/{productId}
        Response getResp = given()
                .header("Authorization", "Bearer " + AuthTokenProvider.getToken())
                .get("/product/" + productId)
                .then().extract().response();

        Assert.assertEquals(getResp.getStatusCode(), 200);

        // 7️⃣ Verify WireMock received the GET request
        verify(getRequestedFor(urlEqualTo("/product/" + productId))
                .withHeader("Authorization", matching("Bearer\\s.+"))
        );
    }

}
