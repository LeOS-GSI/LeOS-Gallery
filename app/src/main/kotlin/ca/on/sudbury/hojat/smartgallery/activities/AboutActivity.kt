package ca.on.sudbury.hojat.smartgallery.activities


import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.Intent.ACTION_SEND
import android.content.Intent.ACTION_SENDTO
import android.content.Intent.EXTRA_EMAIL
import android.content.Intent.EXTRA_SUBJECT
import android.content.Intent.EXTRA_TEXT
import android.content.Intent.createChooser
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import androidx.core.net.toUri
import ca.on.sudbury.hojat.smartgallery.extensions.applyColorFilter
import ca.on.sudbury.hojat.smartgallery.extensions.baseConfig
import ca.on.sudbury.hojat.smartgallery.extensions.beGone
import ca.on.sudbury.hojat.smartgallery.extensions.beVisible
import ca.on.sudbury.hojat.smartgallery.extensions.beVisibleIf
import ca.on.sudbury.hojat.smartgallery.extensions.getContrastColor
import ca.on.sudbury.hojat.smartgallery.extensions.getProperBackgroundColor
import ca.on.sudbury.hojat.smartgallery.extensions.getProperPrimaryColor
import ca.on.sudbury.hojat.smartgallery.extensions.getProperTextColor
import ca.on.sudbury.hojat.smartgallery.extensions.getStoreUrl
import ca.on.sudbury.hojat.smartgallery.extensions.isGone
import ca.on.sudbury.hojat.smartgallery.extensions.launchViewIntent
import ca.on.sudbury.hojat.smartgallery.extensions.redirectToRateUs
import ca.on.sudbury.hojat.smartgallery.extensions.showErrorToast
import ca.on.sudbury.hojat.smartgallery.extensions.toast
import ca.on.sudbury.hojat.smartgallery.extensions.updateTextColors
import ca.on.sudbury.hojat.smartgallery.helpers.APP_FAQ
import ca.on.sudbury.hojat.smartgallery.helpers.APP_ICON_IDS
import ca.on.sudbury.hojat.smartgallery.helpers.APP_LAUNCHER_NAME
import ca.on.sudbury.hojat.smartgallery.helpers.APP_LICENSES
import ca.on.sudbury.hojat.smartgallery.helpers.APP_NAME
import ca.on.sudbury.hojat.smartgallery.helpers.APP_VERSION_NAME
import ca.on.sudbury.hojat.smartgallery.helpers.SHOW_FAQ_BEFORE_MAIL
import ca.on.sudbury.hojat.smartgallery.models.FaqItem
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.dialogs.ConfirmationAdvancedDialog
import ca.on.sudbury.hojat.smartgallery.dialogs.RateStarsDialog
import kotlinx.android.synthetic.main.activity_about.*

class AboutActivity : BaseSimpleActivity() {
    private var appName = ""
    private var primaryColor = 0

    private var firstVersionClickTS = 0L
    private var clicksSinceFirstClick = 0
    private val EASTER_EGG_TIME_LIMIT = 3000L
    private val EASTER_EGG_REQUIRED_CLICKS = 7

    override fun getAppIconIDs() = intent.getIntegerArrayListExtra(APP_ICON_IDS) ?: ArrayList()

    override fun getAppLauncherName() = intent.getStringExtra(APP_LAUNCHER_NAME) ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        appName = intent.getStringExtra(APP_NAME) ?: ""
        val textColor = getProperTextColor()
        val backgroundColor = getProperBackgroundColor()
        primaryColor = getProperPrimaryColor()

        arrayOf(
            about_faq_icon,
            about_rate_us_icon,
            about_donate_icon,
            about_invite_icon,
            about_contributors_icon,
            about_more_apps_icon,
            about_email_icon,
            about_privacy_policy_icon,
            about_licenses_icon,
            about_website_icon,
            about_version_icon
        ).forEach {
            it.applyColorFilter(textColor)
        }

        arrayOf(about_support, about_help_us, about_social, about_other).forEach {
            it.setTextColor(primaryColor)
        }

        arrayOf(
            about_support_holder,
            about_help_us_holder,
            about_social_holder,
            about_other_holder
        ).forEach {
            it.background.applyColorFilter(backgroundColor.getContrastColor())
        }
    }

    override fun onResume() {
        super.onResume()
        updateTextColors(about_scrollview)

        setupFAQ()
        setupEmail()
        setupRateUs()
        setupInvite()
        setupContributors()
        setupDonate()
        setupFacebook()
        setupReddit()
        setupMoreApps()
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
        about_faq_holder.beVisibleIf(faqItems.isNotEmpty())
        about_faq_holder.setOnClickListener {
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
        if (about_faq_holder.isGone()) {
            about_email_holder.background =
                resources.getDrawable(R.drawable.ripple_all_corners, theme)
        }

        if (resources.getBoolean(R.bool.hide_all_external_links)) {
            about_email_holder.beGone()

            if (about_faq_holder.isGone()) {
                about_support.beGone()
                about_support_holder.beGone()
            } else {
                about_faq_holder.background =
                    resources.getDrawable(R.drawable.ripple_all_corners, theme)
            }
        }

        about_email_holder.setOnClickListener {
            val msg =
                "${getString(R.string.before_asking_question_read_faq)}\n\n${getString(R.string.make_sure_latest)}"
            if (intent.getBooleanExtra(
                    SHOW_FAQ_BEFORE_MAIL,
                    false
                ) && !baseConfig.wasBeforeAskingShown
            ) {
                baseConfig.wasBeforeAskingShown = true
                ConfirmationAdvancedDialog(
                    this,
                    msg,
                    0,
                    R.string.read_faq,
                    R.string.skip
                ) { success ->
                    if (success) {
                        about_faq_holder.performClick()
                    } else {
                        about_email_holder.performClick()
                    }
                }
            } else {
                val appVersion = String.format(
                    getString(
                        R.string.app_version,
                        intent.getStringExtra(APP_VERSION_NAME)
                    )
                )
                val deviceOS = String.format(getString(R.string.device_os), Build.VERSION.RELEASE)
                val newline = "\n"
                val separator = "------------------------------"
                val body = "$appVersion$newline$deviceOS$newline$separator$newline$newline"

                val address = getString(R.string.my_email)
                val selectorIntent = Intent(ACTION_SENDTO)
                    .setData("mailto:$address".toUri())
                val emailIntent = Intent(ACTION_SEND).apply {
                    putExtra(EXTRA_EMAIL, arrayOf(address))
                    putExtra(EXTRA_SUBJECT, appName)
                    putExtra(EXTRA_TEXT, body)
                    selector = selectorIntent
                }

                try {
                    startActivity(emailIntent)
                } catch (e: ActivityNotFoundException) {
                    toast(R.string.no_app_found)
                } catch (e: Exception) {
                    showErrorToast(e)
                }
            }
        }
    }

    private fun setupRateUs() {
        if (resources.getBoolean(R.bool.hide_google_relations)) {
            about_rate_us_holder.beGone()
        }

        about_rate_us_holder.setOnClickListener {
            if (baseConfig.wasBeforeRateShown) {
                if (baseConfig.wasAppRated) {
                    redirectToRateUs()
                } else {
                    RateStarsDialog(this)
                }
            } else {
                baseConfig.wasBeforeRateShown = true
                val msg =
                    "${getString(R.string.before_rate_read_faq)}\n\n${getString(R.string.make_sure_latest)}"
                ConfirmationAdvancedDialog(
                    this,
                    msg,
                    0,
                    R.string.read_faq,
                    R.string.skip
                ) { success ->
                    if (success) {
                        about_faq_holder.performClick()
                    } else {
                        about_rate_us_holder.performClick()
                    }
                }
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setupInvite() {
        if (resources.getBoolean(R.bool.hide_google_relations)) {
            about_invite_holder.beGone()
        } else if (about_rate_us_holder.isGone()) {
            about_invite_holder.background =
                resources.getDrawable(R.drawable.ripple_top_corners, theme)
        }

        about_invite_holder.setOnClickListener {
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
        if (about_rate_us_holder.isGone() && about_invite_holder.isGone()) {
            about_contributors_holder.background =
                resources.getDrawable(R.drawable.ripple_all_corners, theme)
        }

        about_contributors_holder.setOnClickListener {
            val intent = Intent(applicationContext, ContributorsActivity::class.java)
            startActivity(intent)
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setupDonate() {
        if (resources.getBoolean(R.bool.show_donate_in_about) && !resources.getBoolean(R.bool.hide_all_external_links)) {
            about_donate_holder.beVisible()

            val contributorsBg =
                if (about_rate_us_holder.isGone() && about_invite_holder.isGone()) {
                    R.drawable.ripple_top_corners
                } else {
                    R.drawable.ripple_background
                }

            about_contributors_holder.background = resources.getDrawable(contributorsBg, theme)
            about_donate_holder.setOnClickListener {
                launchViewIntent("https://simplemobiletools.com/donate")
            }
        } else {
            about_donate_holder.beGone()
        }
    }

    private fun setupFacebook() {
        if (resources.getBoolean(R.bool.hide_all_external_links)) {
            about_social.beGone()
            about_social_holder.beGone()
        }

        about_facebook_holder.setOnClickListener {
            var link = "https://www.facebook.com/simplemobiletools"
            try {
                packageManager.getPackageInfo("com.facebook.katana", 0)
                link = "fb://page/150270895341774"
            } catch (ignored: Exception) {
            }

            launchViewIntent(link)
        }
    }

    private fun setupReddit() {
        about_reddit_holder.setOnClickListener {
            launchViewIntent("https://www.reddit.com/r/SimpleMobileTools")
        }
    }

    private fun setupMoreApps() {
        if (resources.getBoolean(R.bool.hide_google_relations)) {
            about_more_apps_holder.beGone()
        }

        about_more_apps_holder.setOnClickListener {
            launchViewIntent("https://play.google.com/store/apps/dev?id=9070296388022589266")
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setupWebsite() {
        if (resources.getBoolean(R.bool.show_donate_in_about) && !resources.getBoolean(R.bool.hide_all_external_links)) {
            if (about_more_apps_holder.isGone()) {
                about_website_holder.background =
                    resources.getDrawable(R.drawable.ripple_top_corners, theme)
            }

            about_website_holder.beVisible()
            about_website_holder.setOnClickListener {
                launchViewIntent("https://simplemobiletools.com/")
            }
        } else {
            about_website_holder.beGone()
        }
    }

    private fun setupPrivacyPolicy() {
        if (resources.getBoolean(R.bool.hide_all_external_links)) {
            about_privacy_policy_holder.beGone()
        }

        about_privacy_policy_holder.setOnClickListener {
            val appId = baseConfig.appId.removeSuffix(".debug").removeSuffix(".pro")
                .removePrefix("com.simplemobiletools.")
            val url = "https://simplemobiletools.com/privacy/$appId.txt"
            launchViewIntent(url)
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setupLicense() {
        if (about_website_holder.isGone() && about_more_apps_holder.isGone() && about_privacy_policy_holder.isGone()) {
            about_licenses_holder.background =
                resources.getDrawable(R.drawable.ripple_top_corners, theme)
        }

        about_licenses_holder.setOnClickListener {
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
        about_version.text = fullVersion
        about_version_holder.setOnClickListener {
            if (firstVersionClickTS == 0L) {
                firstVersionClickTS = System.currentTimeMillis()
                Handler().postDelayed({
                    firstVersionClickTS = 0L
                    clicksSinceFirstClick = 0
                }, EASTER_EGG_TIME_LIMIT)
            }

            clicksSinceFirstClick++
            if (clicksSinceFirstClick >= EASTER_EGG_REQUIRED_CLICKS) {
                toast(R.string.hello)
                firstVersionClickTS = 0L
                clicksSinceFirstClick = 0
            }
        }
    }
}
