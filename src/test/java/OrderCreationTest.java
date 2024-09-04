import io.qameta.allure.junit4.DisplayName;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import org.praktikum.orders.OrderRequest;

import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class OrderCreationTest {

    private String accessToken;

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site/api";
    }

    @Test
    @DisplayName("Create Order with Authorization")
    @Description("This test creates an order with authorization and valid ingredients.")
    public void createOrderWithAuthorization() {
        // Получаем токен авторизации
        accessToken = "Bearer " + getAccessToken();

        // Список ингредиентов
        List<String> ingredients = Arrays.asList("61c0c5a71d1f82001bdaaa6d");

        // Отправляем запрос на создание заказа
        Response response = createOrder(ingredients, accessToken);

        // Проверяем успешность создания заказа
        response.then().statusCode(200).body("success", equalTo(true));
        response.then().body("order.number", notNullValue());
    }

    @Test
    @DisplayName("Create Order Without Authorization")
    @Description("This test creates an order without authorization and valid ingredients.")
    public void createOrderWithoutAuthorization() {
        // Список ингредиентов
        List<String> ingredients = Arrays.asList("61c0c5a71d1f82001bdaaa6d");

        // Отправляем запрос на создание заказа без токена
        Response response = createOrder(ingredients, null);

        // Проверяем успешность создания заказа
        response.then().statusCode(200).body("success", equalTo(true));
        response.then().body("order.number", notNullValue());
    }

    @Test
    @DisplayName("Create Order Without Ingredients")
    @Description("This test tries to create an order without ingredients and expects an error.")
    public void createOrderWithoutIngredients() {
        // Пустой список ингредиентов
        List<String> ingredients = Arrays.asList();

        // Отправляем запрос на создание заказа без ингредиентов
        Response response = createOrder(ingredients, null);

        // Проверяем статус и сообщение об ошибке
        response.then().statusCode(400).body("message", equalTo("Ingredient ids must be provided"));
    }

    @Test
    @DisplayName("Create Order with Invalid Ingredient Hash")
    @Description("This test tries to create an order with an invalid ingredient hash and expects an error.")
    public void createOrderWithInvalidIngredientHash() {
        // Неправильный ингредиент
        List<String> ingredients = Arrays.asList("60d3b41abdacab0026a733c6");

        // Отправляем запрос на создание заказа с неправильным ингредиентом
        Response response = createOrder(ingredients, null);

        // Логируем фактический ответ
        System.out.println("Response: " + response.asString());

        // Проверяем, что статус 400, и сообщение об ошибке правильное
        response.then().statusCode(400)
                .body("message", equalTo("One or more ids provided are incorrect"));
    }


    @Step("Creating an order with ingredients: {ingredients} and access token: {accessToken}")
    private Response createOrder(List<String> ingredients, String accessToken) {
        OrderRequest orderRequest = new OrderRequest(ingredients);

        return given()
                .header("Content-type", "application/json")
                .header("Authorization", accessToken != null ? accessToken : "")
                .body(orderRequest)
                .when()
                .post("/orders");
    }

    @Step("Get access token for the user")
    private String getAccessToken() {
        // Реализуйте получение токена авторизации для создания заказа
        return "your_access_token";
    }
}
