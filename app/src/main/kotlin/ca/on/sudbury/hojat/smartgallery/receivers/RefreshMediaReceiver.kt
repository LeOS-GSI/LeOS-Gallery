package ca.on.sudbury.hojat.smartgallery.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ca.on.sudbury.hojat.smartgallery.helpers.REFRESH_PATH
import ca.on.sudbury.hojat.smartgallery.extensions.addPathToDB

class RefreshMediaReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val path = intent.getStringExtra(REFRESH_PATH) ?: return
        context.addPathToDB(path)
    }
}
