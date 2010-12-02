package de.uka.iti.pseudo.parser.boogie.ast;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;

/**
 * Tells us to return from the current procedure.
 * 
 * @author timm.felden@felden.com
 * 
 */
public final class ReturnStatement extends Statement {

    public ReturnStatement(Token first) {
        super(first);
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

}
