import io.qameta.allure.junit4.DisplayName;
import io.qameta.allure.Description;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.praktikum.auth.UserRequest;
import org.praktikum.utils.ApiSteps;
import org.praktikum.utils.BASE_URI;

public class AuthRegisterTest {

    private String accessToken;
    private UserRequest userRequest;

    @Before
    public void setUp() {
        RestAssured.baseURI = BASE_URI.getBaseURI();
    }

    @After
    public void tearDown() {
        if (accessToken != null) {
            ApiSteps.deleteUser(accessToken);
        }
    }

    @Test
    @DisplayName("Создание уникального пользователя")
    @Description("Этот тест создает уникального пользователя и проверяет успешность создания")
    public void createUniqueUser() throws InterruptedException {
        userRequest = ApiSteps.createUserData(
                "unique-email_" + System.currentTimeMillis() + "@yandex.ru",
                "password",
                "UniqueUser_" + System.currentTimeMillis()
        );

        Response response = ApiSteps.registerUser(userRequest);

        ApiSteps.checkSuccessfulResponse(response);

        accessToken = ApiSteps.extractAccessToken(response);
    }

    @Test
    @DisplayName("Создание уже зарегистрированного пользователя")
    @Description("Этот тест пытается создать уже зарегистрированного пользователя и ожидает ошибку")
    public void createUserAlreadyRegisteredTest() throws InterruptedException {
        userRequest = ApiSteps.createUserData(
                "already-registered-email_" + System.currentTimeMillis() + "@yandex.ru",
                "password",
                "ExistingUser_" + System.currentTimeMillis()
        );

        Response firstResponse = ApiSteps.registerUser(userRequest);
        ApiSteps.checkSuccessfulResponse(firstResponse);

        accessToken = ApiSteps.extractAccessToken(firstResponse);

        Response secondResponse = ApiSteps.registerUser(userRequest);
        ApiSteps.checkUserAlreadyExistsResponse(secondResponse);
    }
}
