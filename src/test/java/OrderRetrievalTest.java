import io.qameta.allure.junit4.DisplayName;
import io.qameta.allure.Description;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.praktikum.auth.UserRequest;
import org.praktikum.orders.OrderRequest;
import org.praktikum.utils.ApiSteps;
import org.praktikum.utils.RestAssuredConfig;

import static org.praktikum.utils.ApiSteps.getOrders;

public class OrderRetrievalTest {

    private String accessToken;
    private String createdOrderId;

    @Before
    public void setUp() {
        RestAssuredConfig.init();

        UserRequest userRequest = ApiSteps.createUserData(
                "retrieve-order-test-email_" + System.currentTimeMillis() + "@yandex.ru",
                "password",
                "RetrieveOrderTestUser_" + System.currentTimeMillis()
        );
        Response response = ApiSteps.registerUser(userRequest);
        accessToken = ApiSteps.extractAccessToken(response);

        OrderRequest orderRequest = ApiSteps.createOrder(OrderRequest.getValidIngredients());
        Response createOrderResponse = ApiSteps.createOrder(orderRequest, accessToken);
        createdOrderId = ApiSteps.extractOrderId(createOrderResponse);
    }

    @After
    public void tearDown() {
        if (accessToken != null) {
            ApiSteps.deleteUser(accessToken);
        }
    }

    @Test
    @DisplayName("Получение заказов авторизованным пользователем")
    @Description("Этот тест проверяет получение заказов авторизованным пользователем и возвращение созданного заказа")
    public void getOrdersWithAuthTest() {
        Response response = getOrders(accessToken);
        ApiSteps.checkOrderRetrievalSuccess(response, createdOrderId);
    }

    @Test
    @DisplayName("Получение заказов неавторизованным пользователем")
    @Description("Этот тест проверяет получение заказов неавторизованным пользователем и ожидает ошибку авторизации")
    public void getOrdersWithoutAuthTest() {
        Response response = getOrders(null);
        ApiSteps.checkUnauthorizedAccess(response);
    }
}
