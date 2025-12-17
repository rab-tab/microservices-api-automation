package com.microservices.api.core;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.Assert;

import java.util.List;
import java.util.Map;

public class RouteValidator {
    /**
     * Hit the API Gateway with path, method, headers, query/path params, body
     */
    public static Response hitGateway(String path, String method,
                                      Map<String, String> headers,
                                      Map<String, ?> queryParams,
                                      Map<String, ?> pathParams,
                                      Object body) {

        // Replace path params dynamically
        if (pathParams != null) {
            for (var entry : pathParams.entrySet()) {
                path = path.replace("{" + entry.getKey() + "}", entry.getValue().toString());
            }
        }

       // var spec= RequestBuilder.withBearerAuth(AuthTokenProvider.getToken());
        var spec = RestAssured.given().log().all().contentType(ContentType.JSON).header("Authorization", "Bearer "+AuthTokenProvider.getToken());

        if (headers != null) spec.headers(headers);
        if (queryParams != null) spec.queryParams(queryParams);
        if (body != null) spec.body(body);

        Response resp;
        switch (method.toUpperCase()) {
            case "GET":
                resp = spec.get(path).then().log().all().extract().response();
                break;
            case "POST":
                resp = spec.post(path).then().log().all().extract().response();
                break;
            case "PUT":
                resp = spec.put(path).then().log().all().extract().response();
                break;
            case "DELETE":
                resp = spec.delete(path).then().log().all().extract().response();
                break;
            default:
                throw new IllegalArgumentException("Unsupported method: " + method);
        }
        return resp;

    }

    /**
     * Hit downstream service for parity check
     */
    public static Response hitDownstream(String path, String method,
                                         Map<String, String> headers,
                                         Map<String, ?> queryParams,
                                         Map<String, ?> pathParams,

                                         Object body) {

        // Replace path params dynamically
        if (pathParams != null) {
            for (var entry : pathParams.entrySet()) {
                path = path.replace("{" + entry.getKey() + "}", entry.getValue().toString());
            }
        }

        // var spec= RequestBuilder.withBearerAuth(AuthTokenProvider.getToken());
        var spec = RestAssured.given().log().all().contentType(ContentType.JSON).header("Authorization", "Bearer "+AuthTokenProvider.getToken());

        if (headers != null) spec.headers(headers);
        if (queryParams != null) spec.queryParams(queryParams);
        if (body != null) spec.body(body);

        Response resp;
        switch (method.toUpperCase()) {
            case "GET":
                resp = spec.get(path).then().log().all().extract().response();
                break;
            case "POST":
                resp = spec.post(path).then().log().all().extract().response();
                break;
            case "PUT":
                resp = spec.put(path).then().log().all().extract().response();
                break;
            case "DELETE":
                resp = spec.delete(path).then().log().all().extract().response();
                break;
            default:
                throw new IllegalArgumentException("Unsupported method: " + method);
        }
        return resp;

    }

    /**
     * Validate HTTP status
     */
    public static void validateStatus(Response response, int expectedStatus) {
        Assert.assertEquals(response.statusCode(), expectedStatus,
                "Expected status " + expectedStatus + ", but got " + response.statusCode());
    }

    /**
     * Validate downstream parity
     */
    public static void validateDownstreamParity(Response gatewayResp, Response downstreamResp) {
        // Status
        System.out.println("Validating Downstream response code ");
        Assert.assertEquals(gatewayResp.getStatusCode(), downstreamResp.getStatusCode(),
                "Gateway vs Downstream status mismatch");

        System.out.println("Validating Downstream body  ");
        // Body exact match
        Assert.assertEquals(gatewayResp.getBody().asString(), downstreamResp.getBody().asString(),
                "Gateway vs Downstream body mismatch");
    }

    /**
     * Validate presence of required fields in body
     */
    public static void validateBodyFields(Response response, List<String> expectedFields) {
        if (expectedFields == null) return;
        for (String field : expectedFields) {
            Assert.assertTrue(response.getBody().jsonPath().get(field) != null,
                    "Field missing in response: " + field);
        }
    }

    /**
     * Validate header propagation
     */
    public static void validateHeaderPropagation(Response response, Map<String, String> headersToCheck) {
        System.out.println("Validating header propagation  ");
        if (headersToCheck == null) return;
        headersToCheck.keySet().forEach(header -> {
            Assert.assertTrue(response.getHeaders().hasHeaderWithName(header),
                    "Header not propagated: " + header);
        });
    }

    /**
     * Validate path params forwarded correctly
     */
    public static void validatePathParamsForwarded(Map<String, Object> pathParams, String downstreamPath) {
        System.out.println("Validating path params forwarded correctly ");
        if (pathParams == null) return;
        for (var entry : pathParams.entrySet()) {
            Assert.assertTrue(downstreamPath.contains(entry.getValue().toString()),
                    "Path param not forwarded: " + entry.getKey());
        }
    }

    /**
     * Validate query params forwarded correctly
     */
    public static void validateQueryParamsForwarded(Map<String, Object> queryParams, String downstreamPath) {
        System.out.println("Validating Query params forwarded correctly");
        if (queryParams == null) return;
        for (var entry : queryParams.entrySet()) {
            Assert.assertTrue(downstreamPath.contains(entry.getKey() + "=" + entry.getValue()),
                    "Query param not forwarded: " + entry.getKey());
        }
    }

    /**
     * Validate HTTP method forwarded
     */
    public static void validateMethodForwarded(String expectedMethod, String downstreamMethod) {
        System.out.println("Validating HTTP method forwarded correctly");
        Assert.assertEquals(expectedMethod.toUpperCase(), downstreamMethod.toUpperCase(),
                "HTTP method not forwarded correctly");
    }
}
