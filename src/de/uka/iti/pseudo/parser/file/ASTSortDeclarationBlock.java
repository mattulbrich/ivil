package de.uka.iti.pseudo.parser.file;

import java.util.List;

public class ASTSortDeclarationBlock extends ASTDeclarationBlock {

	private List<ASTSortDeclaration> sortDeclarations;

	public ASTSortDeclarationBlock(Token first, List<ASTSortDeclaration> list) {
		super(first);
		this.sortDeclarations = list;
		addChildren(list);
	}

	public void visit(ASTFileVisitor v) {
		v.visit(this);
	}

}
