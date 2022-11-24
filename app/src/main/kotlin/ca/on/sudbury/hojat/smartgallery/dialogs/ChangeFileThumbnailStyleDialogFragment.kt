package ca.on.sudbury.hojat.smartgallery.dialogs

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import ca.on.sudbury.hojat.smartgallery.databinding.DialogFragmentChangeFileThumbnailStyleBinding
import ca.on.sudbury.hojat.smartgallery.extensions.config
import ca.on.sudbury.hojat.smartgallery.helpers.Config
import ca.on.sudbury.hojat.smartgallery.models.RadioItem

/**
 * In settings page click on "File thumbnail style" and the resulting dialog is created by this [DialogFragment].
 */
class ChangeFileThumbnailStyleDialogFragment : DialogFragment() {
    // the binding
    private var _binding: DialogFragmentChangeFileThumbnailStyleBinding? = null
    private val binding get() = _binding!!

    // configuration related variables
    private lateinit var config: Config
    private var thumbnailSpacing = 0 // default thumbnail spacing is 0

    /**
     * Create the UI of the dialog
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFragmentChangeFileThumbnailStyleBinding.inflate(inflater, container, false)

        // load config (I have to do it here cause it's used for loading UI)
        config = requireActivity().config
        thumbnailSpacing = config.thumbnailSpacing

        loadDialogUI()
        return binding.root
    }

    /**
     * Register listeners for views.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fileThumbnailDialogBottomRow.btnOk.setOnClickListener {
            dialogConfirmed()
            dismiss()
        }
        binding.fileThumbnailDialogBottomRow.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    /**
     * Cleaning the stuff
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadDialogUI() {
        binding.apply {
            dialogFileStyleRoundedCorners.isChecked = config.fileRoundedCorners
            dialogFileStyleAnimateGifs.isChecked = config.animateGifs
            dialogFileStyleShowThumbnailVideoDuration.isChecked = config.showThumbnailVideoDuration
            dialogFileStyleShowThumbnailFileTypes.isChecked = config.showThumbnailFileTypes
            dialogFileStyleMarkFavoriteItems.isChecked = config.markFavoriteItems

            dialogFileStyleRoundedCornersHolder.setOnClickListener { dialogFileStyleRoundedCorners.toggle() }
            dialogFileStyleAnimateGifsHolder.setOnClickListener { dialogFileStyleAnimateGifs.toggle() }
            dialogFileStyleShowThumbnailVideoDurationHolder.setOnClickListener { dialogFileStyleShowThumbnailVideoDuration.toggle() }
            dialogFileStyleShowThumbnailFileTypesHolder.setOnClickListener { dialogFileStyleShowThumbnailFileTypes.toggle() }
            dialogFileStyleMarkFavoriteItemsHolder.setOnClickListener { dialogFileStyleMarkFavoriteItems.toggle() }

            dialogFileStyleSpacingHolder.setOnClickListener {
                val items = arrayListOf(
                    RadioItem(0, "0x"),
                    RadioItem(1, "1x"),
                    RadioItem(2, "2x"),
                    RadioItem(4, "4x"),
                    RadioItem(8, "8x"),
                    RadioItem(16, "16x"),
                    RadioItem(32, "32x"),
                    RadioItem(64, "64x")
                )

                val callback: (Any) -> Unit = { newValue ->
                    thumbnailSpacing = newValue as Int
                    updateThumbnailSpacingText()
                }
                RadioGroupDialogFragment(
                    items = items,
                    checkedItemId = thumbnailSpacing,
                    callback = callback
                ).show(requireActivity().supportFragmentManager, "RadioGroupDialogFragment")
            }
        }
        updateThumbnailSpacingText()
    }

    @SuppressLint("SetTextI18n")
    private fun updateThumbnailSpacingText() {
        binding.dialogFileStyleSpacing.text = "${thumbnailSpacing}x"
    }

    private fun dialogConfirmed() {
        config.fileRoundedCorners = binding.dialogFileStyleRoundedCorners.isChecked
        config.animateGifs = binding.dialogFileStyleAnimateGifs.isChecked
        config.showThumbnailVideoDuration =
            binding.dialogFileStyleShowThumbnailVideoDuration.isChecked
        config.showThumbnailFileTypes = binding.dialogFileStyleShowThumbnailFileTypes.isChecked
        config.markFavoriteItems = binding.dialogFileStyleMarkFavoriteItems.isChecked
        config.thumbnailSpacing = thumbnailSpacing
    }
}