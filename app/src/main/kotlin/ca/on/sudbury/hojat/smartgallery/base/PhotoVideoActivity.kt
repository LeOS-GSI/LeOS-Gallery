package ca.on.sudbury.hojat.smartgallery.base

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Html
import android.view.View
import android.widget.RelativeLayout
import androidx.annotation.RequiresApi
import ca.on.sudbury.hojat.smartgallery.BuildConfig
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.activities.MainActivity
import ca.on.sudbury.hojat.smartgallery.activities.MediaActivity
import ca.on.sudbury.hojat.smartgallery.activities.PanoramaVideoActivity
import ca.on.sudbury.hojat.smartgallery.activities.ViewPagerActivity
import ca.on.sudbury.hojat.smartgallery.databinding.FragmentHolderBinding
import ca.on.sudbury.hojat.smartgallery.extensions.config
import ca.on.sudbury.hojat.smartgallery.extensions.setAs
import ca.on.sudbury.hojat.smartgallery.extensions.openPath
import ca.on.sudbury.hojat.smartgallery.extensions.openEditor
import ca.on.sudbury.hojat.smartgallery.extensions.showFileOnMap
import ca.on.sudbury.hojat.smartgallery.extensions.parseFileChannel
import ca.on.sudbury.hojat.smartgallery.dialogs.PropertiesDialog
import ca.on.sudbury.hojat.smartgallery.extensions.isImageFast
import ca.on.sudbury.hojat.smartgallery.extensions.isGif
import ca.on.sudbury.hojat.smartgallery.extensions.isVideoFast
import ca.on.sudbury.hojat.smartgallery.extensions.isRawFast
import ca.on.sudbury.hojat.smartgallery.extensions.isPortrait
import ca.on.sudbury.hojat.smartgallery.extensions.navigationBarHeight
import ca.on.sudbury.hojat.smartgallery.extensions.statusBarHeight
import ca.on.sudbury.hojat.smartgallery.extensions.actionBarHeight
import ca.on.sudbury.hojat.smartgallery.extensions.portrait
import ca.on.sudbury.hojat.smartgallery.extensions.navigationBarRight
import ca.on.sudbury.hojat.smartgallery.extensions.getUriMimeType
import ca.on.sudbury.hojat.smartgallery.extensions.getFinalUriFromPath
import ca.on.sudbury.hojat.smartgallery.extensions.getDoesFilePathExist
import ca.on.sudbury.hojat.smartgallery.extensions.getParentPath
import ca.on.sudbury.hojat.smartgallery.extensions.isExternalStorageManager
import ca.on.sudbury.hojat.smartgallery.extensions.getFilenameFromPath
import ca.on.sudbury.hojat.smartgallery.extensions.rescanPaths
import ca.on.sudbury.hojat.smartgallery.extensions.getColoredDrawableWithColor
import ca.on.sudbury.hojat.smartgallery.extensions.getFilenameFromUri
import ca.on.sudbury.hojat.smartgallery.extensions.checkAppSideloading
import ca.on.sudbury.hojat.smartgallery.extensions.getRealPathFromURI
import ca.on.sudbury.hojat.smartgallery.extensions.navigationBarSize
import ca.on.sudbury.hojat.smartgallery.extensions.sharePathIntent
import ca.on.sudbury.hojat.smartgallery.extensions.toHex
import ca.on.sudbury.hojat.smartgallery.helpers.PERMISSION_WRITE_STORAGE
import ca.on.sudbury.hojat.smartgallery.helpers.REAL_FILE_PATH
import ca.on.sudbury.hojat.smartgallery.helpers.NOMEDIA
import ca.on.sudbury.hojat.smartgallery.helpers.IS_FROM_GALLERY
import ca.on.sudbury.hojat.smartgallery.photoview.PhotoFragment
import ca.on.sudbury.hojat.smartgallery.video.VideoFragment
import ca.on.sudbury.hojat.smartgallery.fragments.ViewPagerFragment
import ca.on.sudbury.hojat.smartgallery.helpers.BOTTOM_ACTION_SHARE
import ca.on.sudbury.hojat.smartgallery.helpers.BOTTOM_ACTION_SHOW_ON_MAP
import ca.on.sudbury.hojat.smartgallery.helpers.BOTTOM_ACTION_SET_AS
import ca.on.sudbury.hojat.smartgallery.helpers.BOTTOM_ACTION_EDIT
import ca.on.sudbury.hojat.smartgallery.helpers.BOTTOM_ACTION_PROPERTIES
import ca.on.sudbury.hojat.smartgallery.helpers.TYPE_VIDEOS
import ca.on.sudbury.hojat.smartgallery.helpers.TYPE_GIFS
import ca.on.sudbury.hojat.smartgallery.helpers.TYPE_RAWS
import ca.on.sudbury.hojat.smartgallery.helpers.TYPE_SVGS
import ca.on.sudbury.hojat.smartgallery.helpers.TYPE_PORTRAITS
import ca.on.sudbury.hojat.smartgallery.helpers.TYPE_IMAGES
import ca.on.sudbury.hojat.smartgallery.helpers.MEDIUM
import ca.on.sudbury.hojat.smartgallery.helpers.PATH
import ca.on.sudbury.hojat.smartgallery.helpers.SKIP_AUTHENTICATION
import ca.on.sudbury.hojat.smartgallery.helpers.SHOW_FAVORITES
import ca.on.sudbury.hojat.smartgallery.helpers.IS_VIEW_INTENT
import ca.on.sudbury.hojat.smartgallery.helpers.IS_IN_RECYCLE_BIN
import ca.on.sudbury.hojat.smartgallery.models.Medium
import ca.on.sudbury.hojat.smartgallery.photoedit.usecases.IsRPlusUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.BeVisibleOrGoneUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.HideKeyboardUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.HideSystemUiUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.IsSvgUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.RunOnBackgroundThreadUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.ShowSafeToastUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.ShowSystemUiUseCase
import ca.on.sudbury.hojat.smartgallery.video.VideoPlayerActivity
import java.io.File
import java.io.FileInputStream

open class PhotoVideoActivity : SimpleActivity(), ViewPagerFragment.FragmentListener {

    private lateinit var binding: FragmentHolderBinding

    private var mMedium: Medium? = null
    private var mIsFullScreen = false
    private var mIsFromGallery = false
    private var mFragment: ViewPagerFragment? = null
    private var mUri: Uri? = null

    var mIsVideo = false

    public override fun onCreate(savedInstanceState: Bundle?) {
        showTransparentTop = true
        showTransparentNavigation = true

        super.onCreate(savedInstanceState)
        binding = FragmentHolderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (checkAppSideloading()) {
            return
        }

        setupOptionsMenu()
        refreshMenuItems()
        handlePermission(PERMISSION_WRITE_STORAGE) {
            if (it) {
                checkIntent(savedInstanceState)
            } else {
                ShowSafeToastUseCase(this, R.string.no_storage_permissions)
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (config.bottomActions) {
            window.navigationBarColor = Color.TRANSPARENT
        } else {
            setTranslucentNavigation()
        }

        if (config.blackBackground) {
            updateStatusbarColor(Color.BLACK)
        }
    }

    fun refreshMenuItems() {
        val visibleBottomActions = if (config.bottomActions) config.visibleBottomActions else 0

        binding.fragmentViewerToolbar.menu.apply {
            findItem(R.id.menu_set_as).isVisible =
                mMedium?.isImage() == true && visibleBottomActions and BOTTOM_ACTION_SET_AS == 0
            findItem(R.id.menu_edit).isVisible =
                mMedium?.isImage() == true && mUri?.scheme == "file" && visibleBottomActions and BOTTOM_ACTION_EDIT == 0
            findItem(R.id.menu_properties).isVisible =
                mUri?.scheme == "file" && visibleBottomActions and BOTTOM_ACTION_PROPERTIES == 0
            findItem(R.id.menu_share).isVisible = visibleBottomActions and BOTTOM_ACTION_SHARE == 0
            findItem(R.id.menu_show_on_map).isVisible =
                visibleBottomActions and BOTTOM_ACTION_SHOW_ON_MAP == 0
        }
    }

    private fun setupOptionsMenu() {
        (binding.fragmentViewerAppbar.layoutParams as RelativeLayout.LayoutParams).topMargin =
            statusBarHeight
        binding.fragmentViewerToolbar.apply {
            setTitleTextColor(Color.WHITE)
            overflowIcon =
                resources.getColoredDrawableWithColor(R.drawable.ic_three_dots_vector, Color.WHITE)
            navigationIcon =
                resources.getColoredDrawableWithColor(R.drawable.ic_arrow_left_vector, Color.WHITE)
        }

        updateMenuItemColors(binding.fragmentViewerToolbar.menu, forceWhiteIcons = true)
        binding.fragmentViewerToolbar.setOnMenuItemClickListener { menuItem ->
            if (mMedium == null || mUri == null) {
                return@setOnMenuItemClickListener true
            }

            when (menuItem.itemId) {
                R.id.menu_set_as -> setAs(mUri!!.toString())
                R.id.menu_open_with -> openPath(mUri!!.toString(), true)
                R.id.menu_share -> sharePathIntent(mUri!!.toString(), BuildConfig.APPLICATION_ID)

                R.id.menu_edit -> openEditor(mUri!!.toString())
                R.id.menu_properties -> showProperties()
                R.id.menu_show_on_map -> showFileOnMap(mUri!!.toString())
                else -> return@setOnMenuItemClickListener false
            }
            return@setOnMenuItemClickListener true
        }

        binding.fragmentViewerToolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun checkIntent(savedInstanceState: Bundle? = null) {
        if (intent.data == null && intent.action == Intent.ACTION_VIEW) {
            HideKeyboardUseCase(this)
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        mUri = intent.data ?: return
        val uri = mUri.toString()
        if (uri.startsWith("content:/") && uri.contains("/storage/") && !intent.getBooleanExtra(
                IS_IN_RECYCLE_BIN,
                false
            )
        ) {
            val guessedPath = uri.substring(uri.indexOf("/storage/"))
            if (getDoesFilePathExist(guessedPath)) {
                val extras = intent.extras ?: Bundle()
                extras.apply {
                    putString(REAL_FILE_PATH, guessedPath)
                    intent.putExtras(this)
                }
            }
        }

        var filename = getFilenameFromUri(mUri!!)
        mIsFromGallery = intent.getBooleanExtra(IS_FROM_GALLERY, false)
        if (mIsFromGallery && filename.isVideoFast() && config.openVideosOnSeparateScreen) {
            launchVideoPlayer()
            return
        }

        if (intent.extras?.containsKey(REAL_FILE_PATH) == true) {
            val realPath = intent.extras!!.getString(REAL_FILE_PATH)
            if (realPath != null && getDoesFilePathExist(realPath)) {
                val isFileFolderHidden = (File(realPath).isHidden || File(
                    realPath.getParentPath(),
                    NOMEDIA
                ).exists() || realPath.contains("/."))
                val preventShowingHiddenFile =
                    (IsRPlusUseCase() && !isExternalStorageManager()) && isFileFolderHidden
                if (!preventShowingHiddenFile) {
                    if (realPath.getFilenameFromPath().contains('.') || filename.contains('.')) {
                        if (isFileTypeVisible(realPath)) {
                            binding.bottomActions.root.visibility = View.GONE
                            sendViewPagerIntent(realPath)
                            finish()
                            return
                        }
                    } else {
                        filename = realPath.getFilenameFromPath()
                    }
                }
            }
        }

        if (mUri!!.scheme == "file") {
            if (filename.contains('.')) {
                binding.bottomActions.root.visibility = View.GONE
                applicationContext.rescanPaths(arrayListOf(mUri!!.path!!))
                sendViewPagerIntent(mUri!!.path!!)
                finish()
            }
            return
        } else {
            val realPath = applicationContext.getRealPathFromURI(mUri!!) ?: ""
            val isFileFolderHidden = (File(realPath).isHidden || File(
                realPath.getParentPath(),
                NOMEDIA
            ).exists() || realPath.contains("/."))
            val preventShowingHiddenFile =
                (IsRPlusUseCase() && !isExternalStorageManager()) && isFileFolderHidden
            if (!preventShowingHiddenFile) {
                if (realPath != mUri.toString() && realPath.isNotEmpty() && mUri!!.authority != "mms" && filename.contains(
                        '.'
                    ) && getDoesFilePathExist(realPath)
                ) {
                    if (isFileTypeVisible(realPath)) {
                        binding.bottomActions.root.visibility = View.GONE
                        applicationContext.rescanPaths(arrayListOf(mUri!!.path!!))
                        sendViewPagerIntent(realPath)
                        finish()
                        return
                    }
                }
            }
        }

        binding.topShadow.layoutParams.height = statusBarHeight + actionBarHeight
        if (!portrait && navigationBarRight && (if (navigationBarRight) navigationBarSize.x else 0) > 0) {
            binding.fragmentViewerToolbar.setPadding(
                0,
                0,
                if (navigationBarRight) navigationBarSize.x else 0,
                0
            )
        } else {
            binding.fragmentViewerToolbar.setPadding(0, 0, 0, 0)
        }

        checkNotchSupport()
        ShowSystemUiUseCase(this)
        val bundle = Bundle()
        val file = File(mUri.toString())
        val intentType = intent.type ?: ""
        val type = when {
            filename.isVideoFast() || intentType.startsWith("video/") -> TYPE_VIDEOS
            filename.isGif() || intentType.equals("image/gif", true) -> TYPE_GIFS
            filename.isRawFast() -> TYPE_RAWS
            IsSvgUseCase(filename) -> TYPE_SVGS
            file.absolutePath.isPortrait() -> TYPE_PORTRAITS
            else -> TYPE_IMAGES
        }

        mIsVideo = type == TYPE_VIDEOS
        mMedium = Medium(
            null,
            filename,
            mUri.toString(),
            mUri!!.path!!.getParentPath(),
            0,
            0,
            file.length(),
            type,
            0,
            false,
            0L,
            0
        )
        binding.fragmentViewerToolbar.title =
            Html.fromHtml("<font color='${Color.WHITE.toHex()}'>${mMedium!!.name}</font>")
        bundle.putSerializable(MEDIUM, mMedium)

        if (savedInstanceState == null) {
            mFragment = if (mIsVideo) VideoFragment() else PhotoFragment()
            mFragment!!.listener = this
            mFragment!!.arguments = bundle
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_placeholder, mFragment!!).commit()
        }

        if (config.blackBackground) {
            binding.fragmentHolder.background = ColorDrawable(Color.BLACK)
        }

        if (config.maxBrightness) {
            val attributes = window.attributes
            attributes.screenBrightness = 1f
            window.attributes = attributes
        }

        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            val isFullscreen = visibility and View.SYSTEM_UI_FLAG_FULLSCREEN != 0
            mFragment?.fullscreenToggled(isFullscreen)
        }

        initBottomActions()
    }

    private fun launchVideoPlayer() {
        val newUri = getFinalUriFromPath(mUri.toString(), BuildConfig.APPLICATION_ID)
        if (newUri == null) {
            ShowSafeToastUseCase(this, R.string.unknown_error_occurred)
            return
        }

        var isPanorama = false
        val realPath = intent?.extras?.getString(REAL_FILE_PATH) ?: ""
        try {
            if (realPath.isNotEmpty()) {
                val fis = FileInputStream(File(realPath))
                parseFileChannel(realPath, fis.channel, 0, 0, 0) {
                    isPanorama = true
                }
            }
        } catch (ignored: Exception) {
        } catch (ignored: OutOfMemoryError) {
        }

        HideKeyboardUseCase(this)
        if (isPanorama) {
            Intent(applicationContext, PanoramaVideoActivity::class.java).apply {
                putExtra(PATH, realPath)
                startActivity(this)
            }
        } else {
            val mimeType = getUriMimeType(mUri.toString(), newUri)
            Intent(applicationContext, VideoPlayerActivity::class.java).apply {
                setDataAndType(newUri, mimeType)
                addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT)
                if (intent.extras != null) {
                    putExtras(intent.extras!!)
                }

                startActivity(this)
            }
        }
        finish()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        initBottomActionsLayout()

        binding.topShadow.layoutParams.height = statusBarHeight + actionBarHeight
        (binding.fragmentViewerAppbar.layoutParams as RelativeLayout.LayoutParams).topMargin =
            statusBarHeight
        if (!portrait && navigationBarRight && (if (navigationBarRight) navigationBarSize.x else 0) > 0) {
            binding.fragmentViewerToolbar.setPadding(
                0,
                0,
                if (navigationBarRight) navigationBarSize.x else 0,
                0
            )
        } else {
            binding.fragmentViewerToolbar.setPadding(0, 0, 0, 0)
        }
    }

    private fun sendViewPagerIntent(path: String) {
        RunOnBackgroundThreadUseCase {
            if (isPathPresentInMediaStore(path)) {
                openViewPager(path)
            } else {
                applicationContext.rescanPaths(arrayListOf(path)) {
                    openViewPager(path)
                }
            }
        }
    }

    private fun openViewPager(path: String) {
        if (!intent.getBooleanExtra(IS_FROM_GALLERY, false)) {
            MediaActivity.mMedia.clear()
        }
        runOnUiThread {
            HideKeyboardUseCase(this)
            Intent(this, ViewPagerActivity::class.java).apply {
                putExtra(SKIP_AUTHENTICATION, intent.getBooleanExtra(SKIP_AUTHENTICATION, false))
                putExtra(SHOW_FAVORITES, intent.getBooleanExtra(SHOW_FAVORITES, false))
                putExtra(IS_VIEW_INTENT, true)
                putExtra(IS_FROM_GALLERY, mIsFromGallery)
                putExtra(PATH, path)
                startActivity(this)
            }
        }
    }

    private fun isPathPresentInMediaStore(path: String): Boolean {
        val uri = MediaStore.Files.getContentUri("external")
        val selection = "${MediaStore.Images.Media.DATA} = ?"
        val selectionArgs = arrayOf(path)

        try {
            val cursor = contentResolver.query(
                uri,
                null,
                selection,
                selectionArgs,
                null
            )
            cursor?.use {
                return cursor.moveToFirst()
            }
        } catch (_: Exception) {
        }

        return false
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun showProperties() {
        PropertiesDialog(this, mUri!!.path!!)
    }

    private fun isFileTypeVisible(path: String): Boolean {
        val filter = config.filterMedia
        return !(path.isImageFast() && filter and TYPE_IMAGES == 0 ||
                path.isVideoFast() && filter and TYPE_VIDEOS == 0 ||
                path.isGif() && filter and TYPE_GIFS == 0 ||
                path.isRawFast() && filter and TYPE_RAWS == 0 ||
                IsSvgUseCase(path) && filter and TYPE_SVGS == 0 ||
                path.isPortrait() && filter and TYPE_PORTRAITS == 0)
    }

    private fun initBottomActions() {
        initBottomActionButtons()
        initBottomActionsLayout()
    }

    private fun initBottomActionsLayout() {
        binding.bottomActions.root.layoutParams.height =
            resources.getDimension(R.dimen.bottom_actions_height).toInt() + navigationBarHeight
        if (config.bottomActions) {
            binding.bottomActions.root.visibility = View.VISIBLE
        } else {
            binding.bottomActions.root.visibility = View.GONE
        }
    }

    private fun initBottomActionButtons() {
        arrayListOf(
            binding.bottomActions.bottomFavorite,
            binding.bottomActions.bottomDelete,
            binding.bottomActions.bottomRotate,
            binding.bottomActions.bottomProperties,
            binding.bottomActions.bottomChangeOrientation,
            binding.bottomActions.bottomSlideshow,
            binding.bottomActions.bottomShowOnMap,
            binding.bottomActions.bottomToggleFileVisibility,
            binding.bottomActions.bottomRename,
            binding.bottomActions.bottomCopy,
            binding.bottomActions.bottomMove,
            binding.bottomActions.bottomResize
        ).forEach {
            it.visibility = View.GONE
        }

        val visibleBottomActions = if (config.bottomActions) config.visibleBottomActions else 0
        BeVisibleOrGoneUseCase(
            binding.bottomActions.bottomEdit,
            visibleBottomActions and BOTTOM_ACTION_EDIT != 0 && mMedium?.isImage() == true
        )
        binding.bottomActions.bottomEdit.setOnClickListener {
            if (mUri != null && binding.bottomActions.root.alpha == 1f) {
                openEditor(mUri!!.toString())
            }
        }

        BeVisibleOrGoneUseCase(
            binding.bottomActions.bottomShare,
            visibleBottomActions and BOTTOM_ACTION_SHARE != 0
        )
        binding.bottomActions.bottomShare.setOnClickListener {
            if (mUri != null && binding.bottomActions.root.alpha == 1f) {
                sharePathIntent(mUri!!.toString(), BuildConfig.APPLICATION_ID)
            }
        }

        BeVisibleOrGoneUseCase(
            binding.bottomActions.bottomSetAs,
            visibleBottomActions and BOTTOM_ACTION_SET_AS != 0 && mMedium?.isImage() == true
        )
        binding.bottomActions.bottomSetAs.setOnClickListener {
            setAs(mUri!!.toString())
        }

        BeVisibleOrGoneUseCase(
            binding.bottomActions.bottomShowOnMap,
            visibleBottomActions and BOTTOM_ACTION_SHOW_ON_MAP != 0
        )
        binding.bottomActions.bottomShowOnMap.setOnClickListener {
            showFileOnMap(mUri!!.toString())
        }
    }

    override fun fragmentClicked() {
        mIsFullScreen = !mIsFullScreen
        if (mIsFullScreen) {
            HideSystemUiUseCase(this)
        } else {
            ShowSystemUiUseCase(this)
        }

        val newAlpha = if (mIsFullScreen) 0f else 1f
        binding.topShadow.animate().alpha(newAlpha).start()
        if (binding.bottomActions.root.visibility != View.GONE) {
            binding.bottomActions.root.animate().alpha(newAlpha).start()
        }

        binding.fragmentViewerToolbar.animate().alpha(newAlpha).withStartAction {
            binding.fragmentViewerToolbar.visibility = View.VISIBLE
        }.withEndAction {
            BeVisibleOrGoneUseCase(binding.fragmentViewerToolbar, newAlpha == 1f)
        }.start()
    }

    override fun videoEnded() = false

    override fun goToPrevItem() {}

    override fun goToNextItem() {}

    override fun launchViewVideoIntent(path: String) {}

    override fun isSlideShowActive() = false
}
