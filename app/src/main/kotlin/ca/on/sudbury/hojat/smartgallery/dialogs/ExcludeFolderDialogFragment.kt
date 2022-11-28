package ca.on.sudbury.hojat.smartgallery.dialogs

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.DialogFragment
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.databinding.DialogFragmentExcludeFolderBinding
import ca.on.sudbury.hojat.smartgallery.extensions.config
import ca.on.sudbury.hojat.smartgallery.extensions.getBasePath
import ca.on.sudbury.hojat.smartgallery.usecases.BeVisibleOrGoneUseCase

/**
 * In main page, long click on a folder and from the context menu, choose "Exclude".
 * The resulting dialog is created by this class.
 */
class ExcludeFolderDialogFragment(
    private val selectedPaths: List<String>,
    val callbackAfterDialogConfirmed: () -> Unit
) : DialogFragment() {

    // the binding
    private var _binding: DialogFragmentExcludeFolderBinding? = null
    private val binding get() = _binding!!

    // some configuration
    private lateinit var alternativePaths: List<String>
    private var radioGroup: RadioGroup? = null

    /**
     * Create the UI of the dialog
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFragmentExcludeFolderBinding.inflate(inflater, container, false)

        // need to load this configuration first and then draw the UI
        alternativePaths = getAlternativePathsList()
        loadDialogUI()
        return binding.root
    }

    @SuppressLint("InflateParams")
    private fun loadDialogUI() {

        binding.apply {
            BeVisibleOrGoneUseCase(excludeFolderParent, alternativePaths.size > 1)

            radioGroup = excludeFolderRadioGroup
            BeVisibleOrGoneUseCase(excludeFolderRadioGroup, alternativePaths.size > 1)
        }
        alternativePaths.forEachIndexed { index, _ ->
            val radioButton = (requireActivity().layoutInflater.inflate(
                R.layout.radio_button,
                null
            ) as RadioButton).apply {
                text = alternativePaths[index]
                isChecked = index == 0
                id = index
            }
            radioGroup!!.addView(
                radioButton,
                RadioGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
        }


    }

    /**
     * Register listeners for views.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding.excludeFolderDialogBottomRow) {
            btnOk.setOnClickListener {
                dialogConfirmed()
                dismiss()
            }
            btnCancel.setOnClickListener { dismiss() }
        }
    }

    private fun dialogConfirmed() {
        val path =
            if (alternativePaths.isEmpty()) selectedPaths[0] else alternativePaths[radioGroup!!.checkedRadioButtonId]
        requireActivity().config.addExcludedFolder(path)
        callbackAfterDialogConfirmed()
    }

    /**
     * Cleaning the stuff
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getAlternativePathsList(): List<String> {
        val pathsList = ArrayList<String>()
        if (selectedPaths.size > 1)
            return pathsList

        val path = selectedPaths[0]
        var basePath = path.getBasePath(requireActivity())
        val relativePath = path.substring(basePath.length)
        val parts = relativePath.split("/").filter(String::isNotEmpty)
        if (parts.isEmpty())
            return pathsList

        pathsList.add(basePath)
        if (basePath == "/")
            basePath = ""

        for (part in parts) {
            basePath += "/$part"
            pathsList.add(basePath)
        }

        return pathsList.reversed()
    }

    companion object {
        const val TAG = "ExcludeFolderDialogFragment"
    }
}