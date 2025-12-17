package com.microservices.api.model;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteMappingData {
    private String scenario;
    private String endpoint;
    private String method;
    private Map<String, Object> pathParams;
    private Map<String, Object> queryParams;
    private Map<String, String> headers;
    private int expectedStatus;
    private boolean validateBody;
    private String expectedField;
    private String expectedValue;
}
