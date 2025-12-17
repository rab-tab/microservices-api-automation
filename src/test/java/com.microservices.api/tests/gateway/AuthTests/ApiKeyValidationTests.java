package com.microservices.api.tests.gateway.AuthTests;

import com.fasterxml.jackson.core.type.TypeReference;
import com.microservices.api.base.Base;
import com.microservices.api.core.RequestBuilder;
import com.microservices.api.data.DataProviderFactory;
import com.microservices.api.constants.ServiceEndpoints;
import com.microservices.api.model.Endpoint;
import com.microservices.api.model.RouteMappingData;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

public class ApiKeyValidationTests extends Base {
    private List<Endpoint> getAllEndpoints() {
        String orderId = "123";
        String productId = "456";
        int quantity = 5;

        List<Endpoint> endpoints = new ArrayList<>();

        // Order
        endpoints.add(new Endpoint(ServiceEndpoints.ORDER_CREATE, "POST",
                "{ \"orderName\": \"TestOrder\", \"amount\": 100 }"));
        endpoints.add(new Endpoint(ServiceEndpoints.ORDER_LIST.replace("{orderId}", orderId), "GET", null));

        // Payment
        endpoints.add(new Endpoint(ServiceEndpoints.GET_PAYMENT.replace("{orderId}", orderId), "GET"));


        // Product
        endpoints.add(new Endpoint(ServiceEndpoints.GET_PRODUCT, "GET", null));
        endpoints.add(new Endpoint(ServiceEndpoints.REDUCE_PRODUCT_QTY
                .replace("{productId}", productId)
                .replace("{qty}", String.valueOf(quantity)), "GET", null));

        return endpoints;
    }

    private void executeAllEndpoints(String token) {
        List<Endpoint> endpoints = getAllEndpoints();

        for (Endpoint ep : endpoints) {
            if ("GET".equalsIgnoreCase(ep.getMethod())) {
                given()
                        .spec(token == null ? RequestBuilder.defaultSpec() : RequestBuilder.withBearerAuth(token))
                        .when()
                        .get(ep.getUrl())
                        .then()
                        .statusCode(token == null || "INVALID_TOKEN".equals(token) ? 401 : 200)
                        .body(token == null || "INVALID_TOKEN".equals(token) ? null : notNullValue());
            } else { // POST/PUT
                given()
                        .spec(token == null ? RequestBuilder.defaultSpec() : RequestBuilder.withBearerAuth(token))
                        .body(ep.getBody())
                        .when()
                        .post(ep.getUrl())
                        .then()
                        .statusCode(token == null || "INVALID_TOKEN".equals(token) ? 401 : 200)
                        .body(token == null || "INVALID_TOKEN".equals(token) ? null : notNullValue());
            }
        }
    }

   /* @Test
    public void testEndpointsWithValidToken() {
        executeAllEndpoints(TokenHelperUtil.getAccessToken());
    }

    @Test
    public void testEndpointsWithoutToken() {
        executeAllEndpoints(null);
    }

    @Test
    public void testEndpointsWithInvalidToken() {
        executeAllEndpoints("INVALID_TOKEN");
    }*/

    @Test(dataProvider = "routeMappingData")
    public void testRouteMappings(RouteMappingData data) {

        // 1. Resolve final endpoint with pathParams
        String finalEndpoint = environmentConfig.baseUrl() + data.getEndpoint();
        if (data.getPathParams() != null) {
            for (var entry : data.getPathParams().entrySet()) {
                finalEndpoint = finalEndpoint.replace("{" + entry.getKey() + "}", String.valueOf(entry.getValue()));
            }
        }

        // 2. Add query params if present
        io.restassured.specification.RequestSpecification request =
                given().spec(RequestBuilder.defaultSpec());

        if (data.getQueryParams() != null) {
            request.queryParams(data.getQueryParams());
        }

        // 3. Add headers if present
        if (data.getHeaders() != null) {
            request.headers(data.getHeaders());
        }

        // 4. Execute request
        switch (data.getMethod().toUpperCase()) {
            case "GET":
                request.get(finalEndpoint)
                        .then().statusCode(data.getExpectedStatus());

            case "POST":
                request.post(finalEndpoint)
                        .then().statusCode(data.getExpectedStatus());

            case "PUT":
                request.put(finalEndpoint)
                        .then().statusCode(data.getExpectedStatus());

            case "DELETE":
                request.delete(finalEndpoint)
                        .then().statusCode(data.getExpectedStatus());

            default:
                throw new RuntimeException("Unsupported HTTP method");
        }
    }


    @DataProvider(name = "routeMappingData")
    public Object[][] getRouteMappingData() {
        return DataProviderFactory.getData(
                System.getProperty("user.dir")+"/src/test/resources/test-data/route_mapping_metadata.json",
                new TypeReference<List<RouteMappingData>>() {
                }
        );
    }
}
