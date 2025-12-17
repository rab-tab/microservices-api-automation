package com.microservices.api.util;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Uploads a single combined WireMock mappings JSON to WireMock
 * running in Kubernetes at WIREMOCK_BASE (default http://wiremock-svc:9095).
 * <p>
 * It attempts the bulk import endpoint first (/__admin/mappings/import).
 * If that fails, it posts the whole file to /__admin/mappings (WireMock accepts single mapping or bulk with "mappings" anyway).
 */
public final class WireMockUploader {

    //private static final String WIREMOCK_BASE =  "http://wiremock-svc:9095";
    private static final String WIREMOCK_BASE =  "http://localhost:9095";

    private WireMockUploader() {
    }

    public static void uploadCombinedMapping(String combinedFilePath) throws Exception {
        Path p = Path.of(combinedFilePath);
        if (!Files.exists(p)) {
            throw new IllegalArgumentException("Combined stub file not found at: " + combinedFilePath);
        }

        String json = Files.readString(p);

        // reset existing mappings first
        RestAssured.given()
                .contentType(ContentType.JSON)
                .post(WIREMOCK_BASE + "/__admin/mappings/reset")
                .then()
                .statusCode(200);

        // Try bulk import (WireMock supports /__admin/mappings/import)
        int status = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(json)
                .post(WIREMOCK_BASE + "/__admin/mappings/import")
                .getStatusCode();

        if (status >= 200 && status < 300) {
            System.out.println("WireMock mappings imported via /__admin/mappings/import");
            return;
        }

        // Fallback: post the whole JSON to /__admin/mappings (WireMock may accept).
        status = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(json)
                .post(WIREMOCK_BASE + "/__admin/mappings")
                .getStatusCode();

        if (status >= 200 && status < 300) {
            System.out.println("WireMock mappings uploaded via /__admin/mappings");
            return;
        }

        throw new IllegalStateException("Failed to upload WireMock mappings. Import status: " + status);
    }
}
