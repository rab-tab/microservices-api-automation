package com.microservices.api.model;

public class Endpoint {
    private final String url;
    private final String method; // GET, POST, PUT
    private final String body;   // Optional JSON body, only for POST/PUT

    public Endpoint(String url, String method) {
        this(url, method, null);
    }

    public Endpoint(String url, String method, String body) {
        this.url = url;
        this.method = method;
        this.body = body;
    }

    // Getters
    public String getUrl() {
        return url;
    }

    public String getMethod() {
        return method;
    }

    public String getBody() {
        return body;
    }
}
