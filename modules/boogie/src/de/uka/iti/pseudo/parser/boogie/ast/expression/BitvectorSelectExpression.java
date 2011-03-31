package de.uka.iti.pseudo.parser.boogie.ast.expression;

import java.util.LinkedList;
import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.ParseException;

public class BitvectorSelectExpression extends Expression {

    private final int first, last;
    private final List<Expression> operands = new LinkedList<Expression>();

    public BitvectorSelectExpression(Expression first, IntegerExpression last) throws ParseException {
        super(first.getLocationToken());
        
        if (!(first instanceof IntegerExpression))
            throw new ParseException("Expected IntegerExression as first argument, but got " + first.toString());

        this.first = ((IntegerExpression) first).getValue().intValue();
        this.last = last.getValue().intValue();

        if (this.first < this.last || this.first < 0 || this.last < 0)
            throw new ParseException("Illegal range in bitvector selection: [" + this.first + ":" + this.last + "]");
    }

    @Override
    public List<Expression> getOperands() {
        return operands;
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public int getLast() {
        return last;
    }

    public int getFirst() {
        return first;
    }

}
