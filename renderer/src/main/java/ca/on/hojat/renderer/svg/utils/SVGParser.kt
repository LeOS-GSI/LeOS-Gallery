package ca.on.hojat.renderer.svg.utils

import ca.on.hojat.renderer.svg.SVGExternalFileResolver
import ca.on.hojat.renderer.svg.SVGParseException
import java.io.InputStream

interface SVGParser {

    /**
     * Try to parse the stream contents to an {@link SVG} instance.
     */
    @Throws(SVGParseException::class)
    fun parseStream(inputStream: InputStream?): SVGBase?

    /**
     * Tells the parser whether to allow the expansion of internal entities.
     * An example of a document containing an internal entities is:
     */
    fun setInternalEntitiesEnabled(enable: Boolean): SVGParser?

    /**
     * Register an [SVGExternalFileResolver] instance that the parser should use when resolving
     * external references such as images, fonts, and CSS stylesheets.
     */
    fun setExternalFileResolver(fileResolver: SVGExternalFileResolver?): SVGParser?
}