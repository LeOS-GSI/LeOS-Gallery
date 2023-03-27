package ca.on.hojat.renderer.svg

import org.xml.sax.SAXException

/**
 * Thrown by the parser if a problem is found in the SVG file.
 */
class SVGParseException(msg: String, cause: Exception?) : SAXException(msg, cause) {

    constructor(msg: String) : this(msg, null)
}