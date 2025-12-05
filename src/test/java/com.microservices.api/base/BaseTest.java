package com.microservices.api.base;

import com.microservices.api.config.EnvironmentConfig;
import org.aeonbits.owner.ConfigFactory;
import org.testng.annotations.BeforeSuite;

public class BaseTest {

    public static EnvironmentConfig environmentConfig;

    @BeforeSuite(alwaysRun = true)
    public void setUp() {
        ConfigFactory.setProperty("env", "staging");
        environmentConfig = ConfigFactory.create(EnvironmentConfig.class);

    }
}
