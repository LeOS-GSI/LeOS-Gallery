package ca.on.sudbury.hojat.smartgallery.activities

import android.app.SearchManager
import android.content.ClipData
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.provider.MediaStore.Images
import android.provider.MediaStore.Video
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuItemCompat
import androidx.documentfile.provider.DocumentFile
import androidx.recyclerview.widget.RecyclerView
import ca.on.hojat.palette.views.MyGridLayoutManager
import ca.on.sudbury.hojat.smartgallery.BuildConfig
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.adapters.DirectoryAdapter
import ca.on.sudbury.hojat.smartgallery.base.SimpleActivity
import ca.on.sudbury.hojat.smartgallery.database.DirectoryOperationsListener
import ca.on.sudbury.hojat.smartgallery.databases.GalleryDatabase
import ca.on.sudbury.hojat.smartgallery.databinding.ActivityMainBinding
import ca.on.sudbury.hojat.smartgallery.dialogs.ChangeSortingDialogFragment
import ca.on.sudbury.hojat.smartgallery.dialogs.ChangeViewTypeDialogFragment
import ca.on.sudbury.hojat.smartgallery.dialogs.CreateNewFolderDialogFragment
import ca.on.sudbury.hojat.smartgallery.dialogs.FilePickerDialogFragment
import ca.on.sudbury.hojat.smartgallery.dialogs.FilterMediaDialogFragment
import ca.on.sudbury.hojat.smartgallery.dialogs.RateStarsDialogFragment
import ca.on.sudbury.hojat.smartgallery.dialogs.SecurityDialogFragment
import ca.on.sudbury.hojat.smartgallery.extensions.addTempFolderIfNeeded
import ca.on.sudbury.hojat.smartgallery.extensions.areSystemAnimationsEnabled
import ca.on.sudbury.hojat.smartgallery.extensions.baseConfig
import ca.on.sudbury.hojat.smartgallery.extensions.checkWhatsNew
import ca.on.sudbury.hojat.smartgallery.extensions.config
import ca.on.sudbury.hojat.smartgallery.extensions.createDirectoryFromMedia
import ca.on.sudbury.hojat.smartgallery.extensions.deleteFiles
import ca.on.sudbury.hojat.smartgallery.extensions.getCachedDirectories
import ca.on.sudbury.hojat.smartgallery.extensions.getCachedMedia
import ca.on.sudbury.hojat.smartgallery.extensions.getDirectorySortingValue
import ca.on.sudbury.hojat.smartgallery.extensions.getDirsToShow
import ca.on.sudbury.hojat.smartgallery.extensions.getDistinctPath
import ca.on.sudbury.hojat.smartgallery.extensions.getDocumentFile
import ca.on.sudbury.hojat.smartgallery.extensions.getDoesFilePathExist
import ca.on.sudbury.hojat.smartgallery.extensions.getFavoritePaths
import ca.on.sudbury.hojat.smartgallery.extensions.getFilePublicUri
import ca.on.sudbury.hojat.smartgallery.extensions.getFilenameFromPath
import ca.on.sudbury.hojat.smartgallery.extensions.getLatestMediaByDateId
import ca.on.sudbury.hojat.smartgallery.extensions.getLatestMediaId
import ca.on.sudbury.hojat.smartgallery.extensions.getMimeType
import ca.on.sudbury.hojat.smartgallery.extensions.getNoMediaFoldersSync
import ca.on.sudbury.hojat.smartgallery.extensions.getProperPrimaryColor
import ca.on.sudbury.hojat.smartgallery.extensions.getProperTextColor
import ca.on.sudbury.hojat.smartgallery.extensions.getRealInternalStoragePath
import ca.on.sudbury.hojat.smartgallery.extensions.getSDCardPath
import ca.on.sudbury.hojat.smartgallery.extensions.getSortedDirectories
import ca.on.sudbury.hojat.smartgallery.extensions.getStorageDirectories
import ca.on.sudbury.hojat.smartgallery.extensions.handleExcludedFolderPasswordProtection
import ca.on.sudbury.hojat.smartgallery.extensions.handleHiddenFolderPasswordProtection
import ca.on.sudbury.hojat.smartgallery.extensions.handleLockedFolderOpening
import ca.on.sudbury.hojat.smartgallery.extensions.handleMediaManagementPrompt
import ca.on.sudbury.hojat.smartgallery.extensions.hasOTGConnected
import ca.on.sudbury.hojat.smartgallery.extensions.hasPermission
import ca.on.sudbury.hojat.smartgallery.extensions.internalStoragePath
import ca.on.sudbury.hojat.smartgallery.extensions.isDownloadsFolder
import ca.on.sudbury.hojat.smartgallery.extensions.isExternalStorageManager
import ca.on.sudbury.hojat.smartgallery.extensions.isMediaFile
import ca.on.sudbury.hojat.smartgallery.extensions.launchAbout
import ca.on.sudbury.hojat.smartgallery.extensions.mediaDB
import ca.on.sudbury.hojat.smartgallery.extensions.movePathsInRecycleBin
import ca.on.sudbury.hojat.smartgallery.extensions.movePinnedDirectoriesToFront
import ca.on.sudbury.hojat.smartgallery.extensions.recycleBinPath
import ca.on.sudbury.hojat.smartgallery.extensions.removeInvalidDBDirectories
import ca.on.sudbury.hojat.smartgallery.extensions.toFileDirItem
import ca.on.sudbury.hojat.smartgallery.extensions.tryDeleteFileDirItem
import ca.on.sudbury.hojat.smartgallery.extensions.underlineText
import ca.on.sudbury.hojat.smartgallery.extensions.updateDBDirectory
import ca.on.sudbury.hojat.smartgallery.extensions.updateWidgets
import ca.on.sudbury.hojat.smartgallery.helpers.DAY_SECONDS
import ca.on.sudbury.hojat.smartgallery.helpers.DIRECTORY
import ca.on.sudbury.hojat.smartgallery.helpers.FAVORITES
import ca.on.sudbury.hojat.smartgallery.helpers.FileLocation
import ca.on.sudbury.hojat.smartgallery.helpers.GET_ANY_INTENT
import ca.on.sudbury.hojat.smartgallery.helpers.GET_IMAGE_INTENT
import ca.on.sudbury.hojat.smartgallery.helpers.GET_VIDEO_INTENT
import ca.on.sudbury.hojat.smartgallery.helpers.GroupBy
import ca.on.sudbury.hojat.smartgallery.helpers.INVALID_NAVIGATION_BAR_COLOR
import ca.on.sudbury.hojat.smartgallery.helpers.MAX_COLUMN_COUNT
import ca.on.sudbury.hojat.smartgallery.helpers.MONTH_MILLISECONDS
import ca.on.sudbury.hojat.smartgallery.helpers.MediaFetcher
import ca.on.sudbury.hojat.smartgallery.helpers.MediaType
import ca.on.sudbury.hojat.smartgallery.helpers.PERMISSION_MEDIA_LOCATION
import ca.on.sudbury.hojat.smartgallery.helpers.PERMISSION_READ_STORAGE
import ca.on.sudbury.hojat.smartgallery.helpers.PERMISSION_WRITE_STORAGE
import ca.on.sudbury.hojat.smartgallery.helpers.PICKED_PATHS
import ca.on.sudbury.hojat.smartgallery.helpers.ProtectionType
import ca.on.sudbury.hojat.smartgallery.helpers.RECYCLE_BIN
import ca.on.sudbury.hojat.smartgallery.helpers.SET_WALLPAPER_INTENT
import ca.on.sudbury.hojat.smartgallery.helpers.SHOW_ALL
import ca.on.sudbury.hojat.smartgallery.helpers.SHOW_TEMP_HIDDEN_DURATION
import ca.on.sudbury.hojat.smartgallery.helpers.SKIP_AUTHENTICATION
import ca.on.sudbury.hojat.smartgallery.helpers.SORT_BY_DATE_MODIFIED
import ca.on.sudbury.hojat.smartgallery.helpers.SORT_BY_DATE_TAKEN
import ca.on.sudbury.hojat.smartgallery.helpers.SORT_BY_SIZE
import ca.on.sudbury.hojat.smartgallery.helpers.SORT_USE_NUMERIC_VALUE
import ca.on.sudbury.hojat.smartgallery.helpers.SmartGalleryTimeFormat
import ca.on.sudbury.hojat.smartgallery.helpers.ViewType
import ca.on.sudbury.hojat.smartgallery.helpers.getDefaultFileFilter
import ca.on.sudbury.hojat.smartgallery.jobs.NewPhotoFetcher
import ca.on.sudbury.hojat.smartgallery.models.Directory
import ca.on.sudbury.hojat.smartgallery.models.FileDirItem
import ca.on.sudbury.hojat.smartgallery.models.Medium
import ca.on.sudbury.hojat.smartgallery.models.Release
import ca.on.sudbury.hojat.smartgallery.repositories.SupportedExtensionsRepository
import ca.on.sudbury.hojat.smartgallery.settings.SettingsActivity
import ca.on.sudbury.hojat.smartgallery.usecases.BeVisibleOrGoneUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.CalculateDirectoryFileCountUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.CalculateDirectorySizeUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.CheckAppIconColorUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.HideKeyboardUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.IsNougatPlusUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.IsPathOnOtgUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.IsRPlusUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.IsSvgUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.LaunchCameraUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.RunOnBackgroundThreadUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.ToggleAppIconColorUseCase
import ca.on.sudbury.hojat.smartgallery.views.MyRecyclerView
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.OutputStream

class MainActivity : SimpleActivity(), DirectoryOperationsListener {

    private lateinit var binding: ActivityMainBinding

    private val PICK_MEDIA = 2
    private val PICK_WALLPAPER = 3
    private val LAST_MEDIA_CHECK_PERIOD = 3000L

    private var mIsPickImageIntent = false
    private var mIsPickVideoIntent = false
    private var mIsGetImageContentIntent = false
    private var mIsGetVideoContentIntent = false
    private var mIsGetAnyContentIntent = false
    private var mIsSetWallpaperIntent = false
    private var mAllowPickingMultiple = false
    private var mIsThirdPartyIntent = false
    private var mIsGettingDirs = false
    private var mLoadedInitialPhotos = false
    private var mIsPasswordProtectionPending = false
    private var mWasProtectionHandled = false
    private var mShouldStopFetching = false
    private var mIsSearchOpen = false
    private var mWasDefaultFolderChecked = false
    private var mWasMediaManagementPromptShown = false
    private var mLatestMediaId = 0L
    private var mLatestMediaDateId = 0L
    private var mCurrentPathPrefix =
        ""                 // used at "Group direct subfolders" for navigation
    private var mOpenedSubfolders =
        arrayListOf("")     // used at "Group direct subfolders" for navigating Up with the back button
    private var mDateFormat = ""
    private var mTimeFormat = ""
    private var mLastMediaHandler = Handler()
    private var mTempShowHiddenHandler = Handler()
    private var mZoomListener: MyRecyclerView.MyZoomListener? = null
    private var mSearchMenuItem: MenuItem? = null
    private var mLastMediaFetcher: MediaFetcher? = null
    private var mDirs = ArrayList<Directory>()

    private var mStoredAnimateGifs = true
    private var mStoredCropThumbnails = true
    private var mStoredScrollHorizontally = true
    private var mStoredTextColor = 0
    private var mStoredPrimaryColor = 0
    private var mStoredStyleString = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // all the necessary warm-up operations in the MainActivity
        val appId = BuildConfig.APPLICATION_ID

        baseConfig.internalStoragePath = getRealInternalStoragePath()

        //update the SD card path
        RunOnBackgroundThreadUseCase {
            val oldPath = baseConfig.sdCardPath
            baseConfig.sdCardPath = getSDCardPath()
            if (oldPath != baseConfig.sdCardPath) {
                baseConfig.sdTreeUri = ""
            }
        }

        baseConfig.appId = appId
        if (baseConfig.appRunCount == 0) {
            baseConfig.wasOrangeIconChecked = true
            CheckAppIconColorUseCase(this)
        } else if (!baseConfig.wasOrangeIconChecked) {
            baseConfig.wasOrangeIconChecked = true
            val primaryColor = resources.getColor(R.color.color_primary)
            if (baseConfig.appIconColor != primaryColor) {
                resources.getIntArray(R.array.md_app_icon_colors).toCollection(ArrayList())
                    .forEachIndexed { index, color ->
                        ToggleAppIconColorUseCase(this, appId, index, color, false)
                    }

                val defaultClassName =
                    "${baseConfig.appId.removeSuffix(".debug")}.activities.SplashActivity"
                packageManager.setComponentEnabledSetting(
                    ComponentName(baseConfig.appId, defaultClassName),
                    PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
                    PackageManager.DONT_KILL_APP
                )

                val orangeClassName =
                    "${baseConfig.appId.removeSuffix(".debug")}.activities.SplashActivity.Orange"
                packageManager.setComponentEnabledSetting(
                    ComponentName(baseConfig.appId, orangeClassName),
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
                )

                baseConfig.appIconColor = primaryColor
                baseConfig.lastIconColor = primaryColor
            }
        }

        baseConfig.appRunCount++

        if (baseConfig.appRunCount % 40 == 0 && !baseConfig.wasAppRated) {
            if (!resources.getBoolean(R.bool.hide_google_relations)) {
                RateStarsDialogFragment().show(supportFragmentManager, RateStarsDialogFragment.TAG)
            }
        }

        if (baseConfig.navigationBarColor == INVALID_NAVIGATION_BAR_COLOR && (window.attributes.flags and WindowManager.LayoutParams.FLAG_FULLSCREEN == 0)) {
            baseConfig.defaultNavigationBarColor = window.navigationBarColor
            baseConfig.navigationBarColor = window.navigationBarColor
        }


        if (savedInstanceState == null) {
            config.temporarilyShowHidden = false
            config.temporarilyShowExcluded = false
            config.tempSkipDeleteConfirmation = false
            removeTempFolder()
            checkRecycleBinItems()
            startNewPhotoFetcher()
        }

        mIsPickImageIntent = isPickImageIntent(intent)
        mIsPickVideoIntent = isPickVideoIntent(intent)
        mIsGetImageContentIntent = isGetImageContentIntent(intent)
        mIsGetVideoContentIntent = isGetVideoContentIntent(intent)
        mIsGetAnyContentIntent = isGetAnyContentIntent(intent)
        mIsSetWallpaperIntent = isSetWallpaperIntent(intent)
        mAllowPickingMultiple = intent.getBooleanExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)
        mIsThirdPartyIntent =
            mIsPickImageIntent || mIsPickVideoIntent || mIsGetImageContentIntent || mIsGetVideoContentIntent ||
                    mIsGetAnyContentIntent || mIsSetWallpaperIntent

        setupOptionsMenu()
        refreshMenuItems()

        binding.directoriesRefreshLayout.setOnRefreshListener { getDirectories() }
        storeStateVariables()
        checkWhatsNewDialog()

        mIsPasswordProtectionPending = config.isAppPasswordProtectionOn
        setupLatestMediaId()

        if (!config.wereFavoritesPinned) {
            config.addPinnedFolders(hashSetOf(FAVORITES))
            config.wereFavoritesPinned = true
        }

        if (!config.wasRecycleBinPinned) {
            config.addPinnedFolders(hashSetOf(RECYCLE_BIN))
            config.wasRecycleBinPinned = true
            config.saveFolderGrouping(SHOW_ALL, GroupBy.DateTakenDaily.id or GroupBy.Descending.id)
        }

        if (!config.wasSVGShowingHandled) {
            config.wasSVGShowingHandled = true
            if (config.filterMedia and MediaType.Svg.id == 0) {
                config.filterMedia += MediaType.Svg.id
            }
        }

        if (!config.wasSortingByNumericValueAdded) {
            config.wasSortingByNumericValueAdded = true
            config.sorting = config.sorting or SORT_USE_NUMERIC_VALUE
        }

        updateWidgets()
        registerFileUpdateListener()

        binding.directoriesSwitchSearching.setOnClickListener {
            launchSearchActivity()
        }

        // just request the permission, tryLoadGallery will then trigger in onResume
        handleMediaPermissions {
            if (!it) {
                Toast.makeText(this, R.string.no_storage_permissions, Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun handleMediaPermissions(callback: (granted: Boolean) -> Unit) {
        handlePermission(PERMISSION_WRITE_STORAGE) { granted ->
            callback(granted)
            if (granted && IsRPlusUseCase()) {
                handlePermission(PERMISSION_MEDIA_LOCATION) {}
                if (!mWasMediaManagementPromptShown) {
                    mWasMediaManagementPromptShown = true
                    handleMediaManagementPrompt { }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mTempShowHiddenHandler.removeCallbacksAndMessages(null)
    }

    override fun onResume() {
        super.onResume()
        config.isThirdPartyIntent = false
        mDateFormat = config.dateFormat
        mTimeFormat =
            if (baseConfig.use24HourFormat) SmartGalleryTimeFormat.FullDay.format else SmartGalleryTimeFormat.HalfDay.format

        setupToolbar(binding.directoriesToolbar, searchMenuItem = mSearchMenuItem)
        refreshMenuItems()

        if (mStoredAnimateGifs != config.animateGifs) {
            getRecyclerAdapter()?.updateAnimateGifs(config.animateGifs)
        }

        if (mStoredCropThumbnails != config.cropThumbnails) {
            getRecyclerAdapter()?.updateCropThumbnails(config.cropThumbnails)
        }

        if (mStoredScrollHorizontally != config.scrollHorizontally) {
            mLoadedInitialPhotos = false
            binding.directoriesGrid.adapter = null
            getDirectories()
        }

        if (mStoredTextColor != getProperTextColor()) {
            getRecyclerAdapter()?.updateTextColor(getProperTextColor())
        }

        val primaryColor = getProperPrimaryColor()
        if (mStoredPrimaryColor != primaryColor) {
            getRecyclerAdapter()?.updatePrimaryColor()
        }

        val styleString =
            "${config.folderStyle}${config.showFolderMediaCount}${config.limitFolderTitle}"
        if (mStoredStyleString != styleString) {
            setupAdapter(mDirs, forceRecreate = true)
        }

        binding.directoriesFastscroller.updateColors(primaryColor)
        binding.directoriesRefreshLayout.isEnabled = config.enablePullToRefresh
        getRecyclerAdapter()?.apply {
            dateFormat = config.dateFormat
            timeFormat =
                if (baseConfig.use24HourFormat) SmartGalleryTimeFormat.FullDay.format else SmartGalleryTimeFormat.HalfDay.format
        }

        binding.directoriesEmptyPlaceholder.setTextColor(getProperTextColor())
        binding.directoriesEmptyPlaceholder2.setTextColor(primaryColor)
        binding.directoriesSwitchSearching.setTextColor(primaryColor)
        binding.directoriesSwitchSearching.underlineText()
        binding.directoriesEmptyPlaceholder2.bringToFront()

        if (!mIsSearchOpen) {
            refreshMenuItems()
            if (mIsPasswordProtectionPending && !mWasProtectionHandled) {
                if (baseConfig.isAppPasswordProtectionOn) {
                    val callback: (hash: String, type: Int, success: Boolean) -> Unit =
                        { _, _, success ->
                            mWasProtectionHandled = success
                            if (success) {
                                mIsPasswordProtectionPending = false
                                tryLoadGallery()
                            } else {
                                finish()
                            }
                        }
                    SecurityDialogFragment(
                        baseConfig.appPasswordHash,
                        baseConfig.appProtectionType,
                        callback
                    ).show(supportFragmentManager, SecurityDialogFragment.TAG)
                } else {
                    mWasProtectionHandled = true
                    mIsPasswordProtectionPending = false
                    tryLoadGallery()
                }
            } else {
                tryLoadGallery()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        binding.directoriesRefreshLayout.isRefreshing = false
        mIsGettingDirs = false
        storeStateVariables()
        mLastMediaHandler.removeCallbacksAndMessages(null)
    }

    override fun onStop() {
        super.onStop()

        if (config.temporarilyShowHidden || config.tempSkipDeleteConfirmation || config.temporarilyShowExcluded) {
            mTempShowHiddenHandler.postDelayed({
                config.temporarilyShowHidden = false
                config.temporarilyShowExcluded = false
                config.tempSkipDeleteConfirmation = false
            }, SHOW_TEMP_HIDDEN_DURATION)
        } else {
            mTempShowHiddenHandler.removeCallbacksAndMessages(null)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isChangingConfigurations) {
            config.temporarilyShowHidden = false
            config.temporarilyShowExcluded = false
            config.tempSkipDeleteConfirmation = false
            mTempShowHiddenHandler.removeCallbacksAndMessages(null)
            removeTempFolder()
            unregisterFileUpdateListener()

            if (!config.showAll) {
                mLastMediaFetcher?.shouldStop = true
                GalleryDatabase.destroyInstance()
            }
        }
    }

    override fun onBackPressed() {
        if (mIsSearchOpen && mSearchMenuItem != null) {
            mSearchMenuItem!!.collapseActionView()
        } else if (config.groupDirectSubfolders) {
            if (mCurrentPathPrefix.isEmpty()) {
                super.onBackPressed()
            } else {
                mOpenedSubfolders.removeAt(mOpenedSubfolders.size - 1)
                mCurrentPathPrefix = mOpenedSubfolders.last()
                setupAdapter(mDirs)
            }
        } else {
            super.onBackPressed()
        }
    }

    private fun refreshMenuItems() {
        if (!mIsThirdPartyIntent) {
            val useBin = config.useRecycleBin
            binding.directoriesToolbar.menu.apply {
                findItem(R.id.increase_column_count).isVisible =
                    config.viewTypeFolders == ViewType.Grid.id && config.dirColumnCnt < MAX_COLUMN_COUNT
                findItem(R.id.reduce_column_count).isVisible =
                    config.viewTypeFolders == ViewType.Grid.id && config.dirColumnCnt > 1
                findItem(R.id.hide_the_recycle_bin).isVisible =
                    useBin && config.showRecycleBinAtFolders
                findItem(R.id.show_the_recycle_bin).isVisible =
                    useBin && !config.showRecycleBinAtFolders
                findItem(R.id.set_as_default_folder).isVisible = config.defaultFolder.isNotEmpty()
                setupSearch(this)
            }
        }

        binding.directoriesToolbar.menu.apply {
            findItem(R.id.temporarily_show_hidden).isVisible =
                (!IsRPlusUseCase() || isExternalStorageManager()) && !config.shouldShowHidden
            findItem(R.id.stop_showing_hidden).isVisible =
                (!IsRPlusUseCase() || isExternalStorageManager()) && config.temporarilyShowHidden

            findItem(R.id.temporarily_show_excluded).isVisible =
                !findItem(R.id.temporarily_show_hidden).isVisible && !config.temporarilyShowExcluded
            findItem(R.id.stop_showing_excluded).isVisible =
                !findItem(R.id.temporarily_show_hidden).isVisible && config.temporarilyShowExcluded
        }
    }


    private fun setupOptionsMenu() {
        val menuId = if (mIsThirdPartyIntent) {
            R.menu.menu_main_intent
        } else {
            R.menu.menu_main
        }

        binding.directoriesToolbar.inflateMenu(menuId)

        if (!mIsThirdPartyIntent) {
            setupSearch(binding.directoriesToolbar.menu)
        }

        binding.directoriesToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.sort -> showSortingDialog()
                R.id.filter -> showFilterMediaDialog()
                R.id.open_camera -> LaunchCameraUseCase(this)
                R.id.show_all -> showAllMedia()
                R.id.change_view_type -> changeViewType()
                R.id.temporarily_show_hidden -> tryToggleTemporarilyShowHidden()
                R.id.stop_showing_hidden -> tryToggleTemporarilyShowHidden()
                R.id.temporarily_show_excluded -> tryToggleTemporarilyShowExcluded()
                R.id.stop_showing_excluded -> tryToggleTemporarilyShowExcluded()
                R.id.create_new_folder -> createNewFolder()
                R.id.show_the_recycle_bin -> toggleRecycleBin(true)
                R.id.hide_the_recycle_bin -> toggleRecycleBin(false)
                R.id.increase_column_count -> increaseColumnCount()
                R.id.reduce_column_count -> reduceColumnCount()
                R.id.set_as_default_folder -> setAsDefaultFolder()
                R.id.settings -> {
                    // fire up settings page
                    HideKeyboardUseCase(this)
                    startActivity(Intent(applicationContext, SettingsActivity::class.java))
                }
                R.id.about -> launchAbout()
                else -> return@setOnMenuItemClickListener false
            }
            return@setOnMenuItemClickListener true
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(ProtectionType.WasHandled.id, mWasProtectionHandled)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        mWasProtectionHandled =
            savedInstanceState.getBoolean(ProtectionType.WasHandled.id, false)
    }

    private fun getRecyclerAdapter() = binding.directoriesGrid.adapter as? DirectoryAdapter

    private fun storeStateVariables() {
        mStoredTextColor = getProperTextColor()
        mStoredPrimaryColor = getProperPrimaryColor()
        config.apply {
            mStoredAnimateGifs = animateGifs
            mStoredCropThumbnails = cropThumbnails
            mStoredScrollHorizontally = scrollHorizontally
            mStoredStyleString = "$folderStyle$showFolderMediaCount$limitFolderTitle"
        }
    }

    private fun setupSearch(menu: Menu) {
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        mSearchMenuItem = menu.findItem(R.id.search)
        (mSearchMenuItem?.actionView as? SearchView)?.apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
            isSubmitButtonEnabled = false
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String) = false

                override fun onQueryTextChange(newText: String): Boolean {
                    if (mIsSearchOpen) {
                        setupAdapter(mDirs, newText)
                    }
                    return true
                }
            })
        }

        MenuItemCompat.setOnActionExpandListener(
            mSearchMenuItem,
            object : MenuItemCompat.OnActionExpandListener {
                override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                    binding.directoriesSwitchSearching.visibility = View.VISIBLE
                    mIsSearchOpen = true
                    binding.directoriesRefreshLayout.isEnabled = false
                    return true
                }

                // this triggers on device rotation too, avoid doing anything
                override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                    if (mIsSearchOpen) {
                        binding.directoriesSwitchSearching.visibility = View.GONE
                        mIsSearchOpen = false
                        binding.directoriesRefreshLayout.isEnabled = config.enablePullToRefresh
                        setupAdapter(mDirs, "")
                    }
                    return true
                }
            })
    }

    private fun startNewPhotoFetcher() {
        if (IsNougatPlusUseCase()) {
            val photoFetcher = NewPhotoFetcher()
            if (!photoFetcher.isScheduled(applicationContext)) {
                photoFetcher.scheduleJob(applicationContext)
            }
        }
    }

    private fun removeTempFolder() {
        if (config.tempFolderPath.isNotEmpty()) {
            val newFolder = File(config.tempFolderPath)
            if (getDoesFilePathExist(newFolder.absolutePath) && newFolder.isDirectory) {
                if (CalculateDirectorySizeUseCase(newFolder, true) == 0L &&
                    CalculateDirectoryFileCountUseCase(newFolder, true) == 0 && newFolder.list()
                        ?.isEmpty() == true
                ) {
                    Toast.makeText(
                        this,
                        String.format(getString(R.string.deleting_folder), config.tempFolderPath),
                        Toast.LENGTH_LONG
                    ).show()
                    tryDeleteFileDirItem(
                        newFolder.toFileDirItem(applicationContext),
                        allowDeleteFolder = true,
                        deleteFromDatabase = true
                    )
                }
            }
            config.tempFolderPath = ""
        }
    }

    private fun checkOTGPath() {
        RunOnBackgroundThreadUseCase {
            if (!config.wasOTGHandled && hasPermission(PERMISSION_WRITE_STORAGE) && hasOTGConnected() && config.otgPath.isEmpty()) {
                getStorageDirectories().firstOrNull {
                    it.trimEnd('/') != internalStoragePath && it.trimEnd(
                        '/'
                    ) != baseConfig.sdCardPath
                }?.apply {
                    config.wasOTGHandled = true
                    val otgPath = trimEnd('/')
                    config.otgPath = otgPath
                    config.addIncludedFolder(otgPath)
                }
            }
        }

    }

    private fun checkDefaultSpamFolders() {
        if (!config.spamFoldersChecked) {
            val spamFolders = arrayListOf(
                "/storage/emulated/0/Android/data/com.facebook.orca/files/stickers"
            )

            val otgPath = config.otgPath
            spamFolders.forEach {
                if (getDoesFilePathExist(it, otgPath)) {
                    config.addExcludedFolder(it)
                }
            }
            config.spamFoldersChecked = true
        }
    }

    private fun tryLoadGallery() {
        // avoid calling anything right after granting the permission, it will be called from onResume()
        val wasMissingPermission =
            config.appRunCount == 1 && !hasPermission(PERMISSION_WRITE_STORAGE)
        handleMediaPermissions {
            if (wasMissingPermission) {
                return@handleMediaPermissions
            }

            if (it) {
                if (!mWasDefaultFolderChecked) {
                    openDefaultFolder()
                    mWasDefaultFolderChecked = true
                }

                checkOTGPath()
                checkDefaultSpamFolders()

                if (config.showAll) {
                    showAllMedia()
                } else {
                    getDirectories()
                }

                setupLayoutManager()
            } else {
                Toast.makeText(this, R.string.no_storage_permissions, Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }


    private fun getDirectories() {
        if (mIsGettingDirs) {
            return
        }

        mShouldStopFetching = true
        mIsGettingDirs = true
        val getImagesOnly = mIsPickImageIntent || mIsGetImageContentIntent
        val getVideosOnly = mIsPickVideoIntent || mIsGetVideoContentIntent

        getCachedDirectories(getVideosOnly, getImagesOnly) {
            gotDirectories(addTempFolderIfNeeded(it))
        }
    }

    private fun launchSearchActivity() {
        HideKeyboardUseCase(this)
        Intent(this, SearchActivity::class.java).apply {
            startActivity(this)
        }
    }


    private fun showSortingDialog() {
        val callback = {
            binding.directoriesGrid.adapter = null
            if (config.directorySorting and SORT_BY_DATE_MODIFIED != 0 || config.directorySorting and SORT_BY_DATE_TAKEN != 0) {
                getDirectories()
            } else {
                RunOnBackgroundThreadUseCase {
                    gotDirectories(getCurrentlyDisplayedDirs())
                }
            }

            getRecyclerAdapter()?.directorySorting = config.directorySorting
        }
        ChangeSortingDialogFragment(
            isDirectorySorting = true,
            showFolderCheckbox = false,
            callback = callback
        ).show(supportFragmentManager, ChangeSortingDialogFragment.TAG)
    }

    private fun showFilterMediaDialog() {

        val callbackAfterDialogConfirmed: (Int) -> Unit = { _ ->
            mShouldStopFetching = true
            binding.directoriesRefreshLayout.isRefreshing = true
            binding.directoriesGrid.adapter = null
            getDirectories()
        }
        FilterMediaDialogFragment(callbackAfterDialogConfirmed).show(
            supportFragmentManager,
            FilterMediaDialogFragment.TAG
        )
    }

    private fun showAllMedia() {
        config.showAll = true
        Intent(this, MediaActivity::class.java).apply {
            putExtra(DIRECTORY, "")

            if (mIsThirdPartyIntent) {
                handleMediaIntent(this)
            } else {
                HideKeyboardUseCase
                startActivity(this)
                finish()
            }
        }
    }

    private fun changeViewType() {

        val callback = {
            refreshMenuItems()
            setupLayoutManager()
            binding.directoriesGrid.adapter = null
            setupAdapter(getRecyclerAdapter()?.dirs ?: mDirs)
        }
        ChangeViewTypeDialogFragment(fromFoldersView = true, callback = callback).show(
            supportFragmentManager,
            ChangeViewTypeDialogFragment.TAG
        )
    }

    private fun tryToggleTemporarilyShowHidden() {
        if (config.temporarilyShowHidden) {
            toggleTemporarilyShowHidden(false)
        } else {
            handleHiddenFolderPasswordProtection {
                toggleTemporarilyShowHidden(true)
            }
        }
    }

    private fun toggleTemporarilyShowHidden(show: Boolean) {
        mLoadedInitialPhotos = false
        config.temporarilyShowHidden = show
        binding.directoriesGrid.adapter = null
        getDirectories()
        refreshMenuItems()
    }

    private fun tryToggleTemporarilyShowExcluded() {
        if (config.temporarilyShowExcluded) {
            toggleTemporarilyShowExcluded(false)
        } else {
            handleExcludedFolderPasswordProtection {
                toggleTemporarilyShowExcluded(true)
            }
        }
    }

    private fun toggleTemporarilyShowExcluded(show: Boolean) {
        mLoadedInitialPhotos = false
        config.temporarilyShowExcluded = show
        binding.directoriesGrid.adapter = null
        getDirectories()
        refreshMenuItems()
    }

    override fun deleteFolders(folders: ArrayList<File>) {
        val fileDirItems =
            folders.asSequence().filter { it.isDirectory }
                .map { FileDirItem(it.absolutePath, it.name, true) }
                .toMutableList() as ArrayList<FileDirItem>
        when {
            fileDirItems.isEmpty() -> return
            fileDirItems.size == 1 -> {
                try {
                    Toast.makeText(
                        this,
                        String.format(
                            getString(R.string.deleting_folder),
                            fileDirItems.first().name
                        ), Toast.LENGTH_LONG
                    ).show()
                } catch (e: Exception) {
                    Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
                }
            }
            else -> {
                val baseString =
                    if (config.useRecycleBin) R.plurals.moving_items_into_bin else R.plurals.delete_items
                val deletingItems =
                    resources.getQuantityString(baseString, fileDirItems.size, fileDirItems.size)
                Toast.makeText(this, deletingItems, Toast.LENGTH_LONG).show()
            }
        }

        val itemsToDelete = ArrayList<FileDirItem>()
        val filter = config.filterMedia
        val showHidden = config.shouldShowHidden
        fileDirItems.filter { it.isDirectory }.forEach { it ->
            val files = File(it.path).listFiles()
            files?.filter {
                it.absolutePath.isMediaFile() && (showHidden || !it.name.startsWith('.')) &&
                        ((
                                SupportedExtensionsRepository.photoExtensions.any { extension ->
                                    it.absolutePath.endsWith(extension, true)
                                } && filter and MediaType.Image.id != 0) ||
                                (SupportedExtensionsRepository.videoExtensions.any { extension ->
                                    it.absolutePath.endsWith(extension, true)
                                } && filter and MediaType.Video.id != 0) ||
                                (it.absolutePath.endsWith(
                                    ".gif",
                                    true
                                ) && filter and MediaType.Gif.id != 0) ||
                                (SupportedExtensionsRepository.rawExtensions.any { extension ->
                                    it.absolutePath.endsWith(extension, true)
                                } && filter and MediaType.Raw.id != 0) ||
                                (IsSvgUseCase(it.absolutePath) && filter and MediaType.Svg.id != 0))
            }?.mapTo(itemsToDelete) { it.toFileDirItem(applicationContext) }
        }

        if (config.useRecycleBin) {
            val pathsToDelete = ArrayList<String>()
            itemsToDelete.mapTo(pathsToDelete) { it.path }

            movePathsInRecycleBin(pathsToDelete) {
                if (it) {
                    deleteFilteredFileDirItems(itemsToDelete, folders)
                } else {
                    Toast.makeText(this, R.string.unknown_error_occurred, Toast.LENGTH_LONG).show()
                }
            }
        } else {
            deleteFilteredFileDirItems(itemsToDelete, folders)
        }
    }

    private fun deleteFilteredFileDirItems(
        fileDirItems: ArrayList<FileDirItem>,
        folders: ArrayList<File>
    ) {
        val otgPath = config.otgPath
        deleteFiles(fileDirItems) {
            runOnUiThread {
                refreshItems()
            }

            RunOnBackgroundThreadUseCase {

                folders.filter { !getDoesFilePathExist(it.absolutePath, otgPath) }.forEach {
                    GalleryDatabase.getInstance(applicationContext).DirectoryDao()
                        .deleteDirPath(it.absolutePath)
                }

                if (config.deleteEmptyFolders) {
                    folders.filter {
                        !it.absolutePath.isDownloadsFolder() && it.isDirectory && it.toFileDirItem(
                            this
                        ).getProperFileCount(this, true) == 0
                    }
                        .forEach {
                            tryDeleteFileDirItem(
                                it.toFileDirItem(this),
                                allowDeleteFolder = true,
                                deleteFromDatabase = true
                            )
                        }
                }
            }
        }
    }

    private fun setupLayoutManager() {
        if (config.viewTypeFolders == ViewType.Grid.id) {
            setupGridLayoutManager()
        } else {
            setupListLayoutManager()
        }

        (binding.directoriesRefreshLayout.layoutParams as RelativeLayout.LayoutParams).addRule(
            RelativeLayout.BELOW,
            R.id.directories_switch_searching
        )
    }

    private fun setupGridLayoutManager() {
        val layoutManager = binding.directoriesGrid.layoutManager as MyGridLayoutManager
        if (config.scrollHorizontally) {
            layoutManager.orientation = RecyclerView.HORIZONTAL
            binding.directoriesRefreshLayout.layoutParams =
                RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
        } else {
            layoutManager.orientation = RecyclerView.VERTICAL
            binding.directoriesRefreshLayout.layoutParams =
                RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
        }

        layoutManager.spanCount = config.dirColumnCnt
    }

    private fun setupListLayoutManager() {
        val layoutManager = binding.directoriesGrid.layoutManager as MyGridLayoutManager
        layoutManager.spanCount = 1
        layoutManager.orientation = RecyclerView.VERTICAL
        binding.directoriesRefreshLayout.layoutParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        mZoomListener = null
    }

    private fun initZoomListener() {
        if (config.viewTypeFolders == ViewType.Grid.id) {
            val layoutManager = binding.directoriesGrid.layoutManager as MyGridLayoutManager
            mZoomListener = object : MyRecyclerView.MyZoomListener {
                override fun zoomIn() {
                    if (layoutManager.spanCount > 1) {
                        reduceColumnCount()
                        getRecyclerAdapter()?.finishActMode()
                    }
                }

                override fun zoomOut() {
                    if (layoutManager.spanCount < MAX_COLUMN_COUNT) {
                        increaseColumnCount()
                        getRecyclerAdapter()?.finishActMode()
                    }
                }
            }
        } else {
            mZoomListener = null
        }
    }


    private fun toggleRecycleBin(show: Boolean) {
        config.showRecycleBinAtFolders = show
        refreshMenuItems()
        RunOnBackgroundThreadUseCase {
            var dirs = getCurrentlyDisplayedDirs()
            if (!show) {
                dirs = dirs.filter { it.path != RECYCLE_BIN } as ArrayList<Directory>
            }
            gotDirectories(dirs)
        }
    }


    private fun createNewFolder() {
        val callback: (String) -> Unit = { pickedPath ->
            val callback: (String) -> Unit = { path ->
                config.tempFolderPath = path
                RunOnBackgroundThreadUseCase {
                    gotDirectories(addTempFolderIfNeeded(getCurrentlyDisplayedDirs()))
                }
            }
            CreateNewFolderDialogFragment(pickedPath, callback).show(
                supportFragmentManager,
                CreateNewFolderDialogFragment.TAG
            )
        }
        FilePickerDialogFragment(
            internalStoragePath,
            false,
            config.shouldShowHidden,
            showFAB = false,
            canAddShowHiddenButton = true,
            callback = callback
        ).show(supportFragmentManager, FilePickerDialogFragment.TAG)
    }

    private fun increaseColumnCount() {
        config.dirColumnCnt =
            ++(binding.directoriesGrid.layoutManager as MyGridLayoutManager).spanCount
        columnCountChanged()
    }

    private fun reduceColumnCount() {
        config.dirColumnCnt =
            --(binding.directoriesGrid.layoutManager as MyGridLayoutManager).spanCount
        columnCountChanged()
    }

    private fun columnCountChanged() {
        refreshMenuItems()
        getRecyclerAdapter()?.apply {
            notifyItemRangeChanged(0, dirs.size)
        }
    }

    private fun isPickImageIntent(intent: Intent) =
        isPickIntent(intent) && (hasImageContentData(intent) || isImageType(intent))

    private fun isPickVideoIntent(intent: Intent) =
        isPickIntent(intent) && (hasVideoContentData(intent) || isVideoType(intent))

    private fun isPickIntent(intent: Intent) = intent.action == Intent.ACTION_PICK

    private fun isGetContentIntent(intent: Intent) =
        intent.action == Intent.ACTION_GET_CONTENT && intent.type != null

    private fun isGetImageContentIntent(intent: Intent) = isGetContentIntent(intent) &&
            (intent.type!!.startsWith("image/") || intent.type == Images.Media.CONTENT_TYPE)

    private fun isGetVideoContentIntent(intent: Intent) = isGetContentIntent(intent) &&
            (intent.type!!.startsWith("video/") || intent.type == Video.Media.CONTENT_TYPE)

    private fun isGetAnyContentIntent(intent: Intent) =
        isGetContentIntent(intent) && intent.type == "*/*"

    private fun isSetWallpaperIntent(intent: Intent?) =
        intent?.action == Intent.ACTION_SET_WALLPAPER

    private fun hasImageContentData(intent: Intent) =
        (intent.data == Images.Media.EXTERNAL_CONTENT_URI ||
                intent.data == Images.Media.INTERNAL_CONTENT_URI)

    private fun hasVideoContentData(intent: Intent) =
        (intent.data == Video.Media.EXTERNAL_CONTENT_URI ||
                intent.data == Video.Media.INTERNAL_CONTENT_URI)

    private fun isImageType(intent: Intent) =
        (intent.type?.startsWith("image/") == true || intent.type == Images.Media.CONTENT_TYPE)

    private fun isVideoType(intent: Intent) =
        (intent.type?.startsWith("video/") == true || intent.type == Video.Media.CONTENT_TYPE)

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_MEDIA && resultData != null) {
                val resultIntent = Intent()
                var resultUri: Uri? = null
                if (mIsThirdPartyIntent) {
                    when {
                        intent.extras?.containsKey(MediaStore.EXTRA_OUTPUT) == true && intent.flags and Intent.FLAG_GRANT_WRITE_URI_PERMISSION != 0 -> {
                            resultUri = fillExtraOutput(resultData)
                        }
                        resultData.extras?.containsKey(PICKED_PATHS) == true -> fillPickedPaths(
                            resultData,
                            resultIntent
                        )
                        else -> fillIntentPath(resultData, resultIntent)
                    }
                }

                if (resultUri != null) {
                    resultIntent.data = resultUri
                    resultIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                setResult(RESULT_OK, resultIntent)
                finish()
            } else if (requestCode == PICK_WALLPAPER) {
                setResult(RESULT_OK)
                finish()
            }
        }
        super.onActivityResult(requestCode, resultCode, resultData)
    }

    private fun fillExtraOutput(resultData: Intent): Uri? {
        val file = File(resultData.data!!.path!!)
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        try {
            val output = intent.extras!!.get(MediaStore.EXTRA_OUTPUT) as Uri
            inputStream = FileInputStream(file)
            outputStream = contentResolver.openOutputStream(output)
            inputStream.copyTo(outputStream!!)
        } catch (e: SecurityException) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
        } catch (ignored: FileNotFoundException) {
            return getFilePublicUri(file, BuildConfig.APPLICATION_ID)
        } finally {
            inputStream?.close()
            outputStream?.close()
        }

        return null
    }

    private fun fillPickedPaths(resultData: Intent, resultIntent: Intent) {
        val paths = resultData.extras!!.getStringArrayList(PICKED_PATHS)
        val uris =
            paths!!.map { getFilePublicUri(File(it), BuildConfig.APPLICATION_ID) } as ArrayList
        val clipData =
            ClipData("Attachment", arrayOf("image/*", "video/*"), ClipData.Item(uris.removeAt(0)))

        uris.forEach {
            clipData.addItem(ClipData.Item(it))
        }

        resultIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        resultIntent.clipData = clipData
    }

    private fun fillIntentPath(resultData: Intent, resultIntent: Intent) {
        val data = resultData.data
        val path = if (data.toString().startsWith("/")) data.toString() else data!!.path
        val uri = getFilePublicUri(File(path!!), BuildConfig.APPLICATION_ID)
        val type = path.getMimeType()
        resultIntent.setDataAndTypeAndNormalize(uri, type)
        resultIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    private fun itemClicked(path: String) {
        handleLockedFolderOpening(path) { success ->
            if (success) {
                Intent(this, MediaActivity::class.java).apply {
                    putExtra(SKIP_AUTHENTICATION, true)
                    putExtra(DIRECTORY, path)
                    handleMediaIntent(this)
                }
            }
        }
    }

    private fun handleMediaIntent(intent: Intent) {
        HideKeyboardUseCase(this)
        intent.apply {
            if (mIsSetWallpaperIntent) {
                putExtra(SET_WALLPAPER_INTENT, true)
                startActivityForResult(this, PICK_WALLPAPER)
            } else {
                putExtra(GET_IMAGE_INTENT, mIsPickImageIntent || mIsGetImageContentIntent)
                putExtra(GET_VIDEO_INTENT, mIsPickVideoIntent || mIsGetVideoContentIntent)
                putExtra(GET_ANY_INTENT, mIsGetAnyContentIntent)
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, mAllowPickingMultiple)
                startActivityForResult(this, PICK_MEDIA)
            }
        }
    }


    private fun gotDirectories(newDirs: ArrayList<Directory>) {
        mIsGettingDirs = false
        mShouldStopFetching = false

        // if hidden item showing is disabled but all Favorite items are hidden, hide the Favorites folder
        if (!config.shouldShowHidden) {
            val favoritesFolder = newDirs.firstOrNull { it.areFavorites() }
            if (favoritesFolder != null && favoritesFolder.tmb.getFilenameFromPath()
                    .startsWith('.')
            ) {
                newDirs.remove(favoritesFolder)
            }
        }

        val dirs = getSortedDirectories(newDirs)
        if (config.groupDirectSubfolders) {
            mDirs = dirs.clone() as ArrayList<Directory>
        }

        var isPlaceholderVisible = dirs.isEmpty()

        runOnUiThread {
            checkPlaceholderVisibility(dirs)
            setupAdapter(dirs.clone() as ArrayList<Directory>)
        }

        // cached folders have been loaded, recheck folders one by one starting with the first displayed
        mLastMediaFetcher?.shouldStop = true
        mLastMediaFetcher = MediaFetcher(applicationContext)
        val getImagesOnly = mIsPickImageIntent || mIsGetImageContentIntent
        val getVideosOnly = mIsPickVideoIntent || mIsGetVideoContentIntent
        val favoritePaths = getFavoritePaths()
        val hiddenString = getString(R.string.hidden)
        val albumCovers = config.parseAlbumCovers()
        val includedFolders = config.includedFolders
        val noMediaFolders = getNoMediaFoldersSync()
        val tempFolderPath = config.tempFolderPath
        val getProperFileSize = config.directorySorting and SORT_BY_SIZE != 0
        val dirPathsToRemove = ArrayList<String>()
        val lastModifieds = mLastMediaFetcher!!.getLastModifieds()
        val dateTakens = mLastMediaFetcher!!.getDateTakens()

        if (config.showRecycleBinAtFolders && !config.showRecycleBinLast && !dirs.map { it.path }
                .contains(RECYCLE_BIN)) {
            try {
                if (mediaDB.getDeletedMediaCount() > 0) {
                    val recycleBin = Directory().apply {
                        path = RECYCLE_BIN
                        name = getString(R.string.recycle_bin)
                        location = FileLocation.Internal.id
                    }

                    dirs.add(0, recycleBin)
                }
            } catch (ignored: Exception) {
            }
        }

        if (dirs.map { it.path }.contains(FAVORITES)) {
            if (mediaDB.getFavoritesCount() > 0) {
                val favorites = Directory().apply {
                    path = FAVORITES
                    name = getString(R.string.favorites)
                    location = FileLocation.Internal.id
                }

                dirs.add(0, favorites)
            }
        }

        // fetch files from MediaStore only, unless the app has the MANAGE_EXTERNAL_STORAGE permission on Android 11+
        val android11Files = mLastMediaFetcher?.getAndroid11FolderMedia(
            getImagesOnly,
            getVideosOnly,
            favoritePaths,
            getFavoritePathsOnly = false,
            getProperDateTaken = true,
            dateTakens = dateTakens
        )
        try {
            for (directory in dirs) {
                if (mShouldStopFetching || isDestroyed || isFinishing) {
                    return
                }

                val sorting = config.getFolderSorting(directory.path)
                val grouping = config.getFolderGrouping(directory.path)
                val getProperDateTaken = config.directorySorting and SORT_BY_DATE_TAKEN != 0 ||
                        sorting and SORT_BY_DATE_TAKEN != 0 ||
                        grouping and GroupBy.DateTakenDaily.id != 0 ||
                        grouping and GroupBy.DateTakenMonthly.id != 0

                val getProperLastModified =
                    config.directorySorting and SORT_BY_DATE_MODIFIED != 0 ||
                            sorting and SORT_BY_DATE_MODIFIED != 0 ||
                            grouping and GroupBy.LastModifiedDaily.id != 0 ||
                            grouping and GroupBy.LastModifiedMonthly.id != 0

                val curMedia = mLastMediaFetcher!!.getFilesFrom(
                    directory.path,
                    getImagesOnly,
                    getVideosOnly,
                    getProperDateTaken,
                    getProperLastModified,
                    getProperFileSize,
                    favoritePaths,
                    false,
                    lastModifieds,
                    dateTakens,
                    android11Files
                )

                val newDir = if (curMedia.isEmpty()) {
                    if (directory.path != tempFolderPath) {
                        dirPathsToRemove.add(directory.path)
                    }
                    directory
                } else {
                    createDirectoryFromMedia(
                        directory.path,
                        curMedia,
                        albumCovers,
                        hiddenString,
                        includedFolders,
                        getProperFileSize,
                        noMediaFolders
                    )
                }

                // we are looping through the already displayed folders looking for changes, do not do anything if nothing changed
                if (directory.copy(subfoldersCount = 0, subfoldersMediaCount = 0) == newDir) {
                    continue
                }

                directory.apply {
                    tmb = newDir.tmb
                    name = newDir.name
                    mediaCnt = newDir.mediaCnt
                    modified = newDir.modified
                    taken = newDir.taken
                    this@apply.size = newDir.size
                    types = newDir.types
                    sortValue = getDirectorySortingValue(curMedia, path, name, size)
                }

                setupAdapter(dirs)

                // update directories and media files in the local db, delete invalid items. Intentionally creating a new thread
                updateDBDirectory(directory)
                if (!directory.isRecycleBin() && !directory.areFavorites()) {
                    Thread {
                        try {
                            mediaDB.insertAll(curMedia)
                        } catch (ignored: Exception) {
                        }
                    }.start()
                }

                if (!directory.isRecycleBin()) {
                    getCachedMedia(directory.path, getVideosOnly, getImagesOnly) {
                        val mediaToDelete = ArrayList<Medium>()
                        it.forEach {
                            if (!curMedia.contains(it)) {
                                val medium = it as? Medium
                                val path = medium?.path
                                if (path != null) {
                                    mediaToDelete.add(medium)
                                }
                            }
                        }
                        mediaDB.deleteMedia(*mediaToDelete.toTypedArray())
                    }
                }
            }

            if (dirPathsToRemove.isNotEmpty()) {
                val dirsToRemove = dirs.filter { dirPathsToRemove.contains(it.path) }
                dirsToRemove.forEach {
                    GalleryDatabase.getInstance(applicationContext).DirectoryDao()
                        .deleteDirPath(it.path)
                }
                dirs.removeAll(dirsToRemove)
                setupAdapter(dirs)
            }
        } catch (ignored: Exception) {
        }

        val foldersToScan = mLastMediaFetcher!!.getFoldersToScan()
        foldersToScan.remove(FAVORITES)
        foldersToScan.add(0, FAVORITES)
        if (config.showRecycleBinAtFolders) {
            if (foldersToScan.contains(RECYCLE_BIN)) {
                foldersToScan.remove(RECYCLE_BIN)
                foldersToScan.add(0, RECYCLE_BIN)
            } else {
                foldersToScan.add(0, RECYCLE_BIN)
            }
        } else {
            foldersToScan.remove(RECYCLE_BIN)
        }

        dirs.filterNot { it.path == RECYCLE_BIN || it.path == FAVORITES }.forEach {
            foldersToScan.remove(it.path)
        }

        // check the remaining folders which were not cached at all yet
        for (folder in foldersToScan) {
            if (mShouldStopFetching || isDestroyed || isFinishing) {
                return
            }

            val sorting = config.getFolderSorting(folder)
            val grouping = config.getFolderGrouping(folder)
            val getProperDateTaken = config.directorySorting and SORT_BY_DATE_TAKEN != 0 ||
                    sorting and SORT_BY_DATE_TAKEN != 0 ||
                    grouping and GroupBy.DateTakenDaily.id != 0 ||
                    grouping and GroupBy.DateTakenMonthly.id != 0

            val getProperLastModified = config.directorySorting and SORT_BY_DATE_MODIFIED != 0 ||
                    sorting and SORT_BY_DATE_MODIFIED != 0 ||
                    grouping and GroupBy.LastModifiedDaily.id != 0 ||
                    grouping and GroupBy.LastModifiedMonthly.id != 0

            val newMedia = mLastMediaFetcher!!.getFilesFrom(
                folder, getImagesOnly, getVideosOnly, getProperDateTaken, getProperLastModified,
                getProperFileSize, favoritePaths, false, lastModifieds, dateTakens, android11Files
            )

            if (newMedia.isEmpty()) {
                continue
            }

            if (isPlaceholderVisible) {
                isPlaceholderVisible = false
                runOnUiThread {
                    binding.directoriesEmptyPlaceholder.visibility = View.GONE
                    binding.directoriesEmptyPlaceholder2.visibility = View.GONE
                    binding.directoriesFastscroller.visibility = View.VISIBLE
                }
            }

            val newDir = createDirectoryFromMedia(
                folder,
                newMedia,
                albumCovers,
                hiddenString,
                includedFolders,
                getProperFileSize,
                noMediaFolders
            )
            dirs.add(newDir)
            setupAdapter(dirs)

            // make sure to create a new thread for these operations, dont just use the common bg thread
            Thread {
                try {
                    GalleryDatabase.getInstance(applicationContext).DirectoryDao().insert(newDir)
                    if (folder != RECYCLE_BIN && folder != FAVORITES) {
                        mediaDB.insertAll(newMedia)
                    }
                } catch (ignored: Exception) {
                }
            }.start()
        }

        mLoadedInitialPhotos = true
        if (config.appRunCount > 1) {
            checkLastMediaChanged()
        }

        runOnUiThread {
            binding.directoriesRefreshLayout.isRefreshing = false
            checkPlaceholderVisibility(dirs)
        }

        checkInvalidDirectories(dirs)
        if (mDirs.size > 50) {
            excludeSpamFolders()
        }

        val excludedFolders = config.excludedFolders
        val everShownFolders = config.everShownFolders.toMutableSet() as HashSet<String>

        // do not add excluded folders and their subfolders at everShownFolders
        dirs.filter { dir ->
            if (excludedFolders.any { dir.path.startsWith(it) }) {
                return@filter false
            }
            return@filter true
        }.mapTo(everShownFolders) { it.path }

        try {
            // scan the internal storage from time to time for new folders
            if (config.appRunCount == 1 || config.appRunCount % 30 == 0) {
                everShownFolders.addAll(getFoldersWithMedia(config.internalStoragePath))
            }

            // catch some extreme exceptions like too many everShownFolders for storing, shouldnt really happen
            config.everShownFolders = everShownFolders
        } catch (e: Exception) {
            config.everShownFolders = HashSet()
        }

        mDirs = dirs.clone() as ArrayList<Directory>
    }

    private fun setAsDefaultFolder() {
        config.defaultFolder = ""
        refreshMenuItems()
    }

    private fun openDefaultFolder() {
        if (config.defaultFolder.isEmpty()) {
            return
        }

        val defaultDir = File(config.defaultFolder)

        if ((!defaultDir.exists() || !defaultDir.isDirectory) && (config.defaultFolder != RECYCLE_BIN && config.defaultFolder != FAVORITES)) {
            config.defaultFolder = ""
            return
        }

        Intent(this, MediaActivity::class.java).apply {
            putExtra(DIRECTORY, config.defaultFolder)
            handleMediaIntent(this)
        }
    }

    private fun checkPlaceholderVisibility(dirs: ArrayList<Directory>) {
        BeVisibleOrGoneUseCase(
            binding.directoriesEmptyPlaceholder,
            dirs.isEmpty() && mLoadedInitialPhotos
        )
        BeVisibleOrGoneUseCase(
            binding.directoriesEmptyPlaceholder2,
            dirs.isEmpty() && mLoadedInitialPhotos
        )

        if (mIsSearchOpen) {
            binding.directoriesEmptyPlaceholder.text = getString(R.string.no_items_found)
            binding.directoriesEmptyPlaceholder2.visibility = View.GONE
        } else if (dirs.isEmpty() && config.filterMedia == getDefaultFileFilter()) {
            if (IsRPlusUseCase() && !isExternalStorageManager()) {
                binding.directoriesEmptyPlaceholder.text = getString(R.string.no_items_found)
                binding.directoriesEmptyPlaceholder2.visibility = View.GONE
            } else {
                binding.directoriesEmptyPlaceholder.text = getString(R.string.no_media_add_included)
                binding.directoriesEmptyPlaceholder2.text = getString(R.string.add_folder)
            }

            binding.directoriesEmptyPlaceholder2.setOnClickListener {
                showAddIncludedFolderDialog {
                    refreshItems()
                }
            }
        } else {
            binding.directoriesEmptyPlaceholder.text = getString(R.string.no_media_with_filters)
            binding.directoriesEmptyPlaceholder2.text =
                getString(R.string.change_filters_underlined)

            binding.directoriesEmptyPlaceholder2.setOnClickListener {
                showFilterMediaDialog()
            }
        }

        binding.directoriesEmptyPlaceholder2.underlineText()
        BeVisibleOrGoneUseCase(
            binding.directoriesFastscroller,
            binding.directoriesEmptyPlaceholder.visibility == View.GONE
        )
    }

    private fun setupAdapter(
        dirs: ArrayList<Directory>,
        textToSearch: String = "",
        forceRecreate: Boolean = false
    ) {
        val currAdapter = binding.directoriesGrid.adapter
        val distinctDirs =
            dirs.distinctBy { it.path.getDistinctPath() }.toMutableList() as ArrayList<Directory>
        val sortedDirs = getSortedDirectories(distinctDirs)
        var dirsToShow =
            getDirsToShow(sortedDirs, mDirs, mCurrentPathPrefix).clone() as ArrayList<Directory>

        if (currAdapter == null || forceRecreate) {
            initZoomListener()
            DirectoryAdapter(
                this,
                dirsToShow,
                this,
                binding.directoriesGrid,
                isPickIntent(intent) || isGetAnyContentIntent(intent),
                binding.directoriesRefreshLayout
            ) {
                val clickedDir = it as Directory
                val path = clickedDir.path
                if (clickedDir.subfoldersCount == 1 || !config.groupDirectSubfolders) {
                    if (path != config.tempFolderPath) {
                        itemClicked(path)
                    }
                } else {
                    mCurrentPathPrefix = path
                    mOpenedSubfolders.add(path)
                    setupAdapter(mDirs, "")
                }
            }.apply {
                setupZoomListener(mZoomListener)
                runOnUiThread {
                    binding.directoriesGrid.adapter = this
                    setupScrollDirection()

                    if (config.viewTypeFolders == ViewType.List.id && areSystemAnimationsEnabled) {
                        binding.directoriesGrid.scheduleLayoutAnimation()
                    }
                }
            }
        } else {
            runOnUiThread {
                if (textToSearch.isNotEmpty()) {
                    dirsToShow = dirsToShow.filter { it.name.contains(textToSearch, true) }
                        .sortedBy { !it.name.startsWith(textToSearch, true) }
                        .toMutableList() as ArrayList
                }
                checkPlaceholderVisibility(dirsToShow)

                (binding.directoriesGrid.adapter as? DirectoryAdapter)?.updateDirs(dirsToShow)
            }
        }

        // recyclerview sometimes becomes empty at init/update, triggering an invisible refresh like this seems to work fine
        binding.directoriesGrid.postDelayed({
            binding.directoriesGrid.scrollBy(0, 0)
        }, 500)
    }

    private fun setupScrollDirection() {
        val scrollHorizontally =
            config.scrollHorizontally && config.viewTypeFolders == ViewType.Grid.id
        binding.directoriesFastscroller.setScrollVertically(!scrollHorizontally)
    }

    private fun checkInvalidDirectories(dirs: ArrayList<Directory>) {
        val invalidDirs = ArrayList<Directory>()
        val otgPath = config.otgPath
        dirs.filter { !it.areFavorites() && !it.isRecycleBin() }.forEach { it ->
            if (!getDoesFilePathExist(it.path, otgPath)) {
                invalidDirs.add(it)
            } else if (it.path != config.tempFolderPath && (!IsRPlusUseCase() || isExternalStorageManager())) {
                // avoid calling file.list() or listfiles() on Android 11+, it became way too slow
                val children = if (
                    IsPathOnOtgUseCase(this, it.path)) {
                    getOTGFolderChildrenNames(it.path)
                } else {
                    File(it.path).list()?.asList()
                }

                val hasMediaFile = children?.any {
                    it != null && (it.isMediaFile() || (it.startsWith(
                        "img_",
                        true
                    ) && File(it).isDirectory))
                } ?: false

                if (!hasMediaFile) {
                    invalidDirs.add(it)
                }
            }
        }

        if (getFavoritePaths().isEmpty()) {
            val favoritesFolder = dirs.firstOrNull { it.areFavorites() }
            if (favoritesFolder != null) {
                invalidDirs.add(favoritesFolder)
            }
        }

        if (config.useRecycleBin) {
            try {
                val binFolder = dirs.firstOrNull { it.path == RECYCLE_BIN }
                if (binFolder != null && mediaDB.getDeletedMedia().isEmpty()) {
                    invalidDirs.add(binFolder)
                }
            } catch (ignored: Exception) {
            }
        }

        if (invalidDirs.isNotEmpty()) {
            dirs.removeAll(invalidDirs)
            setupAdapter(dirs)
            invalidDirs.forEach {
                try {
                    GalleryDatabase.getInstance(applicationContext).DirectoryDao()
                        .deleteDirPath(it.path)
                } catch (ignored: Exception) {
                }
            }
        }
    }

    private fun getCurrentlyDisplayedDirs() = getRecyclerAdapter()?.dirs ?: ArrayList()

    private fun setupLatestMediaId() {
        RunOnBackgroundThreadUseCase {
            if (hasPermission(PERMISSION_READ_STORAGE)) {
                mLatestMediaId = getLatestMediaId()
                mLatestMediaDateId = getLatestMediaByDateId()
            }
        }
    }

    private fun checkLastMediaChanged() {
        if (isDestroyed) {
            return
        }

        mLastMediaHandler.postDelayed({
            RunOnBackgroundThreadUseCase {
                val mediaId = getLatestMediaId()
                val mediaDateId = getLatestMediaByDateId()
                if (mLatestMediaId != mediaId || mLatestMediaDateId != mediaDateId) {
                    mLatestMediaId = mediaId
                    mLatestMediaDateId = mediaDateId
                    runOnUiThread {
                        getDirectories()
                    }
                } else {
                    mLastMediaHandler.removeCallbacksAndMessages(null)
                    checkLastMediaChanged()
                }
            }
        }, LAST_MEDIA_CHECK_PERIOD)
    }

    private fun checkRecycleBinItems() {
        if (config.useRecycleBin && config.lastBinCheck < System.currentTimeMillis() - DAY_SECONDS * 1000) {
            config.lastBinCheck = System.currentTimeMillis()
            Handler().postDelayed({
                RunOnBackgroundThreadUseCase {
                    try {
                        val filesToDelete =
                            mediaDB.getOldRecycleBinItems(System.currentTimeMillis() - MONTH_MILLISECONDS)
                        filesToDelete.forEach {
                            if (File(it.path.replaceFirst(RECYCLE_BIN, recycleBinPath)).delete()) {
                                mediaDB.deleteMediumPath(it.path)
                            }
                        }
                    } catch (_: Exception) {
                    }
                }
            }, 3000L)
        }
    }

    // exclude probably unwanted folders, for example facebook stickers are split between hundreds of separate folders like
    // /storage/emulated/0/Android/data/com.facebook.orca/files/stickers/175139712676531/209575122566323
    // /storage/emulated/0/Android/data/com.facebook.orca/files/stickers/497837993632037/499671223448714
    private fun excludeSpamFolders() {
        RunOnBackgroundThreadUseCase {

            try {
                val internalPath = internalStoragePath
                val checkedPaths = ArrayList<String>()
                val oftenRepeatedPaths = ArrayList<String>()
                val paths = mDirs.map { it.path.removePrefix(internalPath) }
                    .toMutableList() as ArrayList<String>
                paths.forEach {
                    val parts = it.split("/")
                    var currentString = ""
                    for (i in 0 until parts.size) {
                        currentString += "${parts[i]}/"

                        if (!checkedPaths.contains(currentString)) {
                            val cnt = paths.count { it.startsWith(currentString) }
                            if (cnt > 50 && currentString.startsWith("/Android/data", true)) {
                                oftenRepeatedPaths.add(currentString)
                            }
                        }

                        checkedPaths.add(currentString)
                    }
                }

                val substringToRemove = oftenRepeatedPaths.filter {
                    val path = it
                    it == "/" || oftenRepeatedPaths.any { it != path && it.startsWith(path) }
                }

                oftenRepeatedPaths.removeAll(substringToRemove)
                val OTGPath = config.otgPath
                oftenRepeatedPaths.forEach {
                    val file = File("$internalPath/$it")
                    if (getDoesFilePathExist(file.absolutePath, OTGPath)) {
                        config.addExcludedFolder(file.absolutePath)
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    private fun getFoldersWithMedia(path: String): HashSet<String> {
        val folders = HashSet<String>()
        try {
            val files = File(path).listFiles()
            if (files != null) {
                files.sortBy { !it.isDirectory }
                for (file in files) {
                    if (file.isDirectory && !file.startsWith("${config.internalStoragePath}/Android")) {
                        folders.addAll(getFoldersWithMedia(file.absolutePath))
                    } else if (file.isFile && file.absolutePath.isMediaFile()) {
                        folders.add(file.parent ?: "")
                        break
                    }
                }
            }
        } catch (ignored: Exception) {
        }

        return folders
    }


    override fun refreshItems() {
        getDirectories()
    }


    override fun recheckPinnedFolders() {
        RunOnBackgroundThreadUseCase {
            gotDirectories(movePinnedDirectoriesToFront(getCurrentlyDisplayedDirs()))
        }
    }

    override fun updateDirectories(directories: ArrayList<Directory>) {
        RunOnBackgroundThreadUseCase {
            storeDirectoryItems(directories)
            removeInvalidDBDirectories()
        }
    }

    private fun checkWhatsNewDialog() {
        arrayListOf<Release>().apply {
            add(Release(1, R.string.release_001))
            checkWhatsNew(this, BuildConfig.VERSION_CODE)
        }
    }

    private fun storeDirectoryItems(items: ArrayList<Directory>) {
        RunOnBackgroundThreadUseCase {
            GalleryDatabase.getInstance(applicationContext).DirectoryDao().insertAll(items)
        }
    }

    private fun getOTGFolderChildrenNames(path: String) =
        getOTGFolderChildren(this, path)?.map { it.name }?.toMutableList()

    private fun getOTGFolderChildren(owner: Context, path: String): Array<DocumentFile>? =
        owner.getDocumentFile(path)?.listFiles()
}
