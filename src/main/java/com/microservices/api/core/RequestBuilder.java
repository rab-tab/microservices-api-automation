package com.microservices.api.core;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

public class RequestBuilder {
    private static final String BASE_URI = "https://api.example.com";

    // ---------------- Base Builder (no duplication) ----------------
    private static RequestSpecBuilder base() {
        return new RequestSpecBuilder()
                .setBaseUri(BASE_URI)
                .setContentType(ContentType.JSON)
                .log(io.restassured.filter.log.LogDetail.ALL);
    }

    // ---------------- Default Spec ----------------
    public static RequestSpecification defaultSpec() {
        return base().build();
    }

    // ---------------- Authentication ----------------
    public static RequestSpecification withBearerAuth(String token) {
        return base()
                .addHeader("Authorization", "Bearer " + token)
                .build();
    }

    public static RequestSpecification withBasicAuth(String username, String password) {
        return base()
                .setAuth(io.restassured.RestAssured.basic(username, password))
                .build();
    }

    // ---------------- Headers ----------------
    public static RequestSpecification withHeaders(Map<String, String> headers) {
        return base()
                .addHeaders(headers)
                .build();
    }

    // ---------------- Query & Path Params ----------------
    public static RequestSpecification withQueryParams(Map<String, ?> queryParams) {
        return base()
                .addQueryParams(queryParams)
                .build();
    }

    public static RequestSpecification withPathParams(Map<String, ?> pathParams) {
        return base()
                .addPathParams(pathParams)
                .build();
    }

    // ---------------- Body ----------------
    public static RequestSpecification withBody(Object body) {
        return base()
                .setBody(body)
                .build();
    }

    // ---------------- Cookies ----------------
    public static RequestSpecification withCookies(Map<String, ?> cookies) {
        return base()
                .addCookies(cookies)
                .build();
    }

    // ---------------- Timeouts ----------------
    public static RequestSpecification withTimeouts(long connectionTimeout, long socketTimeout) {
        return base()
                .setConfig(RestAssuredConfig.config().httpClient(HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", connectionTimeout)
                        .setParam("http.socket.timeout", socketTimeout)))
                .build();
    }

    // ---------------- Custom Builder (full flexibility) ----------------
    public static RequestSpecBuilder builder() {
        return base();
    }

    // ---------------- Fully Custom Spec ----------------
    public static RequestSpecification customSpec(
            String token,
            Map<String, String> headers,
            Map<String, ?> queryParams,
            Map<String, ?> pathParams,
            Object body,
            Map<String, ?> cookies,
            Long connectionTimeout,
            Long socketTimeout
    ) {
        RequestSpecBuilder builder = new RequestSpecBuilder()
                .setBaseUri(BASE_URI)
                .setContentType(ContentType.JSON)
                .log(io.restassured.filter.log.LogDetail.ALL);

        if (token != null) builder.addHeader("Authorization", "Bearer " + token);
        if (headers != null) builder.addHeaders(headers);
        if (queryParams != null) builder.addQueryParams(queryParams);
        if (pathParams != null) builder.addPathParams(pathParams);
        if (body != null) builder.setBody(body);
        if (cookies != null) builder.addCookies(cookies);
        if (connectionTimeout != null && socketTimeout != null) {
            builder.setConfig(RestAssuredConfig.config()
                    .httpClient(HttpClientConfig.httpClientConfig()
                            .setParam("http.connection.timeout", connectionTimeout)
                            .setParam("http.socket.timeout", socketTimeout)));
        }

        return builder.build();
    }
}

