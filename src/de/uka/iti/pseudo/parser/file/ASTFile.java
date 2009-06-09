package de.uka.iti.pseudo.parser.file;

import java.util.List;

public class ASTFile extends ASTFileElement {
	
	protected final static boolean DEBUG = false;

	private List<ASTDeclarationBlock> declarationBlocks;
	private ASTRawTerm problemTerm;

	public ASTFile(List<ASTDeclarationBlock> blocks, ASTRawTerm problem) {
		this.declarationBlocks = blocks;
		this.problemTerm = problem;
		
		addChildren(blocks);
		if(problem != null)
			addChild(problem);
	}

	public void visit(ASTFileVisitor v) {
		v.visit(this);
	}

}
