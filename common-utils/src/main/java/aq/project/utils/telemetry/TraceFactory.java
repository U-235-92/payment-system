package aq.project.utils.telemetry;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.security.SecureRandom;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class TraceFactory {

    public static String genTraceId() {
        byte[] bytes = new byte[16];

        SecureRandom random = new SecureRandom();
        random.nextBytes(bytes);

        StringBuilder traceId = new StringBuilder();

        for(byte b : bytes) {
            traceId.append(String.format("%02x", b));
        }

        return traceId.toString();
    }
}
