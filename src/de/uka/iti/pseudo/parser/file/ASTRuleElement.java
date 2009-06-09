package de.uka.iti.pseudo.parser.file;

public abstract class ASTRuleElement extends ASTFileElement {

	protected Token firstToken;

	public ASTRuleElement(Token first) {
		this.firstToken = first;
	}
}
