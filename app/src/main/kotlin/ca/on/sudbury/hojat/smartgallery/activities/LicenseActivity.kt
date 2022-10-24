package ca.on.sudbury.hojat.smartgallery.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.widget.LinearLayout
import ca.on.sudbury.hojat.smartgallery.extensions.getContrastColor
import ca.on.sudbury.hojat.smartgallery.extensions.getProperBackgroundColor
import ca.on.sudbury.hojat.smartgallery.extensions.getProperPrimaryColor
import ca.on.sudbury.hojat.smartgallery.extensions.getProperTextColor
import ca.on.sudbury.hojat.smartgallery.extensions.launchViewIntent
import ca.on.sudbury.hojat.smartgallery.extensions.updateTextColors
import ca.on.sudbury.hojat.smartgallery.helpers.APP_ICON_IDS
import ca.on.sudbury.hojat.smartgallery.helpers.APP_LAUNCHER_NAME
import ca.on.sudbury.hojat.smartgallery.helpers.APP_LICENSES
import ca.on.sudbury.hojat.smartgallery.helpers.LICENSE_APNG
import ca.on.sudbury.hojat.smartgallery.helpers.LICENSE_AUDIO_RECORD_VIEW
import ca.on.sudbury.hojat.smartgallery.helpers.LICENSE_AUTOFITTEXTVIEW
import ca.on.sudbury.hojat.smartgallery.helpers.LICENSE_CROPPER
import ca.on.sudbury.hojat.smartgallery.helpers.LICENSE_ESPRESSO
import ca.on.sudbury.hojat.smartgallery.helpers.LICENSE_EVENT_BUS
import ca.on.sudbury.hojat.smartgallery.helpers.LICENSE_EXOPLAYER
import ca.on.sudbury.hojat.smartgallery.helpers.LICENSE_FILTERS
import ca.on.sudbury.hojat.smartgallery.helpers.LICENSE_GESTURE_VIEWS
import ca.on.sudbury.hojat.smartgallery.helpers.LICENSE_GIF_DRAWABLE
import ca.on.sudbury.hojat.smartgallery.helpers.LICENSE_GLIDE
import ca.on.sudbury.hojat.smartgallery.helpers.LICENSE_GSON
import ca.on.sudbury.hojat.smartgallery.helpers.LICENSE_INDICATOR_FAST_SCROLL
import ca.on.sudbury.hojat.smartgallery.helpers.LICENSE_JODA
import ca.on.sudbury.hojat.smartgallery.helpers.LICENSE_KOTLIN
import ca.on.sudbury.hojat.smartgallery.helpers.LICENSE_LEAK_CANARY
import ca.on.sudbury.hojat.smartgallery.helpers.LICENSE_NUMBER_PICKER
import ca.on.sudbury.hojat.smartgallery.helpers.LICENSE_OTTO
import ca.on.sudbury.hojat.smartgallery.helpers.LICENSE_PANORAMA_VIEW
import ca.on.sudbury.hojat.smartgallery.helpers.LICENSE_PATTERN
import ca.on.sudbury.hojat.smartgallery.helpers.LICENSE_PDF_VIEWER
import ca.on.sudbury.hojat.smartgallery.helpers.LICENSE_PHOTOVIEW
import ca.on.sudbury.hojat.smartgallery.helpers.LICENSE_PICASSO
import ca.on.sudbury.hojat.smartgallery.helpers.LICENSE_REPRINT
import ca.on.sudbury.hojat.smartgallery.helpers.LICENSE_ROBOLECTRIC
import ca.on.sudbury.hojat.smartgallery.helpers.LICENSE_RTL
import ca.on.sudbury.hojat.smartgallery.helpers.LICENSE_SANSELAN
import ca.on.sudbury.hojat.smartgallery.helpers.LICENSE_SMS_MMS
import ca.on.sudbury.hojat.smartgallery.helpers.LICENSE_STETHO
import ca.on.sudbury.hojat.smartgallery.helpers.LICENSE_SUBSAMPLING
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.databinding.ActivityLicenseBinding
import ca.on.sudbury.hojat.smartgallery.databinding.ItemLicenseBinding
import ca.on.sudbury.hojat.smartgallery.models.License
import ca.on.sudbury.hojat.smartgallery.usecases.ApplyColorToDrawableUseCase

class LicenseActivity : BaseSimpleActivity() {

    private lateinit var binding: ActivityLicenseBinding

    override fun getAppIconIDs() = intent.getIntegerArrayListExtra(APP_ICON_IDS) ?: ArrayList()
    override fun getAppLauncherName() = intent.getStringExtra(APP_LAUNCHER_NAME) ?: ""

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLicenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dividerMargin = resources.getDimension(R.dimen.medium_margin).toInt()
        val textColor = getProperTextColor()
        val backgroundColor = getProperBackgroundColor()
        val primaryColor = getProperPrimaryColor()

        updateTextColors(binding.licensesHolder)

        val inflater = LayoutInflater.from(this)
        val licenses = initLicenses()
        val licenseMask = intent.getIntExtra(APP_LICENSES, 0) or LICENSE_KOTLIN.toInt()
        licenses.filter { licenseMask and it.id.toInt() != 0 }.forEach { license ->

            ItemLicenseBinding.inflate(inflater).apply {
                ApplyColorToDrawableUseCase(root.background, backgroundColor.getContrastColor())
                licenseTitle.apply {
                    text = getString(license.titleId)
                    setTextColor(primaryColor)
                    setOnClickListener {
                        launchViewIntent(license.urlId)
                    }
                }

                licenseText.apply {
                    text = getString(license.textId)
                    setTextColor(textColor)
                }

                binding.licensesHolder.addView(root)
                (root.layoutParams as LinearLayout.LayoutParams).bottomMargin = dividerMargin
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        updateMenuItemColors(menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun initLicenses() = arrayOf(
        License(LICENSE_KOTLIN, R.string.kotlin_title, R.string.kotlin_text, R.string.kotlin_url),
        License(
            LICENSE_SUBSAMPLING,
            R.string.subsampling_title,
            R.string.subsampling_text,
            R.string.subsampling_url
        ),
        License(LICENSE_GLIDE, R.string.glide_title, R.string.glide_text, R.string.glide_url),
        License(
            LICENSE_CROPPER,
            R.string.cropper_title,
            R.string.cropper_text,
            R.string.cropper_url
        ),
        License(
            LICENSE_RTL,
            R.string.rtl_viewpager_title,
            R.string.rtl_viewpager_text,
            R.string.rtl_viewpager_url
        ),
        License(LICENSE_JODA, R.string.joda_title, R.string.joda_text, R.string.joda_url),
        License(LICENSE_STETHO, R.string.stetho_title, R.string.stetho_text, R.string.stetho_url),
        License(LICENSE_OTTO, R.string.otto_title, R.string.otto_text, R.string.otto_url),
        License(
            LICENSE_PHOTOVIEW,
            R.string.photoview_title,
            R.string.photoview_text,
            R.string.photoview_url
        ),
        License(
            LICENSE_PICASSO,
            R.string.picasso_title,
            R.string.picasso_text,
            R.string.picasso_url
        ),
        License(
            LICENSE_PATTERN,
            R.string.pattern_title,
            R.string.pattern_text,
            R.string.pattern_url
        ),
        License(
            LICENSE_REPRINT,
            R.string.reprint_title,
            R.string.reprint_text,
            R.string.reprint_url
        ),
        License(
            LICENSE_GIF_DRAWABLE,
            R.string.gif_drawable_title,
            R.string.gif_drawable_text,
            R.string.gif_drawable_url
        ),
        License(
            LICENSE_AUTOFITTEXTVIEW,
            R.string.autofittextview_title,
            R.string.autofittextview_text,
            R.string.autofittextview_url
        ),
        License(
            LICENSE_ROBOLECTRIC,
            R.string.robolectric_title,
            R.string.robolectric_text,
            R.string.robolectric_url
        ),
        License(
            LICENSE_ESPRESSO,
            R.string.espresso_title,
            R.string.espresso_text,
            R.string.espresso_url
        ),
        License(LICENSE_GSON, R.string.gson_title, R.string.gson_text, R.string.gson_url),
        License(
            LICENSE_LEAK_CANARY,
            R.string.leak_canary_title,
            R.string.leakcanary_text,
            R.string.leakcanary_url
        ),
        License(
            LICENSE_NUMBER_PICKER,
            R.string.number_picker_title,
            R.string.number_picker_text,
            R.string.number_picker_url
        ),
        License(
            LICENSE_EXOPLAYER,
            R.string.exoplayer_title,
            R.string.exoplayer_text,
            R.string.exoplayer_url
        ),
        License(
            LICENSE_PANORAMA_VIEW,
            R.string.panorama_view_title,
            R.string.panorama_view_text,
            R.string.panorama_view_url
        ),
        License(
            LICENSE_SANSELAN,
            R.string.sanselan_title,
            R.string.sanselan_text,
            R.string.sanselan_url
        ),
        License(
            LICENSE_FILTERS,
            R.string.filters_title,
            R.string.filters_text,
            R.string.filters_url
        ),
        License(
            LICENSE_GESTURE_VIEWS,
            R.string.gesture_views_title,
            R.string.gesture_views_text,
            R.string.gesture_views_url
        ),
        License(
            LICENSE_INDICATOR_FAST_SCROLL,
            R.string.indicator_fast_scroll_title,
            R.string.indicator_fast_scroll_text,
            R.string.indicator_fast_scroll_url
        ),
        License(
            LICENSE_EVENT_BUS,
            R.string.event_bus_title,
            R.string.event_bus_text,
            R.string.event_bus_url
        ),
        License(
            LICENSE_AUDIO_RECORD_VIEW,
            R.string.audio_record_view_title,
            R.string.audio_record_view_text,
            R.string.audio_record_view_url
        ),
        License(
            LICENSE_SMS_MMS,
            R.string.sms_mms_title,
            R.string.sms_mms_text,
            R.string.sms_mms_url
        ),
        License(LICENSE_APNG, R.string.apng_title, R.string.apng_text, R.string.apng_url),
        License(
            LICENSE_PDF_VIEWER,
            R.string.pdf_viewer_title,
            R.string.pdf_viewer_text,
            R.string.pdf_viewer_url
        )
    )
}
