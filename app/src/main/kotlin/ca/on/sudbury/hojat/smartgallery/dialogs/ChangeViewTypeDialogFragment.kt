package ca.on.sudbury.hojat.smartgallery.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import ca.on.sudbury.hojat.smartgallery.databinding.DialogFragmentChangeViewTypeBinding
import ca.on.sudbury.hojat.smartgallery.extensions.config
import ca.on.sudbury.hojat.smartgallery.helpers.Config
import ca.on.sudbury.hojat.smartgallery.helpers.SHOW_ALL
import ca.on.sudbury.hojat.smartgallery.helpers.ViewType
import ca.on.sudbury.hojat.smartgallery.usecases.BeVisibleOrGoneUseCase

/**
 * The view type of the app can be one of these 2 : Grid, List
 */
class ChangeViewTypeDialogFragment(
    private val fromFoldersView: Boolean,
    val path: String = "",
    val callback: () -> Unit
) : DialogFragment() {

    // the binding
    private var _binding: DialogFragmentChangeViewTypeBinding? = null
    private val binding get() = _binding!!

    // configuration related variables
    private lateinit var config: Config
    private var pathToUse = path.ifEmpty { SHOW_ALL }

    /**
     * Create the UI of the dialog
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFragmentChangeViewTypeBinding.inflate(inflater, container, false)
        // load config (I have to do it here cause it's used for loading UI)
        config = requireActivity().config
        loadDialogUi()
        return binding.root
    }

    /**
     * Register listeners for views.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewTypeDialogBottomRow.btnOk.setOnClickListener {
            dialogConfirmed()
            dismiss()
        }
        binding.viewTypeDialogBottomRow.btnCancel.setOnClickListener { dismiss() }
    }

    /**
     * Clean all used resources.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadDialogUi() {
        binding.apply {
            val viewToCheck = if (fromFoldersView) {
                if (config.viewTypeFolders == ViewType.Grid.id) {
                    changeViewTypeDialogRadioGrid.id
                } else {
                    changeViewTypeDialogRadioList.id
                }
            } else {
                val currViewType = config.getFolderViewType(pathToUse)
                if (currViewType == ViewType.Grid.id) {
                    changeViewTypeDialogRadioGrid.id
                } else {
                    changeViewTypeDialogRadioList.id
                }
            }

            changeViewTypeDialogRadio.check(viewToCheck)
            changeViewTypeDialogGroupDirectSubfolders.apply {
                BeVisibleOrGoneUseCase(this, fromFoldersView)
                isChecked = config.groupDirectSubfolders
            }

            changeViewTypeDialogUseForThisFolder.apply {
                BeVisibleOrGoneUseCase(this, !fromFoldersView)
                isChecked = config.hasCustomViewType(pathToUse)
            }
        }
    }

    private fun dialogConfirmed() {
        val viewType =
            if (binding.changeViewTypeDialogRadio.checkedRadioButtonId == binding.changeViewTypeDialogRadioGrid.id) {
                ViewType.Grid.id
            } else {
                ViewType.List.id
            }
        if (fromFoldersView) {
            config.viewTypeFolders = viewType
            config.groupDirectSubfolders =
                binding.changeViewTypeDialogGroupDirectSubfolders.isChecked
        } else {
            if (binding.changeViewTypeDialogUseForThisFolder.isChecked) {
                config.saveFolderViewType(pathToUse, viewType)
            } else {
                config.removeFolderViewType(pathToUse)
                config.viewTypeFiles = viewType
            }
        }
        callback()
    }
}