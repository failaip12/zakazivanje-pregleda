package com.ambulanta.zakazivanje_pregleda.website;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.*;

import java.util.List;

import static com.ambulanta.zakazivanje_pregleda.website.TestUtils.createMockJwt;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class AuthIT extends BaseTest {
    @Test
    void shouldRegisterPatient() {
        page.route("**/api/auth/register", route -> {
            route.fulfill(new Route.FulfillOptions().setStatus(201));
        });

        page.navigate(BASE_URL);

        page.locator("#registerFirstName").click();
        page.locator("#registerFirstName").fill("Pera");
        page.locator("#registerLastName").click();
        page.locator("#registerLastName").fill("Peric");
        page.locator("#registerUsername").click();
        page.locator("#registerUsername").fill("pera.peric");
        page.locator("#registerJMBG").click();
        page.locator("#registerJMBG").fill("1234567890123");
        page.locator("#registerPassword").click();
        page.locator("#registerPassword").fill("password123");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Registruj se")).click();

        assertThat(page.getByText("Uspešna registracija! Sada se možete prijaviti.")).isVisible();
        assertThat(page.locator("#messageArea")).hasClass("success");
    }

    @Test
    void shouldLoginPatient() {
        String patientToken = createMockJwt("patient_user", List.of("ROLE_PATIENT"));
        page.route("**/api/auth/login", route -> {
            route.fulfill(new Route.FulfillOptions()
                    .setStatus(200)
                    .setContentType("application/json")
                    .setBody(String.format("{\"token\":\"%s\"}", patientToken))
            );
        });

        page.route("**/api/doctors", route -> route.fulfill(new Route.FulfillOptions()
                .setStatus(200).setBody("[]")));
        page.route("**/api/appointments", route -> route.fulfill(new Route.FulfillOptions()
                .setStatus(200).setBody("[]")));

        page.navigate(BASE_URL);

        page.getByPlaceholder("Korisničko ime").first().click();
        page.getByPlaceholder("Korisničko ime").first().fill("patient_user");
        page.getByPlaceholder("Lozinka").first().click();
        page.getByPlaceholder("Lozinka").first().fill("password");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Prijavi se")).click();

        assertThat(page.getByText("Dobrodošli, pacijente!")).isVisible();
        assertThat(page.locator("#patientView")).isVisible();
        assertThat(page.locator("#authView")).isHidden();
        assertThat(page.locator("#welcomeMessage")).containsText("Prijavljeni ste kao: patient_user");
    }

    @Test
    void shouldLoginDoctor() {
        String doctorToken = createMockJwt("doctor_user", List.of("ROLE_DOCTOR"));
        page.route("**/api/auth/login", route -> {
            route.fulfill(new Route.FulfillOptions()
                    .setStatus(200)
                    .setContentType("application/json")
                    .setBody(String.format("{\"token\":\"%s\"}", doctorToken))
            );
        });

        page.route("**/api/appointments", route -> route.fulfill(new Route.FulfillOptions()
                .setStatus(200).setBody("[]")));

        page.navigate(BASE_URL);

        page.getByPlaceholder("Korisničko ime").first().click();
        page.getByPlaceholder("Korisničko ime").first().fill("doctor_user");
        page.getByPlaceholder("Lozinka").first().click();
        page.getByPlaceholder("Lozinka").first().fill("password");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Prijavi se")).click();

        assertThat(page.getByText("Dobrodošli, doktore!")).isVisible();
        assertThat(page.locator("#doctorView")).isVisible();
        assertThat(page.locator("#authView")).isHidden();
    }

    @Test
    void shouldLoginAdmin() {
        String adminToken = createMockJwt("admin_user", List.of("ROLE_ADMIN"));
        page.route("**/api/auth/login", route -> {
            route.fulfill(new Route.FulfillOptions()
                    .setStatus(200)
                    .setContentType("application/json")
                    .setBody(String.format("{\"token\":\"%s\"}", adminToken))
            );
        });

        page.navigate(BASE_URL);

        page.getByPlaceholder("Korisničko ime").first().click();
        page.getByPlaceholder("Korisničko ime").first().fill("admin_user");
        page.getByPlaceholder("Lozinka").first().click();
        page.getByPlaceholder("Lozinka").first().fill("password");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Prijavi se")).click();

        assertThat(page.getByText("Administracija")).isVisible();
        assertThat(page.locator("#adminView")).isVisible();
        assertThat(page.locator("#authView")).isHidden();
    }

    @Test
    void shouldLogoutSuccessfully() {
        shouldLoginPatient();

        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Odjavi se")).click();

        assertThat(page.locator("#authView")).isVisible();
        assertThat(page.locator("#userInfo")).isHidden();
        assertThat(page.locator("#messageArea")).hasText("Uspešno ste se odjavili.");
    }
}
