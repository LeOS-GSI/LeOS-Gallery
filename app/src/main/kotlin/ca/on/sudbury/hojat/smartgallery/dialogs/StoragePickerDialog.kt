package ca.on.sudbury.hojat.smartgallery.dialogs

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import ca.on.sudbury.hojat.smartgallery.extensions.getAlertDialogBuilder
import ca.on.sudbury.hojat.smartgallery.extensions.getBasePath
import ca.on.sudbury.hojat.smartgallery.extensions.hasOTGConnected
import ca.on.sudbury.hojat.smartgallery.extensions.internalStoragePath
import ca.on.sudbury.hojat.smartgallery.extensions.setupDialogStuff
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.activities.BaseSimpleActivity
import ca.on.sudbury.hojat.smartgallery.databinding.DialogRadioGroupBinding
import ca.on.sudbury.hojat.smartgallery.extensions.baseConfig
import timber.log.Timber

/**
 * A dialog for choosing between internal, root, SD card (optional) storage.
 * In the main page of the app click on 3 dots icon, from the dropdown menu
 * click on "Create new folder". From the resulting dialog click on the button
 * on top of dialog (which usually shows "Internal"). The resulting dialog is
 * created via this class.
 *
 * @param activity has to be activity to avoid some Theme.AppCompat issues
 * @param currPath current path to decide which storage should be preselected
 * @param pickSingleOption if only one option like "Internal" is available, select it automatically
 * @param callback an anonymous function
 *
 */
class StoragePickerDialog(
    val activity: BaseSimpleActivity,
    private val currPath: String,
    private val showRoot: Boolean,
    pickSingleOption: Boolean,
    val callback: (pickedPath: String) -> Unit
) {
    private val idInternal = 1
    private val idSd = 2
    private val idOtg = 3
    private val idRoot = 4

    private lateinit var radioGroup: RadioGroup
    private var dialog: AlertDialog? = null
    private var defaultSelectedId = 0
    private val availableStorages = ArrayList<String>()

    init {
        Timber.d("Hojat Ghasemi : StoragePickerDialog was called")
        availableStorages.add(activity.internalStoragePath)
        when {
            hasExternalSDCard(activity) -> availableStorages.add(activity.baseConfig.sdCardPath)
            activity.hasOTGConnected() -> availableStorages.add("otg")
            showRoot -> availableStorages.add("root")
        }

        if (pickSingleOption && availableStorages.size == 1) {
            callback(availableStorages.first())
        } else {
            initDialog()
        }
    }

    @SuppressLint("InflateParams")
    private fun initDialog() {
        val inflater = LayoutInflater.from(activity)
        val layoutParams = RadioGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val binding = DialogRadioGroupBinding.inflate(inflater)
        radioGroup = binding.dialogRadioGroup
        val basePath = currPath.getBasePath(activity)

        val internalButton = inflater.inflate(R.layout.radio_button, null) as RadioButton
        internalButton.apply {
            id = idInternal
            text = activity.resources.getString(R.string.internal)
            isChecked = basePath == context.internalStoragePath
            setOnClickListener { internalPicked() }
            if (isChecked) {
                defaultSelectedId = id
            }
        }
        binding.dialogRadioGroup.addView(internalButton, layoutParams)

        if (hasExternalSDCard(activity)) {
            val sdButton = inflater.inflate(R.layout.radio_button, null) as RadioButton
            sdButton.apply {
                id = idSd
                text = activity.resources.getString(R.string.sd_card)
                isChecked = basePath == context.baseConfig.sdCardPath
                setOnClickListener { sdPicked() }
                if (isChecked) {
                    defaultSelectedId = id
                }
            }
            binding.dialogRadioGroup.addView(sdButton, layoutParams)
        }

        if (activity.hasOTGConnected()) {
            val otgButton = inflater.inflate(R.layout.radio_button, null) as RadioButton
            otgButton.apply {
                id = idOtg
                text = activity.resources.getString(R.string.usb)
                isChecked = basePath == context.baseConfig.otgPath
                setOnClickListener { otgPicked() }
                if (isChecked) {
                    defaultSelectedId = id
                }
            }
            binding.dialogRadioGroup.addView(otgButton, layoutParams)
        }

        // allow for example excluding the root folder at the gallery
        if (showRoot) {
            val rootButton = inflater.inflate(R.layout.radio_button, null) as RadioButton
            rootButton.apply {
                id = idRoot
                text = activity.resources.getString(R.string.root)
                isChecked = basePath == "/"
                setOnClickListener { rootPicked() }
                if (isChecked) {
                    defaultSelectedId = id
                }
            }
            binding.dialogRadioGroup.addView(rootButton, layoutParams)
        }

        activity.getAlertDialogBuilder().apply {
            activity.setupDialogStuff(binding.root, this, R.string.select_storage) { alertDialog ->
                dialog = alertDialog
            }
        }
    }

    private fun internalPicked() {
        dialog?.dismiss()
        callback(activity.internalStoragePath)
    }

    private fun sdPicked() {
        dialog?.dismiss()
        callback(activity.baseConfig.sdCardPath)
    }

    private fun otgPicked() {
        activity.handleOTGPermission {
            if (it) {
                callback(activity.baseConfig.otgPath)
                dialog?.dismiss()
            } else {
                radioGroup.check(defaultSelectedId)
            }
        }
    }

    private fun rootPicked() {
        dialog?.dismiss()
        callback("/")
    }

    private fun hasExternalSDCard(owner: Context) = owner.baseConfig.sdCardPath.isNotEmpty()

}
