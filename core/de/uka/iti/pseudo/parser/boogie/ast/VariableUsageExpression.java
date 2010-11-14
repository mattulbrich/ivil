package de.uka.iti.pseudo.parser.boogie.ast;

import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;

/**
 * This expression does something similar to loading a variable from a memory
 * into a register. This is needed to handle statements like "call i, m[i] :=
 * P(i, m[i])" correctly
 * 
 * @author timm.felden@felden.com
 */
public final class VariableUsageExpression extends Expression {

    private final String name;

    public VariableUsageExpression(Token first) {
        super(first);

        name = first.image;
    }

    @Override
    public List<Expression> getOperands() {
        assert false;
        return null;
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public String getName() {
        return name;
    }

}
