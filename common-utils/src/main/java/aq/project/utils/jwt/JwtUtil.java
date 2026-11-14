package aq.project.utils.jwt;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Base64;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class JwtUtil {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static boolean isJwtExpired(String accessToken) {
        String payload = accessToken.split("\\.")[1];
        Base64.Decoder decoder = Base64.getUrlDecoder();

        JsonNode node = mapper.readTree(decoder.decode(payload));
        Instant now = Instant.now();
        Instant exp = Instant.ofEpochSecond(node.get("exp").asLong());

        return now.isAfter(exp.minusSeconds(30));
    }
}
