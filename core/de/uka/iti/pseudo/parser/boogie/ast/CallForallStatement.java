package de.uka.iti.pseudo.parser.boogie.ast;

import java.util.List;

import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ASTVisitor;
import de.uka.iti.pseudo.parser.boogie.Token;

public final class CallForallStatement extends Statement {
    
    private final String name;
    private final List<Attribute> attr;
    /**
     * arglist can contain wildcard expressions. In this case a suitable
     * ∀-quantified variable has to be used.
     */
    private final List<Expression> arglist;

    public CallForallStatement(Token first, List<Attribute> attr, String image, List<Expression> arglist) {
        super(first);

        name = image;
        this.arglist = arglist;
        this.attr = attr;

        addChildren(attr);
        addChildren(arglist);
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public String getName() {
        return name;
    }

    public List<Expression> getArguments() {
        return arglist;
    }

    public List<Attribute> getAttributes() {
        return attr;
    }

}
