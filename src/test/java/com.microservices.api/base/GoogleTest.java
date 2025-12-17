package com.microservices.api.base;

import com.microservices.api.core.ApiLogger;
import com.microservices.api.core.RequestBuilder;
import com.microservices.api.core.ResponseValidator;
import com.microservices.api.core.RestClient;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

public class GoogleTest extends Base {
    String endpoint;

    @Test
    public void googleHomePageTest() {
        endpoint = environmentConfig.baseUrl();
        ApiLogger.logRequest("GET", endpoint);
        //Build the request spec
        var spec = RequestBuilder.defaultSpec();
        Response response = RestClient.get(endpoint, spec);
        ApiLogger.logResponse(response);
        ResponseValidator.verifyStatusCode(response, 200);
        ResponseValidator.assertBodyContains(response, "Google");
        System.out.println("Response body length: " + response.getBody().asString().length());

    }
    /**
     * Test #2: GET request with query params + headers
     * Example API: https://postman-echo.com/get?foo1=bar1&foo2=bar2
     */
    @Test
    public void getWithQueryParamsTest() {

        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("foo1", "bar1");
        queryParams.put("foo2", "bar2");

        RequestSpecification spec = RequestBuilder.builder()
                .setBaseUri("https://postman-echo.com")
                .addQueryParams(queryParams)
                .build();

        Response response = RestClient.get("/get", spec);

        ResponseValidator.logResponse(response);
        ResponseValidator.verifyStatusCode(response, 200);

        // Verify the response
        ResponseValidator.assertJsonValue(response, "args.foo1", "bar1");
        ResponseValidator.assertJsonValue(response, "args.foo2", "bar2");
    }

}

