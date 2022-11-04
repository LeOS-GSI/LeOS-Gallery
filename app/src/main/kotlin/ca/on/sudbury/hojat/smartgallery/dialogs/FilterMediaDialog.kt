package ca.on.sudbury.hojat.smartgallery.dialogs

import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.databinding.DialogFilterMediaBinding
import ca.on.sudbury.hojat.smartgallery.activities.BaseSimpleActivity
import ca.on.sudbury.hojat.smartgallery.extensions.getAlertDialogBuilder
import ca.on.sudbury.hojat.smartgallery.extensions.setupDialogStuff
import ca.on.sudbury.hojat.smartgallery.extensions.config
import ca.on.sudbury.hojat.smartgallery.helpers.MediaType
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
            filterMediaImages.isChecked = filterMedia and MediaType.Image.id != 0
            filterMediaVideos.isChecked = filterMedia and MediaType.Video.id != 0
            filterMediaGifs.isChecked = filterMedia and MediaType.Gif.id != 0
            filterMediaRaws.isChecked = filterMedia and MediaType.Raw.id != 0
            filterMediaSvgs.isChecked = filterMedia and MediaType.Svg.id != 0
            filterMediaPortraits.isChecked = filterMedia and MediaType.Portrait.id != 0
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
            result += MediaType.Image.id
        if (binding.filterMediaVideos.isChecked)
            result += MediaType.Video.id
        if (binding.filterMediaGifs.isChecked)
            result += MediaType.Gif.id
        if (binding.filterMediaRaws.isChecked)
            result += MediaType.Raw.id
        if (binding.filterMediaSvgs.isChecked)
            result += MediaType.Svg.id
        if (binding.filterMediaPortraits.isChecked)
            result += MediaType.Portrait.id

        if (result == 0) {
            result = getDefaultFileFilter()
        }

        if (activity.config.filterMedia != result) {
            activity.config.filterMedia = result
            callback(result)
        }
    }
}
