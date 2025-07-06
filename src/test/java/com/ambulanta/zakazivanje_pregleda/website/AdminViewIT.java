package com.ambulanta.zakazivanje_pregleda.website;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.*;

import java.util.List;

import static com.ambulanta.zakazivanje_pregleda.website.TestUtils.createMockJwt;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AdminViewIT extends BaseTest {
    @BeforeEach
    void loginAsAdmin() {
        String adminToken = createMockJwt("super_admin", List.of("ROLE_ADMIN"));

        page.route("**/api/auth/login", route -> route.fulfill(new Route.FulfillOptions()
                .setStatus(200)
                .setContentType("application/json")
                .setBody(String.format("{\"token\":\"%s\"}", adminToken))));

        page.navigate(BASE_URL);
        page.getByPlaceholder("Korisničko ime").first().click();
        page.getByPlaceholder("Korisničko ime").first().fill("super_admin");
        page.getByPlaceholder("Lozinka").first().click();
        page.getByPlaceholder("Lozinka").first().fill("password");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Prijavi se")).click();
        assertThat(page.locator("#adminView")).isVisible();
        assertThat(page.locator("#welcomeMessage")).containsText("Prijavljeni ste kao: super_admin");
    }

    @Test
    void shouldAddDoctor() {
        page.route("**/api/doctors", route -> {
            if ("POST".equalsIgnoreCase(route.request().method())) {
                String postData = route.request().postData();
                assertTrue(postData.contains("\"firstName\":\"Novi\""));
                assertTrue(postData.contains("\"specialization\":\"Hirurg\""));

                route.fulfill(new Route.FulfillOptions().setStatus(201));
            } else {
                route.resume();
            }
        });

        page.locator("#doctorFirstName").click();
        page.locator("#doctorFirstName").fill("Novi");
        page.locator("#doctorLastName").click();
        page.locator("#doctorLastName").fill("Doktor");
        page.locator("#doctorUsername").click();
        page.locator("#doctorUsername").fill("novi.doktor");
        page.locator("#doctorPassword").click();
        page.locator("#doctorPassword").fill("doktor123");
        page.locator("#doctorSpecialization").click();
        page.locator("#doctorSpecialization").fill("Hirurg");

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Dodaj lekara")).click();

        assertThat(page.locator("#messageArea")).hasText("Novi doktor je uspešno dodat!");
        assertThat(page.locator("#doctorFirstName")).isEmpty();
    }

    @Test
    void shouldDisplayValidationErrorsWhenAddingDoctorFails() {
        page.route("**/api/doctors", route -> {
            if ("POST".equalsIgnoreCase(route.request().method())) {
                String errorJson = """
                {
                  "message": "Greška pri validaciji.",
                  "errors": {
                    "username": "Korisničko ime već postoji."
                  }
                }
                """;
                route.fulfill(new Route.FulfillOptions()
                        .setStatus(400)
                        .setContentType("application/json")
                        .setBody(errorJson));
            } else {
                route.resume();
            }
        });

        page.locator("#doctorFirstName").click();
        page.locator("#doctorFirstName").fill("Postojeci");
        page.locator("#doctorLastName").click();
        page.locator("#doctorLastName").fill("Korisnik");
        page.locator("#doctorUsername").click();
        page.locator("#doctorUsername").fill("postojeci.korisnik");
        page.locator("#doctorPassword").click();
        page.locator("#doctorPassword").fill("password123");
        page.locator("#doctorSpecialization").click();
        page.locator("#doctorSpecialization").fill("Opšta praksa");

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Dodaj lekara")).click();

        assertThat(page.locator("#messageArea")).hasText("Greška pri validaciji.");
        assertThat(page.locator("input[name=username] + .field-error"))
                .hasText("Korisničko ime već postoji.");
        assertThat(page.locator("#doctorFirstName")).not().isEmpty();
    }
}
