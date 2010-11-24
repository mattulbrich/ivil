package de.uka.iti.pseudo.parser.boogie.ast;

import java.util.LinkedList;
import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.ParseException;
import de.uka.iti.pseudo.parser.boogie.Token;

public final class CodeExpression extends Expression {

    final private List<SpecBlock> specs;
    final private List<Expression> operands = new LinkedList<Expression>();

    public CodeExpression(Token location, List<LocalVariableDeclaration> vars, List<SpecBlock> specs)
            throws ParseException {
        super(location);

        this.specs = specs;

        addChildren(vars);
        addChildren(specs);
    }

    @Override
    public List<Expression> getOperands() {
        return operands;
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

}
