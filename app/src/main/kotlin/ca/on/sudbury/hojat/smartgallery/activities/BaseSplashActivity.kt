package ca.on.sudbury.hojat.smartgallery.activities

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ca.on.sudbury.hojat.smartgallery.extensions.baseConfig
import ca.on.sudbury.hojat.smartgallery.extensions.checkAppSideloading
import ca.on.sudbury.hojat.smartgallery.extensions.isUsingSystemDarkTheme
import ca.on.sudbury.hojat.smartgallery.extensions.showSideloadingDialog
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.extensions.getMyContentProviderCursorLoader
import ca.on.sudbury.hojat.smartgallery.extensions.getSharedThemeSync
import ca.on.sudbury.hojat.smartgallery.helpers.SIDELOADING_TRUE
import ca.on.sudbury.hojat.smartgallery.helpers.SIDELOADING_UNCHECKED
import ca.on.sudbury.hojat.smartgallery.models.SharedTheme
import ca.on.sudbury.hojat.smartgallery.usecases.CheckAppIconColorUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.RunOnBackgroundThreadUseCase

abstract class BaseSplashActivity : AppCompatActivity() {
    abstract fun initActivity()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (baseConfig.appSideloadingStatus == SIDELOADING_UNCHECKED) {
            if (checkAppSideloading()) {
                return
            }
        } else if (baseConfig.appSideloadingStatus == SIDELOADING_TRUE) {
            showSideloadingDialog()
            return
        }

        baseConfig.apply {
            if (isUsingAutoTheme) {
                val isUsingSystemDarkTheme = isUsingSystemDarkTheme()
                isUsingSharedTheme = false
                textColor =
                    resources.getColor(if (isUsingSystemDarkTheme) R.color.theme_dark_text_color else R.color.theme_light_text_color)
                backgroundColor =
                    resources.getColor(if (isUsingSystemDarkTheme) R.color.theme_dark_background_color else R.color.theme_light_background_color)
                navigationBarColor = if (isUsingSystemDarkTheme) Color.BLACK else -2
            }
        }

        if (!baseConfig.isUsingAutoTheme && !baseConfig.isUsingSystemTheme) {
            getSharedTheme {
                if (it != null) {
                    baseConfig.apply {
                        wasSharedThemeForced = true
                        isUsingSharedTheme = true
                        wasSharedThemeEverActivated = true

                        textColor = it.textColor
                        backgroundColor = it.backgroundColor
                        primaryColor = it.primaryColor
                        navigationBarColor = it.navigationBarColor
                        accentColor = it.accentColor
                    }

                    if (baseConfig.appIconColor != it.appIconColor) {
                        baseConfig.appIconColor = it.appIconColor
                        CheckAppIconColorUseCase(this)

                    }
                }
                initActivity()
            }
        } else {
            initActivity()
        }
    }

    private fun getSharedTheme(callback: (sharedTheme: SharedTheme?) -> Unit) {
        val cursorLoader = getMyContentProviderCursorLoader()
        RunOnBackgroundThreadUseCase {
            callback(getSharedThemeSync(cursorLoader))
        }
    }

}
