package ca.on.sudbury.hojat.smartgallery.video

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Point
import android.graphics.SurfaceTexture
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.Surface
import android.view.WindowManager
import android.view.GestureDetector
import android.view.TextureView
import android.view.View
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.Toast
import ca.on.sudbury.hojat.smartgallery.BuildConfig
import ca.on.sudbury.hojat.smartgallery.R
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SeekParameters
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.ContentDataSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.video.VideoListener
import ca.on.sudbury.hojat.smartgallery.extensions.actionBarHeight
import ca.on.sudbury.hojat.smartgallery.extensions.statusBarHeight
import ca.on.sudbury.hojat.smartgallery.extensions.portrait
import ca.on.sudbury.hojat.smartgallery.extensions.navigationBarRight
import ca.on.sudbury.hojat.smartgallery.extensions.onGlobalLayout
import ca.on.sudbury.hojat.smartgallery.extensions.updateTextColors
import ca.on.sudbury.hojat.smartgallery.extensions.navigationBarHeight
import ca.on.sudbury.hojat.smartgallery.extensions.getFormattedDuration
import ca.on.sudbury.hojat.smartgallery.extensions.getColoredDrawableWithColor
import ca.on.sudbury.hojat.smartgallery.extensions.getFilenameFromUri
import ca.on.sudbury.hojat.smartgallery.base.SimpleActivity
import ca.on.sudbury.hojat.smartgallery.databinding.ActivityVideoPlayerBinding
import ca.on.sudbury.hojat.smartgallery.extensions.openPath
import ca.on.sudbury.hojat.smartgallery.extensions.config
import ca.on.sudbury.hojat.smartgallery.extensions.hasNavBar
import ca.on.sudbury.hojat.smartgallery.extensions.navigationBarSize
import ca.on.sudbury.hojat.smartgallery.extensions.sharePathIntent
import ca.on.sudbury.hojat.smartgallery.helpers.FAST_FORWARD_VIDEO_MS
import ca.on.sudbury.hojat.smartgallery.helpers.MAX_CLOSE_DOWN_GESTURE_DURATION
import ca.on.sudbury.hojat.smartgallery.helpers.GO_TO_PREV_ITEM
import ca.on.sudbury.hojat.smartgallery.helpers.GO_TO_NEXT_ITEM
import ca.on.sudbury.hojat.smartgallery.helpers.DRAG_THRESHOLD
import ca.on.sudbury.hojat.smartgallery.helpers.HIDE_SYSTEM_UI_DELAY
import ca.on.sudbury.hojat.smartgallery.helpers.RotationRule
import ca.on.sudbury.hojat.smartgallery.helpers.SHOW_NEXT_ITEM
import ca.on.sudbury.hojat.smartgallery.helpers.SHOW_PREV_ITEM
import ca.on.sudbury.hojat.smartgallery.usecases.BeVisibleOrGoneUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.HideSystemUiUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.RunOnBackgroundThreadUseCase
import ca.on.sudbury.hojat.smartgallery.usecases.ShowSystemUiUseCase
import kotlin.math.abs
import kotlin.math.roundToInt

open class VideoPlayerActivity : SimpleActivity(), SeekBar.OnSeekBarChangeListener,
    TextureView.SurfaceTextureListener {

    private lateinit var binding: ActivityVideoPlayerBinding

    private val PLAY_WHEN_READY_DRAG_DELAY = 100L

    private var mIsFullscreen = false
    private var mIsPlaying = false
    private var mWasVideoStarted = false
    private var mIsDragged = false
    private var mIsOrientationLocked = false
    private var mScreenWidth = 0
    private var mCurrTime = 0
    private var mDuration = 0
    private var mDragThreshold = 0f
    private var mTouchDownX = 0f
    private var mTouchDownY = 0f
    private var mTouchDownTime = 0L
    private var mProgressAtDown = 0L
    private var mCloseDownThreshold = 100f

    private var mUri: Uri? = null
    private var mExoPlayer: SimpleExoPlayer? = null
    private var mVideoSize = Point(0, 0)
    private var mTimerHandler = Handler()
    private var mPlayWhenReadyHandler = Handler()

    private var mIgnoreCloseDown = false

    public override fun onCreate(savedInstanceState: Bundle?) {
        showTransparentTop = true
        showTransparentNavigation = true

        super.onCreate(savedInstanceState)
        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupOptionsMenu()
        setupOrientation()
        checkNotchSupport()
        initPlayer()
    }

    override fun onResume() {
        super.onResume()
        binding.topShadow.layoutParams.height = statusBarHeight + actionBarHeight
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        if (config.blackBackground) {
            binding.videoPlayerHolder.background = ColorDrawable(Color.BLACK)
        }

        if (config.maxBrightness) {
            val attributes = window.attributes
            attributes.screenBrightness = 1f
            window.attributes = attributes
        }

        updateTextColors(binding.videoPlayerHolder)

        if (!portrait && navigationBarRight && (if (navigationBarRight) navigationBarSize.x else 0) > 0) {
            binding.videoToolbar.setPadding(
                0,
                0,
                if (navigationBarRight) navigationBarSize.x else 0,
                0
            )
        } else {
            binding.videoToolbar.setPadding(0, 0, 0, 0)
        }
    }

    override fun onPause() {
        super.onPause()
        pauseVideo()

        if (config.rememberLastVideoPosition && mWasVideoStarted) {
            saveVideoProgress()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isChangingConfigurations) {
            pauseVideo()
            binding.rlBottomVideoController.videoCurrTime.text = 0.getFormattedDuration()
            releaseExoPlayer()
            binding.rlBottomVideoController.videoSeekbar.progress = 0
            mTimerHandler.removeCallbacksAndMessages(null)
            mPlayWhenReadyHandler.removeCallbacksAndMessages(null)
        }
    }

    private fun setupOptionsMenu() {
        (binding.videoAppbar.layoutParams as RelativeLayout.LayoutParams).topMargin =
            statusBarHeight
        binding.videoToolbar.apply {
            setTitleTextColor(Color.WHITE)
            overflowIcon =
                resources.getColoredDrawableWithColor(R.drawable.ic_three_dots_vector, Color.WHITE)
            navigationIcon =
                resources.getColoredDrawableWithColor(R.drawable.ic_arrow_left_vector, Color.WHITE)
        }

        updateMenuItemColors(binding.videoToolbar.menu, forceWhiteIcons = true)
        binding.videoToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_change_orientation -> changeOrientation()
                R.id.menu_open_with -> openPath(mUri!!.toString(), true)
                R.id.menu_share -> sharePathIntent(mUri!!.toString(), BuildConfig.APPLICATION_ID)

                else -> return@setOnMenuItemClickListener false
            }
            return@setOnMenuItemClickListener true
        }

        binding.videoToolbar.setNavigationOnClickListener {
            finish()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setVideoSize()
        initTimeHolder()
        binding.videoSurfaceFrame.onGlobalLayout {
            binding.videoSurfaceFrame.controller.resetState()
        }

        binding.topShadow.layoutParams.height = statusBarHeight + actionBarHeight
        (binding.videoAppbar.layoutParams as RelativeLayout.LayoutParams).topMargin =
            statusBarHeight
        if (!portrait && navigationBarRight && (if (navigationBarRight) navigationBarSize.x else 0) > 0) {
            binding.videoToolbar.setPadding(
                0,
                0,
                if (navigationBarRight) navigationBarSize.x else 0,
                0
            )
        } else {
            binding.videoToolbar.setPadding(0, 0, 0, 0)
        }
    }

    private fun setupOrientation() {
        if (!mIsOrientationLocked) {
            if (config.screenRotation == RotationRule.DeviceRotation.id) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
            } else if (config.screenRotation == RotationRule.SystemSetting.id) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initPlayer() {
        mUri = intent.data ?: return
        binding.videoToolbar.title = getFilenameFromUri(mUri!!)
        initTimeHolder()

        ShowSystemUiUseCase(this)
        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            val isFullscreen = visibility and View.SYSTEM_UI_FLAG_FULLSCREEN != 0
            fullscreenToggled(isFullscreen)
        }

        binding.rlBottomVideoController.videoCurrTime.setOnClickListener { doSkip(false) }
        binding.rlBottomVideoController.videoDuration.setOnClickListener { doSkip(true) }
        binding.rlBottomVideoController.videoTogglePlayPause.setOnClickListener { togglePlayPause() }
        binding.videoSurfaceFrame.setOnClickListener { toggleFullscreen() }
        binding.videoSurfaceFrame.controller.settings.swallowDoubleTaps = true

        BeVisibleOrGoneUseCase(
            binding.rlBottomVideoController.videoNextFile, intent.getBooleanExtra(
                SHOW_NEXT_ITEM,
                false
            )
        )

        binding.rlBottomVideoController.videoNextFile.setOnClickListener { handleNextFile() }

        BeVisibleOrGoneUseCase(
            binding.rlBottomVideoController.videoPrevFile,
            intent.getBooleanExtra(
                SHOW_PREV_ITEM,
                false
            )
        )

        binding.rlBottomVideoController.videoPrevFile.setOnClickListener { handlePrevFile() }


        val gestureDetector =
            GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
                override fun onDoubleTap(e: MotionEvent): Boolean {
                    handleDoubleTap(e.rawX)
                    return true
                }
            })

        binding.videoSurfaceFrame.setOnTouchListener { _, event ->
            handleEvent(event)
            gestureDetector.onTouchEvent(event)
            false
        }

        initExoPlayer()
        binding.videoSurface.surfaceTextureListener = this

        if (config.allowVideoGestures) {
            binding.videoBrightnessController.initialize(
                this,
                binding.slideInfo,
                true,
                binding.videoPlayerHolder,
                singleTap = { _, _ ->
                    toggleFullscreen()
                },
                doubleTap = { _, _ ->
                    doSkip(false)
                })

            binding.videoVolumeController.initialize(
                this,
                binding.slideInfo,
                false,
                binding.videoPlayerHolder,
                singleTap = { _, _ ->
                    toggleFullscreen()
                },
                doubleTap = { _, _ ->
                    doSkip(true)
                })
        } else {
            binding.videoBrightnessController.visibility = View.GONE
            binding.videoVolumeController.visibility = View.GONE
        }

        if (config.hideSystemUI) {
            Handler().postDelayed({
                fullscreenToggled(true)
            }, HIDE_SYSTEM_UI_DELAY)
        }

        mDragThreshold = DRAG_THRESHOLD * resources.displayMetrics.density
    }

    private fun initExoPlayer() {
        val dataSpec = DataSpec(mUri)
        val fileDataSource = ContentDataSource(applicationContext)
        try {
            fileDataSource.open(dataSpec)
        } catch (e: Exception) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
        }

        val factory = DataSource.Factory { fileDataSource }
        val audioSource = ExtractorMediaSource(
            fileDataSource.uri,
            factory,
            DefaultExtractorsFactory(),
            null,
            null
        )
        mExoPlayer = ExoPlayerFactory.newSimpleInstance(applicationContext).apply {
            seekParameters = SeekParameters.CLOSEST_SYNC
            audioStreamType = C.STREAM_TYPE_MUSIC
            if (config.loopVideos) {
                repeatMode = Player.REPEAT_MODE_ONE
            }
            prepare(audioSource)
        }
        initExoPlayerListeners()
    }

    private fun initExoPlayerListeners() {
        mExoPlayer!!.addListener(object : Player.EventListener {
            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {}

            override fun onSeekProcessed() {}

            override fun onTracksChanged(
                trackGroups: TrackGroupArray?,
                trackSelections: TrackSelectionArray?
            ) {
            }

            override fun onPlayerError(error: ExoPlaybackException?) {}

            override fun onLoadingChanged(isLoading: Boolean) {}

            override fun onPositionDiscontinuity(reason: Int) {
                // Reset progress views when video loops.
                if (reason == Player.DISCONTINUITY_REASON_PERIOD_TRANSITION) {
                    binding.rlBottomVideoController.videoSeekbar.progress = 0
                    binding.rlBottomVideoController.videoCurrTime.text = 0.getFormattedDuration()
                }
            }

            override fun onRepeatModeChanged(repeatMode: Int) {}

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {}

            override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {}

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> videoPrepared()
                    Player.STATE_ENDED -> videoCompleted()
                }
            }
        })

        mExoPlayer!!.addVideoListener(object : VideoListener {
            override fun onVideoSizeChanged(
                width: Int,
                height: Int,
                unappliedRotationDegrees: Int,
                pixelWidthHeightRatio: Float
            ) {
                mVideoSize.x = width
                mVideoSize.y = height
                setVideoSize()
            }

            override fun onRenderedFirstFrame() {}
        })
    }

    private fun videoPrepared() {
        if (!mWasVideoStarted) {
            binding.rlBottomVideoController.videoTogglePlayPause.visibility = View.VISIBLE
            mDuration = (mExoPlayer!!.duration / 1000).toInt()
            binding.rlBottomVideoController.videoSeekbar.max = mDuration
            binding.rlBottomVideoController.videoDuration.text = mDuration.getFormattedDuration()
            setPosition(mCurrTime)

            if (config.rememberLastVideoPosition) {
                setLastVideoSavedPosition()
            }

            if (config.autoplayVideos) {
                resumeVideo()
            } else {
                binding.rlBottomVideoController.videoTogglePlayPause.setImageResource(R.drawable.ic_play_outline_vector)
            }
        }
    }

    private fun handleDoubleTap(x: Float) {
        val instantWidth = mScreenWidth / 7
        when {
            x <= instantWidth -> doSkip(false)
            x >= mScreenWidth - instantWidth -> doSkip(true)
            else -> togglePlayPause()
        }
    }

    private fun resumeVideo() {
        binding.rlBottomVideoController.videoTogglePlayPause.setImageResource(R.drawable.ic_pause_outline_vector)
        if (mExoPlayer == null) {
            return
        }

        val wasEnded = didVideoEnd()
        if (wasEnded) {
            setPosition(0)
        }

        mWasVideoStarted = true
        mIsPlaying = true
        mExoPlayer?.playWhenReady = true
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun pauseVideo() {
        binding.rlBottomVideoController.videoTogglePlayPause.setImageResource(R.drawable.ic_play_outline_vector)
        if (mExoPlayer == null) {
            return
        }

        mIsPlaying = false
        if (!didVideoEnd()) {
            mExoPlayer?.playWhenReady = false
        }

        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun togglePlayPause() {
        mIsPlaying = !mIsPlaying
        if (mIsPlaying) {
            resumeVideo()
        } else {
            pauseVideo()
        }
    }

    private fun setPosition(seconds: Int) {
        mExoPlayer?.seekTo(seconds * 1000L)
        binding.rlBottomVideoController.videoSeekbar.progress = seconds
        binding.rlBottomVideoController.videoCurrTime.text = seconds.getFormattedDuration()
    }

    private fun setLastVideoSavedPosition() {
        val pos = config.getLastVideoPosition(mUri.toString())
        if (pos > 0) {
            setPosition(pos)
        }
    }

    private fun videoCompleted() {
        if (mExoPlayer == null) {
            return
        }

        clearLastVideoSavedProgress()
        mCurrTime = (mExoPlayer!!.duration / 1000).toInt()
        binding.rlBottomVideoController.videoSeekbar.progress =
            binding.rlBottomVideoController.videoSeekbar.max
        binding.rlBottomVideoController.videoCurrTime.text = mDuration.getFormattedDuration()
        pauseVideo()
    }

    private fun didVideoEnd(): Boolean {
        val currentPos = mExoPlayer?.currentPosition ?: 0
        val duration = mExoPlayer?.duration ?: 0
        return currentPos != 0L && currentPos >= duration
    }

    private fun saveVideoProgress() {
        if (!didVideoEnd()) {
            config.saveLastVideoPosition(
                mUri.toString(),
                mExoPlayer!!.currentPosition.toInt() / 1000
            )
        }
    }

    private fun clearLastVideoSavedProgress() {
        config.removeLastVideoPosition(mUri.toString())
    }

    private fun setVideoSize() {
        val videoProportion = mVideoSize.x.toFloat() / mVideoSize.y.toFloat()
        val display = windowManager.defaultDisplay
        val screenWidth: Int
        val screenHeight: Int

        val realMetrics = DisplayMetrics()
        display.getRealMetrics(realMetrics)
        screenWidth = realMetrics.widthPixels
        screenHeight = realMetrics.heightPixels

        val screenProportion = screenWidth.toFloat() / screenHeight.toFloat()

        binding.videoSurface.layoutParams.apply {
            if (videoProportion > screenProportion) {
                width = screenWidth
                height = (screenWidth.toFloat() / videoProportion).toInt()
            } else {
                width = (videoProportion * screenHeight.toFloat()).toInt()
                height = screenHeight
            }
            binding.videoSurface.layoutParams = this
        }

        val multiplier = if (screenWidth > screenHeight) 0.5 else 0.8
        mScreenWidth = (screenWidth * multiplier).toInt()

        if (config.screenRotation == RotationRule.AspectRatio.id) {
            if (mVideoSize.x > mVideoSize.y) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            } else if (mVideoSize.x < mVideoSize.y) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        }
    }

    private fun changeOrientation() {
        mIsOrientationLocked = true
        requestedOrientation =
            if (resources.configuration.orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            } else {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
    }

    private fun toggleFullscreen() {
        fullscreenToggled(!mIsFullscreen)
    }

    private fun fullscreenToggled(isFullScreen: Boolean) {
        mIsFullscreen = isFullScreen
        if (isFullScreen) {
            HideSystemUiUseCase(this)
        } else {
            ShowSystemUiUseCase(this)
        }

        val newAlpha = if (isFullScreen) 0f else 1f
        arrayOf(
            binding.rlBottomVideoController.videoPrevFile,
            binding.rlBottomVideoController.videoTogglePlayPause,
            binding.rlBottomVideoController.videoNextFile,
            binding.rlBottomVideoController.videoCurrTime,
            binding.rlBottomVideoController.videoSeekbar,
            binding.rlBottomVideoController.videoDuration,
            binding.topShadow,
            binding.videoBottomGradient
        ).forEach {
            it.animate().alpha(newAlpha).start()
        }
        binding.rlBottomVideoController.videoSeekbar.setOnSeekBarChangeListener(if (mIsFullscreen) null else this)
        arrayOf(
            binding.rlBottomVideoController.videoPrevFile,
            binding.rlBottomVideoController.videoNextFile,
            binding.rlBottomVideoController.videoCurrTime,
            binding.rlBottomVideoController.videoDuration
        ).forEach {
            it.isClickable = !mIsFullscreen
        }

        binding.videoAppbar.animate().alpha(newAlpha).withStartAction {
            binding.videoAppbar.visibility = View.VISIBLE
        }.withEndAction {
            BeVisibleOrGoneUseCase(binding.videoAppbar, newAlpha == 1f)
        }.start()
    }

    private fun initTimeHolder() {
        var right = 0
        var bottom = 0

        if (hasNavBar()) {
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                bottom += navigationBarHeight
            } else {
                right += if (navigationBarRight) navigationBarSize.x else 0
                bottom += navigationBarHeight
            }
        }

        binding.rlBottomVideoController.videoTimeHolder.setPadding(0, 0, right, bottom)
        binding.rlBottomVideoController.videoSeekbar.setOnSeekBarChangeListener(this)
        binding.rlBottomVideoController.videoSeekbar.max = mDuration
        binding.rlBottomVideoController.videoDuration.text = mDuration.getFormattedDuration()
        binding.rlBottomVideoController.videoCurrTime.text = mCurrTime.getFormattedDuration()
        setupTimer()
    }

    private fun setupTimer() {
        runOnUiThread(object : Runnable {
            override fun run() {
                if (mExoPlayer != null && !mIsDragged && mIsPlaying) {
                    mCurrTime = (mExoPlayer!!.currentPosition / 1000).toInt()
                    binding.rlBottomVideoController.videoSeekbar.progress = mCurrTime
                    binding.rlBottomVideoController.videoCurrTime.text =
                        mCurrTime.getFormattedDuration()
                }

                mTimerHandler.postDelayed(this, 1000)
            }
        })
    }

    private fun doSkip(forward: Boolean) {
        if (mExoPlayer == null) {
            return
        }

        val curr = mExoPlayer!!.currentPosition
        val newProgress =
            if (forward) curr + FAST_FORWARD_VIDEO_MS else curr - FAST_FORWARD_VIDEO_MS
        val roundProgress = (newProgress / 1000f).roundToInt()
        val limitedProgress =
            (mExoPlayer!!.duration.toInt() / 1000).coerceAtMost(roundProgress).coerceAtLeast(0)
        setPosition(limitedProgress)
        if (!mIsPlaying) {
            togglePlayPause()
        }
    }

    private fun handleEvent(event: MotionEvent) {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                mTouchDownX = event.x
                mTouchDownY = event.y
                mTouchDownTime = System.currentTimeMillis()
                mProgressAtDown = mExoPlayer!!.currentPosition
            }
            MotionEvent.ACTION_POINTER_DOWN -> mIgnoreCloseDown = true
            MotionEvent.ACTION_MOVE -> {
                val diffX = event.x - mTouchDownX
                val diffY = event.y - mTouchDownY

                if (mIsDragged || (abs(diffX) > mDragThreshold && abs(diffX) > abs(diffY)) && binding.videoSurfaceFrame.controller.state.zoom == 1f) {
                    if (!mIsDragged) {
                        arrayOf(
                            binding.rlBottomVideoController.videoCurrTime,
                            binding.rlBottomVideoController.videoSeekbar,
                            binding.rlBottomVideoController.videoDuration
                        ).forEach {
                            it.animate().alpha(1f).start()
                        }
                    }
                    mIgnoreCloseDown = true
                    mIsDragged = true
                    var percent = ((diffX / mScreenWidth) * 100).toInt()
                    percent = 100.coerceAtMost((-100).coerceAtLeast(percent))

                    val skipLength = (mDuration * 1000f) * (percent / 100f)
                    var newProgress = mProgressAtDown + skipLength
                    newProgress =
                        mExoPlayer!!.duration.toFloat().coerceAtMost(newProgress).coerceAtLeast(0f)
                    val newSeconds = (newProgress / 1000).toInt()
                    setPosition(newSeconds)
                    resetPlayWhenReady()
                }
            }
            MotionEvent.ACTION_UP -> {
                val diffX = mTouchDownX - event.x
                val diffY = mTouchDownY - event.y

                val downGestureDuration = System.currentTimeMillis() - mTouchDownTime
                if (config.allowDownGesture && !mIgnoreCloseDown && abs(diffY) > abs(diffX) && diffY < -mCloseDownThreshold &&
                    downGestureDuration < MAX_CLOSE_DOWN_GESTURE_DURATION &&
                    binding.videoSurfaceFrame.controller.state.zoom == 1f
                ) {
                    supportFinishAfterTransition()
                }

                mIgnoreCloseDown = false
                if (mIsDragged) {
                    if (mIsFullscreen) {
                        arrayOf(
                            binding.rlBottomVideoController.videoCurrTime,
                            binding.rlBottomVideoController.videoSeekbar,
                            binding.rlBottomVideoController.videoDuration
                        ).forEach {
                            it.animate().alpha(0f).start()
                        }
                    }

                    if (!mIsPlaying) {
                        togglePlayPause()
                    }
                }
                mIsDragged = false
            }
        }
    }

    private fun handleNextFile() {
        Intent().apply {
            putExtra(GO_TO_NEXT_ITEM, true)
            setResult(RESULT_OK, this)
        }
        finish()
    }

    private fun handlePrevFile() {
        Intent().apply {
            putExtra(GO_TO_PREV_ITEM, true)
            setResult(RESULT_OK, this)
        }
        finish()
    }

    private fun resetPlayWhenReady() {
        mExoPlayer?.playWhenReady = false
        mPlayWhenReadyHandler.removeCallbacksAndMessages(null)
        mPlayWhenReadyHandler.postDelayed({
            mExoPlayer?.playWhenReady = true
        }, PLAY_WHEN_READY_DRAG_DELAY)
    }

    private fun releaseExoPlayer() {
        mExoPlayer?.stop()
        RunOnBackgroundThreadUseCase {
            mExoPlayer?.release()
            mExoPlayer = null
        }
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (mExoPlayer != null && fromUser) {
            setPosition(progress)
            resetPlayWhenReady()
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        mIsDragged = true
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        if (mExoPlayer == null)
            return

        if (mIsPlaying) {
            mExoPlayer!!.playWhenReady = true
        } else {
            togglePlayPause()
        }

        mIsDragged = false
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture) = false

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        RunOnBackgroundThreadUseCase {
            mExoPlayer?.setVideoSurface(Surface(binding.videoSurface.surfaceTexture))
        }
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}
}
