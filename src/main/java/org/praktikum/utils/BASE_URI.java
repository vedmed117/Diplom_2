package org.praktikum.utils;

import io.restassured.RestAssured;

public class BASE_URI {

    private static final String BASE_URI = "https://stellarburgers.nomoreparties.site";

    static {
        RestAssured.baseURI = BASE_URI;
    }

    public static String getBaseURI() {
        return BASE_URI;
    }
}