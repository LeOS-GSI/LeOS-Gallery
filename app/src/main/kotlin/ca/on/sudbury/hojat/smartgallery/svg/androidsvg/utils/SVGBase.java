package ca.on.sudbury.hojat.smartgallery.svg.androidsvg.utils;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Picture;

import androidx.annotation.NonNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.on.sudbury.hojat.smartgallery.svg.androidsvg.PreserveAspectRatio;
import ca.on.sudbury.hojat.smartgallery.svg.androidsvg.RenderOptions;
import ca.on.sudbury.hojat.smartgallery.svg.androidsvg.SVGExternalFileResolver;
import ca.on.sudbury.hojat.smartgallery.svg.androidsvg.SVGParseException;
import ca.on.sudbury.hojat.smartgallery.svg.androidsvg.utils.CSSParser.Ruleset;

import timber.log.Timber;

/**
 * AndroidSVG is a library for reading, parsing and rendering SVG documents on Android devices.
 * <p>
 * All interaction with AndroidSVG is via this class.
 * <p>
 * Typically, you will call one of the SVG loading and parsing classes then call the renderer,
 * passing it a canvas to draw upon.
 *
 * <h3>Usage summary</h3>
 *
 * <ul>
 * <li>Use one of the static {@code getFromX()} methods to read and parse the SVG file.  They will
 * return an instance of this class.
 * <li>Call one of the {@code renderToX()} methods to render the document.
 * </ul>
 *
 * <h3>Usage example</h3>
 *
 * <pre>
 * {@code
 * SVG.registerExternalFileResolver(myResolver);
 *
 * SVG  svg = SVG.getFromAsset(getContext().getAssets(), svgPath);
 *
 * Bitmap  newBM = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
 * Canvas  bmcanvas = new Canvas(newBM);
 * bmcanvas.drawRGB(255, 255, 255);  // Clear background to white
 *
 * svg.renderToCanvas(bmcanvas);
 * }
 * </pre>
 * <p>
 * For more detailed information on how to use this library, see the documentation at {@code http://code.google.com/p/androidsvg/}
 */

public class SVGBase {
    //static final String  TAG = "SVGBase";

    private static final int DEFAULT_PICTURE_WIDTH = 512;
    private static final int DEFAULT_PICTURE_HEIGHT = 512;

    private static final double SQRT2 = 1.414213562373095;

    // Parser configuration singletons
    // Configures the parser that will be used for the next SVG that gets parsed
    private static SVGExternalFileResolver externalFileResolverSingleton = null;
    private static boolean enableInternalEntitiesSingleton = true;

    // The parser configuration settings that was used for the current instance
    // WIll continue to be used for future parsing by this instance. For example
    // when parsing addition CSS.
    private final SVGExternalFileResolver externalFileResolver;
    private final boolean enableInternalEntities;

    // The root svg element
    private SVGBase.Svg rootElement = null;

    // Metadata
    private String title = "";
    private String desc = "";

    // DPI to use for rendering
    private float renderDPI = 96f;   // default is 96

    // CSS rules
    private final Ruleset cssRules = new Ruleset();

    // Map from id attribute to element
    private final Map<String, SVGBase.SvgElementBase> idToElementMap = new HashMap<>();


    enum Unit {
        px,
        em,
        ex,
        in,
        cm,
        mm,
        pt,
        pc,
        percent
    }


    @SuppressWarnings("unused")
    enum GradientSpread {
        pad,
        reflect,
        repeat
    }


    /* package private */
    SVGBase(boolean enableInternalEntities, SVGExternalFileResolver fileResolver) {
        this.enableInternalEntities = enableInternalEntities;
        this.externalFileResolver = fileResolver;
    }


    /**
     * Read and parse an SVG from the given {@code InputStream}.
     *
     * @param is the input stream from which to read the file.
     * @return an SVG instance on which you can call one of the render methods.
     * @throws SVGParseException if there is an error parsing the document.
     */
    @SuppressWarnings("WeakerAccess")
    public static SVGBase getFromInputStream(InputStream is) throws SVGParseException {
        return createParser().parseStream(is);
    }


    /**
     * Read and parse an SVG from the given {@code String}.
     *
     * @param svg the String instance containing the SVG document.
     * @return an SVG instance on which you can call one of the render methods.
     * @throws SVGParseException if there is an error parsing the document.
     */
    @SuppressWarnings({"WeakerAccess", "unused"})
    public static SVGBase getFromString(String svg) throws SVGParseException {
        return createParser().parseStream(new ByteArrayInputStream(svg.getBytes()));
    }


    /**
     * Read and parse an SVG from the given resource location.
     *
     * @param resources  the set of Resources in which to locate the file.
     * @param resourceId the resource identifier of the SVG document.
     * @return an SVG instance on which you can call one of the render methods.
     * @throws SVGParseException if there is an error parsing the document.
     * @since 1.2.1
     */
    @SuppressWarnings("WeakerAccess")
    public static SVGBase getFromResource(Resources resources, int resourceId) throws SVGParseException {
        InputStream is = resources.openRawResource(resourceId);
        //noinspection TryFinallyCanBeTryWithResources
        try {
            return createParser().parseStream(is);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                // Do nothing
            }
        }
    }


    /**
     * Read and parse an SVG from the assets folder.
     *
     * @param assetManager the AssetManager instance to use when reading the file.
     * @param filename     the filename of the SVG document within assets.
     * @return an SVG instance on which you can call one of the render methods.
     * @throws SVGParseException if there is an error parsing the document.
     * @throws IOException       if there is some IO error while reading the file.
     */
    @SuppressWarnings({"WeakerAccess", "unused"})
    public static SVGBase getFromAsset(AssetManager assetManager, String filename) throws SVGParseException, IOException {
        InputStream is = assetManager.open(filename);
        //noinspection TryFinallyCanBeTryWithResources
        try {
            return createParser().parseStream(is);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                // Do nothing
            }
        }
    }


    /**
     * Parse an SVG path definition from the given {@code String}.
     * <p>
     * {@code
     * Path  path = SVG.parsePath("M 0,0 L 100,100");
     * path.setFillType(Path.FillType.EVEN_ODD);
     * <p>
     * // You could render the path to a Canvas now
     * Paint paint = new Paint();
     * paint.setStyle(Paint.Style.FILL);
     * paint.setColor(Color.RED);
     * canvas.drawPath(path, paint);
     * <p>
     * // Or perform other operations on it
     * RectF bounds = new RectF();
     * path.computeBounds(bounds, false);
     * }
     * <p>
     * Note that this method does not throw any exceptions or return any errors. Per the SVG
     * specification, if there are any errors in the path definition, the valid portion of the
     * path up until the first error is returned.
     *
     * @param pathDefinition an SVG path element definition string
     * @return an Android {@code Path}
     * @since 1.5
     */
    public static android.graphics.Path parsePath(String pathDefinition) {
        SVGBase.PathDefinition pathDef = SVGParserImpl.parsePath(pathDefinition);
        SVGAndroidRenderer.PathConverter pathConv = new SVGAndroidRenderer.PathConverter(pathDef);
        return pathConv.getPath();
    }


    //===============================================================================

    /**
     * Tells the parser whether to allow the expansion of internal entities.
     * An example of a document containing an internal entities is:
     * <p>
     * {@code
     * <!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.0//EN" "http://www.w3.org/TR/2001/REC-SVG-20010904/DTD/svg10.dtd" [
     * <!ENTITY hello "Hello World!">
     * ]>
     * <svg>
     * <text>&hello;</text>
     * </svg>
     * }
     * <p>
     * Entities are useful in some circumstances, but SVG files that use them are quite rare.  Note
     * also that enabling entity expansion makes you vulnerable to the
     * <a href="https://en.wikipedia.org/wiki/Billion_laughs_attack">Billion Laughs Attack</a>
     * <p>
     * Entity expansion is enabled by default.
     *
     * @param enable Set true if you want to enable entity expansion by the parser.
     * @since 1.3
     */
    @SuppressWarnings("unused")
    public static void setInternalEntitiesEnabled(boolean enable) {
        enableInternalEntitiesSingleton = enable;
    }

    /**
     * Indicates whether internal entities were enabled when this SVG was parsed.
     *
     * <p>
     * <em>Note: prior to release 1.5, this was a static method of (@code SVG}.  In 1.5, it was
     * changed to a instance method to coincide with the change making parsing settings thread safe.</em>
     * </p>
     *
     * @return true if internal entity expansion is enabled in the parser
     * @since 1.5
     */
    @SuppressWarnings("unused")
    public boolean isInternalEntitiesEnabled() {
        return enableInternalEntities;
    }


    /**
     * Register an {@link SVGExternalFileResolver} instance that the renderer should use when resolving
     * external references such as images, fonts, and CSS stylesheets.
     *
     * <p>
     * <em>Note: prior to release 1.3, this was an instance method of (@code SVG}.  In 1.3, it was
     * changed to a static method so that users can resolve external references to CSSS files while
     * the SVG is being parsed.</em>
     * </p>
     *
     * @param fileResolver the resolver to use.
     * @since 1.3
     */
    @SuppressWarnings("unused")
    public static void registerExternalFileResolver(SVGExternalFileResolver fileResolver) {
        externalFileResolverSingleton = fileResolver;
    }


    /**
     * De-register the current {@link SVGExternalFileResolver} instance.
     *
     * @since 1.3
     */
    @SuppressWarnings("unused")
    public static void deregisterExternalFileResolver() {
        externalFileResolverSingleton = null;
    }


    /**
     * Get the {@link SVGExternalFileResolver} in effect when this SVG was parsed..
     *
     * @return the current external file resolver instance
     * @since 1.5
     */
    @SuppressWarnings("unused")
    public SVGExternalFileResolver getExternalFileResolver() {
        return externalFileResolver;
    }


    /**
     * Set the DPI (dots-per-inch) value to use when rendering.  The DPI setting is used in the
     * conversion of "physical" units - such an "pt" or "cm" - to pixel values.  The default DPI is 96.
     * <p>
     * You should not normally need to alter the DPI from the default of 96 as recommended by the SVG
     * and CSS specifications.
     *
     * @param dpi the DPI value that the renderer should use.
     */
    @SuppressWarnings({"WeakerAccess", "unused"})
    public void setRenderDPI(float dpi) {
        this.renderDPI = dpi;
    }


    /**
     * Get the current render DPI setting.
     *
     * @return the DPI value
     */
    @SuppressWarnings({"WeakerAccess", "unused"})
    public float getRenderDPI() {
        return renderDPI;
    }


    //===============================================================================
    // SVG document rendering to a Picture object (indirect rendering)


    /**
     * Renders this SVG document to a Picture object.
     * <p>
     * An attempt will be made to determine a suitable initial viewport from the contents of the SVG file.
     * If an appropriate viewport can't be determined, a default viewport of 512x512 will be used.
     *
     * @return a Picture object suitable for later rendering using {@code Canvas.drawPicture()}
     */
    @SuppressWarnings("WeakerAccess")
    public android.graphics.Picture renderToPicture() {
        return renderToPicture(null);
    }


    /**
     * Renders this SVG document to a {@link Picture}.
     *
     * @param widthInPixels  the width of the initial viewport
     * @param heightInPixels the height of the initial viewport
     * @return a Picture object suitable for later rendering using {@link Canvas#drawPicture(Picture)}
     */
    @SuppressWarnings({"WeakerAccess", "unused"})
    public android.graphics.Picture renderToPicture(int widthInPixels, int heightInPixels) {
        return renderToPicture(widthInPixels, heightInPixels, null);
    }


    /**
     * Renders this SVG document to a {@link Picture}.
     *
     * @param renderOptions options that describe how to render this SVG on the Canvas.
     * @return a Picture object suitable for later rendering using {@link Canvas#drawPicture(Picture)}
     * @since 1.3
     */
    @SuppressWarnings({"WeakerAccess", "unused"})
    public android.graphics.Picture renderToPicture(RenderOptionsBase renderOptions) {
        SVGBase.Box viewBox = (renderOptions != null && renderOptions.hasViewBox()) ? renderOptions.viewBox
                : rootElement.viewBox;

        // If a viewPort was supplied in the renderOptions, then use its maxX and maxY as the Picture size
        if (renderOptions != null && renderOptions.hasViewPort()) {
            float w = renderOptions.viewPort.maxX();
            float h = renderOptions.viewPort.maxY();
            return renderToPicture((int) Math.ceil(w), (int) Math.ceil(h), renderOptions);
        } else if (rootElement.width != null && rootElement.width.unit != SVGBase.Unit.percent &&
                rootElement.height != null && rootElement.height.unit != SVGBase.Unit.percent) {
            float w = rootElement.width.floatValue(this.renderDPI);
            float h = rootElement.height.floatValue(this.renderDPI);
            return renderToPicture((int) Math.ceil(w), (int) Math.ceil(h), renderOptions);
        } else if (rootElement.width != null && viewBox != null) {
            // Width and viewBox supplied, but no height
            // Determine the Picture size and initial viewport. See SVG spec section 7.12.
            float w = rootElement.width.floatValue(this.renderDPI);
            float h = w * viewBox.height / viewBox.width;
            return renderToPicture((int) Math.ceil(w), (int) Math.ceil(h), renderOptions);
        } else if (rootElement.height != null && viewBox != null) {
            // Height and viewBox supplied, but no width
            float h = rootElement.height.floatValue(this.renderDPI);
            float w = h * viewBox.width / viewBox.height;
            return renderToPicture((int) Math.ceil(w), (int) Math.ceil(h), renderOptions);
        } else {
            return renderToPicture(DEFAULT_PICTURE_WIDTH, DEFAULT_PICTURE_HEIGHT, renderOptions);
        }
    }


    /**
     * Renders this SVG document to a {@link Picture}.
     *
     * @param widthInPixels  the width of the {@code Picture}
     * @param heightInPixels the height of the {@code Picture}
     * @param renderOptions  options that describe how to render this SVG on the Canvas.
     * @return a Picture object suitable for later rendering using {@link Canvas#drawPicture(Picture)}
     * @since 1.3
     */
    @SuppressWarnings({"WeakerAccess", "unused"})
    public android.graphics.Picture renderToPicture(int widthInPixels, int heightInPixels, RenderOptionsBase renderOptions) {
        android.graphics.Picture picture = new android.graphics.Picture();
        android.graphics.Canvas canvas = picture.beginRecording(widthInPixels, heightInPixels);

        if (renderOptions == null || renderOptions.viewPort == null) {
            renderOptions = (renderOptions == null) ? new RenderOptionsBase() : new RenderOptionsBase(renderOptions);
            renderOptions.viewPort(0f, 0f, (float) widthInPixels, (float) heightInPixels);
        }

        SVGAndroidRenderer renderer = new SVGAndroidRenderer(canvas, this.renderDPI, externalFileResolver);

        renderer.renderDocument(this, renderOptions);

        picture.endRecording();
        return picture;
    }


    /**
     * Renders this SVG document to a {@link Picture} using the specified view defined in the document.
     * <p>
     * A View is an special element in a SVG document that describes a rectangular area in the document.
     * Calling this method with a {@code viewId} will result in the specified view being positioned and scaled
     * to the viewport.  In other words, use {@link #renderToPicture()} to render the whole document, or use this
     * method instead to render just a part of it.
     *
     * @param viewId         the id of a view element in the document that defines which section of the document is to be visible.
     * @param widthInPixels  the width of the initial viewport
     * @param heightInPixels the height of the initial viewport
     * @return a Picture object suitable for later rendering using {@code Canvas.drawPicture()}, or null if the viewId was not found.
     */
    @SuppressWarnings({"WeakerAccess", "unused"})
    public android.graphics.Picture renderViewToPicture(String viewId, int widthInPixels, int heightInPixels) {
        RenderOptions renderOptions = new RenderOptions();
        renderOptions.view(viewId)
                .viewPort(0f, 0f, (float) widthInPixels, (float) heightInPixels);


        android.graphics.Picture picture = new android.graphics.Picture();
        android.graphics.Canvas canvas = picture.beginRecording(widthInPixels, heightInPixels);

        SVGAndroidRenderer renderer = new SVGAndroidRenderer(canvas, this.renderDPI, externalFileResolver);

        renderer.renderDocument(this, renderOptions);

        picture.endRecording();
        return picture;
    }


    //===============================================================================
    // SVG document rendering to a canvas object (direct rendering)


    /**
     * Renders this SVG document to a Canvas object.  The full width and height of the canvas
     * will be used as the viewport into which the document will be rendered.
     *
     * @param canvas the canvas to which the document should be rendered.
     * @since 1.3
     */
    @SuppressWarnings({"WeakerAccess", "unused"})
    public void renderToCanvas(android.graphics.Canvas canvas) {
        renderToCanvas(canvas, (RenderOptions) null);
    }


    /**
     * Renders this SVG document to a Canvas object.
     *
     * @param canvas   the canvas to which the document should be rendered.
     * @param viewPort the bounds of the area on the canvas you want the SVG rendered, or null for the whole canvas.
     */
    @SuppressWarnings({"WeakerAccess", "unused"})
    public void renderToCanvas(android.graphics.Canvas canvas, android.graphics.RectF viewPort) {
        RenderOptions renderOptions = new RenderOptions();

        if (viewPort != null) {
            renderOptions.viewPort(viewPort.left, viewPort.top, viewPort.width(), viewPort.height());
        } else {
            renderOptions.viewPort(0f, 0f, (float) canvas.getWidth(), (float) canvas.getHeight());
        }

        SVGAndroidRenderer renderer = new SVGAndroidRenderer(canvas, this.renderDPI, externalFileResolver);

        renderer.renderDocument(this, renderOptions);
    }


    /**
     * Renders this SVG document to a Canvas object.
     *
     * @param canvas        the canvas to which the document should be rendered.
     * @param renderOptions options that describe how to render this SVG on the Canvas.
     * @since 1.3
     */
    @SuppressWarnings({"WeakerAccess", "unused"})
    public void renderToCanvas(android.graphics.Canvas canvas, RenderOptions renderOptions) {
        if (renderOptions == null)
            renderOptions = new RenderOptions();

        if (!renderOptions.hasViewPort()) {
            renderOptions.viewPort(0f, 0f, (float) canvas.getWidth(), (float) canvas.getHeight());
        }

        SVGAndroidRenderer renderer = new SVGAndroidRenderer(canvas, this.renderDPI, externalFileResolver);

        renderer.renderDocument(this, renderOptions);
    }


    /**
     * Renders this SVG document to a Canvas using the specified view defined in the document.
     * <p>
     * A View is an special element in a SVG documents that describes a rectangular area in the document.
     * Calling this method with a {@code viewId} will result in the specified view being positioned and scaled
     * to the viewport.  In other words, use {@link #renderToPicture()} to render the whole document, or use this
     * method instead to render just a part of it.
     * <p>
     * If the {@code <view>} could not be found, nothing will be drawn.
     *
     * @param viewId the id of a view element in the document that defines which section of the document is to be visible.
     * @param canvas the canvas to which the document should be rendered.
     */
    @SuppressWarnings({"WeakerAccess", "unused"})
    public void renderViewToCanvas(String viewId, android.graphics.Canvas canvas) {
        renderToCanvas(canvas, RenderOptions.create().view(viewId));
    }


    /**
     * Renders this SVG document to a Canvas using the specified view defined in the document.
     * <p>
     * A View is an special element in a SVG documents that describes a rectangular area in the document.
     * Calling this method with a {@code viewId} will result in the specified view being positioned and scaled
     * to the viewport.  In other words, use {@link #renderToPicture()} to render the whole document, or use this
     * method instead to render just a part of it.
     * <p>
     * If the {@code <view>} could not be found, nothing will be drawn.
     *
     * @param viewId   the id of a view element in the document that defines which section of the document is to be visible.
     * @param canvas   the canvas to which the document should be rendered.
     * @param viewPort the bounds of the area on the canvas you want the SVG rendered, or null for the whole canvas.
     */
    @SuppressWarnings({"WeakerAccess", "unused"})
    public void renderViewToCanvas(String viewId, android.graphics.Canvas canvas, android.graphics.RectF viewPort) {
        RenderOptions renderOptions = RenderOptions.create().view(viewId);

        if (viewPort != null) {
            renderOptions.viewPort(viewPort.left, viewPort.top, viewPort.width(), viewPort.height());
        }

        renderToCanvas(canvas, renderOptions);
    }


    //===============================================================================
    // Other document utility API functions


    /**
     * Returns the contents of the {@code <title>} element in the SVG document.
     *
     * @return title contents if available, otherwise an empty string.
     * @throws IllegalArgumentException if there is no current SVG document loaded.
     */
    @SuppressWarnings({"WeakerAccess", "unused"})
    public String getDocumentTitle() {
        if (this.rootElement == null)
            throw new IllegalArgumentException("SVG document is empty");

        return title;
    }


    /**
     * Returns the contents of the {@code <desc>} element in the SVG document.
     *
     * @return desc contents if available, otherwise an empty string.
     * @throws IllegalArgumentException if there is no current SVG document loaded.
     */
    @SuppressWarnings({"WeakerAccess", "unused"})
    public String getDocumentDescription() {
        if (this.rootElement == null)
            throw new IllegalArgumentException("SVG document is empty");

        return desc;
    }


    /**
     * Returns the SVG version number as provided in the root {@code <svg>} tag of the document.
     *
     * @return the version string if declared, otherwise an empty string.
     * @throws IllegalArgumentException if there is no current SVG document loaded.
     */
    @SuppressWarnings({"WeakerAccess", "unused"})
    public String getDocumentSVGVersion() {
        if (this.rootElement == null)
            throw new IllegalArgumentException("SVG document is empty");

        return rootElement.version;
    }


    /**
     * Returns a list of ids for all {@code <view>} elements in this SVG document.
     * <p>
     * The returned view ids could be used when calling and of the {@code renderViewToX()} methods.
     *
     * @return the list of id strings.
     * @throws IllegalArgumentException if there is no current SVG document loaded.
     */
    @SuppressWarnings({"WeakerAccess", "unused"})
    public Set<String> getViewList() {
        if (this.rootElement == null)
            throw new IllegalArgumentException("SVG document is empty");

        List<SVGBase.SvgObject> viewElems = getElementsByTagName();

        Set<String> viewIds = new HashSet<>(viewElems.size());
        for (SVGBase.SvgObject elem : viewElems) {
            SVGBase.View view = (SVGBase.View) elem;
            if (view.id != null)
                viewIds.add(view.id);
            else
                Timber.tag("AndroidSVG").w("getViewList(): found a <view> without an id attribute");
        }
        return viewIds;
    }


    /**
     * Returns the width of the document as specified in the SVG file.
     * <p>
     * If the width in the document is specified in pixels, that value will be returned.
     * If the value is listed with a physical unit such as "cm", then the current
     * {@code RenderDPI} value will be used to convert that value to pixels. If the width
     * is missing, or in a form which can't be converted to pixels, such as "100%" for
     * example, -1 will be returned.
     *
     * @return the width in pixels, or -1 if there is no width available.
     * @throws IllegalArgumentException if there is no current SVG document loaded.
     */
    @SuppressWarnings({"WeakerAccess", "unused"})
    public float getDocumentWidth() {
        if (this.rootElement == null)
            throw new IllegalArgumentException("SVG document is empty");

        return getDocumentDimensions(this.renderDPI).width;
    }


    /**
     * Change the width of the document by altering the "width" attribute
     * of the root {@code <svg>} element.
     *
     * @param pixels The new value of width in pixels.
     * @throws IllegalArgumentException if there is no current SVG document loaded.
     */
    @SuppressWarnings({"WeakerAccess", "unused"})
    public void setDocumentWidth(float pixels) {
        if (this.rootElement == null)
            throw new IllegalArgumentException("SVG document is empty");

        this.rootElement.width = new SVGBase.Length(pixels);
    }


    /**
     * Change the width of the document by altering the "width" attribute
     * of the root {@code <svg>} element.
     *
     * @param value A valid SVG 'length' attribute, such as "100px" or "10cm".
     * @throws SVGParseException        if {@code value} cannot be parsed successfully.
     * @throws IllegalArgumentException if there is no current SVG document loaded.
     */
    @SuppressWarnings({"WeakerAccess", "unused"})
    public void setDocumentWidth(String value) throws SVGParseException {
        if (this.rootElement == null)
            throw new IllegalArgumentException("SVG document is empty");

        this.rootElement.width = SVGParserImpl.parseLength(value);
    }


    /**
     * Returns the height of the document as specified in the SVG file.
     * <p>
     * If the height in the document is specified in pixels, that value will be returned.
     * If the value is listed with a physical unit such as "cm", then the current
     * {@code RenderDPI} value will be used to convert that value to pixels. If the height
     * is missing, or in a form which can't be converted to pixels, such as "100%" for
     * example, -1 will be returned.
     *
     * @return the height in pixels, or -1 if there is no height available.
     * @throws IllegalArgumentException if there is no current SVG document loaded.
     */
    @SuppressWarnings({"WeakerAccess", "unused"})
    public float getDocumentHeight() {
        if (this.rootElement == null)
            throw new IllegalArgumentException("SVG document is empty");

        return getDocumentDimensions(this.renderDPI).height;
    }


    /**
     * Change the height of the document by altering the "height" attribute
     * of the root {@code <svg>} element.
     *
     * @param pixels The new value of height in pixels.
     * @throws IllegalArgumentException if there is no current SVG document loaded.
     */
    @SuppressWarnings({"WeakerAccess", "unused"})
    public void setDocumentHeight(float pixels) {
        if (this.rootElement == null)
            throw new IllegalArgumentException("SVG document is empty");

        this.rootElement.height = new SVGBase.Length(pixels);
    }


    /**
     * Change the height of the document by altering the "height" attribute
     * of the root {@code <svg>} element.
     *
     * @param value A valid SVG 'length' attribute, such as "100px" or "10cm".
     * @throws SVGParseException        if {@code value} cannot be parsed successfully.
     * @throws IllegalArgumentException if there is no current SVG document loaded.
     */
    @SuppressWarnings({"WeakerAccess", "unused"})
    public void setDocumentHeight(String value) throws SVGParseException {
        if (this.rootElement == null)
            throw new IllegalArgumentException("SVG document is empty");

        this.rootElement.height = SVGParserImpl.parseLength(value);
    }


    /**
     * Change the document view box by altering the "viewBox" attribute
     * of the root {@code <svg>} element.
     * <p>
     * The viewBox generally describes the bounding box dimensions of the
     * document contents.  A valid viewBox is necessary if you want the
     * document scaled to fit the canvas or viewport the document is to be
     * rendered into.
     * <p>
     * By setting a viewBox that describes only a portion of the document,
     * you can reproduce the effect of image sprites.
     *
     * @param minX   the left coordinate of the viewBox in pixels
     * @param minY   the top coordinate of the viewBox in pixels.
     * @param width  the width of the viewBox in pixels
     * @param height the height of the viewBox in pixels
     * @throws IllegalArgumentException if there is no current SVG document loaded.
     */
    @SuppressWarnings({"WeakerAccess", "unused"})
    public void setDocumentViewBox(float minX, float minY, float width, float height) {
        if (this.rootElement == null)
            throw new IllegalArgumentException("SVG document is empty");

        this.rootElement.viewBox = new SVGBase.Box(minX, minY, width, height);
    }


    /**
     * Returns the viewBox attribute of the current SVG document.
     *
     * @return the document's viewBox attribute as a {@code android.graphics.RectF} object, or null if not set.
     * @throws IllegalArgumentException if there is no current SVG document loaded.
     */
    @SuppressWarnings({"WeakerAccess", "unused"})
    public android.graphics.RectF getDocumentViewBox() {
        if (this.rootElement == null)
            throw new IllegalArgumentException("SVG document is empty");

        if (this.rootElement.viewBox == null)
            return null;

        return this.rootElement.viewBox.toRectF();
    }


    /**
     * Change the document positioning by altering the "preserveAspectRatio"
     * attribute of the root {@code <svg>} element.  See the
     * documentation for {@link PreserveAspectRatio} for more information
     * on how positioning works.
     *
     * @param preserveAspectRatio the new {@code preserveAspectRatio} setting for the root {@code <svg>} element.
     * @throws IllegalArgumentException if there is no current SVG document loaded.
     */
    @SuppressWarnings({"WeakerAccess", "unused"})
    public void setDocumentPreserveAspectRatio(PreserveAspectRatio preserveAspectRatio) {
        if (this.rootElement == null)
            throw new IllegalArgumentException("SVG document is empty");

        this.rootElement.preserveAspectRatio = preserveAspectRatio;
    }


    /**
     * Return the "preserveAspectRatio" attribute of the root {@code <svg>}
     * element in the form of an {@link PreserveAspectRatio} object.
     *
     * @return the preserveAspectRatio setting of the document's root {@code <svg>} element.
     * @throws IllegalArgumentException if there is no current SVG document loaded.
     */
    @SuppressWarnings({"WeakerAccess", "unused"})
    public PreserveAspectRatio getDocumentPreserveAspectRatio() {
        if (this.rootElement == null)
            throw new IllegalArgumentException("SVG document is empty");

        if (this.rootElement.preserveAspectRatio == null)
            return null;

        return this.rootElement.preserveAspectRatio;
    }


    /**
     * Returns the aspect ratio of the document as a width/height fraction.
     * <p>
     * If the width or height of the document are listed with a physical unit such as "cm",
     * then the current {@code renderDPI} setting will be used to convert that value to pixels.
     * <p>
     * If the width or height cannot be determined, -1 will be returned.
     *
     * @return the aspect ratio as a width/height fraction, or -1 if the ratio cannot be determined.
     * @throws IllegalArgumentException if there is no current SVG document loaded.
     */
    @SuppressWarnings({"WeakerAccess", "unused"})
    public float getDocumentAspectRatio() {
        if (this.rootElement == null)
            throw new IllegalArgumentException("SVG document is empty");

        SVGBase.Length w = this.rootElement.width;
        SVGBase.Length h = this.rootElement.height;

        // If width and height are both specified and are not percentages, aspect ratio is calculated from these (SVG1.1 sect 7.12)
        if (w != null && h != null && w.unit != SVGBase.Unit.percent && h.unit != SVGBase.Unit.percent) {
            if (w.isZero() || h.isZero())
                return -1f;
            return w.floatValue(this.renderDPI) / h.floatValue(this.renderDPI);
        }

        // Otherwise, get the ratio from the viewBox
        if (this.rootElement.viewBox != null && this.rootElement.viewBox.width != 0f && this.rootElement.viewBox.height != 0f) {
            return this.rootElement.viewBox.width / this.rootElement.viewBox.height;
        }

        // Could not determine aspect ratio
        return -1f;
    }


    //===============================================================================


    protected static SVGParser createParser() {
        return new SVGParserImpl().setInternalEntitiesEnabled(enableInternalEntitiesSingleton)
                .setExternalFileResolver(externalFileResolverSingleton);
    }


    //===============================================================================


    public SVGBase.Svg getRootElement() {
        return rootElement;
    }


    void setRootElement(SVGBase.Svg rootElement) {
        this.rootElement = rootElement;
    }


    SVGBase.SvgObject resolveIRI(String iri) {
        if (iri == null)
            return null;

        iri = cssQuotedString(iri);
        if (iri.length() > 1 && iri.startsWith("#")) {
            return getElementById(iri.substring(1));
        }
        return null;
    }


    private String cssQuotedString(String str) {
        if (str.startsWith("\"") && str.endsWith("\"")) {
            // Remove quotes and replace escaped double-quote
            str = str.substring(1, str.length() - 1).replace("\\\"", "\"");
        } else if (str.startsWith("'") && str.endsWith("'")) {
            // Remove quotes and replace escaped single-quote
            str = str.substring(1, str.length() - 1).replace("\\'", "'");
        }
        // Remove escaped newline. Replace escape seq representing newline
        return str.replace("\\\n", "").replace("\\A", "\n");
    }


    private SVGBase.Box getDocumentDimensions(float dpi) {
        SVGBase.Length w = this.rootElement.width;
        SVGBase.Length h = this.rootElement.height;

        if (w == null || w.isZero() || w.unit == SVGBase.Unit.percent || w.unit == SVGBase.Unit.em || w.unit == SVGBase.Unit.ex)
            return new SVGBase.Box(-1, -1, -1, -1);

        float wOut = w.floatValue(dpi);
        float hOut;

        if (h != null) {
            if (h.isZero() || h.unit == SVGBase.Unit.percent || h.unit == SVGBase.Unit.em || h.unit == SVGBase.Unit.ex) {
                return new SVGBase.Box(-1, -1, -1, -1);
            }
            hOut = h.floatValue(dpi);
        } else {
            // height is not specified. SVG spec says this is okay. If there is a viewBox, we use
            // that to calculate the height. Otherwise we set height equal to width.
            if (this.rootElement.viewBox != null) {
                hOut = (wOut * this.rootElement.viewBox.height) / this.rootElement.viewBox.width;
            } else {
                hOut = wOut;
            }
        }
        return new SVGBase.Box(0, 0, wOut, hOut);
    }


    //===============================================================================
    // CSS support methods


    void addCSSRules(Ruleset ruleset) {
        this.cssRules.addAll(ruleset);
    }


    List<CSSParser.Rule> getCSSRules() {
        return this.cssRules.getRules();
    }


    boolean hasCSSRules() {
        return !this.cssRules.isEmpty();
    }


    void clearRenderCSSRules() {
        this.cssRules.removeFromSource(CSSParser.Source.RenderOptions);
    }


    //===============================================================================
    // Object sub-types used in the SVG object tree


    static class Box {
        float minX, minY, width, height;

        Box(float minX, float minY, float width, float height) {
            this.minX = minX;
            this.minY = minY;
            this.width = width;
            this.height = height;
        }

        Box(SVGBase.Box copy) {
            this.minX = copy.minX;
            this.minY = copy.minY;
            this.width = copy.width;
            this.height = copy.height;
        }

        static SVGBase.Box fromLimits(float minX, float minY, float maxX, float maxY) {
            return new SVGBase.Box(minX, minY, maxX - minX, maxY - minY);
        }

        android.graphics.RectF toRectF() {
            return new android.graphics.RectF(minX, minY, maxX(), maxY());
        }

        float maxX() {
            return minX + width;
        }

        float maxY() {
            return minY + height;
        }

        void union(SVGBase.Box other) {
            if (other.minX < minX) minX = other.minX;
            if (other.minY < minY) minY = other.minY;
            if (other.maxX() > maxX()) width = other.maxX() - minX;
            if (other.maxY() > maxY()) height = other.maxY() - minY;
        }

        @NonNull
        public String toString() {
            return "[" + minX + " " + minY + " " + width + " " + height + "]";
        }
    }


    // What fill or stroke is
    public abstract static class SvgPaint implements Cloneable {
    }


    public static class Colour extends SVGBase.SvgPaint {
        final int colour;

        static final SVGBase.Colour BLACK = new SVGBase.Colour(0xff000000);  // Black singleton - a common default value.
        static final SVGBase.Colour TRANSPARENT = new SVGBase.Colour(0);     // Transparent black

        Colour(int val) {
            this.colour = val;
        }

        @NonNull
        public String toString() {
            return String.format("#%08x", colour);
        }
    }


    // Special version of Colour that indicates use of 'currentColor' keyword
    static class CurrentColor extends SVGBase.SvgPaint {
        private final static SVGBase.CurrentColor instance = new SVGBase.CurrentColor();

        private CurrentColor() {
        }

        static SVGBase.CurrentColor getInstance() {
            return instance;
        }
    }


    static class PaintReference extends SVGBase.SvgPaint {
        final String href;
        final SVGBase.SvgPaint fallback;

        PaintReference(String href, SVGBase.SvgPaint fallback) {
            this.href = href;
            this.fallback = fallback;
        }

        @NonNull
        public String toString() {
            return href + " " + fallback;
        }
    }


    public static class Length implements Cloneable {
        final float value;
        final SVGBase.Unit unit;

        final static SVGBase.Length ZERO = new SVGBase.Length(0f);
        final static SVGBase.Length PERCENT_100 = new SVGBase.Length(100f, SVGBase.Unit.percent);

        public Length(float value, SVGBase.Unit unit) {
            this.value = value;
            this.unit = unit;
        }

        public Length(float value) {
            this.value = value;
            this.unit = SVGBase.Unit.px;
        }

        float floatValue() {
            return value;
        }

        // Convert length to user units for a horizontally-related context.
        float floatValueX(SVGAndroidRenderer renderer) {
            switch (unit) {
                case em:
                    return value * renderer.getCurrentFontSize();
                case ex:
                    return value * renderer.getCurrentFontXHeight();
                case in:
                    return value * renderer.getDPI();
                case cm:
                    return value * renderer.getDPI() / 2.54f;
                case mm:
                    return value * renderer.getDPI() / 25.4f;
                case pt: // 1 point = 1/72 in
                    return value * renderer.getDPI() / 72f;
                case pc: // 1 pica = 1/6 in
                    return value * renderer.getDPI() / 6f;
                case percent:
                    SVGBase.Box viewPortUser = renderer.getEffectiveViewPortInUserUnits();
                    if (viewPortUser == null)
                        return value;  // Undefined in this situation - so just return value to avoid an NPE
                    return value * viewPortUser.width / 100f;
                case px:
                default:
                    return value;
            }
        }

        // Convert length to user units for a vertically-related context.
        float floatValueY(SVGAndroidRenderer renderer) {
            if (unit == SVGBase.Unit.percent) {
                SVGBase.Box viewPortUser = renderer.getEffectiveViewPortInUserUnits();
                if (viewPortUser == null)
                    return value;  // Undefined in this situation - so just return value to avoid an NPE
                return value * viewPortUser.height / 100f;
            }
            return floatValueX(renderer);
        }

        // Convert length to user units for a context that is not orientation specific.
        // For example, stroke width.
        float floatValue(SVGAndroidRenderer renderer) {
            if (unit == SVGBase.Unit.percent) {
                SVGBase.Box viewPortUser = renderer.getEffectiveViewPortInUserUnits();
                if (viewPortUser == null)
                    return value;  // Undefined in this situation - so just return value to avoid an NPE
                float w = viewPortUser.width;
                float h = viewPortUser.height;
                if (w == h)
                    return value * w / 100f;
                float n = (float) (Math.sqrt(w * w + h * h) / SQRT2);  // see spec section 7.10
                return value * n / 100f;
            }
            return floatValueX(renderer);
        }

        // Convert length to user units for a context that is not orientation specific.
        // For percentage values, use the given 'max' parameter to represent the 100% value.
        float floatValue(SVGAndroidRenderer renderer, float max) {
            if (unit == SVGBase.Unit.percent) {
                return value * max / 100f;
            }
            return floatValueX(renderer);
        }

        // For situations (like calculating the initial viewport) when we can only rely on
        // physical real world units.
        float floatValue(float dpi) {
            switch (unit) {
                case in:
                    return value * dpi;
                case cm:
                    return value * dpi / 2.54f;
                case mm:
                    return value * dpi / 25.4f;
                case pt: // 1 point = 1/72 in
                    return value * dpi / 72f;
                case pc: // 1 pica = 1/6 in
                    return value * dpi / 6f;
                case px:
                case em:
                case ex:
                case percent:
                default:
                    return value;
            }
        }

        boolean isZero() {
            return value == 0f;
        }

        boolean isNegative() {
            return value < 0f;
        }

        @NonNull
        @Override
        public String toString() {
            return String.valueOf(value) + unit;
        }
    }


    public static class CSSClipRect {
        final SVGBase.Length top;
        final SVGBase.Length right;
        final SVGBase.Length bottom;
        final SVGBase.Length left;

        CSSClipRect(SVGBase.Length top, SVGBase.Length right, SVGBase.Length bottom, SVGBase.Length left) {
            this.top = top;
            this.right = right;
            this.bottom = bottom;
            this.left = left;
        }
    }


    //===============================================================================
    // The objects in the SVG object tree
    //===============================================================================


    // Any object that can be part of the tree
    public static class SvgObject {
        SVGBase document;
        SVGBase.SvgContainer parent;

        String getNodeName() {
            return "";
        }
    }


    // Any object in the tree that corresponds to an SVG element
    static abstract class SvgElementBase extends SVGBase.SvgObject {
        String id = null;
        Boolean spacePreserve = null;
        Style baseStyle = null;   // style defined by explicit style attributes in the element (eg. fill="black")
        Style style = null;       // style expressed in a 'style' attribute (eg. style="fill:black")
        List<String> classNames = null;  // contents of the 'class' attribute

        @NonNull
        public String toString() {
            return this.getNodeName();
        }
    }


    // Any object in the tree that corresponds to an SVG element
    static abstract class SvgElement extends SVGBase.SvgElementBase {
        SVGBase.Box boundingBox = null;
    }


    // Any element that can appear inside a <switch> element.
    interface SvgConditional {
        void setRequiredFeatures(Set<String> features);

        Set<String> getRequiredFeatures();

        void setRequiredExtensions(String extensions);

        String getRequiredExtensions();

        void setSystemLanguage(Set<String> languages);

        Set<String> getSystemLanguage();

        void setRequiredFormats(Set<String> mimeTypes);

        Set<String> getRequiredFormats();

        void setRequiredFonts(Set<String> fontNames);

        Set<String> getRequiredFonts();
    }


    // Any element that can appear inside a <switch> element.
    static abstract class SvgConditionalElement extends SVGBase.SvgElement implements SVGBase.SvgConditional {
        Set<String> requiredFeatures = null;
        String requiredExtensions = null;
        Set<String> systemLanguage = null;
        Set<String> requiredFormats = null;
        Set<String> requiredFonts = null;

        @Override
        public void setRequiredFeatures(Set<String> features) {
            this.requiredFeatures = features;
        }

        @Override
        public Set<String> getRequiredFeatures() {
            return this.requiredFeatures;
        }

        @Override
        public void setRequiredExtensions(String extensions) {
            this.requiredExtensions = extensions;
        }

        @Override
        public String getRequiredExtensions() {
            return this.requiredExtensions;
        }

        @Override
        public void setSystemLanguage(Set<String> languages) {
            this.systemLanguage = languages;
        }

        @Override
        public Set<String> getSystemLanguage() {
            return this.systemLanguage;
        }

        @Override
        public void setRequiredFormats(Set<String> mimeTypes) {
            this.requiredFormats = mimeTypes;
        }

        @Override
        public Set<String> getRequiredFormats() {
            return this.requiredFormats;
        }

        @Override
        public void setRequiredFonts(Set<String> fontNames) {
            this.requiredFonts = fontNames;
        }

        @Override
        public Set<String> getRequiredFonts() {
            return this.requiredFonts;
        }
    }


    public interface SvgContainer {
        List<SVGBase.SvgObject> getChildren();

        void addChild(SVGBase.SvgObject elem) throws SVGParseException;
    }


    public static abstract class SvgConditionalContainer extends SVGBase.SvgElement implements SVGBase.SvgContainer, SVGBase.SvgConditional {
        List<SVGBase.SvgObject> children = new ArrayList<>();

        Set<String> requiredFeatures = null;
        String requiredExtensions = null;
        Set<String> systemLanguage = null;
        Set<String> requiredFormats = null;
        Set<String> requiredFonts = null;

        @Override
        public List<SVGBase.SvgObject> getChildren() {
            return children;
        }

        @Override
        public void addChild(SVGBase.SvgObject elem) throws SVGParseException {
            children.add(elem);
        }

        @Override
        public void setRequiredFeatures(Set<String> features) {
            this.requiredFeatures = features;
        }

        @Override
        public Set<String> getRequiredFeatures() {
            return this.requiredFeatures;
        }

        @Override
        public void setRequiredExtensions(String extensions) {
            this.requiredExtensions = extensions;
        }

        @Override
        public String getRequiredExtensions() {
            return this.requiredExtensions;
        }

        @Override
        public void setSystemLanguage(Set<String> languages) {
            this.systemLanguage = languages;
        }

        @Override
        public Set<String> getSystemLanguage() {
            return null;
        }

        @Override
        public void setRequiredFormats(Set<String> mimeTypes) {
            this.requiredFormats = mimeTypes;
        }

        @Override
        public Set<String> getRequiredFormats() {
            return this.requiredFormats;
        }

        @Override
        public void setRequiredFonts(Set<String> fontNames) {
            this.requiredFonts = fontNames;
        }

        @Override
        public Set<String> getRequiredFonts() {
            return this.requiredFonts;
        }
    }


    interface HasTransform {
        void setTransform(android.graphics.Matrix matrix);
    }


    static abstract class SvgPreserveAspectRatioContainer extends SVGBase.SvgConditionalContainer {
        PreserveAspectRatio preserveAspectRatio = null;
    }


    static abstract class SvgViewBoxContainer extends SVGBase.SvgPreserveAspectRatioContainer {
        SVGBase.Box viewBox;
    }


    public static class Svg extends SVGBase.SvgViewBoxContainer {
        SVGBase.Length x;
        SVGBase.Length y;
        SVGBase.Length width;
        SVGBase.Length height;
        public String version;

        @Override
        String getNodeName() {
            return "svg";
        }
    }


    // An SVG element that can contain other elements.
    static class Group extends SVGBase.SvgConditionalContainer implements SVGBase.HasTransform {
        android.graphics.Matrix transform;

        @Override
        public void setTransform(android.graphics.Matrix transform) {
            this.transform = transform;
        }

        @Override
        String getNodeName() {
            return "group";
        }
    }


    interface NotDirectlyRendered {
    }


    // A <defs> object contains objects that are not rendered directly, but are instead
    // referenced from other parts of the file.
    static class Defs extends SVGBase.Group implements SVGBase.NotDirectlyRendered {
        @Override
        String getNodeName() {
            return "defs";
        }
    }


    // One of the element types that can cause graphics to be drawn onto the target canvas.
    // Specifically: 'circle', 'ellipse', 'image', 'line', 'path', 'polygon', 'polyline', 'rect', 'text' and 'use'.
    static abstract class GraphicsElement extends SVGBase.SvgConditionalElement implements SVGBase.HasTransform {
        android.graphics.Matrix transform;

        @Override
        public void setTransform(android.graphics.Matrix transform) {
            this.transform = transform;
        }
    }


    // A linking element (we don't currently do anything with this. It is basically just treated like a Group.
    static class A extends SVGBase.Group {
        String href;

        @Override
        String getNodeName() {
            return "a";
        }
    }


    static class Use extends SVGBase.Group {
        String href;
        SVGBase.Length x;
        SVGBase.Length y;
        SVGBase.Length width;
        SVGBase.Length height;

        @Override
        String getNodeName() {
            return "use";
        }
    }


    static class Path extends SVGBase.GraphicsElement {
        SVGBase.PathDefinition d;
        Float pathLength;

        @Override
        String getNodeName() {
            return "path";
        }
    }


    static class Rect extends SVGBase.GraphicsElement {
        SVGBase.Length x;
        SVGBase.Length y;
        SVGBase.Length width;
        SVGBase.Length height;
        SVGBase.Length rx;
        SVGBase.Length ry;

        @Override
        String getNodeName() {
            return "rect";
        }
    }


    static class Circle extends SVGBase.GraphicsElement {
        SVGBase.Length cx;
        SVGBase.Length cy;
        SVGBase.Length r;

        @Override
        String getNodeName() {
            return "circle";
        }
    }


    static class Ellipse extends SVGBase.GraphicsElement {
        SVGBase.Length cx;
        SVGBase.Length cy;
        SVGBase.Length rx;
        SVGBase.Length ry;

        @Override
        String getNodeName() {
            return "ellipse";
        }
    }


    static class Line extends SVGBase.GraphicsElement {
        SVGBase.Length x1;
        SVGBase.Length y1;
        SVGBase.Length x2;
        SVGBase.Length y2;

        @Override
        String getNodeName() {
            return "line";
        }
    }


    static class PolyLine extends SVGBase.GraphicsElement {
        float[] points;

        @Override
        String getNodeName() {
            return "polyline";
        }
    }


    static class Polygon extends SVGBase.PolyLine {
        @Override
        String getNodeName() {
            return "polygon";
        }
    }


    // A root text container such as <text> or <textPath>
    interface TextRoot {
    }


    interface TextChild {
        void setTextRoot(SVGBase.TextRoot obj);

        SVGBase.TextRoot getTextRoot();
    }


    public static abstract class TextContainer extends SVGBase.SvgConditionalContainer {
        @Override
        public void addChild(SVGBase.SvgObject elem) throws SVGParseException {
            if (elem instanceof SVGBase.TextChild)
                children.add(elem);
            else
                throw new SVGParseException("Text content elements cannot contain " + elem + " elements.");
        }
    }


    static abstract class TextPositionedContainer extends SVGBase.TextContainer {
        List<SVGBase.Length> x;
        List<SVGBase.Length> y;
        List<SVGBase.Length> dx;
        List<SVGBase.Length> dy;
    }


    static class Text extends SVGBase.TextPositionedContainer implements SVGBase.TextRoot, SVGBase.HasTransform {
        android.graphics.Matrix transform;

        @Override
        public void setTransform(android.graphics.Matrix transform) {
            this.transform = transform;
        }

        @Override
        String getNodeName() {
            return "text";
        }
    }


    static class TSpan extends SVGBase.TextPositionedContainer implements SVGBase.TextChild {
        private SVGBase.TextRoot textRoot;

        @Override
        public void setTextRoot(SVGBase.TextRoot obj) {
            this.textRoot = obj;
        }

        @Override
        public SVGBase.TextRoot getTextRoot() {
            return this.textRoot;
        }

        @Override
        String getNodeName() {
            return "tspan";
        }
    }


    public static class TextSequence extends SVGBase.SvgObject implements SVGBase.TextChild {
        String text;

        private SVGBase.TextRoot textRoot;

        public TextSequence(String text) {
            this.text = text;
        }

        @NonNull
        public String toString() {
            return "TextChild: '" + text + "'";
        }

        @Override
        public void setTextRoot(SVGBase.TextRoot obj) {
            this.textRoot = obj;
        }

        @Override
        public SVGBase.TextRoot getTextRoot() {
            return this.textRoot;
        }
    }


    static class TRef extends SVGBase.TextContainer implements SVGBase.TextChild {
        String href;

        private SVGBase.TextRoot textRoot;

        @Override
        public void setTextRoot(SVGBase.TextRoot obj) {
            this.textRoot = obj;
        }

        @Override
        public SVGBase.TextRoot getTextRoot() {
            return this.textRoot;
        }

        @Override
        String getNodeName() {
            return "tref";
        }
    }


    static class TextPath extends SVGBase.TextContainer implements SVGBase.TextChild {
        String href;
        SVGBase.Length startOffset;

        private SVGBase.TextRoot textRoot;

        @Override
        public void setTextRoot(SVGBase.TextRoot obj) {
            this.textRoot = obj;
        }

        @Override
        public SVGBase.TextRoot getTextRoot() {
            return this.textRoot;
        }

        @Override
        String getNodeName() {
            return "textPath";
        }
    }


    // An SVG element that can contain other elements.
    static class Switch extends SVGBase.Group {
        @Override
        String getNodeName() {
            return "switch";
        }
    }


    static class Symbol extends SVGBase.SvgViewBoxContainer implements SVGBase.NotDirectlyRendered {
        @Override
        String getNodeName() {
            return "symbol";
        }
    }


    static class Marker extends SVGBase.SvgViewBoxContainer implements SVGBase.NotDirectlyRendered {
        boolean markerUnitsAreUser;
        SVGBase.Length refX;
        SVGBase.Length refY;
        SVGBase.Length markerWidth;
        SVGBase.Length markerHeight;
        Float orient;

        @Override
        String getNodeName() {
            return "marker";
        }
    }


    static abstract class GradientElement extends SVGBase.SvgElementBase implements SVGBase.SvgContainer {
        List<SVGBase.SvgObject> children = new ArrayList<>();

        Boolean gradientUnitsAreUser;
        android.graphics.Matrix gradientTransform;
        SVGBase.GradientSpread spreadMethod;
        String href;

        @Override
        public List<SVGBase.SvgObject> getChildren() {
            return children;
        }

        @Override
        public void addChild(SVGBase.SvgObject elem) throws SVGParseException {
            if (elem instanceof SVGBase.Stop)
                children.add(elem);
            else
                throw new SVGParseException("Gradient elements cannot contain " + elem + " elements.");
        }
    }


    static class Stop extends SVGBase.SvgElementBase implements SVGBase.SvgContainer {
        Float offset;

        // Dummy container methods. Stop is officially a container, but we
        // are not interested in any of its possible child elements.
        @Override
        public List<SVGBase.SvgObject> getChildren() {
            return Collections.emptyList();
        }

        @Override
        public void addChild(SVGBase.SvgObject elem) { /* do nothing */ }

        @Override
        String getNodeName() {
            return "stop";
        }
    }


    static class SvgLinearGradient extends SVGBase.GradientElement {
        SVGBase.Length x1;
        SVGBase.Length y1;
        SVGBase.Length x2;
        SVGBase.Length y2;

        @Override
        String getNodeName() {
            return "linearGradient";
        }
    }


    static class SvgRadialGradient extends SVGBase.GradientElement {
        SVGBase.Length cx;
        SVGBase.Length cy;
        SVGBase.Length r;
        SVGBase.Length fx;
        SVGBase.Length fy;
        SVGBase.Length fr;

        @Override
        String getNodeName() {
            return "radialGradient";
        }
    }


    static class ClipPath extends SVGBase.Group implements SVGBase.NotDirectlyRendered {
        static final String NODE_NAME = "clipPath";

        Boolean clipPathUnitsAreUser;

        @Override
        String getNodeName() {
            return NODE_NAME;
        }
    }


    static class Pattern extends SVGBase.SvgViewBoxContainer implements SVGBase.NotDirectlyRendered {
        Boolean patternUnitsAreUser;
        Boolean patternContentUnitsAreUser;
        android.graphics.Matrix patternTransform;
        SVGBase.Length x;
        SVGBase.Length y;
        SVGBase.Length width;
        SVGBase.Length height;
        String href;

        @Override
        String getNodeName() {
            return "pattern";
        }
    }


    static class Image extends SVGBase.SvgPreserveAspectRatioContainer implements SVGBase.HasTransform {
        String href;
        SVGBase.Length x;
        SVGBase.Length y;
        SVGBase.Length width;
        SVGBase.Length height;
        android.graphics.Matrix transform;

        @Override
        public void setTransform(android.graphics.Matrix transform) {
            this.transform = transform;
        }

        @Override
        String getNodeName() {
            return "image";
        }
    }


    static class View extends SVGBase.SvgViewBoxContainer implements SVGBase.NotDirectlyRendered {
        static final String NODE_NAME = "view";

        @Override
        String getNodeName() {
            return NODE_NAME;
        }
    }


    static class Mask extends SVGBase.SvgConditionalContainer implements SVGBase.NotDirectlyRendered {
        Boolean maskUnitsAreUser;
        Boolean maskContentUnitsAreUser;
        SVGBase.Length x;
        SVGBase.Length y;
        SVGBase.Length width;
        SVGBase.Length height;

        @Override
        String getNodeName() {
            return "mask";
        }
    }


    static class SolidColor extends SVGBase.SvgElementBase implements SVGBase.SvgContainer {
        // Not needed right now. Colour is set in this.baseStyle.

        // Dummy container methods. Stop is officially a container, but we
        // are not interested in any of its possible child elements.
        @Override
        public List<SVGBase.SvgObject> getChildren() {
            return Collections.emptyList();
        }

        @Override
        public void addChild(SVGBase.SvgObject elem) { /* do nothing */ }

        @Override
        String getNodeName() {
            return "solidColor";
        }
    }


    //===============================================================================
    // Protected setters for internal use


    void setTitle(String title) {
        this.title = title;
    }


    void setDesc(String desc) {
        this.desc = desc;
    }


    //===============================================================================
    // Path definition


    interface PathInterface {
        void moveTo(float x, float y);

        void lineTo(float x, float y);

        void cubicTo(float x1, float y1, float x2, float y2, float x3, float y3);

        void quadTo(float x1, float y1, float x2, float y2);

        void arcTo(float rx, float ry, float xAxisRotation, boolean largeArcFlag, boolean sweepFlag, float x, float y);

        void close();
    }


    static class PathDefinition implements SVGBase.PathInterface {
        private byte[] commands;
        private int commandsLength = 0;
        private float[] coords;
        private int coordsLength = 0;

        private static final byte MOVETO = 0;
        private static final byte LINETO = 1;
        private static final byte CUBICTO = 2;
        private static final byte QUADTO = 3;
        private static final byte ARCTO = 4;   // 4-7
        private static final byte CLOSE = 8;


        PathDefinition() {
            this.commands = new byte[8];
            this.coords = new float[16];
        }


        boolean isEmpty() {
            return commandsLength == 0;
        }


        private void addCommand(byte value) {
            if (commandsLength == commands.length) {
                byte[] newCommands = new byte[commands.length * 2];
                System.arraycopy(commands, 0, newCommands, 0, commands.length);
                commands = newCommands;
            }
            commands[commandsLength++] = value;
        }


        private void coordsEnsure(int num) {
            if (coords.length < (coordsLength + num)) {
                float[] newCoords = new float[coords.length * 2];
                System.arraycopy(coords, 0, newCoords, 0, coords.length);
                coords = newCoords;
            }
        }


        @Override
        public void moveTo(float x, float y) {
            addCommand(MOVETO);
            coordsEnsure(2);
            coords[coordsLength++] = x;
            coords[coordsLength++] = y;
        }


        @Override
        public void lineTo(float x, float y) {
            addCommand(LINETO);
            coordsEnsure(2);
            coords[coordsLength++] = x;
            coords[coordsLength++] = y;
        }


        @Override
        public void cubicTo(float x1, float y1, float x2, float y2, float x3, float y3) {
            addCommand(CUBICTO);
            coordsEnsure(6);
            coords[coordsLength++] = x1;
            coords[coordsLength++] = y1;
            coords[coordsLength++] = x2;
            coords[coordsLength++] = y2;
            coords[coordsLength++] = x3;
            coords[coordsLength++] = y3;
        }


        @Override
        public void quadTo(float x1, float y1, float x2, float y2) {
            addCommand(QUADTO);
            coordsEnsure(4);
            coords[coordsLength++] = x1;
            coords[coordsLength++] = y1;
            coords[coordsLength++] = x2;
            coords[coordsLength++] = y2;
        }


        @Override
        public void arcTo(float rx, float ry, float xAxisRotation, boolean largeArcFlag, boolean sweepFlag, float x, float y) {
            int arc = ARCTO | (largeArcFlag ? 2 : 0) | (sweepFlag ? 1 : 0);
            addCommand((byte) arc);
            coordsEnsure(5);
            coords[coordsLength++] = rx;
            coords[coordsLength++] = ry;
            coords[coordsLength++] = xAxisRotation;
            coords[coordsLength++] = x;
            coords[coordsLength++] = y;
        }


        @Override
        public void close() {
            addCommand(CLOSE);
        }


        void enumeratePath(SVGBase.PathInterface handler) {
            int coordsPos = 0;

            for (int commandPos = 0; commandPos < commandsLength; commandPos++) {
                byte command = commands[commandPos];
                switch (command) {
                    case MOVETO:
                        handler.moveTo(coords[coordsPos++], coords[coordsPos++]);
                        break;
                    case LINETO:
                        handler.lineTo(coords[coordsPos++], coords[coordsPos++]);
                        break;
                    case CUBICTO:
                        handler.cubicTo(coords[coordsPos++], coords[coordsPos++], coords[coordsPos++], coords[coordsPos++], coords[coordsPos++], coords[coordsPos++]);
                        break;
                    case QUADTO:
                        handler.quadTo(coords[coordsPos++], coords[coordsPos++], coords[coordsPos++], coords[coordsPos++]);
                        break;
                    case CLOSE:
                        handler.close();
                        break;
                    default:
                        boolean largeArcFlag = (command & 2) != 0;
                        boolean sweepFlag = (command & 1) != 0;
                        handler.arcTo(coords[coordsPos++], coords[coordsPos++], coords[coordsPos++], largeArcFlag, sweepFlag, coords[coordsPos++], coords[coordsPos++]);
                }
            }
        }

    }


    SVGBase.SvgElementBase getElementById(String id) {
        if (id == null || id.length() == 0)
            return null;
        if (id.equals(rootElement.id))
            return rootElement;

        if (idToElementMap.containsKey(id))
            return idToElementMap.get(id);

        // Search the object tree for a node with id property that matches 'id'
        SVGBase.SvgElementBase result = getElementById(rootElement, id);
        idToElementMap.put(id, result);
        return result;
    }


    private SVGBase.SvgElementBase getElementById(SVGBase.SvgContainer obj, String id) {
        SVGBase.SvgElementBase elem = (SVGBase.SvgElementBase) obj;
        if (id.equals(elem.id))
            return elem;
        for (SVGBase.SvgObject child : obj.getChildren()) {
            if (!(child instanceof SVGBase.SvgElementBase))
                continue;
            SVGBase.SvgElementBase childElem = (SVGBase.SvgElementBase) child;
            if (id.equals(childElem.id))
                return childElem;
            if (child instanceof SVGBase.SvgContainer) {
                SVGBase.SvgElementBase found = getElementById((SVGBase.SvgContainer) child, id);
                if (found != null)
                    return found;
            }
        }
        return null;
    }


    private List<SVGBase.SvgObject> getElementsByTagName() {
        List<SVGBase.SvgObject> result = new ArrayList<>();

        // Search the object tree for nodes with the give element class
        getElementsByTagName(result, rootElement, SVGBase.View.NODE_NAME);
        return result;
    }


    private void getElementsByTagName(List<SVGBase.SvgObject> result, SVGBase.SvgObject obj, String nodeName) {

        if (obj.getNodeName().equals(nodeName))
            result.add(obj);

        if (obj instanceof SVGBase.SvgContainer) {
            for (SVGBase.SvgObject child : ((SVGBase.SvgContainer) obj).getChildren())
                getElementsByTagName(result, child, nodeName);
        }
    }


}
