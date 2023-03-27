package ca.on.hojat.renderer.svg.utils

/**
 * This is just a link to CSSParser class. As CSSParser is package-protected
 * and we don't want it to leak as a public API, we just gaining access
 * through this inheritance.
 */
open class CSSBase(val css: String) {
    @JvmField
     val cssRuleset: CSSParser.Ruleset? = CSSParser(
        CSSParser.Source.RenderOptions,
        null
    ).parse(css)
}