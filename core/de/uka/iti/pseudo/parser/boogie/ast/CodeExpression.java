package de.uka.iti.pseudo.parser.boogie.ast;

import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.ParseException;

public final class CodeExpression extends Expression {

    public CodeExpression() throws ParseException {
        super(null);

        throw new ParseException("Code expressions are not supported yet.");
    }

    @Override
    public List<Expression> getOperands() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

}
