package de.uka.iti.pseudo.parser.boogie.ast;

import java.util.Collections;
import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;

public class GlobalVariableDeclaration extends DeclarationBlock {
    
    private final List<VariableDeclaration> vars;

    public GlobalVariableDeclaration(Token first, List<Attribute> attr, List<VariableDeclaration> vars) {
        super(first, attr);
        this.vars = vars;

        addChildren(vars);
    }

    public List<VariableDeclaration> getVariables() {
        return Collections.unmodifiableList(vars);
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

}
