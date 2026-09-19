package aq.project.utils.security;

import java.util.Base64;

public abstract class BasicAuthorizationHeaderGenerator {

    public static String getBase64BasicAuthorizationValue(String id, String secret) {
        String keyPass = String.format("%s:%s", id, secret);
        return String.format("%s%s", "Basic ", Base64.getEncoder().encodeToString(keyPass.getBytes()));
    }
}
