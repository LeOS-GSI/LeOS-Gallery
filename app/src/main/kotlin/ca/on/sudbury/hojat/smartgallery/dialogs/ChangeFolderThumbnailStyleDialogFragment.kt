package ca.on.sudbury.hojat.smartgallery.dialogs

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.databinding.DialogFragmentChangeFolderThumbnailStyleBinding
import ca.on.sudbury.hojat.smartgallery.databinding.DirectoryItemGridRoundedCornersBinding
import ca.on.sudbury.hojat.smartgallery.databinding.DirectoryItemGridSquareBinding
import ca.on.sudbury.hojat.smartgallery.extensions.config
import ca.on.sudbury.hojat.smartgallery.extensions.getProperTextColor
import ca.on.sudbury.hojat.smartgallery.helpers.Config
import ca.on.sudbury.hojat.smartgallery.helpers.FolderMediaCount
import ca.on.sudbury.hojat.smartgallery.helpers.FolderStyle
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions

/**
 * In the settings page when you click on "Folder thumbnail style" the resulting
 * dialog is created by this class.
 */
class ChangeFolderThumbnailStyleDialogFragment(val callback: () -> Unit) : DialogFragment() {

    // All the bindings
    private var _binding: DialogFragmentChangeFolderThumbnailStyleBinding? = null
    private var _roundedCornersBinding: DirectoryItemGridRoundedCornersBinding? = null
    private var _squareCornersBinding: DirectoryItemGridSquareBinding? = null
    private val binding get() = _binding!!
    private val roundedCornersBinding get() = _roundedCornersBinding!!
    private val squareCornersBinding get() = _squareCornersBinding!!

    // configuration related variables
    private lateinit var config: Config

    /**
     * Create the UI of the dialog
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // load all the bindings
        _binding =
            DialogFragmentChangeFolderThumbnailStyleBinding.inflate(inflater, container, false)
        _roundedCornersBinding =
            DirectoryItemGridRoundedCornersBinding.inflate(inflater, container, false)
        _squareCornersBinding = DirectoryItemGridSquareBinding.inflate(inflater, container, false)

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

        binding.folderThumbnailStyleDialogBottomRow.btnOk.setOnClickListener {
            dialogConfirmed()
            dismiss()
        }
        binding.folderThumbnailStyleDialogBottomRow.btnCancel.setOnClickListener { dismiss() }
    }

    /**
     * Clean all used resources.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _roundedCornersBinding = null
        _squareCornersBinding = null
    }

    private fun loadDialogUi() {
        binding.dialogFolderLimitTitle.isChecked = config.limitFolderTitle
        setupStyle()
        setupMediaCount()
        updateSample()
    }

    private fun dialogConfirmed() {
        val style = when (binding.dialogRadioFolderStyle.checkedRadioButtonId) {
            R.id.dialog_radio_folder_square -> FolderStyle.Square.id
            else -> FolderStyle.RoundedCorners.id
        }

        val count = when (binding.dialogRadioFolderCountHolder.checkedRadioButtonId) {
            R.id.dialog_radio_folder_count_line -> FolderMediaCount.SeparateLine.id
            R.id.dialog_radio_folder_count_brackets -> FolderMediaCount.Brackets.id
            else -> FolderMediaCount.None.id
        }

        config.folderStyle = style
        config.showFolderMediaCount = count
        config.limitFolderTitle = binding.dialogFolderLimitTitle.isChecked
        callback()
    }

    private fun setupStyle() {
        val styleRadio = binding.dialogRadioFolderStyle
        styleRadio.setOnCheckedChangeListener { _, _ ->
            updateSample()
        }

        val styleBtn = when (config.folderStyle) {
            FolderStyle.Square.id -> binding.dialogRadioFolderSquare
            else -> binding.dialogRadioFolderRoundedCorners
        }

        styleBtn.isChecked = true
    }

    @SuppressLint("SetTextI18n")
    private fun updateSample() {
        val photoCount = 36
        val folderName = "Camera"

        val useRoundedCornersLayout =
            binding.dialogRadioFolderStyle.checkedRadioButtonId == R.id.dialog_radio_folder_rounded_corners
        binding.dialogFolderSampleHolder.removeAllViews()

        if (useRoundedCornersLayout) R.layout.directory_item_grid_rounded_corners else R.layout.directory_item_grid_square

        val sampleView =
            if (useRoundedCornersLayout) roundedCornersBinding.root else squareCornersBinding.root

        binding.dialogFolderSampleHolder.addView(sampleView)

        sampleView.layoutParams.width =
            requireActivity().resources.getDimension(R.dimen.sample_thumbnail_size).toInt()
        (sampleView.layoutParams as RelativeLayout.LayoutParams).addRule(RelativeLayout.CENTER_HORIZONTAL)

        when (binding.dialogRadioFolderCountHolder.checkedRadioButtonId) {
            R.id.dialog_radio_folder_count_line -> {

                sampleView.findViewById<TextView>(R.id.dir_name).text = folderName

                with(sampleView.findViewById<TextView>(R.id.photo_cnt)) {
                    text = photoCount.toString()
                    visibility = View.VISIBLE
                }
            }
            R.id.dialog_radio_folder_count_brackets -> {
                sampleView.findViewById<TextView>(R.id.photo_cnt).visibility = View.GONE
                sampleView.findViewById<TextView>(R.id.dir_name).text =
                    "$folderName ($photoCount)"
            }
            else -> {
                sampleView.findViewById<TextView>(R.id.dir_name).text = folderName
                sampleView.findViewById<TextView>(R.id.photo_cnt).visibility = View.GONE
            }
        }

        val options = RequestOptions().centerCrop()
        var builder = Glide.with(requireActivity())
            .load(R.drawable.sample_logo)
            .apply(options)

        if (useRoundedCornersLayout) {
            val cornerRadius =
                binding.root.resources.getDimension(R.dimen.rounded_corner_radius_big).toInt()
            builder = builder.transform(CenterCrop(), RoundedCorners(cornerRadius))
            sampleView.findViewById<TextView>(R.id.dir_name)
                .setTextColor(requireActivity().getProperTextColor())
            sampleView.findViewById<TextView>(R.id.photo_cnt)
                .setTextColor(requireActivity().getProperTextColor())
        }

        builder.into(sampleView.findViewById(R.id.dir_thumbnail))

    }

    private fun setupMediaCount() {
        binding.dialogRadioFolderCountHolder
        binding.dialogRadioFolderCountHolder.setOnCheckedChangeListener { _, _ ->
            updateSample()
        }

        val countBtn = when (config.showFolderMediaCount) {
            FolderMediaCount.SeparateLine.id -> binding.dialogRadioFolderCountLine
            FolderMediaCount.Brackets.id -> binding.dialogRadioFolderCountBrackets
            else -> binding.dialogRadioFolderCountNone
        }

        countBtn.isChecked = true
    }
}