package com.microservices.api.config;

import org.aeonbits.owner.ConfigFactory;
public class ConfigTest {

    public static EnvironmentConfig config;

    public static void main(String[] args) {
        // Set the environment before creating the config
        System.setProperty("env", "prod");

        // Create Owner config instance
        config = ConfigFactory.create(EnvironmentConfig.class,System.getProperties());

        System.out.println("Base URL: " + config.baseUrl());
        System.out.println("API Key: " + config.apiKey());
        System.out.println("Timeout: " + config.timeoutSeconds());
    }
}
