package ca.on.sudbury.hojat.smartgallery.models

import android.content.Context
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import androidx.room.ColumnInfo
import androidx.room.Ignore
import com.bumptech.glide.signature.ObjectKey
import ca.on.sudbury.hojat.smartgallery.extensions.formatDate
import ca.on.sudbury.hojat.smartgallery.helpers.GroupBy
import ca.on.sudbury.hojat.smartgallery.helpers.MediaType
import ca.on.sudbury.hojat.smartgallery.helpers.SORT_BY_NAME
import ca.on.sudbury.hojat.smartgallery.helpers.SORT_BY_PATH
import ca.on.sudbury.hojat.smartgallery.helpers.SORT_BY_SIZE
import ca.on.sudbury.hojat.smartgallery.helpers.SORT_BY_DATE_MODIFIED
import ca.on.sudbury.hojat.smartgallery.helpers.SORT_BY_RANDOM
import ca.on.sudbury.hojat.smartgallery.usecases.FormatFileSizeUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.GetFileExtensionUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.IsApngUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.IsWebpUseCase
import java.io.File
import java.io.Serializable
import java.util.Calendar
import java.util.Locale

@Entity(tableName = "media", indices = [(Index(value = ["full_path"], unique = true))])
data class Medium(
    @PrimaryKey(autoGenerate = true) var id: Long?,
    @ColumnInfo(name = "filename") var name: String,
    @ColumnInfo(name = "full_path") var path: String,
    @ColumnInfo(name = "parent_path") var parentPath: String,
    @ColumnInfo(name = "last_modified") var modified: Long,
    @ColumnInfo(name = "date_taken") var taken: Long,
    @ColumnInfo(name = "size") var size: Long,
    @ColumnInfo(name = "type") var type: Int,
    @ColumnInfo(name = "video_duration") var videoDuration: Int,
    @ColumnInfo(name = "is_favorite") var isFavorite: Boolean,
    @ColumnInfo(name = "deleted_ts") var deletedTS: Long,
    @ColumnInfo(name = "media_store_id") var mediaStoreId: Long,

    @Ignore var gridPosition: Int = 0   // used at grid view decoration at Grouping enabled
) : Serializable, ThumbnailItem() {

    constructor() : this(null, "", "", "", 0L, 0L, 0L, 0, 0, false, 0L, 0L, 0)

    companion object {
        private const val serialVersionUID = -6553149366975655L
    }

    fun isWebP() = IsWebpUseCase(name)

    fun isGIF() = type == MediaType.Gif.id

    fun isImage() = type == MediaType.Image.id

    fun isVideo() = type == MediaType.Video.id

    fun isRaw() = type == MediaType.Raw.id

    fun isSVG() = type == MediaType.Svg.id

    fun isPortrait() = type == MediaType.Portrait.id

    fun isApng() = IsApngUseCase(name)

    fun isHidden() = name.startsWith('.')

    fun isHeic() = name.lowercase(Locale.ROOT).endsWith(".heic") || name.lowercase(Locale.ROOT)
        .endsWith(".heif")

    fun getBubbleText(sorting: Int, context: Context, dateFormat: String, timeFormat: String) =
        when {
            sorting and SORT_BY_NAME != 0 -> name
            sorting and SORT_BY_PATH != 0 -> path
            sorting and SORT_BY_SIZE != 0 -> FormatFileSizeUseCase(size)
            sorting and SORT_BY_DATE_MODIFIED != 0 -> modified.formatDate(
                context,
                dateFormat,
                timeFormat
            )
            sorting and SORT_BY_RANDOM != 0 -> name
            else -> taken.formatDate(context)
        }

    fun getGroupingKey(groupBy: Int): String {
        return when {
            groupBy and GroupBy.LastModifiedDaily.id != 0 -> getDayStartTS(modified, false)
            groupBy and GroupBy.LastModifiedMonthly.id != 0 -> getDayStartTS(modified, true)
            groupBy and GroupBy.DateTakenDaily.id != 0 -> getDayStartTS(taken, false)
            groupBy and GroupBy.DateTakenMonthly.id != 0 -> getDayStartTS(taken, true)
            groupBy and GroupBy.FileType.id != 0 -> type.toString()
            groupBy and GroupBy.Extension.id != 0 -> GetFileExtensionUseCase(name).lowercase(Locale.ROOT)
            groupBy and GroupBy.Folder.id != 0 -> parentPath
            else -> ""
        }
    }

    fun getIsInRecycleBin() = deletedTS != 0L

    private fun getDayStartTS(ts: Long, resetDays: Boolean): String {
        val calendar = Calendar.getInstance(Locale.ENGLISH).apply {
            timeInMillis = ts
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (resetDays) {
                set(Calendar.DAY_OF_MONTH, 1)
            }
        }

        return calendar.timeInMillis.toString()
    }

    fun getSignature(): String {
        val lastModified = if (modified > 1) {
            modified
        } else {
            File(path).lastModified()
        }

        return "$path-$lastModified-$size"
    }

    fun getKey() = ObjectKey(getSignature())

    fun toFileDirItem() = FileDirItem(path, name, false, 0, size, modified, mediaStoreId)
}
