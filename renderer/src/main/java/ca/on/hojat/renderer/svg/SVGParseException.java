package ca.on.hojat.renderer.svg;

/**
 * Thrown by the parser if a problem is found in the SVG file.
 */

public class SVGParseException extends org.xml.sax.SAXException {
    public SVGParseException(String msg) {
        super(msg);
    }

    public SVGParseException(String msg, Exception cause) {
        super(msg, cause);
    }
}
