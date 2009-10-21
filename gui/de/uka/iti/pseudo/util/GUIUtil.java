package de.uka.iti.pseudo.util;

import nonnull.NonNull;

public class GUIUtil {

    private GUIUtil() {
        // hide constructor
    }

    /**
     * <p>
     * Certain characters have special significance in HTML, and should be
     * represented by HTML entities if they are to preserve their meanings. This
     * function returns a string with some of these conversions made.
     * </p>
     * 
     * <p>
     * The translations performed are:
     * </p>
     * <ul>
     * <li>'&amp;' (ampersand) becomes '&amp;amp;'</li>
     * <li>'"' (double quote) becomes '&amp;quot;'</li>
     * <li>'&lt;' (less than) becomes '&amp;lt;'</li>
     * <li>'&gt;' (greater than) becomes '&amp;gt;'</li>
     * </ul>
     * 
     * @param message
     *            The string which has to be translated.
     * @return The message string with the special html characters masked.
     */
    public static @NonNull String htmlentities(@NonNull String message) {
        message = message.replace("&", "&amp;");
        message = message.replace("<", "&lt;");
        message = message.replace(">", "&gt;");
        message = message.replace("\"", "&quot;");
        return message;
    }

}
