package aq.project.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorizationService {

    @Getter
    @Value("${app.service.payment-service.merchant-id}")
    private String merchantId;

    @Value("${app.service.payment-service.merchant-secret}")
    private String merchantSecret;

    public String getAuthorization() {
        String keyPass = String.format("%s:%s", merchantId, merchantSecret);
        String base64KeyPass = Base64.getEncoder().encodeToString(keyPass.getBytes());
        return String.format("Basic %s", base64KeyPass);
    }
}