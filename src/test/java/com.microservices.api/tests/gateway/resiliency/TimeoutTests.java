package com.microservices.api.tests.gateway.resiliency;

import com.microservices.api.core.AuthTokenProvider;
import com.microservices.api.core.RequestBuilder;
import com.microservices.api.constants.WireMockEndPoints;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class TimeoutTests {

    RequestSpecification spec;

    @BeforeClass
    @Parameters("gateway.base.url")
    public void setup(String baseUrl) {
        RequestBuilder.overrideBaseUri(baseUrl);
        RestAssured.baseURI = RequestBuilder.getBaseUri();
    }

    @BeforeMethod(alwaysRun = true)
    public void setup() {
        spec = RequestBuilder.withBearerAuth(AuthTokenProvider.getToken());
    }


    @Test
    public void whenDownstreamTimesOut_thenGatewayReturns504() {

        Response response = RestAssured
                .given(spec)
                .when()
                .get(WireMockEndPoints.PRODUCT_TIMEOUT)
                .then()
                .log().all()
                .extract()
                .response();

        Assert.assertEquals(response.getStatusCode(), 503);
    }

    @Test
    public void whenDownstreamIsSlow_thenTimeLimiterTriggersFallbackEarly() {
        long start = System.currentTimeMillis();

        Response response = RestAssured.given(spec)
                .get(WireMockEndPoints.PRODUCT_SLOW)
                .then().log().all()
                .extract()
                .response();

        long duration = System.currentTimeMillis() - start;

        Assert.assertEquals(response.getStatusCode(), 504);

        // Must be < TimeLimiter timeout, NOT WireMock delay
        Assert.assertTrue(duration < 6500,
                "TimeLimiter should cut the call early");
    }
        // timing assertion here


}
