package com.microservices.api.base;

import org.testng.annotations.Test;

public class SanityTest extends Base {
    @Test
    public void verifySetup() {
        System.out.println("Sanity Test Executed!");
        System.out.println(environmentConfig.baseUrl());
    }
}
