package de.uka.iti.pseudo.parser.boogie.ast;

import java.util.Collections;
import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;
import de.uka.iti.pseudo.parser.boogie.util.ASTConversions;

/**
 * Statement that lets the control flow jump to all destination labels.
 * 
 * @author timm.felden@felden.com
 */
public final class GotoStatement extends Statement {

    final List<String> destinations;

    public GotoStatement(Token first, List<Token> destinations) {
        super(first);

        this.destinations = ASTConversions.toStringList(destinations);
    }

    public List<String> getDestinations() {
        return Collections.unmodifiableList(destinations);
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }
}
