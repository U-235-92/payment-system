package aq.project.util;

import aq.project.entities.Transaction;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class SpecificationFactory {

    public static <T> Specification<Transaction> getSpecFor(String attribute, T value) {
        return (r, q, cb) -> value == null
                ? null
                : cb.equal(r.get(attribute), value);
    }

    public static Specification<Transaction> getSpecGreaterOrEqualFor(String attribute, OffsetDateTime value) {
        return (r, q, cb) -> value == null
                ? null
                : cb.greaterThanOrEqualTo(r.get(attribute), value);
    }

    public static Specification<Transaction> getSpecLessOrEqualFor(String attribute, OffsetDateTime value) {
        return (r, q, cb) -> value == null
                ? null
                : cb.lessThanOrEqualTo(r.get(attribute), value);
    }
}