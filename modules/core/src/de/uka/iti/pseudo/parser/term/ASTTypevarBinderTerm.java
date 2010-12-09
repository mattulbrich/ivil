package de.uka.iti.pseudo.parser.term;

import java.util.Collections;

import nonnull.Nullable;
import checkers.nullness.quals.LazyNonNull;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.term.creation.Typing;

public class ASTTypevarBinderTerm extends ASTTerm {
    
    private Token binderToken;
    private @LazyNonNull Typing boundTyping = null;

    public ASTTypevarBinderTerm(Token binderToken,
            ASTType type, ASTTerm subterm) {
        super(Collections.<ASTTerm>emptyList());
        addChild(type);
        addChild(subterm);
        this.binderToken = binderToken;
        
        assert type instanceof ASTTypeVar
                || type instanceof ASTSchemaType;
    }

    @Override
    public Token getLocationToken() {
        return binderToken;
    }

    public Token getBinderToken() {
        return binderToken;
    }

    public ASTType getBoundType() {
        return (ASTType) getChildren().get(0);
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }
    
    public ASTTerm getTerm() {
        return getSubterms().get(0);
    }
    
    public @Nullable Typing getBoundTyping() {
        return boundTyping;
    }

    public void setBoundTyping(Typing boundTyping) {
        this.boundTyping = boundTyping;
    }


}
