package de.uka.iti.pseudo.parser.proof;

import nonnull.Nullable;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.parser.file.ASTDeclarationBlock;

public class ASTProofScript extends ASTDeclarationBlock {

    private final @Nullable String name;


    public ASTProofScript(Token first, String name, ASTProofScriptNode tree) {
        super(first);
        this.name = name;
        addChild(tree);
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public @Nullable String getName() {
        return name;
    }

    @Override
    public String toString() {
        return super.toString() + "[" + getName() + "]";
    }

}
