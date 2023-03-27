package ca.on.hojat.renderer.svg;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.View;

import androidx.appcompat.widget.AppCompatImageView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

import ca.on.hojat.renderer.R;
import timber.log.Timber;

/**
 * SVGImageView is a View widget that allows users to include SVG images in their layouts.
 * <p>
 * It is implemented as a thin layer over {@code android.widget.ImageView}.
 *
 * <h2>XML attributes</h2>
 * <dl>
 *   <dt><code>svg</code></dt>
 *   <dd>A resource reference, or a file name, of an SVG in your application</dd>
 *   <dt><code>css</code></dt>
 *   <dd>Optional extra CSS to apply when rendering the SVG</dd>
 * </dl>
 */
public class SVGImageView extends AppCompatImageView {
    private static Method setLayerTypeMethod = null;

    static {
        try {
            setLayerTypeMethod = View.class.getMethod("setLayerType", Integer.TYPE, Paint.class);
        } catch (NoSuchMethodException e) { /* do nothing */ }
    }

    private final RenderOptions renderOptions = new RenderOptions();
    private SVG svg = null;


    public SVGImageView(Context context) {
        super(context);
    }


    public SVGImageView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        init(attrs, 0);
    }


    public SVGImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }


    private void init(AttributeSet attrs, int defStyle) {
        if (isInEditMode())
            return;

        TypedArray a = getContext().getTheme()
                .obtainStyledAttributes(attrs, R.styleable.SVGImageView, defStyle, 0);
        try {
            // Check for css attribute
            String css = a.getString(R.styleable.SVGImageView_css);
            if (css != null)
                renderOptions.css(css);

            // Check whether svg attribute is a resourceId
            int resourceId = a.getResourceId(R.styleable.SVGImageView_svg, -1);
            if (resourceId != -1) {
                setImageResource(resourceId);
                return;
            }

            // Check whether svg attribute is a string.
            // Could be a URL/filename or an SVG itself
            String url = a.getString(R.styleable.SVGImageView_svg);
            if (url != null) {
                Uri uri = Uri.parse(url);
                if (internalSetImageURI(uri))
                    return;

                // Not a URL, so try loading it as an asset filename
                if (internalSetImageAsset(url))
                    return;

                // Last chance, maybe there is an actual SVG in the string
                // If the SVG is in the string, then we will assume it is not very large, and thus doesn't need to be parsed in the background.
                setFromString(url);
            }

        } finally {
            a.recycle();
        }
    }


    /**
     * Directly set the SVG that should be rendered by this view.
     *
     * @param svg An {@code SVG} instance
     * @since 1.2.1
     */
    public void setSVG(SVG svg) {
        if (svg == null)
            throw new IllegalArgumentException("Null value passed to setSVG()");
        this.svg = svg;
        doRender();
    }


    /**
     * Directly set the SVG and the CSS.
     *
     * @param svg An {@code SVG} instance
     * @param css Optional extra CSS to apply when rendering
     * @since 1.3
     */
    public void setSVG(SVG svg, String css) {
        if (svg == null)
            throw new IllegalArgumentException("Null value passed to setSVG()");

        this.svg = svg;
        this.renderOptions.css(css);

        doRender();
    }


    /**
     * Directly set the CSS.
     *
     * @param css Extra CSS to apply when rendering
     * @since 1.3
     */
    public void setCSS(String css) {
        this.renderOptions.css(css);
        doRender();
    }


    /**
     * Load an SVG image from the given resource id.
     *
     * @param resourceId the id of an Android resource in your application
     */
    @Override
    public void setImageResource(int resourceId) {
        new LoadResourceTask(this.getContext()).execute(resourceId);
    }


    /**
     * Load an SVG image from the given resource URI.
     *
     * @param uri the URI of an Android resource in your application
     */
    @Override
    public void setImageURI(Uri uri) {
        if (!internalSetImageURI(uri))
            Timber.tag("SVGImageView").e("File not found: %s", uri);
    }


    /**
     * Load an SVG image from the given asset filename.
     *
     * @param filename the file name of an SVG in the assets folder in your application
     */
    public void setImageAsset(String filename) {
        if (!internalSetImageAsset(filename))
            Timber.tag("SVGImageView").e("File not found: %s", filename);
    }


    //===============================================================================================


    /*
     * Attempt to set a picture from a Uri. Return true if it worked.
     */
    private boolean internalSetImageURI(Uri uri) {
        try {
            InputStream is = getContext().getContentResolver().openInputStream(uri);
            new LoadURITask().execute(is);
            return true;
        } catch (FileNotFoundException e) {
            return false;
        }

    }


    private boolean internalSetImageAsset(String filename) {
        try {
            InputStream is = getContext().getAssets().open(filename);
            new LoadURITask().execute(is);
            return true;
        } catch (IOException e) {
            return false;
        }

    }


    private void setFromString(String url) {
        try {
            this.svg = SVG.getFromString(url);
            doRender();
        } catch (SVGParseException e) {
            // Failed to interpret url as a resource, a filename, or an actual SVG...
            Timber.tag("SVGImageView").e("Could not find SVG at: %s", url);
        }
    }


    //===============================================================================================

    /*
     * Use reflection to call an API 11 method from this library (which is configured with a minSdkVersion of 8)
     */
    private void setSoftwareLayerType() {
        if (setLayerTypeMethod == null)
            return;

        try {
            int LAYER_TYPE_SOFTWARE = View.class.getField("LAYER_TYPE_SOFTWARE").getInt(new View(getContext()));
            setLayerTypeMethod.invoke(this, LAYER_TYPE_SOFTWARE, null);
        } catch (Exception e) {
            Timber.tag("SVGImageView").w(e, "Unexpected failure calling setLayerType");
        }
    }

    private void doRender() {
        if (svg == null)
            return;
        Picture picture = this.svg.renderToPicture(renderOptions);
        setSoftwareLayerType();
        setImageDrawable(new PictureDrawable(picture));
    }


    //===============================================================================================

    @SuppressLint("StaticFieldLeak")
    private class LoadResourceTask extends AsyncTask<Integer, Integer, SVG> {
        private final Context context;

        LoadResourceTask(Context context) {
            this.context = context;
        }

        @SuppressLint("TimberArgCount")
        protected SVG doInBackground(Integer... params) {
            int resourceId = params[0];
            try {
                return SVG.getFromResource(context, resourceId);
            } catch (SVGParseException e) {
                Timber.tag("SVGImageView").e(String.format("Error loading resource 0x%x: %s", resourceId, e.getMessage()));
            }
            return null;
        }

        protected void onPostExecute(SVG svg) {
            SVGImageView.this.svg = svg;
            doRender();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class LoadURITask extends AsyncTask<java.io.InputStream, Integer, SVG> {
        protected SVG doInBackground(InputStream... is) {
            try {
                return SVG.getFromInputStream(is[0]);
            } catch (SVGParseException e) {
                Timber.tag("SVGImageView").e("Parse error loading URI: %s", e.getMessage());
            } finally {
                try {
                    is[0].close();
                } catch (IOException e) { /* do nothing */ }
            }
            return null;
        }

        protected void onPostExecute(SVG svg) {
            SVGImageView.this.svg = svg;
            doRender();
        }
    }

}
