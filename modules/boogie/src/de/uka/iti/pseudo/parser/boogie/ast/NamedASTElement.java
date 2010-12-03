package de.uka.iti.pseudo.parser.boogie.ast;

public interface NamedASTElement {

    /**
     * @return the name of this element, that allows to identify it uniquely if
     *         the type of the element is known
     */
    public String getName();
}
