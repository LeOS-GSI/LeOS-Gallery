package ca.on.hojat.fingerprint.core

import android.content.Context


/**
 * Static methods for performing fingerprint authentication.
 *
 * Call [initialize(Context)] in your application's [Activity.onCreate()]}, then call [authenticate()] to perform authentication.
 */
object Reprint {
    /**
     * Load all available reprint modules.
     *
     * This is equivalent to calling  with the spass module,
     * if included, followed by the marshmallow module.
     */
    fun initialize(context: Context) = ReprintInternal.INSTANCE.initialize(context, null)

    /**
     * Return true if a reprint module is registered that
     * has a fingerprint reader.
     */
    fun isHardwarePresent() = ReprintInternal.INSTANCE.isHardwarePresent

    /**
     * Return true if a reprint module is registered that
     * has registered fingerprints.
     */
    fun hasFingerprintRegistered() = ReprintInternal.INSTANCE.hasFingerprintRegistered()

    /**
     * Start a fingerprint authentication request.
     *
     * Equivalent to calling  with [RestartPredicates.defaultPredicate()].
     *
     * @param listener The listener that will be notified of authentication events.
     */
    fun authenticate(listener: AuthenticationListener) =
        authenticate(listener, RestartPredicates.defaultPredicate())


    /**
     * Start a fingerprint authentication request.
     *
     * If [isHardwarePresent()] or [hasFingerprintRegistered()] return false, no
     * authentication will take place, and the listener's  will immediately be called with the corresponding failure
     * reason. In this case, errorMessage will be non-null, fatal will be true, and the other values
     * are unspecified.
     *
     * @param listener         The listener that will be notified of authentication events.
     * @param restartPredicate A predicate that will be called after each failure. If it returns
     *                         true, the fingerprint sensor will remain active and the listener will
     *                         not be called. If it returns false, the sensor will be turned off and
     *                         onFailure will be called.
     */
    fun authenticate(
        listener: AuthenticationListener,
        restartPredicate: RestartPredicate
    ) = ReprintInternal.INSTANCE.authenticate(listener, restartPredicate)

    /**
     * Cancel any active authentication requests.
     *
     * If no authentication is active, this call has no
     * effect.
     */
    fun cancelAuthentication() = ReprintInternal.INSTANCE.cancelAuthentication()

    interface Logger {
        fun log(message: String)

        fun logException(throwable: Throwable, message: String)
    }

    interface RestartPredicate {
        /**
         * Return true if the authentication should be restarted after the given non-fatal failure.
         *
         * @param reason       The reason for this failure.
         * @param restartCount The number of times this authentication call has already been
         *                     restarted.
         */
        fun invoke(
            reason: AuthenticationFailureReason,
            restartCount: Int
        ):Boolean
    }

}