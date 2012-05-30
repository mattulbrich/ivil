/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.parser.program;

import de.uka.iti.pseudo.parser.ASTVisitException;

/**
 * This interface is part of the visitor pattern for program ast elements
 */

public interface ASTProgramVisitor {

    public void visit(ASTLabelStatement labeledStatement) throws ASTVisitException;

    public void visit(ASTAssertStatement assertStatement) throws ASTVisitException;

    public void visit(ASTEndStatement endStatement) throws ASTVisitException;

    public void visit(ASTAssumeStatement assumeStatement) throws ASTVisitException;

    public void visit(ASTSkipStatement skipStatement) throws ASTVisitException;

    public void visit(ASTGotoStatement gotoStatement) throws ASTVisitException;

    public void visit(ASTHavocStatement havocStatement) throws ASTVisitException;

    public void visit(ASTAssignment assignmentStatement) throws ASTVisitException;
    
    public void visit(ASTAssignmentStatement astAssignmentStatement) throws ASTVisitException;
 
    public void visit(ASTSchematicAssignmentStatement astSchematicAssignmentStatement) throws ASTVisitException;
    
    public void visit(ASTSourceLineStatement sourceLineStatement) throws ASTVisitException;

}
