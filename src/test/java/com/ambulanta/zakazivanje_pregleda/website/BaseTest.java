package com.ambulanta.zakazivanje_pregleda.website;

import com.microsoft.playwright.*;
import org.eclipse.jetty.ee10.webapp.WebAppContext;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.eclipse.jetty.server.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;

public class BaseTest {
    protected static String BASE_URL = "http://localhost:8080";
    private static int PORT;

    private static Server server;
    private static Playwright playwright;
    private static Browser browser;

    protected BrowserContext context;
    protected Page page;

    @BeforeAll
    static void startServerAndBrowser() throws Exception {
        PORT = findFreePort();
        BASE_URL = "http://localhost:" + PORT;

        server = new Server(PORT);
        WebAppContext webAppContext = new WebAppContext();
        webAppContext.setContextPath("/");

        Resource staticResources = ResourceFactory.of(webAppContext).newClassLoaderResource("static");
        if (staticResources == null) {
            throw new RuntimeException("Could not find static resource folder.");
        }
        webAppContext.setBaseResource(staticResources);

        server.setHandler(webAppContext);
        server.start();

        playwright = Playwright.create();
        browser = playwright.chromium().launch(
                // new BrowserType.LaunchOptions().setHeadless(false)
        );
    }


    @AfterAll
    static void stopServerAndBrowser() throws Exception {
        if (playwright != null) {
            playwright.close();
            playwright = null;
        }
        if (server != null) {
            server.stop();
            server = null;
        }
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

    private static int findFreePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new RuntimeException("Could not find a free port.", e);
        }
    }
}