package aq.project.util.telemetry;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import org.aspectj.lang.ProceedingJoinPoint;

public abstract class TelemetryUtils {

    public static Tracer getTracer(String tracerPrefix, String tracerSuffix, OpenTelemetry openTelemetry) {
        String tracerName = tracerPrefix + "." + tracerSuffix;
        return openTelemetry.getTracer(tracerName);
    }

    public static Span getSpan(Tracer tracer, String spanName) {
        return tracer.spanBuilder(spanName).startSpan();
    }

    public static Span getSpan(Tracer tracer, ProceedingJoinPoint pjp) {
        String methodName = pjp.getSignature().getName();
        String className = pjp.getSignature().getDeclaringType().getName();
        return tracer.spanBuilder(className + "." + methodName).startSpan();
    }

    public static String getTraceId(Span span) {
        return span.getSpanContext().getTraceId();
    }

    public static String getSpanId(Span span) {
        return span.getSpanContext().getSpanId();
    }
}
