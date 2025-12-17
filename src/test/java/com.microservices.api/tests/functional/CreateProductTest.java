package com.microservices.api.tests.functional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.microservices.api.base.Base;
import com.microservices.api.constants.ServiceEndpoints;
import com.microservices.api.core.AuthTokenProvider;
import com.microservices.api.core.RequestBuilder;
import com.microservices.api.data.DataProviderFactory;
import com.microservices.api.model.ProductRequest;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.List;

import static com.microservices.api.core.ResponseValidator.assertJsonValue;

public class CreateProductTest {

    @BeforeClass
    @Parameters("gateway.base.url")
    public void setup(String baseUrl) {
        RequestBuilder.overrideBaseUri(baseUrl);
        RestAssured.baseURI = RequestBuilder.getBaseUri();
    }
    @DataProvider(name = "createProductData")
    public Object[][] getCreateProductData() {
        return DataProviderFactory.getData(
                System.getProperty("user.dir") + "/src/test/resources/test-data/create_product_data.json",
                new TypeReference<List<ProductRequest>>() {
                }
        );
    }

    @Test(dataProvider = "createProductData", description = "Test create product flow")
    public void testCreateFetchProduct(ProductRequest productData) {

        RequestSpecification spec = RequestBuilder.withBearerAuth(AuthTokenProvider.getToken());
        Response response = RestAssured
                .given(spec)
                .body(productData)
                .post(ServiceEndpoints.CREATE_PRODUCT)
                .then().log().all()
                .statusCode(201)
                .extract()
                .response();
        String productId = response.asString();

        Response resp = RestAssured
                .given(spec)
                .pathParam("productId", Integer.valueOf(productId))
                .get(ServiceEndpoints.GET_PRODUCT)
                .then().log().all()
                .statusCode(200)
                .extract().response();

        assertJsonValue(resp, "productId", Integer.valueOf(productId));
        assertJsonValue(resp, "productName", productData.getName());
        assertJsonValue(resp, "quantity", productData.getQuantity());
        assertJsonValue(resp, "price", productData.getPrice());


    }
}
