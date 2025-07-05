package com.ambulanta.zakazivanje_pregleda.website;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthIT {

    @LocalServerPort
    private int port;

    // Shared between all tests in this class.
    static Playwright playwright;
    static Browser browser;

    // New instance for each test method.
    BrowserContext context;
    Page page;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
    }

    @AfterAll
    static void closeBrowser() {
        playwright.close();
    }

    @BeforeEach
    void createContextAndPage() {
        context = browser.newContext();
        page = context.newPage();
    }

    @AfterEach
    void closeContext() {
        context.close();
    }

    @Test
    void shouldRegisterPatient() {
        page.navigate("http://localhost:" + port + "/");

        page.locator("#registerFirstName").click();
        page.locator("#registerFirstName").fill("test");
        page.locator("#registerLastName").click();
        page.locator("#registerLastName").fill("test");
        page.locator("#registerUsername").click();
        page.locator("#registerUsername").fill("test");
        page.locator("#registerJMBG").click();
        page.locator("#registerJMBG").fill("1234567890123");
        page.locator("#registerPassword").click();
        page.locator("#registerPassword").fill("test");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Registruj se")).click();

        assertThat(page.getByText("Uspešno ste se registrovali. Sada se možete prijaviti.")).isVisible();
    }

    @Test
    void shouldLoginPatient() {
        page.navigate("http://localhost:" + port + "/");

        page.getByPlaceholder("Korisničko ime").first().click();
        page.getByPlaceholder("Korisničko ime").first().fill("test");
        page.getByPlaceholder("Lozinka").first().click();
        page.getByPlaceholder("Lozinka").first().fill("test");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Prijavi se")).click();
        page.locator("#patientView").waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        assertThat(page.getByText("Dobrodošli, pacijente!")).isVisible();
    }

    @Test
    void shouldLoginDoctor() {
        page.navigate("http://localhost:" + port + "/");

        page.getByPlaceholder("Korisničko ime").first().click();
        page.getByPlaceholder("Korisničko ime").first().fill("doktor");
        page.getByPlaceholder("Lozinka").first().click();
        page.getByPlaceholder("Lozinka").first().fill("doktor");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Prijavi se")).click();
        assertThat(page.getByText("Dobrodošli, doktore!")).isVisible();
    }

    @Test
    void shouldLoginAdmin() {
        page.navigate("http://localhost:" + port + "/");

        page.getByPlaceholder("Korisničko ime").first().click();
        page.getByPlaceholder("Korisničko ime").first().fill("admin");
        page.getByPlaceholder("Lozinka").first().click();
        page.getByPlaceholder("Lozinka").first().fill("admin");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Prijavi se")).click();
        page.waitForURL("**/admin");
        assertThat(page.getByText("Administracija")).isVisible();
    }
}
