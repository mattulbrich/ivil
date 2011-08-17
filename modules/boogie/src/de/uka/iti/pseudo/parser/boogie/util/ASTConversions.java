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
        char[] in = name.toCharArray(), out;

        // calculate target length
        int length = in.length;
        for (int i = 0; i < in.length; i++)
            if (in[i] == '_' || in[i] == '\'' || in[i] == '~' || in[i] == '#' || in[i] == '$' || in[i] == '^'
                    || in[i] == '.' || in[i] == '?' || in[i] == '`')
                ++length;

        if (length == in.length)
            return name;

        out = new char[length];
        for (int i = 0, j = 0; i < in.length; i++, j++) {
            if (in[i] == '_' || in[i] == '\'' || in[i] == '~' || in[i] == '#' || in[i] == '$' || in[i] == '^'
                    || in[i] == '.' || in[i] == '?' || in[i] == '`') {
                out[j++] = '_';
                switch (in[i]) {
                case '_':
                    out[j] = '_';
                    break;
                case '\'':
                    out[j] = 'p';
                    break;
                case '~':
                    out[j] = 't';
                    break;
                case '#':
                    out[j] = 'h';
                    break;
                case '$':
                    out[j] = 'd';
                    break;
                case '^':
                    out[j] = 'c';
                    break;
                case '.':
                    out[j] = 'o';
                    break;
                case '?':
                    out[j] = 'q';
                    break;
                case '`':
                    out[j] = 'a';
                    break;
                }
            } else {
                out[j] = in[i];
            }
        }

        return new String(out);
    }
}
