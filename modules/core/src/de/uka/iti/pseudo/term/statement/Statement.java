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

import java.util.Arrays;
import java.util.List;

import nonnull.DeepNonNull;
import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.Program;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.util.Util;

/**
 * A statement describes one step of execution in a program. A program is a
 * sequence of statements.
 * 
 * Statements have {@link #subTerms} whose number and type varies from subclass
 * to subclass. Statements are also immutable objects.
 * 
 * As a means for better user feedback, statements carry a source line number
 * with which they refer to the source statement which is the reason for their
 * existence. This information is purely for information purposes and may be in
 * a later version moved to {@link Program} like the statement annotations.
 * 
 * There are four basic statement types which make up the programming language:
 * <ul>
 * <li>Assignment statements</li>
 * <li>{@code assert} statements</li>
 * <li>{@code assume} statements</li>
 * <li>{@code havoc} statements</li>
 * <li>{@code goto} statements</li>
 * </ul>
 * 
 * In addition to that, {@code end} and {@code skip} statements are implemented
 * whose semantic can be expressed by short sequences of basic statements. They
 * may become deprecated and dropped from the list of supported statements.
 * 
 * There are "syntactic sugar" convenience construcsts which are available to
 * the parser but which are not sustained to this point; they are translated
 * earlier.
 */
public abstract class Statement {

    /**
     * The source line number.
     */
    private int sourceLineNumber;
    
    /**
     * The array of subterms.
     */
    private @NonNull Term[] subTerms;
    
    /**
     * The empty array stands for "no arguments".
     */
    private static final Term[] NO_TERMS = new Term[0];

    /**
     * Instantiates a new statement with source line number and arguments.
     * 
     * The array given as an argument is cloned to ensure it is immutable.
     * 
     * @param sourceLineNumber
     *            the source line number to set for the statement
     * @param subTerms
     *            an array of argument terms
     */
    public Statement(int sourceLineNumber, @DeepNonNull Term[] subTerms) {
        this.subTerms = subTerms.clone();
        this.sourceLineNumber = sourceLineNumber;
    }

    /**
     * Instantiates a new statement with source line number and a single
     * argument.
     * 
     * @param sourceLineNumber
     *            the source line number to set for the statement
     * @param subTerm
     *            a single argument term
     */
    public Statement(int sourceLineNumber, @NonNull Term subTerm) {
        this.subTerms = new Term[] { subTerm };
        this.sourceLineNumber = sourceLineNumber;
    }

    /**
     * Instantiates a new statement with a source line number only.
     * 
     * Subterms are set to an empty array of terms (not <code>null</code>).
     * 
     * @param sourceLineNumber
     *            the source line number to set for the statement
     */
    public Statement(int sourceLineNumber) {
        this.sourceLineNumber = sourceLineNumber;
        this.subTerms = NO_TERMS;
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
    
    /**
     * Gets the argument terms of this statement.
     * 
     * @return argument terms as immutable list.
     */
    public List<Term> getSubterms() {
        return Util.readOnlyArrayList(subTerms);
    }

    /**
     * Retrieves a string representation of this statement. Uses
     * {@link Term#SHOW_TYPES} to decide whether or not types are to be
     * included.
     * 
     * @return a string representation of this statement
     * 
     */
    public @NonNull String toString() {
        return toString(Term.SHOW_TYPES);
    }

    /**
     * Retrieves a string representation of this statement. Uses the parameter
     * {@code typed} to decide whether or not types are to be included.
     * 
     * @return a string representation of this statement
     * 
     * @param typed
     *            a flag deciding whether types are to be included in the string
     *            or not.
     */
    public abstract String toString(boolean typed);

    /**
     * Two statements are equal iff they are of the same class and have the same
     * arguments.
     * 
     * @param object object to compare to.
     * 
     * @return <code>true</code> if this object is equal to the argument.
     */
    public boolean equals(@Nullable Object object) {
        if (object instanceof Statement) {
            Statement statement = (Statement) object;
            return statement.getClass() == getClass() &&
                Arrays.equals(subTerms, statement.subTerms);
        }
        return false;
    }
    
    /**
     * The hash code of a statement is the hashcode of the class xored with the
     * hash code of the argument list.
     * 
     * @return hash code for this statement.
     */
    @Override public int hashCode() {
        int h = getClass().hashCode();
        h ^= Util.readOnlyArrayList(subTerms).hashCode();
        return h;
    }

    /**
     * Retrieves the number of arguments to this statements.
     * 
     * @return the number of arguments provided to the constructor. 
     */
    public int countSubterms() {
        return subTerms.length;
    }

    /**
     * Gets the source line number of this statement.
     * 
     * @return the source line number
     */
    public int getSourceLineNumber() {
        return sourceLineNumber;
    }
    
    public abstract void visit(StatementVisitor visitor) throws TermException;
}
