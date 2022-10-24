package ca.on.sudbury.hojat.smartgallery.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.Menu
import android.widget.LinearLayout
import ca.on.sudbury.hojat.smartgallery.extensions.getContrastColor
import ca.on.sudbury.hojat.smartgallery.extensions.getProperBackgroundColor
import ca.on.sudbury.hojat.smartgallery.extensions.getProperPrimaryColor
import ca.on.sudbury.hojat.smartgallery.extensions.getProperTextColor
import ca.on.sudbury.hojat.smartgallery.extensions.removeUnderlines
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.databinding.ActivityFaqBinding
import ca.on.sudbury.hojat.smartgallery.databinding.ItemFaqBinding
import ca.on.sudbury.hojat.smartgallery.helpers.APP_FAQ
import ca.on.sudbury.hojat.smartgallery.helpers.APP_ICON_IDS
import ca.on.sudbury.hojat.smartgallery.helpers.APP_LAUNCHER_NAME
import ca.on.sudbury.hojat.smartgallery.models.FaqItem
import ca.on.sudbury.hojat.smartgallery.usecases.ApplyColorToDrawableUseCase

class FAQActivity : BaseSimpleActivity() {

    private lateinit var activityBinding: ActivityFaqBinding

    override fun getAppIconIDs() = intent.getIntegerArrayListExtra(APP_ICON_IDS) ?: ArrayList()
    override fun getAppLauncherName() = intent.getStringExtra(APP_LAUNCHER_NAME) ?: ""

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityBinding = ActivityFaqBinding.inflate(layoutInflater)
        setContentView(activityBinding.root)

        val dividerMargin = resources.getDimension(R.dimen.medium_margin).toInt()
        val titleColor = getProperPrimaryColor()
        val backgroundColor = getProperBackgroundColor()
        val textColor = getProperTextColor()

        val inflater = LayoutInflater.from(this)
        val faqItems = intent.getSerializableExtra(APP_FAQ) as ArrayList<FaqItem>

        faqItems.forEach { faqItem ->

            ItemFaqBinding.inflate(inflater).apply {
                ApplyColorToDrawableUseCase(root.background, backgroundColor.getContrastColor())
                this.faqTitle.apply {
                    text =
                        if (faqItem.title is Int) getString(faqItem.title) else faqItem.title as String
                    setTextColor(titleColor)
                }

                faqText.apply {
                    text =
                        if (faqItem.text is Int) Html.fromHtml(getString(faqItem.text)) else faqItem.text as String
                    setTextColor(textColor)
                    setLinkTextColor(titleColor)

                    movementMethod = LinkMovementMethod.getInstance()
                    removeUnderlines()
                }
                activityBinding.faqHolder.addView(root)
                (root.layoutParams as LinearLayout.LayoutParams).bottomMargin = dividerMargin
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        updateMenuItemColors(menu)
        return super.onCreateOptionsMenu(menu)
    }
}
