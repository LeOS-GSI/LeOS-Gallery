package ca.on.hojat.renderer.exif

import java.text.DecimalFormat
import java.text.NumberFormat

object ExifUtil {

    private val formatter: NumberFormat = DecimalFormat.getInstance()

    @JvmStatic
    fun processLensSpecifications(values: Array<Rational>): String {
        val minFocal = values[0]
        val maxFocal = values[1]
        val minF = values[2]
        val maxF = values[3]

        formatter.maximumFractionDigits = 1
        return "${formatter.format(minFocal.toDouble())}-${formatter.format(maxFocal.toDouble())}mm f/${
            formatter.format(
                minF.toDouble()
            )
        }-${formatter.format(maxF.toDouble())}"
    }
}