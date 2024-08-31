import io.qameta.allure.junit4.DisplayName;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.praktikum.auth.UserRequest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class AuthUserUpdateTest {

    private String accessToken;
    private UserRequest userRequest;

    @Before
    @Step("Setup: Register a new user for testing update operations")
    public void setUp() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site/api";
        // Создаем тестового пользователя
        userRequest = new UserRequest(
                "update-test-email_" + System.currentTimeMillis() + "@yandex.ru",
                "password",
                "UpdateTestUser"
        );

        // Регистрируем пользователя для тестов обновления данных
        Response response = registerUser(userRequest);
        accessToken = response.then().extract().path("accessToken").toString();
    }

    @After
    @Step("Teardown: Delete the test user")
    public void tearDown() {
        if (accessToken != null) {
            deleteUser(accessToken);
        }
    }

    @Test
    @DisplayName("Update User Data with Authorization")
    @Description("This test verifies that a user can update their data when authenticated.")
    @Step("Test: Update user data with valid authorization")
    public void updateUserWithAuthorization() {
        // Создаем новый запрос с измененными данными
        UserRequest updatedRequest = new UserRequest(
                "updated-email_" + System.currentTimeMillis() + "@yandex.ru",
                "newpassword",
                "UpdatedUserName"
        );

        // Отправляем PATCH запрос на обновление данных пользователя
        Response response = updateUser(updatedRequest, accessToken);
        response.then().statusCode(200).body("success", equalTo(true));

        // Проверяем, что данные пользователя обновлены
        response.then().body("user.email", equalTo(updatedRequest.getEmail()))
                .body("user.name", equalTo(updatedRequest.getName()));
    }

    @Test
    @DisplayName("Update User Data without Authorization")
    @Description("This test verifies that attempting to update user data without authorization results in an error.")
    @Step("Test: Update user data without authorization")
    public void updateUserWithoutAuthorization() {
        // Создаем новый запрос с измененными данными
        UserRequest updatedRequest = new UserRequest(
                "no-auth-email_" + System.currentTimeMillis() + "@yandex.ru",
                "newpassword",
                "NoAuthUserName"
        );

        // Пытаемся обновить данные пользователя без авторизации
        Response response = updateUser(updatedRequest, null);
        response.then().statusCode(401).body("message", equalTo("You should be authorised"));
    }

    @Step("Register a new user")
    private Response registerUser(UserRequest user) {
        return given()
                .header("Content-type", "application/json")
                .body(user)
                .when()
                .post("/auth/register");
    }

    @Step("Update user data with access token: {accessToken}")
    private Response updateUser(UserRequest user, String accessToken) {
        return given()
                .header("Content-type", "application/json")
                .header("Authorization", accessToken != null ? accessToken : "")
                .body(user)
                .when()
                .patch("/auth/user");
    }

    @Step("Delete user with access token: {accessToken}")
    private void deleteUser(String accessToken) {
        given()
                .header("Authorization", accessToken)
                .when()
                .delete("/auth/user")
                .then()
                .statusCode(equalTo(202));
    }
}
