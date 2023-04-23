package ca.on.hojat.renderer.exif

import java.text.DecimalFormat
import java.text.NumberFormat

object ExifUtil {

    private val formatter: NumberFormat = DecimalFormat.getInstance()

    @JvmStatic
    fun processLensSpecifications(values: Array<Rational>): String {
        val min_focal = values[0]
        val max_focal = values[1]
        val min_f = values[2]
        val max_f = values[3]

        formatter.maximumFractionDigits = 1
        return "${formatter.format(min_focal.toDouble())}-${formatter.format(max_focal.toDouble())}mm f/${
            formatter.format(
                min_f.toDouble()
            )
        }-${formatter.format(max_f.toDouble())}"
    }
}