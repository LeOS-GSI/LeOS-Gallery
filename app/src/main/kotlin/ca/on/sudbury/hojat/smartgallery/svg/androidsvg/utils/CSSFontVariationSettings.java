package ca.on.sudbury.hojat.smartgallery.svg.androidsvg.utils;

import androidx.annotation.NonNull;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

public class CSSFontVariationSettings {
    private final HashMap<String, Float> settings;

    private static final String NORMAL = "normal";

    static final String VARIATION_WEIGHT = "wght";
    static final String VARIATION_ITALIC = "ital";
    static final String VARIATION_OBLIQUE = "slnt";
    static final String VARIATION_WIDTH = "wdth";

    static final Float VARIATION_ITALIC_VALUE_ON = 1f;
    static final Float VARIATION_OBLIQUE_VALUE_ON = -14f;  // -14 degrees


    private static class FontVariationEntry {
        String name;
        Float val;

        public FontVariationEntry(String name, Float val) {
            this.name = name;
            this.val = val;
        }
    }


    public CSSFontVariationSettings() {
        this.settings = new HashMap<>();
    }

    private CSSFontVariationSettings(HashMap<String, Float> initialMap) {
        this.settings = initialMap;
    }

    public CSSFontVariationSettings(ca.on.sudbury.hojat.smartgallery.svg.androidsvg.utils.CSSFontVariationSettings other) {
        this.settings = new HashMap<>(other.settings);
    }


    public void addSetting(String key, float value) {
        this.settings.put(key, value);
    }


    public void applySettings(ca.on.sudbury.hojat.smartgallery.svg.androidsvg.utils.CSSFontVariationSettings variationSet) {
        if (variationSet == null)
            return;
        this.settings.putAll(variationSet.settings);
    }


    @NonNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Float> entry : this.settings.entrySet()) {
            if (sb.length() > 0)
                sb.append(',');
            sb.append("'");
            sb.append(entry.getKey());
            sb.append("' ");
            DecimalFormat format = new DecimalFormat("#.##");
            sb.append(format.format(entry.getValue()));
        }
        return sb.toString();
    }


    //-----------------------------------------------------------------------------------------------
    // Parsing font-variation-settings property value

    /*
     * Parse the value of the CSS property "font-variation-settings".
     *
     * Format is: normal | [ <string> <number>]#
     */
    static ca.on.sudbury.hojat.smartgallery.svg.androidsvg.utils.CSSFontVariationSettings parseFontVariationSettings(String val) {
        ca.on.sudbury.hojat.smartgallery.svg.androidsvg.utils.CSSFontVariationSettings result = new ca.on.sudbury.hojat.smartgallery.svg.androidsvg.utils.CSSFontVariationSettings();

        TextScanner scan = new TextScanner(val);
        scan.skipWhitespace();

        if (scan.consume(NORMAL))
            return null;

        while (!scan.empty()) {
            ca.on.sudbury.hojat.smartgallery.svg.androidsvg.utils.CSSFontVariationSettings.FontVariationEntry entry = nextFeatureEntry(scan);
            if (entry == null)
                return null;
            result.settings.put(entry.name, entry.val);
            scan.skipCommaWhitespace();
        }
        return result;
    }


    private static ca.on.sudbury.hojat.smartgallery.svg.androidsvg.utils.CSSFontVariationSettings.FontVariationEntry nextFeatureEntry(TextScanner scan) {
        scan.skipWhitespace();
        String name = scan.nextQuotedString();
        if (name == null || name.length() != 4)
            return null;
        scan.skipWhitespace();
        if (scan.empty())
            return null;
        Float num = scan.nextFloat();
        return new ca.on.sudbury.hojat.smartgallery.svg.androidsvg.utils.CSSFontVariationSettings.FontVariationEntry(name, num);
    }


}
