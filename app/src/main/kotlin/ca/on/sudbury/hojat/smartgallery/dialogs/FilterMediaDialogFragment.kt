package ca.on.sudbury.hojat.smartgallery.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import ca.on.sudbury.hojat.smartgallery.databinding.DialogFragmentFilterMediaBinding
import ca.on.sudbury.hojat.smartgallery.extensions.config
import ca.on.sudbury.hojat.smartgallery.helpers.MediaType
import ca.on.sudbury.hojat.smartgallery.helpers.getDefaultFileFilter

/**
 * In main page click on the 3-vertical-dots button and from the drop-down menu,
 * choose the "Filter media" option. The resulting dialog is created by this class.
 */
class FilterMediaDialogFragment(val callback: (result: Int) -> Unit) : DialogFragment() {

    // the binding
    private var _binding: DialogFragmentFilterMediaBinding? = null
    private val binding get() = _binding!!

    /**
     * Create the UI of the dialog
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFragmentFilterMediaBinding.inflate(inflater, container, false)

        loadDialogUi()
        return binding.root
    }


    /**
     * Register listeners for views.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.filterMediaDialogBottomRow.btnOk.setOnClickListener {
            dialogConfirmed()
            dismiss()
        }

        binding.filterMediaDialogBottomRow.btnCancel.setOnClickListener { dismiss() }
    }


    /**
     * Cleaning the stuff
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadDialogUi() {
        val filterMedia = requireActivity().config.filterMedia
        binding.apply {
            filterMediaImages.isChecked = filterMedia and MediaType.Image.id != 0
            filterMediaVideos.isChecked = filterMedia and MediaType.Video.id != 0
            filterMediaGifs.isChecked = filterMedia and MediaType.Gif.id != 0
            filterMediaRaws.isChecked = filterMedia and MediaType.Raw.id != 0
            filterMediaSvgs.isChecked = filterMedia and MediaType.Svg.id != 0
            filterMediaPortraits.isChecked = filterMedia and MediaType.Portrait.id != 0
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

        if (requireActivity().config.filterMedia != result) {
            requireActivity().config.filterMedia = result
            callback(result)
        }
    }

    companion object{
        const val TAG="FilterMediaDialogFragment"
    }
}