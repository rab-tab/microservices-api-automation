package com.microservices.api.core;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.List;
import java.util.Map;

public class BodyLoader {

    private static final String TEST_DATA_PATH = System.getProperty("user.dir") + "/src/test/resources/test-data/";

    /**
     * Load JSON body from file.
     * If the file contains an array, returns the first element.
     * If the file contains an object, returns it directly.
     *
     * @param fileName JSON file name in test-data folder
     * @return Map representing JSON body
     */
    public static Map<String, Object> loadBody(String fileName) {
        System.out.println("In load body-- file name "+fileName);
        if (fileName == null || fileName.isEmpty()) return null;

        try {
            String bodyPath = TEST_DATA_PATH + fileName;
            System.out.println("in Load body---"+bodyPath);
            ObjectMapper mapper = new ObjectMapper();
            Object parsed = mapper.readValue(new File(bodyPath), Object.class);

            if (parsed instanceof List) {
                List<?> list = (List<?>) parsed;
                if (list.isEmpty()) {
                    throw new RuntimeException("Body file contains empty array: " + bodyPath);
                }

                Object first = list.get(0);
                if (!(first instanceof Map)) {
                    throw new RuntimeException("First element in array is not a JSON object: " + first);
                }
                System.out.println("returning in if block--body map "+first);
                return (Map<String, Object>) first;

            } else if (parsed instanceof Map) {
                System.out.println("returning in if block--body map "+parsed);
                return (Map<String, Object>) parsed;
            } else {
                throw new RuntimeException("Unsupported JSON type in body file: " + parsed.getClass());
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to load body file: " + fileName, e);
        }
    }
}

