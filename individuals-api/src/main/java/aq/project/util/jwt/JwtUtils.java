package aq.project.util.jwt;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Base64;

public final class JwtUtils {

    public static boolean isTokenExpired(String accessToken) {
        String payload = accessToken.split("\\.")[1];
        Base64.Decoder decoder = Base64.getDecoder();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(decoder.decode(payload));
        Instant now = Instant.now();
        Instant exp = Instant.ofEpochSecond(node.get("exp").asLong());
        return now.isAfter(exp);
    }
}
