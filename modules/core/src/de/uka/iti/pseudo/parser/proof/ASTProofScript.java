package de.uka.iti.pseudo.parser.proof;

import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.parser.file.ASTDeclarationBlock;
import de.uka.iti.pseudo.proof.ProofIdentifier.Kind;

public class ASTProofScript extends ASTDeclarationBlock {

    private final Kind kind;
    private final String name;


    public ASTProofScript(Token first, Kind kind, String name, ASTProofScriptNode tree) {
        super(first);
        this.kind = kind;
        this.name = name;
        addChild(tree);
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public Kind getKind() {
        return kind;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return super.toString() + "[" + kind + "," + getName() + "]";
    }

}
