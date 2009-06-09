package de.uka.iti.pseudo.term;

import java.util.Arrays;
import java.util.Collections;

import de.uka.iti.pseudo.environment.FixOperator;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.term.ASTOperatorIdentifierTerm;
import de.uka.iti.pseudo.parser.term.ASTTerm;
import de.uka.iti.pseudo.parser.term.ASTVisitor;
import de.uka.iti.pseudo.parser.term.Token;

public class ASTFixTerm extends ASTTerm {

	private FixOperator fixOperator;
	private ASTOperatorIdentifierTerm operator;

	public ASTFixTerm(ASTOperatorIdentifierTerm op, FixOperator infixOp,
			ASTTerm t1, ASTTerm t2) {
		
		super(Arrays.asList(new ASTTerm[] { t1, t2 }));
		this.operator = op;
		this.fixOperator = infixOp;
		
		// we are created later as a replacement, so better copy the filename from the original
		setFilename(op.getFileName());
	}

	public ASTFixTerm(ASTOperatorIdentifierTerm op, FixOperator prefixOp,
			ASTTerm t1) {
		
		super(Collections.singletonList(t1));
		this.operator = op;
		this.fixOperator = prefixOp;
	}

	@Override
	protected Token getLocationToken() {
		return operator.getSymbol();
	}

	@Override
	public void visit(ASTVisitor v) throws ASTVisitException {
		v.visit(this);
	}

	public FixOperator getFixOperator() {
		return fixOperator;
	}

}
