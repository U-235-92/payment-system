package aq.project.repositories;

import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class JwtTokenRepository {

    private static final String ADMIN_ACCESS_TOKEN = "admin_access_token";

    private final Map<String, String> jwtRepository = new HashMap<>();

    public boolean isAdminTokenExists() {
        return jwtRepository.get(ADMIN_ACCESS_TOKEN) != null;
    }

    public String getAdminAccessToken() {
        return jwtRepository.get(ADMIN_ACCESS_TOKEN);
    }

    public void putAdminAccessToken(String token) {
        jwtRepository.put(ADMIN_ACCESS_TOKEN, token);
    }
}
