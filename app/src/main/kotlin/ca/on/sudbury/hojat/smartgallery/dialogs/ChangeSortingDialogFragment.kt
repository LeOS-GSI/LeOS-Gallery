package ca.on.sudbury.hojat.smartgallery.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.databinding.DialogFragmentChangeSortingBinding
import ca.on.sudbury.hojat.smartgallery.extensions.config
import ca.on.sudbury.hojat.smartgallery.helpers.Config
import ca.on.sudbury.hojat.smartgallery.helpers.SHOW_ALL
import ca.on.sudbury.hojat.smartgallery.helpers.SORT_BY_CUSTOM
import ca.on.sudbury.hojat.smartgallery.helpers.SORT_BY_DATE_MODIFIED
import ca.on.sudbury.hojat.smartgallery.helpers.SORT_BY_DATE_TAKEN
import ca.on.sudbury.hojat.smartgallery.helpers.SORT_BY_NAME
import ca.on.sudbury.hojat.smartgallery.helpers.SORT_BY_PATH
import ca.on.sudbury.hojat.smartgallery.helpers.SORT_BY_RANDOM
import ca.on.sudbury.hojat.smartgallery.helpers.SORT_BY_SIZE
import ca.on.sudbury.hojat.smartgallery.helpers.SORT_DESCENDING
import ca.on.sudbury.hojat.smartgallery.helpers.SORT_USE_NUMERIC_VALUE
import ca.on.sudbury.hojat.smartgallery.usecases.BeVisibleOrGoneUseCase

class ChangeSortingDialogFragment(
    val isDirectorySorting: Boolean,
    val showFolderCheckbox: Boolean,
    val path: String = "",
    val callback: () -> Unit
) : DialogFragment() {

    // the binding
    private var _binding: DialogFragmentChangeSortingBinding? = null
    private val binding get() = _binding!!

    // configuration related variables
    private var currSorting = 0
    private var pathToUse = if (!isDirectorySorting && path.isEmpty()) SHOW_ALL else path
    private lateinit var config: Config

    /**
     * Create the UI of the dialog
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFragmentChangeSortingBinding.inflate(inflater, container, false)

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

        binding.changeSortingDialogBottomRow.btnOk.setOnClickListener {
            dialogConfirmed()
            dismiss()
        }
        binding.changeSortingDialogBottomRow.btnCancel.setOnClickListener { dismiss() }
    }

    /**
     * Cleaning the stuff
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadDialogUi() {

        currSorting =
            if (isDirectorySorting) config.directorySorting else config.getFolderSorting(pathToUse)
        binding.apply {
            BeVisibleOrGoneUseCase(
                useForThisFolderDivider.root,
                showFolderCheckbox || (currSorting and SORT_BY_NAME != 0 || currSorting and SORT_BY_PATH != 0)
            )

            BeVisibleOrGoneUseCase(
                sortingDialogNumericSorting,
                showFolderCheckbox && (currSorting and SORT_BY_NAME != 0 || currSorting and SORT_BY_PATH != 0)
            )
            sortingDialogNumericSorting.isChecked = currSorting and SORT_USE_NUMERIC_VALUE != 0

            BeVisibleOrGoneUseCase(sortingDialogUseForThisFolder, showFolderCheckbox)
            sortingDialogUseForThisFolder.isChecked = config.hasCustomSorting(pathToUse)
            BeVisibleOrGoneUseCase(sortingDialogBottomNote, !isDirectorySorting)
            BeVisibleOrGoneUseCase(sortingDialogRadioCustom, isDirectorySorting)
        }
        setupSortRadio()
        setupOrderRadio()
    }

    private fun setupSortRadio() {
        binding.sortingDialogRadioSorting.setOnCheckedChangeListener { _, checkedId ->
            val isSortingByNameOrPath =
                checkedId == binding.sortingDialogRadioName.id || checkedId == binding.sortingDialogRadioPath.id
            BeVisibleOrGoneUseCase(binding.sortingDialogNumericSorting, isSortingByNameOrPath)
            BeVisibleOrGoneUseCase(
                binding.useForThisFolderDivider.root,
                binding.sortingDialogNumericSorting.visibility == View.VISIBLE || binding.sortingDialogUseForThisFolder.visibility == View.VISIBLE
            )

            val hideSortOrder =
                checkedId == binding.sortingDialogRadioCustom.id || checkedId == binding.sortingDialogRadioRandom.id
            BeVisibleOrGoneUseCase(binding.sortingDialogRadioOrder, !hideSortOrder)
            BeVisibleOrGoneUseCase(binding.sortingDialogOrderDivider.root, !hideSortOrder)
        }

        val sortBtn = when {
            currSorting and SORT_BY_PATH != 0 -> binding.sortingDialogRadioPath
            currSorting and SORT_BY_SIZE != 0 -> binding.sortingDialogRadioSize
            currSorting and SORT_BY_DATE_MODIFIED != 0 -> binding.sortingDialogRadioLastModified
            currSorting and SORT_BY_DATE_TAKEN != 0 -> binding.sortingDialogRadioDateTaken
            currSorting and SORT_BY_RANDOM != 0 -> binding.sortingDialogRadioRandom
            currSorting and SORT_BY_CUSTOM != 0 -> binding.sortingDialogRadioCustom
            else -> binding.sortingDialogRadioName
        }
        sortBtn.isChecked = true
    }

    private fun setupOrderRadio() {
        var orderBtn = binding.sortingDialogRadioAscending

        if (currSorting and SORT_DESCENDING != 0) {
            orderBtn = binding.sortingDialogRadioDescending
        }
        orderBtn.isChecked = true
    }

    private fun dialogConfirmed() {

        val sortingRadio = binding.sortingDialogRadioSorting
        var sorting = when (sortingRadio.checkedRadioButtonId) {
            R.id.sorting_dialog_radio_name -> SORT_BY_NAME
            R.id.sorting_dialog_radio_path -> SORT_BY_PATH
            R.id.sorting_dialog_radio_size -> SORT_BY_SIZE
            R.id.sorting_dialog_radio_last_modified -> SORT_BY_DATE_MODIFIED
            R.id.sorting_dialog_radio_random -> SORT_BY_RANDOM
            R.id.sorting_dialog_radio_custom -> SORT_BY_CUSTOM
            else -> SORT_BY_DATE_TAKEN
        }

        if (binding.sortingDialogRadioOrder.checkedRadioButtonId == R.id.sorting_dialog_radio_descending) {
            sorting = sorting or SORT_DESCENDING
        }

        if (binding.sortingDialogNumericSorting.isChecked) {
            sorting = sorting or SORT_USE_NUMERIC_VALUE
        }

        if (isDirectorySorting) {
            config.directorySorting = sorting
        } else {
            if (binding.sortingDialogUseForThisFolder.isChecked) {
                config.saveCustomSorting(pathToUse, sorting)
            } else {
                config.removeCustomSorting(pathToUse)
                config.sorting = sorting
            }
        }

        if (currSorting != sorting) {
            callback()
        }
    }

}