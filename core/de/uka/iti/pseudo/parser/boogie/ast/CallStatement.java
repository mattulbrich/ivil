package de.uka.iti.pseudo.parser.boogie.ast;

import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;

public final class CallStatement extends Statement {
    
    private final List<Attribute> attr;
    private final String name;

    /**
     * NOTE: outParam can contain null's, in which case the result has to be
     * discarded.
     */
    private final List<String> outParam;
    private final List<Expression> arglist;

    public CallStatement(Token first, List<Attribute> attr, String name, List<String> outParam, List<Expression> arglist) {
        super(first);

        this.attr = attr;
        this.name = name;
        this.outParam = outParam;
        this.arglist = arglist;

        addChildren(attr);
        addChildren(arglist);
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public List<Attribute> getAttr() {
        return attr;
    }

    public String getName() {
        return name;
    }

    /**
     * @return the returned list can contain nulls to indicate that the
     *         respective result shall be discarded
     */
    public List<String> getOutParam() {
        return outParam;
    }

    public List<Expression> getArglist() {
        return arglist;
    }

}
