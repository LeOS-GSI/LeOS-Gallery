package ca.on.sudbury.hojat.smartgallery.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.databinding.DialogFragmentSlideshowBinding
import ca.on.sudbury.hojat.smartgallery.extensions.config
import ca.on.sudbury.hojat.smartgallery.helpers.SLIDESHOW_DEFAULT_INTERVAL
import ca.on.sudbury.hojat.smartgallery.helpers.SlideshowAnimation
import ca.on.sudbury.hojat.smartgallery.models.RadioItem
import ca.on.sudbury.hojat.smartgallery.usecases.HideKeyboardUseCase

/**
 * Click on 3 dots button and choose "slideshow" the resulting dialog is
 * create by this class.
 */
class SlideShowDialogFragment(val callbackAfterDialogConfirmed: () -> Unit) : DialogFragment() {

    // the binding
    private var _binding: DialogFragmentSlideshowBinding? = null
    private val binding get() = _binding!!

    /**
     * Create the UI of the dialog
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // load the binding
        _binding = DialogFragmentSlideshowBinding.inflate(inflater, container, false)

        loadDialogUi()
        return binding.root

    }

    private fun loadDialogUi() {
        binding.apply {
            intervalHint.hint =
                getString(R.string.seconds_raw).replaceFirstChar { it.uppercaseChar() }
        }

        // read some shared prefs before drawing UI
        val config = requireActivity().config
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

    /**
     * Register listeners for views.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            slideshowDialogBottomRow.btnOk.setOnClickListener {
                storeValues()
                callbackAfterDialogConfirmed()
                dismiss()
            }
            slideshowDialogBottomRow.btnCancel.setOnClickListener { dismiss() }
            intervalValue.setOnClickListener {
                intervalValue.selectAll()
            }
            intervalValue.setOnFocusChangeListener { v, hasFocus ->
                if (!hasFocus)
                    HideKeyboardUseCase(requireActivity(), v)
            }
            animationHolder.setOnClickListener {
                val items = arrayListOf(
                    RadioItem(
                        SlideshowAnimation.None.id,
                        getString(R.string.no_animation)
                    ),
                    RadioItem(SlideshowAnimation.Slide.id, getString(R.string.slide)),
                    RadioItem(SlideshowAnimation.Fade.id, getString(R.string.fade))
                )
                val callback: (Any) -> Unit = { newValue ->
                    requireActivity().config.slideshowAnimation = newValue as Int
                    animationValue.text = getAnimationText()
                }
                RadioGroupDialogFragment(
                    items = items,
                    checkedItemId = requireActivity().config.slideshowAnimation,
                    callback = callback
                ).show(requireActivity().supportFragmentManager, RadioGroupDialogFragment.TAG)
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


    }

    /**
     * Clean all used resources.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getAnimationText(): String {
        return when (requireActivity().config.slideshowAnimation) {
            SlideshowAnimation.Slide.id -> getString(R.string.slide)
            SlideshowAnimation.Fade.id -> getString(R.string.fade)
            else -> getString(R.string.no_animation)
        }
    }

    private fun storeValues() {
        var interval = binding.intervalValue.text.toString()
        if (interval.trim('0').isEmpty())
            interval = SLIDESHOW_DEFAULT_INTERVAL.toString()

        requireActivity().config.apply {
            slideshowAnimation = getAnimationValue(binding.animationValue.text.toString().trim())
            slideshowInterval = interval.toInt()
            slideshowIncludeVideos = binding.includeVideos.isChecked
            slideshowIncludeGIFs = binding.includeGifs.isChecked
            slideshowRandomOrder = binding.randomOrder.isChecked
            slideshowMoveBackwards = binding.moveBackwards.isChecked
            loopSlideshow = binding.loopSlideshow.isChecked
        }
    }

    private fun getAnimationValue(text: String): Int {
        return when (text) {
            getString(R.string.slide) -> SlideshowAnimation.Slide.id
            getString(R.string.fade) -> SlideshowAnimation.Fade.id
            else -> SlideshowAnimation.None.id
        }
    }

    companion object {
        const val TAG = "SlideShowDialogFragment"
    }
}