package ca.on.sudbury.hojat.smartgallery.extensions

import android.app.Application
import ca.on.sudbury.hojat.smartgallery.helpers.isNougatPlus
import java.util.Locale

fun Application.checkUseEnglish() {
    if (baseConfig.useEnglish && !isNougatPlus()) {
        val conf = resources.configuration
        conf.locale = Locale.ENGLISH
        resources.updateConfiguration(conf, resources.displayMetrics)
    }
}
