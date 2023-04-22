package ca.on.hojat.fingerprint.core

/**
 * A listener that is notified of the results of fingerprint
 * authentication.
 */
interface AuthenticationListener {

    /**
     * Called after a fingerprint is successfully authenticated.
     *
     * @param moduleTag The [ReprintModule.tag()] of the module
     * that was used for authentication.
     */
    fun onSuccess(moduleTag: Int)

    /**
     * Called after an error or authentication failure.
     *
     * @param failureReason The general reason for the failure.
     * @param fatal         If true, the failure is unrecoverable and the sensor is no longer running. If
     *                      false, the sensor is still running, and one or more callbacks will be called in
     *                      the future.
     * @param errorMessage  An informative string given by the underlying fingerprint sdk that can be
     *                      displayed in the ui. This string is never null, and will be localized to the
     *                      current locale. You should show this text to the user, or some other message of
     *                      your own based on the failureReason.
     * @param moduleTag     The [ReprintModule.tag()] of the module that is currently active. This is
     *                      useful to know the meaning of the error code.
     * @param errorCode     The specific error code returned by the module's underlying sdk. Check the
     *                      constants defined in the module for possible values and their meanings.
     */
    fun onFailure(
        failureReason: AuthenticationFailureReason,
        fatal: Boolean,
        errorMessage: CharSequence,
        moduleTag: Int,
        errorCode: Int
    )
}