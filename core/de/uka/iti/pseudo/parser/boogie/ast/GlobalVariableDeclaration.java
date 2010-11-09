package de.uka.iti.pseudo.parser.boogie.ast;

import java.util.Collections;
import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;

public class GlobalVariableDeclaration extends DeclarationBlock {
    
    private final List<Variable> vars;

    public GlobalVariableDeclaration(Token first, List<Attribute> attr, List<Variable> vars) {
        super(first, attr);
        this.vars = vars;

        addChildren(vars);
    }

    public List<Variable> getVariables() {
        return Collections.unmodifiableList(vars);
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

}
