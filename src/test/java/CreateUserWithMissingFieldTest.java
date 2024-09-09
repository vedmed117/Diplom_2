import io.qameta.allure.junit4.DisplayName;
import io.qameta.allure.Description;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.praktikum.auth.UserRequest;
import org.praktikum.utils.ApiSteps;
import org.praktikum.utils.BASE_URI;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class CreateUserWithMissingFieldTest {

    private final String email;
    private final String password;
    private final String name;

    public CreateUserWithMissingFieldTest(String email, String password, String name) {
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
        RestAssured.baseURI = BASE_URI.getBaseURI();
    }

    @Test
    @DisplayName("Создание пользователя с отсутствующим обязательным полем")
    @Description("Этот тест пытается создать пользователя с отсутствующим обязательным полем и ожидает ошибку")
    public void createUserWithMissingFieldTest() {
        UserRequest userRequest = ApiSteps.createUserData(email, password, name);
        Response response = ApiSteps.registerUser(userRequest);
        ApiSteps.checkMissingFieldResponse(response);
    }
}
