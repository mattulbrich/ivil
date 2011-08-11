package de.uka.iti.pseudo.parser.boogie.ast;

import java.util.LinkedList;
import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;
import de.uka.iti.pseudo.parser.boogie.ast.expression.Expression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.VariableUsageExpression;
import de.uka.iti.pseudo.parser.boogie.ast.expression.WildcardExpression;
import de.uka.iti.pseudo.parser.boogie.util.ASTConversions;

public final class CallStatement extends Statement {

    private final List<Attribute> attr;
    private final String name;

    /**
     * NOTE: can be empty if results are discarded.
     */
    private final List<Expression> outParam;
    private final List<Expression> arglist;

    public CallStatement(Token first, List<Attribute> attr, String name, List<Token> outParam, List<Expression> arglist) {
        super(first);

        this.attr = attr;
        this.name = ASTConversions.getEscapedName(name);
        this.arglist = arglist;

        this.outParam = new LinkedList<Expression>();
        for (int i = 0; i < outParam.size(); i++)
            if (outParam.get(i).image.equals("*"))
                this.outParam.add(new WildcardExpression(outParam.get(i)));
            else
                this.outParam.add(new VariableUsageExpression(outParam.get(i)));

        addChildren(attr);
        addChildren(arglist);
        addChildren(this.outParam);
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
    public List<Expression> getOutParam() {
        return outParam;
    }

    public List<Expression> getArguments() {
        return arglist;
    }

}
