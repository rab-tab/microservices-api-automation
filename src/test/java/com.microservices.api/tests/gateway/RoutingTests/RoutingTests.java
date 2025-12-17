package com.microservices.api.tests.gateway.RoutingTests;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.api.core.BodyLoader;
import com.microservices.api.core.RouteValidator;
import com.microservices.api.data.DataProviderFactory;
import com.microservices.api.model.ProductRequest;
import com.microservices.api.model.RoutingScenario;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class RoutingTests {
    private static final String ROUTING_JSON_PATH =
            System.getProperty("user.dir") + "/src/test/resources/test-data/api_gateway_routing_tests.json";

    @DataProvider(name = "routingScenarios")
    public Object[][] routingScenarios() throws Exception {
       /* ObjectMapper mapper = new ObjectMapper();
        var scenarios = mapper.readValue(
                new File(ROUTING_JSON_PATH),
                new TypeReference<List<RoutingScenario>>() {
                }
        );*/
        return DataProviderFactory.getData(
                ROUTING_JSON_PATH,
                new TypeReference<List<RoutingScenario>>() {
                }
        );

       /* Object[][] result = new Object[scenarios.size()][1];
        for (int i = 0; i < scenarios.size(); i++) result[i][0] = scenarios.get(i);
        return result;*/
    }

    @Test(dataProvider = "routingScenarios")
    public void runRoutingTest(RoutingScenario scenario) throws IOException {

        System.out.println("Running scenario: " + scenario.getId() + " - " + scenario.getDescription());

        // ✅ Load request body (from bodyFile or inline body)
        Map<String, Object> finalBody = BodyLoader.loadBody(scenario.getRequest().getBodyFile());
        System.out.println("Body-- "+finalBody);
        if (finalBody == null) {
            finalBody = scenario.getRequest().getBody(); // fallback if no file
        }

        // Hit API Gateway
        var gatewayResp = RouteValidator.hitGateway(
                scenario.getRequest().getPath(),
                scenario.getRequest().getMethod(),
                scenario.getRequest().getHeaders(),
                scenario.getRequest().getQueryParams(),
                scenario.getRequest().getPathParams(),
                finalBody
        );

        // Validate HTTP status
        RouteValidator.validateStatus(gatewayResp, scenario.getExpected().getExpectedStatus());

        // Validate downstream parity
        if (scenario.getExpected().isValid() && scenario.getExpected().getExpectedDownstream() != null) {
            var ds = scenario.getExpected().getExpectedDownstream().getRequest();
            var downstreamResp = RouteValidator.hitDownstream(
                    ds.getDownstreamUrl(),
                    ds.getDownstreamPath(),
                    ds.getHeaders(),
                    ds.getQueryParams(),
                    ds.getPathParams(),
                    finalBody
            );


            RouteValidator.validateDownstreamParity(gatewayResp, downstreamResp);
            RouteValidator.validatePathParamsForwarded(scenario.getRequest().getPathParams(), ds.getDownstreamPath());
            RouteValidator.validateQueryParamsForwarded(scenario.getRequest().getQueryParams(), ds.getDownstreamPath());
            RouteValidator.validateMethodForwarded(scenario.getRequest().getMethod(), ds.getMethod());
        }

        // Validate response body fields
        RouteValidator.validateBodyFields(gatewayResp, scenario.getExpected().getExpectedBodyFields());

        // Validate header forwarding
        RouteValidator.validateHeaderPropagation(gatewayResp, scenario.getRequest().getHeaders());
    }
}
