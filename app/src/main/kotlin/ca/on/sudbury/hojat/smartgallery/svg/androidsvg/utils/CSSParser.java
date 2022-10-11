package ca.on.sudbury.hojat.smartgallery.svg.androidsvg.utils;


import android.annotation.SuppressLint;


import timber.log.Timber;

import androidx.annotation.NonNull;

import ca.on.sudbury.hojat.smartgallery.BuildConfig;
import ca.on.sudbury.hojat.smartgallery.svg.androidsvg.SVGExternalFileResolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;

/*
 * A very simple CSS parser that is not entirely compliant with the CSS spec but
 * hopefully parses almost all the CSS we are likely to strike in an SVG file.
 */
public class CSSParser {
    private static final String TAG = "CSSParser";

    static final String CSS_MIME_TYPE = "text/css";

    static final String ID = "id";
    static final String CLASS = "class";

    private static final int SPECIFICITY_ID_ATTRIBUTE = 1000000;
    private static final int SPECIFICITY_ATTRIBUTE_OR_PSEUDOCLASS = 1000;
    private static final int SPECIFICITY_ELEMENT_OR_PSEUDOELEMENT = 1;

    private CSSParser.MediaType deviceMediaType;
    private CSSParser.Source source;    // Where these rules came from (Parser or RenderOptions)

    private SVGExternalFileResolver externalFileResolver;

    private boolean inMediaRule = false;


    @SuppressWarnings("unused")
    enum MediaType {
        all,
        aural,       // deprecated
        braille,     // deprecated
        embossed,    // deprecated
        handheld,    // deprecated
        print,
        projection,  // deprecated
        screen,
        speech,
        tty,         // deprecated
        tv           // deprecated
    }

    enum Combinator {
        DESCENDANT,  // E F
        CHILD,       // E > F
        FOLLOWS      // E + F
    }

    enum AttribOp {
        EXISTS,     // *[foo]
        EQUALS,     // *[foo=bar]
        INCLUDES,   // *[foo~=bar]
        DASHMATCH,  // *[foo|=bar]
    }

    // Supported SVG attributes
    enum PseudoClassIdents {
        target,
        root,
        nth_child,
        nth_last_child,
        nth_of_type,
        nth_last_of_type,
        first_child,
        last_child,
        first_of_type,
        last_of_type,
        only_child,
        only_of_type,
        empty,
        not,

        // Others from  Selectors 3 (and earlier)
        // Supported but always fail to match.
        lang,  // might support later
        link, visited, hover, active, focus, enabled, disabled, checked, indeterminate,

        // Added in Selectors 4 spec
        // Might support these later
        //matches,
        //something,  // Not final name(?)
        //has,
        //dir,  might support later
        //target_within,
        //blank,

        // Operators from Selectors 4
        // any-link, local-link, scope, focus-visible, focus-within, drop, current, past,
        // future, playing, paused, read-only, read-write, placeholder-shown, default, valid, invalid,
        // in-range, out-of-range, required, optional, user-invalid, nth-col, nth-last-col
        UNSUPPORTED;

        private static final Map<String, CSSParser.PseudoClassIdents> cache = new HashMap<>();

        static {
            for (CSSParser.PseudoClassIdents attr : values()) {
                if (attr != UNSUPPORTED) {
                    final String key = attr.name().replace('_', '-');
                    cache.put(key, attr);
                }
            }
        }

        public static CSSParser.PseudoClassIdents fromString(String str) {
            CSSParser.PseudoClassIdents attr = cache.get(str);
            if (attr != null) {
                return attr;
            }
            return UNSUPPORTED;
        }
    }


    private static class Attrib {
        final public String name;
        final CSSParser.AttribOp operation;
        final public String value;

        Attrib(String name, CSSParser.AttribOp op, String value) {
            this.name = name;
            this.operation = op;
            this.value = value;
        }
    }

    static class SimpleSelector {
        CSSParser.Combinator combinator;
        String tag;       // null means "*"
        List<CSSParser.Attrib> attribs = null;
        List<CSSParser.PseudoClass> pseudos = null;

        SimpleSelector(CSSParser.Combinator combinator, String tag) {
            this.combinator = (combinator != null) ? combinator : CSSParser.Combinator.DESCENDANT;
            this.tag = tag;
        }

        void addAttrib(String attrName, CSSParser.AttribOp op, String attrValue) {
            if (attribs == null)
                attribs = new ArrayList<>();
            attribs.add(new CSSParser.Attrib(attrName, op, attrValue));
        }

        void addPseudo(CSSParser.PseudoClass pseudo) {
            if (pseudos == null)
                pseudos = new ArrayList<>();
            pseudos.add(pseudo);
        }

        @NonNull
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (combinator == CSSParser.Combinator.CHILD)
                sb.append("> ");
            else if (combinator == CSSParser.Combinator.FOLLOWS)
                sb.append("+ ");
            sb.append((tag == null) ? "*" : tag);
            if (attribs != null) {
                for (CSSParser.Attrib attr : attribs) {
                    sb.append('[').append(attr.name);
                    switch (attr.operation) {
                        case EQUALS:
                            sb.append('=').append(attr.value);
                            break;
                        case INCLUDES:
                            sb.append("~=").append(attr.value);
                            break;
                        case DASHMATCH:
                            sb.append("|=").append(attr.value);
                            break;
                        default:
                            break;
                    }
                    sb.append(']');
                }
            }
            if (pseudos != null) {
                for (CSSParser.PseudoClass pseu : pseudos)
                    sb.append(':').append(pseu);
            }
            return sb.toString();
        }
    }

    public static class Ruleset {
        private List<CSSParser.Rule> rules = null;

        // Add a rule to the ruleset. The position at which it is inserted is determined by its specificity value.
        void add(CSSParser.Rule rule) {
            if (this.rules == null)
                this.rules = new LinkedList<>();

            ListIterator<CSSParser.Rule> iter = this.rules.listIterator();
            while (iter.hasNext()) {
                int i = iter.nextIndex();
                CSSParser.Rule nextRule = iter.next();

                if (nextRule.selector.specificity > rule.selector.specificity) {
                    rules.add(i, rule);
                    return;
                }
            }

            rules.add(rule);
        }

        public void addAll(CSSParser.Ruleset rules) {
            if (rules.rules == null)
                return;
            if (this.rules == null)
                this.rules = new LinkedList<>();
            for (CSSParser.Rule rule : rules.rules) {
                this.add(rule);
            }
        }

        public List<CSSParser.Rule> getRules() {
            return this.rules;
        }

        public boolean isEmpty() {
            return this.rules == null || this.rules.isEmpty();
        }

        int ruleCount() {
            return (this.rules != null) ? this.rules.size() : 0;
        }

        /*
         * Remove all rules that were added from a given Source.
         */
        public void removeFromSource(CSSParser.Source sourceToBeRemoved) {
            if (this.rules == null)
                return;
            Iterator<CSSParser.Rule> iter = this.rules.iterator();
            while (iter.hasNext()) {
                if (iter.next().source == sourceToBeRemoved)
                    iter.remove();
            }
        }

        @NonNull
        @Override
        public String toString() {
            if (rules == null)
                return "";
            StringBuilder sb = new StringBuilder();
            for (CSSParser.Rule rule : rules)
                sb.append(rule.toString()).append('\n');
            return sb.toString();
        }
    }


    public enum Source {
        Document,
        RenderOptions
    }


    public static class Rule {
        final CSSParser.Selector selector;
        final Style style;
        final CSSParser.Source source;

        Rule(CSSParser.Selector selector, Style style, CSSParser.Source source) {
            this.selector = selector;
            this.style = style;
            this.source = source;
        }

        @NonNull
        @Override
        public String toString() {
            return selector + " {...} (src=" + this.source + ")";
        }
    }


    static class Selector {
        List<CSSParser.SimpleSelector> simpleSelectors = null;
        int specificity = 0;

        void add(CSSParser.SimpleSelector part) {
            if (this.simpleSelectors == null)
                this.simpleSelectors = new ArrayList<>();
            this.simpleSelectors.add(part);
        }

        int size() {
            return (this.simpleSelectors == null) ? 0 : this.simpleSelectors.size();
        }

        CSSParser.SimpleSelector get(int i) {
            return this.simpleSelectors.get(i);
        }

        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        boolean isEmpty() {
            return (this.simpleSelectors == null) || this.simpleSelectors.isEmpty();
        }

        // Methods for accumulating a specificity value as SimpleSelector entries are added.
        // Number of ID selectors in the simpleSelectors
        void addedIdAttribute() {
            specificity += SPECIFICITY_ID_ATTRIBUTE;
        }

        // Number of class selectors, attributes selectors, and pseudo-classes
        void addedAttributeOrPseudo() {
            specificity += SPECIFICITY_ATTRIBUTE_OR_PSEUDOCLASS;
        }

        // Number of type (element) selectors and pseudo-elements
        void addedElement() {
            specificity += SPECIFICITY_ELEMENT_OR_PSEUDOELEMENT;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (CSSParser.SimpleSelector sel : simpleSelectors)
                sb.append(sel).append(' ');
            return sb.append('[').append(specificity).append(']').toString();
        }
    }


    //===========================================================================================


    CSSParser() {
        this(CSSParser.MediaType.screen, CSSParser.Source.Document, null);
    }


    CSSParser(CSSParser.Source source, SVGExternalFileResolver externalFileResolver) {
        this(CSSParser.MediaType.screen, source, externalFileResolver);
    }


    CSSParser(CSSParser.MediaType rendererMediaType, CSSParser.Source source, SVGExternalFileResolver externalFileResolver) {
        this.deviceMediaType = rendererMediaType;
        this.source = source;
        this.externalFileResolver = externalFileResolver;
    }


    CSSParser.Ruleset parse(String sheet) {
        CSSTextScanner scan = new CSSTextScanner(sheet);
        scan.skipWhitespace();

        return parseRuleset(scan);
    }


    static boolean mediaMatches(String mediaListStr, CSSParser.MediaType rendererMediaType) {
        CSSTextScanner scan = new CSSTextScanner(mediaListStr);
        scan.skipWhitespace();
        List<CSSParser.MediaType> mediaList = parseMediaList(scan);
        return mediaMatches(mediaList, rendererMediaType);
    }


    //==============================================================================


    @SuppressLint("TimberArgCount")
    private static void warn(Object... args) {
        Timber.tag(TAG).w(String.format("Ignoring @%s rule", args));
    }


   /*
   private static void  error(String format, Object... args)
   {
      Log.e(TAG, String.format(format, args));
   }


   private static void  debug(String format, Object... args)
   {
      if (LibConfig.DEBUG)
         Log.d(TAG, String.format(format, args));
   }
   */


    //==============================================================================


    // Returns true if 'deviceMediaType' matches one of the media types in 'mediaList'
    private static boolean mediaMatches(List<CSSParser.MediaType> mediaList, CSSParser.MediaType rendererMediaType) {
        if (mediaList.size() == 0) // No specific media specified, so match all
            return true;
        for (CSSParser.MediaType type : mediaList) {
            if (type == CSSParser.MediaType.all || type == rendererMediaType)
                return true;
        }
        return false;
    }


    private static List<CSSParser.MediaType> parseMediaList(CSSTextScanner scan) {
        ArrayList<CSSParser.MediaType> typeList = new ArrayList<>();
        while (!scan.empty()) {
            String type = scan.nextWord();
            if (type == null)
                break;
            try {
                typeList.add(CSSParser.MediaType.valueOf(type));
            } catch (IllegalArgumentException e) {
                // Ignore invalid media types
            }
            // If there is a comma, keep looping, otherwise break
            if (!scan.skipCommaWhitespace())
                break;
        }
        return typeList;
    }


    private void parseAtRule(CSSParser.Ruleset ruleset, CSSTextScanner scan) throws CSSParseException {
        String atKeyword = scan.nextIdentifier();
        scan.skipWhitespace();
        if (atKeyword == null)
            throw new CSSParseException("Invalid '@' rule");
        if (!inMediaRule && atKeyword.equals("media")) {
            List<CSSParser.MediaType> mediaList = parseMediaList(scan);
            if (!scan.consume('{'))
                throw new CSSParseException("Invalid @media rule: missing rule set");

            scan.skipWhitespace();
            if (mediaMatches(mediaList, deviceMediaType)) {
                inMediaRule = true;
                ruleset.addAll(parseRuleset(scan));
                inMediaRule = false;
            } else {
                parseRuleset(scan);  // parse and ignore accompanying ruleset
            }

            if (!scan.empty() && !scan.consume('}'))
                throw new CSSParseException("Invalid @media rule: expected '}' at end of rule set");

        } else if (!inMediaRule && atKeyword.equals("import")) {
            String file = scan.nextURL();
            if (file == null)
                file = scan.nextCSSString();
            if (file == null)
                throw new CSSParseException("Invalid @import rule: expected string or url()");

            scan.skipWhitespace();
            List<CSSParser.MediaType> mediaList = parseMediaList(scan);

            if (!scan.empty() && !scan.consume(';'))
                throw new CSSParseException("Invalid @media rule: expected '}' at end of rule set");

            if (externalFileResolver != null && mediaMatches(mediaList, deviceMediaType)) {
                String css = externalFileResolver.resolveCSSStyleSheet(file);
                if (css == null)
                    return;
                ruleset.addAll(parse(css));
            }
        }
        //} else if (atKeyword.equals("charset")) {
        else {
            // Unknown/unsupported at-rule
            warn(atKeyword);
            skipAtRule(scan);
        }
        scan.skipWhitespace();
    }


    // Skip an unsupported at-rule: "ignore everything up to and including the next semicolon or block".
    private void skipAtRule(CSSTextScanner scan) {
        int depth = 0;
        while (!scan.empty()) {
            int ch = scan.nextChar();
            if (ch == ';' && depth == 0)
                return;
            if (ch == '{')
                depth++;
            else if (ch == '}' && depth > 0) {
                if (--depth == 0)
                    return;
            }
        }
    }


    private CSSParser.Ruleset parseRuleset(CSSTextScanner scan) {
        CSSParser.Ruleset ruleset = new CSSParser.Ruleset();
        try {
            while (!scan.empty()) {
                if (scan.consume("<!--"))
                    continue;
                if (scan.consume("-->"))
                    continue;

                if (scan.consume('@')) {
                    parseAtRule(ruleset, scan);
                    continue;
                }
                if (parseRule(ruleset, scan))
                    continue;

                // Nothing recognisable found. Could be end of rule set. Return.
                break;
            }
        } catch (CSSParseException e) {
            timber.log.Timber.tag(TAG).e("CSS parser terminated early due to error: %s", e.getMessage());
            if (BuildConfig.DEBUG)
                timber.log.Timber.tag(TAG).e(e, "Stacktrace:");
        }
        return ruleset;
    }


    private boolean parseRule(CSSParser.Ruleset ruleset, CSSTextScanner scan) throws CSSParseException {
        List<CSSParser.Selector> selectors = scan.nextSelectorGroup();
        if (selectors != null && !selectors.isEmpty()) {
            if (!scan.consume('{'))
                throw new CSSParseException("Malformed rule block: expected '{'");
            scan.skipWhitespace();
            Style ruleStyle = parseDeclarations(scan);
            scan.skipWhitespace();
            for (CSSParser.Selector selector : selectors) {
                ruleset.add(new CSSParser.Rule(selector, ruleStyle, source));
            }
            return true;
        } else {
            return false;
        }
    }


    // Parse a list of CSS declarations
    private Style parseDeclarations(CSSTextScanner scan) throws CSSParseException {
        Style ruleStyle = new Style();
        do {
            String propertyName = scan.nextIdentifier();
            scan.skipWhitespace();
            if (!scan.consume(':'))
                throw new CSSParseException("Expected ':'");
            scan.skipWhitespace();
            String propertyValue = scan.nextPropertyValue();
            if (propertyValue == null)
                throw new CSSParseException("Expected property value");
            // Check for !important flag.
            scan.skipWhitespace();
            if (scan.consume('!')) {
                scan.skipWhitespace();
                if (!scan.consume("important")) {
                    throw new CSSParseException("Malformed rule set: found unexpected '!'");
                }
                // We don't do anything with these. We just ignore them. TODO
                scan.skipWhitespace();
            }
            scan.consume(';');
            // TODO: support CSS only values such as "inherit"
            Style.processStyleProperty(ruleStyle, propertyName, propertyValue, false);
            scan.skipWhitespace();
        } while (!scan.empty() && !scan.consume('}'));
        return ruleStyle;
    }


    /*
     * Used by SVGParser to parse the "class" attribute.
     * Follows ordered set parser algorithm: https://dom.spec.whatwg.org/#concept-ordered-set-parser
     */
    public static List<String> parseClassAttribute(String val) {
        CSSTextScanner scan = new CSSTextScanner(val);
        List<String> classNameList = null;

        while (!scan.empty()) {
            String className = scan.nextToken();
            if (className == null)
                continue;
            if (classNameList == null)
                classNameList = new ArrayList<>();
            classNameList.add(className);
            scan.skipWhitespace();
        }
        return classNameList;
    }


    //==============================================================================
    // Matching a selector against an object/element


    static class RuleMatchContext {
        SVGBase.SvgElementBase targetElement;    // From RenderOptions.target() and used for the :target selector

        @NonNull
        @Override
        public String toString() {
            if (targetElement != null)
                return String.format("<%s id=\"%s\">", targetElement.getNodeName(), targetElement.id);
            else
                return "";
        }
    }


    /*
     * Used by renderer to check if a CSS rule matches the current element.
     */
    static boolean ruleMatch(CSSParser.RuleMatchContext ruleMatchContext, CSSParser.Selector selector, SVGBase.SvgElementBase obj) {
        // Check the most common case first as a shortcut.
        if (selector.size() == 1)
            return selectorMatch(ruleMatchContext, selector.get(0), obj);

        // Build the list of ancestor objects
        List<SVGBase.SvgContainer> ancestors = new ArrayList<>();
        SVGBase.SvgContainer parent = obj.parent;
        while (parent != null) {
            ancestors.add(parent);
            parent = ((SVGBase.SvgObject) parent).parent;
        }

        Collections.reverse(ancestors);

        // We start at the last part of the simpleSelectors and loop back through the parts
        // Get the next simpleSelectors part
        return ruleMatch(ruleMatchContext, selector, selector.size() - 1, ancestors, ancestors.size() - 1, obj);
    }


    private static boolean ruleMatch(CSSParser.RuleMatchContext ruleMatchContext, CSSParser.Selector selector, int selPartPos, List<SVGBase.SvgContainer> ancestors, int ancestorsPos, SVGBase.SvgElementBase obj) {
        // We start at the last part of the simpleSelectors and loop back through the parts
        // Get the next simpleSelectors part
        CSSParser.SimpleSelector sel = selector.get(selPartPos);
        if (!selectorMatch(ruleMatchContext, sel, obj))
            return false;

        // Selector part matched, check its combinator
        if (sel.combinator == CSSParser.Combinator.DESCENDANT) {
            if (selPartPos == 0)
                return true;
            // Search up the ancestors list for a node that matches the next simpleSelectors
            while (ancestorsPos >= 0) {
                if (ruleMatchOnAncestors(ruleMatchContext, selector, selPartPos - 1, ancestors, ancestorsPos))
                    return true;
                ancestorsPos--;
            }
            return false;
        } else if (sel.combinator == CSSParser.Combinator.CHILD) {
            return ruleMatchOnAncestors(ruleMatchContext, selector, selPartPos - 1, ancestors, ancestorsPos);
        } else //if (sel.combinator == Combinator.FOLLOWS)
        {
            int childPos = getChildPosition(ancestors, ancestorsPos, obj);
            if (childPos <= 0)
                return false;
            SVGBase.SvgElementBase prevSibling = (SVGBase.SvgElementBase) obj.parent.getChildren().get(childPos - 1);
            return ruleMatch(ruleMatchContext, selector, selPartPos - 1, ancestors, ancestorsPos, prevSibling);
        }
    }


    private static boolean ruleMatchOnAncestors(CSSParser.RuleMatchContext ruleMatchContext, CSSParser.Selector selector, int selPartPos, List<SVGBase.SvgContainer> ancestors, int ancestorsPos) {
        CSSParser.SimpleSelector sel = selector.get(selPartPos);
        SVGBase.SvgElementBase obj = (SVGBase.SvgElementBase) ancestors.get(ancestorsPos);

        if (!selectorMatch(ruleMatchContext, sel, obj))
            return false;

        // Selector part matched, check its combinator
        if (sel.combinator == CSSParser.Combinator.DESCENDANT) {
            if (selPartPos == 0)
                return true;
            // Search up the ancestors list for a node that matches the next simpleSelectors
            while (ancestorsPos > 0) {
                if (ruleMatchOnAncestors(ruleMatchContext, selector, selPartPos - 1, ancestors, --ancestorsPos))
                    return true;
            }
            return false;
        } else if (sel.combinator == CSSParser.Combinator.CHILD) {
            return ruleMatchOnAncestors(ruleMatchContext, selector, selPartPos - 1, ancestors, ancestorsPos - 1);
        } else //if (sel.combinator == Combinator.FOLLOWS)
        {
            int childPos = getChildPosition(ancestors, ancestorsPos, obj);
            if (childPos <= 0)
                return false;
            SVGBase.SvgElementBase prevSibling = (SVGBase.SvgElementBase) obj.parent.getChildren().get(childPos - 1);
            return ruleMatch(ruleMatchContext, selector, selPartPos - 1, ancestors, ancestorsPos, prevSibling);
        }
    }


    private static int getChildPosition(List<SVGBase.SvgContainer> ancestors, int ancestorsPos, SVGBase.SvgElementBase obj) {
        if (ancestorsPos < 0)  // Has no parent, so must be only child of document
            return 0;
        if (ancestors.get(ancestorsPos) != obj.parent)  // parent doesn't match, so obj must be an indirect reference (eg. from a <use>)
            return -1;
        int childPos = 0;
        for (SVGBase.SvgObject child : obj.parent.getChildren()) {
            if (child == obj)
                return childPos;
            childPos++;
        }
        return -1;
    }


    private static boolean selectorMatch(CSSParser.RuleMatchContext ruleMatchContext, CSSParser.SimpleSelector sel, SVGBase.SvgElementBase obj) {
        // Check tag name. tag==null means tag is "*" which matches everything.
        if (sel.tag != null && !sel.tag.equals(obj.getNodeName().toLowerCase(Locale.US)))
            return false;

        // If here, then tag part matched

        // Check the attributes
        if (sel.attribs != null) {
            int count = sel.attribs.size();
            for (int i = 0; i < count; i++) {
                CSSParser.Attrib attr = sel.attribs.get(i);
                switch (attr.name) {
                    case ID:
                        if (!attr.value.equals(obj.id))
                            return false;
                        break;
                    case CLASS:
                        if (obj.classNames == null)
                            return false;
                        if (!obj.classNames.contains(attr.value))
                            return false;
                        break;
                    default:
                        // Other attribute simpleSelectors not yet supported
                        return false;
                }
            }
        }

        // Check the pseudo classes
        if (sel.pseudos != null) {
            int count = sel.pseudos.size();
            for (int i = 0; i < count; i++) {
                CSSParser.PseudoClass pseudo = sel.pseudos.get(i);
                if (!pseudo.matches(ruleMatchContext, obj))
                    return false;
            }
        }

        // If w reached this point, the simpleSelectors matched
        return true;
    }


    //==============================================================================


    interface PseudoClass {
        boolean matches(CSSParser.RuleMatchContext ruleMatchContext, SVGBase.SvgElementBase obj);
    }


    static class PseudoClassAnPlusB implements CSSParser.PseudoClass {
        private final int a;
        private final int b;
        private final boolean isFromStart;
        private final boolean isOfType;
        private final String nodeName;  // The node name for when isOfType is true


        PseudoClassAnPlusB(int a, int b, boolean isFromStart, boolean isOfType, String nodeName) {
            this.a = a;
            this.b = b;
            this.isFromStart = isFromStart;
            this.isOfType = isOfType;
            this.nodeName = nodeName;
        }

        @Override
        public boolean matches(CSSParser.RuleMatchContext ruleMatchContext, SVGBase.SvgElementBase obj) {
            // If this is a "*-of-type" pseudoclass, and the node name hasn't been specified,
            // then match true if the element being tested is first of its type
            String nodeNameToCheck = (isOfType && nodeName == null) ? obj.getNodeName() : nodeName;

            // Initialise with correct values for root element
            int childPos = 0;
            int childCount = 1;

            // If this is not the root element, then determine
            // this objects sibling position and total sibling count
            if (obj.parent != null) {
                childCount = 0;
                for (SVGBase.SvgObject node : obj.parent.getChildren()) {
                    SVGBase.SvgElementBase child = (SVGBase.SvgElementBase) node;  // This should be safe. We shouldn't be styling any SvgObject that isn't an element.
                    if (child == obj)
                        childPos = childCount;
                    if (nodeNameToCheck == null || child.getNodeName().equals(nodeNameToCheck))
                        childCount++;   // this is a child of the right type
                }
            }

            childPos = isFromStart ? childPos + 1            // nth-child positions start at 1, not 0
                    : childCount - childPos;  // for nth-last-child() type pseudo classes

            // Check if an + b == childPos.  The test is true for any n >= 0.
            // So rearranging fo n we get: n = (childPos - b) / a
            if (a == 0) {
                // a is zero for pseudo classes like: nth-child(b)
                // So we match if childPos == b
                return childPos == b;
            }
            // Otherwise we match if ((childPos - b) / a) is an integer (modulus is 0) and is >= 0
            return ((childPos - b) % a) == 0 &&
                    //((childPos - b) / a) >= 0;
                    (Integer.signum(childPos - b) == 0 || Integer.signum(childPos - b) == Integer.signum(a));  // Faster equivalent of ((childPos - b) / a) >= 0;
        }

        @NonNull
        @Override
        public String toString() {
            String last = isFromStart ? "" : "last-";
            return isOfType ? String.format(Locale.US, "nth-%schild(%dn%+d of type <%s>)", last, a, b, nodeName)
                    : String.format(Locale.US, "nth-%schild(%dn%+d)", last, a, b);
        }

    }


    static class PseudoClassOnlyChild implements CSSParser.PseudoClass {
        private final boolean isOfType;
        private final String nodeName;  // The node name for when isOfType is true


        public PseudoClassOnlyChild(boolean isOfType, String nodeName) {
            this.isOfType = isOfType;
            this.nodeName = nodeName;
        }

        @Override
        public boolean matches(CSSParser.RuleMatchContext ruleMatchContext, SVGBase.SvgElementBase obj) {
            // If this is a "*-of-type" pseudoclass, and the node name hasn't been specified,
            // then match true if the element being tested is first of its type
            String nodeNameToCheck = (isOfType && nodeName == null) ? obj.getNodeName() : nodeName;

            // Initialise with correct values for root element
            int childCount = 1;

            // If this is not the root element, then determine
            // this objects sibling position and total sibling count
            if (obj.parent != null) {
                childCount = 0;
                for (SVGBase.SvgObject node : obj.parent.getChildren()) {
                    SVGBase.SvgElementBase child = (SVGBase.SvgElementBase) node;  // This should be safe. We shouldn't be styling any SvgObject that isn't an element.
                    if (nodeNameToCheck == null || child.getNodeName().equals(nodeNameToCheck))
                        childCount++;   // this is a child of the right type
                }
            }

            return (childCount == 1);
        }

        @NonNull
        @Override
        public String toString() {
            return isOfType ? String.format("only-of-type <%s>", nodeName)
                    : "only-child";
        }

    }


    static class PseudoClassRoot implements CSSParser.PseudoClass {
        @Override
        public boolean matches(CSSParser.RuleMatchContext ruleMatchContext, SVGBase.SvgElementBase obj) {
            return obj.parent == null;
        }

        @NonNull
        @Override
        public String toString() {
            return "root";
        }

    }


    static class PseudoClassEmpty implements CSSParser.PseudoClass {
        @Override
        public boolean matches(CSSParser.RuleMatchContext ruleMatchContext, SVGBase.SvgElementBase obj) {
            //return (obj.getChildren().length == 0;

            // temp implementation
            if (obj instanceof SVGBase.SvgContainer)
                return ((SVGBase.SvgContainer) obj).getChildren().size() == 0;
            else
                return true;
            // FIXME  all SVG graphics elements can have children, although for now we drop and ignore
            // them. This will be fixed when implement the DOM.  For now return true.
        }

        @NonNull
        @Override
        public String toString() {
            return "empty";
        }

    }


    static class PseudoClassNot implements CSSParser.PseudoClass {
        private final List<CSSParser.Selector> selectorGroup;

        PseudoClassNot(List<CSSParser.Selector> selectorGroup) {
            this.selectorGroup = selectorGroup;
        }

        @Override
        public boolean matches(CSSParser.RuleMatchContext ruleMatchContext, SVGBase.SvgElementBase obj) {
            // If this element matches any of the selectors in the simpleSelectors group
            // provided to not, then :not fails to match.
            for (CSSParser.Selector selector : selectorGroup) {
                if (ruleMatch(ruleMatchContext, selector, obj))
                    return false;
            }
            return true;
        }

        int getSpecificity() {
            // The specificity of :not is the highest specificity of the selectors in its simpleSelectors parameter list
            int highest = Integer.MIN_VALUE;
            for (CSSParser.Selector selector : selectorGroup) {
                if (selector.specificity > highest)
                    highest = selector.specificity;
            }
            return highest;
        }

        @NonNull
        @Override
        public String toString() {
            return "not(" + selectorGroup + ")";
        }

    }


    static class PseudoClassTarget implements CSSParser.PseudoClass {
        @Override
        public boolean matches(CSSParser.RuleMatchContext ruleMatchContext, SVGBase.SvgElementBase obj) {
            if (ruleMatchContext != null)
                return obj == ruleMatchContext.targetElement;
            else
                return false;
        }

        @NonNull
        @Override
        public String toString() {
            return "target";
        }

    }


    static class PseudoClassNotSupported implements CSSParser.PseudoClass {
        private final String clazz;

        PseudoClassNotSupported(String clazz) {
            this.clazz = clazz;
        }

        @Override
        public boolean matches(CSSParser.RuleMatchContext ruleMatchContext, SVGBase.SvgElementBase obj) {
            return false;
        }

        @NonNull
        @Override
        public String toString() {
            return clazz;
        }

    }


}
