package ca.on.sudbury.hojat.smartgallery

import android.app.Application
import ca.on.sudbury.hojat.smartgallery.extensions.checkUseEnglish
import ca.on.sudbury.hojat.smartgallery.reprint.core.Reprint
import com.squareup.picasso.Downloader
import com.squareup.picasso.Picasso
import okhttp3.Request
import okhttp3.Response
import timber.log.Timber

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        checkUseEnglish()
        Reprint.initialize(this)
        Picasso.setSingletonInstance(Picasso.Builder(this).downloader(object : Downloader {
            override fun load(request: Request) = Response.Builder().build()

            override fun shutdown() {}
        }).build())
        Timber.plant(Timber.DebugTree())
    }
}
