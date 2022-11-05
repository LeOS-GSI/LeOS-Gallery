package ca.on.sudbury.hojat.smartgallery.dialogs

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.view.View
import android.widget.RelativeLayout
import ca.on.sudbury.hojat.smartgallery.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import ca.on.sudbury.hojat.smartgallery.activities.BaseSimpleActivity
import ca.on.sudbury.hojat.smartgallery.extensions.getAlertDialogBuilder
import ca.on.sudbury.hojat.smartgallery.extensions.setupDialogStuff
import ca.on.sudbury.hojat.smartgallery.extensions.getProperTextColor
import ca.on.sudbury.hojat.smartgallery.extensions.config
import ca.on.sudbury.hojat.smartgallery.helpers.FolderMediaCount
import ca.on.sudbury.hojat.smartgallery.helpers.FolderStyle
import kotlinx.android.synthetic.main.dialog_change_folder_thumbnail_style.view.*
import kotlinx.android.synthetic.main.directory_item_grid_square.view.*

/**
 * In the settings page when you click on "Folder thumbnail style" the resulting
 * dialog is created by this class.
 */
class ChangeFolderThumbnailStyleDialog(
    val activity: BaseSimpleActivity,
    val callback: () -> Unit
) : DialogInterface.OnClickListener {

    private var config = activity.config

    private var view =
        activity.layoutInflater.inflate(R.layout.dialog_change_folder_thumbnail_style, null).apply {
            dialog_folder_limit_title.isChecked = config.limitFolderTitle
        }

    init {
        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok, this)
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(view, this) {
                    setupStyle()
                    setupMediaCount()
                    updateSample()
                }
            }
    }

    private fun setupStyle() {
        val styleRadio = view.dialog_radio_folder_style
        styleRadio.setOnCheckedChangeListener { _, _ ->
            updateSample()
        }

        val styleBtn = when (config.folderStyle) {
            FolderStyle.Square.id -> styleRadio.dialog_radio_folder_square
            else -> styleRadio.dialog_radio_folder_rounded_corners
        }

        styleBtn.isChecked = true
    }

    private fun setupMediaCount() {
        val countRadio = view.dialog_radio_folder_count_holder
        countRadio.setOnCheckedChangeListener { _, _ ->
            updateSample()
        }

        val countBtn = when (config.showFolderMediaCount) {
            FolderMediaCount.SeparateLine.id -> countRadio.dialog_radio_folder_count_line
            FolderMediaCount.Brackets.id -> countRadio.dialog_radio_folder_count_brackets
            else -> countRadio.dialog_radio_folder_count_none
        }

        countBtn.isChecked = true
    }

    @SuppressLint("SetTextI18n")
    private fun updateSample() {
        val photoCount = 36
        val folderName = "Camera"
        view.apply {
            val useRoundedCornersLayout =
                dialog_radio_folder_style.checkedRadioButtonId == R.id.dialog_radio_folder_rounded_corners
            dialog_folder_sample_holder.removeAllViews()

            val layout =
                if (useRoundedCornersLayout) R.layout.directory_item_grid_rounded_corners else R.layout.directory_item_grid_square
            val sampleView = activity.layoutInflater.inflate(layout, null)
            dialog_folder_sample_holder.addView(sampleView)

            sampleView.layoutParams.width =
                activity.resources.getDimension(R.dimen.sample_thumbnail_size).toInt()
            (sampleView.layoutParams as RelativeLayout.LayoutParams).addRule(RelativeLayout.CENTER_HORIZONTAL)

            when (dialog_radio_folder_count_holder.checkedRadioButtonId) {
                R.id.dialog_radio_folder_count_line -> {
                    dir_name.text = folderName
                    photo_cnt.text = photoCount.toString()
                    photo_cnt.visibility = View.VISIBLE
                }
                R.id.dialog_radio_folder_count_brackets -> {
                    photo_cnt.visibility = View.GONE
                    dir_name.text = "$folderName ($photoCount)"
                }
                else -> {
                    dir_name.text = folderName
                    photo_cnt?.visibility = View.GONE
                }
            }

            val options = RequestOptions().centerCrop()
            var builder = Glide.with(activity)
                .load(R.drawable.sample_logo)
                .apply(options)

            if (useRoundedCornersLayout) {
                val cornerRadius = resources.getDimension(R.dimen.rounded_corner_radius_big).toInt()
                builder = builder.transform(CenterCrop(), RoundedCorners(cornerRadius))
                dir_name.setTextColor(activity.getProperTextColor())
                photo_cnt.setTextColor(activity.getProperTextColor())
            }

            builder.into(dir_thumbnail)
        }
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        val style = when (view.dialog_radio_folder_style.checkedRadioButtonId) {
            R.id.dialog_radio_folder_square -> FolderStyle.Square.id
            else -> FolderStyle.RoundedCorners.id
        }

        val count = when (view.dialog_radio_folder_count_holder.checkedRadioButtonId) {
            R.id.dialog_radio_folder_count_line -> FolderMediaCount.SeparateLine.id
            R.id.dialog_radio_folder_count_brackets -> FolderMediaCount.Brackets.id
            else -> FolderMediaCount.None.id
        }

        config.folderStyle = style
        config.showFolderMediaCount = count
        config.limitFolderTitle = view.dialog_folder_limit_title.isChecked
        callback()
    }
}
