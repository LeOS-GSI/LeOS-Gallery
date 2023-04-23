package ca.on.hojat.fingerprint.core

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.core.os.CancellationSignal
import ca.on.hojat.fingerprint.R
import ca.on.hojat.fingerprint.core.Reprint.RestartPredicate
import ca.on.hojat.fingerprint.module.MarshmallowReprintModule
import java.util.concurrent.atomic.AtomicReference
import timber.log.Timber

/**
 * Methods for performing fingerprint authentication.
 */
internal enum class ReprintInternal {

    @SuppressLint("StaticFieldLeak")
    INSTANCE;

    private val cancellationSignal = AtomicReference<CancellationSignal>()
    private var module: ReprintModule? = null
    private var context: Context? = null
    fun initialize(context: Context, logger: Reprint.Logger?) {
        var logger = logger
        this.context = context.applicationContext

        // The SPass module doesn't work below API 17, and the Imprint module obviously requires
        // Marshmallow.
        if (module != null) return
        if (logger == null) logger = NULL_LOGGER

        // Only use the Spass module on APIs that don't support Imprint.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            registerSpassModule(context, logger)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val marshmallowModule = MarshmallowReprintModule(context, logger)

            // Some phones like the Galaxy S5 run marshmallow, but only work with Spass
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M && !marshmallowModule.isHardwarePresent()) {
                registerSpassModule(context, logger)
            } else {
                registerModule(marshmallowModule)
            }
        }
    }

    private fun registerSpassModule(context: Context, logger: Reprint.Logger) {
        try {
            val spassModuleClass = Class.forName(REPRINT_SPASS_MODULE)
            val constructor = spassModuleClass.getConstructor(
                Context::class.java, Reprint.Logger::class.java
            )
            val module = constructor.newInstance(context, logger) as ReprintModule
            registerModule(module)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    fun registerModule(module: ReprintModule) {
        if (this.module != null && module.tag() == this.module!!.tag()) {
            return
        }
        if (module.isHardwarePresent()) {
            this.module = module
        }
    }

    val isHardwarePresent: Boolean
        get() = module != null && module!!.isHardwarePresent()

    fun hasFingerprintRegistered(): Boolean {
        return module != null && module!!.hasFingerprintRegistered()
    }

    /**
     * Start an authentication request.
     *
     * @param listener         The listener to be notified.
     * @param restartPredicate The predicate that determines whether to restart or not.
     */
    fun authenticate(listener: AuthenticationListener, restartPredicate: RestartPredicate) {
        if (module == null || !module!!.isHardwarePresent()) {
            listener.onFailure(
                AuthenticationFailureReason.NO_HARDWARE, true,
                getString(R.string.fingerprint_error_hw_not_available)!!, 0, 0
            )
            return
        }
        if (!module!!.hasFingerprintRegistered()) {
            listener.onFailure(
                AuthenticationFailureReason.NO_FINGERPRINTS_REGISTERED, true,
                "Not recognized", 0, 0
            )
            return
        }
        cancellationSignal.set(CancellationSignal())
        module!!.authenticate(cancellationSignal.get(), listener, restartPredicate)
    }

    fun cancelAuthentication() {
        val signal = cancellationSignal.getAndSet(null)
        if (signal != null) {
            try {
                signal.cancel()
            } catch (e: NullPointerException) {
                Timber.e(e)
            }
        }
    }

    private fun getString(resid: Int): String? {
        return if (context == null) null else context!!.getString(resid)
    }

    companion object {
        val NULL_LOGGER: Reprint.Logger = object : Reprint.Logger {
            override fun log(message: String) {}
            override fun logException(throwable: Throwable, message: String) {}
        }
        private const val REPRINT_SPASS_MODULE =
            "ca.on.hojat.fingerprint.module.spass.SpassReprintModule"
    }
}