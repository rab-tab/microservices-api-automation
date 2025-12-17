package com.microservices.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoutingScenario {

    @JsonProperty("id")
    private String id;

    @JsonProperty("description")
    private String description;

    @JsonProperty("request")
    private Request request;

    @JsonProperty("expected")
    private Expected expected;

    // ================================
    // SHARED REQUEST MODEL
    // ================================
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Request {

        @JsonProperty("method")
        private String method;

        @JsonProperty("path")
        private String path;

        @JsonProperty("headers")
        private Map<String, String> headers;

        @JsonProperty("queryParams")
        private Map<String, Object> queryParams;

        @JsonProperty("pathParams")
        private Map<String, Object> pathParams;

        @JsonProperty("body")
        private Map<String, Object> body;

        @JsonProperty("bodyFile")
        private String bodyFile;

        // Downstream-specific fields
        @JsonProperty("downStreamServiceName")
        private String downStreamServiceName;

        @JsonProperty("downstreamUrl")
        private String downstreamUrl;

        @JsonProperty("downstreamPath")
        private String downstreamPath;
    }

    // ================================
    // EXPECTED MODEL
    // ================================
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Expected {

        @JsonProperty("isValid")
        private boolean isValid;

        @JsonProperty("expectedStatus")
        private int expectedStatus;

        @JsonProperty("expectedBodyFields")
        private List<String> expectedBodyFields;

        @JsonProperty("expectedDownstream")
        private ExpectedDownstream expectedDownstream;
    }

    // ================================
    // EXPECTED DOWNSTREAM
    // ================================
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ExpectedDownstream {

        @JsonProperty("request")
        private Request request;  // <—— Reuse SAME Request object
    }
}
