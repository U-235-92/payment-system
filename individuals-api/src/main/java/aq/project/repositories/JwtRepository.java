package aq.project.repositories;

import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class JwtRepository {

    private static final String ADMIN_ACCESS_TOKEN = "admin_access_token";

    private final Map<String, String> jwtRepository = new HashMap<>();

    public boolean isAdminJwtExist() {
        return jwtRepository.get(ADMIN_ACCESS_TOKEN) != null;
    }

    public String getAdminJwt() {
        return jwtRepository.get(ADMIN_ACCESS_TOKEN);
    }

    public void putAdminJwt(String token) {
        jwtRepository.put(ADMIN_ACCESS_TOKEN, token);
    }
}
