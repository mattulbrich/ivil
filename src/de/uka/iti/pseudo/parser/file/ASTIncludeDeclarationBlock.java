package de.uka.iti.pseudo.parser.file;

import java.util.List;

public class ASTIncludeDeclarationBlock extends ASTDeclarationBlock {

	private List<Token> includeStrings;

	public ASTIncludeDeclarationBlock(Token first, List<Token> list) {
		super(first);
		
		includeStrings = list;
		
		assert onlyStringTokens();
	}

	private boolean onlyStringTokens() {
		for (Token token : includeStrings) {
			if(token.kind != FileParserConstants.STRING)
				return false;
		}
		return true;
	}

	public void visit(ASTFileVisitor v) {
		v.visit(this);
	}

}
