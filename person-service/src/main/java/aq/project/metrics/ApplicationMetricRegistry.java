package aq.project.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;

public class MetricsRegistry implements MeterBinder {
    
    @Override
    public void bindTo(MeterRegistry meterRegistry) {

    }
}
