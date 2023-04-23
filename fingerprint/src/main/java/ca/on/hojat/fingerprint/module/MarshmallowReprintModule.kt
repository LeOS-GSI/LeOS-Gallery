package ca.on.hojat.fingerprint.module

import android.annotation.TargetApi
import android.content.Context
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.os.CancellationSignal
import ca.on.hojat.fingerprint.R
import ca.on.hojat.fingerprint.core.AuthenticationFailureReason
import ca.on.hojat.fingerprint.core.AuthenticationListener
import ca.on.hojat.fingerprint.core.Reprint
import ca.on.hojat.fingerprint.core.Reprint.RestartPredicate
import ca.on.hojat.fingerprint.core.ReprintModule

/**
 * A reprint module that authenticates fingerprint using
 * the marshmallow Imprint API.
 *
 * This module supports most phones running Android Marshmallow.
 *
 * The values of error codes provided by the api overlap
 * for fatal and non-fatal authentication failures.
 * Fatal error code constants start with FINGERPRINT_ERROR,
 * and non-fatal error codes start with FINGERPRINT_ACQUIRED.
 */
@TargetApi(Build.VERSION_CODES.M)
@RequiresApi(Build.VERSION_CODES.M)
class MarshmallowReprintModule(
    var context: Context,
    val logger: Reprint.Logger
) : ReprintModule {

    init {
        // Only an application context is needed here
        this.context = context.applicationContext
    }

    companion object {
        private const val TAG = 1

        // The following FINGERPRINT constants are copied from FingerprintManager, since that class
        // isn't available pre-marshmallow, and they aren't defined in FingerprintManagerCompat for some
        // reason.

        /**
         * The hardware is unavailable. Try again later.
         */
        const val FINGERPRINT_ERROR_HW_UNAVAILABLE = 1

        /**
         * Error state returned when the sensor was unable
         * to process the current image.
         */
        const val FINGERPRINT_ERROR_UNABLE_TO_PROCESS = 2

        /**
         * Error state returned when the current request
         * has been running too long. This is intended to
         * prevent programs from waiting for the fingerprint
         * sensor indefinitely. The timeout is platform and
         * sensor-specific, but is generally in the order of
         * 30 seconds.
         */
        const val FINGERPRINT_ERROR_TIMEOUT = 3

        /**
         * Error state returned for operations like enrollment;
         * the operation cannot be completed because there's
         * not enough storage remaining to complete the
         * operation.
         */
        const val FINGERPRINT_ERROR_NO_SPACE = 4

        /**
         * The operation was canceled because the fingerprint
         * sensor is unavailable. For example, this may happen
         * when the user is switched, the device is locked or
         * another pending operation prevents or disables it.
         */
        const val FINGERPRINT_ERROR_CANCELED = 5

        /**
         * The operation was canceled because the API is
         * locked out due to too many attempts.
         */
        const val FINGERPRINT_ERROR_LOCKOUT = 7

        // The following ACQUIRED constants are used with
        // help messages

        /**
         * The fingerprint image was too noisy to process
         * due to a detected condition (i.e. dry skin) or
         * a possibly dirty sensor (See [FINGERPRINT_ACQUIRED_IMAGER_DIRTY]).
         */
        const val FINGERPRINT_ACQUIRED_INSUFFICIENT = 2

        /**
         * The fingerprint image was too noisy due to suspected
         * or detected dirt on the sensor. For example, it's
         * reasonable to return this after multiple [FINGERPRINT_ACQUIRED_INSUFFICIENT]
         * or actual detection of dirt on the sensor
         * (stuck pixels, swaths, etc.). The user is expected
         * to take action to clean the sensor when this is returned.
         */
        const val FINGERPRINT_ACQUIRED_IMAGER_DIRTY = 3

        /**
         * A fingerprint was read that is not registered.
         *
         *
         * This constant is defined by reprint, and is not in
         * the FingerprintManager.
         */
        const val FINGERPRINT_AUTHENTICATION_FAILED = 1001
    }

    override fun isHardwarePresent(): Boolean {
        val fingerprintManager = fingerprintManager() ?: return false

        // Normally, a security exception is only thrown if
        // you don't have the USE_FINGERPRINT permission in
        // your manifest. However, some OEMs have pushed updates
        // to M for phones that don't have sensors at all, and
        // for some reason decided not to implement the
        // USE_FINGERPRINT permission. So on those devices, a
        // SecurityException is raised no matter what. This has
        // been confirmed on a number of devices, including the
        // LG LS770, LS991, and the HTC One M8.
        //
        // On Robolectric, FingerprintManager.isHardwareDetected
        // raises an NPE.
        return try {
            fingerprintManager.isHardwareDetected
        } catch (e: SecurityException) {
            logger.logException(
                e,
                "MarshmallowReprintModule: isHardwareDetected failed unexpectedly"
            )
            false
        } catch (e: NullPointerException) {
            logger.logException(
                e,
                "MarshmallowReprintModule: isHardwareDetected failed unexpectedly"
            )
            false
        }
    }

    @Throws(SecurityException::class)
    override fun hasFingerprintRegistered(): Boolean {
        val fingerprintManager = fingerprintManager() ?: return false

        // Some devices with fingerprint sensors throw an
        // IllegalStateException when trying to parse an
        // internal settings file during this call. See #29
        // (on Reprint library not on Smart Gallery).
        return try {
            fingerprintManager.hasEnrolledFingerprints()
        } catch (e: IllegalStateException) {
            logger.logException(
                e,
                "MarshmallowReprintModule: hasEnrolledFingerprints failed unexpectedly"
            )
            false
        }
    }

    override fun authenticate(
        cancellationSignal: CancellationSignal,
        listener: AuthenticationListener,
        restartPredicate: RestartPredicate
    ) {
        authenticate(cancellationSignal, listener, restartPredicate, 0)
    }

    @Throws(SecurityException::class)
    fun authenticate(
        cancellationSignal: CancellationSignal?,
        listener: AuthenticationListener,
        restartPredicate: RestartPredicate,
        restartCount: Int
    ) {
        val fingerprintManager = fingerprintManager()

        if (fingerprintManager == null) {
            listener.onFailure(
                AuthenticationFailureReason.UNKNOWN,
                true,
                context.getString(R.string.fingerprint_error_hw_not_available),
                TAG,
                FINGERPRINT_ERROR_CANCELED
            )
            return
        }

        val callback = AuthCallback(
            restartCount,
            restartPredicate,
            cancellationSignal!!,
            listener
        )

        // Why getCancellationSignalObject returns an Object is unexplained
        val signalObject =
            cancellationSignal.cancellationSignalObject as android.os.CancellationSignal

        // Occasionally, an NPE will bubble up out of
        // FingerprintManager.authenticate
        try {
            fingerprintManager.authenticate(
                null,
                signalObject,
                0,
                callback,
                null
            )
        } catch (e: NullPointerException) {
            logger.logException(e, "MarshmallowReprintModule: authenticate failed unexpectedly")
            listener.onFailure(
                AuthenticationFailureReason.UNKNOWN,
                true,
                context.getString(R.string.fingerprint_error_unable_to_process),
                TAG,
                FINGERPRINT_ERROR_CANCELED
            )
        }
    }

    override fun tag() = TAG

    /**
     * We used to use the appcompat library to load the fingerprint manager, but v25.1.0 was broken
     * on many phones. Instead, we handle the manager ourselves. FingerprintManagerCompat just
     * forwards calls anyway, so it doesn't add any value for us.
     *
     */
    private fun fingerprintManager(): FingerprintManager? {

        return try {
            context.getSystemService(FingerprintManager::class.java)
        } catch (e: Exception) {
            logger.logException(
                e,
                "Could not get fingerprint system service on API that should support it."
            )
            null
        } catch (e: NoClassDefFoundError) {
            logger.log("FingerprintManager not available on this device")
            null
        }
    }

    inner class AuthCallback(
        private var restartCount: Int,
        private val restartPredicate: RestartPredicate,
        private val cancellationSignal: CancellationSignal,
        private var listener: AuthenticationListener?
    ) : FingerprintManager.AuthenticationCallback() {


        override fun onAuthenticationError(
            errMsgId: Int,
            errString: CharSequence?
        ) {
            if (listener == null)
                return

            var failureReason = AuthenticationFailureReason.UNKNOWN
            when (errMsgId) {
                FINGERPRINT_ERROR_HW_UNAVAILABLE -> {
                    failureReason = AuthenticationFailureReason.HARDWARE_UNAVAILABLE
                }

                FINGERPRINT_ERROR_UNABLE_TO_PROCESS, FINGERPRINT_ERROR_NO_SPACE -> {
                    failureReason = AuthenticationFailureReason.SENSOR_FAILED
                }

                FINGERPRINT_ERROR_TIMEOUT -> {
                    failureReason = AuthenticationFailureReason.TIMEOUT
                }

                FINGERPRINT_ERROR_LOCKOUT -> {
                    failureReason = AuthenticationFailureReason.LOCKED_OUT
                }

                FINGERPRINT_ERROR_CANCELED -> {
                    // don't send a cancelled message
                    return
                }

            }

            if (
                errMsgId == FINGERPRINT_ERROR_TIMEOUT && restartPredicate.invoke(
                    failureReason,
                    restartCount
                )
            ) {
                authenticate(
                    cancellationSignal,
                    listener!!,
                    restartPredicate,
                    restartCount
                )

            } else {
                listener?.onFailure(failureReason, true, errString!!, TAG, errMsgId)
                listener = null
            }

        }

        override fun onAuthenticationHelp(
            helpMsgId: Int,
            helpString: CharSequence?
        ) {
            if (listener == null)
                return

            if (
                restartPredicate.invoke(AuthenticationFailureReason.SENSOR_FAILED, restartCount++)
                    .not()
            )
                cancellationSignal.cancel()

            listener?.onFailure(
                AuthenticationFailureReason.SENSOR_FAILED,
                false, helpString!!, TAG, helpMsgId
            )
        }

        override fun onAuthenticationSucceeded(
            result: FingerprintManager.AuthenticationResult?
        ) {
            if (listener == null)
                return
            listener?.onSuccess(TAG)
            listener = null
        }

        override fun onAuthenticationFailed() {

            if (listener == null)
                return

            listener?.onFailure(
                AuthenticationFailureReason.AUTHENTICATION_FAILED,
                false,
                "Not recognized",
                TAG,
                FINGERPRINT_AUTHENTICATION_FAILED
            )
        }
    }
}