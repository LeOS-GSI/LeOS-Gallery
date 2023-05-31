package ca.on.sudbury.hojat.smartgallery.views


import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatSeekBar
import ca.on.sudbury.hojat.smartgallery.usecases.ApplyColorFilterUseCase

class MySeekBar : AppCompatSeekBar {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    fun setColors(accentColor: Int) {
        ApplyColorFilterUseCase(progressDrawable, accentColor)
        ApplyColorFilterUseCase(thumb, accentColor)
    }
}
