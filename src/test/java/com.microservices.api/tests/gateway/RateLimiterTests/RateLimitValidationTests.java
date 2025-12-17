package com.microservices.api.tests.gateway.RateLimiterTests;

import com.microservices.api.base.Base;
import com.microservices.api.core.AuthTokenProvider;
import com.microservices.api.constants.ServiceEndpoints;

import com.microservices.api.core.RequestBuilder;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import com.microservices.api.util.RedisUtil;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class RateLimitValidationTests extends Base {

    @BeforeClass
    public void resetRedisBeforeTests() {
        // Reset Redis counters for predictable rate-limiter tests
        RedisUtil.flushAll();
    }

    @Test(dataProvider = "routeProvider", description = "Test basic rate limiting")
    public void testRateLimitBasic(String route) {
        String bucketKey = "TEST_KEY_" + System.currentTimeMillis();
        // First request should succeed
        RequestSpecification spec = RequestBuilder.withBearerAuth(AuthTokenProvider.getToken())
                .header("X-TEST-RATE-LIMIT", bucketKey);
      /*  RestAssured.given(spec)
                .get(route)
                .then()
                .statusCode(200);
        System.out.println("Redis keys after first request: " + RedisUtil.executeCommand("KEYS", "*"));
        // Second immediate request should fail with 429
        RestAssured.given(spec)
                .get(route)
                .then()
                .statusCode(429);
        Set<String> keys = (Set<String>) RedisUtil.executeCommand("KEYS", "*");
        System.out.println("Redis keys after requests: " + keys);*/

        CompletableFuture<Response> r1 = CompletableFuture.supplyAsync(
                () -> RestAssured.given(spec).get(route)
        );

        CompletableFuture<Response> r2 = CompletableFuture.supplyAsync(
                () -> RestAssured.given(spec).get(route)
        );

        Response first = null;
        try {
            first = r1.get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        Response second = null;
        try {
            second = r2.get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        Assert.assertEquals(first.getStatusCode(), 200);
        Assert.assertEquals(second.getStatusCode(), 429);

    }

   /* @Test(dataProvider = "routeProvider", description = "Test rate limiter replenishment")
    public void testRateLimitReplenish(String route) throws InterruptedException {
        // Wait 1 second for replenish (assuming replenishRate=1 req/sec)
        Thread.sleep(1000);

        RequestBuilder.defaultSpec()
                .get(route)
                .then()
                .statusCode(200);
    }

    @Test(dataProvider = "routeProvider", description = "Test burst behavior with parallel requests")
    public void testRateLimitBurst(String route) {
        // Simulate 5 parallel requests; only first allowed (burstCapacity=1)
        IntStream.range(0, 5).parallel().forEach(i -> {
            int expectedStatus = (i < 1) ? 200 : 429;
            RequestBuilder.defaultSpec()
                    .get(route)
                    .then()
                    .statusCode(expectedStatus);
        });
    }*/

    @DataProvider(name = "routeProvider")
    public Object[][] routeProvider() {
        return new Object[][]{
               // {ServiceEndpoints.ORDER_LIST.replace("{orderId}", "1")},
                //{ServiceEndpoints.ORDER_CREATE},
                //{ServiceEndpoints.GET_PAYMENT.replace("{orderId}", "1")},
                {ServiceEndpoints.GET_PRODUCT.replace("{productId}", "40")},
                //{ServiceEndpoints.REDUCE_PRODUCT_QTY.replace("{productId}", "456").replace("{qty}", "1")}
        };
    }
}
