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
    public void setUp() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site/api";
        userRequest = createUserData(
                "login-test-email_" + System.currentTimeMillis() + "@yandex.ru",
                "password",
                "LoginTestUser"
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
    @DisplayName("Login with valid credentials")
    @Description("This test checks that a user can log in with valid credentials.")
    public void loginWithValidCredentials() {
        Response response = loginUser(userRequest);
        checkLoginSuccess(response);
    }

    @Test
    @DisplayName("Login with invalid credentials")
    @Description("This test checks that a user cannot log in with invalid credentials.")
    public void loginWithInvalidCredentials() {
        UserRequest invalidUserRequest = createUserData("invalid-email@yandex.ru", "wrongPassword", "WrongUser");
        Response response = loginUser(invalidUserRequest);
        checkLoginFailure(response);
    }

    @Step("Create user data with email: {email}, password: {password}, name: {name}")
    public UserRequest createUserData(String email, String password, String name) {
        return new UserRequest(email, password, name);
    }

    @Step("Register a new user")
    public Response registerUser(UserRequest user) {
        return given()
                .header("Content-type", "application/json")
                .body(user)
                .when()
                .post("/auth/register");
    }

    @Step("Log in user")
    public Response loginUser(UserRequest user) {
        return given()
                .header("Content-type", "application/json")
                .body(user)
                .when()
                .post("/auth/login");
    }

    @Step("Check login success")
    public void checkLoginSuccess(Response response) {
        response.then().statusCode(200).body("success", equalTo(true));
    }

    @Step("Check login failure")
    public void checkLoginFailure(Response response) {
        response.then().statusCode(401).body("message", equalTo("email or password are incorrect"));
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
                .statusCode(equalTo(202));
    }
}
