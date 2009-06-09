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

public interface ASTVisitor {

	void visit(ASTApplicationTerm applicationTerm) throws ASTVisitException;

	void visit(ASTBinderTerm binderTerm) throws ASTVisitException;

	void visit(ASTIdentifierTerm identifierTerm) throws ASTVisitException;

	void visit(ASTListTerm listTerm) throws ASTVisitException;

	void visit(ASTModalityTerm modalityTerm) throws ASTVisitException;

	void visit(ASTModAssignment modAssignment) throws ASTVisitException;

	void visit(ASTModCompound modCompound) throws ASTVisitException;

	void visit(ASTModIf modIf) throws ASTVisitException;

	void visit(ASTModSkip modSkip) throws ASTVisitException;

	void visit(ASTModWhile modWhile) throws ASTVisitException;

	void visit(ASTNumberLiteralTerm numberLiteralTerm) throws ASTVisitException;

	void visit(ASTTypeApplication typeRef) throws ASTVisitException;
	
	void visit(ASTTypeVar typeVar) throws ASTVisitException;

	void visit(ASTOperatorIdentifierTerm operatorIdentifierTerm) throws ASTVisitException;

	void visit(ASTFixTerm infixTerm) throws ASTVisitException;

    void visit(ASTAsType asType) throws ASTVisitException;

    void visit(ASTSchemaVariableTerm schemaVariableTerm) throws ASTVisitException;

    

}
