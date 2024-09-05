import io.qameta.allure.junit4.DisplayName;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
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
    public void setUp() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site/api";
        UserRequest userRequest = createUserData(
                "test-email_" + System.currentTimeMillis() + "@yandex.ru",
                "password",
                "TestUser_" + System.currentTimeMillis()
        );
        Response response = registerUser(userRequest);
        accessToken = extractAccessToken(response);
    }

    @After
    public void tearDown() {
        if (accessToken != null) {
            deleteUser(accessToken);
        }
    }

    @Test
    @DisplayName("Create order with authorization")
    @Description("This test verifies the creation of an order with authorization")
    public void createOrderWithAuthTest() {
        OrderRequest orderRequest = createOrder(Arrays.asList("61c0c5a71d1f82001bdaaa6d"));
        Response response = createOrder(orderRequest, accessToken);
        checkOrderCreation(response);
    }

    @Test
    @DisplayName("Create order without authorization")
    @Description("This test verifies the creation of an order without authorization")
    public void createOrderWithoutAuthTest() {
        OrderRequest orderRequest = createOrder(Arrays.asList("61c0c5a71d1f82001bdaaa6d"));
        Response response = createOrder(orderRequest, null);
        checkOrderCreation(response);
    }

    @Test
    @DisplayName("Create order without ingredients")
    @Description("This test verifies the creation of an order without ingredients")
    public void createOrderWithoutIngredientsTest() {
        OrderRequest orderRequest = createOrder(Arrays.asList());
        Response response = createOrder(orderRequest, accessToken);
        checkOrderCreationWithoutIngredients(response, "Ingredient ids must be provided");
    }

    @Test
    @DisplayName("Create order with invalid ingredient hash")
    @Description("This test verifies the creation of an order with an invalid ingredient hash")
    public void createOrderWithInvalidIngredientHash() {
        OrderRequest orderRequest = createOrder(Arrays.asList("test"));
        Response response = createOrder(orderRequest, accessToken);
        checkOrderCreationWithInvalidHash(response, "Internal Server Error");
    }

    @Step("Create user data with email: {email}, password: {password}, name: {name}")
    public UserRequest createUserData(String email, String password, String name) {
        return new UserRequest(email, password, name);
    }

    @Step("Create order with ingredients: {ingredients}")
    public OrderRequest createOrder(java.util.List<String> ingredients) {
        return new OrderRequest(ingredients);
    }

    @Step("Register a new user")
    public Response registerUser(UserRequest user) {
        return given()
                .header("Content-type", "application/json")
                .body(user)
                .when()
                .post("/auth/register");
    }

    @Step("Create order with access token: {accessToken}")
    public Response createOrder(OrderRequest order, String accessToken) {
        return given()
                .header("Authorization", accessToken != null ? accessToken : "")
                .header("Content-type", "application/json")
                .body(order)
                .when()
                .post("/orders");
    }

    @Step("Check successful order creation")
    public void checkOrderCreation(Response response) {
        response.then().statusCode(200).body("success", equalTo(true));
    }


    @Step("Check invalid order creation")
    public void checkOrderCreationWithInvalidHash(Response response, String message) {
        response.then().statusCode(500);
    }

    @Step("Check invalid order creation")
    public void checkOrderCreationWithoutIngredients(Response response, String message) {
        response.then().statusCode(400);
    }

    @Step("Extract access token from the response")
    public String extractAccessToken(Response response) {
        return response.then().extract().path("accessToken").toString();
    }

    private void deleteUser(String accessToken) {
        given()
                .header("Authorization", accessToken)
                .when()
                .delete("/auth/user")
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(202)));
    }
}
