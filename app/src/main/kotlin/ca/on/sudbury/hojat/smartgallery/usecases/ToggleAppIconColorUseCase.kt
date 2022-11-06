package ca.on.sudbury.hojat.smartgallery.usecases

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import ca.on.sudbury.hojat.smartgallery.extensions.baseConfig
import ca.on.sudbury.hojat.smartgallery.repositories.AppIconRepository

/**
 * Changes the app icon's color.
 */
object ToggleAppIconColorUseCase {
    operator fun invoke(
        owner: Context,
        appId: String,
        colorIndex: Int,
        color: Int,
        enable: Boolean
    ) {
        val className =
            "${appId.removeSuffix(".debug")}.activities.SplashActivity${AppIconRepository.appIconColorStrings[colorIndex]}"
        val state =
            if (enable) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        owner.packageManager.setComponentEnabledSetting(
            ComponentName(appId, className),
            state,
            PackageManager.DONT_KILL_APP
        )
        if (enable) {
            owner.baseConfig.lastIconColor = color
        }
    }
}