package aq.project.utils.telemetry;

import reactor.core.publisher.Mono;

import java.security.SecureRandom;

import static aq.project.utils.constants.CustomHttpHeaders.X_TRACE_ID_HEADER;

public abstract class TracePropagator {

    private TracePropagator() { super(); }

    private static String genTraceId() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        StringBuilder traceId = new StringBuilder();
        for(byte b : bytes) {
            traceId.append(String.format("%02x", b));
        }
        return traceId.toString();
    }

    public static Mono<String> fetchTraceId() {
        return Mono.deferContextual(context -> Mono
                .just(context.getOrDefault(X_TRACE_ID_HEADER, genTraceId())));
    }
}
