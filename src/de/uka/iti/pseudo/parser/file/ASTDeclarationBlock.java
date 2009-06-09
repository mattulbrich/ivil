package de.uka.iti.pseudo.parser.file;

public abstract class ASTDeclarationBlock extends ASTFileElement {

	protected Token firstToken;

	public ASTDeclarationBlock(Token firstToken) {
		super();
		this.firstToken = firstToken;
	}

}
