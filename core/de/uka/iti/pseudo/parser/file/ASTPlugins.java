package de.uka.iti.pseudo.parser.file;

import java.util.List;

import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;

public class ASTPlugins extends ASTDeclarationBlock {

    // private List<ASTPluginDeclaration> pluginDeclarations;

    public ASTPlugins(Token firstToken, List<ASTPluginDeclaration> pairs) {
        super(firstToken);
        addChildren(pairs);
        // this.pluginDeclarations = pairs;
    }

    @Override public void visit(ASTVisitor v) throws ASTVisitException {
        v.visit(this);
    }
    
}
