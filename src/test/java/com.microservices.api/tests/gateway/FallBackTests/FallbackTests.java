package com.microservices.api.tests.gateway.FallBackTests;


import com.microservices.api.base.Base;
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

public class FallbackTests extends Base {

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
    public void whenDownstreamHealthy_thenNoFallback() {
        long start = System.currentTimeMillis();
        Response response = RestAssured.given(spec)
                .get(WireMockEndPoints.PRODUCT_HEALTHY)
                .then()
                .extract()
                .response();

        long duration = System.currentTimeMillis() - start;
        System.out.println("Duration = " + duration + " ms");
        Assert.assertEquals(response.getStatusCode(), 200);
        Assert.assertEquals(response.jsonPath().getString("productId"), "123");
        //Assert.assertFalse(response.asString().contains("temporarily unavailable"));
    }

    @Test(priority = 2)
    public void whenDownstreamHealthy_thenGatewayReturnsRealResponse() {
        Response response = RestAssured
                .given(spec)
                .get(WireMockEndPoints.PRODUCT_HEALTHY);

        Assert.assertEquals(response.getStatusCode(), 200);
        String productName = response.jsonPath().getString("name");
        Assert.assertNotNull(productName, "Expected 'name' in response");
        Assert.assertEquals(productName, "Healthy Product");

    }

    @Test(priority = 3)
    public void shouldOpenThenRecoverCircuitBreakerAndServeHealthyTraffic()
            throws InterruptedException {

        // 1️⃣ Initial healthy call → CB CLOSED
        Response healthyInitial = RestAssured.given(spec)
                .get(WireMockEndPoints.PRODUCT_HEALTHY)
                .then()
                .extract()
                .response();

        Assert.assertEquals(healthyInitial.getStatusCode(), 200);

        // 2️⃣ Cause failures / slowness → CB OPENS
        for (int i = 0; i < 6; i++) {
            RestAssured.given(spec)
                    .get(WireMockEndPoints.PRODUCT_SLOW)
                    .then();
        }

        // 3️⃣ Verify CB is OPEN (immediate fallback)
        long start = System.currentTimeMillis();
        Response openResponse = RestAssured.given(spec)
                .get(WireMockEndPoints.PRODUCT_HEALTHY)
                .then()
                .extract()
                .response();
        long duration = System.currentTimeMillis() - start;

        Assert.assertEquals(openResponse.getStatusCode(), 503);
        Assert.assertTrue(duration < 1000,
                "CB should short-circuit when OPEN");

        // 4️⃣ Wait for OPEN → HALF_OPEN
        Thread.sleep(6000); // must be > waitDurationInOpenState

        // 5️⃣ First HALF_OPEN trial call succeeds
        Response halfOpenTrial = RestAssured.given(spec)
                .get(WireMockEndPoints.PRODUCT_HEALTHY)
                .then()
                .extract()
                .response();

        Assert.assertEquals(halfOpenTrial.getStatusCode(), 200);

        // 6️⃣ CB should now be CLOSED
        Response healthyAfterRecovery = RestAssured.given(spec)
                .get(WireMockEndPoints.PRODUCT_HEALTHY)
                .then()
                .extract()
                .response();

        Assert.assertEquals(healthyAfterRecovery.getStatusCode(), 200);
    }

    @Test(priority = 4)
    public void whenDownstreamIsSlow_thenFallbackTriggered() {
        long start = System.currentTimeMillis();

        Response response = RestAssured.given(spec)
                .get(WireMockEndPoints.PRODUCT_SLOW)
                .then()
                .extract()
                .response();

        long duration = System.currentTimeMillis() - start;

        Assert.assertEquals(response.getStatusCode(), 503);
        Assert.assertTrue(response.asString().contains("Product Service"));

    }

    @Test(priority = 5)
    public void whenDownstreamReturns500_thenFallbackTriggered() {
        Response response = RestAssured.given(spec)
                .get(WireMockEndPoints.PRODUCT_ERROR)
                .then()
                .extract()
                .response();

        Assert.assertEquals(response.getStatusCode(), 503);
        Assert.assertTrue(response.asString().contains("Product Service"));
    }


    @Test(priority = 6)
    public void whenDownstreamIsSlow_thenTimeLimiterTriggersFallback() {
        spec = RequestBuilder.withBearerAuth(AuthTokenProvider.getToken());
        Response response = RestAssured
                .given(spec)
                .get(WireMockEndPoints.PRODUCT_SLOW)
                .then().log().all()
                .extract()
                .response();

        Assert.assertEquals(response.getStatusCode(), 503, "Expected fallback (503) when downstream is slow");
        Assert.assertTrue(response.getBody().asString().contains("Product Service"),
                "Fallback message should contain 'Product Service'");
    }
}



