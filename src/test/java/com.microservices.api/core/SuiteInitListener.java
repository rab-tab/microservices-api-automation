package com.microservices.api.core;

import com.microservices.api.config.EnvironmentConfig;
import org.aeonbits.owner.ConfigFactory;
import org.testng.ISuite;
import org.testng.ISuiteListener;

public class SuiteInitListener implements ISuiteListener {

    public static EnvironmentConfig environmentConfig;

    @Override
    public void onStart(ISuite suite) {
        ConfigFactory.setProperty("env", "staging");
        environmentConfig = ConfigFactory.create(EnvironmentConfig.class);

        RuntimeConfig.setBaseUri(environmentConfig.baseUrl());
        RuntimeConfig.setApiKey(environmentConfig.apiKey());
        RuntimeConfig.setTimeoutSeconds(environmentConfig.timeoutSeconds());

        System.out.println(">>> Loaded BASE URL: " + environmentConfig.baseUrl());
    }
}
