package com.ambulanta.zakazivanje_pregleda.website;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.ambulanta.zakazivanje_pregleda.website.TestUtils.createMockJwt;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class DoctorViewIT extends BaseTest {

    private static final String MOCK_APPOINTMENTS_JSON = String.format("""
        [
          {
            "id": 101,
            "appointmentTime": "%s",
            "status": "CONFIRMED",
            "patient": { "firstName": "Ana", "lastName": "Anić", "jmbg": "1111111111111" }
          },
          {
            "id": 102,
            "appointmentTime": "%s",
            "status": "CONFIRMED",
            "patient": { "firstName": "Marko", "lastName": "Marković", "jmbg": "2222222222222" }
          }
        ]
        """,
            Instant.now().plus(5, ChronoUnit.DAYS).toString(),
            Instant.now().minus(5, ChronoUnit.DAYS).toString()
    );

    @BeforeEach
    void loginAsDoctor() {
        String doctorToken = createMockJwt("dr_house", List.of("ROLE_DOCTOR"));

        page.route("**/api/auth/login", route -> route.fulfill(new Route.FulfillOptions()
                .setStatus(200)
                .setContentType("application/json")
                .setBody(String.format("{\"token\":\"%s\"}", doctorToken))));

        page.route("**/api/appointments?status=CONFIRMED", route -> route.fulfill(new Route.FulfillOptions()
                .setStatus(200)
                .setContentType("application/json")
                .setBody(MOCK_APPOINTMENTS_JSON)));

        page.navigate(BASE_URL);
        page.getByPlaceholder("Korisničko ime").first().click();
        page.getByPlaceholder("Korisničko ime").first().fill("dr_house");
        page.getByPlaceholder("Lozinka").first().click();
        page.getByPlaceholder("Lozinka").first().fill("password");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Prijavi se")).click();

        assertThat(page.locator("#doctorView")).isVisible();
        assertThat(page.locator("#welcomeMessage")).containsText("Prijavljeni ste kao: dr_house");
    }

    @Test
    void shouldShowFutureAppointmentsByDefault() {
        assertThat(page.locator("#doctorAppointments")).containsText("Ana Anić");
        assertThat(page.locator("#doctorAppointments")).not().containsText("Marko Marković");
    }

    @Test
    void shouldDisplayPastAppointmentsWhenFilterIsChanged() {
        page.locator("#doctorAppointmentFilter").selectOption("past");

        assertThat(page.locator("#doctorAppointments")).containsText("Marko Marković");
        assertThat(page.locator("#doctorAppointments")).not().containsText("Ana Anić");
    }

    @Test
    void shouldDisplayAllAppointmentsWhenFilterIsChanged() {
        page.locator("#doctorAppointmentFilter").selectOption("all");

        assertThat(page.locator("#doctorAppointments")).containsText("Ana Anić");
        assertThat(page.locator("#doctorAppointments")).containsText("Marko Marković");
    }

    @Test
    void shouldShowMessageWhenNoAppointmentsMatchFilter() {
        page.route("**/api/appointments?status=CONFIRMED", route -> route.fulfill(new Route.FulfillOptions()
                .setStatus(200)
                .setContentType("application/json")
                .setBody("[]")));

        page.locator("#doctorAppointmentFilter").selectOption("all");

        assertThat(page.locator("#doctorAppointments"))
                .containsText("Nema termina koji odgovaraju izabranom filteru.");
    }
}
