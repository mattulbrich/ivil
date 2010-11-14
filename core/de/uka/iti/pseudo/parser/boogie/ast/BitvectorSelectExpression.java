package de.uka.iti.pseudo.parser.boogie.ast;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.ParseException;

public class BitvectorSelectExpression extends Expression {

    private final BigInteger first, last;
    private final List<Expression> operands = new LinkedList<Expression>();

    public BitvectorSelectExpression(Expression first, IntegerExpression last) throws ParseException {
        super(first.getLocationToken());
        
        if (!(first instanceof IntegerExpression))
            throw new ParseException("Expected IntegerExression as first argument, but got " + first.toString());

        this.first = ((IntegerExpression) first).getValue();
        this.last = last.getValue();
    }

    @Override
    public List<Expression> getOperands() {
        return operands;
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public BigInteger getLast() {
        return last;
    }

    public BigInteger getFirst() {
        return first;
    }

}
