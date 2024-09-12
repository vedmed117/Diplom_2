package org.praktikum.utils;

public class API_URI {

    private static final String REGISTER = "/api/auth/register";
    private static final String LOGIN = "/api/auth/login";
    private static final String USER = "/api/auth/user";
    private static final String ORDERS = "/api/orders";
    private static final String ALL_ORDERS = "/api/orders/all";

    public static String getRegisterUri() {
        return REGISTER;
    }

    public static String getLoginUri() {
        return LOGIN;
    }

    public static String getUserUri() {
        return USER;
    }

    public static String getOrdersUri() {
        return ORDERS;
    }

    public static String getAllOrdersUri() {
        return ALL_ORDERS;
    }
}
