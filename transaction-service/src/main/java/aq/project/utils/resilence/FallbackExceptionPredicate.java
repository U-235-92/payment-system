package aq.project.utils.resilence;

import aq.project.exceptions.ServiceHttpException;
import lombok.NoArgsConstructor;

import java.util.function.Predicate;

@NoArgsConstructor
public class FallbackExceptionPredicate<T extends Throwable> implements Predicate<T> {

    @Override
    public boolean test(T throwable) {
        return throwable instanceof ServiceHttpException;
    }
}
