import io.qameta.allure.junit4.DisplayName;
import io.qameta.allure.Description;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.praktikum.auth.UserRequest;
import org.praktikum.orders.OrderRequest;
import org.praktikum.utils.ApiSteps;
import org.praktikum.utils.BASE_URI;

public class OrderCreationTest {

    private String accessToken;

    @Before
    public void setUp() {
        RestAssured.baseURI = BASE_URI.getBaseURI();
        UserRequest userRequest = ApiSteps.createUserData(
                "test-email_" + System.currentTimeMillis() + "@yandex.ru",
                "password",
                "TestUser_" + System.currentTimeMillis()
        );
        Response response = ApiSteps.registerUser(userRequest);
        accessToken = ApiSteps.extractAccessToken(response);
    }

    @After
    public void tearDown() {
        if (accessToken != null) {
            ApiSteps.deleteUser(accessToken);
        }
    }

    @Test
    @DisplayName("Создание заказа с авторизацией")
    @Description("Этот тест проверяет создание заказа с авторизацией")
    public void createOrderWithAuthTest() {
        OrderRequest orderRequest = ApiSteps.createOrder(OrderRequest.getValidIngredients());
        Response response = ApiSteps.createOrder(orderRequest, accessToken);
        ApiSteps.checkOrderCreation(response);
    }

    @Test
    @DisplayName("Создание заказа без авторизации")
    @Description("Этот тест проверяет создание заказа без авторизации")
    public void createOrderWithoutAuthTest() {
        OrderRequest orderRequest = ApiSteps.createOrder(OrderRequest.getValidIngredients());
        Response response = ApiSteps.createOrder(orderRequest, null);
        ApiSteps.checkOrderCreation(response);
    }

    @Test
    @DisplayName("Создание заказа без ингредиентов")
    @Description("Этот тест проверяет создание заказа без ингредиентов")
    public void createOrderWithoutIngredientsTest() {
        OrderRequest orderRequest = ApiSteps.createOrder(OrderRequest.getNoIngredients());
        Response response = ApiSteps.createOrder(orderRequest, accessToken);
        ApiSteps.checkOrderCreationWithoutIngredients(response, "Ingredient ids must be provided");
    }

    @Test
    @DisplayName("Создание заказа с недопустимым хэшем ингредиента")
    @Description("Этот тест проверяет создание заказа с недопустимым хэшем ингредиента")
    public void createOrderWithInvalidIngredientHash() {
        OrderRequest orderRequest = ApiSteps.createOrder(OrderRequest.getInvalidIngredientHash());
        Response response = ApiSteps.createOrder(orderRequest, accessToken);
        ApiSteps.checkOrderCreationWithInvalidHash(response, "Internal Server Error");
    }
}
