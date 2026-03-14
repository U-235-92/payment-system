package aq.project.util.observability;

import io.opentelemetry.api.trace.Span;

public final class ObserverUtils {

    private ObserverUtils() {};

    public static String getTraceId(Span span) {
        return span.getSpanContext().getTraceId();
    }

    public static String getSpanId(Span span) {
        return span.getSpanContext().getSpanId();
    }
}
