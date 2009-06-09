package de.uka.iti.pseudo.parser.file;

import de.uka.iti.pseudo.parser.ASTVisitException;

//  Documentation needed
// probably do not need it any more
@Deprecated
public class ASTWithClause extends ASTRuleElement {
    
    Token schemaIdentifier;

    public ASTWithClause(Token first, ASTType type, Token schemaIdentifier) {
        super(first);
        addChild(type);
        this.schemaIdentifier = schemaIdentifier;
    }

    public void visit(ASTFileVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public Token getSchemaIdentifier() {
        return schemaIdentifier;
    }
    
}
