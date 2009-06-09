package de.uka.iti.pseudo.parser.file;

import java.util.List;

public class ASTFunctionDeclaration extends ASTFileElement {

	private ASTType rangeType;
	private List<ASTType> argumentTypes;
	private Token precedence;
	private Token infixOperator;
	

	public ASTFunctionDeclaration(ASTType range, List<ASTType> tyrefs) {
		this.rangeType = range;
		this.argumentTypes = tyrefs;
		addChild(range);
		addChildren(tyrefs);
	}

	public ASTFunctionDeclaration(ASTType range, List<ASTType> tyrefs, Token infixOperator, Token precedence) {
		this(range, tyrefs);
		this.infixOperator = infixOperator;
		this.precedence = precedence;
	}

	public void visit(ASTFileVisitor v) {
		v.visit(this);
	}

}
