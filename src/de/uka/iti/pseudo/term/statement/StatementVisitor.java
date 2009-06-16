package de.uka.iti.pseudo.term.statement;

import de.uka.iti.pseudo.term.TermException;

public interface StatementVisitor {

    void visit(AssertStatement assertStatement) throws TermException;

    void visit(AssignmentStatement assignmentStatement) throws TermException;

    void visit(AssumeStatement assumeStatement) throws TermException;

    void visit(EndStatement endStatement) throws TermException;

    void visit(GotoStatement gotoStatement) throws TermException;

    void visit(SkipStatement skipStatement) throws TermException;

}
