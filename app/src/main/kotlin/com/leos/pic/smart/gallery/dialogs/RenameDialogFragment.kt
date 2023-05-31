package ca.on.sudbury.hojat.smartgallery.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.viewpager.widget.ViewPager
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.activities.BaseSimpleActivity
import ca.on.sudbury.hojat.smartgallery.adapters.RenameAdapter
import ca.on.sudbury.hojat.smartgallery.databinding.DialogFragmentRenameBinding
import ca.on.sudbury.hojat.smartgallery.extensions.baseConfig
import ca.on.sudbury.hojat.smartgallery.extensions.getProperBackgroundColor
import ca.on.sudbury.hojat.smartgallery.extensions.getProperPrimaryColor
import ca.on.sudbury.hojat.smartgallery.extensions.getProperTextColor
import ca.on.sudbury.hojat.smartgallery.extensions.onTabSelectionChanged
import ca.on.sudbury.hojat.smartgallery.helpers.RENAME_PATTERN
import ca.on.sudbury.hojat.smartgallery.helpers.RENAME_SIMPLE
import ca.on.sudbury.hojat.smartgallery.views.MyViewPager

/**
 * Choose a few pics/videos and from the context menu click on "rename".
 * The resulting dialog is created via this class.
 */
class RenameDialogFragment(
    val paths: ArrayList<String>,
    private val useMediaFileExtension: Boolean,
    val callback: () -> Unit
) : DialogFragment() {

    // the binding
    private var _binding: DialogFragmentRenameBinding? = null
    private val binding get() = _binding!!

    // references to the UI which are needed throughout the Fragment calls
    private lateinit var tabsAdapter: RenameAdapter
    private lateinit var viewPager: MyViewPager


    /**
     * Create the UI of the dialog
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogFragmentRenameBinding.inflate(inflater, container, false)
        loadDialogUI()
        return binding.root
    }

    private fun loadDialogUI() {
        binding.apply {
            viewPager = dialogTabViewPager
            tabsAdapter = RenameAdapter(requireActivity() as BaseSimpleActivity, paths)
            viewPager.adapter = tabsAdapter

            viewPager.currentItem = requireActivity().baseConfig.lastRenameUsed

            if (requireActivity().baseConfig.isUsingSystemTheme) {
                dialogTabLayout.setBackgroundColor(resources.getColor(R.color.you_dialog_background_color))
            } else {
                dialogTabLayout.setBackgroundColor(root.context.getProperBackgroundColor())
            }

            val textColor = root.context.getProperTextColor()
            dialogTabLayout.setTabTextColors(textColor, textColor)
            dialogTabLayout.setSelectedTabIndicatorColor(root.context.getProperPrimaryColor())

            if (requireActivity().baseConfig.isUsingSystemTheme) {
                dialogTabLayout.setBackgroundColor(resources.getColor(R.color.you_dialog_background_color))
            }


        }
        requireDialog().window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
    }

    /**
     * Register listeners for views.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                }

                override fun onPageSelected(position: Int) {
                    dialogTabLayout.getTabAt(position)!!.select()
                }

                override fun onPageScrollStateChanged(state: Int) {}
            })
            dialogTabLayout.onTabSelectionChanged(tabSelectedAction = {
                viewPager.currentItem = when {
                    it.text.toString().equals(
                        root.resources.getString(R.string.simple_renaming),
                        true
                    ) -> RENAME_SIMPLE
                    else -> RENAME_PATTERN
                }
            })
            renameDialogBottomRow.btnOk.setOnClickListener {
                dialogConfirmed()
                dismiss()
            }
            renameDialogBottomRow.btnCancel.setOnClickListener { dismiss() }
        }
    }

    private fun dialogConfirmed() {
        tabsAdapter.dialogConfirmed(useMediaFileExtension, viewPager.currentItem) {
            if (it) {
                requireActivity().baseConfig.lastRenameUsed = viewPager.currentItem
                callback()
            }
        }
    }

    /**
     * Cleaning the stuff
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "RenameDialogFragment"
    }
}