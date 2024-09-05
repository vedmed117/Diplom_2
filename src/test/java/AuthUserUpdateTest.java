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
    public void setUp() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site/api";
        userRequest = createUserData(
                "update-test-email_" + System.currentTimeMillis() + "@yandex.ru",
                "password",
                "UpdateTestUser"
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
    @DisplayName("Update User Data with Authorization")
    @Description("This test verifies that a user can update their data when authenticated.")
    public void updateUserWithAuthorization() {
        UserRequest updatedRequest = createUserData(
                "updated-email_" + System.currentTimeMillis() + "@yandex.ru",
                "newpassword",
                "UpdatedUserName"
        );
        Response response = updateUser(updatedRequest, accessToken);
        checkSuccessfulUpdate(response, updatedRequest);
    }

    @Test
    @DisplayName("Update User Data without Authorization")
    @Description("This test verifies that attempting to update user data without authorization results in an error.")
    public void updateUserWithoutAuthorization() {
        UserRequest updatedRequest = createUserData(
                "no-auth-email_" + System.currentTimeMillis() + "@yandex.ru",
                "newpassword",
                "NoAuthUserName"
        );
        Response response = updateUser(updatedRequest, null);
        checkUnauthorizedUpdate(response);
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

    @Step("Update user data with access token: {accessToken}")
    public Response updateUser(UserRequest user, String accessToken) {
        return given()
                .header("Content-type", "application/json")
                .header("Authorization", accessToken != null ? accessToken : "")
                .body(user)
                .when()
                .patch("/auth/user");
    }

    @Step("Check successful update of user data")
    public void checkSuccessfulUpdate(Response response, UserRequest updatedRequest) {
        response.then().statusCode(200).body("success", equalTo(true));
        response.then().body("user.email", equalTo(updatedRequest.getEmail()))
                .body("user.name", equalTo(updatedRequest.getName()));
    }

    @Step("Check unauthorized update of user data")
    public void checkUnauthorizedUpdate(Response response) {
        response.then().statusCode(401).body("message", equalTo("You should be authorised"));
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
