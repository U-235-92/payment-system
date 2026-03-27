package aq.project.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ApplicationMetricRegistry implements MeterBinder {

    private static final String STATUS = "status";
    private static final String SUCCESS = "success";
    private static final String FAIL = "fail";
    private static final String BY = "by";
    private static final String ID = "id";
    private static final String EMAIL = "email";

    private Counter successCreatePersonCounter;
    private Counter failCreatePersonCounter;

    private Counter successUpdatePersonCounter;
    private Counter failUpdatePersonCounter;

    private Counter successUndoUpdatePersonCounter;
    private Counter failUndoUpdatePersonCounter;

    private Counter successDeletePersonByPersonIdCounter;
    private Counter failDeletePersonByPersonIdCounter;

    private Counter successUndoDeletePersonCounter;
    private Counter failUndoDeletePersonCounter;

    private Counter successDeletePersonByKeycloakIdCounter;
    private Counter failDeletePersonByKeycloakIdCounter;

    private Counter successReadPersonByKeycloakIdCounter;
    private Counter failReadPersonByKeycloakIdCounter;

    private Counter successReadPersonByPersonIdCounter;
    private Counter failReadPersonByPersonIdCounter;

    private Counter successReadPersonByEmailCounter;
    private Counter failReadPersonByEmailCounter;

    @Override
    public void bindTo(MeterRegistry meterRegistry) {
        successCreatePersonCounter = getCounter("create_person_count", meterRegistry, List.of(Tag.of(STATUS, SUCCESS)));
        failCreatePersonCounter = getCounter("create_person_count", meterRegistry, List.of(Tag.of(STATUS, FAIL)));

        successUpdatePersonCounter = getCounter("update_person_count", meterRegistry, List.of(Tag.of(STATUS, SUCCESS)));
        failUpdatePersonCounter = getCounter("update_person_count", meterRegistry, List.of(Tag.of(STATUS, FAIL)));

        successUndoUpdatePersonCounter = getCounter("undo_update_person_count", meterRegistry, List.of(Tag.of(STATUS, SUCCESS)));
        failUndoUpdatePersonCounter = getCounter("undo_update_person_count", meterRegistry, List.of(Tag.of(STATUS, FAIL)));

        successDeletePersonByPersonIdCounter = getCounter("delete_person_by_person_id_count", meterRegistry, List.of(Tag.of(STATUS, SUCCESS)));
        failDeletePersonByPersonIdCounter = getCounter("delete_person_by_person_id_count", meterRegistry, List.of(Tag.of(STATUS, FAIL)));

        successDeletePersonByKeycloakIdCounter = getCounter("delete_person_by_keycloak_id_count", meterRegistry, List.of(Tag.of(STATUS, SUCCESS)));
        failDeletePersonByKeycloakIdCounter = getCounter("delete_person_by_keycloak_id_count", meterRegistry, List.of(Tag.of(STATUS, FAIL)));

        successUndoDeletePersonCounter = getCounter("undo_delete_person_count", meterRegistry, List.of(Tag.of(STATUS, SUCCESS)));
        failUndoDeletePersonCounter = getCounter("undo_delete_person_count", meterRegistry, List.of(Tag.of(STATUS, FAIL)));

        successReadPersonByKeycloakIdCounter = getCounter("read_person_by_keycloak_id_count", meterRegistry, List.of(Tag.of(STATUS, SUCCESS), Tag.of(BY, ID)));
        failReadPersonByKeycloakIdCounter = getCounter("read_person_by_keycloak_id_count", meterRegistry, List.of(Tag.of(STATUS, FAIL), Tag.of(BY, ID)));

        successReadPersonByPersonIdCounter = getCounter("read_person_by_person_id_count", meterRegistry, List.of(Tag.of(STATUS, SUCCESS), Tag.of(BY, ID)));
        failReadPersonByPersonIdCounter = getCounter("read_person_by_person_id_count", meterRegistry, List.of(Tag.of(STATUS, FAIL), Tag.of(BY, ID)));

        successReadPersonByEmailCounter = getCounter("read_person_count", meterRegistry, List.of(Tag.of(STATUS, SUCCESS), Tag.of(BY, EMAIL)));
        failReadPersonByEmailCounter = getCounter("read_person_count", meterRegistry, List.of(Tag.of(STATUS, FAIL), Tag.of(BY, EMAIL)));
    }

    private Counter getCounter(String metricName, MeterRegistry meterRegistry, Iterable<Tag> tags) {
        return Counter.builder("person_service." + metricName)
                .tags(tags)
                .register(meterRegistry);
    }

    public void incrementSuccessCreatePersonCounter() {
        successCreatePersonCounter.increment();
    }

    public void incrementFailCreatePersonCounter() {
        failCreatePersonCounter.increment();
    }

    public void incrementSuccessUpdatePersonCounter() {
        successUpdatePersonCounter.increment();
    }

    public void incrementFailUpdatePersonCounter() {
        failUpdatePersonCounter.increment();
    }

    public void incrementSuccessUndoUpdatePersonCounter() {
        successUndoUpdatePersonCounter.increment();
    }

    public void incrementFailUndoUpdatePersonCounter() {
        failUndoUpdatePersonCounter.increment();
    }

    public void incrementSuccessDeletePersonByPersonIdCounter() {
        successDeletePersonByPersonIdCounter.increment();
    }

    public void incrementFailDeletePersonByPersonIdCounter() {
        failDeletePersonByPersonIdCounter.increment();
    }

    public void incrementSuccessDeletePersonByKeycloakIdCounter() {
        successDeletePersonByKeycloakIdCounter.increment();
    }

    public void incrementFailDeletePersonByKeycloakIdCounter() {
        failDeletePersonByKeycloakIdCounter.increment();
    }

    public void incrementSuccessUndoDeletePersonCounter() {
        successUndoDeletePersonCounter.increment();
    }

    public void incrementFailUndoDeletePersonCounter() {
        failUndoDeletePersonCounter.increment();
    }

    public void incrementSuccessGetPersonByKeycloakIdCounter() {
        successReadPersonByKeycloakIdCounter.increment();
    }

    public void incrementFailGetPersonByKeycloakIdCounter() {
        failReadPersonByKeycloakIdCounter.increment();
    }

    public void incrementSuccessReadPersonByPersonIdCounter() {
        successReadPersonByPersonIdCounter.increment();
    }

    public void incrementFailReadPersonByPersonIdCounter() {
        failReadPersonByPersonIdCounter.increment();
    }

    public void incrementSuccessReadPersonByEmailCounter() {
        successReadPersonByEmailCounter.increment();
    }

    public void incrementFailReadPersonByEmailCounter() {
        failReadPersonByEmailCounter.increment();
    }
}
