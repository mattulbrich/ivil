/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.term.statement;

import de.uka.iti.pseudo.term.TermException;

/**
 * An interface which allows to implement the visitor pattern on
 * {@link Statement}s.
 * 
 * @see Statement#visit(StatementVisitor)
 */
public interface StatementVisitor {

    void visit(AssertStatement assertStatement) throws TermException;

    void visit(AssignmentStatement assignmentStatement) throws TermException;

    void visit(AssumeStatement assumeStatement) throws TermException;

    void visit(EndStatement endStatement) throws TermException;

    void visit(GotoStatement gotoStatement) throws TermException;

    void visit(SkipStatement skipStatement) throws TermException;

    void visit(HavocStatement havocStatement) throws TermException;

    void visit(UpdateStatement updateStatement) throws TermException;

}
