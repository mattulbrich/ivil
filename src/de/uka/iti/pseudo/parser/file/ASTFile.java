package de.uka.iti.pseudo.parser.file;

import java.util.Collections;
import java.util.List;

import de.uka.iti.pseudo.parser.ASTVisitException;

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

	public void visit(ASTFileVisitor v) throws ASTVisitException{
		v.visit(this);
	}

	public static boolean isDEBUG() {
		return DEBUG;
	}

	public List<ASTDeclarationBlock> getDeclarationBlocks() {
		return Collections.unmodifiableList(declarationBlocks);
	}

	public ASTRawTerm getProblemTerm() {
		return problemTerm;
	}
	
	@Override
	protected Token getLocationToken() {
		return null;
	}

}
