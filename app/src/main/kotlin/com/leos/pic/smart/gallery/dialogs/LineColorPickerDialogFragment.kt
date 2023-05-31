package ca.on.sudbury.hojat.smartgallery.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.activities.BaseSimpleActivity
import ca.on.sudbury.hojat.smartgallery.databinding.DialogFragmentLineColorPickerBinding
import ca.on.sudbury.hojat.smartgallery.extensions.copyToClipboard
import ca.on.sudbury.hojat.smartgallery.extensions.getThemeId
import ca.on.sudbury.hojat.smartgallery.extensions.toHex
import ca.on.sudbury.hojat.smartgallery.interfaces.LineColorPickerListener
import ca.on.sudbury.hojat.smartgallery.usecases.BeVisibleOrGoneUseCase

class LineColorPickerDialogFragment(
    val color: Int,
    val isPrimaryColorPicker: Boolean,
    private val primaryColors: Int = R.array.md_primary_colors,
    private val appIconIDs: ArrayList<Int>? = null,
    val menu: Menu? = null,
    val callback: (wasPositivePressed: Boolean, color: Int) -> Unit
) : DialogFragment() {

    // the binding
    private var _binding: DialogFragmentLineColorPickerBinding? = null
    private val binding get() = _binding!!

    // configurations
    private val primaryColorsCount = 19
    private val defaultPrimaryColorIndex = 14
    private val defaultSecondaryColorIndex = 6
    private var defaultColorValue = -1
    private var wasDimmedBackgroundRemoved = false


    /**
     * Create the UI of the dialog
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // load the binding
        _binding = DialogFragmentLineColorPickerBinding.inflate(
            inflater,
            container,
            false
        )

        // Configurations needed before drawing the UI
        defaultColorValue = requireActivity().resources.getColor(R.color.color_primary)

        loadDialogUi()
        return binding.root
    }

    override fun onResume() {
        super.onResume()

        getSpecificColor().apply {
            (requireActivity() as BaseSimpleActivity).updateActionbarColor(this)
            requireActivity().setTheme((requireActivity() as AppCompatActivity).getThemeId(this))
        }

    }

    private fun getSpecificColor() = binding.secondaryLineColorPicker.getCurrentColor()

    private fun loadDialogUi() {
        binding.apply {
            hexCode.text = color.toHex()
            hexCode.setOnLongClickListener {
                requireActivity().copyToClipboard(hexCode.text.toString().trim().substring(1))
                true
            }

            BeVisibleOrGoneUseCase(lineColorPickerIcon, !isPrimaryColorPicker)
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

            BeVisibleOrGoneUseCase(secondaryLineColorPicker, isPrimaryColorPicker)
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
    }

    /**
     * Register listeners for views.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding.lineColorPickerDialogBottomRow) {
            btnOk.setOnClickListener {
                dismiss()
                dialogConfirmed()
            }
            btnCancel.setOnClickListener {
                dismiss()
                callback(false, 0)
            }
        }
    }

    private fun dialogConfirmed() {
        val targetView = if (isPrimaryColorPicker)
            binding.secondaryLineColorPicker
        else
            binding.primaryLineColorPicker
        val color = targetView.getCurrentColor()
        callback(true, color)
    }

    /**
     * Clean all used resources.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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

    private fun getDefaultColorPair() =
        Pair(defaultPrimaryColorIndex, defaultSecondaryColorIndex)

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

    private fun getColors(id: Int) =
        requireActivity().resources.getIntArray(id).toCollection(ArrayList())

    private fun primaryColorChanged(index: Int) {
        binding.lineColorPickerIcon.setImageResource(appIconIDs?.getOrNull(index) ?: 0)
    }

    private fun colorUpdated(color: Int) {
        binding.hexCode.text = color.toHex()
        if (isPrimaryColorPicker) {
            (requireActivity() as BaseSimpleActivity).updateActionbarColor(color)
            requireActivity().setTheme((requireActivity() as AppCompatActivity).getThemeId(color))
            (requireActivity() as BaseSimpleActivity).updateMenuItemColors(menu, true, color)

            if (!wasDimmedBackgroundRemoved) {
                dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                wasDimmedBackgroundRemoved = true
            }
        }
    }

    companion object{
        const val TAG = "LineColorPickerDialogFragment"
    }
}