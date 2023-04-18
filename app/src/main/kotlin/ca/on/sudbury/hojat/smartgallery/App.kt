package ca.on.sudbury.hojat.smartgallery

import android.app.Application
import ca.on.hojat.fingerprint.core.Reprint
import ca.on.sudbury.hojat.smartgallery.extensions.baseConfig
import ca.on.sudbury.hojat.smartgallery.usecases.IsNougatPlusUseCase
import com.squareup.picasso.Downloader
import com.squareup.picasso.Picasso
import java.util.Locale
import okhttp3.Request
import okhttp3.Response
import timber.log.Timber

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        // Check if the device is using English
        if (baseConfig.useEnglish && !IsNougatPlusUseCase()) {
            val conf = resources.configuration
            conf.locale = Locale.ENGLISH
            resources.updateConfiguration(conf, resources.displayMetrics)
        }


        Reprint.initialize(this)
        Picasso.setSingletonInstance(Picasso.Builder(this).downloader(object : Downloader {
            override fun load(request: Request) = Response.Builder().build()

            override fun shutdown() {}
        }).build())

        // Plant the Timber
        Timber.plant(Timber.DebugTree())
    }
}
