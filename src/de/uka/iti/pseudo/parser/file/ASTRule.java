package de.uka.iti.pseudo.parser.file;

import java.util.List;

import de.uka.iti.pseudo.parser.ASTVisitException;

public class ASTRule extends ASTDeclarationBlock {

	private Token name;
	private List<ASTRuleElement> ruleElements;

	public ASTRule(Token first, Token name, List<ASTRuleElement> list) {
		super(first);
		this.name = name;
		this.ruleElements = list;
		
		addChildren(ruleElements);
	}

	public void visit(ASTFileVisitor v) throws ASTVisitException {
		v.visit(this);
	}

}
