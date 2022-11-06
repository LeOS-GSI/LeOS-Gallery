package ca.on.sudbury.hojat.smartgallery.helpers

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.activities.MediaActivity
import ca.on.sudbury.hojat.smartgallery.databases.GalleryDatabase
import ca.on.sudbury.hojat.smartgallery.extensions.config
import ca.on.sudbury.hojat.smartgallery.extensions.getFileKey
import ca.on.sudbury.hojat.smartgallery.extensions.getFolderNameFromPath
import ca.on.sudbury.hojat.smartgallery.extensions.widgetsDB
import ca.on.sudbury.hojat.smartgallery.models.Widget
import ca.on.sudbury.hojat.smartgallery.usecases.RunOnBackgroundThreadUseCase
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey

class SmartGalleryWidgetProvider : AppWidgetProvider() {
    @RequiresApi(Build.VERSION_CODES.M)
    private fun setupAppOpenIntent(context: Context, views: RemoteViews, id: Int, widget: Widget) {
        val intent = Intent(context, MediaActivity::class.java).apply {
            putExtra(DIRECTORY, widget.folderPath)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            widget.widgetId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(id, pendingIntent)
    }

    @SuppressLint("CheckResult")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        RunOnBackgroundThreadUseCase {
            val config = context.config
            context.widgetsDB.getWidgets().filter { appWidgetIds.contains(it.widgetId) }.forEach {
                val views = RemoteViews(context.packageName, R.layout.widget).apply {

                    setInt(R.id.widget_background, "setColorFilter", config.widgetBgColor)
                    setInt(
                        R.id.widget_background,
                        "setImageAlpha",
                        Color.alpha(config.widgetBgColor)
                    )

                    val visibility = if (config.showWidgetFolderName) View.VISIBLE else View.GONE
                    setViewVisibility(R.id.widget_folder_name, visibility)
                    setTextColor(R.id.widget_folder_name, config.widgetTextColor)
                    setTextViewText(
                        R.id.widget_folder_name,
                        context.getFolderNameFromPath(it.folderPath)
                    )
                }

                val path =
                    GalleryDatabase.getInstance(context.applicationContext).DirectoryDao()
                        .getDirectoryThumbnail(it.folderPath) ?: return@forEach
                val options = RequestOptions()
                    .signature(getFileSignature(path))
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)

                if (context.config.cropThumbnails) {
                    options.centerCrop()
                } else {
                    options.fitCenter()
                }

                val density = context.resources.displayMetrics.density
                val appWidgetOptions = appWidgetManager.getAppWidgetOptions(appWidgetIds.first())
                val width = appWidgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
                val height = appWidgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)

                val widgetSize = (width.coerceAtLeast(height) * density).toInt()
                val image = Glide.with(context)
                    .asBitmap()
                    .load(path)
                    .apply(options)
                    .submit(widgetSize, widgetSize)
                    .get()

                views.setImageViewBitmap(R.id.widget_imageview, image)
                setupAppOpenIntent(context, views, R.id.widget_holder, it)
                appWidgetManager.updateAppWidget(it.widgetId, views)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        onUpdate(context, appWidgetManager, intArrayOf(appWidgetId))
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        RunOnBackgroundThreadUseCase {
            appWidgetIds.forEach {
                context.widgetsDB.deleteWidgetId(it)
            }
        }
    }

    private fun getFileSignature(path: String, lastModified: Long? = null) =
        ObjectKey(path.getFileKey(lastModified))
}
