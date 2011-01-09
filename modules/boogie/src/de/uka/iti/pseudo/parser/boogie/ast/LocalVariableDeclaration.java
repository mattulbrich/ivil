package de.uka.iti.pseudo.parser.boogie.ast;

import java.util.Collections;
import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTElement;
import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;

/**
 * A collection of local VariableDeclaration declarations. A Procedure can have
 * many of these blocks.
 * 
 * @author timm.felden@felden.com
 */
public class LocalVariableDeclaration extends ASTElement {

    private final Token first;
    private final List<Attribute> attributes;
    private final List<VariableDeclaration> vars;

    public LocalVariableDeclaration(Token first, List<Attribute> attr, List<VariableDeclaration> vars) {
        this.first = first;
        this.attributes = attr;
        this.vars = vars;

        addChildren(attributes);
        addChildren(vars);
    }

    public List<VariableDeclaration> getVariableDeclarations() {
        return Collections.unmodifiableList(vars);
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    @Override
    public Token getLocationToken() {
        return first;
    }

}
