package de.uka.iti.pseudo.parser.file;

import java.util.List;

import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.parser.program.ASTStatement;

public class ASTProgramDeclaration extends ASTDeclarationBlock {

    private Token name;
    private Token source;
    
    public ASTProgramDeclaration(Token first, Token name, Token source, List<ASTStatement> list) {
        super(first);
        this.name = name;
        this.source = source;
        
        assert list.size() >= 1;
        addChildren(list);
    }
    
    public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public Token getName() {
        return name;
    }

    public Token getSource() {
        return source;
    }

}
