package com.microservices.api.tests.gateway.RoutingTests;

import com.fasterxml.jackson.core.type.TypeReference;
import com.microservices.api.core.RequestBuilder;
import com.microservices.api.data.DataProviderFactory;
import io.restassured.response.Response;
import com.microservices.api.model.RouteMappingData;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

public class RouteMappingTests {
    private static final String routeMappingPayloadPath =
            System.getProperty("user.dir") + "/src/test/resources/test-date/route_mapping_metadata.json";

    @Test(dataProvider = "routeMappingData")
    public void routeMappingTest(RouteMappingData data) {

        // getTest().info("Scenario: " + data.getScenario());
        // getTest().info("Endpoint: " + data.getEndpoint());

        var specBuilder = RequestBuilder.builder();

        if (data.getPathParams() != null)
            specBuilder.addPathParams(data.getPathParams());

        if (data.getQueryParams() != null)
            specBuilder.addQueryParams(data.getQueryParams());

        if (data.getHeaders() != null)
            data.getHeaders().forEach(specBuilder::addHeader);

        Response response;

        switch (data.getMethod().toUpperCase()) {
            case "GET":
                response = specBuilder.build().get(data.getEndpoint());
                break;
            case "POST":
                response = specBuilder.build().post(data.getEndpoint());
                break;
            case "PUT":
                response = specBuilder.build().put(data.getEndpoint());
                break;
            case "DELETE":
                response = specBuilder.build().delete(data.getEndpoint());
                break;
            default:
                throw new IllegalArgumentException("Unsupported HTTP method: " + data.getMethod());
        }

        // getTest().info("Response: " + response.asString());
        Assert.assertEquals(response.statusCode(), data.getExpectedStatus());
    }

    @DataProvider(name = "routeMappingData")
    public Object[][] routeMappingData() {
        return DataProviderFactory.getData(
                routeMappingPayloadPath,
                new TypeReference<List<RouteMappingData>>() {
                }
        );
    }
}
