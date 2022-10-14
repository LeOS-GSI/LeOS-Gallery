package ca.on.sudbury.hojat.smartgallery.views


import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.RelativeLayout
import androidx.biometric.auth.AuthPromptHost
import ca.on.sudbury.hojat.smartgallery.R
import ca.on.sudbury.hojat.smartgallery.extensions.getProperPrimaryColor
import ca.on.sudbury.hojat.smartgallery.extensions.getProperTextColor
import ca.on.sudbury.hojat.smartgallery.extensions.toast
import ca.on.sudbury.hojat.smartgallery.extensions.updateTextColors
import ca.on.sudbury.hojat.smartgallery.helpers.PROTECTION_PATTERN
import ca.on.sudbury.hojat.smartgallery.interfaces.HashListener
import ca.on.sudbury.hojat.smartgallery.interfaces.SecurityTab
import ca.on.sudbury.hojat.smartgallery.patternlockview.PatternLockView
import ca.on.sudbury.hojat.smartgallery.patternlockview.listener.PatternLockViewListener
import ca.on.sudbury.hojat.smartgallery.patternlockview.utils.PatternLockUtils
import kotlinx.android.synthetic.main.tab_pattern.view.*

class PatternTab(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs),
    SecurityTab {
    private var hash = ""
    private var requiredHash = ""
    private var scrollView: MyScrollView? = null
    private lateinit var hashListener: HashListener

    @SuppressLint("ClickableViewAccessibility")
    override fun onFinishInflate() {
        super.onFinishInflate()
        val textColor = context.getProperTextColor()
        context.updateTextColors(pattern_lock_holder)

        pattern_lock_view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> scrollView?.isScrollable = false
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> scrollView?.isScrollable = true
            }
            false
        }

        pattern_lock_view.correctStateColor = context.getProperPrimaryColor()
        pattern_lock_view.normalStateColor = textColor
        pattern_lock_view.addPatternLockListener(object : PatternLockViewListener {
            override fun onComplete(pattern: MutableList<PatternLockView.Dot>?) {
                receivedHash(PatternLockUtils.patternToSha1(pattern_lock_view, pattern))
            }

            override fun onCleared() {}

            override fun onStarted() {}

            override fun onProgress(progressPattern: MutableList<PatternLockView.Dot>?) {}
        })
    }

    override fun initTab(
        requiredHash: String,
        listener: HashListener,
        scrollView: MyScrollView,
        biometricPromptHost: AuthPromptHost,
        showBiometricAuthentication: Boolean
    ) {
        this.requiredHash = requiredHash
        this.scrollView = scrollView
        hash = requiredHash
        hashListener = listener
    }

    private fun receivedHash(newHash: String) {
        when {
            hash.isEmpty() -> {
                hash = newHash
                pattern_lock_view.clearPattern()
                pattern_lock_title.setText(R.string.repeat_pattern)
            }
            hash == newHash -> {
                pattern_lock_view.setViewMode(PatternLockView.PatternViewMode.CORRECT)
                Handler().postDelayed({
                    hashListener.receivedHash(hash, PROTECTION_PATTERN)
                }, 300)
            }
            else -> {
                pattern_lock_view.setViewMode(PatternLockView.PatternViewMode.WRONG)
                context.toast(R.string.wrong_pattern)
                Handler().postDelayed({
                    pattern_lock_view.clearPattern()
                    if (requiredHash.isEmpty()) {
                        hash = ""
                        pattern_lock_title.setText(R.string.insert_pattern)
                    }
                }, 1000)
            }
        }
    }

    override fun visibilityChanged(isVisible: Boolean) {}
}
