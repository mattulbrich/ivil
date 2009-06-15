/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.parser.program;

import de.uka.iti.pseudo.parser.ASTVisitException;

/**
 * This interface is part of the visitor pattern for program ast elements
 */

public interface ASTProgramVisitor {

    public void visit(ASTStatementList statementList) throws ASTVisitException;
    
    public void visit(ASTLabeledStatement labeledStatement) throws ASTVisitException;

    public void visit(ASTAssertStatement assertStatement) throws ASTVisitException;

    public void visit(ASTEndStatement endStatement) throws ASTVisitException;

    public void visit(ASTAssumeStatement assumeStatement) throws ASTVisitException;

    public void visit(ASTSkipStatement skipStatement) throws ASTVisitException;

    public void visit(ASTGotoStatement gotoStatement) throws ASTVisitException;

    public void visit(ASTAssignmentStatement assignmentStatement) throws ASTVisitException;
    
    public void visit(ASTSourceStatement sourceStatement) throws ASTVisitException;

}
