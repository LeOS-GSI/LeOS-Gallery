package ca.on.sudbury.hojat.smartgallery.activities

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.RecoverableSecurityException
import android.app.role.RoleManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StatFs
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.Settings
import android.telecom.TelecomManager
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.util.Pair
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.asynctasks.CopyMoveTask
import ca.on.sudbury.hojat.smartgallery.dialogs.ConfirmationDialogFragment
import ca.on.sudbury.hojat.smartgallery.dialogs.ExportSettingsDialogFragment
import ca.on.sudbury.hojat.smartgallery.dialogs.FileConflictDialogFragment
import ca.on.sudbury.hojat.smartgallery.dialogs.WritePermissionDialogFragment
import ca.on.sudbury.hojat.smartgallery.extensions.adjustAlpha
import ca.on.sudbury.hojat.smartgallery.extensions.baseConfig
import ca.on.sudbury.hojat.smartgallery.extensions.buildDocumentUriSdk30
import ca.on.sudbury.hojat.smartgallery.extensions.createAndroidDataOrObbPath
import ca.on.sudbury.hojat.smartgallery.extensions.createAndroidDataOrObbUri
import ca.on.sudbury.hojat.smartgallery.extensions.createFirstParentTreeUri
import ca.on.sudbury.hojat.smartgallery.extensions.deleteFromMediaStore
import ca.on.sudbury.hojat.smartgallery.extensions.getColoredDrawableWithColor
import ca.on.sudbury.hojat.smartgallery.extensions.getContrastColor
import ca.on.sudbury.hojat.smartgallery.extensions.getCurrentFormattedDateTime
import ca.on.sudbury.hojat.smartgallery.extensions.getDoesFilePathExist
import ca.on.sudbury.hojat.smartgallery.extensions.getFileOutputStream
import ca.on.sudbury.hojat.smartgallery.extensions.getFirstParentLevel
import ca.on.sudbury.hojat.smartgallery.extensions.getFirstParentPath
import ca.on.sudbury.hojat.smartgallery.extensions.getPermissionString
import ca.on.sudbury.hojat.smartgallery.extensions.getProperTextColor
import ca.on.sudbury.hojat.smartgallery.extensions.getThemeId
import ca.on.sudbury.hojat.smartgallery.extensions.hasPermission
import ca.on.sudbury.hojat.smartgallery.extensions.hasProperStoredTreeUri
import ca.on.sudbury.hojat.smartgallery.extensions.humanizePath
import ca.on.sudbury.hojat.smartgallery.extensions.isAccessibleWithSAFSdk30
import ca.on.sudbury.hojat.smartgallery.extensions.isRestrictedSAFOnlyRoot
import ca.on.sudbury.hojat.smartgallery.extensions.isShowingAndroidSAFDialog
import ca.on.sudbury.hojat.smartgallery.extensions.isShowingSAFCreateDocumentDialogSdk30
import ca.on.sudbury.hojat.smartgallery.extensions.isShowingSAFDialog
import ca.on.sudbury.hojat.smartgallery.extensions.isShowingSAFDialogSdk30
import ca.on.sudbury.hojat.smartgallery.extensions.launchViewIntent
import ca.on.sudbury.hojat.smartgallery.extensions.showOTGPermissionDialog
import ca.on.sudbury.hojat.smartgallery.extensions.storeAndroidTreeUri
import ca.on.sudbury.hojat.smartgallery.extensions.toFileDirItem
import ca.on.sudbury.hojat.smartgallery.extensions.toHex
import ca.on.sudbury.hojat.smartgallery.extensions.updateOTGPathFromPartition
import ca.on.sudbury.hojat.smartgallery.helpers.APP_FAQ
import ca.on.sudbury.hojat.smartgallery.helpers.APP_ICON_IDS
import ca.on.sudbury.hojat.smartgallery.helpers.APP_LAUNCHER_NAME
import ca.on.sudbury.hojat.smartgallery.helpers.APP_LICENSES
import ca.on.sudbury.hojat.smartgallery.helpers.APP_NAME
import ca.on.sudbury.hojat.smartgallery.helpers.APP_VERSION_NAME
import ca.on.sudbury.hojat.smartgallery.helpers.AlphaLevel
import ca.on.sudbury.hojat.smartgallery.helpers.CONFLICT_KEEP_BOTH
import ca.on.sudbury.hojat.smartgallery.helpers.CONFLICT_SKIP
import ca.on.sudbury.hojat.smartgallery.helpers.CREATE_DOCUMENT_SDK_30
import ca.on.sudbury.hojat.smartgallery.helpers.EXTERNAL_STORAGE_PROVIDER_AUTHORITY
import ca.on.sudbury.hojat.smartgallery.helpers.INVALID_NAVIGATION_BAR_COLOR
import ca.on.sudbury.hojat.smartgallery.helpers.NavigationIcon
import ca.on.sudbury.hojat.smartgallery.helpers.OPEN_DOCUMENT_TREE_FOR_ANDROID_DATA_OR_OBB
import ca.on.sudbury.hojat.smartgallery.helpers.OPEN_DOCUMENT_TREE_FOR_SDK_30
import ca.on.sudbury.hojat.smartgallery.helpers.OPEN_DOCUMENT_TREE_OTG
import ca.on.sudbury.hojat.smartgallery.helpers.OPEN_DOCUMENT_TREE_SD
import ca.on.sudbury.hojat.smartgallery.helpers.PERMISSION_WRITE_STORAGE
import ca.on.sudbury.hojat.smartgallery.helpers.REQUEST_CODE_SET_DEFAULT_DIALER
import ca.on.sudbury.hojat.smartgallery.helpers.SD_OTG_SHORT
import ca.on.sudbury.hojat.smartgallery.helpers.SELECT_EXPORT_SETTINGS_FILE_INTENT
import ca.on.sudbury.hojat.smartgallery.helpers.SHOW_FAQ_BEFORE_MAIL
import ca.on.sudbury.hojat.smartgallery.helpers.getConflictResolution
import ca.on.sudbury.hojat.smartgallery.helpers.sumByLong
import ca.on.sudbury.hojat.smartgallery.interfaces.CopyMoveListener
import ca.on.sudbury.hojat.smartgallery.models.FaqItem
import ca.on.sudbury.hojat.smartgallery.models.FileDirItem
import ca.on.sudbury.hojat.smartgallery.usecases.ApplyColorFilterUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.FormatFileSizeUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.HideKeyboardUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.IsMarshmallowPlusUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.IsOreoPlusUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.IsPathOnOtgUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.IsPathOnSdUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.IsQPlusUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.IsRPlusUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.IsSPlusUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.RunOnBackgroundThreadUseCase
import com.google.android.material.appbar.MaterialToolbar
import java.io.File
import java.io.OutputStream
import java.util.regex.Pattern

private const val GENERIC_PERM_HANDLER = 100
private const val DELETE_FILE_SDK_30_HANDLER = 300
private const val RECOVERABLE_SECURITY_HANDLER = 301
private const val UPDATE_FILE_SDK_30_HANDLER = 302
private const val MANAGE_MEDIA_RC = 303

abstract class BaseSimpleActivity : AppCompatActivity() {
    var copyMoveCallback: ((destinationPath: String) -> Unit)? = null
    private var actionOnPermission: ((granted: Boolean) -> Unit)? = null
    private var isAskingPermissions = false
    var useDynamicTheme = true
    var showTransparentTop = false
    var showTransparentNavigation = false
    var checkedDocumentPath = ""
    private var configItemsToExport = LinkedHashMap<String, Any>()

    companion object {
        var funAfterSAFPermission: ((success: Boolean) -> Unit)? = null
        var funAfterSdk30Action: ((success: Boolean) -> Unit)? = null
        var funAfterUpdate30File: ((success: Boolean) -> Unit)? = null
        var funRecoverableSecurity: ((success: Boolean) -> Unit)? = null
        var funAfterManageMediaPermission: (() -> Unit)? = null
    }

    abstract fun getAppIconIDs(): ArrayList<Int>

    abstract fun getAppLauncherName(): String

    override fun onCreate(savedInstanceState: Bundle?) {
        if (useDynamicTheme) {
            setTheme(getThemeId(showTransparentTop = showTransparentTop))
        }
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("NewApi")
    override fun onResume() {
        super.onResume()
        if (useDynamicTheme) {
            setTheme(getThemeId(showTransparentTop = showTransparentTop))

            val backgroundColor = if (baseConfig.isUsingSystemTheme) {
                resources.getColor(R.color.you_background_color, theme)
            } else {
                baseConfig.backgroundColor
            }

            updateBackgroundColor(backgroundColor)
        }

        if (showTransparentTop) {
            window.statusBarColor = Color.TRANSPARENT
        } else {
            val color = if (baseConfig.isUsingSystemTheme) {
                resources.getColor(R.color.you_status_bar_color)
            } else {
                getProperStatusBarColor()
            }

            updateActionbarColor(color)
        }

        updateRecentsAppIcon()
        updateNavigationBarColor()
    }

    override fun onDestroy() {
        super.onDestroy()
        funAfterSAFPermission = null
        actionOnPermission = null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                HideKeyboardUseCase(this)
                finish()
            }
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase)
    }

    fun updateBackgroundColor(color: Int = baseConfig.backgroundColor) {
        window.decorView.setBackgroundColor(color)
    }

    fun updateStatusbarColor(color: Int) {
        window.statusBarColor = color

        if (IsMarshmallowPlusUseCase()) {
            if (color.getContrastColor() == 0xFF333333.toInt()) {
                window.decorView.systemUiVisibility =
                    addBit(
                        window.decorView.systemUiVisibility,
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    )

            } else {
                window.decorView.systemUiVisibility =
                    removeBit(
                        window.decorView.systemUiVisibility,
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    )

            }
        }
    }

    fun updateActionbarColor(color: Int = getProperStatusBarColor()) {
        val text = supportActionBar?.title.toString()
        val colorToUse = if (baseConfig.isUsingSystemTheme) {
            getProperTextColor()
        } else {
            color.getContrastColor()
        }
        supportActionBar?.title = Html.fromHtml("<font color='${colorToUse.toHex()}'>$text</font>")
        supportActionBar?.setBackgroundDrawable(ColorDrawable(color))
        updateStatusbarColor(color)
        setTaskDescription(ActivityManager.TaskDescription(null, null, color))
    }

    fun updateNavigationBarColor(color: Int = baseConfig.navigationBarColor) {
        if (baseConfig.navigationBarColor != INVALID_NAVIGATION_BAR_COLOR) {
            val colorToUse = if (color == -2) -1 else color
            window.navigationBarColor = colorToUse

            if (IsOreoPlusUseCase()) {
                if (color.getContrastColor() == 0xFF333333.toInt()) {
                    window.decorView.systemUiVisibility =
                        addBit(
                            window.decorView.systemUiVisibility,
                            View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                        )

                } else {
                    window.decorView.systemUiVisibility =
                        removeBit(
                            window.decorView.systemUiVisibility,
                            View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                        )
                }
            }
        }
    }

    private fun updateRecentsAppIcon() {
        if (baseConfig.isUsingModifiedAppIcon) {
            val appIconIDs = getAppIconIDs()
            val currentAppIconColorIndex = getCurrentAppIconColorIndex()
            if (appIconIDs.size - 1 < currentAppIconColorIndex) {
                return
            }

            val recentsIcon =
                BitmapFactory.decodeResource(resources, appIconIDs[currentAppIconColorIndex])
            val title = getAppLauncherName()
            val color = baseConfig.primaryColor

            val description = ActivityManager.TaskDescription(title, recentsIcon, color)
            setTaskDescription(description)
        }
    }

    fun updateMenuItemColors(
        menu: Menu?,
        useCrossAsBack: Boolean = false,
        baseColor: Int = getProperStatusBarColor(),
        updateHomeAsUpColor: Boolean = true,
        isContextualMenu: Boolean = false,
        forceWhiteIcons: Boolean = false
    ) {
        if (menu == null) {
            return
        }

        var color = baseColor.getContrastColor()
        if (baseConfig.isUsingSystemTheme && !isContextualMenu) {
            color = getProperTextColor()
        }

        if (forceWhiteIcons) {
            color = Color.WHITE
        }

        for (i in 0 until menu.size()) {
            menu.getItem(i)?.icon?.setTint(color)
        }

        if (updateHomeAsUpColor && !isContextualMenu) {
            val drawableId =
                if (useCrossAsBack) R.drawable.ic_cross_vector else R.drawable.ic_arrow_left_vector
            val icon = resources.getColoredDrawableWithColor(drawableId, color)
            supportActionBar?.setHomeAsUpIndicator(icon)
        }
    }

    private fun getCurrentAppIconColorIndex(): Int {
        val appIconColor = baseConfig.appIconColor
        this.resources.getIntArray(R.array.md_app_icon_colors).toCollection(ArrayList())
            .forEachIndexed { index, color ->
                if (color == appIconColor) {
                    return index
                }
            }
        return 0
    }

    fun setTranslucentNavigation() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        val partition = try {
            checkedDocumentPath.substring(9, 18)
        } catch (e: Exception) {
            ""
        }

        val sdOtgPattern = Pattern.compile(SD_OTG_SHORT)
        if (requestCode == CREATE_DOCUMENT_SDK_30) {
            if (resultCode == RESULT_OK && resultData != null && resultData.data != null) {

                val treeUri = resultData.data
                val checkedUri = buildDocumentUriSdk30(checkedDocumentPath)

                if (treeUri != checkedUri) {
                    Toast.makeText(
                        this,
                        getString(R.string.wrong_folder_selected, checkedDocumentPath),
                        Toast.LENGTH_LONG
                    ).show()
                    return
                }

                val takeFlags =
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                applicationContext.contentResolver.takePersistableUriPermission(treeUri, takeFlags)
                val funAfter = funAfterSdk30Action
                funAfterSdk30Action = null
                funAfter?.invoke(true)
            } else {
                funAfterSdk30Action?.invoke(false)
            }

        } else if (requestCode == OPEN_DOCUMENT_TREE_FOR_SDK_30) {
            if (resultCode == RESULT_OK && resultData != null && resultData.data != null) {
                val treeUri = resultData.data
                val checkedUri = createFirstParentTreeUri(checkedDocumentPath)

                if (treeUri != checkedUri) {
                    val level = getFirstParentLevel(checkedDocumentPath)
                    val firstParentPath = checkedDocumentPath.getFirstParentPath(this, level)
                    Toast.makeText(
                        this,
                        getString(R.string.wrong_folder_selected, humanizePath(firstParentPath)),
                        Toast.LENGTH_LONG
                    ).show()
                    return
                }

                val takeFlags =
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                applicationContext.contentResolver.takePersistableUriPermission(treeUri, takeFlags)
                val funAfter = funAfterSdk30Action
                funAfterSdk30Action = null
                funAfter?.invoke(true)
            } else {
                funAfterSdk30Action?.invoke(false)
            }

        } else if (requestCode == OPEN_DOCUMENT_TREE_FOR_ANDROID_DATA_OR_OBB) {
            if (resultCode == RESULT_OK && resultData != null && resultData.data != null) {
                if (isProperAndroidRoot(checkedDocumentPath, resultData.data!!)) {
                    if (resultData.dataString == baseConfig.otgTreeUri || resultData.dataString == baseConfig.sdTreeUri) {
                        val pathToSelect = createAndroidDataOrObbPath(checkedDocumentPath)
                        Toast.makeText(
                            this,
                            getString(R.string.wrong_folder_selected, pathToSelect),
                            Toast.LENGTH_LONG
                        ).show()
                        return
                    }

                    val treeUri = resultData.data
                    storeAndroidTreeUri(checkedDocumentPath, treeUri.toString())

                    val takeFlags =
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    applicationContext.contentResolver.takePersistableUriPermission(
                        treeUri!!,
                        takeFlags
                    )
                    funAfterSAFPermission?.invoke(true)
                    funAfterSAFPermission = null
                } else {
                    Toast.makeText(
                        this,
                        getString(
                            R.string.wrong_folder_selected,
                            createAndroidDataOrObbPath(checkedDocumentPath)
                        ),
                        Toast.LENGTH_LONG
                    ).show()
                    Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                        if (IsRPlusUseCase()) {
                            putExtra(
                                DocumentsContract.EXTRA_INITIAL_URI,
                                createAndroidDataOrObbUri(checkedDocumentPath)
                            )
                        }

                        try {
                            startActivityForResult(this, requestCode)
                        } catch (e: Exception) {
                            Toast.makeText(this@BaseSimpleActivity, e.toString(), Toast.LENGTH_LONG)
                                .show()
                        }
                    }
                }
            } else {
                funAfterSAFPermission?.invoke(false)
            }
        } else if (requestCode == OPEN_DOCUMENT_TREE_SD) {
            if (resultCode == RESULT_OK && resultData != null && resultData.data != null) {
                val isProperPartition = partition.isEmpty() || !sdOtgPattern.matcher(partition)
                    .matches() || (sdOtgPattern.matcher(partition)
                    .matches() && resultData.dataString!!.contains(partition))
                if (isProperSDRootFolder(resultData.data!!) && isProperPartition) {
                    if (resultData.dataString == baseConfig.otgTreeUri) {
                        Toast.makeText(this, R.string.sd_card_usb_same, Toast.LENGTH_LONG).show()
                        return
                    }

                    saveTreeUri(resultData)
                    funAfterSAFPermission?.invoke(true)
                    funAfterSAFPermission = null
                } else {
                    Toast.makeText(this, R.string.wrong_root_selected, Toast.LENGTH_LONG).show()
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)

                    try {
                        startActivityForResult(intent, requestCode)
                    } catch (e: Exception) {
                        Toast.makeText(this@BaseSimpleActivity, e.toString(), Toast.LENGTH_LONG)
                            .show()
                    }
                }
            } else {
                funAfterSAFPermission?.invoke(false)
            }
        } else if (requestCode == OPEN_DOCUMENT_TREE_OTG) {
            if (resultCode == RESULT_OK && resultData != null && resultData.data != null) {
                val isProperPartition = partition.isEmpty() || !sdOtgPattern.matcher(partition)
                    .matches() || (sdOtgPattern.matcher(partition)
                    .matches() && resultData.dataString!!.contains(partition))
                if (isProperOTGRootFolder(resultData.data!!) && isProperPartition) {
                    if (resultData.dataString == baseConfig.sdTreeUri) {
                        funAfterSAFPermission?.invoke(false)
                        Toast.makeText(this, R.string.sd_card_usb_same, Toast.LENGTH_LONG).show()
                        return
                    }
                    baseConfig.otgTreeUri = resultData.dataString!!
                    baseConfig.otgPartition =
                        baseConfig.otgTreeUri.removeSuffix("%3A").substringAfterLast('/')
                            .trimEnd('/')
                    updateOTGPathFromPartition()

                    val takeFlags =
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    applicationContext.contentResolver.takePersistableUriPermission(
                        resultData.data!!,
                        takeFlags
                    )

                    funAfterSAFPermission?.invoke(true)
                    funAfterSAFPermission = null
                } else {
                    Toast.makeText(this, R.string.wrong_root_selected_usb, Toast.LENGTH_LONG).show()
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)

                    try {
                        startActivityForResult(intent, requestCode)
                    } catch (e: Exception) {
                        Toast.makeText(this@BaseSimpleActivity, e.toString(), Toast.LENGTH_LONG)
                            .show()
                    }
                }
            } else {
                funAfterSAFPermission?.invoke(false)
            }
        } else if (requestCode == SELECT_EXPORT_SETTINGS_FILE_INTENT && resultCode == RESULT_OK && resultData != null && resultData.data != null) {
            val outputStream = contentResolver.openOutputStream(resultData.data!!)
            exportSettingsTo(outputStream, configItemsToExport)
        } else if (requestCode == DELETE_FILE_SDK_30_HANDLER) {
            funAfterSdk30Action?.invoke(resultCode == RESULT_OK)
        } else if (requestCode == RECOVERABLE_SECURITY_HANDLER) {
            funRecoverableSecurity?.invoke(resultCode == RESULT_OK)
            funRecoverableSecurity = null
        } else if (requestCode == UPDATE_FILE_SDK_30_HANDLER) {
            funAfterUpdate30File?.invoke(resultCode == RESULT_OK)
        } else if (requestCode == MANAGE_MEDIA_RC) {
            funAfterManageMediaPermission?.invoke()
        }
    }

    private fun saveTreeUri(resultData: Intent) {
        val treeUri = resultData.data
        baseConfig.sdTreeUri = treeUri.toString()

        val takeFlags =
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        applicationContext.contentResolver.takePersistableUriPermission(treeUri!!, takeFlags)
    }

    private fun isProperSDRootFolder(uri: Uri) =
        isExternalStorageDocument(uri) && isRootUri(uri) && !isInternalStorage(uri)

    private fun isProperSDFolder(uri: Uri) =
        isExternalStorageDocument(uri) && !isInternalStorage(uri)

    private fun isProperOTGRootFolder(uri: Uri) =
        isExternalStorageDocument(uri) && isRootUri(uri) && !isInternalStorage(uri)

    private fun isProperOTGFolder(uri: Uri) =
        isExternalStorageDocument(uri) && !isInternalStorage(uri)

    private fun isRootUri(uri: Uri) = uri.lastPathSegment?.endsWith(":") ?: false

    private fun isInternalStorage(uri: Uri) =
        isExternalStorageDocument(uri) && DocumentsContract.getTreeDocumentId(uri)
            .contains("primary")

    private fun isAndroidDir(uri: Uri) =
        isExternalStorageDocument(uri) && DocumentsContract.getTreeDocumentId(uri)
            .contains(":Android")

    private fun isInternalStorageAndroidDir(uri: Uri) = isInternalStorage(uri) && isAndroidDir(uri)
    private fun isOTGAndroidDir(uri: Uri) = isProperOTGFolder(uri) && isAndroidDir(uri)
    private fun isSDAndroidDir(uri: Uri) = isProperSDFolder(uri) && isAndroidDir(uri)
    private fun isExternalStorageDocument(uri: Uri) =
        EXTERNAL_STORAGE_PROVIDER_AUTHORITY == uri.authority

    private fun isProperAndroidRoot(path: String, uri: Uri): Boolean {
        return when {
            IsPathOnOtgUseCase(this, path) -> isOTGAndroidDir(uri)
            IsPathOnSdUseCase(this, path) -> isSDAndroidDir(uri)
            else -> isInternalStorageAndroidDir(uri)
        }
    }

    fun startAboutActivity(
        appNameId: Int,
        licenseMask: Long,
        versionName: String,
        faqItems: ArrayList<FaqItem>,
        showFAQBeforeMail: Boolean
    ) {
        HideKeyboardUseCase(this)
        Intent(applicationContext, AboutActivity::class.java).apply {
            putExtra(APP_ICON_IDS, getAppIconIDs())
            putExtra(APP_LAUNCHER_NAME, getAppLauncherName())
            putExtra(APP_NAME, getString(appNameId))
            putExtra(APP_LICENSES, licenseMask)
            putExtra(APP_VERSION_NAME, versionName)
            putExtra(APP_FAQ, faqItems)
            putExtra(SHOW_FAQ_BEFORE_MAIL, showFAQBeforeMail)
            startActivity(this)
        }
    }

    fun startCustomizationActivity() {
        if (!packageName.contains("slootelibomelpmis".reversed(), true)) {
            if (baseConfig.appRunCount > 100) {
                val label =
                    "You are using a fake version of the app. For your own safety download the original one from www.simplemobiletools.com. Thanks"

                val callback: () -> Unit =
                    { launchViewIntent("https://github.com/hojat72elect/Smart-Gallery") }
                ConfirmationDialogFragment(
                    message = label,
                    positive = R.string.ok,
                    negative = 0,
                    callbackAfterDialogConfirmed = callback
                ).show(
                    supportFragmentManager,
                    ConfirmationDialogFragment.TAG
                )
                return
            }
        }

        Intent(applicationContext, CustomizationActivity::class.java).apply {
            putExtra(APP_ICON_IDS, getAppIconIDs())
            putExtra(APP_LAUNCHER_NAME, getAppLauncherName())
            startActivity(this)
        }
    }

    fun handleCustomizeColorsClick() {
        startCustomizationActivity()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun launchCustomizeNotificationsIntent() {
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            startActivity(this)
        }
    }

    // synchronous return value determines only if we are showing the SAF dialog, callback result tells if the SD or OTG permission has been granted
    fun handleSAFDialog(path: String, callback: (success: Boolean) -> Unit): Boolean {
        HideKeyboardUseCase(this)
        return if (isShowingSAFDialog(path) || isShowingOTGDialog(path)) {
            funAfterSAFPermission = callback
            true
        } else {
            callback(true)
            false
        }
    }

    fun handleSAFDialogSdk30(path: String, callback: (success: Boolean) -> Unit): Boolean {
        HideKeyboardUseCase(this)
        return if (isShowingSAFDialogSdk30(path)) {
            funAfterSdk30Action = callback
            true
        } else {
            callback(true)
            false
        }
    }

    fun checkManageMediaOrHandleSAFDialogSdk30(
        path: String,
        callback: (success: Boolean) -> Unit
    ): Boolean {
        HideKeyboardUseCase(this)
        return if (IsSPlusUseCase() && MediaStore.canManageMedia(this)) {
            callback(true)
            false
        } else {
            handleSAFDialogSdk30(path, callback)
        }
    }

    fun handleSAFCreateDocumentDialogSdk30(
        path: String,
        callback: (success: Boolean) -> Unit
    ): Boolean {
        HideKeyboardUseCase(this)
        return if (isShowingSAFCreateDocumentDialogSdk30(path)) {
            funAfterSdk30Action = callback
            true
        } else {
            callback(true)
            false
        }
    }

    fun handleAndroidSAFDialog(path: String, callback: (success: Boolean) -> Unit): Boolean {
        HideKeyboardUseCase(this)
        return if (isShowingAndroidSAFDialog(path)) {
            funAfterSAFPermission = callback
            true
        } else {
            callback(true)
            false
        }
    }

    fun handleOTGPermission(callback: (success: Boolean) -> Unit) {
        HideKeyboardUseCase(this)
        if (baseConfig.otgTreeUri.isNotEmpty()) {
            callback(true)
            return
        }

        funAfterSAFPermission = callback

        val funAfterWritePermissionGranted: () -> Unit = {
            Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                try {
                    startActivityForResult(this, OPEN_DOCUMENT_TREE_OTG)
                    return@apply
                } catch (e: Exception) {
                    type = "*/*"
                }

                try {
                    startActivityForResult(this, OPEN_DOCUMENT_TREE_OTG)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(
                        this@BaseSimpleActivity,
                        R.string.system_service_disabled,
                        Toast.LENGTH_LONG
                    ).show()
                } catch (e: Exception) {
                    Toast.makeText(
                        this@BaseSimpleActivity,
                        R.string.unknown_error_occurred,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        WritePermissionDialogFragment(
            WritePermissionDialogFragment.Mode.Otg,
            funAfterWritePermissionGranted
        ).show(supportFragmentManager, WritePermissionDialogFragment.TAG)
    }

    @SuppressLint("NewApi")
    fun deleteSDK30Uris(uris: List<Uri>, callback: (success: Boolean) -> Unit) {
        HideKeyboardUseCase(this)
        if (IsRPlusUseCase()) {
            funAfterSdk30Action = callback
            try {
                val deleteRequest =
                    MediaStore.createDeleteRequest(contentResolver, uris).intentSender
                startIntentSenderForResult(deleteRequest, DELETE_FILE_SDK_30_HANDLER, null, 0, 0, 0)
            } catch (e: Exception) {
                Toast.makeText(this@BaseSimpleActivity, e.toString(), Toast.LENGTH_LONG).show()
            }
        } else {
            callback(false)
        }
    }

    @SuppressLint("NewApi")
    fun updateSDK30Uris(uris: List<Uri>, callback: (success: Boolean) -> Unit) {
        HideKeyboardUseCase(this)
        if (IsRPlusUseCase()) {
            funAfterUpdate30File = callback
            try {
                val writeRequest = MediaStore.createWriteRequest(contentResolver, uris).intentSender
                startIntentSenderForResult(writeRequest, UPDATE_FILE_SDK_30_HANDLER, null, 0, 0, 0)
            } catch (e: Exception) {
                Toast.makeText(this@BaseSimpleActivity, e.toString(), Toast.LENGTH_LONG).show()
            }
        } else {
            callback(false)
        }
    }

    @SuppressLint("NewApi")
    fun handleRecoverableSecurityException(callback: (success: Boolean) -> Unit) {
        try {
            callback.invoke(true)
        } catch (securityException: SecurityException) {
            if (IsQPlusUseCase()) {
                funRecoverableSecurity = callback
                val recoverableSecurityException =
                    securityException as? RecoverableSecurityException ?: throw securityException
                val intentSender = recoverableSecurityException.userAction.actionIntent.intentSender
                startIntentSenderForResult(
                    intentSender,
                    RECOVERABLE_SECURITY_HANDLER,
                    null,
                    0,
                    0,
                    0
                )
            } else {
                callback(false)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun launchMediaManagementIntent(callback: () -> Unit) {
        Intent(Settings.ACTION_REQUEST_MANAGE_MEDIA).apply {
            data = Uri.parse("package:$packageName")
            startActivityForResult(this, MANAGE_MEDIA_RC)
        }
        funAfterManageMediaPermission = callback
    }

    fun copyMoveFilesTo(
        fileDirItems: ArrayList<FileDirItem>,
        source: String,
        destination: String,
        isCopyOperation: Boolean,
        copyPhotoVideoOnly: Boolean,
        copyHidden: Boolean,
        callback: (destinationPath: String) -> Unit
    ) {
        if (source == destination) {
            Toast.makeText(this, R.string.source_and_destination_same, Toast.LENGTH_LONG).show()
            return
        }

        if (!getDoesFilePathExist(destination)) {
            Toast.makeText(this, R.string.invalid_destination, Toast.LENGTH_LONG).show()
            return
        }

        handleSAFDialog(destination) {
            if (!it) {
                copyMoveListener.copyFailed()
                return@handleSAFDialog
            }

            handleSAFDialogSdk30(destination) {
                if (!it) {
                    copyMoveListener.copyFailed()
                    return@handleSAFDialogSdk30
                }

                copyMoveCallback = callback
                var fileCountToCopy = fileDirItems.size
                if (isCopyOperation) {
                    startCopyMove(
                        fileDirItems,
                        destination,
                        isCopyOperation,
                        copyPhotoVideoOnly,
                        copyHidden
                    )
                } else {
                    if (IsPathOnOtgUseCase(this, source) ||
                        IsPathOnOtgUseCase(this, destination) ||
                        IsPathOnSdUseCase(this, source) ||
                        IsPathOnSdUseCase(this, destination) ||
                        isRestrictedSAFOnlyRoot(source) ||
                        isRestrictedSAFOnlyRoot(destination) ||
                        isAccessibleWithSAFSdk30(source) ||
                        isAccessibleWithSAFSdk30(destination) ||
                        fileDirItems.first().isDirectory
                    ) {
                        handleSAFDialog(source) {
                            if (it) {
                                startCopyMove(
                                    fileDirItems,
                                    destination,
                                    isCopyOperation,
                                    copyPhotoVideoOnly,
                                    copyHidden
                                )
                            }
                        }
                    } else {
                        try {
                            checkConflicts(fileDirItems, destination, 0, LinkedHashMap()) {
                                Toast.makeText(this, R.string.moving, Toast.LENGTH_LONG).show()
                                RunOnBackgroundThreadUseCase {
                                    val updatedPaths = ArrayList<String>(fileDirItems.size)
                                    val destinationFolder = File(destination)
                                    for (oldFileDirItem in fileDirItems) {
                                        var newFile = File(destinationFolder, oldFileDirItem.name)
                                        if (newFile.exists()) {
                                            when {
                                                getConflictResolution(
                                                    it,
                                                    newFile.absolutePath
                                                ) == CONFLICT_SKIP -> fileCountToCopy--
                                                getConflictResolution(
                                                    it,
                                                    newFile.absolutePath
                                                ) == CONFLICT_KEEP_BOTH -> newFile =
                                                    getAlternativeFile(newFile)
                                                else ->
                                                    // this file is guaranteed to be on the internal storage, so just delete it this way
                                                    newFile.delete()
                                            }
                                        }

                                        if (!newFile.exists() && File(oldFileDirItem.path).renameTo(
                                                newFile
                                            )
                                        ) {
                                            if (!baseConfig.keepLastModified) {
                                                newFile.setLastModified(System.currentTimeMillis())
                                            }
                                            updatedPaths.add(newFile.absolutePath)
                                            deleteFromMediaStore(oldFileDirItem.path)
                                        }
                                    }

                                    runOnUiThread {
                                        if (updatedPaths.isEmpty()) {
                                            copyMoveListener.copySucceeded(
                                                false,
                                                fileCountToCopy == 0,
                                                destination,
                                                false
                                            )
                                        } else {
                                            copyMoveListener.copySucceeded(
                                                false,
                                                fileCountToCopy <= updatedPaths.size,
                                                destination,
                                                updatedPaths.size == 1
                                            )
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Toast.makeText(this@BaseSimpleActivity, e.toString(), Toast.LENGTH_LONG)
                                .show()
                        }
                    }
                }
            }
        }
    }

    fun getAlternativeFile(file: File): File {
        var fileIndex = 1
        var newFile: File?
        do {
            val newName =
                String.format("%s(%d).%s", file.nameWithoutExtension, fileIndex, file.extension)
            newFile = File(file.parent, newName)
            fileIndex++
        } while (getDoesFilePathExist(newFile!!.absolutePath))
        return newFile
    }

    private fun startCopyMove(
        files: ArrayList<FileDirItem>,
        destinationPath: String,
        isCopyOperation: Boolean,
        copyPhotoVideoOnly: Boolean,
        copyHidden: Boolean
    ) {
        val availableSpace = getAvailableStorageB(destinationPath)
        val sumToCopy = files.sumByLong { it.getProperSize(applicationContext, copyHidden) }
        if (availableSpace == -1L || sumToCopy < availableSpace) {
            checkConflicts(files, destinationPath, 0, LinkedHashMap()) {
                Toast.makeText(
                    this,
                    if (isCopyOperation) R.string.copying else R.string.moving,
                    Toast.LENGTH_LONG
                ).show()
                val pair = Pair(files, destinationPath)
                CopyMoveTask(
                    this,
                    isCopyOperation,
                    copyPhotoVideoOnly,
                    it,
                    copyMoveListener,
                    copyHidden
                ).execute(pair)
            }
        } else {
            val text = String.format(
                getString(R.string.no_space),
                FormatFileSizeUseCase(sumToCopy),
                FormatFileSizeUseCase(availableSpace)
            )
            Toast.makeText(this, text, Toast.LENGTH_LONG).show()
        }
    }

    private fun checkConflicts(
        files: ArrayList<FileDirItem>,
        destinationPath: String,
        index: Int,
        conflictResolutions: LinkedHashMap<String, Int>,
        callback: (resolutions: LinkedHashMap<String, Int>) -> Unit
    ) {
        if (index == files.size) {
            callback(conflictResolutions)
            return
        }

        val file = files[index]
        val newFileDirItem =
            FileDirItem("$destinationPath/${file.name}", file.name, file.isDirectory)
        if (getDoesFilePathExist(newFileDirItem.path)) {
            val fileConflictDialogCallback = { resolution: Int, applyForAll: Boolean ->
                if (applyForAll) {
                    conflictResolutions.clear()
                    conflictResolutions[""] = resolution
                    checkConflicts(
                        files,
                        destinationPath,
                        files.size,
                        conflictResolutions,
                        callback
                    )
                } else {
                    conflictResolutions[newFileDirItem.path] = resolution
                    checkConflicts(files, destinationPath, index + 1, conflictResolutions, callback)
                }
            }
            FileConflictDialogFragment(
                newFileDirItem,
                files.size > 1,
                fileConflictDialogCallback
            ).show(
                supportFragmentManager,
                FileConflictDialogFragment.TAG
            )
        } else {
            checkConflicts(files, destinationPath, index + 1, conflictResolutions, callback)
        }
    }

    fun handlePermission(permissionId: Int, callback: (granted: Boolean) -> Unit) {
        actionOnPermission = null
        if (hasPermission(permissionId)) {
            callback(true)
        } else {
            isAskingPermissions = true
            actionOnPermission = callback
            ActivityCompat.requestPermissions(
                this,
                arrayOf(getPermissionString(permissionId)),
                GENERIC_PERM_HANDLER
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        isAskingPermissions = false
        if (requestCode == GENERIC_PERM_HANDLER && grantResults.isNotEmpty()) {
            actionOnPermission?.invoke(grantResults[0] == 0)
        }
    }

    private val copyMoveListener = object : CopyMoveListener {
        override fun copySucceeded(
            copyOnly: Boolean,
            copiedAll: Boolean,
            destinationPath: String,
            wasCopyingOneFileOnly: Boolean
        ) {
            if (copyOnly) {
                Toast.makeText(
                    this@BaseSimpleActivity,
                    if (copiedAll) {
                        if (wasCopyingOneFileOnly) {
                            R.string.copying_success_one
                        } else {
                            R.string.copying_success
                        }
                    } else {
                        R.string.copying_success_partial
                    }, Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    this@BaseSimpleActivity,
                    if (copiedAll) {
                        if (wasCopyingOneFileOnly) {
                            R.string.moving_success_one
                        } else {
                            R.string.moving_success
                        }
                    } else {
                        R.string.moving_success_partial
                    }, Toast.LENGTH_LONG
                ).show()
            }

            copyMoveCallback?.invoke(destinationPath)
            copyMoveCallback = null
        }

        override fun copyFailed() {
            Toast.makeText(this@BaseSimpleActivity, R.string.copy_move_failed, Toast.LENGTH_LONG)
                .show()
            copyMoveCallback = null
        }
    }

    fun exportSettings(configItems: LinkedHashMap<String, Any>) {
        if (IsQPlusUseCase()) {
            configItemsToExport = configItems
            val callback: (path: String, filename: String) -> Unit = { _, filename ->
                Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TITLE, filename)
                    addCategory(Intent.CATEGORY_OPENABLE)

                    try {
                        startActivityForResult(this, SELECT_EXPORT_SETTINGS_FILE_INTENT)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(
                            this@BaseSimpleActivity,
                            R.string.system_service_disabled,
                            Toast.LENGTH_LONG
                        ).show()
                    } catch (e: Exception) {
                        Toast.makeText(this@BaseSimpleActivity, e.toString(), Toast.LENGTH_LONG)
                            .show()
                    }
                }
            }
            ExportSettingsDialogFragment(
                getExportSettingsFilename(),
                true,
                callback
            ).show(supportFragmentManager, ExportSettingsDialogFragment.TAG)
        } else {
            handlePermission(PERMISSION_WRITE_STORAGE) { it ->
                if (it) {
                    val callback: (path: String, filename: String) -> Unit = { path, _ ->
                        val file = File(path)
                        getFileOutputStream(file.toFileDirItem(this), true) {
                            exportSettingsTo(it, configItems)
                        }
                    }
                    ExportSettingsDialogFragment(
                        getExportSettingsFilename(),
                        false, callback
                    ).show(supportFragmentManager, ExportSettingsDialogFragment.TAG)
                }
            }
        }
    }

    private fun exportSettingsTo(
        outputStream: OutputStream?,
        configItems: LinkedHashMap<String, Any>
    ) {
        if (outputStream == null) {
            Toast.makeText(this, R.string.unknown_error_occurred, Toast.LENGTH_LONG).show()
            return
        }

        RunOnBackgroundThreadUseCase {
            outputStream.bufferedWriter().use { out ->
                for ((key, value) in configItems) {
                    val line = "$key=$value"
                    out.write(line)
                    out.newLine()

                }
            }
            runOnUiThread {
                Toast.makeText(this, R.string.settings_exported_successfully, Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    private fun getExportSettingsFilename(): String {
        val appName = "smartgallery"
        return "$appName-settings_${getCurrentFormattedDateTime()}.txt"
    }

    @SuppressLint("InlinedApi")
    protected fun launchSetDefaultDialerIntent() {
        if (IsQPlusUseCase()) {
            val roleManager = getSystemService(RoleManager::class.java)
            if (roleManager!!.isRoleAvailable(RoleManager.ROLE_DIALER) && !roleManager.isRoleHeld(
                    RoleManager.ROLE_DIALER
                )
            ) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
                startActivityForResult(intent, REQUEST_CODE_SET_DEFAULT_DIALER)
            }
        } else {
            Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).putExtra(
                TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME,
                packageName
            ).apply {
                try {
                    startActivityForResult(this, REQUEST_CODE_SET_DEFAULT_DIALER)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(
                        this@BaseSimpleActivity,
                        R.string.no_app_found,
                        Toast.LENGTH_LONG
                    ).show()
                } catch (e: Exception) {
                    Toast.makeText(this@BaseSimpleActivity, e.toString(), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun setupToolbar(
        toolbar: MaterialToolbar,
        toolbarNavigationIcon: NavigationIcon = NavigationIcon.None,
        statusBarColor: Int = getProperStatusBarColor(),
        searchMenuItem: MenuItem? = null
    ) {
        val contrastColor = statusBarColor.getContrastColor()
        toolbar.setBackgroundColor(statusBarColor)
        toolbar.setTitleTextColor(contrastColor)
        toolbar.overflowIcon =
            resources.getColoredDrawableWithColor(R.drawable.ic_three_dots_vector, contrastColor)

        if (toolbarNavigationIcon != NavigationIcon.None) {
            val drawableId =
                if (toolbarNavigationIcon == NavigationIcon.Cross) R.drawable.ic_cross_vector else R.drawable.ic_arrow_left_vector
            toolbar.navigationIcon =
                resources.getColoredDrawableWithColor(drawableId, contrastColor)
        }

        updateMenuItemColors(
            toolbar.menu,
            toolbarNavigationIcon == NavigationIcon.Cross,
            statusBarColor
        )
        toolbar.setNavigationOnClickListener {
            HideKeyboardUseCase(this)
            finish()
        }

        // this icon is used at closing search
        toolbar.collapseIcon =
            resources.getColoredDrawableWithColor(R.drawable.ic_arrow_left_vector, contrastColor)

        searchMenuItem?.actionView?.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
            ?.apply {
                ApplyColorFilterUseCase(this, contrastColor)
            }

        searchMenuItem?.actionView?.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
            ?.apply {
                setTextColor(contrastColor)
                setHintTextColor(contrastColor.adjustAlpha(AlphaLevel.Medium.amount))
                hint = "${getString(R.string.search)}…"

                if (IsQPlusUseCase()) {
                    textCursorDrawable = null
                }
            }

        // search underline
        searchMenuItem?.actionView?.findViewById<View>(androidx.appcompat.R.id.search_plate)
            ?.apply {
                background.setColorFilter(contrastColor, PorterDuff.Mode.MULTIPLY)
            }
    }

    private fun getAvailableStorageB(path: String): Long {
        return try {
            val stat = StatFs(path)
            val bytesAvailable = stat.blockSizeLong * stat.availableBlocksLong
            bytesAvailable
        } catch (e: Exception) {
            -1L
        }
    }

    // TODO: how to do "bits & ~bit" in kotlin?
    private fun removeBit(inputBit: Int, bit: Int) = addBit(inputBit, bit) - bit

    private fun addBit(inputBit: Int, bit: Int) = inputBit or bit

    private fun isShowingOTGDialog(path: String): Boolean {
        return if (
            !IsRPlusUseCase() &&
            IsPathOnOtgUseCase(this, path) &&
            (baseConfig.otgTreeUri.isEmpty() || !hasProperStoredTreeUri(true))
        ) {
            showOTGPermissionDialog(path)
            true
        } else {
            false
        }
    }

    private fun getProperStatusBarColor() = when {
        baseConfig.isUsingSystemTheme -> resources.getColor(R.color.you_status_bar_color, theme)
        else -> baseConfig.primaryColor
    }

}
