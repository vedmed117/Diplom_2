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
import org.praktikum.utils.RestAssuredConfig;

public class AuthLoginTest {

    private String accessToken;
    private UserRequest userRequest;

    @Before
    public void setUp() {
        RestAssuredConfig.init();
        RestAssured.baseURI = BASE_URI.getBaseURI();
        userRequest = ApiSteps.createUserData(
                "login-test-email_" + System.currentTimeMillis() + "@yandex.ru",
                "password",
                "LoginTestUser"
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
    @DisplayName("Вход с валидными данными")
    @Description("Этот тест проверяет, что пользователь может войти с валидными данными")
    public void loginWithValidCredentials() {
        Response response = ApiSteps.loginUser(userRequest);
        ApiSteps.checkLoginSuccess(response);
    }

    @Test
    @DisplayName("Вход с  невалидными данными")
    @Description("Этот тест проверяет, что пользователь не может войти с невалидными данными")
    public void loginWithInvalidCredentials() {
        UserRequest invalidUserRequest = ApiSteps.createUserData("invalid-email@yandex.ru", "wrongPassword", "WrongUser");
        Response response = ApiSteps.loginUser(invalidUserRequest);
        ApiSteps.checkLoginFailure(response);
    }
}
