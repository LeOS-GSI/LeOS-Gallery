package ca.on.sudbury.hojat.smartgallery.activities

import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.View
import ca.on.sudbury.hojat.smartgallery.extensions.getContrastColor
import ca.on.sudbury.hojat.smartgallery.extensions.getProperBackgroundColor
import ca.on.sudbury.hojat.smartgallery.extensions.getProperPrimaryColor
import ca.on.sudbury.hojat.smartgallery.extensions.getProperTextColor
import ca.on.sudbury.hojat.smartgallery.extensions.removeUnderlines
import ca.on.sudbury.hojat.smartgallery.extensions.updateTextColors
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.databinding.ActivityContributorsBinding
import ca.on.sudbury.hojat.smartgallery.helpers.APP_ICON_IDS
import ca.on.sudbury.hojat.smartgallery.helpers.APP_LAUNCHER_NAME
import ca.on.sudbury.hojat.smartgallery.usecases.ApplyColorFilterUseCase

class ContributorsActivity : BaseSimpleActivity() {

    private lateinit var binding: ActivityContributorsBinding

    override fun getAppIconIDs() = intent.getIntegerArrayListExtra(APP_ICON_IDS) ?: ArrayList()
    override fun getAppLauncherName() = intent.getStringExtra(APP_LAUNCHER_NAME) ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContributorsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val textColor = getProperTextColor()
        val backgroundColor = getProperBackgroundColor()
        val primaryColor = getProperPrimaryColor()

        updateTextColors(binding.contributorsHolder)
        binding.contributorsDevelopmentLabel.setTextColor(primaryColor)
        binding.contributorsTranslationLabel.setTextColor(primaryColor)

        binding.contributorsLabel.apply {
            setTextColor(textColor)
            text = Html.fromHtml(getString(R.string.contributors_label))
            setLinkTextColor(primaryColor)
            movementMethod = LinkMovementMethod.getInstance()
            removeUnderlines()
        }

        ApplyColorFilterUseCase(binding.contributorsDevelopmentIcon, textColor)
        ApplyColorFilterUseCase(binding.contributorsFooterIcon, textColor)

        arrayOf(
            binding.contributorsDevelopmentHolder,
            binding.contributorsTranslationHolder
        ).forEach {
            ApplyColorFilterUseCase(it.background, backgroundColor.getContrastColor())
        }

        if (resources.getBoolean(R.bool.hide_all_external_links)) {
            binding.contributorsFooterIcon.visibility = View.GONE
            binding.contributorsLabel.visibility = View.GONE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        updateMenuItemColors(menu)
        return super.onCreateOptionsMenu(menu)
    }
}
