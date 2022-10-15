package ca.on.sudbury.hojat.smartgallery.receivers

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import ca.on.sudbury.hojat.smartgallery.helpers.ensureBackgroundThread
import ca.on.sudbury.hojat.smartgallery.extensions.updateDirectoryPath
import ca.on.sudbury.hojat.smartgallery.helpers.MediaFetcher

@SuppressLint("UnsafeProtectedBroadcastReceiver")
class BootCompletedReceiver : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onReceive(context: Context, intent: Intent) {
        ensureBackgroundThread {
            MediaFetcher(context).getFoldersToScan().forEach {
                context.updateDirectoryPath(it)
            }
        }
    }
}
