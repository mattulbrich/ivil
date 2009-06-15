package de.uka.iti.pseudo.term.statement;

import java.util.List;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.util.Util;

public abstract class Statement {

    private Term[] subTerms;
    private static Term[] NO_TERMS = new Term[0];

    public Statement(Term[] subTerms) throws TermException {
        this.subTerms = subTerms;
    }

    public Statement(Term subTerm) throws TermException {
        this.subTerms = new Term[] { subTerm };
    }

    public Statement() {
        subTerms = NO_TERMS;
    }

    /**
     * constructors should call this method to ensure that the first argument to
     * the statement is of boolean type.
     * 
     * @throws TermException
     *             the first argument to this statement is not of type booleanß
     */
    protected void ensureCondition() throws TermException {
        assert subTerms.length > 0;

        if (subTerms[0].getType().equals(Environment.getBoolType()))
            throw new TermException(
                    "This statement expects a boolean condition, but received "
                            + subTerms[0]);
    }
    
    public List<Term> getSubterms() {
        return Util.readOnlyArrayList(subTerms);
    }

    public abstract String toString(boolean typed);

    public abstract void visit(StatementVisitor visitor) throws TermException;

    public int countSubterms() {
        return subTerms.length;
    }
}
