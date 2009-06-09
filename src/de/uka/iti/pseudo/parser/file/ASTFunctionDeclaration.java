package de.uka.iti.pseudo.parser.file;

import java.util.Collections;
import java.util.List;

import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.parser.ASTVisitException;

public class ASTFunctionDeclaration extends ASTFileElement {

    private ASTType rangeType;

    private List<ASTType> argumentTypes;

    private Token precedence;

    private Token fixity;

    private Token name;
    
    private boolean unique;

	private Token operatorIdentifier;

    public ASTFunctionDeclaration(ASTType range, Token name,
            List<ASTType> tyrefs, boolean unique) {
    	this(range, name, tyrefs, unique, null, null, null);
    }

    public ASTFunctionDeclaration(ASTType range, Token name,
            List<ASTType> tyrefs, boolean unique, Token fixOperator, Token operatorIdentifier, Token precedence) {
    	this.rangeType = range;
    	this.argumentTypes = tyrefs;
    	this.name = name;
    	this.fixity = fixOperator;
    	this.operatorIdentifier = operatorIdentifier;
    	this.precedence = precedence;
    	addChild(range);
    	addChildren(tyrefs);
    }

    public void visit(ASTFileVisitor v) throws ASTVisitException {
        v.visit(this);
    }

    public ASTType getRangeType() {
        return rangeType;
    }

    public List<ASTType> getArgumentTypes() {
        return Collections.unmodifiableList(argumentTypes);
    }

    public Token getPrecedence() {
        return precedence;
    }

    public Token getName() {
        return name;
    }

    public boolean isInfix() {
        return fixity != null && fixity.image.equals("infix");
    }
    
    public boolean isPrefix() {
        return fixity != null && fixity.image.equals("prefix");
    }

    public Token getOperatorIdentifier() {
		return operatorIdentifier;
	}

	public Token getLocationToken() {
        return name;
    }

	public boolean isUnique() {
		return unique;
	}

}
