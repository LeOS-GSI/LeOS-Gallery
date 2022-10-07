package ca.on.sudbury.hojat.smartgallery.dialogs

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.biometric.auth.AuthPromptHost
import androidx.fragment.app.FragmentActivity
import ca.on.sudbury.hojat.smartgallery.extensions.baseConfig
import ca.on.sudbury.hojat.smartgallery.extensions.beGone
import ca.on.sudbury.hojat.smartgallery.extensions.getAlertDialogBuilder
import ca.on.sudbury.hojat.smartgallery.extensions.getProperBackgroundColor
import ca.on.sudbury.hojat.smartgallery.extensions.getProperPrimaryColor
import ca.on.sudbury.hojat.smartgallery.extensions.getProperTextColor
import ca.on.sudbury.hojat.smartgallery.extensions.isFingerPrintSensorAvailable
import ca.on.sudbury.hojat.smartgallery.extensions.onGlobalLayout
import ca.on.sudbury.hojat.smartgallery.extensions.setupDialogStuff
import ca.on.sudbury.hojat.smartgallery.helpers.PROTECTION_FINGERPRINT
import ca.on.sudbury.hojat.smartgallery.helpers.PROTECTION_PATTERN
import ca.on.sudbury.hojat.smartgallery.helpers.PROTECTION_PIN
import ca.on.sudbury.hojat.smartgallery.helpers.SHOW_ALL_TABS
import ca.on.sudbury.hojat.smartgallery.helpers.isRPlus
import com.simplemobiletools.commons.R
import ca.on.sudbury.hojat.smartgallery.adapters.PasswordTypesAdapter
import com.simplemobiletools.commons.extensions.isBiometricIdAvailable
import com.simplemobiletools.commons.extensions.onPageChangeListener
import com.simplemobiletools.commons.extensions.onTabSelectionChanged
import ca.on.sudbury.hojat.smartgallery.interfaces.HashListener
import com.simplemobiletools.commons.views.MyDialogViewPager
import kotlinx.android.synthetic.main.dialog_security.view.*

class SecurityDialog(
    private val activity: Activity,
    private val requiredHash: String,
    private val showTabIndex: Int,
    private val callback: (hash: String, type: Int, success: Boolean) -> Unit
) : HashListener {
    private var dialog: AlertDialog? = null

    @SuppressLint("InflateParams")
    private val view = LayoutInflater.from(activity).inflate(R.layout.dialog_security, null)
    private var tabsAdapter: PasswordTypesAdapter
    private var viewPager: MyDialogViewPager

    init {
        view.apply {
            viewPager = findViewById(R.id.dialog_tab_view_pager)
            viewPager.offscreenPageLimit = 2
            tabsAdapter = PasswordTypesAdapter(
                context = context,
                requiredHash = requiredHash,
                hashListener = this@SecurityDialog,
                scrollView = dialog_scrollview,
                biometricPromptHost = AuthPromptHost(activity as FragmentActivity),
                showBiometricIdTab = shouldShowBiometricIdTab(),
                showBiometricAuthentication = showTabIndex == PROTECTION_FINGERPRINT && isRPlus()
            )
            viewPager.adapter = tabsAdapter
            viewPager.onPageChangeListener {
                dialog_tab_layout.getTabAt(it)?.select()
            }

            viewPager.onGlobalLayout {
                updateTabVisibility()
            }

            if (showTabIndex == SHOW_ALL_TABS) {
                val textColor = context.getProperTextColor()

                if (shouldShowBiometricIdTab()) {
                    val tabTitle = if (isRPlus()) R.string.biometrics else R.string.fingerprint
                    dialog_tab_layout.addTab(
                        dialog_tab_layout.newTab().setText(tabTitle),
                        PROTECTION_FINGERPRINT
                    )
                }

                if (activity.baseConfig.isUsingSystemTheme) {
                    dialog_tab_layout.setBackgroundColor(activity.resources.getColor(R.color.you_dialog_background_color))
                } else {
                    dialog_tab_layout.setBackgroundColor(context.getProperBackgroundColor())
                }

                dialog_tab_layout.setTabTextColors(textColor, textColor)
                dialog_tab_layout.setSelectedTabIndicatorColor(context.getProperPrimaryColor())
                dialog_tab_layout.onTabSelectionChanged(tabSelectedAction = {
                    viewPager.currentItem = when {
                        it.text.toString().equals(
                            resources.getString(R.string.pattern),
                            true
                        ) -> PROTECTION_PATTERN
                        it.text.toString()
                            .equals(resources.getString(R.string.pin), true) -> PROTECTION_PIN
                        else -> PROTECTION_FINGERPRINT
                    }
                    updateTabVisibility()
                })
            } else {
                dialog_tab_layout.beGone()
                viewPager.currentItem = showTabIndex
                viewPager.allowSwiping = false
            }
        }

        activity.getAlertDialogBuilder()
            .setOnCancelListener { onCancelFail() }
            .setNegativeButton(R.string.cancel) { _, _ -> onCancelFail() }
            .apply {
                activity.setupDialogStuff(view, this) { alertDialog ->
                    dialog = alertDialog
                }
            }
    }

    private fun onCancelFail() {
        callback("", 0, false)
        dialog?.dismiss()
    }

    override fun receivedHash(hash: String, type: Int) {
        callback(hash, type, true)
        if (!activity.isFinishing) {
            dialog?.dismiss()
        }
    }

    private fun updateTabVisibility() {
        for (i in 0..2) {
            tabsAdapter.isTabVisible(i, viewPager.currentItem == i)
        }
    }

    private fun shouldShowBiometricIdTab(): Boolean {
        return if (isRPlus()) {
            activity.isBiometricIdAvailable()
        } else {
            activity.isFingerPrintSensorAvailable()
        }
    }
}
