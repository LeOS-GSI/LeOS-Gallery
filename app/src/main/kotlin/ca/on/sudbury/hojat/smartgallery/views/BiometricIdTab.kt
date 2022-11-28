package ca.on.sudbury.hojat.smartgallery.views

import android.content.Context
import android.util.AttributeSet
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.biometric.auth.AuthPromptCallback
import androidx.biometric.auth.AuthPromptHost
import androidx.biometric.auth.Class2BiometricAuthPrompt
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import ca.on.hojat.palette.views.MyScrollView
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.extensions.updateTextColors
import ca.on.sudbury.hojat.smartgallery.helpers.ProtectionType
import ca.on.sudbury.hojat.smartgallery.interfaces.HashListener
import ca.on.sudbury.hojat.smartgallery.interfaces.SecurityTab
import kotlinx.android.synthetic.main.tab_biometric_id.view.*

class BiometricIdTab(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs),
    SecurityTab {
    private lateinit var hashListener: HashListener
    private lateinit var biometricPromptHost: AuthPromptHost

    override fun onFinishInflate() {
        super.onFinishInflate()
        context.updateTextColors(biometric_lock_holder)

        open_biometric_dialog.setOnClickListener {
            showBiometricPrompt(
                biometricPromptHost.activity!! as AppCompatActivity,
                successCallback = hashListener::receivedHash
            )
        }
    }

    override fun initTab(
        requiredHash: String,
        listener: HashListener,
        scrollView: MyScrollView,
        biometricPromptHost: AuthPromptHost,
        showBiometricAuthentication: Boolean
    ) {
        this.biometricPromptHost = biometricPromptHost
        hashListener = listener
        if (showBiometricAuthentication) {
            open_biometric_dialog.performClick()
        }
    }

    override fun visibilityChanged(isVisible: Boolean) {}

    private fun showBiometricPrompt(
        owner: AppCompatActivity,
        successCallback: ((String, Int) -> Unit)? = null,
        failureCallback: (() -> Unit)? = null
    ) {
        Class2BiometricAuthPrompt.Builder(
            owner.getText(R.string.authenticate),
            owner.getText(R.string.cancel)
        )
            .build()
            .startAuthentication(
                AuthPromptHost(owner as FragmentActivity),
                object : AuthPromptCallback() {
                    override fun onAuthenticationSucceeded(
                        activity: FragmentActivity?,
                        result: BiometricPrompt.AuthenticationResult
                    ) {
                        successCallback?.invoke("", ProtectionType.FingerPrint.id)
                    }

                    override fun onAuthenticationError(
                        activity: FragmentActivity?,
                        errorCode: Int,
                        errString: CharSequence
                    ) {
                        val isCanceledByUser =
                            errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON || errorCode == BiometricPrompt.ERROR_USER_CANCELED
                        if (!isCanceledByUser) {
                            Toast.makeText(owner, errString.toString(), Toast.LENGTH_LONG).show()
                        }
                        failureCallback?.invoke()
                    }

                    override fun onAuthenticationFailed(activity: FragmentActivity?) {
                        Toast.makeText(
                            owner,
                            R.string.authentication_failed, Toast.LENGTH_LONG
                        ).show()
                        failureCallback?.invoke()
                    }
                }
            )
    }
}
