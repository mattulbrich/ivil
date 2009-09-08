package de.uka.iti.pseudo.term.statement;

import java.util.Arrays;
import java.util.List;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.util.Util;

public abstract class Statement {

    private int sourceLineNumber;
    private Term[] subTerms;
    private static Term[] NO_TERMS = new Term[0];

    public Statement(int sourceLineNumber, Term[] subTerms) throws TermException {
        this.subTerms = subTerms;
        this.sourceLineNumber = sourceLineNumber;
    }

    public Statement(int sourceLineNumber, Term subTerm) throws TermException {
        this(sourceLineNumber, new Term[] { subTerm });
    }

    public Statement() {
        subTerms = NO_TERMS;
    }

    /**
     * constructors should call this method to ensure that the first argument to
     * the statement is of boolean type.
     * 
     * @throws TermException
     *             the first argument to this statement is not of type boolean
     */
    protected void ensureCondition() throws TermException {
        assert subTerms.length > 0;

        if (!subTerms[0].getType().equals(Environment.getBoolType()))
            throw new TermException(
                    "This statement expects a boolean condition, but received "
                            + subTerms[0]);
    }
    
    public List<Term> getSubterms() {
        return Util.readOnlyArrayList(subTerms);
    }
    
    public String toString() {
        return toString(Term.SHOW_TYPES);
    }

    public abstract String toString(boolean typed);
    
    public boolean equals(Object object) {
        if (object instanceof Statement) {
            Statement statement = (Statement) object;
            return statement.getClass() == getClass() &&
                Arrays.equals(subTerms, statement.subTerms);
        }
        return false;
    }

    public abstract void visit(StatementVisitor visitor) throws TermException;

    public int countSubterms() {
        return subTerms.length;
    }

    public int getSourceLineNumber() {
        return sourceLineNumber;
    }
}
