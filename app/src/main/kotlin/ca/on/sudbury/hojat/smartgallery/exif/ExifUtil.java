package ca.on.sudbury.hojat.smartgallery.exif;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Created by alessandro on 20/04/14.
 */
public class ExifUtil {

    static final NumberFormat formatter = DecimalFormat.getInstance();

    public static String processLensSpecifications(Rational[] values) {
        Rational min_focal = values[0];
        Rational max_focal = values[1];
        Rational min_f = values[2];
        Rational max_f = values[3];

        formatter.setMaximumFractionDigits(1);

        return formatter.format(min_focal.toDouble()) +
                "-" +
                formatter.format(max_focal.toDouble()) +
                "mm f/" +
                formatter.format(min_f.toDouble()) +
                "-" +
                formatter.format(max_f.toDouble());
    }

}
