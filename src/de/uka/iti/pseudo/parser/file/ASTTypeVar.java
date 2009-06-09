package de.uka.iti.pseudo.parser.file;

import de.uka.iti.pseudo.parser.ASTVisitException;

public class ASTTypeVar extends ASTType {

	private Token typeVarToken;

	public ASTTypeVar(Token token) {
		this.typeVarToken = token;
	}

	public void visit(ASTFileVisitor v) throws ASTVisitException {
		v.visit(this);
	}

}
