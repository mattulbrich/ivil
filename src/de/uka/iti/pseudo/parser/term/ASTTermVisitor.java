/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.parser.term;

import de.uka.iti.pseudo.parser.ASTVisitException;

/**
 * The Interface ASTVisitor is part of the visitor pattern for Term ASTs.
 * 
 * The implementing visit methods may throw ASTVisitExceptions if their
 * visitation fails.
 */
public interface ASTTermVisitor {

	void visit(ASTApplicationTerm applicationTerm) throws ASTVisitException;

	void visit(ASTBinderTerm binderTerm) throws ASTVisitException;

	void visit(ASTIdentifierTerm identifierTerm) throws ASTVisitException;

	void visit(ASTListTerm listTerm) throws ASTVisitException;
	
	void visit(ASTNumberLiteralTerm numberLiteralTerm) throws ASTVisitException;

	void visit(ASTOperatorIdentifierTerm operatorIdentifierTerm) throws ASTVisitException;

    void visit(ASTFixTerm infixTerm) throws ASTVisitException;

    void visit(ASTAsType asType) throws ASTVisitException;

    void visit(ASTSchemaVariableTerm schemaVariableTerm) throws ASTVisitException;

    void visit(ASTProgramTerm modalityTerm) throws ASTVisitException;

	void visit(ASTTypeApplication typeRef) throws ASTVisitException;
	
	void visit(ASTTypeVar typeVar) throws ASTVisitException;

}
