package ca.on.sudbury.hojat.smartgallery.views

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.provider.Settings
import android.util.AttributeSet
import android.widget.RelativeLayout
import androidx.biometric.auth.AuthPromptHost
import ca.on.hojat.fingerprint.core.AuthenticationFailureReason
import ca.on.hojat.fingerprint.core.AuthenticationListener
import ca.on.hojat.fingerprint.core.Reprint
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.extensions.applyColorFilter
import ca.on.sudbury.hojat.smartgallery.extensions.beGoneIf
import ca.on.sudbury.hojat.smartgallery.extensions.getProperTextColor
import ca.on.sudbury.hojat.smartgallery.extensions.toast
import ca.on.sudbury.hojat.smartgallery.extensions.updateTextColors
import ca.on.sudbury.hojat.smartgallery.helpers.PROTECTION_FINGERPRINT
import ca.on.sudbury.hojat.smartgallery.interfaces.HashListener
import ca.on.sudbury.hojat.smartgallery.interfaces.SecurityTab
import kotlinx.android.synthetic.main.tab_fingerprint.view.*

class FingerprintTab(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs),
    SecurityTab {
    private val recheckPeriod = 3000L
    private val registerHandler = Handler()

    lateinit var hashListener: HashListener

    override fun onFinishInflate() {
        super.onFinishInflate()
        val textColor = context.getProperTextColor()
        context.updateTextColors(fingerprint_lock_holder)
        fingerprint_image.applyColorFilter(textColor)

        fingerprint_settings.setOnClickListener {
            context.startActivity(Intent(Settings.ACTION_SETTINGS))
        }
    }

    override fun initTab(
        requiredHash: String,
        listener: HashListener,
        scrollView: MyScrollView,
        biometricPromptHost: AuthPromptHost,
        showBiometricAuthentication: Boolean
    ) {
        hashListener = listener
    }

    override fun visibilityChanged(isVisible: Boolean) {
        if (isVisible) {
            checkRegisteredFingerprints()
        } else {
            Reprint.cancelAuthentication()
        }
    }

    private fun checkRegisteredFingerprints() {
        val hasFingerprints = Reprint.hasFingerprintRegistered()
        fingerprint_settings.beGoneIf(hasFingerprints)
        fingerprint_label.text =
            context.getString(if (hasFingerprints) R.string.place_finger else R.string.no_fingerprints_registered)

        Reprint.authenticate(object : AuthenticationListener {
            override fun onSuccess(moduleTag: Int) {
                hashListener.receivedHash("", PROTECTION_FINGERPRINT)
            }

            override fun onFailure(
                failureReason: AuthenticationFailureReason,
                fatal: Boolean,
                errorMessage: CharSequence?,
                moduleTag: Int,
                errorCode: Int
            ) {
                when (failureReason) {
                    AuthenticationFailureReason.AUTHENTICATION_FAILED -> context.toast(R.string.authentication_failed)
                    AuthenticationFailureReason.LOCKED_OUT -> context.toast(R.string.authentication_blocked)
                }
            }
        })

        registerHandler.postDelayed({
            checkRegisteredFingerprints()
        }, recheckPeriod)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        registerHandler.removeCallbacksAndMessages(null)
        Reprint.cancelAuthentication()
    }
}
