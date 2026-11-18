package aq.project.utils.security;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class SecurityUtil {

    public static String getHash(String target, String hashAlg) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(hashAlg);

            byte[] sourceBytes = target.getBytes();

            return new String(messageDigest.digest(sourceBytes));

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}