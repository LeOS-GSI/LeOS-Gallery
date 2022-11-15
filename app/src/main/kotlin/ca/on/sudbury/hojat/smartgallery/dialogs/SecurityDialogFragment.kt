package ca.on.sudbury.hojat.smartgallery.dialogs

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.biometric.BiometricManager
import androidx.biometric.auth.AuthPromptHost
import androidx.fragment.app.DialogFragment
import androidx.viewpager.widget.ViewPager
import ca.on.hojat.fingerprint.core.Reprint
import ca.on.hojat.palette.views.MyDialogViewPager
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.adapters.PasswordTypesAdapter
import ca.on.sudbury.hojat.smartgallery.databinding.DialogFragmentSecurityBinding
import ca.on.sudbury.hojat.smartgallery.extensions.baseConfig
import ca.on.sudbury.hojat.smartgallery.extensions.getProperBackgroundColor
import ca.on.sudbury.hojat.smartgallery.extensions.getProperPrimaryColor
import ca.on.sudbury.hojat.smartgallery.extensions.getProperTextColor
import ca.on.sudbury.hojat.smartgallery.extensions.onGlobalLayout
import ca.on.sudbury.hojat.smartgallery.extensions.onTabSelectionChanged
import ca.on.sudbury.hojat.smartgallery.helpers.ProtectionType
import ca.on.sudbury.hojat.smartgallery.helpers.SHOW_ALL_TABS
import ca.on.sudbury.hojat.smartgallery.interfaces.HashListener
import ca.on.sudbury.hojat.smartgallery.usecases.IsMarshmallowPlusUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.IsRPlusUseCase

/**
 * In the settings page, there's a section named "Security" with 3 check boxes.
 * Any of the dialogs that you'll see after clicking on those checkboxes are created
 * by this DialogFragment.
 *
 */
class SecurityDialogFragment(
    private val requiredHash: String,
    private val showTabIndex: Int,
    private val callback: (hash: String, type: Int, success: Boolean) -> Unit
) : DialogFragment(), HashListener {

    // the binding
    private var _binding: DialogFragmentSecurityBinding? = null
    private val binding get() = _binding!!

    // configuration
    private lateinit var tabsAdapter: PasswordTypesAdapter
    private lateinit var viewPager: MyDialogViewPager

    /**
     * Create the UI of the dialog
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // load the binding
        _binding = DialogFragmentSecurityBinding.inflate(inflater, container, false)

        loadDialogUi()
        return binding.root
    }

    /**
     * Clean all used resources.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        callback("", 0, false)
    }

    /**
     * I have no idea why we're using this interface and what it's been used for.
     */
    override fun receivedHash(hash: String, type: Int) {
        callback(hash, type, true)
        if (!requireActivity().isFinishing) {
            dismiss()
        }

    }

    private fun loadDialogUi() {
        binding.apply {
            viewPager = binding.dialogTabViewPager
            viewPager.offscreenPageLimit = 2
            tabsAdapter = PasswordTypesAdapter(
                context = root.context,
                requiredHash = requiredHash,
                hashListener = this@SecurityDialogFragment,
                scrollView = dialogScrollview,
                biometricPromptHost = AuthPromptHost(requireActivity()),
                showBiometricIdTab = shouldShowBiometricIdTab(),
                showBiometricAuthentication = showTabIndex == ProtectionType.FingerPrint.id && IsRPlusUseCase()
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
                        ProtectionType.FingerPrint.id
                    )
                }

                if (requireActivity().baseConfig.isUsingSystemTheme) {
                    dialogTabLayout.setBackgroundColor(requireActivity().resources.getColor(R.color.you_dialog_background_color))
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
                        ) -> ProtectionType.Pattern.id
                        it.text.toString()
                            .equals(
                                root.resources.getString(R.string.pin),
                                true
                            ) -> ProtectionType.Pin.id
                        else -> ProtectionType.FingerPrint.id
                    }
                    updateTabVisibility()
                })
            } else {
                dialogTabLayout.visibility = View.GONE
                viewPager.currentItem = showTabIndex
                viewPager.allowSwiping = false
            }
        }
    }

    private fun shouldShowBiometricIdTab() = if (IsRPlusUseCase()) {
        isBiometricIdAvailable(requireActivity())
    } else {
        isFingerPrintSensorAvailable()
    }


    @SuppressLint("WrongConstant")
    private fun isBiometricIdAvailable(owner: Context): Boolean =
        when (BiometricManager.from(owner).canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_WEAK
        )) {
            BiometricManager.BIOMETRIC_SUCCESS, BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> true
            else -> false
        }

    private fun isFingerPrintSensorAvailable() =
        IsMarshmallowPlusUseCase() && Reprint.isHardwarePresent()

    private fun updateTabVisibility() {
        for (i in 0..2) {
            tabsAdapter.isTabVisible(i, viewPager.currentItem == i)
        }
    }
}