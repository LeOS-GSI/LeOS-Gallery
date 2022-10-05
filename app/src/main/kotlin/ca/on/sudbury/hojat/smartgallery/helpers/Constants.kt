package ca.on.sudbury.hojat.smartgallery.helpers

import android.graphics.Color
import com.simplemobiletools.commons.helpers.MONTH_SECONDS

// shared preferences
const val DIRECTORY_SORT_ORDER = "directory_sort_order"
const val GROUP_FOLDER_PREFIX = "group_folder_"
const val VIEW_TYPE_PREFIX = "view_type_folder_"
const val SHOW_HIDDEN_MEDIA = "show_hidden_media"
const val TEMPORARILY_SHOW_HIDDEN = "temporarily_show_hidden"
const val TEMPORARILY_SHOW_EXCLUDED = "temporarily_show_excluded"
const val EXCLUDED_PASSWORD_PROTECTION = "excluded_password_protection"
const val EXCLUDED_PASSWORD_HASH = "excluded_password_hash"
const val EXCLUDED_PROTECTION_TYPE = "excluded_protection_type"
const val IS_THIRD_PARTY_INTENT = "is_third_party_intent"
const val AUTOPLAY_VIDEOS = "autoplay_videos"
const val REMEMBER_LAST_VIDEO_POSITION = "remember_last_video_position"
const val LOOP_VIDEOS = "loop_videos"
const val OPEN_VIDEOS_ON_SEPARATE_SCREEN = "open_videos_on_separate_screen"
const val ANIMATE_GIFS = "animate_gifs"
const val MAX_BRIGHTNESS = "max_brightness"
const val CROP_THUMBNAILS = "crop_thumbnails"
const val SHOW_THUMBNAIL_VIDEO_DURATION = "show_thumbnail_video_duration"
const val SCREEN_ROTATION = "screen_rotation"
const val DISPLAY_FILE_NAMES = "display_file_names"
const val BLACK_BACKGROUND = "dark_background"
const val PINNED_FOLDERS = "pinned_folders"
const val FILTER_MEDIA = "filter_media"
const val DEFAULT_FOLDER = "default_folder"
const val DIR_COLUMN_CNT = "dir_column_cnt"
const val DIR_LANDSCAPE_COLUMN_CNT = "dir_landscape_column_cnt"
const val DIR_HORIZONTAL_COLUMN_CNT = "dir_horizontal_column_cnt"
const val DIR_LANDSCAPE_HORIZONTAL_COLUMN_CNT = "dir_landscape_horizontal_column_cnt"
const val MEDIA_COLUMN_CNT = "media_column_cnt"
const val MEDIA_LANDSCAPE_COLUMN_CNT = "media_landscape_column_cnt"
const val MEDIA_HORIZONTAL_COLUMN_CNT = "media_horizontal_column_cnt"
const val MEDIA_LANDSCAPE_HORIZONTAL_COLUMN_CNT = "media_landscape_horizontal_column_cnt"
const val SHOW_ALL =
    "show_all"                           // display images and videos from all folders together
const val HIDE_FOLDER_TOOLTIP_SHOWN = "hide_folder_tooltip_shown"
const val EXCLUDED_FOLDERS = "excluded_folders"
const val INCLUDED_FOLDERS = "included_folders"
const val ALBUM_COVERS = "album_covers"
const val HIDE_SYSTEM_UI = "hide_system_ui"
const val DELETE_EMPTY_FOLDERS = "delete_empty_folders"
const val ALLOW_PHOTO_GESTURES = "allow_photo_gestures"
const val ALLOW_VIDEO_GESTURES = "allow_video_gestures"
const val TEMP_FOLDER_PATH = "temp_folder_path"
const val VIEW_TYPE_FOLDERS = "view_type_folders"
const val VIEW_TYPE_FILES = "view_type_files"
const val SHOW_EXTENDED_DETAILS = "show_extended_details"
const val EXTENDED_DETAILS = "extended_details"
const val HIDE_EXTENDED_DETAILS = "hide_extended_details"
const val ALLOW_INSTANT_CHANGE = "allow_instant_change"
const val WAS_NEW_APP_SHOWN = "was_new_app_shown_clock"
const val LAST_FILEPICKER_PATH = "last_filepicker_path"
const val TEMP_SKIP_DELETE_CONFIRMATION = "temp_skip_delete_confirmation"
const val BOTTOM_ACTIONS = "bottom_actions"
const val LAST_VIDEO_POSITION_PREFIX = "last_video_position_"
const val VISIBLE_BOTTOM_ACTIONS = "visible_bottom_actions"
const val WERE_FAVORITES_PINNED = "were_favorites_pinned"
const val WAS_RECYCLE_BIN_PINNED = "was_recycle_bin_pinned"
const val USE_RECYCLE_BIN = "use_recycle_bin"
const val GROUP_BY = "group_by"
const val EVER_SHOWN_FOLDERS = "ever_shown_folders"
const val SHOW_RECYCLE_BIN_AT_FOLDERS = "show_recycle_bin_at_folders"
const val SHOW_RECYCLE_BIN_LAST = "show_recycle_bin_last"
const val ALLOW_ZOOMING_IMAGES = "allow_zooming_images"
const val WAS_SVG_SHOWING_HANDLED = "was_svg_showing_handled"
const val LAST_BIN_CHECK = "last_bin_check"
const val SHOW_HIGHEST_QUALITY = "show_highest_quality"
const val ALLOW_DOWN_GESTURE = "allow_down_gesture"
const val LAST_EDITOR_CROP_ASPECT_RATIO = "last_editor_crop_aspect_ratio"
const val LAST_EDITOR_CROP_OTHER_ASPECT_RATIO_X = "last_editor_crop_other_aspect_ratio_x_2"
const val LAST_EDITOR_CROP_OTHER_ASPECT_RATIO_Y = "last_editor_crop_other_aspect_ratio_y_2"
const val GROUP_DIRECT_SUBFOLDERS = "group_direct_subfolders"
const val SHOW_WIDGET_FOLDER_NAME = "show_widget_folder_name"
const val ALLOW_ONE_TO_ONE_ZOOM = "allow_one_to_one_zoom"
const val ALLOW_ROTATING_WITH_GESTURES = "allow_rotating_with_gestures"
const val LAST_EDITOR_DRAW_COLOR = "last_editor_draw_color"
const val LAST_EDITOR_BRUSH_SIZE = "last_editor_brush_size"
const val SHOW_NOTCH = "show_notch"
const val FILE_LOADING_PRIORITY = "file_loading_priority"
const val SPAM_FOLDERS_CHECKED = "spam_folders_checked"
const val SHOW_THUMBNAIL_FILE_TYPES = "show_thumbnail_file_types"
const val MARK_FAVORITE_ITEMS = "mark_favorite_items"
const val EDITOR_BRUSH_COLOR = "editor_brush_color"
const val EDITOR_BRUSH_HARDNESS = "editor_brush_hardness"
const val EDITOR_BRUSH_SIZE = "editor_brush_size"
const val WERE_FAVORITES_MIGRATED = "were_favorites_migrated"
const val FOLDER_THUMBNAIL_STYLE = "folder_thumbnail_style"
const val FOLDER_MEDIA_COUNT = "folder_media_count"
const val LIMIT_FOLDER_TITLE = "folder_limit_title"
const val THUMBNAIL_SPACING = "thumbnail_spacing"
const val FILE_ROUNDED_CORNERS = "file_rounded_corners"
const val CUSTOM_FOLDERS_ORDER = "custom_folders_order"

// slideshow
const val SLIDESHOW_INTERVAL = "slideshow_interval"
const val SLIDESHOW_INCLUDE_VIDEOS = "slideshow_include_videos"
const val SLIDESHOW_INCLUDE_GIFS = "slideshow_include_gifs"
const val SLIDESHOW_RANDOM_ORDER = "slideshow_random_order"
const val SLIDESHOW_MOVE_BACKWARDS = "slideshow_move_backwards"
const val SLIDESHOW_ANIMATION = "slideshow_animation"
const val SLIDESHOW_LOOP = "loop_slideshow"
const val SLIDESHOW_DEFAULT_INTERVAL = 5
const val SLIDESHOW_SLIDE_DURATION = 500L
const val SLIDESHOW_FADE_DURATION = 1500L
const val SLIDESHOW_START_ON_ENTER = "slideshow_start_on_enter"

const val EXTERNAL_STORAGE_PROVIDER_AUTHORITY = "com.android.externalstorage.documents"
const val EXTRA_SHOW_ADVANCED = "android.content.extra.SHOW_ADVANCED"

const val APP_NAME = "app_name"
const val APP_LICENSES = "app_licenses"
const val APP_FAQ = "app_faq"
const val APP_VERSION_NAME = "app_version_name"
const val APP_ICON_IDS = "app_icon_ids"
const val APP_ID = "app_id"
const val APP_LAUNCHER_NAME = "app_launcher_name"
const val REAL_FILE_PATH = "real_file_path_2"
const val IS_FROM_GALLERY = "is_from_gallery"
const val BROADCAST_REFRESH_MEDIA = "com.simplemobiletools.REFRESH_MEDIA"
const val REFRESH_PATH = "refresh_path"
const val IS_CUSTOMIZING_COLORS = "is_customizing_colors"
const val BLOCKED_NUMBERS_EXPORT_DELIMITER = ","
const val BLOCKED_NUMBERS_EXPORT_EXTENSION = ".txt"
const val NOMEDIA = ".nomedia"
const val YOUR_ALARM_SOUNDS_MIN_ID = 1000
const val SHOW_FAQ_BEFORE_MAIL = "show_faq_before_mail"
const val INVALID_NAVIGATION_BAR_COLOR = -1
const val CHOPPED_LIST_DEFAULT_SIZE = 50
const val SAVE_DISCARD_PROMPT_INTERVAL = 1000L
val DEFAULT_WIDGET_BG_COLOR = Color.parseColor("#AA000000")
const val SD_OTG_PATTERN = "^/storage/[A-Za-z0-9]{4}-[A-Za-z0-9]{4}$"
const val SD_OTG_SHORT = "^[A-Za-z0-9]{4}-[A-Za-z0-9]{4}$"
const val KEY_PHONE = "phone"
const val CONTACT_ID = "contact_id"
const val IS_PRIVATE = "is_private"
const val MD5 = "MD5"
const val SHORT_ANIMATION_DURATION = 150L
val DARK_GREY = 0xFF333333.toInt()

const val LOWER_ALPHA = 0.25f
const val MEDIUM_ALPHA = 0.5f
const val HIGHER_ALPHA = 0.75f

const val HOUR_MINUTES = 60
const val DAY_MINUTES = 24 * HOUR_MINUTES
const val WEEK_MINUTES = DAY_MINUTES * 7
const val MONTH_MINUTES = DAY_MINUTES * 30
const val YEAR_MINUTES = DAY_MINUTES * 365

const val MINUTE_SECONDS = 60
const val HOUR_SECONDS = HOUR_MINUTES * 60
const val DAY_SECONDS = DAY_MINUTES * 60
const val WEEK_SECONDS = WEEK_MINUTES * 60
const val MONTH_SECONDS = MONTH_MINUTES * 60
const val YEAR_SECONDS = YEAR_MINUTES * 60

/////////////////////////////////////////////////////////////////////////////////////


/////////////////////////////////////////////////////////////////////////////////////

// slideshow animations
const val SLIDESHOW_ANIMATION_NONE = 0
const val SLIDESHOW_ANIMATION_SLIDE = 1
const val SLIDESHOW_ANIMATION_FADE = 2

const val RECYCLE_BIN = "recycle_bin"
const val SHOW_FAVORITES = "show_favorites"
const val SHOW_RECYCLE_BIN = "show_recycle_bin"
const val IS_IN_RECYCLE_BIN = "is_in_recycle_bin"
const val SHOW_NEXT_ITEM = "show_next_item"
const val SHOW_PREV_ITEM = "show_prev_item"
const val GO_TO_NEXT_ITEM = "go_to_next_item"
const val GO_TO_PREV_ITEM = "go_to_prev_item"
const val MAX_COLUMN_COUNT = 20
const val SHOW_TEMP_HIDDEN_DURATION = 300000L
const val CLICK_MAX_DURATION = 150
const val CLICK_MAX_DISTANCE = 100
const val MAX_CLOSE_DOWN_GESTURE_DURATION = 300
const val DRAG_THRESHOLD = 8
const val MONTH_MILLISECONDS = MONTH_SECONDS * 1000L
const val MIN_SKIP_LENGTH = 2000
const val HIDE_SYSTEM_UI_DELAY = 500L
const val MAX_PRINT_SIDE_SIZE = 4096
const val FAST_FORWARD_VIDEO_MS = 10000

const val DIRECTORY = "directory"
const val MEDIUM = "medium"
const val PATH = "path"
const val GET_IMAGE_INTENT = "get_image_intent"
const val GET_VIDEO_INTENT = "get_video_intent"
const val GET_ANY_INTENT = "get_any_intent"
const val SET_WALLPAPER_INTENT = "set_wallpaper_intent"
const val IS_VIEW_INTENT = "is_view_intent"
const val PICKED_PATHS = "picked_paths"
const val SHOULD_INIT_FRAGMENT = "should_init_fragment"
const val PORTRAIT_PATH = "portrait_path"
const val SKIP_AUTHENTICATION = "skip_authentication"

// rotations
const val ROTATE_BY_SYSTEM_SETTING = 0
const val ROTATE_BY_DEVICE_ROTATION = 1
const val ROTATE_BY_ASPECT_RATIO = 2

// file loading priority
const val PRIORITY_SPEED = 0
const val PRIORITY_COMPROMISE = 1
const val PRIORITY_VALIDITY = 2

// extended details values
const val EXT_NAME = 1
const val EXT_PATH = 2
const val EXT_SIZE = 4
const val EXT_RESOLUTION = 8
const val EXT_LAST_MODIFIED = 16
const val EXT_DATE_TAKEN = 32
const val EXT_CAMERA_MODEL = 64
const val EXT_EXIF_PROPERTIES = 128
const val EXT_DURATION = 256
const val EXT_ARTIST = 512
const val EXT_ALBUM = 1024
const val EXT_GPS = 2048

// media types
const val TYPE_IMAGES = 1
const val TYPE_VIDEOS = 2
const val TYPE_GIFS = 4
const val TYPE_RAWS = 8
const val TYPE_SVGS = 16
const val TYPE_PORTRAITS = 32

fun getDefaultFileFilter() = TYPE_IMAGES or TYPE_VIDEOS or TYPE_GIFS or TYPE_RAWS or TYPE_SVGS

const val LOCATION_INTERNAL = 1
const val LOCATION_SD = 2
const val LOCATION_OTG = 3

const val GROUP_BY_NONE = 1
const val GROUP_BY_LAST_MODIFIED_DAILY = 2
const val GROUP_BY_DATE_TAKEN_DAILY = 4
const val GROUP_BY_FILE_TYPE = 8
const val GROUP_BY_EXTENSION = 16
const val GROUP_BY_FOLDER = 32
const val GROUP_BY_LAST_MODIFIED_MONTHLY = 64
const val GROUP_BY_DATE_TAKEN_MONTHLY = 128
const val GROUP_DESCENDING = 1024
const val GROUP_SHOW_FILE_COUNT = 2048

// bottom actions
const val BOTTOM_ACTION_TOGGLE_FAVORITE = 1
const val BOTTOM_ACTION_EDIT = 2
const val BOTTOM_ACTION_SHARE = 4
const val BOTTOM_ACTION_DELETE = 8
const val BOTTOM_ACTION_ROTATE = 16
const val BOTTOM_ACTION_PROPERTIES = 32
const val BOTTOM_ACTION_CHANGE_ORIENTATION = 64
const val BOTTOM_ACTION_SLIDESHOW = 128
const val BOTTOM_ACTION_SHOW_ON_MAP = 256
const val BOTTOM_ACTION_TOGGLE_VISIBILITY = 512
const val BOTTOM_ACTION_RENAME = 1024
const val BOTTOM_ACTION_SET_AS = 2048
const val BOTTOM_ACTION_COPY = 4096
const val BOTTOM_ACTION_MOVE = 8192
const val BOTTOM_ACTION_RESIZE = 16384

const val DEFAULT_BOTTOM_ACTIONS =
    BOTTOM_ACTION_TOGGLE_FAVORITE or BOTTOM_ACTION_EDIT or BOTTOM_ACTION_SHARE or BOTTOM_ACTION_DELETE

// aspect ratios used at the editor for cropping
const val ASPECT_RATIO_FREE = 0
const val ASPECT_RATIO_ONE_ONE = 1
const val ASPECT_RATIO_FOUR_THREE = 2
const val ASPECT_RATIO_SIXTEEN_NINE = 3
const val ASPECT_RATIO_OTHER = 4

// some constants related to zooming videos
const val MIN_VIDEO_ZOOM_SCALE = 1f
const val MAX_VIDEO_ZOOM_SCALE = 5f
const val ZOOM_MODE_NONE = 0
const val ZOOM_MODE_DRAG = 1
const val ZOOM_MODE_ZOOM = 2

// constants related to image quality
const val LOW_TILE_DPI = 160
const val NORMAL_TILE_DPI = 220
const val WEIRD_TILE_DPI = 240
const val HIGH_TILE_DPI = 280

const val ROUNDED_CORNERS_NONE = 1
const val ROUNDED_CORNERS_SMALL = 2
const val ROUNDED_CORNERS_BIG = 3

const val FOLDER_MEDIA_CNT_LINE = 1
const val FOLDER_MEDIA_CNT_BRACKETS = 2
const val FOLDER_MEDIA_CNT_NONE = 3

const val FOLDER_STYLE_SQUARE = 1
const val FOLDER_STYLE_ROUNDED_CORNERS = 2


val photoExtensions: Array<String>
    get() = arrayOf(
        ".jpg",
        ".png",
        ".jpeg",
        ".bmp",
        ".webp",
        ".heic",
        ".heif",
        ".apng",
        ".avif"
    )
val videoExtensions: Array<String>
    get() = arrayOf(
        ".mp4",
        ".mkv",
        ".webm",
        ".avi",
        ".3gp",
        ".mov",
        ".m4v",
        ".3gpp"
    )
val audioExtensions: Array<String>
    get() = arrayOf(
        ".mp3",
        ".wav",
        ".wma",
        ".ogg",
        ".m4a",
        ".opus",
        ".flac",
        ".aac"
    )
val rawExtensions: Array<String>
    get() = arrayOf(
        ".dng",
        ".orf",
        ".nef",
        ".arw",
        ".rw2",
        ".cr2",
        ".cr3"
    )
