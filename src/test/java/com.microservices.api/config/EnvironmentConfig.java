package com.microservices.api.config;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.Sources;

@Sources({
        "classpath:config/application-${env}.properties",
        "classpath:config/application-default.properties"
})
public interface EnvironmentConfig extends Config {
    @Key("base.url")
    String baseUrl();

    @Key("api.key")
    String apiKey();

    @Key("timeout.seconds")
    int timeoutSeconds();
}
