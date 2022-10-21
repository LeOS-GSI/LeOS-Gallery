package ca.on.sudbury.hojat.smartgallery.extensions

import android.app.Application
import ca.on.sudbury.hojat.smartgallery.photoedit.usecases.IsNougatPlusUseCase
import java.util.Locale

fun Application.checkUseEnglish() {
    if (baseConfig.useEnglish && !IsNougatPlusUseCase()) {
        val conf = resources.configuration
        conf.locale = Locale.ENGLISH
        resources.updateConfiguration(conf, resources.displayMetrics)
    }
}
