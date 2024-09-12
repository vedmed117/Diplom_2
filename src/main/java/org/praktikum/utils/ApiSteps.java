package org.praktikum.utils;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.praktikum.auth.UserRequest;
import org.praktikum.orders.OrderRequest;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.equalTo;

public class ApiSteps {

    @Step("Создание данных пользователя с email: {email}, паролем: {password}, именем: {name}")
    public static UserRequest createUserData(String email, String password, String name) {
        return new UserRequest(email, password, name);
    }

    @Step("Регистрация нового пользователя")
    public static Response registerUser(UserRequest user) {
        return given()
                .header("Content-type", "application/json")
                .body(user)
                .when()
                .post(API_URI.getRegisterUri());
    }

    @Step("Проверка, что отсутствующие обязательные поля вызывают ошибку при регистрации")
    public static void checkMissingFieldResponse(Response response) {
        response.then().statusCode(403).body("message", equalTo("Email, password and name are required fields"));
    }

    @Step("Вход пользователя в систему")
    public static Response loginUser(UserRequest user) {
        return given()
                .header("Content-type", "application/json")
                .body(user)
                .when()
                .post(API_URI.getLoginUri());
    }

    @Step("Обновление данных пользователя с access token: {accessToken}")
    public static Response updateUser(UserRequest user, String accessToken) {
        return given()
                .header("Content-type", "application/json")
                .header("Authorization", accessToken != null ? accessToken : "")
                .body(user)
                .when()
                .patch(API_URI.getUserUri());
    }

    @Step("Проверка успешного обновления данных пользователя")
    public static void checkSuccessfulUpdate(Response response, UserRequest updatedRequest) {
        response.then().statusCode(200).body("success", equalTo(true));
        response.then().body("user.email", equalTo(updatedRequest.getEmail()))
                .body("user.name", equalTo(updatedRequest.getName()));
    }

    @Step("Проверка ошибки при обновлении данных без авторизации")
    public static void checkUnauthorizedUpdate(Response response) {
        response.then().statusCode(401).body("message", equalTo("You should be authorised"));
    }

    @Step("Создание заказа с ингредиентами: {ingredients}")
    public static OrderRequest createOrder(List<String> ingredients) {
        return new OrderRequest(ingredients);
    }

    @Step("Создание заказа с access token: {accessToken}")
    public static Response createOrder(OrderRequest order, String accessToken) {
        return given()
                .header("Authorization", accessToken != null ? accessToken : "")
                .header("Content-type", "application/json")
                .body(order)
                .when()
                .post(API_URI.getOrdersUri());
    }

    @Step("Проверка успешного создания заказа")
    public static void checkOrderCreation(Response response) {
        response.then().statusCode(200).body("success", equalTo(true));
    }

    @Step("Проверка неудачного создания заказа с неверным хэшем ингредиента")
    public static void checkOrderCreationWithInvalidHash(Response response, String message) {
        response.then().statusCode(500);
    }

    @Step("Проверка неудачного создания заказа без ингредиентов")
    public static void checkOrderCreationWithoutIngredients(Response response, String message) {
        response.then().statusCode(400);
    }

    @Step("Проверка успешного входа в систему")
    public static void checkLoginSuccess(Response response) {
        response.then().statusCode(200).body("success", equalTo(true));
    }

    @Step("Проверка неудачного входа в систему")
    public static void checkLoginFailure(Response response) {
        response.then().statusCode(401).body("message", equalTo("email or password are incorrect"));
    }

    @Step("Проверка успешной регистрации")
    public static void checkSuccessfulResponse(Response response) {
        response.then().statusCode(200).body("success", equalTo(true));
    }

    @Step("Проверка, что пользователь уже существует")
    public static void checkUserAlreadyExistsResponse(Response response) {
        response.then().statusCode(403).body("message", equalTo("User already exists"));
    }

    @Step("Извлечение access token из ответа")
    public static String extractAccessToken(Response response) {
        return response.then().extract().path("accessToken").toString();
    }

    @Step("Удаление пользователя с access token: {accessToken}")
    public static void deleteUser(String accessToken) {
        given()
                .header("Authorization", accessToken)
                .when()
                .delete(API_URI.getUserUri())
                .then()
                .statusCode(equalTo(202));
    }
    @Step("Получение заказов с access token: {accessToken}")
    public static Response getOrders(String accessToken) {
        return given()
                .header("Authorization", accessToken != null ? accessToken : "")
                .when()
                .get(API_URI.getOrdersUri());
    }

    @Step("Проверка успешного получения заказов")
    public static void checkOrderRetrievalSuccess(Response response) {
        response.then().statusCode(200).body("success", equalTo(true));
    }

    @Step("Проверка ошибки доступа без авторизации")
    public static void checkUnauthorizedAccess(Response response) {
        response.then().statusCode(401).body("message", equalTo("You should be authorised"));
    }

    @Step("Извлечение ID созданного заказа")
    public static String extractOrderId(Response response) {
        return response.then().extract().path("order.number").toString();
    }

    @Step("Проверка успешного получения заказов с созданным заказом")
    public static void checkOrderRetrievalSuccess(Response response, String createdOrderId) {
        response.then().statusCode(200)
                .body("success", equalTo(true))
                .body("orders.number", hasItem(Integer.parseInt(createdOrderId)));
    }
}
