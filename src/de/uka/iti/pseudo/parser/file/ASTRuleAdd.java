package de.uka.iti.pseudo.parser.file;

public class ASTRuleAdd extends ASTRuleElement {

	private ASTRawTerm rawTerm;
	private MatchingLocation matchingLocation;

	public ASTRuleAdd(Token first, ASTRawTerm rawTerm, MatchingLocation matchingLocation) {
		super(first);
		this.rawTerm = rawTerm;
		this.matchingLocation = matchingLocation;
		
		addChild(rawTerm);
	}

	public void visit(ASTFileVisitor v) {
		v.visit(this);
	}

}
