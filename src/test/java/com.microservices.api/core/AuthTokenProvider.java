package com.microservices.api.core;

import com.microservices.api.constants.ServiceEndpoints;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

import static com.microservices.api.core.SuiteInitListener.environmentConfig;

public class AuthTokenProvider {
    // static token for all tests (can be read from env or file)
    private static String TOKEN;


    /**
     * Returns a token for the current session. Generates dynamically if not already fetched.
     */
    public static synchronized String getToken() {
        if (TOKEN == null) {
            TOKEN = fetchTokenFromAuthService();
        }
        return TOKEN;
    }

    /**
     * Force refresh token (optional, if token expires mid-session)
     */
    public static synchronized void refreshToken() {
        TOKEN = fetchTokenFromAuthService();
    }

    /**
     * Calls your login/authenticate API to get a token
     */
    private static String fetchTokenFromAuthService() {
        if (environmentConfig == null) {
            throw new IllegalStateException("EnvironmentConfig not initialized");
        }

        String loginEndpoint = environmentConfig.auth0Domain() + ServiceEndpoints.LOGIN; // adjust endpoint
        String clientId = environmentConfig.auth0ClientId();
        String clientSecret = environmentConfig.auth0ClientSecret();
        String audience = environmentConfig.auth0Audience();
        String grantType = environmentConfig.auth0GrantType();
        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put("client_id", clientId);
        bodyMap.put("client_secret", clientSecret);
        bodyMap.put("audience", audience);
        bodyMap.put("grant_type", grantType);

        Response response = RestAssured.given().log().all()
                .contentType("application/json")
                .body(bodyMap)
                .post(loginEndpoint)
                .then().log().all()
                .statusCode(200)
                .extract()
                .response();

        // Extract token from response JSON (adjust JSON path as per your API)
        return response.jsonPath().getString("access_token");
    }

}
