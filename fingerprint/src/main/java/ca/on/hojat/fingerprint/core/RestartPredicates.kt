package ca.on.hojat.fingerprint.core

object RestartPredicates {

    /**
     * A predicate that will retry all non-fatal failures
     * indefinitely, and timeouts a given number
     * of times.
     *
     * @param timeoutRestartCount The maximum number of times
     * to restart after a timeout.
     */
    fun restartTimeouts(timeoutRestartCount: Int): Reprint.RestartPredicate {
        return object : Reprint.RestartPredicate {
            private var timeoutRestarts = 0

            override fun invoke(
                reason: AuthenticationFailureReason,
                restartCount: Int
            ) =
                reason != AuthenticationFailureReason.TIMEOUT || timeoutRestarts++ < timeoutRestartCount

        }
    }

    /**
     * A predicate that will retry all non-fatal failures
     * indefinitely, and timeouts 5 times.
     */
    fun defaultPredicate() = restartTimeouts(5)
}