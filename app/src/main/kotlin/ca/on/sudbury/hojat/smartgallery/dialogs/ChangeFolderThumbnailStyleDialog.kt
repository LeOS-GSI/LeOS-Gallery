package ca.on.sudbury.hojat.smartgallery.dialogs

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import ca.on.sudbury.hojat.smartgallery.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import ca.on.sudbury.hojat.smartgallery.activities.BaseSimpleActivity
import ca.on.sudbury.hojat.smartgallery.databinding.DialogChangeFolderThumbnailStyleBinding
import ca.on.sudbury.hojat.smartgallery.databinding.DirectoryItemGridRoundedCornersBinding
import ca.on.sudbury.hojat.smartgallery.databinding.DirectoryItemGridSquareBinding
import ca.on.sudbury.hojat.smartgallery.extensions.getAlertDialogBuilder
import ca.on.sudbury.hojat.smartgallery.extensions.setupDialogStuff
import ca.on.sudbury.hojat.smartgallery.extensions.getProperTextColor
import ca.on.sudbury.hojat.smartgallery.extensions.config
import ca.on.sudbury.hojat.smartgallery.helpers.FolderMediaCount
import ca.on.sudbury.hojat.smartgallery.helpers.FolderStyle

/**
 * In the settings page when you click on "Folder thumbnail style" the resulting
 * dialog is created by this class.
 */
class ChangeFolderThumbnailStyleDialog(
    val activity: BaseSimpleActivity,
    val callback: () -> Unit
) : DialogInterface.OnClickListener {

    private var config = activity.config
    var binding = DialogChangeFolderThumbnailStyleBinding.inflate(activity.layoutInflater)

    init {

        binding.dialogFolderLimitTitle.isChecked = config.limitFolderTitle

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok, this)
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this) {
                    setupStyle()
                    setupMediaCount()
                    updateSample()
                }
            }
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

    @SuppressLint("SetTextI18n")
    private fun updateSample() {
        val photoCount = 36
        val folderName = "Camera"
        binding.apply {
            val useRoundedCornersLayout =
                dialogRadioFolderStyle.checkedRadioButtonId == R.id.dialog_radio_folder_rounded_corners
            dialogFolderSampleHolder.removeAllViews()

            if (useRoundedCornersLayout) R.layout.directory_item_grid_rounded_corners else R.layout.directory_item_grid_square
            val roundedCornersBinding =
                DirectoryItemGridRoundedCornersBinding.inflate(activity.layoutInflater)
            val squareCornersBinding =
                DirectoryItemGridSquareBinding.inflate(activity.layoutInflater)

            val sampleView =
                if (useRoundedCornersLayout) roundedCornersBinding.root else squareCornersBinding.root

            dialogFolderSampleHolder.addView(sampleView)

            sampleView.layoutParams.width =
                activity.resources.getDimension(R.dimen.sample_thumbnail_size).toInt()
            (sampleView.layoutParams as RelativeLayout.LayoutParams).addRule(RelativeLayout.CENTER_HORIZONTAL)

            when (dialogRadioFolderCountHolder.checkedRadioButtonId) {
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
            var builder = Glide.with(activity)
                .load(R.drawable.sample_logo)
                .apply(options)

            if (useRoundedCornersLayout) {
                val cornerRadius =
                    root.resources.getDimension(R.dimen.rounded_corner_radius_big).toInt()
                builder = builder.transform(CenterCrop(), RoundedCorners(cornerRadius))
                sampleView.findViewById<TextView>(R.id.dir_name)
                    .setTextColor(activity.getProperTextColor())
                sampleView.findViewById<TextView>(R.id.photo_cnt)
                    .setTextColor(activity.getProperTextColor())
            }

            builder.into(sampleView.findViewById(R.id.dir_thumbnail))
        }
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
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
}
