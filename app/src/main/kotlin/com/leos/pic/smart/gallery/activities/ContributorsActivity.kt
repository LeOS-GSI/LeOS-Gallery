package ca.on.sudbury.hojat.smartgallery.activities

import android.os.Bundle
import android.view.Menu
import ca.on.sudbury.hojat.smartgallery.databinding.ActivityContributorsBinding
import ca.on.sudbury.hojat.smartgallery.extensions.getContrastColor
import ca.on.sudbury.hojat.smartgallery.extensions.getProperBackgroundColor
import ca.on.sudbury.hojat.smartgallery.extensions.getProperPrimaryColor
import ca.on.sudbury.hojat.smartgallery.extensions.getProperTextColor
import ca.on.sudbury.hojat.smartgallery.extensions.updateTextColors
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

        ApplyColorFilterUseCase(binding.contributorsDevelopmentIcon, textColor)

        arrayOf(
            binding.contributorsDevelopmentHolder,
            binding.contributorsTranslationHolder
        ).forEach {
            ApplyColorFilterUseCase(it.background, backgroundColor.getContrastColor())
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        updateMenuItemColors(menu)
        return super.onCreateOptionsMenu(menu)
    }
}
