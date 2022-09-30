package ca.on.sudbury.hojat.smartgallery.dialogs

import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.databinding.DialogFilterMediaBinding
import com.simplemobiletools.commons.activities.BaseSimpleActivity
import com.simplemobiletools.commons.extensions.getAlertDialogBuilder
import com.simplemobiletools.commons.extensions.setupDialogStuff
import ca.on.sudbury.hojat.smartgallery.extensions.config
import ca.on.sudbury.hojat.smartgallery.helpers.TYPE_IMAGES
import ca.on.sudbury.hojat.smartgallery.helpers.TYPE_VIDEOS
import ca.on.sudbury.hojat.smartgallery.helpers.TYPE_GIFS
import ca.on.sudbury.hojat.smartgallery.helpers.TYPE_RAWS
import ca.on.sudbury.hojat.smartgallery.helpers.TYPE_SVGS
import ca.on.sudbury.hojat.smartgallery.helpers.TYPE_PORTRAITS
import ca.on.sudbury.hojat.smartgallery.helpers.getDefaultFileFilter

class FilterMediaDialog(
    val activity: BaseSimpleActivity,
    val callback: (result: Int) -> Unit
) {

    // we create the binding by referencing the owner Activity
    var binding = DialogFilterMediaBinding.inflate(activity.layoutInflater)

    init {
        val filterMedia = activity.config.filterMedia
        binding.apply {
            filterMediaImages.isChecked = filterMedia and TYPE_IMAGES != 0
            filterMediaVideos.isChecked = filterMedia and TYPE_VIDEOS != 0
            filterMediaGifs.isChecked = filterMedia and TYPE_GIFS != 0
            filterMediaRaws.isChecked = filterMedia and TYPE_RAWS != 0
            filterMediaSvgs.isChecked = filterMedia and TYPE_SVGS != 0
            filterMediaPortraits.isChecked = filterMedia and TYPE_PORTRAITS != 0
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok) { _, _ -> dialogConfirmed() }
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this, R.string.filter_media)
            }
    }

    private fun dialogConfirmed() {
        var result = 0
        if (binding.filterMediaImages.isChecked)
            result += TYPE_IMAGES
        if (binding.filterMediaVideos.isChecked)
            result += TYPE_VIDEOS
        if (binding.filterMediaGifs.isChecked)
            result += TYPE_GIFS
        if (binding.filterMediaRaws.isChecked)
            result += TYPE_RAWS
        if (binding.filterMediaSvgs.isChecked)
            result += TYPE_SVGS
        if (binding.filterMediaPortraits.isChecked)
            result += TYPE_PORTRAITS

        if (result == 0) {
            result = getDefaultFileFilter()
        }

        if (activity.config.filterMedia != result) {
            activity.config.filterMedia = result
            callback(result)
        }
    }
}
