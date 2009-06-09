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
 * This default visitor allows extending classes to define a default action on
 * the ast nodes.
 */
public abstract class ASTDefaultVisitor implements ASTVisitor {

    /**
     * Default action on AST nodes. This method is called by default on any AST
     * element
     * 
     * @param term
     *            the term to work on
     */
    protected abstract void defaultVisit(ASTElement term) throws ASTVisitException;

    public void visit(ASTApplicationTerm applicationTerm) throws ASTVisitException {
        defaultVisit(applicationTerm);
    }

    public void visit(ASTBinderTerm binderTerm) throws ASTVisitException {
        defaultVisit(binderTerm);
    }

    public void visit(ASTIdentifierTerm identifierTerm) throws ASTVisitException {
        defaultVisit(identifierTerm);
    }

    public void visit(ASTListTerm listTerm) throws ASTVisitException {
        defaultVisit(listTerm);
    }

    public void visit(ASTModalityTerm modalityTerm) throws ASTVisitException {
        defaultVisit(modalityTerm);
    }

    public void visit(ASTModAssignment modAssignment) throws ASTVisitException {
        defaultVisit(modAssignment);
    }

    public void visit(ASTModCompound modCompound) throws ASTVisitException {
        defaultVisit(modCompound);
    }

    public void visit(ASTModIf modIf) throws ASTVisitException {
        defaultVisit(modIf);
    }

    public void visit(ASTModSkip modSkip) throws ASTVisitException {
        defaultVisit(modSkip);
    }

    public void visit(ASTModWhile modWhile) throws ASTVisitException {
        defaultVisit(modWhile);
    }
    
    public void visit(ASTModSchema modSchema) throws ASTVisitException {
        defaultVisit(modSchema);
    }

    public void visit(ASTNumberLiteralTerm numberLiteralTerm) throws ASTVisitException {
        defaultVisit(numberLiteralTerm);
    }

    public void visit(ASTTypeApplication typeRef) throws ASTVisitException {
        defaultVisit(typeRef);
    }
    
    public void visit(ASTTypeVar asType) throws ASTVisitException {
        defaultVisit(asType);
    }

    public void visit(ASTOperatorIdentifierTerm operatorIdentifierTerm) throws ASTVisitException {
        defaultVisit(operatorIdentifierTerm);
    }

    public void visit(ASTFixTerm infixTerm) throws ASTVisitException {
        defaultVisit(infixTerm);
    }

    public void visit(ASTAsType asType) throws ASTVisitException {
        defaultVisit(asType);
    }
    
    public void visit(ASTSchemaVariableTerm schemaVariableTerm) throws ASTVisitException {
        defaultVisit(schemaVariableTerm);
    }
}
