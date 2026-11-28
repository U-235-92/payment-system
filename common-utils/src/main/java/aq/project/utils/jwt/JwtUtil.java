package aq.project.utils.jwt;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.function.Supplier;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class JwtUtil {

    public static final String ADMIN_ACCESS_TOKEN = "admin-access-token";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static String toBearerToken(String jwt) {
        return "Bearer " + jwt;
    }

    public static String getAdminJwt(
            Supplier<String> jwtSupplier,
            Map<String, String> jwtRepository
    ) {
        if(jwtRepository.isEmpty()) {
            updateJwt(jwtSupplier, jwtRepository);

            return jwtRepository.get(ADMIN_ACCESS_TOKEN);
        } else {
            String accessToken = jwtRepository.get(ADMIN_ACCESS_TOKEN);

            if(JwtUtil.isJwtExpired(accessToken)) {
                updateJwt(jwtSupplier, jwtRepository);

                return jwtRepository.get(ADMIN_ACCESS_TOKEN);
            }
            return accessToken;
        }
    }

    private static void updateJwt(
            Supplier<String> jwtSupplier,
            Map<String, String> jwtRepository
    ) {
        jwtRepository.put(ADMIN_ACCESS_TOKEN, jwtSupplier.get());
    }

    public static boolean isJwtExpired(String accessToken) {
        String payload = accessToken.split("\\.")[1];
        Base64.Decoder decoder = Base64.getUrlDecoder();

        JsonNode node = OBJECT_MAPPER.readTree(decoder.decode(payload));

        Instant now = Instant.now();
        Instant exp = Instant.ofEpochSecond(node.get("exp").asLong());

        return now.isAfter(exp.minusSeconds(30));
    }
}