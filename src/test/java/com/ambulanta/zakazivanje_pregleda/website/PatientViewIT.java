package com.ambulanta.zakazivanje_pregleda.website;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.junit.jupiter.api.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.ambulanta.zakazivanje_pregleda.website.TestUtils.createMockJwt;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class PatientViewIT extends BaseTest {
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
            Instant.now().plus(3, ChronoUnit.DAYS).toString(),   // Future appointment
            Instant.now().minus(3, ChronoUnit.DAYS).toString() // Past appointment
    );

    @BeforeEach
    void loginAsPatient() {
        String patientToken = createMockJwt("test_patient", List.of("ROLE_PATIENT"));
        page.route("**/api/auth/login", route -> route.fulfill(new Route.FulfillOptions()
                .setStatus(200)
                .setContentType("application/json")
                .setBody(String.format("{\"token\":\"%s\"}", patientToken))));

        String doctorsJson = "[{\"id\":1,\"firstName\":\"Marko\",\"lastName\":\"Markovic\",\"specialization\":\"Kardiolog\"}]";
        page.route("**/api/doctors", route -> route.fulfill(new Route.FulfillOptions()
                .setStatus(200)
                .setContentType("application/json")
                .setBody(doctorsJson)));

        page.route("**/api/appointments", route -> route.fulfill(new Route.FulfillOptions()
                .setStatus(200)
                .setContentType("application/json")
                .setBody("[]")));

        page.route("**/api/doctors/1/appointments", route -> route.fulfill(new Route.FulfillOptions()
                .setStatus(200)
                .setContentType("application/json")
                .setBody("[]")));

        page.navigate(BASE_URL);
        page.getByPlaceholder("Korisničko ime").first().click();
        page.getByPlaceholder("Korisničko ime").first().fill("test_patient");
        page.getByPlaceholder("Lozinka").first().click();
        page.getByPlaceholder("Lozinka").first().fill("password");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Prijavi se")).click();

        assertThat(page.locator("#patientView")).isVisible();
    }

    @Test
    void shouldBookAppointment() {
        page.locator("#doctorSelect").selectOption("1");

        assertThat(page.locator(".time-slot.available").first()).isVisible();
        assertThat(page.locator("#bookAppointmentBtn")).isDisabled();

        page.locator("#appointmentDate").fill("2028-12-12");

        page.locator(".time-slot.available").first().click();
        assertThat(page.locator(".time-slot.selected")).isVisible();
        assertThat(page.locator("#bookAppointmentBtn")).isEnabled();

        page.locator("#bookAppointmentBtn").click();

        assertThat(page.locator("#messageArea"))
                .hasText("Zahtev za termin je uspešno poslat i čeka obradu.");
    }

    @Test
    void shouldDisplayTimetableAndAllowBooking() {
        page.locator("#doctorSelect").selectOption("1");

        assertThat(page.locator(".time-slot.available").first()).isVisible();
        assertThat(page.locator("#bookAppointmentBtn")).isDisabled();

        page.locator(".time-slot.available").first().click();
        assertThat(page.locator(".time-slot.selected")).isVisible();
        assertThat(page.locator("#bookAppointmentBtn")).isEnabled();

        page.route("**/api/appointments", route -> {
            if ("POST".equalsIgnoreCase(route.request().method())) {
                route.fulfill(new Route.FulfillOptions().setStatus(201));
            } else {
                route.fulfill(new Route.FulfillOptions()
                        .setStatus(200)
                        .setContentType("application/json")
                        .setBody("[]"));
            }
        });

        page.locator("#bookAppointmentBtn").click();

        assertThat(page.locator("#messageArea"))
                .hasText("Zahtev za termin je uspešno poslat i čeka obradu.");
    }
}
