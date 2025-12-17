package com.microservices.api.constants;

public class WireMockEndPoints {
    private WireMockEndPoints() {}

    // gateway paths (gateway routes /product/** to downstream)
    public static final String PRODUCT_SLOW = "/product/slow/123";
    public static final String PRODUCT_TIMEOUT = "/product/timeout/123";
    public static final String PRODUCT_ERROR = "/product/error/123";
    public static final String PRODUCT_CB_ERROR1 = "/product/cb/error1";
    public static final String PRODUCT_CB_ERROR2 = "/product/cb/error2";
    public static final String PRODUCT_CB_OPEN = "/product/cb/open";
    public static final String PRODUCT_RETRY_STEP1 = "/product/retry/step1";
    public static final String PRODUCT_RETRY_STEP2 = "/product/retry/step2";
    public static final String PRODUCT_RETRY_SUCCESS = "/product/retry/success";
    public static final String PRODUCT_HEALTHY = "/product/healthy/123";
    public static final String PRODUCT_BULKHEAD = "/product/bulkhead/123";
}
