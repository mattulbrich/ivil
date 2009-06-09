package de.uka.iti.pseudo.parser.file;

import java.util.Arrays;
import java.util.Collections;

import de.uka.iti.pseudo.environment.FixOperator;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.term.ASTTerm;
import de.uka.iti.pseudo.parser.term.ASTVisitor;
import de.uka.iti.pseudo.parser.term.Token;

public class ASTFixTerm extends ASTTerm {

	private FixOperator fixOperator;
	private Token operator;

	public ASTFixTerm(Token op, FixOperator infixOp,
			ASTTerm t1, ASTTerm t2) {
		
		super(Arrays.asList(new ASTTerm[] { t1, t2 }));
		this.operator = op;
		this.fixOperator = infixOp;
		
	}

	public ASTFixTerm(Token op, FixOperator prefixOp,
			ASTTerm t1) {
		
		super(Collections.singletonList(t1));
		this.operator = op;
		this.fixOperator = prefixOp;
	}

	@Override
	protected Token getLocationToken() {
		return operator;
	}

	@Override
	public void visit(ASTVisitor v) throws ASTVisitException {
		v.visit(this);
	}

	public FixOperator getFixOperator() {
		return fixOperator;
	}
	
	public String toString() {
	    return super.toString() + "[" + operator + "]";
	}

}
