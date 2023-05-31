package ca.on.sudbury.hojat.smartgallery.dialogs

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.DialogFragment
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.activities.BaseSimpleActivity
import ca.on.sudbury.hojat.smartgallery.databinding.DialogFragmentRadioGroupBinding
import ca.on.sudbury.hojat.smartgallery.extensions.baseConfig
import ca.on.sudbury.hojat.smartgallery.extensions.getBasePath
import ca.on.sudbury.hojat.smartgallery.extensions.hasOTGConnected
import ca.on.sudbury.hojat.smartgallery.extensions.internalStoragePath

/**
 * A dialog for choosing between internal, root, SD card (optional) storage.
 * In the main page of the app click on 3 dots icon, from the dropdown menu
 * click on "Create new folder". From the resulting dialog click on the button
 * on top of dialog (which usually shows "Internal"). The resulting dialog is
 * created via this class.
 *
 * In this specific class, since it's creating the UI in a very awful way, the
 * listeners are not registered in a separate callback like my other fragments.
 *
 * Has to have access to activity to avoid some Theme.AppCompat issues.
 * @param currPath current path to decide which storage should be preselected.
 * @param pickSingleOption if only one option like "Internal" is available, select it automatically.
 * @param callback an anonymous function.
 *
 */
class StoragePickerDialogFragment(
    private val currPath: String,
    private val showRoot: Boolean,
    private val pickSingleOption: Boolean,
    val callback: (pickedPath: String) -> Unit
) : DialogFragment() {

    // the binding
    private var _binding: DialogFragmentRadioGroupBinding? = null
    private val binding get() = _binding!!

    // needed for drawing the UI
    private val idInternal = 1
    private val idSd = 2
    private val idOtg = 3
    private val idRoot = 4
    private var defaultSelectedId = 0
    private val availableStorages = ArrayList<String>()


    /**
     * Create the UI of the dialog
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFragmentRadioGroupBinding.inflate(
            inflater,
            container,
            false
        )
        loadDialogUI()
        return binding.root
    }

    private fun loadDialogUI() {
        availableStorages.add(requireActivity().internalStoragePath)
        when {
            hasExternalSDCard(requireActivity()) -> availableStorages.add(requireActivity().baseConfig.sdCardPath)
            requireActivity().hasOTGConnected() -> availableStorages.add("otg")
            showRoot -> availableStorages.add("root")
        }

        if (pickSingleOption && availableStorages.size == 1) {
            callback(availableStorages.first())
        } else {
            initRadioGroup()
        }

    }

    @SuppressLint("InflateParams", "UseGetLayoutInflater")
    private fun initRadioGroup() {
        val inflater = LayoutInflater.from(activity)
        val layoutParams = RadioGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val basePath = currPath.getBasePath(requireActivity())
        val internalButton = inflater.inflate(R.layout.radio_button, null) as RadioButton
        internalButton.apply {
            id = idInternal
            text = resources.getString(R.string.internal)
            isChecked = basePath == context.internalStoragePath
            setOnClickListener { internalPicked() }
            if (isChecked) {
                defaultSelectedId = id
            }
        }
        binding.dialogRadioGroup.addView(internalButton, layoutParams)
        if (hasExternalSDCard(requireActivity())) {
            val sdButton = inflater.inflate(R.layout.radio_button, null) as RadioButton
            sdButton.apply {
                id = idSd
                text = resources.getString(R.string.sd_card)
                isChecked = basePath == context.baseConfig.sdCardPath
                setOnClickListener { sdPicked() }
                if (isChecked) {
                    defaultSelectedId = id
                }
            }
            binding.dialogRadioGroup.addView(sdButton, layoutParams)
        }
        if (requireActivity().hasOTGConnected()) {
            val otgButton = inflater.inflate(R.layout.radio_button, null) as RadioButton
            otgButton.apply {
                id = idOtg
                text = resources.getString(R.string.usb)
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
                text = resources.getString(R.string.root)
                isChecked = basePath == "/"
                setOnClickListener { rootPicked() }
                if (isChecked) {
                    defaultSelectedId = id
                }
            }
            binding.dialogRadioGroup.addView(rootButton, layoutParams)
        }

    }

    /**
     * Cleaning the stuff
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun hasExternalSDCard(owner: Context) = owner.baseConfig.sdCardPath.isNotEmpty()

    private fun internalPicked() {
        dismiss()
        callback(requireActivity().internalStoragePath)
    }

    private fun rootPicked() {
        dismiss()
        callback("/")
    }

    private fun otgPicked() {
        (requireActivity() as BaseSimpleActivity).handleOTGPermission {
            if (it) {
                callback(requireActivity().baseConfig.otgPath)
                dismiss()
            } else {
                binding.dialogRadioGroup.check(defaultSelectedId)
            }
        }
    }

    private fun sdPicked() {
        dismiss()
        callback(requireActivity().baseConfig.sdCardPath)
    }

    companion object {
        const val TAG = "StoragePickerDialogFragment"
    }
}