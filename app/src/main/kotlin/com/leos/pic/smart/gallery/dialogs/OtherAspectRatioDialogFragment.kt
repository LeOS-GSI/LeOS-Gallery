package ca.on.sudbury.hojat.smartgallery.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import ca.on.sudbury.hojat.smartgallery.databinding.DialogFragmentOtherAspectRatioBinding

/**
 * While you're in the editing page, if you click on "other" in
 * the aspect ratio section, this dialog will be called.
 */
class OtherAspectRatioDialogFragment(
    private val lastOtherAspectRatio: Pair<Float, Float>?,
    val callbackAfterRatioPicked: (aspectRatio: Pair<Float, Float>) -> Unit
) : DialogFragment() {

    // the binding
    private var _binding: DialogFragmentOtherAspectRatioBinding? = null
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
        _binding = DialogFragmentOtherAspectRatioBinding.inflate(
            inflater,
            container,
            false
        )

        loadDialogUi()
        return binding.root
    }

    private fun loadDialogUi() {

        with(binding) {
            // checks the radio buttons that are previously chosen by the user.
            val radio1SelectedItemId = when (lastOtherAspectRatio) {
                Pair(2f, 1f) -> otherAspectRatio21.id
                Pair(3f, 2f) -> otherAspectRatio32.id
                Pair(4f, 3f) -> otherAspectRatio43.id
                Pair(5f, 3f) -> otherAspectRatio53.id
                Pair(16f, 9f) -> otherAspectRatio169.id
                Pair(19f, 9f) -> otherAspectRatio199.id
                else -> 0
            }
            otherAspectRatioDialogRadio1.check(radio1SelectedItemId)
            val radio2SelectedItemId = when (lastOtherAspectRatio) {
                Pair(1f, 2f) -> otherAspectRatio12.id
                Pair(2f, 3f) -> otherAspectRatio23.id
                Pair(3f, 4f) -> otherAspectRatio34.id
                Pair(3f, 5f) -> otherAspectRatio35.id
                Pair(9f, 16f) -> otherAspectRatio916.id
                Pair(9f, 19f) -> otherAspectRatio919.id
                else -> 0
            }
            otherAspectRatioDialogRadio2.check(radio2SelectedItemId)
            if (radio1SelectedItemId == 0 && radio2SelectedItemId == 0) {
                otherAspectRatioDialogRadio1.check(otherAspectRatioCustom.id)
            }
        }


    }

    /**
     * Register listeners for views.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            btnCancelOtherAspectRatioDialog.setOnClickListener { dismiss() }
            otherAspectRatio21.setOnClickListener { ratioPicked(Pair(2f, 1f)) }
            otherAspectRatio32.setOnClickListener { ratioPicked(Pair(3f, 2f)) }
            otherAspectRatio43.setOnClickListener { ratioPicked(Pair(4f, 3f)) }
            otherAspectRatio53.setOnClickListener { ratioPicked(Pair(5f, 3f)) }
            otherAspectRatio169.setOnClickListener { ratioPicked(Pair(16f, 9f)) }
            otherAspectRatio199.setOnClickListener { ratioPicked(Pair(19f, 9f)) }
            otherAspectRatioCustom.setOnClickListener { customRatioPicked() }
            otherAspectRatio12.setOnClickListener { ratioPicked(Pair(1f, 2f)) }
            otherAspectRatio23.setOnClickListener { ratioPicked(Pair(2f, 3f)) }
            otherAspectRatio34.setOnClickListener { ratioPicked(Pair(3f, 4f)) }
            otherAspectRatio35.setOnClickListener { ratioPicked(Pair(3f, 5f)) }
            otherAspectRatio916.setOnClickListener { ratioPicked(Pair(9f, 16f)) }
            otherAspectRatio919.setOnClickListener { ratioPicked(Pair(9f, 19f)) }
        }
    }

    /**
     * Clean all used resources.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun customRatioPicked() {
        val callbackAfterDialogConfirmed: (Pair<Float, Float>) -> Unit = { aspectRatio ->
            callbackAfterRatioPicked(aspectRatio)
            dismiss()
        }
        CustomAspectRatioDialogFragment(lastOtherAspectRatio, callbackAfterDialogConfirmed).show(
            requireActivity().supportFragmentManager,
            CustomAspectRatioDialogFragment.TAG
        )
    }

    private fun ratioPicked(pair: Pair<Float, Float>) {
        callbackAfterRatioPicked(pair)
        dismiss()
    }

    companion object{
        const val TAG = "OtherAspectRatioDialogFragment"
    }
}