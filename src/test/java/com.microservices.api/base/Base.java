package com.microservices.api.base;

import com.microservices.api.config.EnvironmentConfig;
import com.microservices.api.util.WireMockUploader;
import org.aeonbits.owner.ConfigFactory;
import org.testng.annotations.BeforeSuite;

public class Base {

    private static final String COMBINED_STUBS = System.getProperty("user.dir")+"/src/test/resources/wiremock/mappings/product-stubs.json";

    public static EnvironmentConfig environmentConfig;

    @BeforeSuite(alwaysRun = true)
    public void setUp() {
        ConfigFactory.setProperty("env", "staging");
        environmentConfig = ConfigFactory.create(EnvironmentConfig.class);
        try {
            WireMockUploader.uploadCombinedMapping(COMBINED_STUBS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }
}
