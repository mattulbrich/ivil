package de.uka.iti.pseudo.parser.file;

import java.util.Collections;
import java.util.List;

import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.parser.ASTVisitException;

public class ASTSortDeclaration extends ASTFileElement implements
        ASTLocatedElement {

    private List<Token> typeVariables;

    private Token name;

    public ASTSortDeclaration(Token name, List<Token> tyvars) {
        this.name = name;
        this.typeVariables = tyvars;
    }

    public List<Token> getTypeVariables() {
        return Collections.unmodifiableList(typeVariables);
    }

    public Token getName() {
        return name;
    }

    public void visit(ASTFileVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public String getLocation() {
        return getFileName() + ":" + name.beginLine;
    }
    
    @Override
	protected Token getLocationToken() {
    	return name;
	}

}
