package ca.on.sudbury.hojat.smartgallery.photoedit

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.Color
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.view.View
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.exifinterface.media.ExifInterface
import androidx.recyclerview.widget.LinearLayoutManager
import ca.on.hojat.renderer.cropper.CropImageView
import ca.on.sudbury.hojat.smartgallery.BuildConfig
import ca.on.sudbury.hojat.smartgallery.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import ca.on.sudbury.hojat.smartgallery.extensions.onGlobalLayout
import ca.on.sudbury.hojat.smartgallery.extensions.getFileOutputStream
import ca.on.sudbury.hojat.smartgallery.extensions.getFilenameFromContentUri
import ca.on.sudbury.hojat.smartgallery.extensions.sharePathIntent
import ca.on.sudbury.hojat.smartgallery.extensions.getProperPrimaryColor
import ca.on.sudbury.hojat.smartgallery.extensions.getParentPath
import ca.on.sudbury.hojat.smartgallery.extensions.internalStoragePath
import ca.on.sudbury.hojat.smartgallery.extensions.getCurrentFormattedDateTime
import ca.on.sudbury.hojat.smartgallery.extensions.baseConfig
import ca.on.sudbury.hojat.smartgallery.extensions.launchViewIntent
import ca.on.sudbury.hojat.smartgallery.extensions.getFilenameFromPath
import ca.on.sudbury.hojat.smartgallery.extensions.getCompressionFormat
import ca.on.sudbury.hojat.smartgallery.extensions.rescanPaths
import ca.on.sudbury.hojat.smartgallery.extensions.getRealPathFromURI
import ca.on.sudbury.hojat.smartgallery.extensions.checkAppSideloading
import ca.on.sudbury.hojat.smartgallery.helpers.PERMISSION_WRITE_STORAGE
import ca.on.sudbury.hojat.smartgallery.helpers.NavigationIcon
import ca.on.sudbury.hojat.smartgallery.helpers.REAL_FILE_PATH
import ca.on.sudbury.hojat.smartgallery.models.FileDirItem
import ca.on.sudbury.hojat.smartgallery.adapters.FiltersAdapter
import ca.on.sudbury.hojat.smartgallery.base.SimpleActivity
import ca.on.sudbury.hojat.smartgallery.databinding.ActivityEditBinding
import ca.on.sudbury.hojat.smartgallery.dialogs.ColorPickerDialogFragment
import ca.on.sudbury.hojat.smartgallery.dialogs.ConfirmationDialogFragment
import ca.on.sudbury.hojat.smartgallery.dialogs.OtherAspectRatioDialogFragment
import ca.on.sudbury.hojat.smartgallery.dialogs.ResizeDialogFragment
import ca.on.sudbury.hojat.smartgallery.dialogs.SaveAsDialogFragment
import ca.on.sudbury.hojat.smartgallery.extensions.config
import ca.on.sudbury.hojat.smartgallery.extensions.fixDateTaken
import ca.on.sudbury.hojat.smartgallery.extensions.openEditor
import ca.on.sudbury.hojat.smartgallery.helpers.AspectRatio
import ca.on.sudbury.hojat.smartgallery.helpers.FilterThumbnailsManager
import ca.on.sudbury.hojat.smartgallery.models.FilterItem
import ca.on.sudbury.hojat.smartgallery.usecases.IsNougatPlusUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.ApplyColorFilterUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.BeVisibleOrGoneUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.CopyNonDimensionExifAttributesUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.GetFileExtensionUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.IsPathOnOtgUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.RunOnBackgroundThreadUseCase
import com.zomato.photofilters.FilterPack
import com.zomato.photofilters.imageprocessors.Filter
import java.io.File
import java.io.InputStream
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.ByteArrayInputStream

private const val TEMP_FOLDER_NAME = "images"
private const val ASPECT_X = "aspectX"
private const val ASPECT_Y = "aspectY"
private const val CROP = "crop"

// constants for bottom primary action groups
private const val PRIMARY_ACTION_NONE = 0
private const val PRIMARY_ACTION_FILTER = 1
private const val PRIMARY_ACTION_CROP_ROTATE = 2
private const val PRIMARY_ACTION_DRAW = 3
private const val CROP_ROTATE_NONE = 0
private const val CROP_ROTATE_ASPECT_RATIO = 1

class EditActivity : SimpleActivity(), CropImageView.OnCropImageCompleteListener {

    private lateinit var binding: ActivityEditBinding

    companion object {
        init {
            System.loadLibrary("NativeImageProcessor")
        }
    }


    private lateinit var saveUri: Uri
    private var uri: Uri? = null
    private var resizeWidth = 0
    private var resizeHeight = 0
    private var drawColor = 0
    private var lastOtherAspectRatio: Pair<Float, Float>? = null
    private var currPrimaryAction = PRIMARY_ACTION_NONE
    private var currCropRotateAction = CROP_ROTATE_ASPECT_RATIO
    private var currAspectRatio = AspectRatio.Free.id
    private var isCropIntent = false
    private var isEditingWithThirdParty = false
    private var isSharingBitmap = false
    private var wasDrawCanvasPositioned = false
    private var oldExif: ExifInterface? = null
    private var filterInitialBitmap: Bitmap? = null
    private var originalUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (checkAppSideloading()) {
            return
        }

        setupOptionsMenu()
        handlePermission(PERMISSION_WRITE_STORAGE) {
            if (!it) {
                Toast.makeText(this, R.string.no_storage_permissions, Toast.LENGTH_LONG).show()
                finish()
            }
            initEditActivity()
        }
    }

    override fun onResume() {
        super.onResume()
        isEditingWithThirdParty = false
        binding.bottomEditorDrawActions.bottomDrawWidth.setColors(
            getProperPrimaryColor()
        )
        setupToolbar(binding.editorToolbar, NavigationIcon.Arrow)
    }

    override fun onStop() {
        super.onStop()
        if (isEditingWithThirdParty) {
            finish()
        }
    }

    private fun setupOptionsMenu() {
        binding.editorToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.save_as -> saveImage()
                R.id.edit -> editWith()
                R.id.share -> shareImage()
                else -> return@setOnMenuItemClickListener false
            }
            return@setOnMenuItemClickListener true
        }
    }

    private fun initEditActivity() {
        if (intent.data == null) {
            Toast.makeText(this, R.string.invalid_image_path, Toast.LENGTH_LONG).show()
            finish()
            return
        }

        uri = intent.data!!
        originalUri = uri
        if (uri!!.scheme != "file" && uri!!.scheme != "content") {
            Toast.makeText(this, R.string.unknown_file_location, Toast.LENGTH_LONG).show()
            finish()
            return
        }

        if (intent.extras?.containsKey(REAL_FILE_PATH) == true) {
            val realPath = intent.extras!!.getString(REAL_FILE_PATH)
            uri = when {
                IsPathOnOtgUseCase(this, realPath!!) -> uri
                realPath.startsWith("file:/") -> Uri.parse(realPath)
                else -> Uri.fromFile(File(realPath))
            }
        } else {
            (getRealPathFromURI(uri!!))?.apply {
                uri = Uri.fromFile(File(this))
            }
        }

        saveUri = when {
            intent.extras?.containsKey(MediaStore.EXTRA_OUTPUT) == true && intent.extras!!.get(
                MediaStore.EXTRA_OUTPUT
            ) is Uri -> intent.extras!!.get(MediaStore.EXTRA_OUTPUT) as Uri
            else -> uri!!
        }

        isCropIntent = intent.extras?.get(CROP) == "true"
        if (isCropIntent) {
            binding.bottomEditorPrimaryActions.root.visibility = View.GONE
            (binding.bottomEditorCropRotateActions.root.layoutParams as RelativeLayout.LayoutParams).addRule(
                RelativeLayout.ALIGN_PARENT_BOTTOM,
                1
            )
        }

        loadDefaultImageView()
        setupBottomActions()

        if (config.lastEditorCropAspectRatio == AspectRatio.Other.id) {
            if (config.lastEditorCropOtherAspectRatioX == 0f) {
                config.lastEditorCropOtherAspectRatioX = 1f
            }

            if (config.lastEditorCropOtherAspectRatioY == 0f) {
                config.lastEditorCropOtherAspectRatioY = 1f
            }

            lastOtherAspectRatio =
                Pair(config.lastEditorCropOtherAspectRatioX, config.lastEditorCropOtherAspectRatioY)
        }
        updateAspectRatio(config.lastEditorCropAspectRatio)
        binding.cropImageView.guidelines = CropImageView.Guidelines.ON
        binding.bottomAspectRatios.root.visibility = View.VISIBLE
    }

    private fun loadDefaultImageView() {
        binding.defaultImageView.visibility = View.VISIBLE
        binding.cropImageView.visibility = View.GONE
        binding.editorDrawCanvas.visibility = View.GONE

        val options = RequestOptions()
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)

        Glide.with(this)
            .asBitmap()
            .load(uri)
            .apply(options)
            .listener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    isFirstResource: Boolean
                ): Boolean {
                    if (uri != originalUri) {
                        uri = originalUri
                        Handler().post {
                            loadDefaultImageView()
                        }
                    }
                    return false
                }

                override fun onResourceReady(
                    bitmap: Bitmap?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    val currentFilter = getFiltersAdapter()?.getCurrentFilter()
                    if (filterInitialBitmap == null) {
                        loadCropImageView()
                        bottomCropRotateClicked()
                    }

                    if (filterInitialBitmap != null && currentFilter != null && currentFilter.filter.name != getString(
                            R.string.none
                        )
                    ) {
                        binding.defaultImageView.onGlobalLayout {
                            applyFilter(currentFilter)
                        }
                    } else {
                        filterInitialBitmap = bitmap
                    }

                    if (isCropIntent) {
                        binding.bottomEditorPrimaryActions.bottomPrimaryFilter.visibility =
                            View.GONE
                        binding.bottomEditorPrimaryActions.bottomPrimaryDraw.visibility = View.GONE
                    }

                    return false
                }
            }).into(binding.defaultImageView)
    }

    private fun loadCropImageView() {
        binding.defaultImageView.visibility = View.GONE
        binding.editorDrawCanvas.visibility = View.GONE
        binding.cropImageView.apply {
            visibility = View.VISIBLE
            setOnCropImageCompleteListener(this@EditActivity)
            setImageUriAsync(uri)
            guidelines = CropImageView.Guidelines.ON

            if (isCropIntent && shouldCropSquare()) {
                currAspectRatio = AspectRatio.OneOne.id
                setFixedAspectRatio(true)
                binding.bottomEditorCropRotateActions.bottomAspectRatio.visibility = View.GONE
            }
        }
    }

    private fun loadDrawCanvas() {
        binding.defaultImageView.visibility = View.GONE
        binding.cropImageView.visibility = View.GONE
        binding.editorDrawCanvas.visibility = View.VISIBLE

        if (!wasDrawCanvasPositioned) {
            wasDrawCanvasPositioned = true
            binding.editorDrawCanvas.onGlobalLayout {
                RunOnBackgroundThreadUseCase {
                    fillCanvasBackground()
                }
            }
        }
    }

    private fun fillCanvasBackground() {
        val size = Point()
        windowManager.defaultDisplay.getSize(size)
        val options = RequestOptions()
            .format(DecodeFormat.PREFER_ARGB_8888)
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .fitCenter()

        try {
            val builder = Glide.with(applicationContext)
                .asBitmap()
                .load(uri)
                .apply(options)
                .into(binding.editorDrawCanvas.width, binding.editorDrawCanvas.height)

            val bitmap = builder.get()
            runOnUiThread {
                binding.editorDrawCanvas.apply {
                    updateBackgroundBitmap(bitmap)
                    layoutParams.width = bitmap.width
                    layoutParams.height = bitmap.height
                    y = (height - bitmap.height) / 2f
                    requestLayout()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    private fun saveImage() {
        setOldExif()

        if (binding.cropImageView.visibility == View.VISIBLE) {
            binding.cropImageView.getCroppedImageAsync()
        } else if (binding.editorDrawCanvas.visibility == View.VISIBLE) {
            val bitmap = binding.editorDrawCanvas.getBitmap()
            if (saveUri.scheme == "file") {
                val callback: (String) -> Unit = { savePath ->
                    saveBitmapToFile(bitmap, savePath, true)
                }
                SaveAsDialogFragment(
                    path = saveUri.path!!,
                    appendFilename = true,
                    callback = callback
                ).show(supportFragmentManager, SaveAsDialogFragment.TAG)
            } else if (saveUri.scheme == "content") {
                val filePathGetter = getNewFilePath()
                val callback: (savePath: String) -> Unit = { saveBitmapToFile(bitmap, it, true) }
                SaveAsDialogFragment(
                    path = filePathGetter.first,
                    appendFilename = filePathGetter.second,
                    callback = callback
                ).show(supportFragmentManager, SaveAsDialogFragment.TAG)
            }
        } else {
            val currentFilter = getFiltersAdapter()?.getCurrentFilter() ?: return
            val filePathGetter = getNewFilePath()
            val callback: (String) -> Unit = { savePath ->
                Toast.makeText(this, R.string.saving, Toast.LENGTH_LONG).show()
                // clean up everything to free as much memory as possible
                binding.defaultImageView.setImageResource(0)
                binding.cropImageView.setImageBitmap(null)
                binding.bottomEditorFilterActions.bottomActionsFilterList.adapter = null
                binding.bottomEditorFilterActions.bottomActionsFilterList.visibility = View.GONE

                RunOnBackgroundThreadUseCase {
                    try {
                        val originalBitmap = Glide.with(applicationContext).asBitmap().load(uri)
                            .submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).get()
                        currentFilter.filter.processFilter(originalBitmap)
                        saveBitmapToFile(originalBitmap, savePath, false)
                    } catch (e: OutOfMemoryError) {
                        Toast.makeText(this, R.string.out_of_memory_error, Toast.LENGTH_LONG).show()
                    }
                }
            }
            SaveAsDialogFragment(
                path = filePathGetter.first,
                appendFilename = filePathGetter.second,
                callback = callback
            ).show(supportFragmentManager, SaveAsDialogFragment.TAG)
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    private fun setOldExif() {
        var inputStream: InputStream? = null
        try {
            if (IsNougatPlusUseCase()) {
                inputStream = contentResolver.openInputStream(uri!!)
                oldExif = ExifInterface(inputStream!!)
            }
        } catch (_: Exception) {
        } finally {
            inputStream?.close()
        }
    }

    private fun shareImage() {
        RunOnBackgroundThreadUseCase {

            when {
                binding.defaultImageView.visibility == View.VISIBLE -> {
                    val currentFilter = getFiltersAdapter()?.getCurrentFilter()
                    if (currentFilter == null) {
                        Toast.makeText(this, R.string.unknown_error_occurred, Toast.LENGTH_LONG)
                            .show()
                        return@RunOnBackgroundThreadUseCase
                    }

                    val originalBitmap = Glide.with(applicationContext).asBitmap().load(uri)
                        .submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).get()
                    currentFilter.filter.processFilter(originalBitmap)
                    shareBitmap(originalBitmap)
                }
                binding.cropImageView.visibility == View.VISIBLE -> {
                    isSharingBitmap = true
                    runOnUiThread {
                        binding.cropImageView.getCroppedImageAsync()
                    }
                }
                binding.editorDrawCanvas.visibility == View.VISIBLE -> shareBitmap(binding.editorDrawCanvas.getBitmap())
            }
        }
    }

    private fun getTempImagePath(bitmap: Bitmap, callback: (path: String?) -> Unit) {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(CompressFormat.PNG, 0, bytes)

        val folder = File(cacheDir, TEMP_FOLDER_NAME)
        if (!folder.exists()) {
            if (!folder.mkdir()) {
                callback(null)
                return
            }
        }

        val filename = applicationContext.getFilenameFromContentUri(saveUri) ?: "tmp.jpg"
        val newPath = "$folder/$filename"
        val fileDirItem = FileDirItem(newPath, filename)
        getFileOutputStream(fileDirItem, true) {
            if (it != null) {
                try {
                    it.write(bytes.toByteArray())
                    callback(newPath)
                } catch (_: Exception) {
                } finally {
                    it.close()
                }
            } else {
                callback("")
            }
        }
    }

    private fun shareBitmap(bitmap: Bitmap) {
        getTempImagePath(bitmap) {
            if (it != null) {
                sharePathIntent(it, BuildConfig.APPLICATION_ID)
            } else {
                Toast.makeText(this, R.string.unknown_error_occurred, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getFiltersAdapter() =
        binding.bottomEditorFilterActions.bottomActionsFilterList.adapter as? FiltersAdapter

    private fun setupBottomActions() {
        setupPrimaryActionButtons()
        setupCropRotateActionButtons()
        setupAspectRatioButtons()
        setupDrawButtons()
    }

    private fun setupPrimaryActionButtons() {
        binding.bottomEditorPrimaryActions.bottomPrimaryFilter.setOnClickListener {
            bottomFilterClicked()
        }

        binding.bottomEditorPrimaryActions.bottomPrimaryCropRotate.setOnClickListener {
            bottomCropRotateClicked()
        }

        binding.bottomEditorPrimaryActions.bottomPrimaryDraw.setOnClickListener {
            bottomDrawClicked()
        }
    }

    private fun bottomFilterClicked() {
        currPrimaryAction = if (currPrimaryAction == PRIMARY_ACTION_FILTER) {
            PRIMARY_ACTION_NONE
        } else {
            PRIMARY_ACTION_FILTER
        }
        updatePrimaryActionButtons()
    }

    private fun bottomCropRotateClicked() {
        currPrimaryAction = if (currPrimaryAction == PRIMARY_ACTION_CROP_ROTATE) {
            PRIMARY_ACTION_NONE
        } else {
            PRIMARY_ACTION_CROP_ROTATE
        }
        updatePrimaryActionButtons()
    }

    private fun bottomDrawClicked() {
        currPrimaryAction = if (currPrimaryAction == PRIMARY_ACTION_DRAW) {
            PRIMARY_ACTION_NONE
        } else {
            PRIMARY_ACTION_DRAW
        }
        updatePrimaryActionButtons()
    }

    private fun setupCropRotateActionButtons() {
        binding.bottomEditorCropRotateActions.bottomRotate.setOnClickListener {
            binding.cropImageView.rotateImage(90)
        }

        BeVisibleOrGoneUseCase(binding.bottomEditorCropRotateActions.bottomResize, !isCropIntent)
        binding.bottomEditorCropRotateActions.bottomResize.setOnClickListener {
            resizeImage()
        }

        binding.bottomEditorCropRotateActions.bottomFlipHorizontally.setOnClickListener {
            binding.cropImageView.flipImageHorizontally()
        }

        binding.bottomEditorCropRotateActions.bottomFlipVertically.setOnClickListener {
            binding.cropImageView.flipImageVertically()
        }

        binding.bottomEditorCropRotateActions.bottomAspectRatio.setOnClickListener {
            currCropRotateAction = if (currCropRotateAction == CROP_ROTATE_ASPECT_RATIO) {
                binding.cropImageView.guidelines = CropImageView.Guidelines.OFF
                binding.bottomAspectRatios.root.visibility = View.GONE
                CROP_ROTATE_NONE
            } else {
                binding.cropImageView.guidelines = CropImageView.Guidelines.ON
                binding.bottomAspectRatios.root.visibility = View.VISIBLE
                CROP_ROTATE_ASPECT_RATIO
            }
            updateCropRotateActionButtons()
        }
    }

    private fun setupAspectRatioButtons() {
        binding.bottomAspectRatios.bottomAspectRatioFree.setOnClickListener {
            updateAspectRatio(AspectRatio.Free.id)
        }

        binding.bottomAspectRatios.bottomAspectRatioOneOne.setOnClickListener {
            updateAspectRatio(AspectRatio.OneOne.id)
        }

        binding.bottomAspectRatios.bottomAspectRatioFourThree.setOnClickListener {
            updateAspectRatio(AspectRatio.FourThree.id)
        }

        binding.bottomAspectRatios.bottomAspectRatioSixteenNine.setOnClickListener {
            updateAspectRatio(AspectRatio.SixteenNine.id)
        }

        binding.bottomAspectRatios.bottomAspectRatioOther.setOnClickListener {
            val callbackAfterRatioPicked: (Pair<Float, Float>) -> Unit = { aspectRatioPair ->
                lastOtherAspectRatio = aspectRatioPair
                config.lastEditorCropOtherAspectRatioX = aspectRatioPair.first
                config.lastEditorCropOtherAspectRatioY = aspectRatioPair.second
                updateAspectRatio(AspectRatio.Other.id)
            }
            OtherAspectRatioDialogFragment(lastOtherAspectRatio, callbackAfterRatioPicked).show(
                supportFragmentManager, OtherAspectRatioDialogFragment.TAG
            )
        }

        updateAspectRatioButtons()
    }

    private fun setupDrawButtons() {
        updateDrawColor(config.lastEditorDrawColor)
        binding.bottomEditorDrawActions.bottomDrawWidth.progress = config.lastEditorBrushSize
        updateBrushSize(config.lastEditorBrushSize)

        binding.bottomEditorDrawActions.bottomDrawColorClickable.setOnClickListener {
            val callback: (wasPositivePressed: Boolean, color: Int) -> Unit =
                { wasPositivePressed, color ->
                    if (wasPositivePressed) {
                        updateDrawColor(color)
                    }
                }
            ColorPickerDialogFragment(color = drawColor, callback = callback).show(
                supportFragmentManager,
                ColorPickerDialogFragment.TAG
            )
        }

        binding.bottomEditorDrawActions.bottomDrawWidth.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                config.lastEditorBrushSize = progress
                updateBrushSize(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.bottomEditorDrawActions.bottomDrawUndo.setOnClickListener {
            binding.editorDrawCanvas.undo()
        }
    }

    private fun updateBrushSize(percent: Int) {
        binding.editorDrawCanvas.updateBrushSize(percent)
        val scale = 0.03f.coerceAtLeast(percent / 100f)
        binding.bottomEditorDrawActions.bottomDrawColor.scaleX = scale
        binding.bottomEditorDrawActions.bottomDrawColor.scaleY = scale
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updatePrimaryActionButtons() {
        if (binding.cropImageView.visibility == View.GONE && currPrimaryAction == PRIMARY_ACTION_CROP_ROTATE) {
            loadCropImageView()
        } else if (binding.defaultImageView.visibility == View.GONE && currPrimaryAction == PRIMARY_ACTION_FILTER) {
            loadDefaultImageView()
        } else if (binding.editorDrawCanvas.visibility == View.GONE && currPrimaryAction == PRIMARY_ACTION_DRAW) {
            loadDrawCanvas()
        }

        arrayOf(
            binding.bottomEditorPrimaryActions.bottomPrimaryFilter,
            binding.bottomEditorPrimaryActions.bottomPrimaryCropRotate,
            binding.bottomEditorPrimaryActions.bottomPrimaryDraw
        ).forEach { imageView ->
            ApplyColorFilterUseCase(imageView, Color.WHITE)
        }

        val currentPrimaryActionButton = when (currPrimaryAction) {
            PRIMARY_ACTION_FILTER -> binding.bottomEditorPrimaryActions.bottomPrimaryFilter
            PRIMARY_ACTION_CROP_ROTATE -> binding.bottomEditorPrimaryActions.bottomPrimaryCropRotate
            PRIMARY_ACTION_DRAW -> binding.bottomEditorPrimaryActions.bottomPrimaryDraw
            else -> null
        }

        ApplyColorFilterUseCase(currentPrimaryActionButton, getProperPrimaryColor())

        BeVisibleOrGoneUseCase(
            binding.bottomEditorFilterActions.root,
            currPrimaryAction == PRIMARY_ACTION_FILTER
        )
        BeVisibleOrGoneUseCase(
            binding.bottomEditorCropRotateActions.root,
            currPrimaryAction == PRIMARY_ACTION_CROP_ROTATE
        )
        BeVisibleOrGoneUseCase(
            binding.bottomEditorDrawActions.root,
            currPrimaryAction == PRIMARY_ACTION_DRAW
        )

        if (currPrimaryAction == PRIMARY_ACTION_FILTER && binding.bottomEditorFilterActions.bottomActionsFilterList.adapter == null) {
            RunOnBackgroundThreadUseCase {

                val thumbnailSize =
                    resources.getDimension(R.dimen.bottom_filters_thumbnail_size).toInt()

                val bitmap = try {
                    Glide.with(this)
                        .asBitmap()
                        .load(uri).listener(object : RequestListener<Bitmap> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Bitmap>?,
                                isFirstResource: Boolean
                            ): Boolean {
                                Toast.makeText(this@EditActivity, e.toString(), Toast.LENGTH_LONG)
                                    .show()
                                return false
                            }

                            override fun onResourceReady(
                                resource: Bitmap?,
                                model: Any?,
                                target: Target<Bitmap>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                            ) = false
                        })
                        .submit(thumbnailSize, thumbnailSize)
                        .get()
                } catch (e: GlideException) {
                    Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
                    finish()
                    return@RunOnBackgroundThreadUseCase
                }

                runOnUiThread {
                    val filterThumbnailsManager = FilterThumbnailsManager()
                    filterThumbnailsManager.clearThumbs()

                    val noFilter = Filter(getString(R.string.none))
                    filterThumbnailsManager.addThumb(FilterItem(bitmap, noFilter))

                    FilterPack.getFilterPack(this).forEach {
                        val filterItem = FilterItem(bitmap, it)
                        filterThumbnailsManager.addThumb(filterItem)
                    }

                    val filterItems = filterThumbnailsManager.processThumbs()
                    val adapter = FiltersAdapter(applicationContext, filterItems) {
                        val layoutManager =
                            binding.bottomEditorFilterActions.bottomActionsFilterList.layoutManager as LinearLayoutManager
                        applyFilter(filterItems[it])

                        if (it == layoutManager.findLastCompletelyVisibleItemPosition() || it == layoutManager.findLastVisibleItemPosition()) {
                            binding.bottomEditorFilterActions.bottomActionsFilterList.smoothScrollBy(
                                thumbnailSize,
                                0
                            )
                        } else if (it == layoutManager.findFirstCompletelyVisibleItemPosition() || it == layoutManager.findFirstVisibleItemPosition()) {
                            binding.bottomEditorFilterActions.bottomActionsFilterList.smoothScrollBy(
                                -thumbnailSize,
                                0
                            )
                        }
                    }

                    binding.bottomEditorFilterActions.bottomActionsFilterList.adapter = adapter
                    adapter.notifyDataSetChanged()
                }
            }
        }

        if (currPrimaryAction != PRIMARY_ACTION_CROP_ROTATE) {
            binding.bottomAspectRatios.root.visibility = View.GONE
            currCropRotateAction = CROP_ROTATE_NONE
        }
        updateCropRotateActionButtons()
    }

    private fun applyFilter(filterItem: FilterItem) {
        val newBitmap = Bitmap.createBitmap(filterInitialBitmap!!)
        binding.defaultImageView.setImageBitmap(filterItem.filter.processFilter(newBitmap))
    }

    private fun updateAspectRatio(aspectRatio: Int) {
        currAspectRatio = aspectRatio
        config.lastEditorCropAspectRatio = aspectRatio
        updateAspectRatioButtons()

        binding.cropImageView.apply {
            if (aspectRatio == AspectRatio.Free.id) {
                setFixedAspectRatio(false)
            } else {
                val newAspectRatio = when (aspectRatio) {
                    AspectRatio.OneOne.id -> Pair(1f, 1f)
                    AspectRatio.FourThree.id -> Pair(4f, 3f)
                    AspectRatio.SixteenNine.id -> Pair(16f, 9f)
                    else -> Pair(lastOtherAspectRatio!!.first, lastOtherAspectRatio!!.second)
                }

                setAspectRatio(newAspectRatio.first.toInt(), newAspectRatio.second.toInt())
            }
        }
    }

    private fun updateAspectRatioButtons() {
        arrayOf(
            binding.bottomAspectRatios.bottomAspectRatioFree,
            binding.bottomAspectRatios.bottomAspectRatioOneOne,
            binding.bottomAspectRatios.bottomAspectRatioFourThree,
            binding.bottomAspectRatios.bottomAspectRatioSixteenNine,
            binding.bottomAspectRatios.bottomAspectRatioOther
        ).forEach {
            it.setTextColor(Color.WHITE)
        }

        val currentAspectRatioButton = when (currAspectRatio) {
            AspectRatio.Free.id -> binding.bottomAspectRatios.bottomAspectRatioFree
            AspectRatio.OneOne.id -> binding.bottomAspectRatios.bottomAspectRatioOneOne
            AspectRatio.FourThree.id -> binding.bottomAspectRatios.bottomAspectRatioFourThree
            AspectRatio.SixteenNine.id -> binding.bottomAspectRatios.bottomAspectRatioSixteenNine
            else -> binding.bottomAspectRatios.bottomAspectRatioOther
        }

        currentAspectRatioButton.setTextColor(getProperPrimaryColor())
    }

    private fun updateCropRotateActionButtons() {
        arrayOf(binding.bottomEditorCropRotateActions.bottomAspectRatio).forEach { imageView ->
            ApplyColorFilterUseCase(imageView, Color.WHITE)
        }

        val primaryActionView = when (currCropRotateAction) {
            CROP_ROTATE_ASPECT_RATIO -> binding.bottomEditorCropRotateActions.bottomAspectRatio
            else -> null
        }

        ApplyColorFilterUseCase(primaryActionView, getProperPrimaryColor())
    }

    private fun updateDrawColor(color: Int) {
        drawColor = color
        ApplyColorFilterUseCase(binding.bottomEditorDrawActions.bottomDrawColor, color)
        config.lastEditorDrawColor = color
        binding.editorDrawCanvas.updateColor(color)
    }

    private fun resizeImage() {
        val point = getAreaSize()
        if (point == null) {
            Toast.makeText(this, R.string.unknown_error_occurred, Toast.LENGTH_LONG).show()
            return
        }
        val callback: (Point) -> Unit = { newSize ->
            resizeWidth = newSize.x
            resizeHeight = newSize.y
            binding.cropImageView.getCroppedImageAsync()
        }
        ResizeDialogFragment(point, callback).show(supportFragmentManager, ResizeDialogFragment.TAG)
    }

    private fun shouldCropSquare(): Boolean {
        val extras = intent.extras
        return if (extras != null && extras.containsKey(ASPECT_X) && extras.containsKey(ASPECT_Y)) {
            extras.getInt(ASPECT_X) == extras.getInt(ASPECT_Y)
        } else {
            false
        }
    }

    private fun getAreaSize(): Point? {
        val rect = binding.cropImageView.cropRect ?: return null
        val rotation = binding.cropImageView.rotatedDegrees
        return if (rotation == 0 || rotation == 180) {
            Point(rect.width(), rect.height())
        } else {
            Point(rect.height(), rect.width())
        }
    }

    override fun onCropImageComplete(view: CropImageView, result: CropImageView.CropResult) {
        if (result.error == null) {
            setOldExif()

            val bitmap = result.bitmap
            if (isSharingBitmap) {
                isSharingBitmap = false
                shareBitmap(bitmap)
                return
            }

            if (isCropIntent) {
                if (saveUri.scheme == "file") {
                    saveBitmapToFile(bitmap, saveUri.path!!, true)
                } else {
                    var inputStream: InputStream? = null
                    var outputStream: OutputStream? = null
                    try {
                        val stream = ByteArrayOutputStream()
                        bitmap.compress(CompressFormat.JPEG, 100, stream)
                        inputStream = ByteArrayInputStream(stream.toByteArray())
                        outputStream = contentResolver.openOutputStream(saveUri)
                        inputStream.copyTo(outputStream!!)
                    } catch (e: Exception) {
                        Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
                        return
                    } finally {
                        inputStream?.close()
                        outputStream?.close()
                    }

                    Intent().apply {
                        data = saveUri
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        setResult(RESULT_OK, this)
                    }
                    finish()
                }
            } else if (saveUri.scheme == "file") {
                val callback: (savePath: String) -> Unit = {
                    saveBitmapToFile(bitmap, it, true)
                }
                SaveAsDialogFragment(
                    path = saveUri.path!!,
                    appendFilename = true,
                    callback = callback
                ).show(supportFragmentManager, SaveAsDialogFragment.TAG)
            } else if (saveUri.scheme == "content") {
                val filePathGetter = getNewFilePath()
                val callback: (String) -> Unit = { savePath ->
                    saveBitmapToFile(bitmap, savePath, true)
                }
                SaveAsDialogFragment(
                    path = filePathGetter.first,
                    appendFilename = filePathGetter.second,
                    callback = callback
                ).show(supportFragmentManager, SaveAsDialogFragment.TAG)
            } else {
                Toast.makeText(this, R.string.unknown_file_location, Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(
                this,
                "${getString(R.string.image_editing_failed)}: ${result.error.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun getNewFilePath(): Pair<String, Boolean> {
        var newPath = applicationContext.getRealPathFromURI(saveUri) ?: ""
        if (newPath.startsWith("/mnt/")) {
            newPath = ""
        }

        var shouldAppendFilename = true
        if (newPath.isEmpty()) {
            val filename = applicationContext.getFilenameFromContentUri(saveUri) ?: ""
            if (filename.isNotEmpty()) {
                val path =
                    if (intent.extras?.containsKey(REAL_FILE_PATH) == true) intent.getStringExtra(
                        REAL_FILE_PATH
                    )?.getParentPath() else internalStoragePath
                newPath = "$path/$filename"
                shouldAppendFilename = false
            }
        }

        if (newPath.isEmpty()) {
            newPath = "$internalStoragePath/${getCurrentFormattedDateTime()}.${
                GetFileExtensionUseCase(saveUri.toString())
            }"
            shouldAppendFilename = false
        }

        return Pair(newPath, shouldAppendFilename)
    }

    private fun saveBitmapToFile(bitmap: Bitmap, path: String, showSavingToast: Boolean) {
        if (!packageName.contains("slootelibomelpmis".reversed(), true)) {
            if (baseConfig.appRunCount > 100) {
                val label =
                    "sknahT .moc.slootelibomelpmis.www morf eno lanigiro eht daolnwod ytefas nwo ruoy roF .ppa eht fo noisrev ekaf a gnisu era uoY".reversed()
                runOnUiThread {
                    val callback: () -> Unit = {
                        launchViewIntent("6629852208836920709=di?ved/sppa/erots/moc.elgoog.yalp//:sptth".reversed())
                    }
                    ConfirmationDialogFragment(
                        message = label,
                        positive = R.string.ok,
                        negative = 0,
                        callbackAfterDialogConfirmed = callback
                    ).show(supportFragmentManager, ConfirmationDialogFragment.TAG)
                }
                return
            }
        }

        try {
            RunOnBackgroundThreadUseCase {
                val file = File(path)
                val fileDirItem = FileDirItem(path, path.getFilenameFromPath())
                getFileOutputStream(fileDirItem, true) {
                    if (it != null) {
                        saveBitmap(file, bitmap, it, showSavingToast)
                    } else {
                        Toast.makeText(this, R.string.image_editing_failed, Toast.LENGTH_LONG)
                            .show()
                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
        } catch (e: OutOfMemoryError) {
            Toast.makeText(this, R.string.out_of_memory_error, Toast.LENGTH_LONG).show()
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    private fun saveBitmap(
        file: File,
        bitmap: Bitmap,
        out: OutputStream,
        showSavingToast: Boolean
    ) {
        if (showSavingToast) {
            runOnUiThread { Toast.makeText(this, R.string.saving, Toast.LENGTH_LONG).show() }
        }

        if (resizeWidth > 0 && resizeHeight > 0) {
            val resized = Bitmap.createScaledBitmap(bitmap, resizeWidth, resizeHeight, false)
            resized.compress(file.absolutePath.getCompressionFormat(), 90, out)
        } else {
            bitmap.compress(file.absolutePath.getCompressionFormat(), 90, out)
        }

        try {
            if (IsNougatPlusUseCase()) {
                val newExif = ExifInterface(file.absolutePath)

                oldExif?.let { CopyNonDimensionExifAttributesUseCase(it, newExif) }
            }
        } catch (_: Exception) {
        }

        setResult(RESULT_OK, intent)
        scanFinalPath(file.absolutePath)
        out.close()
    }

    private fun editWith() {
        openEditor(uri.toString(), true)
        isEditingWithThirdParty = true
    }

    private fun scanFinalPath(path: String) {
        val paths = arrayListOf(path)
        applicationContext.rescanPaths(paths) {
            fixDateTaken(paths, false)
            setResult(RESULT_OK, intent)
            Toast.makeText(this, R.string.file_saved, Toast.LENGTH_LONG).show()
            finish()
        }
    }
}
