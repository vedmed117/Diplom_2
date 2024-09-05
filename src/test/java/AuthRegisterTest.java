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
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;

public class AuthRegisterTest {

    private String accessToken;
    private UserRequest userRequest;

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site/api";
    }

    @After
    public void tearDown() {
        if (accessToken != null) {
            deleteUser(accessToken);
        }
    }

    @Test
    @DisplayName("Create Unique User")
    @Description("This test creates a unique user and verifies the creation.")
    public void createUniqueUser() throws InterruptedException {
        userRequest = createUserData(
                "unique-email_" + System.currentTimeMillis() + "@yandex.ru",
                "password",
                "UniqueUser_" + System.currentTimeMillis()
        );

        Response response = registerUser(userRequest);

        checkSuccessfulResponse(response);

        accessToken = extractAccessToken(response);
    }

    @Test
    @DisplayName("Create User Already Registered")
    @Description("This test tries to create a user that is already registered and expects an error.")
    public void createUserAlreadyRegisteredTest() throws InterruptedException {
        // Создание уникальных данных пользователя
        userRequest = createUserData(
                "already-registered-email_" + System.currentTimeMillis() + "@yandex.ru",
                "password",
                "ExistingUser_" + System.currentTimeMillis()
        );

        Response firstResponse = registerUser(userRequest);
        checkSuccessfulResponse(firstResponse);

        accessToken = extractAccessToken(firstResponse);

        Response secondResponse = registerUser(userRequest);
        checkUserAlreadyExistsResponse(secondResponse);
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

   @Step("Check that registration was successful")
    public void checkSuccessfulResponse(Response response) {
        response.then().statusCode(200).body("success", equalTo(true));
    }


    @Step("Check that user already exists")
    public void checkUserAlreadyExistsResponse(Response response) {
        response.then().statusCode(403).body("message", equalTo("User already exists"));
    }

    // Шаг: извлечение accessToken
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
