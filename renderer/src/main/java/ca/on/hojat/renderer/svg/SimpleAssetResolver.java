package ca.on.hojat.renderer.svg;


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
