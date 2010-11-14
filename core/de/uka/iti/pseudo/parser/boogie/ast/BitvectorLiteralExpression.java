package de.uka.iti.pseudo.parser.boogie.ast;

import java.math.BigInteger;
import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;

public final class BitvectorLiteralExpression extends Expression {

    final private BigInteger value, dimension;

    public BitvectorLiteralExpression(Token first) {
        super(first);

        value = new BigInteger(first.image.substring(0, first.image.indexOf('b')));
        dimension = new BigInteger(first.image.substring(1 + first.image.indexOf('v')));
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

    public BigInteger getValue() {
        return value;
    }

    public BigInteger getDimension() {
        return dimension;
    }

}
