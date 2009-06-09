package de.uka.iti.pseudo.parser.file;

import de.uka.iti.pseudo.parser.ASTVisitException;

public class ASTRuleFind extends ASTRuleElement {

	private MatchingLocation matchingLocation;
	private ASTRawTerm rawTerm;

	public ASTRuleFind(Token t, ASTRawTerm rawTerm, MatchingLocation matchingLocation) {
		super(t);
		this.rawTerm = rawTerm;
		this.matchingLocation = matchingLocation;
		
		addChild(rawTerm);
	}

	public void visit(ASTFileVisitor v) throws ASTVisitException {
		v.visit(this);
	}

}
