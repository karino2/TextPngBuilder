package com.livejournal.karino2.textpngbuilder;

import android.os.Bundle;

import java.io.File;
import java.util.List;

/**
 * Created by karino on 1/5/15.
 */
public class TextStyle {
    public TextStyle() {
        this(false, "Verdana, Roboto, sans-serif", 12, null);
    }

    File fontPath;
    public TextStyle(boolean isvertical, String fontFam, int fontSz, File externalFont) {
        vertical = isvertical;
        fontFamily = fontFam;
        fontSize = fontSz;
        fontPath = externalFont;
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


    public String getFontPathString()
    {
        if(fontPath == null)
            return "";
        return fontPath.getAbsolutePath();
    }


    boolean isExternalFontExist() {
        if(fontPath != null &&
                fontPath.exists())
            return true;
        return false;
    }

    public void saveInstanceState(Bundle outState) {
        outState.putBoolean("IS_VERTICAL", vertical);
        outState.putInt("FONT_SIZE", fontSize);
        outState.putString("FONT_FAMILY", fontFamily);
        outState.putString("EXTERNAL_FONT_PATH", getFontPathString());
    }

    public void restoreInstanceState(Bundle savedInstanceState) {
        vertical = savedInstanceState.getBoolean("IS_VERTICAL");
        fontSize = savedInstanceState.getInt("FONT_SIZE");
        fontFamily = savedInstanceState.getString("FONT_FAMILY");
        String fontPathStr = savedInstanceState.getString("EXTERNAL_FONT_PATH");
        if(!fontPathStr.equals(""))
            fontPath = null;
        else
            fontPath = new File(fontPathStr);
    }

    public void setFontPath(File newFontPath) {
        fontPath = newFontPath;
    }

    public String buildHtml(List<String> strings) {
        StringBuilder builder = new StringBuilder();
        builder.append("<html><head><style>");

        if(isExternalFontExist()) {
            // builder.append("@font-face {font-family: EXTERNAL; src:url(\"file:///storage/sdcard0/fonts/ipam.otf\"); }  ");
            builder.append(
                    String.format("@font-face {font-family: EXTERNAL; src:url(\"file://%s\"); }  ", fontPath.getAbsolutePath())
            );
        }

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
        builder.append("</style></head><body>");
        for(String line : strings) {
            builder.append(escapeHtml(line));
            builder.append("<br>");
        }
        builder.append("</body></html>");
        return builder.toString();

    }

    private String escapeHtml(String line) {
        return line.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll(" ", "&nbsp;");
    }

    public void setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
    }
}
