package ca.on.sudbury.hojat.smartgallery.dialogs

import android.view.Menu
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import ca.on.sudbury.hojat.smartgallery.extensions.beVisibleIf
import ca.on.sudbury.hojat.smartgallery.extensions.copyToClipboard
import ca.on.sudbury.hojat.smartgallery.extensions.getThemeId
import ca.on.sudbury.hojat.smartgallery.extensions.setupDialogStuff
import ca.on.sudbury.hojat.smartgallery.extensions.toHex
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.activities.BaseSimpleActivity
import ca.on.sudbury.hojat.smartgallery.databinding.DialogLineColorPickerBinding
import ca.on.sudbury.hojat.smartgallery.interfaces.LineColorPickerListener

class LineColorPickerDialog(
    val activity: BaseSimpleActivity,
    val color: Int,
    val isPrimaryColorPicker: Boolean,
    private val primaryColors: Int = R.array.md_primary_colors,
    val appIconIDs: ArrayList<Int>? = null,
    val menu: Menu? = null,
    val callback: (wasPositivePressed: Boolean, color: Int) -> Unit
) {

    private val primaryColorsCount = 19
    private val defaultPrimaryColorIndex = 14
    private val defaultSecondaryColorIndex = 6
    private val defaultColorValue = activity.resources.getColor(R.color.color_primary)
    private var wasDimmedBackgroundRemoved = false
    private var dialog: AlertDialog? = null

    private var binding = DialogLineColorPickerBinding.inflate(activity.layoutInflater)

    init {
        binding.apply {
            hexCode.text = color.toHex()
            hexCode.setOnLongClickListener {
                activity.copyToClipboard(hexCode.text.toString().trim().substring(1))
                true
            }

            lineColorPickerIcon.beVisibleIf(!isPrimaryColorPicker)
            val indexes = getColorIndexes(color)

            val primaryColorIndex = indexes.first
            primaryColorChanged(primaryColorIndex)
            primaryLineColorPicker.updateColors(getColors(primaryColors), primaryColorIndex)
            primaryLineColorPicker.listener = object : LineColorPickerListener {
                override fun colorChanged(index: Int, color: Int) {
                    val secondaryColors = getColorsForIndex(index)
                    secondaryLineColorPicker.updateColors(secondaryColors)

                    val newColor =
                        if (isPrimaryColorPicker) secondaryLineColorPicker.getCurrentColor() else color
                    colorUpdated(newColor)

                    if (!isPrimaryColorPicker) {
                        primaryColorChanged(index)
                    }
                }
            }

            secondaryLineColorPicker.beVisibleIf(isPrimaryColorPicker)
            secondaryLineColorPicker.updateColors(
                getColorsForIndex(primaryColorIndex),
                indexes.second
            )
            secondaryLineColorPicker.listener = object : LineColorPickerListener {
                override fun colorChanged(index: Int, color: Int) {
                    colorUpdated(color)
                }
            }
        }

        dialog = AlertDialog.Builder(activity)
            .setPositiveButton(R.string.ok) { _, _ -> dialogConfirmed() }
            .setNegativeButton(R.string.cancel) { _, _ -> dialogDismissed() }
            .setOnCancelListener { dialogDismissed() }
            .create().apply {
                activity.setupDialogStuff(binding.root, this)
            }
    }

    fun getSpecificColor() = binding.secondaryLineColorPicker.getCurrentColor()

    private fun colorUpdated(color: Int) {
        binding.hexCode.text = color.toHex()
        if (isPrimaryColorPicker) {
            activity.updateActionbarColor(color)
            activity.setTheme(activity.getThemeId(color))
            activity.updateMenuItemColors(menu, true, color)

            if (!wasDimmedBackgroundRemoved) {
                dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                wasDimmedBackgroundRemoved = true
            }
        }
    }

    private fun getColorIndexes(color: Int): Pair<Int, Int> {
        if (color == defaultColorValue) {
            return getDefaultColorPair()
        }

        for (i in 0 until primaryColorsCount) {
            getColorsForIndex(i).indexOfFirst { color == it }.apply {
                if (this != -1) {
                    return Pair(i, this)
                }
            }
        }

        return getDefaultColorPair()
    }

    private fun primaryColorChanged(index: Int) {
        binding.lineColorPickerIcon.setImageResource(appIconIDs?.getOrNull(index) ?: 0)
    }

    private fun getDefaultColorPair() =
        Pair(defaultPrimaryColorIndex, defaultSecondaryColorIndex)

    private fun dialogDismissed() {
        callback(false, 0)
    }

    private fun dialogConfirmed() {
        val targetView =
            if (isPrimaryColorPicker) binding.secondaryLineColorPicker else binding.primaryLineColorPicker
        val color = targetView.getCurrentColor()
        callback(true, color)
    }

    private fun getColorsForIndex(index: Int) = when (index) {
        0 -> getColors(R.array.md_reds)
        1 -> getColors(R.array.md_pinks)
        2 -> getColors(R.array.md_purples)
        3 -> getColors(R.array.md_deep_purples)
        4 -> getColors(R.array.md_indigos)
        5 -> getColors(R.array.md_blues)
        6 -> getColors(R.array.md_light_blues)
        7 -> getColors(R.array.md_cyans)
        8 -> getColors(R.array.md_teals)
        9 -> getColors(R.array.md_greens)
        10 -> getColors(R.array.md_light_greens)
        11 -> getColors(R.array.md_limes)
        12 -> getColors(R.array.md_yellows)
        13 -> getColors(R.array.md_ambers)
        14 -> getColors(R.array.md_oranges)
        15 -> getColors(R.array.md_deep_oranges)
        16 -> getColors(R.array.md_browns)
        17 -> getColors(R.array.md_blue_greys)
        18 -> getColors(R.array.md_greys)
        else -> throw RuntimeException("Invalid color id $index")
    }

    private fun getColors(id: Int) = activity.resources.getIntArray(id).toCollection(ArrayList())
}