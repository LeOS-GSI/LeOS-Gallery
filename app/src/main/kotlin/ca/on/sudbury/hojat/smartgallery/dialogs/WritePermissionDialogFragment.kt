package ca.on.sudbury.hojat.smartgallery.dialogs

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.activities.BaseSimpleActivity
import ca.on.sudbury.hojat.smartgallery.databinding.DialogFragmentWritePermissionBinding
import ca.on.sudbury.hojat.smartgallery.databinding.DialogFragmentWritePermissionOtgBinding
import ca.on.sudbury.hojat.smartgallery.extensions.humanizePath
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

/**
 * if app doesn't have permission to write on SD card or OTG, this dialog will be shown to force
 * the user grant those permissions.
 */
class WritePermissionDialogFragment(
    val mode: Mode,
    val callback: () -> Unit
) : DialogFragment() {

    // all the bindings
    private var _writePermissionBinding: DialogFragmentWritePermissionBinding? = null
    private val writePermissionBinding get() = _writePermissionBinding!!
    private var _otgWritePermissionBinding: DialogFragmentWritePermissionOtgBinding? = null
    private val otgWritePermissionBinding get() = _otgWritePermissionBinding!!

    private lateinit var binding: ViewBinding

    /**
     * Create the UI of the dialog
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // load the bindings
        _writePermissionBinding =
            DialogFragmentWritePermissionBinding.inflate(inflater, container, false)
        _otgWritePermissionBinding =
            DialogFragmentWritePermissionOtgBinding.inflate(inflater, container, false)
        // UI is created from 1 of 2 different XML views
        binding =
            if (mode == Mode.SdCard) writePermissionBinding else otgWritePermissionBinding

        loadDialogUi()

        return binding.root
    }


    /**
     * Register listeners for views.
     *
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (mode == Mode.SdCard) {
            writePermissionBinding.bottomRow.btnOk.setOnClickListener {
                dialogConfirmed()
            }
            writePermissionBinding.bottomRow.btnCancel.setOnClickListener {
                permissionNotGranted()
            }
        } else {
            otgWritePermissionBinding.otgBottomRow.btnOk.setOnClickListener {
                dialogConfirmed()
            }
            otgWritePermissionBinding.otgBottomRow.btnCancel.setOnClickListener {
                permissionNotGranted()
            }
        }
    }

    /**
     * Cleaning the stuff.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _writePermissionBinding = null
        _otgWritePermissionBinding = null
    }

    private fun loadDialogUi() {
        val glide = Glide.with(requireActivity())
        val crossFade = DrawableTransitionOptions.withCrossFade()
        when (mode) {
            Mode.Otg -> {
                otgWritePermissionBinding.writePermissionsDialogOtgText.setText(R.string.confirm_usb_storage_access_text)
                glide.load(R.drawable.img_write_storage_otg).transition(crossFade)
                    .into(otgWritePermissionBinding.writePermissionsDialogOtgImage)
            }
            Mode.SdCard -> {
                glide.load(R.drawable.img_write_storage).transition(crossFade)
                    .into(writePermissionBinding.writePermissionsDialogImage)
                glide.load(R.drawable.img_write_storage_sd).transition(crossFade)
                    .into(writePermissionBinding.writePermissionsDialogImageSd)
            }
            is Mode.OpenDocumentTreeSDK30 -> {
                val humanizedPath = requireActivity().humanizePath(mode.path)
                otgWritePermissionBinding.writePermissionsDialogOtgText.text =
                    Html.fromHtml(
                        requireActivity().getString(
                            R.string.confirm_storage_access_android_text_specific,
                            humanizedPath
                        )
                    )
                glide.load(R.drawable.img_write_storage_sdk_30).transition(crossFade)
                    .into(otgWritePermissionBinding.writePermissionsDialogOtgImage)

                otgWritePermissionBinding.writePermissionsDialogOtgImage.setOnClickListener {
                    dialogConfirmed()
                }
            }
            Mode.CreateDocumentSDK30 -> {
                otgWritePermissionBinding.writePermissionsDialogOtgText.text =
                    Html.fromHtml(requireActivity().getString(R.string.confirm_create_doc_for_new_folder_text))
                glide.load(R.drawable.img_write_storage_create_doc_sdk_30).transition(crossFade)
                    .into(otgWritePermissionBinding.writePermissionsDialogOtgImage)

                otgWritePermissionBinding.writePermissionsDialogOtgImage.setOnClickListener {
                    dialogConfirmed()
                }
            }
        }
    }

    private fun dialogConfirmed() {
        dismiss()
        callback()
    }

    private fun permissionNotGranted() {
        BaseSimpleActivity.funAfterSAFPermission?.invoke(false)
        BaseSimpleActivity.funAfterSAFPermission = null
    }

    sealed class Mode {
        object Otg : Mode()
        object SdCard : Mode()
        data class OpenDocumentTreeSDK30(val path: String) : Mode()
        object CreateDocumentSDK30 : Mode()
    }

    companion object {
        const val TAG = "WritePermissionDialogFragment"
    }
}