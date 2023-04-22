package ca.on.hojat.fingerprint.core;


import static ca.on.hojat.fingerprint.core.AuthenticationFailureReason.TIMEOUT;

import androidx.annotation.NonNull;

public class RestartPredicates {
    /**
     * A predicate that will retry all non-fatal failures indefinitely, and timeouts a given number
     * of times.
     *
     * @param timeoutRestartCount The maximum number of times to restart after a timeout.
     */
    public static Reprint.RestartPredicate restartTimeouts(final int timeoutRestartCount) {
        return new Reprint.RestartPredicate() {
            private int timeoutRestarts = 0;

            @Override
            public boolean invoke(@NonNull AuthenticationFailureReason reason, int restartCount) {
                return reason != TIMEOUT || timeoutRestarts++ < timeoutRestartCount;
            }
        };
    }

    /**
     * A predicate that will retry all non-fatal failures indefinitely, and timeouts 5 times.
     */
    public static Reprint.RestartPredicate defaultPredicate() {
        return restartTimeouts(5);
    }

}
