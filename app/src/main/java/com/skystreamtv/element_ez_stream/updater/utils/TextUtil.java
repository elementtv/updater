package com.skystreamtv.element_ez_stream.updater.utils;

import android.support.annotation.Nullable;
import android.text.Html;
import android.text.Spanned;
import android.widget.EditText;

/**
 * Text Utility
 */

public class TextUtil {

    /**
     * Returns false if string is null or empty
     *
     * @param string string to be checked
     * @return false if string is null or empty
     */
    public static boolean isNullOrEmpty(@Nullable String string) {
        return string == null || string.length() == 0; // string.isEmpty() in Java 6
    }

    /**
     * Returns false if EditText contains a null or empty string
     *
     * @param editText EditText containing string to be checked
     * @return false if string is null or empty
     */
    public static boolean isNullOrEmpty(EditText editText) {
        return isNullOrEmpty(editText.getText().toString());
    }

    /**
     * Removes HTML from a string
     *
     * @param htmlText string containing html
     * @return string without html
     */
    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String htmlText) {
        Spanned message;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            message = Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY);
        } else {
            message = Html.fromHtml(htmlText);
        }
        return message;
    }
}
