package aq.project.util;

import io.opentelemetry.api.trace.Span;

public final class Observability {

    private Observability() {};

    public static String getTraceId(Span span) {
        return span.getSpanContext().getTraceId();
    }

    public static String getSpanId(Span span) {
        return span.getSpanContext().getSpanId();
    }
}
