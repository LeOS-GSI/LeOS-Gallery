package ca.on.sudbury.hojat.smartgallery.dialogs

import android.annotation.SuppressLint
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.databinding.DialogSlideshowBinding
import ca.on.sudbury.hojat.smartgallery.activities.BaseSimpleActivity
import ca.on.sudbury.hojat.smartgallery.extensions.getAlertDialogBuilder
import ca.on.sudbury.hojat.smartgallery.extensions.setupDialogStuff
import ca.on.sudbury.hojat.smartgallery.models.RadioItem
import ca.on.sudbury.hojat.smartgallery.extensions.config
import ca.on.sudbury.hojat.smartgallery.helpers.SLIDESHOW_DEFAULT_INTERVAL
import ca.on.sudbury.hojat.smartgallery.helpers.SlideshowAnimation
import ca.on.sudbury.hojat.smartgallery.usecases.HideKeyboardUseCase

@SuppressLint("InflateParams")
class SlideShowDialog(
    val activity: BaseSimpleActivity,
    val callback: () -> Unit
) {
    // we create the binding by referencing the owner Activity
    var binding = DialogSlideshowBinding.inflate(activity.layoutInflater)

    init {
        binding.apply {
            intervalHint.hint =
                activity.getString(R.string.seconds_raw).replaceFirstChar { it.uppercaseChar() }
            intervalValue.setOnClickListener {
                intervalValue.selectAll()
            }

            intervalValue.setOnFocusChangeListener { v, hasFocus ->
                if (!hasFocus)
                    HideKeyboardUseCase(activity, v)
            }

            animationHolder.setOnClickListener {
                val items = arrayListOf(
                    RadioItem(
                        SlideshowAnimation.None.id,
                        activity.getString(R.string.no_animation)
                    ),
                    RadioItem(SlideshowAnimation.Slide.id, activity.getString(R.string.slide)),
                    RadioItem(SlideshowAnimation.Fade.id, activity.getString(R.string.fade))
                )

                RadioGroupDialog(activity, items, activity.config.slideshowAnimation) {
                    activity.config.slideshowAnimation = it as Int
                    animationValue.text = getAnimationText()
                }
            }

            includeVideosHolder.setOnClickListener {
                intervalValue.clearFocus()
                includeVideos.toggle()
            }

            includeGifsHolder.setOnClickListener {
                intervalValue.clearFocus()
                includeGifs.toggle()
            }

            randomOrderHolder.setOnClickListener {
                intervalValue.clearFocus()
                randomOrder.toggle()
            }

            moveBackwardsHolder.setOnClickListener {
                intervalValue.clearFocus()
                moveBackwards.toggle()
            }

            loopSlideshowHolder.setOnClickListener {
                intervalValue.clearFocus()
                loopSlideshow.toggle()
            }
        }
        setupValues()

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .apply {
                activity.setupDialogStuff(binding.root, this) { alertDialog ->
                    alertDialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN) // hides the keyboard
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        storeValues()
                        callback()
                        alertDialog.dismiss()
                    }
                }
            }
    }

    private fun setupValues() {
        val config = activity.config
        binding.apply {
            intervalValue.setText(config.slideshowInterval.toString())
            animationValue.text = getAnimationText()
            includeVideos.isChecked = config.slideshowIncludeVideos
            includeGifs.isChecked = config.slideshowIncludeGIFs
            randomOrder.isChecked = config.slideshowRandomOrder
            moveBackwards.isChecked = config.slideshowMoveBackwards
            loopSlideshow.isChecked = config.loopSlideshow
        }
    }

    private fun storeValues() {
        var interval = binding.intervalValue.text.toString()
        if (interval.trim('0').isEmpty())
            interval = SLIDESHOW_DEFAULT_INTERVAL.toString()

        activity.config.apply {
            slideshowAnimation = getAnimationValue(binding.animationValue.text.toString().trim())
            slideshowInterval = interval.toInt()
            slideshowIncludeVideos = binding.includeVideos.isChecked
            slideshowIncludeGIFs = binding.includeGifs.isChecked
            slideshowRandomOrder = binding.randomOrder.isChecked
            slideshowMoveBackwards = binding.moveBackwards.isChecked
            loopSlideshow = binding.loopSlideshow.isChecked
        }
    }

    private fun getAnimationText(): String {
        return when (activity.config.slideshowAnimation) {
            SlideshowAnimation.Slide.id -> activity.getString(R.string.slide)
            SlideshowAnimation.Fade.id -> activity.getString(R.string.fade)
            else -> activity.getString(R.string.no_animation)
        }
    }

    private fun getAnimationValue(text: String): Int {
        return when (text) {
            activity.getString(R.string.slide) -> SlideshowAnimation.Slide.id
            activity.getString(R.string.fade) -> SlideshowAnimation.Fade.id
            else -> SlideshowAnimation.None.id
        }
    }
}
