package com.microservices.api.tests.gateway.resiliency;

import com.microservices.api.base.Base;
import com.microservices.api.constants.WireMockEndPoints;
import com.microservices.api.core.AuthTokenProvider;
import com.microservices.api.core.RequestBuilder;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class CircuitBreakerTests extends Base {
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


    @Test(priority = 1)
    public void whenCircuitBreakerOpen_thenImmediateFallback() {
        // trip circuit breaker
        for (int i = 0; i < 6; i++) {
            RestAssured.given(spec)
                    .get(WireMockEndPoints.PRODUCT_ERROR).then().log().all();

        }

        long start = System.currentTimeMillis();

        Response response = RestAssured.given(spec)
                .get(WireMockEndPoints.PRODUCT_ERROR)
                .then()
                .extract()
                .response();

        long duration = System.currentTimeMillis() - start;

        Assert.assertEquals(response.getStatusCode(), 503);
        Assert.assertTrue(duration < 1000,
                "Circuit breaker should short-circuit immediately");
    }

    @Test(priority = 2)
    public void whenCircuitBreakerOpen_thenImmediateFallbackWithoutCallingDownstream() {
        // this call should be short-circuited by the gateway if CB is open
        long start = System.currentTimeMillis();

        Response response = RestAssured
                .given(spec)
                .get(WireMockEndPoints.PRODUCT_CB_OPEN);

        long duration = System.currentTimeMillis() - start;
        Assert.assertTrue(duration < 2000, "Expected immediate fallback from circuit-breaker short-circuit. Took: " + duration + "ms");

        Assert.assertEquals(response.getStatusCode(), 503);
    }

    @Test(priority = 3)
    public void whenCircuitBreakerHalfOpen_andTrialCallSucceeds_thenCloseCircuit()
            throws InterruptedException {

        // Step 1: Trip the circuit breaker (OPEN)
        for (int i = 0; i < 6; i++) {
            RestAssured.given(spec)
                    .get(WireMockEndPoints.PRODUCT_ERROR)
                    .then();
        }

        // Step 2: Verify immediate fallback (OPEN state)
        long openStart = System.currentTimeMillis();
        Response openResponse = RestAssured.given(spec)
                .get(WireMockEndPoints.PRODUCT_ERROR)
                .then()
                .extract()
                .response();

        long openDuration = System.currentTimeMillis() - openStart;

        Assert.assertEquals(openResponse.getStatusCode(), 503);
        Assert.assertTrue(openDuration < 1000,
                "Circuit breaker should be OPEN and short-circuiting");

        // Step 3: Wait for OPEN → HALF_OPEN transition
        Thread.sleep(6000); // must be > waitDurationInOpenState

        // Step 4: First HALF_OPEN trial call succeeds
        Response halfOpenSuccess = RestAssured.given(spec)
                .get(WireMockEndPoints.PRODUCT_HEALTHY)
                .then()
                .extract()
                .response();

        Assert.assertEquals(halfOpenSuccess.getStatusCode(), 200);

        // Step 5: Circuit should now be CLOSED
        Response subsequentHealthyCall = RestAssured.given(spec)
                .get(WireMockEndPoints.PRODUCT_HEALTHY)
                .then()
                .extract()
                .response();

        Assert.assertEquals(subsequentHealthyCall.getStatusCode(), 200);
        Assert.assertFalse(
                subsequentHealthyCall.asString().contains("temporarily unavailable"),
                "Circuit breaker should be CLOSED and no fallback triggered");
    }


    @Test(priority = 4)
    public void whenDownstreamReturns500_thenFallbackAndCircuitBreakerCountsFailure() {
        // call multiple times to ensure failure threshold is reached
        int callsToTrigger = 6;
        for (int i = 0; i < callsToTrigger; i++) {
            RestAssured
                    .given(spec)
                    .get(WireMockEndPoints.PRODUCT_ERROR)
                    .then().log().all()
                    .extract()
                    .response();
        }

        // final call: expect fallback (CB may open or count failures)
        Response response = RestAssured
                .given(spec)
                .get(WireMockEndPoints.PRODUCT_ERROR);

        Assert.assertEquals(response.getStatusCode(), 503, "Expected fallback (503) after repeated 500s");
    }


}
