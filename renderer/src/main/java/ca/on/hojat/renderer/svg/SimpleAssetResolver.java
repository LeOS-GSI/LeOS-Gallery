package ca.on.hojat.renderer.svg;

import timber.log.Timber;


/**
 * A sample implementation of {@link SVGExternalFileResolver} that retrieves files from
 * an application's "assets" folder.
 */

public class SimpleAssetResolver extends SVGExternalFileResolver {
    private static final String TAG = "SimpleAssetResolver";

    private android.content.res.AssetManager assetManager;


    @SuppressWarnings({"WeakerAccess", "unused"})
    public SimpleAssetResolver(android.content.res.AssetManager assetManager) {
        super();
        this.assetManager = assetManager;
    }


    private static final java.util.Set<String> supportedFormats = new java.util.HashSet<>(8);

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


    /**
     * Attempt to find the specified font in the "assets" folder and return a Typeface object.
     * For the font name "Foo", first the file "Foo.ttf" will be tried and if that fails, "Foo.otf".
     */
    @Override
    public android.graphics.Typeface resolveFont(String fontFamily, float fontWeight, String fontStyle, float fontStretch) {
        Timber.tag(TAG).i("resolveFont('" + fontFamily + "'," + fontWeight + ",'" + fontStyle + "'," + fontStretch + ")");

        // Try font name with suffix ".ttf"
        try {
            return android.graphics.Typeface.createFromAsset(assetManager, fontFamily + ".ttf");
        } catch (RuntimeException ignored) {
        }

        // That failed, so try ".otf"
        try {
            return android.graphics.Typeface.createFromAsset(assetManager, fontFamily + ".otf");
        } catch (RuntimeException ignored) {
        }

        // That failed, so try ".ttc" (Truetype collection), if supported on this version of Android
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            android.graphics.Typeface.Builder builder = new android.graphics.Typeface.Builder(assetManager, fontFamily + ".ttc");
            // Get the first font file in the collection
            builder.setTtcIndex(0);
            return builder.build();
        }

        return null;
    }


    /**
     * Attempt to find the specified image file in the <code>assets</code> folder and return a decoded Bitmap.
     */
    @Override
    public android.graphics.Bitmap resolveImage(String filename) {
        timber.log.Timber.tag(TAG).i("resolveImage(" + filename + ")");

        try {
            java.io.InputStream istream = assetManager.open(filename);
            return android.graphics.BitmapFactory.decodeStream(istream);
        } catch (java.io.IOException e1) {
            return null;
        }
    }


    /**
     * Returns true when passed the MIME types for SVG, JPEG, PNG or any of the
     * other bitmap image formats supported by Android's BitmapFactory class.
     */
    @Override
    public boolean isFormatSupported(String mimeType) {
        return supportedFormats.contains(mimeType);
    }


    /**
     * Attempt to find the specified stylesheet file in the "assets" folder and return its string contents.
     *
     * @since 1.3
     */
    @Override
    public String resolveCSSStyleSheet(String url) {
        timber.log.Timber.tag(TAG).i("resolveCSSStyleSheet(" + url + ")");
        return getAssetAsString(url);
    }


    /*
     * Read the contents of the asset whose name is given by "url" and return it as a String.
     */
    private String getAssetAsString(String url) {
        java.io.InputStream is = null;
        //noinspection TryFinallyCanBeTryWithResources
        try {
            is = assetManager.open(url);

            //noinspection CharsetObjectCanBeUsed
            java.io.Reader r = new java.io.InputStreamReader(is, java.nio.charset.Charset.forName("UTF-8"));
            char[] buffer = new char[4096];
            StringBuilder sb = new StringBuilder();
            int len = r.read(buffer);
            while (len > 0) {
                sb.append(buffer, 0, len);
                len = r.read(buffer);
            }
            return sb.toString();
        } catch (java.io.IOException e) {
            return null;
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (java.io.IOException e) {
                // Do nothing
            }
        }
    }

}
