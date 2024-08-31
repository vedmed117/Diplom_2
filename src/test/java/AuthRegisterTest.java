import io.qameta.allure.junit4.DisplayName;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.praktikum.auth.UserRequest;

import java.util.Arrays;
import java.util.Collection;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;

@RunWith(Parameterized.class)
public class AuthRegisterTest {

    private String accessToken;
    private UserRequest userRequest;
    private final String email;
    private final String password;
    private final String name;

    public AuthRegisterTest(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> testData() {
        return Arrays.asList(new Object[][]{
                {null, "password", "UserWithoutEmail"},
                {"user@example.com", null, "UserWithoutPassword"},
                {"user@example.com", "password", null}
        });
    }

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
        // Создаем уникальные данные пользователя
        userRequest = new UserRequest(
                "unique-email_" + System.currentTimeMillis() + "@yandex.ru",
                "password",
                "UniqueUser_" + System.currentTimeMillis()
        );

        // Отправляем POST запрос на регистрацию пользователя
        Response response = given()
                .header("Content-type", "application/json")
                .and()
                .body(userRequest)
                .when()
                .post("/auth/register");

        // Устанавливаем задержку в 3 секунды перед проверкой ответа
        Thread.sleep(3000);

        // Извлекаем accessToken из ответа
        accessToken = response.then().extract().path("accessToken").toString();

        // Проверяем успешность получения accessToken
        if (accessToken != null && !accessToken.isEmpty()) {
            System.out.println("Access token получен: " + accessToken);
        } else {
            System.out.println("Не удалось получить access token");
        }

        // Проверка успешного создания пользователя
        response.then().statusCode(200).body("success", equalTo(true));
    }

    @Test
    @DisplayName("Create User Already Registered")
    @Description("This test tries to create a user that is already registered and expects an error.")
    public void createUserAlreadyRegisteredTest() throws InterruptedException {
        // Создаем уникальные данные пользователя
        userRequest = new UserRequest(
                "already-registered-email_" + System.currentTimeMillis() + "@yandex.ru",
                "password",
                "ExistingUser_" + System.currentTimeMillis()
        );

        // Регистрируем пользователя впервые
        Response firstResponse = registerUser(userRequest);
        firstResponse.then().statusCode(200).body("success", equalTo(true));

        // Извлекаем accessToken
        accessToken = firstResponse.then().extract().path("accessToken").toString();

        // Пытаемся зарегистрировать того же пользователя снова
        Response secondResponse = registerUser(userRequest);
        secondResponse.then().statusCode(403).body("message", equalTo("User already exists"));
    }

    @Test
    @DisplayName("Create User with Missing Required Field")
    @Description("This test tries to create a user with a missing required field and expects an error.")
    public void createUserWithMissingFieldTest() {
        userRequest = new UserRequest(email, password, name);

        Response response = registerUser(userRequest);
        response.then().statusCode(403).body("message", equalTo("Email, password and name are required fields"));
    }

    @Step("Register a new user")
    private Response registerUser(UserRequest user) {
        return given()
                .header("Content-type", "application/json")
                .body(user)
                .when()
                .post("/auth/register");
    }

    @Step("Delete user with access token: {accessToken}")
    private void deleteUser(String accessToken) {
        given()
                .header("Authorization", accessToken)
                .when()
                .delete("/auth/user")
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(202)));
    }
}
