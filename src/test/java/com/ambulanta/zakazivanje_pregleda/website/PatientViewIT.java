package com.ambulanta.zakazivanje_pregleda.website;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PatientViewIT {

    @LocalServerPort
    private int port;

    // Shared between all tests in this class.
    static Playwright playwright;
    static Browser browser;
    static BrowserType.LaunchOptions launchOptions;

    // New instance for each test method.
    BrowserContext context;
    Page page;

    @BeforeAll
    static void launchBrowser() {
        launchOptions = new BrowserType.LaunchOptions().setHeadless(false);
        playwright = Playwright.create();
        browser = playwright.chromium().launch(launchOptions);
    }

    @AfterAll
    static void closeBrowser() {
        playwright.close();
    }

    @BeforeEach
    void createContextAndPage() {
        context = browser.newContext();
        page = context.newPage();
        page.navigate("http://localhost:" + port + "/");
        page.getByPlaceholder("Korisničko ime").first().click();
        page.getByPlaceholder("Korisničko ime").first().fill("test");
        page.getByPlaceholder("Lozinka").first().click();
        page.getByPlaceholder("Lozinka").first().fill("test");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Prijavi se")).click();
    }

    @AfterEach
    void closeContext() {
        context.close();
    }

    @Test
    void shouldBookAppointment() {
        page.waitForSelector("#doctorSelect option");
        page.locator("#doctorSelect").selectOption("1");
        page.locator("#appointmentDate").fill("2025-12-12");
        page.getByText("09:00").click();
        page.locator("#bookAppointmentBtn").click();

        assertThat(page.getByText("Uspešno ste zakazali termin.")).isVisible();
    }

    @Test
    void shouldShowFutureAppointments() {
        page.locator("#appointmentFilter").selectOption("future");
        assertThat(page.getByText("Zakazani termini")).isVisible();
    }

    @Test
    void shouldShowPastAppointments() {
        page.locator("#appointmentFilter").selectOption("past");
        assertThat(page.getByText("Nema prošlih termina.")).isVisible();
    }

    @Test
    void shouldShowAllAppointments() {
        page.locator("#appointmentFilter").selectOption("all");
        assertThat(page.getByText("Zakazani termini")).isVisible();
    }
}
