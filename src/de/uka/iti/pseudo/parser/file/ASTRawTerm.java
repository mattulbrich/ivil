package de.uka.iti.pseudo.parser.file;

public class ASTRawTerm extends ASTFileElement {
	
	private Token termToken;

	public ASTRawTerm(Token token) {
		this.termToken = token;
		
		assert termToken.kind == FileParser.TERM;
	}
 
	public void visit(ASTFileVisitor v) {
		v.visit(this);
	}

}
