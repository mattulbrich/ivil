package de.uka.iti.pseudo.parser.file;

import de.uka.iti.pseudo.parser.ASTVisitException;

public class ASTRawTerm extends ASTFileElement {
	
	private Token termToken;

	public ASTRawTerm(Token token) {
		this.termToken = token;
		
		assert termToken.kind == FileParser.TERM;
	}
 
	public void visit(ASTFileVisitor v)  throws ASTVisitException {
		v.visit(this);
	}

	public Token getTermToken() {
		return termToken;
	}

	protected Token getLocationToken() {
		return termToken;
	}
	
}
