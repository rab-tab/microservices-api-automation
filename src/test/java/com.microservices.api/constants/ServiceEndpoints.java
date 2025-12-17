package com.microservices.api.constants;

public class ServiceEndpoints {
    // Login
    public static final String LOGIN="/oauth/token";
    // Order Service
    public static final String ORDER_CREATE = "/order/placeOrder";
    public static final String ORDER_LIST = "/order/{orderId}";

    // Payment Service
    public static final String GET_PAYMENT = "/payment/order/{orderId}";


    // Product Service
    public static final String GET_PRODUCT = "/product/{productId}";
    public static final String CREATE_PRODUCT = "/product";
    public static final String REDUCE_PRODUCT_QTY = "/product/reduceQuantity/{productId}?quantity={qty}";
}
