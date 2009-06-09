package de.uka.iti.pseudo.parser.file;

import de.uka.iti.pseudo.parser.ASTVisitException;

public class ASTRuleReplace extends ASTRuleElement {

	private ASTRawTerm rawTerm;

	public ASTRuleReplace(Token first, ASTRawTerm rawTerm) {
		super(first);
		this.rawTerm = rawTerm;
		addChild(rawTerm);
	}

	public void visit(ASTFileVisitor v) throws ASTVisitException {
		v.visit(this);
	}

}
