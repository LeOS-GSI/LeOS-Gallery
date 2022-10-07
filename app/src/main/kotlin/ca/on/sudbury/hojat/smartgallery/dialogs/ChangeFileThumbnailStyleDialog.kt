package ca.on.sudbury.hojat.smartgallery.dialogs

import android.annotation.SuppressLint
import android.content.DialogInterface
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.databinding.DialogChangeFileThumbnailStyleBinding
import ca.on.sudbury.hojat.smartgallery.activities.BaseSimpleActivity
import com.simplemobiletools.commons.dialogs.RadioGroupDialog
import ca.on.sudbury.hojat.smartgallery.extensions.getAlertDialogBuilder
import ca.on.sudbury.hojat.smartgallery.extensions.setupDialogStuff
import com.simplemobiletools.commons.models.RadioItem
import ca.on.sudbury.hojat.smartgallery.extensions.config


class ChangeFileThumbnailStyleDialog(
    val activity: BaseSimpleActivity
) : DialogInterface.OnClickListener {

    // we create the binding by referencing the owner Activity
    var binding = DialogChangeFileThumbnailStyleBinding.inflate(activity.layoutInflater)

    private var config = activity.config
    private var thumbnailSpacing = config.thumbnailSpacing

    init {
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

                RadioGroupDialog(activity, items, thumbnailSpacing) {
                    thumbnailSpacing = it as Int
                    updateThumbnailSpacingText()
                }
            }
        }

        updateThumbnailSpacingText()

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok, this)
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this)
            }
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        config.fileRoundedCorners = binding.dialogFileStyleRoundedCorners.isChecked
        config.animateGifs = binding.dialogFileStyleAnimateGifs.isChecked
        config.showThumbnailVideoDuration = binding.dialogFileStyleShowThumbnailVideoDuration.isChecked
        config.showThumbnailFileTypes = binding.dialogFileStyleShowThumbnailFileTypes.isChecked
        config.markFavoriteItems = binding.dialogFileStyleMarkFavoriteItems.isChecked
        config.thumbnailSpacing = thumbnailSpacing
    }

    @SuppressLint("SetTextI18n")
    private fun updateThumbnailSpacingText() {
        binding.dialogFileStyleSpacing.text = "${thumbnailSpacing}x"
    }
}
