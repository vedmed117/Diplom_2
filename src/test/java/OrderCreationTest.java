import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.praktikum.auth.UserRequest;
import org.praktikum.orders.OrderRequest;

import java.util.Arrays;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;

public class OrderCreationTest {

    private String accessToken;

    @Before
    @Step("Установка базового URL и создание тестового пользователя")
    public void setUp() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site/api";

        // Создаем уникальные данные пользователя
        UserRequest userRequest = new UserRequest(
                "test-email_" + System.currentTimeMillis() + "@yandex.ru",
                "password",
                "TestUser_" + System.currentTimeMillis()
        );

        // Отправляем запрос на создание пользователя
        Response response = given()
                .header("Content-type", "application/json")
                .body(userRequest)
                .when()
                .post("/auth/register");

        // Извлекаем accessToken из ответа
        accessToken = response.then().extract().path("accessToken").toString();
    }

    @After
    @Step("Удаление тестового пользователя")
    public void tearDown() {
        // Удаляем пользователя с использованием accessToken
        if (accessToken != null) {
            given()
                    .header("Authorization", accessToken)
                    .when()
                    .delete("/auth/user")
                    .then()
                    .statusCode(anyOf(equalTo(200), equalTo(202)));
        }
    }

    @Test
    @DisplayName("Создание заказа с авторизацией")
    @Description("Тест проверяет создание заказа с авторизацией")
    public void createOrderWithAuthTest() {
        // Создаем заказ с ингредиентами
        OrderRequest orderRequest = new OrderRequest(Arrays.asList("61c0c5a71d1f82001bdaaa6d"));

        // Отправляем запрос на создание заказа
        given()
                .header("Authorization", accessToken)
                .header("Content-type", "application/json")
                .body(orderRequest)
                .when()
                .post("/orders")
                .then()
                .statusCode(200)
                .body("success", equalTo(true));
    }

    @Test
    @DisplayName("Создание заказа без авторизации")
    @Description("Тест проверяет создание заказа без авторизации")
    public void createOrderWithoutAuthTest() {
        // Создаем заказ с ингредиентами
        OrderRequest orderRequest = new OrderRequest(Arrays.asList("61c0c5a71d1f82001bdaaa6d"));

        // Отправляем запрос на создание заказа
        given()
                .header("Content-type", "application/json")
                .body(orderRequest)
                .when()
                .post("/orders")
                .then()
                .statusCode(200)  // Статус 200, так как заказ можно создать и без авторизации
                .body("success", equalTo(true));
    }

    @Test
    @DisplayName("Создание заказа без ингредиентов")
    @Description("Тест проверяет создание заказа без ингредиентов")
    public void createOrderWithoutIngredientsTest() {
        // Создаем заказ без ингредиентов
        OrderRequest orderRequest = new OrderRequest(Arrays.asList());

        // Отправляем запрос на создание заказа
        given()
                .header("Authorization", accessToken)
                .header("Content-type", "application/json")
                .body(orderRequest)
                .when()
                .post("/orders")
                .then()
                .statusCode(400)
                .body("message", equalTo("Ingredient ids must be provided"));
    }

    @Test
    @DisplayName("Создание заказа с неверным хешем ингредиентов")
    @Description("Тест проверяет создание заказа с неверным хешем ингредиентов")
    public void createOrderWithInvalidIngredientHash() {
        // Создаем заказ с неверным хешем ингредиентов
        OrderRequest orderRequest = new OrderRequest(Arrays.asList("test"));

        // Отправляем запрос на создание заказа
        given()
                .header("Authorization", accessToken)
                .header("Content-type", "application/json")
                .body(orderRequest)
                .when()
                .post("/orders")
                .then()
                .statusCode(500);  // Ожидаем 500 - Internal Server Error;
    }


    @Step("Создать тестового пользователя")
    private Response registerUser(UserRequest userRequest) {
        return given()
                .header("Content-type", "application/json")
                .body(userRequest)
                .when()
                .post("/auth/register");
    }

    @Step("Удалить пользователя с токеном: {accessToken}")
    private void deleteUser(String accessToken) {
        given()
                .header("Authorization", accessToken)
                .when()
                .delete("/auth/user")
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(202)));
    }
}
