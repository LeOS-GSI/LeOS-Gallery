package ca.on.sudbury.hojat.smartgallery.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.content.Intent.EXTRA_SUBJECT
import android.content.Intent.EXTRA_TEXT
import android.content.Intent.createChooser
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.View
import android.widget.Toast
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.databinding.ActivityAboutBinding
import ca.on.sudbury.hojat.smartgallery.dialogs.ConfirmationAdvancedDialogFragment
import ca.on.sudbury.hojat.smartgallery.dialogs.RateStarsDialogFragment
import ca.on.sudbury.hojat.smartgallery.extensions.baseConfig
import ca.on.sudbury.hojat.smartgallery.extensions.getContrastColor
import ca.on.sudbury.hojat.smartgallery.extensions.getProperBackgroundColor
import ca.on.sudbury.hojat.smartgallery.extensions.getProperPrimaryColor
import ca.on.sudbury.hojat.smartgallery.extensions.getProperTextColor
import ca.on.sudbury.hojat.smartgallery.extensions.getStoreUrl
import ca.on.sudbury.hojat.smartgallery.extensions.launchViewIntent
import ca.on.sudbury.hojat.smartgallery.extensions.redirectToRateUs
import ca.on.sudbury.hojat.smartgallery.extensions.updateTextColors
import ca.on.sudbury.hojat.smartgallery.helpers.APP_FAQ
import ca.on.sudbury.hojat.smartgallery.helpers.APP_ICON_IDS
import ca.on.sudbury.hojat.smartgallery.helpers.APP_LAUNCHER_NAME
import ca.on.sudbury.hojat.smartgallery.helpers.APP_LICENSES
import ca.on.sudbury.hojat.smartgallery.helpers.APP_NAME
import ca.on.sudbury.hojat.smartgallery.helpers.APP_VERSION_NAME
import ca.on.sudbury.hojat.smartgallery.models.FaqItem
import ca.on.sudbury.hojat.smartgallery.usecases.ApplyColorFilterUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.BeVisibleOrGoneUseCase

class AboutActivity : BaseSimpleActivity() {

    private lateinit var binding: ActivityAboutBinding
    private var appName = ""
    private var primaryColor = 0
    private var firstVersionClickTS = 0L
    private var clicksSinceFirstClick = 0
    private val easterEggTimeLimit = 3000L
    private val easterEggRequiredClicks = 7

    override fun getAppIconIDs() = intent.getIntegerArrayListExtra(APP_ICON_IDS) ?: ArrayList()

    override fun getAppLauncherName() = intent.getStringExtra(APP_LAUNCHER_NAME) ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)

        setContentView(binding.root)
        appName = intent.getStringExtra(APP_NAME) ?: ""
        val textColor = getProperTextColor()
        val backgroundColor = getProperBackgroundColor()
        primaryColor = getProperPrimaryColor()

        arrayOf(
            binding.aboutFaqIcon,
            binding.aboutDonateIcon,
            binding.aboutInviteIcon,
            binding.aboutContributorsIcon,
            binding.aboutEmailIcon,
            binding.aboutPrivacyPolicyIcon,
            binding.aboutLicensesIcon,
            binding.aboutWebsiteIcon,
            binding.aboutVersionIcon
        ).forEach { imageview ->
            ApplyColorFilterUseCase(imageview, textColor)
        }

        arrayOf(
            binding.aboutSupport,
            binding.aboutHelpUs,
            binding.aboutOther
        ).forEach { textView ->
            textView.setTextColor(primaryColor)
        }

        arrayOf(
            binding.aboutSupportHolder,
            binding.aboutHelpUsHolder,
            binding.aboutSocialHolder,
            binding.aboutOtherHolder
        ).forEach { linearLayout ->
            ApplyColorFilterUseCase(linearLayout.background, backgroundColor.getContrastColor())

        }
    }

    override fun onResume() {
        super.onResume()
        updateTextColors(binding.aboutScrollview)

        setupFAQ()
        setupEmail()
        setupRateUs()
        setupInvite()
        setupContributors()
        setupDonate()
        setupWebsite()
        setupPrivacyPolicy()
        setupLicense()
        setupVersion()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        updateMenuItemColors(menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun setupFAQ() {
        val faqItems = intent.getSerializableExtra(APP_FAQ) as ArrayList<FaqItem>
        BeVisibleOrGoneUseCase(binding.aboutFaqHolder, faqItems.isNotEmpty())
        binding.aboutFaqHolder.setOnClickListener {
            Intent(applicationContext, FAQActivity::class.java).apply {
                putExtra(APP_ICON_IDS, getAppIconIDs())
                putExtra(APP_LAUNCHER_NAME, getAppLauncherName())
                putExtra(APP_FAQ, faqItems)
                startActivity(this)
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setupEmail() {
        if (binding.aboutFaqHolder.visibility == View.GONE) {
            binding.aboutEmailHolder.background =
                resources.getDrawable(R.drawable.ripple_all_corners, theme)
        }

        if (resources.getBoolean(R.bool.hide_all_external_links)) {
            binding.aboutEmailHolder.visibility = View.GONE

            if (binding.aboutFaqHolder.visibility == View.GONE) {
                binding.aboutSupport.visibility = View.GONE
                binding.aboutSupportHolder.visibility = View.GONE
            } else {
                binding.aboutFaqHolder.background =
                    resources.getDrawable(R.drawable.ripple_all_corners, theme)
            }
        }

        binding.aboutEmailHolder.setOnClickListener {
            Toast.makeText(this, "This feature isn't implemented yet!", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupRateUs() {
        if (resources.getBoolean(R.bool.hide_google_relations)) {
            binding.aboutRateUsHolder.visibility = View.GONE
        }

        binding.aboutRateUsHolder.setOnClickListener {
            if (baseConfig.wasBeforeRateShown) {
                if (baseConfig.wasAppRated) {
                    redirectToRateUs()
                } else {
                    RateStarsDialogFragment().show(
                        supportFragmentManager,
                        RateStarsDialogFragment.TAG
                    )
                }
            } else {
                baseConfig.wasBeforeRateShown = true
                val msg =
                    "${getString(R.string.before_rate_read_faq)}\n\n${getString(R.string.make_sure_latest)}"
                val callback: (Boolean) -> Unit = { result ->
                    if (result) {
                        binding.aboutFaqHolder.performClick()
                    } else {
                        binding.aboutRateUsHolder.performClick()
                    }
                }
                ConfirmationAdvancedDialogFragment(
                    message = msg,
                    messageId = 0,
                    positive = R.string.read_faq,
                    negative = R.string.skip,
                    callback = callback
                ).show(supportFragmentManager, ConfirmationAdvancedDialogFragment.TAG)
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setupInvite() {
        if (resources.getBoolean(R.bool.hide_google_relations)) {
            binding.aboutInviteHolder.visibility = View.GONE
        } else if (binding.aboutRateUsHolder.visibility == View.GONE) {
            binding.aboutInviteHolder.background =
                resources.getDrawable(R.drawable.ripple_top_corners, theme)
        }

        binding.aboutInviteHolder.setOnClickListener {
            val text = String.format(getString(R.string.share_text), appName, getStoreUrl())
            Intent().apply {
                action = ACTION_SEND
                putExtra(EXTRA_SUBJECT, appName)
                putExtra(EXTRA_TEXT, text)
                type = "text/plain"
                startActivity(createChooser(this, getString(R.string.invite_via)))
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setupContributors() {
        if (binding.aboutRateUsHolder.visibility == View.GONE && binding.aboutInviteHolder.visibility == View.GONE) {
            binding.aboutContributorsHolder.background =
                resources.getDrawable(R.drawable.ripple_all_corners, theme)
        }

        binding.aboutContributorsHolder.setOnClickListener {
            val intent = Intent(applicationContext, ContributorsActivity::class.java)
            startActivity(intent)
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setupDonate() {
        if (resources.getBoolean(R.bool.show_donate_in_about) && !resources.getBoolean(R.bool.hide_all_external_links)) {
            binding.aboutDonateHolder.visibility = View.VISIBLE

            val contributorsBg =
                if (binding.aboutRateUsHolder.visibility == View.GONE && binding.aboutInviteHolder.visibility == View.GONE) {
                    R.drawable.ripple_top_corners
                } else {
                    R.drawable.ripple_background
                }

            binding.aboutContributorsHolder.background =
                resources.getDrawable(contributorsBg, theme)
            binding.aboutDonateHolder.setOnClickListener {
                launchViewIntent("https://simplemobiletools.com/donate")
            }
        } else {
            binding.aboutDonateHolder.visibility = View.GONE
        }
    }



    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setupWebsite() {
        if (resources.getBoolean(R.bool.show_donate_in_about) && !resources.getBoolean(R.bool.hide_all_external_links)) {
            if (binding.aboutMoreAppsHolder.visibility == View.GONE) {
                binding.aboutWebsiteHolder.background =
                    resources.getDrawable(R.drawable.ripple_top_corners, theme)
            }

            binding.aboutWebsiteHolder.visibility = View.VISIBLE
            binding.aboutWebsiteHolder.setOnClickListener {
                launchViewIntent("https://simplemobiletools.com/")
            }
        } else {
            binding.aboutWebsiteHolder.visibility = View.GONE
        }
    }

    private fun setupPrivacyPolicy() {
        if (resources.getBoolean(R.bool.hide_all_external_links)) {
            binding.aboutPrivacyPolicyHolder.visibility = View.GONE
        }

        binding.aboutPrivacyPolicyHolder.setOnClickListener {
            val appId = baseConfig.appId.removeSuffix(".debug").removeSuffix(".pro")
                .removePrefix("com.simplemobiletools.")
            val url = "https://simplemobiletools.com/privacy/$appId.txt"
            launchViewIntent(url)
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setupLicense() {
        if (binding.aboutWebsiteHolder.visibility == View.GONE && binding.aboutMoreAppsHolder.visibility == View.GONE && binding.aboutPrivacyPolicyHolder.visibility == View.GONE) {
            binding.aboutLicensesHolder.background =
                resources.getDrawable(R.drawable.ripple_top_corners, theme)
        }

        binding.aboutLicensesHolder.setOnClickListener {
            Intent(applicationContext, LicenseActivity::class.java).apply {
                putExtra(APP_ICON_IDS, getAppIconIDs())
                putExtra(APP_LAUNCHER_NAME, getAppLauncherName())
                putExtra(APP_LICENSES, intent.getIntExtra(APP_LICENSES, 0))
                startActivity(this)
            }
        }
    }

    /**
     * used to ascertain versions of the app (free, pro, and so on)
     */
    private fun setupVersion() {
        val version = intent.getStringExtra(APP_VERSION_NAME) ?: ""

        val fullVersion = String.format(getString(R.string.version_placeholder, version))
        binding.aboutVersion.text = fullVersion
        binding.aboutVersionHolder.setOnClickListener {
            if (firstVersionClickTS == 0L) {
                firstVersionClickTS = System.currentTimeMillis()
                Handler().postDelayed({
                    firstVersionClickTS = 0L
                    clicksSinceFirstClick = 0
                }, easterEggTimeLimit)
            }

            clicksSinceFirstClick++
            if (clicksSinceFirstClick >= easterEggRequiredClicks) {
                Toast.makeText(this, R.string.hello, Toast.LENGTH_LONG).show()
                firstVersionClickTS = 0L
                clicksSinceFirstClick = 0
            }
        }
    }
}
