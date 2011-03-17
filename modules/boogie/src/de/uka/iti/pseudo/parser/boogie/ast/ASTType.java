package de.uka.iti.pseudo.parser.boogie.ast;

import de.uka.iti.pseudo.parser.boogie.ASTElement;

/**
 * Type interface with some usefull implementations. Types are either built-in,
 * template or maptypes. Synonyms are not treated as "AST level" Types.<br>
 * 
 * <b>Note</b>: Types don't add child types to the AST as type checking and
 * lowering does not require this; thus all types appearing on the AST type an
 * typeable object and have to be checked for this object<br>
 * Types themselfes are checked for wellformedness in an earlier phase.
 * 
 * @author timm.felden@felden.com
 * 
 */
public abstract class ASTType extends ASTElement {
    /**
     * Human readable Typerepresentation, that is also used as typeidentifier in
     * the typeSpace table.
     * 
     * @return a pretty human readable type representation, that represents type
     *         arguments as <...>, map arguments as [...]
     */
    abstract public String getPrettyName();
}
