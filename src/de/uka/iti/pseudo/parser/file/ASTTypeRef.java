package de.uka.iti.pseudo.parser.file;

import java.util.List;

public class ASTTypeRef extends ASTType {

	private List<ASTType> argTypes;
	private Token typeToken;

	public ASTTypeRef(Token token, List<ASTType> args) {
		this.typeToken = token;
		this.argTypes = args;
		
		addChildren(args);
	}

	public void visit(ASTFileVisitor v) {
		v.visit(this);
	}

}
