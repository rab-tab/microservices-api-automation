package com.microservices.api.tests.gateway.resiliency;

import com.microservices.api.core.AuthTokenProvider;
import com.microservices.api.core.RequestBuilder;
import com.microservices.api.constants.WireMockEndPoints;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.concurrent.*;

public class BulkheadTests {

    RequestSpecification spec;
    @Test
    public void whenBulkheadFull_thenSomeRequestsRejected() throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(5);

        spec = RequestBuilder.withBearerAuth(AuthTokenProvider.getToken());
        Response retryResponse1 = RestAssured
                .given(spec)
                .get(WireMockEndPoints.PRODUCT_BULKHEAD)
                .then().log().all()
                .extract()
                .response();



        Callable<Response> task = () ->  RestAssured
                .given(spec).get(WireMockEndPoints.PRODUCT_BULKHEAD);

        Future<Response>[] futures = new Future[5];
        for (int i = 0; i < 5; i++) futures[i] = executor.submit(task);

        int rejected = 0;
        for (Future<Response> f : futures) {
            Response r = f.get();
            if (r.getStatusCode() == 503) rejected++;
        }

        System.out.println("Bulkhead rejected " + rejected + " requests");
        Assert.assertTrue(rejected > 0);
        executor.shutdown();
    }
}
