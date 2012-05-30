/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.parser.file;

import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ASTVisitor;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.parser.term.ASTTerm;
import nonnull.NonNull;

public class ASTRuleReplace extends ASTRuleElement {

	private ASTTerm rawTerm;

	public ASTRuleReplace(@NonNull Token first, @NonNull ASTTerm rawTerm) {
		super(first);
		this.rawTerm = rawTerm;
		addChild(rawTerm);
	}

	public void visit(ASTVisitor v) throws ASTVisitException {
		v.visit(this);
	}
	
	@Override
	public Token getLocationToken() {
		return rawTerm.getLocationToken();
	}
	
	public ASTTerm getTerm() {
	    return (ASTTerm) getChildren().get(0);
	}

}
