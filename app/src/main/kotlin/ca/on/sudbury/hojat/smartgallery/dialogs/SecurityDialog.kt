package ca.on.sudbury.hojat.smartgallery.dialogs

import android.app.Activity
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.biometric.auth.AuthPromptHost
import androidx.fragment.app.FragmentActivity
import androidx.viewpager.widget.ViewPager
import ca.on.hojat.fingerprint.core.Reprint
import ca.on.sudbury.hojat.smartgallery.extensions.baseConfig
import ca.on.sudbury.hojat.smartgallery.extensions.getAlertDialogBuilder
import ca.on.sudbury.hojat.smartgallery.extensions.getProperBackgroundColor
import ca.on.sudbury.hojat.smartgallery.extensions.getProperPrimaryColor
import ca.on.sudbury.hojat.smartgallery.extensions.getProperTextColor
import ca.on.sudbury.hojat.smartgallery.extensions.onGlobalLayout
import ca.on.sudbury.hojat.smartgallery.extensions.setupDialogStuff
import ca.on.sudbury.hojat.smartgallery.helpers.PROTECTION_FINGERPRINT
import ca.on.sudbury.hojat.smartgallery.helpers.PROTECTION_PATTERN
import ca.on.sudbury.hojat.smartgallery.helpers.PROTECTION_PIN
import ca.on.sudbury.hojat.smartgallery.helpers.SHOW_ALL_TABS
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.adapters.PasswordTypesAdapter
import ca.on.sudbury.hojat.smartgallery.databinding.DialogSecurityBinding
import ca.on.sudbury.hojat.smartgallery.extensions.isBiometricIdAvailable
import ca.on.sudbury.hojat.smartgallery.extensions.onTabSelectionChanged
import ca.on.sudbury.hojat.smartgallery.interfaces.HashListener
import ca.on.hojat.palette.views.MyDialogViewPager
import ca.on.sudbury.hojat.smartgallery.photoedit.usecases.IsMarshmallowPlusUseCase
import ca.on.sudbury.hojat.smartgallery.photoedit.usecases.IsRPlusUseCase

/**
 * In the settings page, there's section named "Security" with 3 check boxes.
 * Any of the dialogs that you'll see after clicking on those checkboxes,
 * are created by this class.
 *
 */
class SecurityDialog(
    private val activity: Activity,
    private val requiredHash: String,
    private val showTabIndex: Int,
    private val callback: (hash: String, type: Int, success: Boolean) -> Unit
) : HashListener {

    private var dialog: AlertDialog? = null
    private var tabsAdapter: PasswordTypesAdapter
    private var viewPager: MyDialogViewPager

    init {
        val binding = DialogSecurityBinding.inflate(activity.layoutInflater)
        binding.apply {
            viewPager = binding.dialogTabViewPager
            viewPager.offscreenPageLimit = 2
            tabsAdapter = PasswordTypesAdapter(
                context = root.context,
                requiredHash = requiredHash,
                hashListener = this@SecurityDialog,
                scrollView = dialogScrollview,
                biometricPromptHost = AuthPromptHost(activity as FragmentActivity),
                showBiometricIdTab = shouldShowBiometricIdTab(),
                showBiometricAuthentication = showTabIndex == PROTECTION_FINGERPRINT && IsRPlusUseCase()
            )
            viewPager.adapter = tabsAdapter

            viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                }

                override fun onPageSelected(position: Int) {
                    dialogTabLayout.getTabAt(position)?.select()
                }

                override fun onPageScrollStateChanged(state: Int) {

                }
            })

            viewPager.onGlobalLayout {
                updateTabVisibility()
            }

            if (showTabIndex == SHOW_ALL_TABS) {
                val textColor = root.context.getProperTextColor()

                if (shouldShowBiometricIdTab()) {
                    val tabTitle =
                        if (IsRPlusUseCase()) R.string.biometrics else R.string.fingerprint
                    dialogTabLayout.addTab(
                        dialogTabLayout.newTab().setText(tabTitle),
                        PROTECTION_FINGERPRINT
                    )
                }

                if (activity.baseConfig.isUsingSystemTheme) {
                    dialogTabLayout.setBackgroundColor(activity.resources.getColor(R.color.you_dialog_background_color))
                } else {
                    dialogTabLayout.setBackgroundColor(root.context.getProperBackgroundColor())
                }

                dialogTabLayout.setTabTextColors(textColor, textColor)
                dialogTabLayout.setSelectedTabIndicatorColor(root.context.getProperPrimaryColor())
                dialogTabLayout.onTabSelectionChanged(tabSelectedAction = {
                    viewPager.currentItem = when {
                        it.text.toString().equals(
                            root.resources.getString(R.string.pattern),
                            true
                        ) -> PROTECTION_PATTERN
                        it.text.toString()
                            .equals(root.resources.getString(R.string.pin), true) -> PROTECTION_PIN
                        else -> PROTECTION_FINGERPRINT
                    }
                    updateTabVisibility()
                })
            } else {
                dialogTabLayout.visibility = View.GONE
                viewPager.currentItem = showTabIndex
                viewPager.allowSwiping = false
            }
        }
        activity.getAlertDialogBuilder()
            .setOnCancelListener { onCancelFail() }
            .setNegativeButton(R.string.cancel) { _, _ -> onCancelFail() }
            .apply {
                activity.setupDialogStuff(binding.root, this) { alertDialog ->
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
        return if (IsRPlusUseCase()) {
            activity.isBiometricIdAvailable()
        } else {
            isFingerPrintSensorAvailable()
        }
    }

    private fun isFingerPrintSensorAvailable() =
        IsMarshmallowPlusUseCase() && Reprint.isHardwarePresent()
}
