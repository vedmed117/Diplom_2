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

public class AuthLoginTest {

    private String accessToken;
    private UserRequest userRequest;

    @Before
    @Step("Setup: Register a new user for testing login")
    public void setUp() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site/api";
        // Создаем тестового пользователя
        userRequest = new UserRequest(
                "login-test-email_" + System.currentTimeMillis() + "@yandex.ru",
                "password",
                "LoginTestUser"
        );

        // Регистрируем пользователя для тестов логина
        Response response = registerUser(userRequest);
        accessToken = response.then().extract().path("accessToken").toString();
    }

    @After
    @Step("Tear down: Delete the test user")
    public void tearDown() {
        if (accessToken != null) {
            deleteUser(accessToken);
        }
    }

    @Test
    @DisplayName("Login with Existing User")
    @Description("This test verifies that a user can log in with correct credentials.")
    @Step("Test: Login with existing user credentials")
    public void loginWithExistingUser() {
        Response response = loginUser(userRequest.getEmail(), userRequest.getPassword());
        response.then().statusCode(200).body("success", equalTo(true));
    }

    @Test
    @DisplayName("Login with Invalid Credentials")
    @Description("This test verifies that login fails with incorrect credentials.")
    @Step("Test: Attempt to login with invalid credentials")
    public void loginWithInvalidCredentials() {
        Response response = loginUser("wrong-email@yandex.ru", "wrongpassword");
        response.then().statusCode(401).body("message", equalTo("email or password are incorrect"));
    }

    @Step("Register a new user")
    private Response registerUser(UserRequest user) {
        return given()
                .header("Content-type", "application/json")
                .body(user)
                .when()
                .post("/auth/register");
    }

    @Step("Login user with email: {email} and password: {password}")
    private Response loginUser(String email, String password) {
        return given()
                .header("Content-type", "application/json")
                .body("{\"email\":\"" + email + "\", \"password\":\"" + password + "\"}")
                .when()
                .post("/auth/login");
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
