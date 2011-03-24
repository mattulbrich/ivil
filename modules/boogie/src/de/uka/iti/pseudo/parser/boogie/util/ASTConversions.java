package de.uka.iti.pseudo.parser.boogie.util;

import java.util.ArrayList;
import java.util.List;

import de.uka.iti.pseudo.parser.boogie.Token;

/**
 * This class provides some often used conversions, such as StringList2TokenList
 * 
 * 
 * @author timm.felden@felden.com
 */
public class ASTConversions {

    /**
     * @param source
     *            a List of Tokens
     * @return a List of Strings containing the properly escaped Token images
     */
    public static List<String> toEscapedNameList(List<Token> source) {
        List<String> rval = new ArrayList<String>(source.size());
        for (Token t : source)
            rval.add(getEscapedName(t));

        return rval;
    }

    /**
     * @param source
     *            a List of unescaped names
     * @return a List of Strings containing the properly escaped names
     */
    public static List<String> toEscapedList(List<String> source) {
        List<String> rval = new ArrayList<String>(source.size());
        for (String t : source)
            rval.add(getEscapedName(t));

        return rval;
    }

    /**
     * @param source
     *            a List of Tokens
     * @return a List of Strings containing the Tokens images
     */
    public static List<String> toStringList(List<Token> source) {
        List<String> rval = new ArrayList<String>(source.size());
        for (Token t : source)
            rval.add(t.image);

        return rval;
    }

    /**
     * Gets the name from t.image and returnes a propperly escaped version of
     * it.
     * <ul>
     * <li>_ -> __
     * <li>' -> _p
     * <li>~ -> _t
     * <li># -> _h
     * <li>$ -> _d
     * <li>^ -> _c
     * <li>. -> _o
     * <li>? -> _q
     * <li>` -> _a
     * </ul>
     */
    public static String getEscapedName(Token t) {
        return getEscapedName(t.image);
    }

    /**
     * Gets the name from t.image and returnes a propperly escaped version of
     * it.
     * <ul>
     * <li>_ -> __
     * <li>' -> _p
     * <li>~ -> _t
     * <li># -> _h
     * <li>$ -> _d
     * <li>^ -> _c
     * <li>. -> _o
     * <li>? -> _q
     * <li>` -> _a
     * </ul>
     */
    public static String getEscapedName(String name) {
        // note: this might decrease the performance of the boogie loader
        // significantly; it might be useful to decompose the name into a char
        // array and replace all characters at the same time

        return name.replace("_", "__").replace("'", "_p").replace("~", "_t").replace("#", "_h")
                .replace("$", "_d").replace("^", "_c").replace(".", "_o").replace("?", "_q").replace("`", "_a");
    }
}
