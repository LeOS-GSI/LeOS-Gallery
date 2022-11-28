package ca.on.sudbury.hojat.smartgallery.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import ca.on.sudbury.hojat.smartgallery.databinding.DialogFragmentRateStarsBinding
import ca.on.sudbury.hojat.smartgallery.extensions.getProperPrimaryColor
import ca.on.sudbury.hojat.smartgallery.usecases.ApplyColorFilterUseCase

/**
 * In the "About" page, in the "Help us" section click on "Rate us".
 * The resulting dialog is created by this class.
 */
class RateStarsDialogFragment : DialogFragment() {


    // the binding
    private var _binding: DialogFragmentRateStarsBinding? = null
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
        _binding = DialogFragmentRateStarsBinding.inflate(
            inflater,
            container,
            false
        )
        loadDialogUI()
        return binding.root

    }

    private fun loadDialogUI() {
        val primaryColor = requireActivity().getProperPrimaryColor()
        with(binding) {
            arrayOf(rateStar1, rateStar2, rateStar3, rateStar4, rateStar5).forEach {
                ApplyColorFilterUseCase(it, primaryColor)
            }
        }

    }

    /**
     * Register listeners for views.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            btnCancelRateUsDialog.setOnClickListener { dismiss() }
            rateStar1.setOnClickListener { submitRating() }
            rateStar2.setOnClickListener { submitRating() }
            rateStar3.setOnClickListener { submitRating() }
            rateStar4.setOnClickListener { submitRating() }
            rateStar5.setOnClickListener { submitRating() }
        }
    }

    /**
     * Clean all used resources.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun submitRating() {
        Toast.makeText(
            requireContext(),
            "This feature has not been implemented yet",
            Toast.LENGTH_LONG
        ).show()
        dismiss()
    }

    companion object {
        const val TAG = "RateStarsDialogFragment"
    }
}