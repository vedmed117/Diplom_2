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

public class AuthUserUpdateTest {

    private String accessToken;
    private UserRequest userRequest;

    @Before
    public void setUp() {
        RestAssuredConfig.init();
        userRequest = ApiSteps.createUserData(
                "update-test-email_" + System.currentTimeMillis() + "@yandex.ru",
                "password",
                "UpdateTestUser"
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
    @DisplayName("Обновление данных авторизованного пользователя")
    @Description("Этот тест проверяет, что авторизованный пользователь может обновить свои данные")
    public void updateUserWithAuthorization() {
        UserRequest updatedRequest = ApiSteps.createUserData(
                "updated-email_" + System.currentTimeMillis() + "@yandex.ru",
                "newpassword",
                "UpdatedUserName"
        );
        Response response = ApiSteps.updateUser(updatedRequest, accessToken);
        ApiSteps.checkSuccessfulUpdate(response, updatedRequest);
    }

    @Test
    @DisplayName("Обновление данных неавторизованного пользователя")
    @Description("Этот тест проверяет, что попытка обновить данные неавторизованного пользователя возвращает ошибку")
    public void updateUserWithoutAuthorization() {
        UserRequest updatedRequest = ApiSteps.createUserData(
                "no-auth-email_" + System.currentTimeMillis() + "@yandex.ru",
                "newpassword",
                "NoAuthUserName"
        );
        Response response = ApiSteps.updateUser(updatedRequest, null);
        ApiSteps.checkUnauthorizedUpdate(response);
    }
}
