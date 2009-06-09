package de.uka.iti.pseudo.parser.file;

import de.uka.iti.pseudo.parser.ASTVisitException;

public class ASTRuleAdd extends ASTRuleElement {

	private ASTRawTerm rawTerm;
	private MatchingLocation matchingLocation;

	public ASTRuleAdd(Token first, ASTRawTerm rawTerm, MatchingLocation matchingLocation) {
		super(first);
		this.rawTerm = rawTerm;
		this.matchingLocation = matchingLocation;
		
		addChild(rawTerm);
	}

	public void visit(ASTFileVisitor v) throws ASTVisitException {
		v.visit(this);
	}

	@Override
	protected Token getLocationToken() {
		return rawTerm.getLocationToken();
	}

}
