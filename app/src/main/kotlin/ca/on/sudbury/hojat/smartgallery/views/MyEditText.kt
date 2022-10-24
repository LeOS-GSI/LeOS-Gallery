package ca.on.sudbury.hojat.smartgallery.views


import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import ca.on.sudbury.hojat.smartgallery.extensions.adjustAlpha
import ca.on.sudbury.hojat.smartgallery.helpers.MEDIUM_ALPHA
import ca.on.sudbury.hojat.smartgallery.usecases.ApplyColorFilterUseCase

class MyEditText : AppCompatEditText {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    fun setColors(textColor: Int, accentColor: Int) {
        ApplyColorFilterUseCase(background?.mutate(), accentColor)

        // requires android:textCursorDrawable="@null" in xml to color the cursor too
        setTextColor(textColor)
        setHintTextColor(textColor.adjustAlpha(MEDIUM_ALPHA))
        setLinkTextColor(accentColor)
    }
}
