package ca.on.sudbury.hojat.smartgallery.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import ca.on.sudbury.hojat.smartgallery.databinding.DialogFragmentWhatsNewBinding
import ca.on.sudbury.hojat.smartgallery.models.Release

class WhatsNewDialogFragment(private val releases: List<Release>) : DialogFragment() {

    // the binding
    private var _binding: DialogFragmentWhatsNewBinding? = null
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
        _binding = DialogFragmentWhatsNewBinding.inflate(inflater, container, false)

        loadDialogUi()
        return binding.root
    }

    private fun loadDialogUi() {
        binding.whatsNewContent.text = getNewReleases()
    }

    /**
     * Register listeners for views.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnOkWhatsNewDialog.setOnClickListener { dismiss() }
    }

    /**
     * Clean all used resources.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getNewReleases(): String {
        val sb = StringBuilder()
        releases.forEach { release ->
            val parts = requireActivity().getString(release.textId).split("\n").map(String::trim)
            parts.forEach { releasePart ->
                sb.append("- $releasePart\n")
            }
        }
        return sb.toString()
    }
}