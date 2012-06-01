package de.uka.iti.ivil.jbc.util;

/**
 * Escapes names according to N_e, defined in "Allgemeine Namenstransformation".
 * 
 * build and revert are mutually inverse functions.
 * 
 * @invariant ∀ s : String :: build(revert(s)) == s;
 * 
 * @author timm.felden@felden.com
 */
public final class EscapeName {

    public static String build(String name) {
        char[] in = name.toCharArray(), out;

        // calculate target length
        int length = in.length;
        for (int i = 0; i < in.length; i++) {
            final char c = in[i];
            if (!(('a' <= c && c <= 'z') || ('A' <= c && c <= 'Y') || ('0' <= c && c <= '9')))
                length+='Z'==c?1:4;
        }

        // no change required, so return
        if (length == in.length)
            return name;

        // apply N_ec for each character in the string
        out = new char[length];
        for (int i = 0, j = 0; i < in.length; i++) {
            final char c = in[i];
            if (('a' <= c && c <= 'z') || ('A' <= c && c <= 'Y') || ('0' <= c && c <= '9')) {
                out[j] = c;
                j++;
            } else if('Z' == c){
                out[j + 1] = out[j] = 'Z';
                j += 2;
            } else {
                out[j] = 'Z';
                final String h = Integer.toHexString(0x10000 | c);
                for (int p = 1; p < 5; p++)
                    out[j + p] = h.charAt(p);
                j += 5;
            }
        }

        return new String(out);
    }
    
    /**
     * Reverts escaping.
     * 
     * @param escaped
     *            String that has been escaped with build
     * @return the original string with escaping undone
     */
    public static String revert(String escaped) {
        char[] in = escaped.toCharArray(), out;

        // calculate target length
        int length = in.length;
        for (int i = 0; i < in.length; i++) {
            final char c = in[i];
            if ('Z' == c) {
                length -= 'Z' == in[i + 1] ? 1 : 4;
                i += 'Z' == in[i + 1] ? 1 : 4;
            }
        }

        // no change required, so return
        if (length == in.length)
            return escaped;

        // apply N_ec for each character in the string
        out = new char[length];
        
        for (int i = 0, j = 0; j < out.length; j++) {
            final char c = in[i];
            if ('Z' != c) {
                out[j] = c;
                i++;
            } else if ('Z' == in[i + 1]) {
                out[j] = 'Z';
                i += 2;
            } else {
                out[j] = (char) Integer.parseInt(escaped.substring(i + 1, i + 5), 16);
                i += 5;
            }
        }

        return new String(out);
    }
}
