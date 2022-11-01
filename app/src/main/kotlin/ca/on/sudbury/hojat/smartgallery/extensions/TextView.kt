package ca.on.sudbury.hojat.smartgallery.extensions

import android.graphics.Paint
import android.text.SpannableString
import android.text.TextPaint
import android.text.style.URLSpan
import android.widget.TextView

/**
 * These 2 extension functions of [TextView] are pretty simple and are specific to this class.
 * That's why I'm not going to remove them; they'll remain in here at least till the time I
 * migrate to Jetpack Compose.
 */

fun TextView.underlineText() {
    paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
}

fun TextView.removeUnderlines() {
    val spannable = SpannableString(text)
    for (u in spannable.getSpans(0, spannable.length, URLSpan::class.java)) {
        spannable.setSpan(object : URLSpan(u.url) {
            override fun updateDrawState(textPaint: TextPaint) {
                super.updateDrawState(textPaint)
                textPaint.isUnderlineText = false
            }
        }, spannable.getSpanStart(u), spannable.getSpanEnd(u), 0)
    }
    text = spannable
}
