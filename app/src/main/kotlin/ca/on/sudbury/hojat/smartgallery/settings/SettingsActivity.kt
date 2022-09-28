package ca.on.sudbury.hojat.smartgallery.settings

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import ca.on.sudbury.hojat.smartgallery.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.simplemobiletools.commons.dialogs.*
import com.simplemobiletools.commons.extensions.*
import com.simplemobiletools.commons.helpers.*
import com.simplemobiletools.commons.models.RadioItem
import ca.on.sudbury.hojat.smartgallery.activities.ExcludedFoldersActivity
import ca.on.sudbury.hojat.smartgallery.activities.HiddenFoldersActivity
import ca.on.sudbury.hojat.smartgallery.activities.IncludedFoldersActivity
import ca.on.sudbury.hojat.smartgallery.base.SimpleActivity
import ca.on.sudbury.hojat.smartgallery.databinding.ActivitySettingsBinding
import ca.on.sudbury.hojat.smartgallery.dialogs.ChangeFileThumbnailStyleDialog
import ca.on.sudbury.hojat.smartgallery.dialogs.ChangeFolderThumbnailStyleDialog
import ca.on.sudbury.hojat.smartgallery.dialogs.ManageBottomActionsDialog
import ca.on.sudbury.hojat.smartgallery.dialogs.ManageExtendedDetailsDialog
import ca.on.sudbury.hojat.smartgallery.extensions.*
import ca.on.sudbury.hojat.smartgallery.helpers.*
import ca.on.sudbury.hojat.smartgallery.models.AlbumCover
import java.io.File
import java.io.InputStream
import java.util.*
import kotlin.system.exitProcess

private const val PICK_IMPORT_SOURCE_INTENT = 1

class SettingsActivity : SimpleActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private var mRecycleBinContentSize = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()
        setupToolbar(binding.settingsToolbar, NavigationIcon.Arrow)
        setupSettingItems()
    }

    private fun setupSettingItems() {
        setupCustomizeColors()
        setupUseEnglish()
        setupChangeDateTimeFormat()
        setupFileLoadingPriority()
        setupManageIncludedFolders()
        setupManageExcludedFolders()
        setupManageHiddenFolders()
        setupShowHiddenItems()
        setupAutoplayVideos()
        setupRememberLastVideo()
        setupLoopVideos()
        setupOpenVideosOnSeparateScreen()
        setupMaxBrightness()
        setupCropThumbnails()
        setupDarkBackground()
        setupScrollHorizontally()
        setupScreenRotation()
        setupHideSystemUI()
        setupHiddenItemPasswordProtection()
        setupExcludedItemPasswordProtection()
        setupAppPasswordProtection()
        setupFileDeletionPasswordProtection()
        setupDeleteEmptyFolders()
        setupAllowPhotoGestures()
        setupAllowVideoGestures()
        setupAllowDownGesture()
        setupAllowRotatingWithGestures()
        setupShowNotch()
        setupBottomActions()
        setupFileThumbnailStyle()
        setupFolderThumbnailStyle()
        setupKeepLastModified()
        setupEnablePullToRefresh()
        setupAllowZoomingImages()
        setupShowHighestQuality()
        setupAllowOneToOneZoom()
        setupAllowInstantChange()
        setupShowExtendedDetails()
        setupHideExtendedDetails()
        setupManageExtendedDetails()
        setupSkipDeleteConfirmation()
        setupManageBottomActions()
        setupUseRecycleBin()
        setupShowRecycleBin()
        setupShowRecycleBinLast()
        setupEmptyRecycleBin()
        updateTextColors(binding.settingsHolder)
        setupClearCache()
        setupExportSettings()
        setupImportSettings()

        arrayOf(
            binding.settingsColorCustomizationLabel,
            binding.settingsGeneralSettingsLabel,
            binding.settingsVideosLabel,
            binding.settingsThumbnailsLabel,
            binding.settingsScrollingLabel,
            binding.settingsFullscreenMediaLabel,
            binding.settingsDeepZoomableImagesLabel,
            binding.settingsExtendedDetailsLabel,
            binding.settingsSecurityLabel,
            binding.settingsFileOperationsLabel,
            binding.settingsBottomActionsLabel,
            binding.settingsRecycleBinLabel,
            binding.settingsMigratingLabel
        ).forEach {
            it.setTextColor(getProperPrimaryColor())
        }

        arrayOf(
            binding.settingsColorCustomizationHolder,
            binding.settingsGeneralSettingsHolder,
            binding.settingsVideosHolder,
            binding.settingsThumbnailsHolder,
            binding.settingsScrollingHolder,
            binding.settingsFullscreenMediaHolder,
            binding.settingsDeepZoomableImagesHolder,
            binding.settingsExtendedDetailsHolder,
            binding.settingsSecurityHolder,
            binding.settingsFileOperationsHolder,
            binding.settingsBottomActionsHolder,
            binding.settingsRecycleBinHolder,
            binding.settingsMigratingHolder
        ).forEach {
            it.background.applyColorFilter(getProperBackgroundColor().getContrastColor())
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == PICK_IMPORT_SOURCE_INTENT && resultCode == Activity.RESULT_OK && resultData != null && resultData.data != null) {
            val inputStream = contentResolver.openInputStream(resultData.data!!)
            parseFile(inputStream)
        }
    }

    private fun setupCustomizeColors() {
        binding.settingsCustomizeColorsHolder.setOnClickListener {
            startCustomizationActivity()
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setupUseEnglish() {
        binding.settingsUseEnglishHolder.beVisibleIf(config.wasUseEnglishToggled || Locale.getDefault().language != "en")
        binding.settingsUseEnglish.isChecked = config.useEnglish

        if (binding.settingsUseEnglishHolder.isGone()) {
            binding.settingsChangeDateTimeFormatHolder.background = resources.getDrawable(R.drawable.ripple_top_corners, theme)
        }

        binding.settingsUseEnglishHolder.setOnClickListener {
            binding.settingsUseEnglish.toggle()
            config.useEnglish = binding.settingsUseEnglish.isChecked
            exitProcess(0)
        }
    }

    private fun setupChangeDateTimeFormat() {
        binding.settingsChangeDateTimeFormatHolder.setOnClickListener {
            ChangeDateTimeFormatDialog(this) {}
        }
    }

    private fun setupFileLoadingPriority() {
        binding.settingsFileLoadingPriorityHolder.beGoneIf(isRPlus() && !isExternalStorageManager())
        binding.settingsFileLoadingPriority.text = getFileLoadingPriorityText()
        binding.settingsFileLoadingPriorityHolder.setOnClickListener {
            val items = arrayListOf(
                RadioItem(PRIORITY_SPEED, getString(R.string.speed)),
                RadioItem(PRIORITY_COMPROMISE, getString(R.string.compromise)),
                RadioItem(PRIORITY_VALIDITY, getString(R.string.avoid_showing_invalid_files))
            )

            RadioGroupDialog(this@SettingsActivity, items, config.fileLoadingPriority) {
                config.fileLoadingPriority = it as Int
                binding.settingsFileLoadingPriority.text = getFileLoadingPriorityText()
            }
        }
    }

    private fun getFileLoadingPriorityText() = getString(
        when (config.fileLoadingPriority) {
            PRIORITY_SPEED -> R.string.speed
            PRIORITY_COMPROMISE -> R.string.compromise
            else -> R.string.avoid_showing_invalid_files
        }
    )

    private fun setupManageIncludedFolders() {
        binding.settingsManageIncludedFoldersHolder.beGoneIf(isRPlus() && !isExternalStorageManager())
        binding.settingsManageIncludedFoldersHolder.setOnClickListener {
            startActivity(Intent(this, IncludedFoldersActivity::class.java))
        }
    }

    private fun setupManageExcludedFolders() {
        binding.settingsManageExcludedFoldersHolder.setOnClickListener {
            handleExcludedFolderPasswordProtection {
                startActivity(Intent(this, ExcludedFoldersActivity::class.java))
            }
        }
    }

    private fun setupManageHiddenFolders() {
        binding.settingsManageHiddenFoldersHolder.beGoneIf(isQPlus())
        binding.settingsManageHiddenFoldersHolder.setOnClickListener {
            handleHiddenFolderPasswordProtection {
                startActivity(Intent(this, HiddenFoldersActivity::class.java))
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setupShowHiddenItems() {
        if (isRPlus() && !isExternalStorageManager()) {
            binding.settingsShowHiddenItemsHolder.beGone()
            binding.settingsManageExcludedFoldersHolder.background = resources.getDrawable(R.drawable.ripple_bottom_corners, theme)
        }

        binding.settingsShowHiddenItems.isChecked = config.showHiddenMedia
        binding.settingsShowHiddenItemsHolder.setOnClickListener {
            if (config.showHiddenMedia) {
                toggleHiddenItems()
            } else {
                handleHiddenFolderPasswordProtection {
                    toggleHiddenItems()
                }
            }
        }
    }

    private fun toggleHiddenItems() {
        binding.settingsShowHiddenItems.toggle()
        config.showHiddenMedia = binding.settingsShowHiddenItems.isChecked
    }

    private fun setupAutoplayVideos() {
        binding.settingsAutoplayVideos.isChecked = config.autoplayVideos
        binding.settingsAutoplayVideosHolder.setOnClickListener {
            binding.settingsAutoplayVideos.toggle()
            config.autoplayVideos = binding.settingsAutoplayVideos.isChecked
        }
    }

    private fun setupRememberLastVideo() {
        binding.settingsRememberLastVideoPosition.isChecked = config.rememberLastVideoPosition
        binding.settingsRememberLastVideoPositionHolder.setOnClickListener {
            binding.settingsRememberLastVideoPosition.toggle()
            config.rememberLastVideoPosition = binding.settingsRememberLastVideoPosition.isChecked
        }
    }

    private fun setupLoopVideos() {
        binding.settingsLoopVideos.isChecked = config.loopVideos
        binding.settingsLoopVideosHolder.setOnClickListener {
            binding.settingsLoopVideos.toggle()
            config.loopVideos =
                binding.settingsLoopVideos.isChecked
        }
    }

    private fun setupOpenVideosOnSeparateScreen() {
        binding.settingsOpenVideosOnSeparateScreen.isChecked = config.openVideosOnSeparateScreen
        binding.settingsOpenVideosOnSeparateScreenHolder.setOnClickListener {
            binding.settingsOpenVideosOnSeparateScreen.toggle()
            config.openVideosOnSeparateScreen =
                binding.settingsOpenVideosOnSeparateScreen.isChecked
        }
    }

    private fun setupMaxBrightness() {
        binding.settingsMaxBrightness.isChecked = config.maxBrightness
        binding.settingsMaxBrightnessHolder.setOnClickListener {
            binding.settingsMaxBrightness.toggle()
            config.maxBrightness = binding.settingsMaxBrightness.isChecked
        }
    }

    private fun setupCropThumbnails() {
        binding.settingsCropThumbnails.isChecked = config.cropThumbnails
        binding.settingsCropThumbnailsHolder.setOnClickListener {
            binding.settingsCropThumbnails.toggle()
            config.cropThumbnails = binding.settingsCropThumbnails.isChecked
        }
    }

    private fun setupDarkBackground() {
        binding.settingsBlackBackground.isChecked = config.blackBackground
        binding.settingsBlackBackgroundHolder.setOnClickListener {
            binding.settingsBlackBackground.toggle()
            config.blackBackground = binding.settingsBlackBackground.isChecked
        }
    }

    private fun setupScrollHorizontally() {
        binding.settingsScrollHorizontally.isChecked = config.scrollHorizontally
        binding.settingsScrollHorizontallyHolder.setOnClickListener {
            binding.settingsScrollHorizontally.toggle()
            config.scrollHorizontally = binding.settingsScrollHorizontally.isChecked

            if (config.scrollHorizontally) {
                config.enablePullToRefresh = false
                binding.settingsEnablePullToRefresh.isChecked = false
            }
        }
    }

    private fun setupHideSystemUI() {
        binding.settingsHideSystemUi.isChecked = config.hideSystemUI
        binding.settingsHideSystemUiHolder.setOnClickListener {
            binding.settingsHideSystemUi.toggle()
            config.hideSystemUI = binding.settingsHideSystemUi.isChecked
        }
    }

    private fun setupHiddenItemPasswordProtection() {
        binding.settingsHiddenItemPasswordProtectionHolder.beGoneIf(isRPlus() && !isExternalStorageManager())
        binding.settingsHiddenItemPasswordProtection.isChecked = config.isHiddenPasswordProtectionOn
        binding.settingsHiddenItemPasswordProtectionHolder.setOnClickListener {
            val tabToShow = if (config.isHiddenPasswordProtectionOn) config.hiddenProtectionType else SHOW_ALL_TABS
            SecurityDialog(this, config.hiddenPasswordHash, tabToShow) { hash, type, success ->
                if (success) {
                    val hasPasswordProtection = config.isHiddenPasswordProtectionOn
                    binding.settingsHiddenItemPasswordProtection.isChecked = !hasPasswordProtection
                    config.isHiddenPasswordProtectionOn = !hasPasswordProtection
                    config.hiddenPasswordHash = if (hasPasswordProtection) "" else hash
                    config.hiddenProtectionType = type

                    if (config.isHiddenPasswordProtectionOn) {
                        val confirmationTextId = if (config.hiddenProtectionType == PROTECTION_FINGERPRINT)
                            R.string.fingerprint_setup_successfully else R.string.protection_setup_successfully
                        ConfirmationDialog(this, "", confirmationTextId, R.string.ok, 0) { }
                    }
                }
            }
        }
    }

    private fun setupExcludedItemPasswordProtection() {
        binding.settingsExcludedItemPasswordProtectionHolder.beGoneIf(binding.settingsHiddenItemPasswordProtectionHolder.isVisible())
        binding.settingsExcludedItemPasswordProtection.isChecked = config.isExcludedPasswordProtectionOn
        binding.settingsExcludedItemPasswordProtectionHolder.setOnClickListener {
            val tabToShow = if (config.isExcludedPasswordProtectionOn) config.excludedProtectionType else SHOW_ALL_TABS
            SecurityDialog(this, config.excludedPasswordHash, tabToShow) { hash, type, success ->
                if (success) {
                    val hasPasswordProtection = config.isExcludedPasswordProtectionOn
                    binding.settingsExcludedItemPasswordProtection.isChecked = !hasPasswordProtection
                    config.isExcludedPasswordProtectionOn = !hasPasswordProtection
                    config.excludedPasswordHash = if (hasPasswordProtection) "" else hash
                    config.excludedProtectionType = type

                    if (config.isExcludedPasswordProtectionOn) {
                        val confirmationTextId = if (config.excludedProtectionType == PROTECTION_FINGERPRINT)
                            R.string.fingerprint_setup_successfully else R.string.protection_setup_successfully
                        ConfirmationDialog(this, "", confirmationTextId, R.string.ok, 0) { }
                    }
                }
            }
        }
    }

    private fun setupAppPasswordProtection() {
        binding.settingsAppPasswordProtection.isChecked = config.isAppPasswordProtectionOn
        binding.settingsAppPasswordProtectionHolder.setOnClickListener {
            val tabToShow = if (config.isAppPasswordProtectionOn) config.appProtectionType else SHOW_ALL_TABS
            SecurityDialog(this, config.appPasswordHash, tabToShow) { hash, type, success ->
                if (success) {
                    val hasPasswordProtection = config.isAppPasswordProtectionOn
                    binding.settingsAppPasswordProtection.isChecked = !hasPasswordProtection
                    config.isAppPasswordProtectionOn = !hasPasswordProtection
                    config.appPasswordHash = if (hasPasswordProtection) "" else hash
                    config.appProtectionType = type

                    if (config.isAppPasswordProtectionOn) {
                        val confirmationTextId = if (config.appProtectionType == PROTECTION_FINGERPRINT)
                            R.string.fingerprint_setup_successfully else R.string.protection_setup_successfully
                        ConfirmationDialog(this, "", confirmationTextId, R.string.ok, 0) { }
                    }
                }
            }
        }
    }

    private fun setupFileDeletionPasswordProtection() {
        binding.settingsFileDeletionPasswordProtection.isChecked = config.isDeletePasswordProtectionOn
        binding.settingsFileDeletionPasswordProtectionHolder.setOnClickListener {
            val tabToShow = if (config.isDeletePasswordProtectionOn) config.deleteProtectionType else SHOW_ALL_TABS
            SecurityDialog(this, config.deletePasswordHash, tabToShow) { hash, type, success ->
                if (success) {
                    val hasPasswordProtection = config.isDeletePasswordProtectionOn
                    binding.settingsFileDeletionPasswordProtection.isChecked = !hasPasswordProtection
                    config.isDeletePasswordProtectionOn = !hasPasswordProtection
                    config.deletePasswordHash = if (hasPasswordProtection) "" else hash
                    config.deleteProtectionType = type

                    if (config.isDeletePasswordProtectionOn) {
                        val confirmationTextId = if (config.deleteProtectionType == PROTECTION_FINGERPRINT)
                            R.string.fingerprint_setup_successfully else R.string.protection_setup_successfully
                        ConfirmationDialog(this, "", confirmationTextId, R.string.ok, 0) { }
                    }
                }
            }
        }
    }

    private fun setupDeleteEmptyFolders() {
        binding.settingsDeleteEmptyFolders.isChecked = config.deleteEmptyFolders
        binding.settingsDeleteEmptyFoldersHolder.setOnClickListener {
            binding.settingsDeleteEmptyFolders.toggle()
            config.deleteEmptyFolders = binding.settingsDeleteEmptyFolders.isChecked
        }
    }

    private fun setupAllowPhotoGestures() {
        binding.settingsAllowPhotoGestures.isChecked = config.allowPhotoGestures
        binding.settingsAllowPhotoGesturesHolder.setOnClickListener {
            binding.settingsAllowPhotoGestures.toggle()
            config.allowPhotoGestures = binding.settingsAllowPhotoGestures.isChecked
        }
    }

    private fun setupAllowVideoGestures() {
        binding.settingsAllowVideoGestures.isChecked = config.allowVideoGestures
        binding.settingsAllowVideoGesturesHolder.setOnClickListener {
            binding.settingsAllowVideoGestures.toggle()
            config.allowVideoGestures = binding.settingsAllowVideoGestures.isChecked
        }
    }

    private fun setupAllowDownGesture() {
        binding.settingsAllowDownGesture.isChecked = config.allowDownGesture
        binding.settingsAllowDownGestureHolder.setOnClickListener {
            binding.settingsAllowDownGesture.toggle()
            config.allowDownGesture = binding.settingsAllowDownGesture.isChecked
        }
    }

    private fun setupAllowRotatingWithGestures() {
        binding.settingsAllowRotatingWithGestures.isChecked = config.allowRotatingWithGestures
        binding.settingsAllowRotatingWithGesturesHolder.setOnClickListener {
            binding.settingsAllowRotatingWithGestures.toggle()
            config.allowRotatingWithGestures = binding.settingsAllowRotatingWithGestures.isChecked
        }
    }

    private fun setupShowNotch() {
        binding.settingsShowNotchHolder.beVisibleIf(isPiePlus())
        binding.settingsShowNotch.isChecked = config.showNotch
        binding.settingsShowNotchHolder.setOnClickListener {
            binding.settingsShowNotch.toggle()
            config.showNotch = binding.settingsShowNotch.isChecked
        }
    }

    private fun setupFileThumbnailStyle() {
        binding.settingsFileThumbnailStyleHolder.setOnClickListener {
            ChangeFileThumbnailStyleDialog(this)
        }
    }

    private fun setupFolderThumbnailStyle() {
        with(binding) {
            settingsFolderThumbnailStyle.text = getFolderStyleText()
            settingsFolderThumbnailStyleHolder.setOnClickListener {
                ChangeFolderThumbnailStyleDialog(this@SettingsActivity) {
                    settingsFolderThumbnailStyle.text = getFolderStyleText()
                }
            }
        }
    }

    private fun getFolderStyleText() = getString(
        when (config.folderStyle) {
            FOLDER_STYLE_SQUARE -> R.string.square
            else -> R.string.rounded_corners
        }
    )

    private fun setupKeepLastModified() {
        with(binding) {
            settingsKeepLastModified.isChecked = config.keepLastModified
            settingsKeepLastModifiedHolder.setOnClickListener {
                handleMediaManagementPrompt {
                    settingsKeepLastModified.toggle()
                    config.keepLastModified = settingsKeepLastModified.isChecked
                }
            }
        }
    }

    private fun setupEnablePullToRefresh() {
        with(binding) {
            settingsEnablePullToRefresh.isChecked = config.enablePullToRefresh
            settingsEnablePullToRefreshHolder.setOnClickListener {
                settingsEnablePullToRefresh.toggle()
                config.enablePullToRefresh = settingsEnablePullToRefresh.isChecked
            }
        }
    }

    private fun setupAllowZoomingImages() {
        with(binding) {
            settingsAllowZoomingImages.isChecked = config.allowZoomingImages
            updateDeepZoomToggleButtons()
            settingsAllowZoomingImagesHolder.setOnClickListener {
                settingsAllowZoomingImages.toggle()
                config.allowZoomingImages = settingsAllowZoomingImages.isChecked
                updateDeepZoomToggleButtons()
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun updateDeepZoomToggleButtons() {
        with(binding) {
            settingsAllowRotatingWithGesturesHolder.beVisibleIf(config.allowZoomingImages)
            settingsShowHighestQualityHolder.beVisibleIf(config.allowZoomingImages)
            settingsAllowOneToOneZoomHolder.beVisibleIf(config.allowZoomingImages)

            if (config.allowZoomingImages) {
                settingsAllowZoomingImagesHolder.background = resources.getDrawable(R.drawable.ripple_top_corners, theme)
            } else {
                settingsAllowZoomingImagesHolder.background = resources.getDrawable(R.drawable.ripple_all_corners, theme)
            }
        }
    }

    private fun setupShowHighestQuality() {
        with(binding) {
            settingsShowHighestQuality.isChecked = config.showHighestQuality
            settingsShowHighestQualityHolder.setOnClickListener {
                settingsShowHighestQuality.toggle()
                config.showHighestQuality = settingsShowHighestQuality.isChecked
            }
        }
    }

    private fun setupAllowOneToOneZoom() {
        with(binding) {
            settingsAllowOneToOneZoom.isChecked = config.allowOneToOneZoom
            settingsAllowOneToOneZoomHolder.setOnClickListener {
                settingsAllowOneToOneZoom.toggle()
                config.allowOneToOneZoom = settingsAllowOneToOneZoom.isChecked
            }
        }
    }

    private fun setupAllowInstantChange() {
        with(binding) {
            settingsAllowInstantChange.isChecked = config.allowInstantChange
            settingsAllowInstantChangeHolder.setOnClickListener {
                settingsAllowInstantChange.toggle()
                config.allowInstantChange = settingsAllowInstantChange.isChecked
            }
        }
    }

    private fun setupShowExtendedDetails() {
        with(binding) {
            settingsShowExtendedDetails.isChecked = config.showExtendedDetails
            updateExtendedDetailsButtons()
            settingsShowExtendedDetailsHolder.setOnClickListener {
                settingsShowExtendedDetails.toggle()
                config.showExtendedDetails = settingsShowExtendedDetails.isChecked
                updateExtendedDetailsButtons()
            }
        }
    }

    private fun setupHideExtendedDetails() {
        with(binding) {
            settingsHideExtendedDetails.isChecked = config.hideExtendedDetails
            settingsHideExtendedDetailsHolder.setOnClickListener {
                settingsHideExtendedDetails.toggle()
                config.hideExtendedDetails = settingsHideExtendedDetails.isChecked
            }
        }
    }

    private fun setupManageExtendedDetails() {
        with(binding) {
            settingsManageExtendedDetailsHolder.setOnClickListener {
                ManageExtendedDetailsDialog(this@SettingsActivity) {
                    if (config.extendedDetails == 0) {
                        settingsShowExtendedDetailsHolder.callOnClick()
                    }
                }
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun updateExtendedDetailsButtons() {
        with(binding) {
            settingsManageExtendedDetailsHolder.beVisibleIf(config.showExtendedDetails)
            settingsHideExtendedDetailsHolder.beVisibleIf(config.showExtendedDetails)
            if (config.showExtendedDetails) {
                settingsShowExtendedDetailsHolder.background = resources.getDrawable(R.drawable.ripple_top_corners, theme)
            } else {
                settingsShowExtendedDetailsHolder.background = resources.getDrawable(R.drawable.ripple_all_corners, theme)
            }
        }
    }

    private fun setupSkipDeleteConfirmation() {
        with(binding) {
            settingsSkipDeleteConfirmation.isChecked = config.skipDeleteConfirmation
            settingsSkipDeleteConfirmationHolder.setOnClickListener {
                settingsSkipDeleteConfirmation.toggle()
                config.skipDeleteConfirmation = settingsSkipDeleteConfirmation.isChecked
            }
        }
    }

    private fun setupScreenRotation() {
        with(binding) {
            settingsScreenRotation.text = getScreenRotationText()
            settingsScreenRotationHolder.setOnClickListener {
                val items = arrayListOf(
                    RadioItem(ROTATE_BY_SYSTEM_SETTING, getString(R.string.screen_rotation_system_setting)),
                    RadioItem(ROTATE_BY_DEVICE_ROTATION, getString(R.string.screen_rotation_device_rotation)),
                    RadioItem(ROTATE_BY_ASPECT_RATIO, getString(R.string.screen_rotation_aspect_ratio))
                )
                RadioGroupDialog(this@SettingsActivity, items, config.screenRotation) {
                    config.screenRotation = it as Int
                    settingsScreenRotation.text = getScreenRotationText()
                }
            }
        }
    }

    private fun getScreenRotationText() = getString(
        when (config.screenRotation) {
            ROTATE_BY_SYSTEM_SETTING -> R.string.screen_rotation_system_setting
            ROTATE_BY_DEVICE_ROTATION -> R.string.screen_rotation_device_rotation
            else -> R.string.screen_rotation_aspect_ratio
        }
    )

    private fun setupBottomActions() {
        with(binding) {
            settingsBottomActionsCheckbox.isChecked = config.bottomActions
            updateManageBottomActionsButtons()
            settingsBottomActionsCheckboxHolder.setOnClickListener {
                settingsBottomActionsCheckbox.toggle()
                config.bottomActions = settingsBottomActionsCheckbox.isChecked
                updateManageBottomActionsButtons()
            }
        }
    }

    private fun setupManageBottomActions() {
        with(binding) {
            settingsManageBottomActionsHolder.setOnClickListener {
                ManageBottomActionsDialog(this@SettingsActivity) {
                    if (config.visibleBottomActions == 0) {
                        settingsBottomActionsCheckboxHolder.callOnClick()
                        config.bottomActions = false
                        config.visibleBottomActions = DEFAULT_BOTTOM_ACTIONS
                    }
                }
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun updateManageBottomActionsButtons() {
        with(binding) {
            settingsManageBottomActionsHolder.beVisibleIf(config.bottomActions)
            if (config.bottomActions) {
                settingsBottomActionsCheckboxHolder.background = resources.getDrawable(R.drawable.ripple_top_corners, theme)
            } else {
                settingsBottomActionsCheckboxHolder.background = resources.getDrawable(R.drawable.ripple_all_corners, theme)
            }
        }
    }

    private fun setupUseRecycleBin() {
        updateRecycleBinButtons()
        with(binding) {
            settingsUseRecycleBin.isChecked = config.useRecycleBin
            settingsUseRecycleBinHolder.setOnClickListener {
                settingsUseRecycleBin.toggle()
                config.useRecycleBin = settingsUseRecycleBin.isChecked
                updateRecycleBinButtons()
            }
        }
    }

    private fun setupShowRecycleBin() {
        with(binding) {
            settingsShowRecycleBin.isChecked = config.showRecycleBinAtFolders
            settingsShowRecycleBinHolder.setOnClickListener {
                settingsShowRecycleBin.toggle()
                config.showRecycleBinAtFolders = settingsShowRecycleBin.isChecked
                updateRecycleBinButtons()
            }
        }
    }

    private fun setupShowRecycleBinLast() {
        with(binding) {
            settingsShowRecycleBinLast.isChecked = config.showRecycleBinLast
            settingsShowRecycleBinLastHolder.setOnClickListener {
                settingsShowRecycleBinLast.toggle()
                config.showRecycleBinLast = settingsShowRecycleBinLast.isChecked
                if (config.showRecycleBinLast) {
                    config.removePinnedFolders(setOf(RECYCLE_BIN))
                }
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun updateRecycleBinButtons() {
        with(binding) {
            settingsShowRecycleBinLastHolder.beVisibleIf(config.useRecycleBin && config.showRecycleBinAtFolders)
            settingsEmptyRecycleBinHolder.beVisibleIf(config.useRecycleBin)
            settingsShowRecycleBinHolder.beVisibleIf(config.useRecycleBin)
            if (config.useRecycleBin) {
                settingsUseRecycleBinHolder.background = resources.getDrawable(R.drawable.ripple_top_corners, theme)
            } else {
                settingsUseRecycleBinHolder.background = resources.getDrawable(R.drawable.ripple_all_corners, theme)
            }
        }
    }

    private fun setupEmptyRecycleBin() {
        ensureBackgroundThread {
            try {
                mRecycleBinContentSize = mediaDB.getDeletedMedia().sumByLong { medium ->
                    val size = medium.size
                    if (size == 0L) {
                        val path = medium.path.removePrefix(RECYCLE_BIN).prependIndent(recycleBinPath)
                        File(path).length()
                    } else {
                        size
                    }
                }
            } catch (ignored: Exception) {
            }

            runOnUiThread {
                binding.settingsEmptyRecycleBinSize.text = mRecycleBinContentSize.formatSize()
            }
        }

        binding.settingsEmptyRecycleBinHolder.setOnClickListener {
            if (mRecycleBinContentSize == 0L) {
                toast(R.string.recycle_bin_empty)
            } else {
                showRecycleBinEmptyingDialog {
                    emptyTheRecycleBin()
                    mRecycleBinContentSize = 0L
                    binding.settingsEmptyRecycleBinSize.text = 0L.formatSize()
                }
            }
        }
    }

    private fun setupClearCache() {
        ensureBackgroundThread {
            val size = cacheDir.getProperSize(true).formatSize()
            runOnUiThread {
                binding.settingsClearCacheSize.text = size
            }
        }

        binding.settingsClearCacheHolder.setOnClickListener {
            ensureBackgroundThread {
                cacheDir.deleteRecursively()
                runOnUiThread {
                    binding.settingsClearCacheSize.text = cacheDir.getProperSize(true).formatSize()
                }
            }
        }
    }

    private fun setupExportSettings() {
        binding.settingsExportHolder.setOnClickListener {
            val configItems = LinkedHashMap<String, Any>().apply {
                put(IS_USING_SHARED_THEME, config.isUsingSharedTheme)
                put(TEXT_COLOR, config.textColor)
                put(BACKGROUND_COLOR, config.backgroundColor)
                put(PRIMARY_COLOR, config.primaryColor)
                put(ACCENT_COLOR, config.accentColor)
                put(APP_ICON_COLOR, config.appIconColor)
                put(USE_ENGLISH, config.useEnglish)
                put(WAS_USE_ENGLISH_TOGGLED, config.wasUseEnglishToggled)
                put(WIDGET_BG_COLOR, config.widgetBgColor)
                put(WIDGET_TEXT_COLOR, config.widgetTextColor)
                put(DATE_FORMAT, config.dateFormat)
                put(USE_24_HOUR_FORMAT, config.use24HourFormat)
                put(INCLUDED_FOLDERS, TextUtils.join(",", config.includedFolders))
                put(EXCLUDED_FOLDERS, TextUtils.join(",", config.excludedFolders))
                put(SHOW_HIDDEN_MEDIA, config.showHiddenMedia)
                put(FILE_LOADING_PRIORITY, config.fileLoadingPriority)
                put(AUTOPLAY_VIDEOS, config.autoplayVideos)
                put(REMEMBER_LAST_VIDEO_POSITION, config.rememberLastVideoPosition)
                put(LOOP_VIDEOS, config.loopVideos)
                put(OPEN_VIDEOS_ON_SEPARATE_SCREEN, config.openVideosOnSeparateScreen)
                put(ALLOW_VIDEO_GESTURES, config.allowVideoGestures)
                put(ANIMATE_GIFS, config.animateGifs)
                put(CROP_THUMBNAILS, config.cropThumbnails)
                put(SHOW_THUMBNAIL_VIDEO_DURATION, config.showThumbnailVideoDuration)
                put(SHOW_THUMBNAIL_FILE_TYPES, config.showThumbnailFileTypes)
                put(MARK_FAVORITE_ITEMS, config.markFavoriteItems)
                put(SCROLL_HORIZONTALLY, config.scrollHorizontally)
                put(ENABLE_PULL_TO_REFRESH, config.enablePullToRefresh)
                put(MAX_BRIGHTNESS, config.maxBrightness)
                put(BLACK_BACKGROUND, config.blackBackground)
                put(HIDE_SYSTEM_UI, config.hideSystemUI)
                put(ALLOW_INSTANT_CHANGE, config.allowInstantChange)
                put(ALLOW_PHOTO_GESTURES, config.allowPhotoGestures)
                put(ALLOW_DOWN_GESTURE, config.allowDownGesture)
                put(ALLOW_ROTATING_WITH_GESTURES, config.allowRotatingWithGestures)
                put(SHOW_NOTCH, config.showNotch)
                put(SCREEN_ROTATION, config.screenRotation)
                put(ALLOW_ZOOMING_IMAGES, config.allowZoomingImages)
                put(SHOW_HIGHEST_QUALITY, config.showHighestQuality)
                put(ALLOW_ONE_TO_ONE_ZOOM, config.allowOneToOneZoom)
                put(SHOW_EXTENDED_DETAILS, config.showExtendedDetails)
                put(HIDE_EXTENDED_DETAILS, config.hideExtendedDetails)
                put(EXTENDED_DETAILS, config.extendedDetails)
                put(DELETE_EMPTY_FOLDERS, config.deleteEmptyFolders)
                put(KEEP_LAST_MODIFIED, config.keepLastModified)
                put(SKIP_DELETE_CONFIRMATION, config.skipDeleteConfirmation)
                put(BOTTOM_ACTIONS, config.bottomActions)
                put(VISIBLE_BOTTOM_ACTIONS, config.visibleBottomActions)
                put(USE_RECYCLE_BIN, config.useRecycleBin)
                put(SHOW_RECYCLE_BIN_AT_FOLDERS, config.showRecycleBinAtFolders)
                put(SHOW_RECYCLE_BIN_LAST, config.showRecycleBinLast)
                put(SORT_ORDER, config.sorting)
                put(DIRECTORY_SORT_ORDER, config.directorySorting)
                put(GROUP_BY, config.groupBy)
                put(GROUP_DIRECT_SUBFOLDERS, config.groupDirectSubfolders)
                put(PINNED_FOLDERS, TextUtils.join(",", config.pinnedFolders))
                put(DISPLAY_FILE_NAMES, config.displayFileNames)
                put(FILTER_MEDIA, config.filterMedia)
                put(DIR_COLUMN_CNT, config.dirColumnCnt)
                put(MEDIA_COLUMN_CNT, config.mediaColumnCnt)
                put(SHOW_ALL, config.showAll)
                put(SHOW_WIDGET_FOLDER_NAME, config.showWidgetFolderName)
                put(VIEW_TYPE_FILES, config.viewTypeFiles)
                put(VIEW_TYPE_FOLDERS, config.viewTypeFolders)
                put(SLIDESHOW_INTERVAL, config.slideshowInterval)
                put(SLIDESHOW_INCLUDE_VIDEOS, config.slideshowIncludeVideos)
                put(SLIDESHOW_INCLUDE_GIFS, config.slideshowIncludeGIFs)
                put(SLIDESHOW_RANDOM_ORDER, config.slideshowRandomOrder)
                put(SLIDESHOW_MOVE_BACKWARDS, config.slideshowMoveBackwards)
                put(SLIDESHOW_LOOP, config.loopSlideshow)
                put(LAST_EDITOR_CROP_ASPECT_RATIO, config.lastEditorCropAspectRatio)
                put(LAST_EDITOR_CROP_OTHER_ASPECT_RATIO_X, config.lastEditorCropOtherAspectRatioX)
                put(LAST_EDITOR_CROP_OTHER_ASPECT_RATIO_Y, config.lastEditorCropOtherAspectRatioY)
                put(LAST_CONFLICT_RESOLUTION, config.lastConflictResolution)
                put(LAST_CONFLICT_APPLY_TO_ALL, config.lastConflictApplyToAll)
                put(EDITOR_BRUSH_COLOR, config.editorBrushColor)
                put(EDITOR_BRUSH_HARDNESS, config.editorBrushHardness)
                put(EDITOR_BRUSH_SIZE, config.editorBrushSize)
                put(ALBUM_COVERS, config.albumCovers)
                put(FOLDER_THUMBNAIL_STYLE, config.folderStyle)
                put(FOLDER_MEDIA_COUNT, config.showFolderMediaCount)
                put(LIMIT_FOLDER_TITLE, config.limitFolderTitle)
                put(THUMBNAIL_SPACING, config.thumbnailSpacing)
                put(FILE_ROUNDED_CORNERS, config.fileRoundedCorners)
            }

            exportSettings(configItems)
        }
    }

    private fun setupImportSettings() {
        binding.settingsImportHolder.setOnClickListener {
            if (isQPlus()) {
                Intent(Intent.ACTION_GET_CONTENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "text/plain"
                    startActivityForResult(this, PICK_IMPORT_SOURCE_INTENT)
                }
            } else {
                handlePermission(PERMISSION_READ_STORAGE) {
                    if (it) {
                        FilePickerDialog(this) {
                            ensureBackgroundThread {
                                parseFile(File(it).inputStream())
                            }
                        }
                    }
                }
            }
        }
    }

    private fun parseFile(inputStream: InputStream?) {
        if (inputStream == null) {
            toast(R.string.unknown_error_occurred)
            return
        }

        var importedItems = 0
        val configValues = LinkedHashMap<String, Any>()
        inputStream.bufferedReader().use {
            while (true) {
                try {
                    val line = it.readLine() ?: break
                    val split = line.split("=".toRegex(), 2)
                    if (split.size == 2) {
                        configValues[split[0]] = split[1]
                    }
                    importedItems++
                } catch (e: Exception) {
                    showErrorToast(e)
                }
            }
        }

        for ((key, value) in configValues) {
            when (key) {
                IS_USING_SHARED_THEME -> config.isUsingSharedTheme = value.toBoolean()
                TEXT_COLOR -> config.textColor = value.toInt()
                BACKGROUND_COLOR -> config.backgroundColor = value.toInt()
                PRIMARY_COLOR -> config.primaryColor = value.toInt()
                ACCENT_COLOR -> config.accentColor = value.toInt()
                APP_ICON_COLOR -> {
                    if (getAppIconColors().contains(value.toInt())) {
                        config.appIconColor = value.toInt()
                        checkAppIconColor()
                    }
                }
                USE_ENGLISH -> config.useEnglish = value.toBoolean()
                WAS_USE_ENGLISH_TOGGLED -> config.wasUseEnglishToggled = value.toBoolean()
                WIDGET_BG_COLOR -> config.widgetBgColor = value.toInt()
                WIDGET_TEXT_COLOR -> config.widgetTextColor = value.toInt()
                DATE_FORMAT -> config.dateFormat = value.toString()
                USE_24_HOUR_FORMAT -> config.use24HourFormat = value.toBoolean()
                INCLUDED_FOLDERS -> config.addIncludedFolders(value.toStringSet())
                EXCLUDED_FOLDERS -> config.addExcludedFolders(value.toStringSet())
                SHOW_HIDDEN_MEDIA -> config.showHiddenMedia = value.toBoolean()
                FILE_LOADING_PRIORITY -> config.fileLoadingPriority = value.toInt()
                AUTOPLAY_VIDEOS -> config.autoplayVideos = value.toBoolean()
                REMEMBER_LAST_VIDEO_POSITION -> config.rememberLastVideoPosition = value.toBoolean()
                LOOP_VIDEOS -> config.loopVideos = value.toBoolean()
                OPEN_VIDEOS_ON_SEPARATE_SCREEN -> config.openVideosOnSeparateScreen = value.toBoolean()
                ALLOW_VIDEO_GESTURES -> config.allowVideoGestures = value.toBoolean()
                ANIMATE_GIFS -> config.animateGifs = value.toBoolean()
                CROP_THUMBNAILS -> config.cropThumbnails = value.toBoolean()
                SHOW_THUMBNAIL_VIDEO_DURATION -> config.showThumbnailVideoDuration = value.toBoolean()
                SHOW_THUMBNAIL_FILE_TYPES -> config.showThumbnailFileTypes = value.toBoolean()
                MARK_FAVORITE_ITEMS -> config.markFavoriteItems = value.toBoolean()
                SCROLL_HORIZONTALLY -> config.scrollHorizontally = value.toBoolean()
                ENABLE_PULL_TO_REFRESH -> config.enablePullToRefresh = value.toBoolean()
                MAX_BRIGHTNESS -> config.maxBrightness = value.toBoolean()
                BLACK_BACKGROUND -> config.blackBackground = value.toBoolean()
                HIDE_SYSTEM_UI -> config.hideSystemUI = value.toBoolean()
                ALLOW_INSTANT_CHANGE -> config.allowInstantChange = value.toBoolean()
                ALLOW_PHOTO_GESTURES -> config.allowPhotoGestures = value.toBoolean()
                ALLOW_DOWN_GESTURE -> config.allowDownGesture = value.toBoolean()
                ALLOW_ROTATING_WITH_GESTURES -> config.allowRotatingWithGestures = value.toBoolean()
                SHOW_NOTCH -> config.showNotch = value.toBoolean()
                SCREEN_ROTATION -> config.screenRotation = value.toInt()
                ALLOW_ZOOMING_IMAGES -> config.allowZoomingImages = value.toBoolean()
                SHOW_HIGHEST_QUALITY -> config.showHighestQuality = value.toBoolean()
                ALLOW_ONE_TO_ONE_ZOOM -> config.allowOneToOneZoom = value.toBoolean()
                SHOW_EXTENDED_DETAILS -> config.showExtendedDetails = value.toBoolean()
                HIDE_EXTENDED_DETAILS -> config.hideExtendedDetails = value.toBoolean()
                EXTENDED_DETAILS -> config.extendedDetails = value.toInt()
                DELETE_EMPTY_FOLDERS -> config.deleteEmptyFolders = value.toBoolean()
                KEEP_LAST_MODIFIED -> config.keepLastModified = value.toBoolean()
                SKIP_DELETE_CONFIRMATION -> config.skipDeleteConfirmation = value.toBoolean()
                BOTTOM_ACTIONS -> config.bottomActions = value.toBoolean()
                VISIBLE_BOTTOM_ACTIONS -> config.visibleBottomActions = value.toInt()
                USE_RECYCLE_BIN -> config.useRecycleBin = value.toBoolean()
                SHOW_RECYCLE_BIN_AT_FOLDERS -> config.showRecycleBinAtFolders = value.toBoolean()
                SHOW_RECYCLE_BIN_LAST -> config.showRecycleBinLast = value.toBoolean()
                SORT_ORDER -> config.sorting = value.toInt()
                DIRECTORY_SORT_ORDER -> config.directorySorting = value.toInt()
                GROUP_BY -> config.groupBy = value.toInt()
                GROUP_DIRECT_SUBFOLDERS -> config.groupDirectSubfolders = value.toBoolean()
                PINNED_FOLDERS -> config.addPinnedFolders(value.toStringSet())
                DISPLAY_FILE_NAMES -> config.displayFileNames = value.toBoolean()
                FILTER_MEDIA -> config.filterMedia = value.toInt()
                DIR_COLUMN_CNT -> config.dirColumnCnt = value.toInt()
                MEDIA_COLUMN_CNT -> config.mediaColumnCnt = value.toInt()
                SHOW_ALL -> config.showAll = value.toBoolean()
                SHOW_WIDGET_FOLDER_NAME -> config.showWidgetFolderName = value.toBoolean()
                VIEW_TYPE_FILES -> config.viewTypeFiles = value.toInt()
                VIEW_TYPE_FOLDERS -> config.viewTypeFolders = value.toInt()
                SLIDESHOW_INTERVAL -> config.slideshowInterval = value.toInt()
                SLIDESHOW_INCLUDE_VIDEOS -> config.slideshowIncludeVideos = value.toBoolean()
                SLIDESHOW_INCLUDE_GIFS -> config.slideshowIncludeGIFs = value.toBoolean()
                SLIDESHOW_RANDOM_ORDER -> config.slideshowRandomOrder = value.toBoolean()
                SLIDESHOW_MOVE_BACKWARDS -> config.slideshowMoveBackwards = value.toBoolean()
                SLIDESHOW_LOOP -> config.loopSlideshow = value.toBoolean()
                LAST_EDITOR_CROP_ASPECT_RATIO -> config.lastEditorCropAspectRatio = value.toInt()
                LAST_EDITOR_CROP_OTHER_ASPECT_RATIO_X -> config.lastEditorCropOtherAspectRatioX = value.toString().toFloat()
                LAST_EDITOR_CROP_OTHER_ASPECT_RATIO_Y -> config.lastEditorCropOtherAspectRatioY = value.toString().toFloat()
                LAST_CONFLICT_RESOLUTION -> config.lastConflictResolution = value.toInt()
                LAST_CONFLICT_APPLY_TO_ALL -> config.lastConflictApplyToAll = value.toBoolean()
                EDITOR_BRUSH_COLOR -> config.editorBrushColor = value.toInt()
                EDITOR_BRUSH_HARDNESS -> config.editorBrushHardness = value.toString().toFloat()
                EDITOR_BRUSH_SIZE -> config.editorBrushSize = value.toString().toFloat()
                FOLDER_THUMBNAIL_STYLE -> config.folderStyle = value.toInt()
                FOLDER_MEDIA_COUNT -> config.showFolderMediaCount = value.toInt()
                LIMIT_FOLDER_TITLE -> config.limitFolderTitle = value.toBoolean()
                THUMBNAIL_SPACING -> config.thumbnailSpacing = value.toInt()
                FILE_ROUNDED_CORNERS -> config.fileRoundedCorners = value.toBoolean()
                ALBUM_COVERS -> {
                    val existingCovers = config.parseAlbumCovers()
                    val existingCoverPaths = existingCovers.map { it.path }.toMutableList() as ArrayList<String>

                    val listType = object : TypeToken<List<AlbumCover>>() {}.type
                    val covers = Gson().fromJson<ArrayList<AlbumCover>>(value.toString(), listType) ?: ArrayList(1)
                    covers.filter { !existingCoverPaths.contains(it.path) && getDoesFilePathExist(it.tmb) }.forEach {
                        existingCovers.add(it)
                    }

                    config.albumCovers = Gson().toJson(existingCovers)
                }
            }
        }

        toast(if (configValues.size > 0) R.string.settings_imported_successfully else R.string.no_entries_for_importing)
        runOnUiThread {
            setupSettingItems()
        }
    }
}