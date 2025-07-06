package com.ambulanta.zakazivanje_pregleda.website;

import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

public class TestUtils {
    public static String createMockJwt(String username, List<String> roles) {
        String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";

        long exp = Instant.now().getEpochSecond() + 3600; // Expires in 1 hour
        String rolesJson = roles.stream()
                .map(r -> "\"" + r + "\"")
                .collect(Collectors.joining(","));

        String payload = String.format(
                "{\"sub\":\"%s\",\"roles\":[%s],\"exp\":%d}",
                username,
                rolesJson,
                exp
        );

        String encodedHeader = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(header.getBytes());
        String encodedPayload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes());

        return String.format("%s.%s.fakesignature", encodedHeader, encodedPayload);
    }
}
