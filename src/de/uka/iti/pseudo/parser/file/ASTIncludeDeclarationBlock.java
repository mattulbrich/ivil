package de.uka.iti.pseudo.parser.file;

import java.util.Collections;
import java.util.List;

import de.uka.iti.pseudo.parser.ASTVisitException;

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

	public void visit(ASTFileVisitor v) throws ASTVisitException {
		v.visit(this);
	}

	public List<Token> getIncludeStrings() {
		return Collections.unmodifiableList(includeStrings);
	}

}
