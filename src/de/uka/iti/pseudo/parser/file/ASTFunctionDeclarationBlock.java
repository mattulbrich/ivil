package de.uka.iti.pseudo.parser.file;

import java.util.List;

public class ASTFunctionDeclarationBlock extends ASTDeclarationBlock {

	private List<ASTFunctionDeclaration> functionDeclarations;

	public ASTFunctionDeclarationBlock(Token first,	List<ASTFunctionDeclaration> list) {
		super(first);
		this.functionDeclarations = list;
		addChildren(list);
	}

	public void visit(ASTFileVisitor v) {
		v.visit(this);
	}

}
