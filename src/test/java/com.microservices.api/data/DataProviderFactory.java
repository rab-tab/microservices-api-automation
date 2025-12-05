package com.microservices.api.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.DataProvider;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.List;

import static com.microservices.api.base.BaseTest.environmentConfig;

public class DataProviderFactory {

    @DataProvider(name = "jsonDataProvider")
    public static <T> Object[][] getData(String filePath, TypeReference<List<T>> typeRef) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            List<T> dataList = mapper.readValue(new File(filePath), typeRef);
            Object[][] result = new Object[dataList.size()][1];
            for (int i = 0; i < dataList.size(); i++) {
                result[i][0] = dataList.get(i);
            }
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return new Object[0][0];
        }
    }
}
