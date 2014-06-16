package de.uka.iti.pseudo.parser.proof;

import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.parser.file.ASTDeclarationBlock;

public class ASTProofSourceFile extends ASTDeclarationBlock {

    private final String path;

    public ASTProofSourceFile(Token firstToken, String path) {
        super(firstToken);
        this.path = path;
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public String getPath() {
        return path;
    }

}
