package de.uka.iti.pseudo.parser.boogie.ast;

import java.util.Collections;
import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;

public final class IfStatement extends Statement {

    private final Expression guard;
    private final List<Statement> thenBlock;
    private final List<Statement> elseBlock;

    /**
     * null values are used here to allow for easy creation of understandable
     * ivil native code.
     * 
     * @param first
     *            the start of this if statement
     * @param guard
     *            the guard of this if statement; if null then a wildcard was
     *            supplied
     * @param then
     *            a statementlist, that has to be executed if guard is valid
     * @param else1
     *            a statementlist, that has to be executed if guard is not
     *            valid; can be null, what means no else block was supplied
     */
    public IfStatement(Token first, Expression guard, List<Statement> then, List<Statement> else1) {
        super(first);

        this.guard = guard;
        this.thenBlock = then;
        this.elseBlock = else1;
        
        addChild(guard);
        addChildren(thenBlock);

        if (null != elseBlock)
            addChildren(elseBlock);
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }
    
    public boolean isWildcardIf() {
        return guard instanceof WildcardExpression;
    }

    //! only get guard expression if this if statement doesnt have a wildcard
    public Expression getGuard() {
        assert null != guard;
        return guard;
    }

    public List<Statement> getThenBlock() {
        return Collections.unmodifiableList(thenBlock);
    }

    /**
     * @return the else block, can be null to indicate that no else block
     *         exists.
     */
    public List<Statement> getElseBlock() {
        return Collections.unmodifiableList(elseBlock);
    }
}
