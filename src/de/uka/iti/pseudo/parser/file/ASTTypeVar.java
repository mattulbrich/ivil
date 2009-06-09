package de.uka.iti.pseudo.parser.file;

public class ASTTypeVar extends ASTType {

	private Token typeVarToken;

	public ASTTypeVar(Token token) {
		this.typeVarToken = token;
	}

	public void visit(ASTFileVisitor v) {
		v.visit(this);
	}

}
