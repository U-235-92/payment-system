package aq.project.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccessTokenService {

    public String getAccessTokenHash(String accessTokenKey, String accessTokenHashAlg) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(accessTokenHashAlg);

            byte[] sourceBytes = accessTokenKey.getBytes();

            return new String(messageDigest.digest(sourceBytes));

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}