package aq.project._utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class FallbackAssert {

    private static final int RATE_LIMIT = 100;

    private static final int RETRY_LIMIT = 5;

    public static void assertThrowsRateLimit(
            Class<? extends Throwable> clazz,
            Executable executable
    ) {
        Runnable runnable = () -> {
            try {
                executable.execute();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };

        for(int i = 0; i < RATE_LIMIT; i++) {
            if(i < RATE_LIMIT - 1)
                new Thread(runnable).start();
            else
                Assertions.assertThrows(clazz, executable);
        }
    }

    public static void assertThrowsRetryLimit(
            Class<? extends Throwable> clazz,
            Executable executable
    ) {
        Runnable runnable = () -> {
            try {
                executable.execute();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };

        for(int i = 0; i < RETRY_LIMIT; i++) {
            if(i < RETRY_LIMIT - 1)
                new Thread(runnable).start();
            else
                Assertions.assertThrows(clazz, executable);
        }
    }
}