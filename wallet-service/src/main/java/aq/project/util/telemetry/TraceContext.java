package aq.project.util.telemetry;

import org.springframework.stereotype.Component;

@Component
public class TraceContext {

    private final ThreadLocal<String> threadLocal = new ThreadLocal<>();

    public void setTraceId(String traceId) {
        threadLocal.set(traceId);
    }

    public String getTraceId() {
        return threadLocal.get();
    }

    public void clean() {
        threadLocal.remove();
    }
}
