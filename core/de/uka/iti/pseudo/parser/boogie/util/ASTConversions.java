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
    public static List<String> toStringList(List<Token> source) {
        List<String> rval = new ArrayList<String>(source.size());
        for (Token t : source)
            rval.add(t.image);

        return rval;
    }
}
