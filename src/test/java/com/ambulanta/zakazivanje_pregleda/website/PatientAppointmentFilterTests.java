package com.ambulanta.zakazivanje_pregleda.website;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.ambulanta.zakazivanje_pregleda.website.TestUtils.createMockJwt;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class PatientAppointmentFilterTests extends BaseTest {
    private static final String MOCK_APPOINTMENTS_JSON = String.format("""
        [
          {
            "id": 201,
            "appointmentTime": "%s",
            "status": "CONFIRMED",
            "doctor": { "firstName": "Jelena", "lastName": "Jelić", "specialization": "Pedijatar" }
          },
          {
            "id": 202,
            "appointmentTime": "%s",
            "status": "CONFIRMED",
            "doctor": { "firstName": "Petar", "lastName": "Petrović", "specialization": "Kardiolog" }
          }
        ]
        """,
            Instant.now().plus(3, ChronoUnit.DAYS).toString(),
            Instant.now().minus(3, ChronoUnit.DAYS).toString()
    );

    @BeforeEach
    void loginAsPatientAndMockData() {
        String patientToken = createMockJwt("test_patient_filters", List.of("ROLE_PATIENT"));
        page.route("**/api/auth/login", route -> route.fulfill(new Route.FulfillOptions()
                .setStatus(200)
                .setContentType("application/json")
                .setBody(String.format("{\"token\":\"%s\"}", patientToken))));

        page.route("**/api/doctors", route -> route.fulfill(new Route.FulfillOptions()
                .setStatus(200).setBody("[]")));

        page.route("**/api/appointments", route -> route.fulfill(new Route.FulfillOptions()
                .setStatus(200)
                .setContentType("application/json")
                .setBody(MOCK_APPOINTMENTS_JSON)));

        page.navigate(BASE_URL);
        page.getByPlaceholder("Korisničko ime").first().fill("test_patient_filters");
        page.getByPlaceholder("Lozinka").first().fill("password");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Prijavi se")).click();

        assertThat(page.locator("#patientView")).isVisible();
    }

    @Test
    void shouldDisplayFutureAppointmentsByDefault() {
        assertThat(page.locator("#patientAppointments")).containsText("Dr. Jelena Jelić");
        assertThat(page.locator("#patientAppointments")).not().containsText("Dr. Petar Petrović");
    }

    @Test
    void shouldDisplayPastAppointmentsWhenFilterIsChanged() {
        page.locator("#appointmentFilter").selectOption("past");

        assertThat(page.locator("#patientAppointments")).containsText("Dr. Petar Petrović");
        assertThat(page.locator("#patientAppointments")).not().containsText("Dr. Jelena Jelić");
    }

    @Test
    void shouldDisplayAllAppointmentsWhenFilterIsChanged() {
        page.locator("#appointmentFilter").selectOption("all");

        assertThat(page.locator("#patientAppointments")).containsText("Dr. Jelena Jelić");
        assertThat(page.locator("#patientAppointments")).containsText("Dr. Petar Petrović");
    }

    @Test
    void shouldShowMessageWhenNoAppointmentsExist() {
        page.route("**/api/appointments", route -> route.fulfill(new Route.FulfillOptions()
                .setStatus(200)
                .setContentType("application/json")
                .setBody("[]")));

        page.locator("#appointmentFilter").selectOption("all");

        assertThat(page.locator("#patientAppointments"))
                .containsText("Nema termina koji odgovaraju izabranom filteru.");
    }
}
