package ca.on.sudbury.hojat.smartgallery.video

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Point
import android.graphics.SurfaceTexture
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.view.Surface
import android.view.WindowManager
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.view.TextureView
import android.widget.RelativeLayout
import android.widget.SeekBar
import ca.on.sudbury.hojat.smartgallery.R
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.SeekParameters
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.upstream.ContentDataSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.FileDataSource
import ca.on.sudbury.hojat.smartgallery.extensions.beGoneIf
import ca.on.sudbury.hojat.smartgallery.extensions.updateTextColors
import ca.on.sudbury.hojat.smartgallery.extensions.beVisibleIf
import ca.on.sudbury.hojat.smartgallery.extensions.isGone
import ca.on.sudbury.hojat.smartgallery.extensions.navigationBarHeight
import ca.on.sudbury.hojat.smartgallery.extensions.beGone
import ca.on.sudbury.hojat.smartgallery.extensions.navigationBarWidth
import ca.on.sudbury.hojat.smartgallery.extensions.onGlobalLayout
import ca.on.sudbury.hojat.smartgallery.extensions.beInvisible
import ca.on.sudbury.hojat.smartgallery.extensions.beVisible
import ca.on.sudbury.hojat.smartgallery.extensions.showErrorToast
import ca.on.sudbury.hojat.smartgallery.extensions.getFormattedDuration
import ca.on.sudbury.hojat.smartgallery.extensions.getDuration
import ca.on.sudbury.hojat.smartgallery.extensions.getVideoResolution
import ca.on.sudbury.hojat.smartgallery.extensions.beInvisibleIf
import ca.on.sudbury.hojat.smartgallery.extensions.realScreenSize
import ca.on.sudbury.hojat.smartgallery.extensions.isVisible
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import ca.on.sudbury.hojat.smartgallery.activities.PanoramaVideoActivity
import ca.on.sudbury.hojat.smartgallery.databinding.PagerVideoItemBinding
import ca.on.sudbury.hojat.smartgallery.extensions.config
import ca.on.sudbury.hojat.smartgallery.extensions.hasNavBar
import ca.on.sudbury.hojat.smartgallery.extensions.parseFileChannel
import ca.on.sudbury.hojat.smartgallery.fragments.ViewPagerFragment
import ca.on.sudbury.hojat.smartgallery.helpers.Config
import ca.on.sudbury.hojat.smartgallery.helpers.MEDIUM
import ca.on.sudbury.hojat.smartgallery.helpers.SHOULD_INIT_FRAGMENT
import ca.on.sudbury.hojat.smartgallery.helpers.PATH
import ca.on.sudbury.hojat.smartgallery.helpers.FAST_FORWARD_VIDEO_MS
import ca.on.sudbury.hojat.smartgallery.models.Medium
import java.io.File
import java.io.FileInputStream
import kotlin.math.roundToInt

class VideoFragment : ViewPagerFragment(), TextureView.SurfaceTextureListener, SeekBar.OnSeekBarChangeListener {

    private var _binding: PagerVideoItemBinding? = null
    private val binding get() = _binding!!

    private val _progress = "progress"

    private var mIsFullscreen = false
    private var mWasFragmentInit = false
    private var mIsPanorama = false
    private var mIsFragmentVisible = false
    private var mIsDragged = false
    private var mWasVideoStarted = false
    private var mWasPlayerInited = false
    private var mWasLastPositionRestored = false
    private var mPlayOnPrepared = false
    private var mIsPlayerPrepared = false
    private var mCurrTime = 0
    private var mDuration = 0
    private var mPositionWhenInit = 0
    private var mPositionAtPause = 0L
    var mIsPlaying = false

    private var mExoPlayer: SimpleExoPlayer? = null
    private var mVideoSize = Point(1, 1)
    private var mTimerHandler = Handler()

    private var mStoredShowExtendedDetails = false
    private var mStoredHideExtendedDetails = false
    private var mStoredBottomActions = true
    private var mStoredExtendedDetails = 0
    private var mStoredRememberLastVideoPosition = false

    private lateinit var mMedium: Medium
    private lateinit var mConfig: Config


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = PagerVideoItemBinding.inflate(inflater, container, false)

        mMedium = requireArguments().getSerializable(MEDIUM) as Medium
        mConfig = requireContext().config
        binding.apply {
            panoramaOutline.setOnClickListener { openPanorama() }
            rlBottomVideoTimeHolder.videoCurrTime.setOnClickListener { skip(false) }
            rlBottomVideoTimeHolder.videoDuration.setOnClickListener { skip(true) }
            videoHolder.setOnClickListener { toggleFullscreen() }
            videoPreview.setOnClickListener { toggleFullscreen() }
            videoSurfaceFrame.controller.settings.swallowDoubleTaps = true

            videoPlayOutline.setOnClickListener {
                if (mConfig.openVideosOnSeparateScreen) {
                    launchVideoPlayer()
                } else {
                    togglePlayPause()
                }
            }

            rlBottomVideoTimeHolder.videoTogglePlayPause.setOnClickListener {
                togglePlayPause()
            }


            rlBottomVideoTimeHolder.videoSeekbar.setOnSeekBarChangeListener(this@VideoFragment)
            // adding an empty click listener just to avoid ripple animation at toggling fullscreen
            rlBottomVideoTimeHolder.videoSeekbar.setOnClickListener { }
            videoSurface.surfaceTextureListener = this@VideoFragment

            val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                    if (!mConfig.allowInstantChange) {
                        toggleFullscreen()
                        return true
                    }

                    val viewWidth = root.width
                    val instantWidth = viewWidth / 7
                    val clickedX = e?.rawX ?: 0f
                    when {
                        clickedX <= instantWidth -> listener?.goToPrevItem()
                        clickedX >= viewWidth - instantWidth -> listener?.goToNextItem()
                        else -> toggleFullscreen()
                    }
                    return true
                }

                override fun onDoubleTap(e: MotionEvent?): Boolean {
                    if (e != null) {
                        handleDoubleTap(e.rawX)
                    }

                    return true
                }
            })

            videoPreview.setOnTouchListener { _, event ->
                handleEvent(event)
                false
            }

            videoSurfaceFrame.setOnTouchListener { _, event ->
                if (videoSurfaceFrame.controller.state.zoom == 1f) {
                    handleEvent(event)
                }

                gestureDetector.onTouchEvent(event)
                false
            }
        }

        if (!requireArguments().getBoolean(SHOULD_INIT_FRAGMENT, true)) {
            return binding.root
        }

        storeStateVariables()
        Glide.with(requireContext()).load(mMedium.path).into(binding.videoPreview)

        // setMenuVisibility is not called at VideoActivity (third party intent)
        if (!mIsFragmentVisible && activity is VideoActivity) {
            mIsFragmentVisible = true
        }

        mIsFullscreen = requireActivity().window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_FULLSCREEN == View.SYSTEM_UI_FLAG_FULLSCREEN
        initTimeHolder()
        checkIfPanorama()

        ensureBackgroundThread {
            activity?.getVideoResolution(mMedium.path)?.apply {
                mVideoSize.x = x
                mVideoSize.y = y
            }
        }

        if (mIsPanorama) {
            binding.apply {
                panoramaOutline.beVisible()
                videoPlayOutline.beGone()
                videoVolumeController.beGone()
                videoBrightnessController.beGone()
                Glide.with(requireContext()).load(mMedium.path).into(videoPreview)
            }
        }

        if (!mIsPanorama) {
            if (savedInstanceState != null) {
                mCurrTime = savedInstanceState.getInt(_progress)
            }

            mWasFragmentInit = true
            setVideoSize()

            binding.apply {
                videoBrightnessController.initialize(requireActivity(), slideInfo, true, container, singleTap = { _, _ ->
                    if (mConfig.allowInstantChange) {
                        listener?.goToPrevItem()
                    } else {
                        toggleFullscreen()
                    }
                }, doubleTap = { _, _ ->
                    doSkip(false)
                })

                videoVolumeController.initialize(requireActivity(), slideInfo, false, container, singleTap = { _, _ ->
                    if (mConfig.allowInstantChange) {
                        listener?.goToNextItem()
                    } else {
                        toggleFullscreen()
                    }
                }, doubleTap = { _, _ ->
                    doSkip(true)
                })

                videoSurface.onGlobalLayout {
                    if (mIsFragmentVisible && mConfig.autoplayVideos && !mConfig.openVideosOnSeparateScreen) {
                        playVideo()
                    }
                }
            }
        }

        setupVideoDuration()
        if (mStoredRememberLastVideoPosition) {
            restoreLastVideoSavedPosition()
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        mConfig = requireContext().config      // make sure we get a new config, in case the user changed something in the app settings
        requireActivity().updateTextColors(binding.videoHolder)
        val allowVideoGestures = mConfig.allowVideoGestures
        binding.videoSurface.beGoneIf(mConfig.openVideosOnSeparateScreen || mIsPanorama)
        binding.videoSurfaceFrame.beGoneIf(binding.videoSurface.isGone())
        binding.videoVolumeController.beVisibleIf(allowVideoGestures && !mIsPanorama)
        binding.videoBrightnessController.beVisibleIf(allowVideoGestures && !mIsPanorama)

        checkExtendedDetails()
        initTimeHolder()
        storeStateVariables()
    }

    override fun onPause() {
        super.onPause()
        storeStateVariables()
        pauseVideo()
        if (mStoredRememberLastVideoPosition && mIsFragmentVisible && mWasVideoStarted) {
            saveVideoProgress()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (activity?.isChangingConfigurations == false) {
            cleanup()
        }
        _binding = null
    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        if (mIsFragmentVisible && !menuVisible) {
            pauseVideo()
        }

        mIsFragmentVisible = menuVisible
        if (mWasFragmentInit && menuVisible && mConfig.autoplayVideos && !mConfig.openVideosOnSeparateScreen) {
            playVideo()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setVideoSize()
        initTimeHolder()
        checkExtendedDetails()
        binding.videoSurfaceFrame.onGlobalLayout {
            binding.videoSurfaceFrame.controller.resetState()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(_progress, mCurrTime)
    }

    private fun storeStateVariables() {
        mConfig.apply {
            mStoredShowExtendedDetails = showExtendedDetails
            mStoredHideExtendedDetails = hideExtendedDetails
            mStoredExtendedDetails = extendedDetails
            mStoredBottomActions = bottomActions
            mStoredRememberLastVideoPosition = rememberLastVideoPosition
        }
    }

    private fun saveVideoProgress() {
        if (!videoEnded()) {
            if (mExoPlayer != null) {
                mConfig.saveLastVideoPosition(mMedium.path, mExoPlayer!!.currentPosition.toInt() / 1000)
            } else {
                mConfig.saveLastVideoPosition(mMedium.path, mPositionAtPause.toInt() / 1000)
            }
        }
    }

    private fun restoreLastVideoSavedPosition() {
        val pos = mConfig.getLastVideoPosition(mMedium.path)
        if (pos > 0) {
            mPositionAtPause = pos * 1000L
            setPosition(pos)
        }
    }

    private fun setupTimeHolder() {
        _binding?.rlBottomVideoTimeHolder?.videoSeekbar?.max = mDuration
        _binding?.rlBottomVideoTimeHolder?.videoDuration?.text = mDuration.getFormattedDuration()
        setupTimer()
    }

    private fun setupTimer() {
        activity?.runOnUiThread(object : Runnable {
            override fun run() {
                if (mExoPlayer != null && !mIsDragged && mIsPlaying) {
                    mCurrTime = (mExoPlayer!!.currentPosition / 1000).toInt()
                    _binding?.rlBottomVideoTimeHolder?.videoSeekbar?.progress = mCurrTime
                    _binding?.rlBottomVideoTimeHolder?.videoCurrTime?.text = mCurrTime.getFormattedDuration()
                }

                mTimerHandler.postDelayed(this, 1000)
            }
        })
    }

    private fun initExoPlayer() {
        if (activity == null || mConfig.openVideosOnSeparateScreen || mIsPanorama || mExoPlayer != null) {
            return
        }

        mExoPlayer = ExoPlayerFactory.newSimpleInstance(context)
        mExoPlayer!!.seekParameters = SeekParameters.CLOSEST_SYNC
        if (mConfig.loopVideos && listener?.isSlideShowActive() == false) {
            mExoPlayer?.repeatMode = Player.REPEAT_MODE_ONE
        }

        val isContentUri = mMedium.path.startsWith("content://")
        val uri = if (isContentUri) Uri.parse(mMedium.path) else Uri.fromFile(File(mMedium.path))
        val dataSpec = DataSpec(uri)
        val fileDataSource = if (isContentUri) ContentDataSource(context) else FileDataSource()
        try {
            fileDataSource.open(dataSpec)
        } catch (e: Exception) {
            activity?.showErrorToast(e)
            return
        }

        val factory = DataSource.Factory { fileDataSource }
        val audioSource = ExtractorMediaSource(fileDataSource.uri, factory, DefaultExtractorsFactory(), null, null)
        mPlayOnPrepared = true
        mExoPlayer!!.audioStreamType = C.STREAM_TYPE_MUSIC
        mExoPlayer!!.prepare(audioSource)

        if (binding.videoSurface.surfaceTexture != null) {
            mExoPlayer!!.setVideoSurface(Surface(binding.videoSurface.surfaceTexture))
        }

        mExoPlayer!!.addListener(object : Player.EventListener {
            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {}

            override fun onSeekProcessed() {}

            override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {}

            override fun onPlayerError(error: ExoPlaybackException?) {}

            override fun onLoadingChanged(isLoading: Boolean) {}

            override fun onPositionDiscontinuity(reason: Int) {
                // Reset progress views when video loops.
                if (reason == Player.DISCONTINUITY_REASON_PERIOD_TRANSITION) {
                    binding.rlBottomVideoTimeHolder.videoSeekbar.progress = 0
                    binding.rlBottomVideoTimeHolder.videoCurrTime.text = 0.getFormattedDuration()
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

        mExoPlayer!!.addVideoListener(object : SimpleExoPlayer.VideoListener {
            override fun onVideoSizeChanged(width: Int, height: Int, unappliedRotationDegrees: Int, pixelWidthHeightRatio: Float) {
                mVideoSize.x = width
                mVideoSize.y = (height / pixelWidthHeightRatio).toInt()
                setVideoSize()
            }

            override fun onRenderedFirstFrame() {}
        })
    }

    private fun launchVideoPlayer() {
        listener?.launchViewVideoIntent(mMedium.path)
    }

    private fun toggleFullscreen() {
        listener?.fragmentClicked()
    }

    private fun handleDoubleTap(x: Float) {
        val viewWidth = binding.root.width
        val instantWidth = viewWidth / 7
        when {
            x <= instantWidth -> doSkip(false)
            x >= viewWidth - instantWidth -> doSkip(true)
            else -> togglePlayPause()
        }
    }

    private fun checkExtendedDetails() {
        if (mConfig.showExtendedDetails) {
            binding.videoDetails.apply {
                beInvisible()   // make it invisible so we can measure it, but not show yet
                text = getMediumExtendedDetails(mMedium)
                onGlobalLayout {
                    if (isAdded) {
                        val realY = getExtendedDetailsY(height)
                        if (realY > 0) {
                            y = realY
                            beVisibleIf(text.isNotEmpty())
                            alpha = if (!mConfig.hideExtendedDetails || !mIsFullscreen) 1f else 0f
                        }
                    }
                }
            }
        } else {
            binding.videoDetails.beGone()
        }
    }

    private fun initTimeHolder() {
        var right = 0
        var bottom = requireContext().navigationBarHeight
        if (mConfig.bottomActions) {
            bottom += resources.getDimension(R.dimen.bottom_actions_height).toInt()
        }

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE && activity?.hasNavBar() == true) {
            right += requireActivity().navigationBarWidth
        }

        (binding.rlBottomVideoTimeHolder.videoTimeHolder.layoutParams as RelativeLayout.LayoutParams).apply {
            bottomMargin = bottom
            rightMargin = right
        }
        binding.rlBottomVideoTimeHolder.videoTimeHolder.beInvisibleIf(mIsFullscreen)
    }

    private fun checkIfPanorama() {
        try {
            val fis = FileInputStream(File(mMedium.path))
            fis.use {
                requireContext().parseFileChannel(mMedium.path, it.channel, 0, 0, 0) {
                    mIsPanorama = true
                }
            }
        } catch (ignored: Exception) {
        } catch (ignored: OutOfMemoryError) {
        }
    }

    private fun openPanorama() {
        Intent(context, PanoramaVideoActivity::class.java).apply {
            putExtra(PATH, mMedium.path)
            startActivity(this)
        }
    }

    override fun fullscreenToggled(isFullscreen: Boolean) {
        mIsFullscreen = isFullscreen
        val newAlpha = if (isFullscreen) 0f else 1f
        if (!mIsFullscreen) {
            binding.rlBottomVideoTimeHolder.videoTimeHolder.beVisible()
        }

        binding.rlBottomVideoTimeHolder.videoSeekbar.setOnSeekBarChangeListener(if (mIsFullscreen) null else this)
        arrayOf(
            binding.rlBottomVideoTimeHolder.videoCurrTime,
            binding.rlBottomVideoTimeHolder.videoDuration,
            binding.rlBottomVideoTimeHolder.videoTogglePlayPause
        ).forEach {
            it.isClickable = !mIsFullscreen
        }

        binding.rlBottomVideoTimeHolder.videoTimeHolder.animate().alpha(newAlpha).start()
        binding.videoDetails.apply {
            if (mStoredShowExtendedDetails && isVisible() && context != null && resources != null) {
                animate().y(getExtendedDetailsY(height))

                if (mStoredHideExtendedDetails) {
                    animate().alpha(newAlpha).start()
                }
            }
        }
    }

    private fun getExtendedDetailsY(height: Int): Float {
        val smallMargin = context?.resources?.getDimension(R.dimen.small_margin) ?: return 0f
        val fullscreenOffset = smallMargin + if (mIsFullscreen) 0 else requireContext().navigationBarHeight
        var actionsHeight = 0f
        if (!mIsFullscreen) {
            actionsHeight += resources.getDimension(R.dimen.video_player_play_pause_size)
            if (mConfig.bottomActions) {
                actionsHeight += resources.getDimension(R.dimen.bottom_actions_height)
            }
        }
        return requireContext().realScreenSize.y - height - actionsHeight - fullscreenOffset
    }

    private fun skip(forward: Boolean) {
        if (mIsPanorama) {
            return
        } else if (mExoPlayer == null) {
            playVideo()
            return
        }

        mPositionAtPause = 0L
        doSkip(forward)
    }

    private fun doSkip(forward: Boolean) {
        if (mExoPlayer == null) {
            return
        }

        val curr = mExoPlayer!!.currentPosition
        val newProgress = if (forward) curr + FAST_FORWARD_VIDEO_MS else curr - FAST_FORWARD_VIDEO_MS
        val roundProgress = (newProgress / 1000f).roundToInt()
        val limitedProgress = (mExoPlayer!!.duration.toInt() / 1000).coerceAtMost(roundProgress).coerceAtLeast(0)
        setPosition(limitedProgress)
        if (!mIsPlaying) {
            togglePlayPause()
        }
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        if (fromUser) {
            if (mExoPlayer != null) {
                if (!mWasPlayerInited) {
                    mPositionWhenInit = progress
                }
                setPosition(progress)
            }

            if (mExoPlayer == null) {
                mPositionAtPause = progress * 1000L
                playVideo()
            }
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {
        if (mExoPlayer == null) {
            return
        }

        mExoPlayer!!.playWhenReady = false
        mIsDragged = true
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        if (mIsPanorama) {
            openPanorama()
            return
        }

        if (mExoPlayer == null) {
            return
        }

        if (mIsPlaying) {
            mExoPlayer!!.playWhenReady = true
        } else {
            playVideo()
        }

        mIsDragged = false
    }

    private fun togglePlayPause() {
        if (activity == null || !isAdded) {
            return
        }

        if (mIsPlaying) {
            pauseVideo()
        } else {
            playVideo()
        }
    }

    fun playVideo() {
        if (mExoPlayer == null) {
            initExoPlayer()
            return
        }

        if (binding.videoPreview.isVisible()) {
            binding.videoPreview.beGone()
            initExoPlayer()
        }

        val wasEnded = videoEnded()
        if (wasEnded) {
            setPosition(0)
        }

        if (mStoredRememberLastVideoPosition && !mWasLastPositionRestored) {
            mWasLastPositionRestored = true
            restoreLastVideoSavedPosition()
        }

        if (!wasEnded || !mConfig.loopVideos) {
            binding.rlBottomVideoTimeHolder.videoTogglePlayPause.setImageResource(R.drawable.ic_pause_outline_vector)
        }

        if (!mWasVideoStarted) {
            binding.videoPlayOutline.beGone()
            binding.rlBottomVideoTimeHolder.videoTogglePlayPause.beVisible()
        }

        mWasVideoStarted = true
        if (mIsPlayerPrepared) {
            mIsPlaying = true
        }
        mExoPlayer?.playWhenReady = true
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun pauseVideo() {
        if (mExoPlayer == null) {
            return
        }

        mIsPlaying = false
        if (!videoEnded()) {
            mExoPlayer?.playWhenReady = false
        }

        binding.rlBottomVideoTimeHolder.videoTogglePlayPause.setImageResource(R.drawable.ic_play_outline_vector)
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        mPositionAtPause = mExoPlayer?.currentPosition ?: 0L
        releaseExoPlayer()
    }

    private fun videoEnded(): Boolean {
        val currentPos = mExoPlayer?.currentPosition ?: 0
        val duration = mExoPlayer?.duration ?: 0
        return currentPos != 0L && currentPos >= duration
    }

    private fun setPosition(seconds: Int) {
        mExoPlayer?.seekTo(seconds * 1000L)
        _binding?.rlBottomVideoTimeHolder?.videoSeekbar?.progress = seconds
        _binding?.rlBottomVideoTimeHolder?.videoCurrTime?.text = seconds.getFormattedDuration()

        if (!mIsPlaying) {
            mPositionAtPause = mExoPlayer?.currentPosition ?: 0L
        }
    }

    private fun setupVideoDuration() {
        ensureBackgroundThread {
            mDuration = context?.getDuration(mMedium.path) ?: 0

            activity?.runOnUiThread {
                setupTimeHolder()
                setPosition(0)
            }
        }
    }

    private fun videoPrepared() {
        if (mDuration == 0) {
            mDuration = (mExoPlayer!!.duration / 1000).toInt()
            setupTimeHolder()
            setPosition(mCurrTime)

            if (mIsFragmentVisible && (mConfig.autoplayVideos)) {
                playVideo()
            }
        }

        if (mPositionWhenInit != 0 && !mWasPlayerInited) {
            setPosition(mPositionWhenInit)
            mPositionWhenInit = 0
        }

        mIsPlayerPrepared = true
        if (mPlayOnPrepared && !mIsPlaying) {
            if (mPositionAtPause != 0L) {
                mExoPlayer?.seekTo(mPositionAtPause)
                mPositionAtPause = 0L
            }
            playVideo()
        }
        mWasPlayerInited = true
        mPlayOnPrepared = false
    }

    private fun videoCompleted() {
        if (!isAdded || mExoPlayer == null) {
            return
        }

        mCurrTime = (mExoPlayer!!.duration / 1000).toInt()
        if (listener?.videoEnded() == false && mConfig.loopVideos) {
            playVideo()
        } else {
            binding.rlBottomVideoTimeHolder.videoSeekbar.progress = binding.rlBottomVideoTimeHolder.videoSeekbar.max
            binding.rlBottomVideoTimeHolder.videoCurrTime.text = mDuration.getFormattedDuration()
            pauseVideo()
        }
    }

    private fun cleanup() {
        pauseVideo()
        releaseExoPlayer()

        if (mWasFragmentInit) {
            binding.rlBottomVideoTimeHolder.videoCurrTime.text = 0.getFormattedDuration()
            binding.rlBottomVideoTimeHolder.videoSeekbar.progress = 0
            mTimerHandler.removeCallbacksAndMessages(null)
        }
    }

    private fun releaseExoPlayer() {
        mIsPlayerPrepared = false
        mExoPlayer?.stop()
        ensureBackgroundThread {
            mExoPlayer?.release()
            mExoPlayer = null
        }
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture) = false

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        ensureBackgroundThread {
            mExoPlayer?.setVideoSurface(Surface(binding.videoSurface.surfaceTexture))
        }
    }

    private fun setVideoSize() {
        if (activity == null || mConfig.openVideosOnSeparateScreen) {
            return
        }

        val videoProportion = mVideoSize.x.toFloat() / mVideoSize.y.toFloat()
        val display = requireActivity().windowManager.defaultDisplay
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
    }
}
