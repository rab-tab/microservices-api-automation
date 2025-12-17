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

    // Auth0 Client Credentials
    @Key("auth0.domain")
    String auth0Domain();

    @Key("auth0.client_id")
    String auth0ClientId();

    @Key("auth0.client_secret")
    String auth0ClientSecret();

    @Key("auth0.audience")
    String auth0Audience();

    @Key("auth0.grant_type")
    String auth0GrantType();


}
