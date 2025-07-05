package com.ambulanta.zakazivanje_pregleda.website;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AdminViewIT {

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
        page.getByPlaceholder("Korisničko ime").first().fill("admin");
        page.getByPlaceholder("Lozinka").first().click();
        page.getByPlaceholder("Lozinka").first().fill("admin123");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Prijavi se")).click();
    }

    @AfterEach
    void closeContext() {
        context.close();
    }

    @Test
    void shouldAddDoctor() {
        page.locator("#doctorFirstName").click();
        page.locator("#doctorFirstName").fill("test");
        page.locator("#doctorLastName").click();
        page.locator("#doctorLastName").fill("test");
        page.locator("#doctorUsername").click();
        page.locator("#doctorUsername").fill("testdoktor");
        page.locator("#doctorPassword").click();
        page.locator("#doctorPassword").fill("test");
        page.locator("#doctorSpecialization").click();
        page.locator("#doctorSpecialization").fill("test");
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Dodaj lekara")).click();

        assertThat(page.getByText("Uspešno ste dodali novog lekara.")).isVisible();
    }
}
