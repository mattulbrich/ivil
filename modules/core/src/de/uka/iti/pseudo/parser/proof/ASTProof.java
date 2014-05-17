package de.uka.iti.pseudo.parser.proof;

import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.parser.file.ASTDeclarationBlock;

public class ASTProof extends ASTDeclarationBlock {

    public enum Kind {
        RULE, PROGRAM, PROBLEM
    }

    public ASTProof(Token first, Kind kind, Token id, ASTProofNode tree) {
        super(first);
        addChild(tree);
    }

    @Override
    public void visit(ASTVisitor v) throws ASTVisitException {
        // TODO Auto-generated method stub

    }

}
