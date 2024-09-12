package org.praktikum.utils;

import io.restassured.RestAssured;

public class RestAssuredConfig {

    public static void init() {
        RestAssured.baseURI = BASE_URI.getBaseURI();
    }
}
