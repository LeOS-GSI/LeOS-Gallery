package ca.on.hojat.renderer.svg;

import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

import timber.log.Timber;

/**
 * A sample implementation of {@link SVGExternalFileResolver} that retrieves files from
 * an application's "assets" folder.
 */

public class SimpleAssetResolver extends SVGExternalFileResolver {
    private static final String TAG = "SimpleAssetResolver";
    private static final Set<String> supportedFormats = new HashSet<>(8);

    // Static initialiser
    static {
        // PNG, JPEG and SVG are required by the SVG 1.2 spec
        supportedFormats.add("image/svg+xml");
        supportedFormats.add("image/jpeg");
        supportedFormats.add("image/png");
        // Other image formats supported by Android BitmapFactory
        supportedFormats.add("image/pjpeg");
        supportedFormats.add("image/gif");
        supportedFormats.add("image/bmp");
        supportedFormats.add("image/x-windows-bmp");
        // .webp supported in 4.0+ (ICE_CREAM_SANDWICH)
        supportedFormats.add("image/webp");
    }

    private AssetManager assetManager;

    @SuppressWarnings({"WeakerAccess", "unused"})
    public SimpleAssetResolver(AssetManager assetManager) {
        super();
        this.assetManager = assetManager;
    }

    /*
     * Read the contents of the asset whose name is given by "url" and return it as a String.
     */
    private String getAssetAsString(String url) {
        InputStream is = null;
        //noinspection TryFinallyCanBeTryWithResources
        try {
            is = assetManager.open(url);

            //noinspection CharsetObjectCanBeUsed
            Reader r = new InputStreamReader(is, Charset.forName("UTF-8"));
            char[] buffer = new char[4096];
            StringBuilder sb = new StringBuilder();
            int len = r.read(buffer);
            while (len > 0) {
                sb.append(buffer, 0, len);
                len = r.read(buffer);
            }
            return sb.toString();
        } catch (IOException e) {
            return null;
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException e) {
                Timber.e(e);
            }
        }
    }

}
