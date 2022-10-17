package ca.on.hojat.fingerprint.core;

import android.content.Context;
import android.os.Build;
import android.annotation.SuppressLint;
import java.util.concurrent.atomic.AtomicReference;
import androidx.core.os.CancellationSignal;
import java.lang.reflect.Constructor;
import ca.on.hojat.fingerprint.module.MarshmallowReprintModule;
import ca.on.hojat.fingerprint.R;

/**
 * Methods for performing fingerprint authentication.
 */
enum ReprintInternal {
    @SuppressLint("StaticFieldLeak") INSTANCE;

    public static final Reprint.Logger NULL_LOGGER = new Reprint.Logger() {
        public void log(String message) {
        }

        public void logException(Throwable throwable, String message) {
        }
    };

    private static final String REPRINT_SPASS_MODULE = "ca.on.hojat.fingerprint.module.spass.SpassReprintModule";
    private final AtomicReference<CancellationSignal> cancellationSignal = new AtomicReference<>();
    private ReprintModule module;
    private Context context;

    public void initialize(Context context, Reprint.Logger logger) {
        this.context = context.getApplicationContext();

        // The SPass module doesn't work below API 17, and the Imprint module obviously requires
        // Marshmallow.
        if (module != null) return;

        if (logger == null) logger = ReprintInternal.NULL_LOGGER;

        // Only use the Spass module on APIs that don't support Imprint.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            registerSpassModule(context, logger);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            MarshmallowReprintModule marshmallowModule = new MarshmallowReprintModule(context, logger);

            // Some phones like the Galaxy S5 run marshmallow, but only work with Spass
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M && !marshmallowModule.isHardwarePresent()) {
                registerSpassModule(context, logger);
            } else {
                registerModule(marshmallowModule);
            }
        }
    }

    private void registerSpassModule(Context context, Reprint.Logger logger) {
        try {
            final Class<?> spassModuleClass = Class.forName(REPRINT_SPASS_MODULE);
            final Constructor<?> constructor = spassModuleClass.getConstructor(Context.class, Reprint.Logger.class);
            ReprintModule module = (ReprintModule) constructor.newInstance(context, logger);
            registerModule(module);
        } catch (Exception ignored) {
        }
    }

    public void registerModule(ReprintModule module) {
        if (module == null || this.module != null && module.tag() == this.module.tag()) {
            return;
        }

        if (module.isHardwarePresent()) {
            this.module = module;
        }
    }

    public boolean isHardwarePresent() {
        return module != null && module.isHardwarePresent();
    }

    public boolean hasFingerprintRegistered() {
        return module != null && module.hasFingerprintRegistered();
    }

    /**
     * Start an authentication request.
     *
     * @param listener         The listener to be notified.
     * @param restartPredicate The predicate that determines whether to restart or not.
     */
    public void authenticate(final AuthenticationListener listener, Reprint.RestartPredicate restartPredicate) {
        if (module == null || !module.isHardwarePresent()) {
            listener.onFailure(AuthenticationFailureReason.NO_HARDWARE, true,
                    getString(R.string.fingerprint_error_hw_not_available), 0, 0);
            return;
        }

        if (!module.hasFingerprintRegistered()) {
            listener.onFailure(AuthenticationFailureReason.NO_FINGERPRINTS_REGISTERED, true,
                    "Not recognized", 0, 0);
            return;
        }

        cancellationSignal.set(new CancellationSignal());
        module.authenticate(cancellationSignal.get(), listener, restartPredicate);
    }

    public void cancelAuthentication() {
        final androidx.core.os.CancellationSignal signal = cancellationSignal.getAndSet(null);
        if (signal != null) {
            try {
                signal.cancel();
            } catch (NullPointerException e) {
                // Occasionally the cancel call throws an NPE when trying to unparcelize something.
            }
        }
    }

    private String getString(int resid) {
        return context == null ? null : context.getString(resid);
    }
}
