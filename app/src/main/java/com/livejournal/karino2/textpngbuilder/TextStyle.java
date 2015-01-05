package com.livejournal.karino2.textpngbuilder;

import java.util.List;

/**
 * Created by karino on 1/5/15.
 */
public class TextStyle {
    public TextStyle() {
        this(false, "Verdana, Roboto, sans-serif", 12);
    }

    public TextStyle(boolean isvertical, String fontFam, int fontSz) {
        vertical = isvertical;
        fontFamily = fontFam;
        fontSize = fontSz;
    }

    String fontFamily;
    public String getFontFamily()
    {
        return fontFamily;
    }
    int fontSize;
    public int getFontSize()
    {
        return fontSize;
    }

    boolean vertical;
    public boolean isVertical() {
        return vertical;
    }


    public String buildHtml(List<String> strings) {
        StringBuilder builder = new StringBuilder();
        builder.append("<html><head><style>");
        builder.append("body { ");
        if(isVertical())
            builder.append("-webkit-writing-mode: vertical-rl;");

        builder.append("font-size: ");
        builder.append(String.valueOf(getFontSize()));
        builder.append("pt; ");

        // font-family: "Bodoni MT", Didot, "Didot LT STD", "Hoefler Text", Garamond, "Times New Roman", serif;
        builder.append("font-family: ");
        builder.append(getFontFamily());
        builder.append(";");

        builder.append("}");
        builder.append("</style></head><body><pre>");
        for(String line : strings) {
            builder.append(escapeHtml(line));
            builder.append("<br>");
        }
        builder.append("</pre></body></html>");
        return builder.toString();

    }

    private String escapeHtml(String line) {
        return line.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
    }

}
