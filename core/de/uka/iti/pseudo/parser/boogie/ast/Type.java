package de.uka.iti.pseudo.parser.boogie.ast;

import de.uka.iti.pseudo.parser.boogie.ASTElement;

/**
 * Type interface with some usefull implementations. Types are either built-in,
 * template or maptypes. Synonyms are not treated as "AST level" Types.
 * 
 * @author timm.felden@felden.com
 * 
 */
public abstract class Type extends ASTElement {
    /**
     * Human readable Typerepresentation, that is also used as typeidentifier in
     * the typeSpace table.
     * 
     * @return a pretty human readable type representation, that represents type
     *         arguments as <...>, map arguments as [...]
     */
    abstract public String getPrettyName();
}
