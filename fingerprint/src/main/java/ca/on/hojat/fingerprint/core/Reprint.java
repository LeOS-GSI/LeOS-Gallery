package ca.on.hojat.fingerprint.core;

import android.content.Context;


/**
 * Static methods for performing fingerprint authentication.
 * <p/>
 * Call {@link #initialize(Context)} in your application's {@code onCreate}, then call
 * to perform authentication.
 */
public class Reprint {
    public interface Logger {
        void log(String message);

        void logException(Throwable throwable, String message);
    }

    public interface RestartPredicate {
        /**
         * Return true if the authentication should be restarted after the given non-fatal failure.
         *
         * @param reason       The reason for this failure.
         * @param restartCount The number of times this authentication call has already been
         *                     restarted.
         */
        boolean invoke(AuthenticationFailureReason reason, int restartCount);
    }

    /**
     * Load all available reprint modules.
     * <p/>
     * This is equivalent to calling  with the spass module,
     * if included, followed by the marshmallow module.
     */
    public static void initialize(Context context) {
        ReprintInternal.INSTANCE.initialize(context, null);
    }

    /**
     * Load all available reprint modules.
     * <p/>
     * This is equivalent to calling  with the spass module,
     * if included, followed by the marshmallow module.
     *
     * @param logger An optional logger instance that will receive log messages from Reprint.
     */
    public static void initialize(Context context, Logger logger) {
        ReprintInternal.INSTANCE.initialize(context, logger);
    }

    /**
     * Return true if a reprint module is registered that has a fingerprint reader.
     */
    public static boolean isHardwarePresent() {
        return ReprintInternal.INSTANCE.isHardwarePresent();
    }

    /**
     * Return true if a reprint module is registered that has registered fingerprints.
     */
    public static boolean hasFingerprintRegistered() {
        return ReprintInternal.INSTANCE.hasFingerprintRegistered();
    }

    /**
     * Start a fingerprint authentication request.
     * <p/>
     * Equivalent to calling  with
     * {@link RestartPredicates#defaultPredicate()}
     *
     * @param listener The listener that will be notified of authentication events.
     */
    public static void authenticate(AuthenticationListener listener) {
        authenticate(listener, RestartPredicates.defaultPredicate());
    }

    /**
     * Start a fingerprint authentication request.
     * <p/>
     * If {@link #isHardwarePresent()} or {@link #hasFingerprintRegistered()} return false, no
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
    public static void authenticate(AuthenticationListener listener, Reprint.RestartPredicate restartPredicate) {
        ReprintInternal.INSTANCE.authenticate(listener, restartPredicate);
    }

    /**
     * Cancel any active authentication requests.
     * <p/>
     * If no authentication is active, this call has no effect.
     */
    public static void cancelAuthentication() {
        ReprintInternal.INSTANCE.cancelAuthentication();
    }
}
