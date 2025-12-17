package com.microservices.api.tests.gateway.resiliency;

import com.microservices.api.core.AuthTokenProvider;
import com.microservices.api.core.RequestBuilder;
import com.microservices.api.constants.WireMockEndPoints;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.Assert;
import org.testng.annotations.Test;

public class RetryTests {
    RequestSpecification spec;
    @Test
    public void whenRetrySucceeds_thenFinalResponseIs200() {

        spec = RequestBuilder.withBearerAuth(AuthTokenProvider.getToken());
        Response retryResponse1 = RestAssured
                .given(spec)
                .get(WireMockEndPoints.PRODUCT_RETRY_STEP1)
                .then().log().all()
                .extract()
                .response();

        Response retryResponse2 = RestAssured
                .given(spec)
                .get(WireMockEndPoints.PRODUCT_RETRY_STEP2)
                .then().log().all()
                .extract()
                .response();


        // final call succeeds
        Response response = RestAssured
                .given(spec).get(WireMockEndPoints.PRODUCT_RETRY_SUCCESS);
        Assert.assertEquals(response.getStatusCode(), 200);
        Assert.assertTrue(response.getBody().asString().contains("Retry succeeded"));
    }
}

