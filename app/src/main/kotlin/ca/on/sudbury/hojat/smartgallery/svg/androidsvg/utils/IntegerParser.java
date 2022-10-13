package ca.on.sudbury.hojat.smartgallery.svg.androidsvg.utils;

/**
 * Parse a SVG/CSS 'integer' or hex number from a String.
 * <p>
 * We use our own parser to gain a bit of speed.  This routine is
 * around twice as fast as the system one.
 */

class IntegerParser {
    private final int pos;
    private final long value;


    IntegerParser(long value, int pos) {
        this.value = value;
        this.pos = pos;
    }


    /*
     * Return the value of pos after the parse.
     */
    int getEndPos() {
        return this.pos;
    }


    /*
     * Scan the string for an SVG integer.
     * Assumes maxPos will not be greater than input.length().
     */
    static ca.on.sudbury.hojat.smartgallery.svg.androidsvg.utils.IntegerParser parseInt(String input, int startpos, int len) {
        int pos = startpos;
        long value = 0;
        char ch;

        if (pos >= len)
            return null;  // String is empty - no number found

        int sigStart = pos;

        while (pos < len) {
            ch = input.charAt(pos);
            if (ch >= '0' && ch <= '9') {
                value = value * 10 + ((int) ch - (int) '0');
                if (value > Integer.MAX_VALUE)
                    return null;
            } else
                break;
            pos++;
        }

        // Have we seen anything number-ish at all so far?
        if (pos == sigStart) {
            return null;
        }

        return new ca.on.sudbury.hojat.smartgallery.svg.androidsvg.utils.IntegerParser(value, pos);
    }


    /*
     * Return the parsed value as an actual float.
     */
    public int value() {
        return (int) value;
    }


    /*
     * Scan the string for an SVG hex integer.
     * Assumes maxPos will not be greater than input.length().
     */
    static ca.on.sudbury.hojat.smartgallery.svg.androidsvg.utils.IntegerParser parseHex(String input, int len) {
        int pos = 1;
        long value = 0;
        char ch;

        if (pos >= len)
            return null;  // String is empty - no number found

        while (pos < len) {
            ch = input.charAt(pos);
            if (ch >= '0' && ch <= '9') {
                value = value * 16 + ((int) ch - (int) '0');
            } else if (ch >= 'A' && ch <= 'F') {
                value = value * 16 + ((int) ch - (int) 'A') + 10;
            } else if (ch >= 'a' && ch <= 'f') {
                value = value * 16 + ((int) ch - (int) 'a') + 10;
            } else
                break;

            if (value > 0xffffffffL)
                return null;

            pos++;
        }

        // Have we seen anything number-ish at all so far?
        if (pos == 1) {
            return null;
        }

        return new ca.on.sudbury.hojat.smartgallery.svg.androidsvg.utils.IntegerParser(value, pos);
    }

}
