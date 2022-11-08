package ca.on.sudbury.hojat.smartgallery.views

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import androidx.biometric.auth.AuthPromptHost
import ca.on.hojat.palette.views.MyScrollView
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.extensions.getProperTextColor
import ca.on.sudbury.hojat.smartgallery.extensions.updateTextColors
import ca.on.sudbury.hojat.smartgallery.helpers.ProtectionType
import ca.on.sudbury.hojat.smartgallery.interfaces.HashListener
import ca.on.sudbury.hojat.smartgallery.interfaces.SecurityTab
import ca.on.sudbury.hojat.smartgallery.usecases.ApplyColorFilterUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.PerformHapticFeedbackUseCase
import kotlinx.android.synthetic.main.tab_pin.view.*
import timber.log.Timber
import java.math.BigInteger
import java.security.MessageDigest
import java.util.Locale

class PinTab(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs), SecurityTab {
    private var hash = ""
    private var requiredHash = ""
    private var pin = ""
    private lateinit var hashListener: HashListener

    override fun onFinishInflate() {
        super.onFinishInflate()
        context.updateTextColors(pin_lock_holder)

        pin_0.setOnClickListener { addNumber("0") }
        pin_1.setOnClickListener { addNumber("1") }
        pin_2.setOnClickListener { addNumber("2") }
        pin_3.setOnClickListener { addNumber("3") }
        pin_4.setOnClickListener { addNumber("4") }
        pin_5.setOnClickListener { addNumber("5") }
        pin_6.setOnClickListener { addNumber("6") }
        pin_7.setOnClickListener { addNumber("7") }
        pin_8.setOnClickListener { addNumber("8") }
        pin_9.setOnClickListener { addNumber("9") }
        pin_c.setOnClickListener { clear() }
        pin_ok.setOnClickListener { confirmPIN() }
        ApplyColorFilterUseCase(pin_ok, context.getProperTextColor())
    }

    override fun initTab(
        requiredHash: String,
        listener: HashListener,
        scrollView: MyScrollView,
        biometricPromptHost: AuthPromptHost,
        showBiometricAuthentication: Boolean
    ) {
        this.requiredHash = requiredHash
        hash = requiredHash
        hashListener = listener
    }

    private fun addNumber(number: String) {
        if (pin.length < 10) {
            pin += number
            updatePinCode()
        }
        PerformHapticFeedbackUseCase(this)
    }

    private fun clear() {
        if (pin.isNotEmpty()) {
            pin = pin.substring(0, pin.length - 1)
            updatePinCode()
        }
        PerformHapticFeedbackUseCase(this)
    }

    private fun confirmPIN() {
        val newHash = getHashedPin()
        if (pin.isEmpty()) {
            Timber.d("Please enter pin")
        } else if (hash.isEmpty()) {
            hash = newHash
            resetPin()
            pin_lock_title.setText(R.string.repeat_pin)
        } else if (hash == newHash) {
            hashListener.receivedHash(hash, ProtectionType.Pin.id)
        } else {
            resetPin()
            Timber.d("wrong pin entered")
            if (requiredHash.isEmpty()) {
                hash = ""
                pin_lock_title.setText(R.string.enter_pin)
            }
        }
        PerformHapticFeedbackUseCase(this)
    }

    private fun resetPin() {
        pin = ""
        pin_lock_current_pin.text = ""
    }

    private fun updatePinCode() {
        pin_lock_current_pin.text = "*".repeat(pin.length)
        if (hash.isNotEmpty() && hash == getHashedPin()) {
            hashListener.receivedHash(hash, ProtectionType.Pin.id)
        }
    }

    private fun getHashedPin(): String {
        val messageDigest = MessageDigest.getInstance("SHA-1")
        messageDigest.update(pin.toByteArray(charset("UTF-8")))
        val digest = messageDigest.digest()
        val bigInteger = BigInteger(1, digest)
        return String.format(Locale.getDefault(), "%0${digest.size * 2}x", bigInteger)
            .lowercase(Locale.getDefault())
    }

    override fun visibilityChanged(isVisible: Boolean) {}
}
