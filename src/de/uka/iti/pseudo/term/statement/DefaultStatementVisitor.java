package de.uka.iti.pseudo.term.statement;

import de.uka.iti.pseudo.term.TermException;

public abstract class DefaultStatementVisitor implements StatementVisitor {

    protected abstract void visitDefault(Statement statement);

    
    public void visit(AssertStatement assertStatement) throws TermException {
        visitDefault(assertStatement);
    }

   
    public void visit(AssignmentStatement assignmentStatement) throws TermException {
        visitDefault(assignmentStatement);
    }

    public void visit(AssumeStatement assumeStatement) throws TermException {
        visitDefault(assumeStatement);
    }

    public void visit(EndStatement endStatement) throws TermException {
        visitDefault(endStatement);
    }

    public void visit(GotoStatement gotoStatement) throws TermException {
        visitDefault(gotoStatement);
    }

    public void visit(SkipStatement skipStatement) throws TermException {
        visitDefault(skipStatement);
    }

}
