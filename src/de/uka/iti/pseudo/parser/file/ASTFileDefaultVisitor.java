/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.parser.file;

// TODO DOC

import de.uka.iti.pseudo.parser.ASTVisitException;

public abstract class ASTFileDefaultVisitor implements ASTFileVisitor {

	protected abstract void visitDefault(ASTFileElement arg) throws ASTVisitException;
	
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

	public void visit(ASTRawTerm arg) throws ASTVisitException {
		visitDefault(arg);
	}

	public void visit(ASTTypeRef arg) throws ASTVisitException {
		visitDefault(arg);
	}

	public void visit(ASTTypeVar arg) throws ASTVisitException {
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
