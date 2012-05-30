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

import de.uka.iti.pseudo.parser.ASTElement;
import de.uka.iti.pseudo.parser.ASTVisitException;

/**
 * The Class ASTFileDefaultVisitor provides a default 
 * method which is applied on every argument.
 */
public abstract class ASTFileDefaultVisitor implements ASTFileVisitor {

	/**
	 * Extending classes need to implement a default behaviour.
	 * 
	 * <p>This method will be called unless a visit method is overridden.
	 * 
	 * @param arg the file element to apply to
	 * 
	 * @throws ASTVisitException may be thrown by any implementation
	 */
	protected abstract void visitDefault(ASTElement arg) throws ASTVisitException;
	
	public void visit(ASTFile arg) throws ASTVisitException {
		visitDefault(arg);
	}

	public void visit(ASTIncludeDeclarationBlock arg) throws ASTVisitException {
		visitDefault(arg);
	}

	public void visit(ASTSortDeclarationBlock arg) throws ASTVisitException {
		visitDefault(arg);
	}

	public void visit(ASTSortDeclaration arg) throws ASTVisitException {
		visitDefault(arg);
	}

	public void visit(ASTFunctionDeclaration arg) throws ASTVisitException {
		visitDefault(arg);
	}

	public void visit(ASTFunctionDeclarationBlock arg) throws ASTVisitException {
		visitDefault(arg);
	}

	public void visit(ASTRule arg) throws ASTVisitException {
		visitDefault(arg);
	}

	public void visit(ASTRuleFind arg) throws ASTVisitException {
		visitDefault(arg);
	}

	public void visit(ASTRuleAssume arg) throws ASTVisitException {
		visitDefault(arg);
	}

	public void visit(ASTRuleReplace arg) throws ASTVisitException {
		visitDefault(arg);
	}
	
	public void visit(ASTRuleRemove ruleRemove) throws ASTVisitException {
	    visitDefault(ruleRemove);
	}

	public void visit(ASTRuleAdd arg) throws ASTVisitException {
		visitDefault(arg);
	}

	public void visit(ASTBinderDeclarationBlock arg) throws ASTVisitException {
		visitDefault(arg);
	}

	public void visit(ASTBinderDeclaration arg) throws ASTVisitException {
		visitDefault(arg);
	}

    public void visit(ASTWhereClause arg) throws ASTVisitException {
        visitDefault(arg);
    }
    
    public void visit(ASTLocatedTerm arg) throws ASTVisitException {
        visitDefault(arg);
    }

    public void visit(ASTGoalAction arg) throws ASTVisitException {
        visitDefault(arg);
    }

}
