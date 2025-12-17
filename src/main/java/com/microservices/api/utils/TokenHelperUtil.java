package com.microservices.api.utils;

import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.Map;

public class TokenHelperUtil {
    private static final String TOKEN_URL = "https://YOUR_AUTH0_DOMAIN/oauth/token";
    private static final String CLIENT_ID = "YOUR_CLIENT_ID";
    private static final String CLIENT_SECRET = "YOUR_CLIENT_SECRET";
    private static final String AUDIENCE = "https://YOUR_AUTH0_AUDIENCE/";
    private static final String GRANT_TYPE = "client_credentials";

    public static String getAccessToken() {
        Response response = RestAssured
                .given()
                .contentType("application/json")
                .body(Map.of(
                        "client_id", CLIENT_ID,
                        "client_secret", CLIENT_SECRET,
                        "audience", AUDIENCE,
                        "grant_type", GRANT_TYPE
                ))
                .when()
                .post(TOKEN_URL)
                .then()
                .statusCode(200)
                .extract()
                .response();

        return response.jsonPath().getString("access_token");
    }
}
