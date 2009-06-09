package de.uka.iti.pseudo.parser.file;

public class ASTRuleReplace extends ASTRuleElement {

	private ASTRawTerm rawTerm;

	public ASTRuleReplace(Token first, ASTRawTerm rawTerm) {
		super(first);
		this.rawTerm = rawTerm;
		addChild(rawTerm);
	}

	public void visit(ASTFileVisitor v) {
		v.visit(this);
	}

}
