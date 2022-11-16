package ca.on.sudbury.hojat.smartgallery.dialogs

import android.app.Activity
import android.view.LayoutInflater
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.databinding.DialogWhatsNewBinding
import ca.on.sudbury.hojat.smartgallery.extensions.getAlertDialogBuilder
import ca.on.sudbury.hojat.smartgallery.extensions.setupDialogStuff
import ca.on.sudbury.hojat.smartgallery.models.Release
import timber.log.Timber

class WhatsNewDialog(
    val activity: Activity,
    private val releases: List<Release>
) {
    init {

        Timber.d("Hojat Ghasemi : WhatsNewDialog was called")

        val binding = DialogWhatsNewBinding.inflate(LayoutInflater.from(activity))
        binding.whatsNewContent.text = getNewReleases()

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok, null)
            .apply {
                activity.setupDialogStuff(
                    binding.root,
                    this,
                    R.string.whats_new,
                    cancelOnTouchOutside = false
                )
            }
    }

    private fun getNewReleases(): String {
        val sb = StringBuilder()

        releases.forEach { release ->
            val parts = activity.getString(release.textId).split("\n").map(String::trim)
            parts.forEach { releasePart ->
                sb.append("- $releasePart\n")
            }
        }

        return sb.toString()
    }
}
