package ca.on.sudbury.hojat.smartgallery.dialogs

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.databinding.DialogExcludeFolderBinding
import ca.on.sudbury.hojat.smartgallery.activities.BaseSimpleActivity
import ca.on.sudbury.hojat.smartgallery.extensions.beVisibleIf
import ca.on.sudbury.hojat.smartgallery.extensions.getAlertDialogBuilder
import ca.on.sudbury.hojat.smartgallery.extensions.getBasePath
import ca.on.sudbury.hojat.smartgallery.extensions.setupDialogStuff
import ca.on.sudbury.hojat.smartgallery.extensions.config


@SuppressLint("InflateParams")
class ExcludeFolderDialog(
    val activity: BaseSimpleActivity,
    private val selectedPaths: List<String>,
    val callback: () -> Unit
) {

    // we create the binding by referencing the owner Activity
    var binding = DialogExcludeFolderBinding.inflate(activity.layoutInflater)

    private val alternativePaths = getAlternativePathsList()
    private var radioGroup: RadioGroup? = null

    init {
        binding.apply {
            excludeFolderParent.beVisibleIf(alternativePaths.size > 1)

            radioGroup = excludeFolderRadioGroup
            excludeFolderRadioGroup.beVisibleIf(alternativePaths.size > 1)
        }

        alternativePaths.forEachIndexed { index, _ ->
            val radioButton = (activity.layoutInflater.inflate(
                R.layout.radio_button,
                null
            ) as RadioButton).apply {
                text = alternativePaths[index]
                isChecked = index == 0
                id = index
            }
            radioGroup!!.addView(
                radioButton,
                RadioGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok) { _, _ -> dialogConfirmed() }
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this)
            }
    }

    private fun dialogConfirmed() {
        val path =
            if (alternativePaths.isEmpty()) selectedPaths[0] else alternativePaths[radioGroup!!.checkedRadioButtonId]
        activity.config.addExcludedFolder(path)
        callback()
    }

    private fun getAlternativePathsList(): List<String> {
        val pathsList = ArrayList<String>()
        if (selectedPaths.size > 1)
            return pathsList

        val path = selectedPaths[0]
        var basePath = path.getBasePath(activity)
        val relativePath = path.substring(basePath.length)
        val parts = relativePath.split("/").filter(String::isNotEmpty)
        if (parts.isEmpty())
            return pathsList

        pathsList.add(basePath)
        if (basePath == "/")
            basePath = ""

        for (part in parts) {
            basePath += "/$part"
            pathsList.add(basePath)
        }

        return pathsList.reversed()
    }
}
