package ca.on.sudbury.hojat.smartgallery.dialogs

import android.app.Activity
import android.text.Html
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.activities.BaseSimpleActivity
import ca.on.sudbury.hojat.smartgallery.databinding.DialogWritePermissionBinding
import ca.on.sudbury.hojat.smartgallery.databinding.DialogWritePermissionOtgBinding
import ca.on.sudbury.hojat.smartgallery.extensions.getAlertDialogBuilder
import ca.on.sudbury.hojat.smartgallery.extensions.humanizePath
import ca.on.sudbury.hojat.smartgallery.extensions.setupDialogStuff
import timber.log.Timber

class WritePermissionDialog(activity: Activity, val mode: Mode, val callback: () -> Unit) {
    sealed class Mode {
        object Otg : Mode()
        object SdCard : Mode()
        data class OpenDocumentTreeSDK30(val path: String) : Mode()
        object CreateDocumentSDK30 : Mode()
    }

    private var dialog: AlertDialog? = null

    // we create the binding by referencing the owner Activity
    private var dialogWritePermissionBinding =
        DialogWritePermissionBinding.inflate(activity.layoutInflater)
    private var dialogWritePermissionOtgBinding =
        DialogWritePermissionOtgBinding.inflate(activity.layoutInflater)

    init {
        Timber.d("Hojat Ghasemi : WritePermissionDialog was called")
        val binding =
            if (mode == Mode.SdCard) dialogWritePermissionBinding else dialogWritePermissionOtgBinding
        var dialogTitle = R.string.confirm_storage_access_title

        val glide = Glide.with(activity)
        val crossFade = DrawableTransitionOptions.withCrossFade()
        when (mode) {
            Mode.Otg -> {
                dialogWritePermissionOtgBinding.writePermissionsDialogOtgText.setText(R.string.confirm_usb_storage_access_text)
                glide.load(R.drawable.img_write_storage_otg).transition(crossFade)
                    .into(dialogWritePermissionOtgBinding.writePermissionsDialogOtgImage)
            }
            Mode.SdCard -> {
                glide.load(R.drawable.img_write_storage).transition(crossFade)
                    .into(dialogWritePermissionBinding.writePermissionsDialogImage)
                glide.load(R.drawable.img_write_storage_sd).transition(crossFade)
                    .into(dialogWritePermissionBinding.writePermissionsDialogImageSd)
            }
            is Mode.OpenDocumentTreeSDK30 -> {
                dialogTitle = R.string.confirm_folder_access_title
                val humanizedPath = activity.humanizePath(mode.path)
                dialogWritePermissionOtgBinding.writePermissionsDialogOtgText.text =
                    Html.fromHtml(
                        activity.getString(
                            R.string.confirm_storage_access_android_text_specific,
                            humanizedPath
                        )
                    )
                glide.load(R.drawable.img_write_storage_sdk_30).transition(crossFade)
                    .into(dialogWritePermissionOtgBinding.writePermissionsDialogOtgImage)

                dialogWritePermissionOtgBinding.writePermissionsDialogOtgImage.setOnClickListener {
                    dialogConfirmed()
                }
            }
            Mode.CreateDocumentSDK30 -> {
                dialogTitle = R.string.confirm_folder_access_title
                dialogWritePermissionOtgBinding.writePermissionsDialogOtgText.text =
                    Html.fromHtml(activity.getString(R.string.confirm_create_doc_for_new_folder_text))
                glide.load(R.drawable.img_write_storage_create_doc_sdk_30).transition(crossFade)
                    .into(dialogWritePermissionOtgBinding.writePermissionsDialogOtgImage)

                dialogWritePermissionOtgBinding.writePermissionsDialogOtgImage.setOnClickListener {
                    dialogConfirmed()
                }
            }
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok) { _, _ -> dialogConfirmed() }
            .setOnCancelListener {
                BaseSimpleActivity.funAfterSAFPermission?.invoke(false)
                BaseSimpleActivity.funAfterSAFPermission = null
            }
            .apply {
                activity.setupDialogStuff(binding.root, this, dialogTitle) { alertDialog ->
                    dialog = alertDialog
                }
            }
    }

    private fun dialogConfirmed() {
        dialog?.dismiss()
        callback()
    }
}
