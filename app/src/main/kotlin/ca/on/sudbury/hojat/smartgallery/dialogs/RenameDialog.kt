package ca.on.sudbury.hojat.smartgallery.dialogs


import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import ca.on.sudbury.hojat.smartgallery.extensions.baseConfig
import ca.on.sudbury.hojat.smartgallery.extensions.getAlertDialogBuilder
import ca.on.sudbury.hojat.smartgallery.extensions.getProperBackgroundColor
import ca.on.sudbury.hojat.smartgallery.extensions.getProperPrimaryColor
import ca.on.sudbury.hojat.smartgallery.extensions.getProperTextColor
import ca.on.sudbury.hojat.smartgallery.extensions.setupDialogStuff
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.activities.BaseSimpleActivity
import ca.on.sudbury.hojat.smartgallery.adapters.RenameAdapter
import ca.on.sudbury.hojat.smartgallery.extensions.onPageChangeListener
import ca.on.sudbury.hojat.smartgallery.extensions.onTabSelectionChanged
import ca.on.sudbury.hojat.smartgallery.helpers.RENAME_PATTERN
import ca.on.sudbury.hojat.smartgallery.helpers.RENAME_SIMPLE
import ca.on.sudbury.hojat.smartgallery.views.MyViewPager
import kotlinx.android.synthetic.main.dialog_rename.view.*

class RenameDialog(
    val activity: BaseSimpleActivity,
    val paths: ArrayList<String>,
    private val useMediaFileExtension: Boolean,
    val callback: () -> Unit
) {
    var dialog: AlertDialog? = null

    @SuppressLint("InflateParams")
    val view = LayoutInflater.from(activity).inflate(R.layout.dialog_rename, null)!!
    var tabsAdapter: RenameAdapter
    var viewPager: MyViewPager

    init {
        view.apply {
            viewPager = findViewById(R.id.dialog_tab_view_pager)
            tabsAdapter = RenameAdapter(activity, paths)
            viewPager.adapter = tabsAdapter
            viewPager.onPageChangeListener {
                dialog_tab_layout.getTabAt(it)!!.select()
            }
            viewPager.currentItem = activity.baseConfig.lastRenameUsed

            if (activity.baseConfig.isUsingSystemTheme) {
                dialog_tab_layout.setBackgroundColor(activity.resources.getColor(R.color.you_dialog_background_color))
            } else {
                dialog_tab_layout.setBackgroundColor(context.getProperBackgroundColor())
            }

            val textColor = context.getProperTextColor()
            dialog_tab_layout.setTabTextColors(textColor, textColor)
            dialog_tab_layout.setSelectedTabIndicatorColor(context.getProperPrimaryColor())

            if (activity.baseConfig.isUsingSystemTheme) {
                dialog_tab_layout.setBackgroundColor(activity.resources.getColor(R.color.you_dialog_background_color))
            }

            dialog_tab_layout.onTabSelectionChanged(tabSelectedAction = {
                viewPager.currentItem = when {
                    it.text.toString().equals(
                        resources.getString(R.string.simple_renaming),
                        true
                    ) -> RENAME_SIMPLE
                    else -> RENAME_PATTERN
                }
            })
        }

        activity.getAlertDialogBuilder()
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel) { _, _ -> dismissDialog() }
            .apply {
                activity.setupDialogStuff(view, this) { alertDialog ->
                    dialog = alertDialog
                    alertDialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        tabsAdapter.dialogConfirmed(useMediaFileExtension, viewPager.currentItem) {
                            dismissDialog()
                            if (it) {
                                activity.baseConfig.lastRenameUsed = viewPager.currentItem
                                callback()
                            }
                        }
                    }
                }
            }
    }

    private fun dismissDialog() {
        dialog?.dismiss()
    }
}
