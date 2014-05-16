/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.environment.creation;

import java.math.BigInteger;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.environment.Program;
import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.statement.GotoStatement;
import de.uka.iti.pseudo.term.statement.Statement;
import de.uka.iti.pseudo.util.TermUtil;

/**
 * A class which works on one program and allows to
 * <ul>
 * <li>insert instructions
 * <li>remove instructions
 * <li>change instructions/annotations
 * </ul>
 *
 * A modified program can be retrieved using the method
 * {@link #makeProgram(String)}.
 *
 * <p>
 * The provided environment may be enriched by new definitions of integer
 * literals.
 *
 * @see Program
 */
public class ProgramChanger {

    /**
     * The mutable list of statements.
     */
    private final LinkedList<Statement> statements;

    /**
     * The mutable list of statement annotations.
     */
    private final LinkedList</*@Nullable*/ String> statementAnnotations;

    /**
     * The underlying environment.
     */
    private final Environment env;

    /**
     * The referenced source file.
     */
    private @Nullable final URL sourceFile;

    /**
     * Instantiates a new program changer.
     *
     * @param program
     *            the program to modify
     * @param env
     *            the underlying environment
     */
    public ProgramChanger(@NonNull Program program, @NonNull Environment env) {
        this.env = env;
        this.sourceFile = program.getSourceFile();
        this.statements = new LinkedList<Statement>(program.getStatements());
        this.statementAnnotations =
                new LinkedList</*@Nullable*/ String>(program.getTextAnnotations());
    }

    /**
     * Insert a statement.
     *
     * The statement is inserted <i>before</i> the statement at
     * <code>index</code>. All existing gotos targeting <b>above</b> index are
     * incremented.
     *
     * <p>
     * Appends a statement to the statement list if index is equal to the length
     * of the list of statements.
     *
     * @param index
     *            a non-negative index into the list of statements.
     * @param statement
     *            the statement to be inserted
     *
     * @throws TermException
     *             if adding integer literals to the environment fails
     * @throws IndexOutOfBoundsException
     *             if the index is negative or beyond the end of the statement
     *             list.
     */
    public void insertAt(int index, Statement statement) throws TermException {
        insertAt(index, statement, null);
    }

    /**
     * Insert a statement.
     *
     * The statement is inserted <i>before</i> the statement at
     * <code>index</code>. All existing gotos targeting <b>above</b> index are
     * incremented.
     *
     * <p>
     * Appends a statement to the statement list if index is equal to the length
     * of the list of statements.
     *
     * @param index
     *            a non-negative index into the list of statements.
     * @param statement
     *            the statement to be inserted
     * @param annotation
     *            the annotation to be used for that statement
     *
     * @throws TermException
     *             if adding integer literals to the environment fails
     * @throws IndexOutOfBoundsException
     *             if the index is negative or beyond the end of the statement
     *             list.
     * @throws NullPointerException
     *             if statement is null
     */
    public void insertAt(int index, Statement statement, @Nullable String annotation)
            throws TermException {
        // statements.size() is ok here!
        if (index < 0 || index > statements.size()) {
            throw new IndexOutOfBoundsException("Index outside the program boundaries");
        }

        if (statement == null) {
            throw new NullPointerException();
        }

        updateGotoStatements(index + 1, +1);
        statements.add(index, statement);
        statementAnnotations.add(index, annotation);
    }

    /**
     * Replace a statement.
     *
     * @param index
     *            the index at which the statement is to be replaced, must be
     *            non-negative and smaller than the length of the list
     * @param statement
     *            the statement to be replaced
     *
     * @throws IndexOutOfBoundsException
     *             if the index is not within the bounds of the program
     * @throws NullPointerException
     *             if statement is null
     */
    public void replaceAt(int index, Statement statement) {
        if (index < 0 || index >= statements.size()) {
            throw new IndexOutOfBoundsException("Index outside the program boundaries");
        }

        if (statement == null) {
            throw new NullPointerException();
        }

        statements.set(index, statement);
    }

    /**
     * Replace a statement annotation.
     *
     * @param index
     *            the index at which the statement annotation is to be replaced,
     *            must be non-negative and smaller than the length of the list
     * @param annotation
     *            the new annotation
     *
     * @throws IndexOutOfBoundsException
     *             if the index is not within the bounds of the program
     */
    public void replaceAnnotationAt(int index, String annotation) {
        if (index < 0 || index >= statements.size()) {
            throw new IndexOutOfBoundsException("Index outside the program boundaries");
        }

        statementAnnotations.set(index, annotation);
    }

    /**
     * Replace a statement and its annotation.
     *
     * @param index
     *            the index at which the statement is to be replaced, must be
     *            non-negative and smaller than the length of the list
     * @param statement
     *            the statement to be replaced
     * @param annotation
     *            the new annotation
     *
     * @throws IndexOutOfBoundsException
     *             if the index is not within the bounds of the program
     * @throws NullPointerException
     *             if statement is null
     */
    public void replaceAt(int index, Statement statement, String annotation) {
        if (index < 0 || index >= statements.size()) {
            throw new IndexOutOfBoundsException("Index outside the program boundaries");
        }

        if (statement == null) {
            throw new NullPointerException();
        }

        statements.set(index, statement);
        statementAnnotations.set(index, annotation);
    }

    /**
     * Insert a statement.
     *
     * The statement is inserted <i>after</i> the statement at
     * <code>index</code>. All existing gotos targeting <b>above</b> index are
     * incremented.
     *
     * <p>
     * Appends a statement to the statement list if index is equal to the length
     * of the list of statements.
     *
     * @param index
     *            a non-negative index into the list of statements.
     * @param statement
     *            the statement to be inserted
     *
     * @throws TermException
     *             if adding integer literals to the environment fails
     * @throws IndexOutOfBoundsException
     *             if the index is negative or beyond the end of the statement
     *             list.
     */
    public void insertAfter(int index, Statement statement) throws TermException {
        insertAt(index, statement, null);
    }

    /**
     * Insert a statement.
     *
     * The statement is inserted <i>before</i> the statement at
     * <code>index</code>. All existing gotos targeting <b>above</b> index are
     * incremented.
     *
     * @param index
     *            a non-negative index into the list of statements.
     * @param statement
     *            the statement to be inserted
     * @param annotation
     *            the annotation to be used for that statement
     *
     * @throws TermException
     *             if adding integer literals to the environment fails
     * @throws IndexOutOfBoundsException
     *             if the index is negative or beyond the end of the statement
     *             list.
     * @throws NullPointerException
     *             if statement is null
     */
    public void insertAfter(int index, Statement statement,
            @Nullable String annotation) throws TermException {
        // statements.size() is ok here!
        if (index < 0 || index > statements.size()) {
            throw new IndexOutOfBoundsException("Index outside the program boundaries");
        }

        if (statement == null) {
            throw new NullPointerException();
        }

        updateGotoStatements(index + 1, +1);
        statements.add(index + 1 , statement);
        statementAnnotations.add(index + 1, annotation);
    }

    /**
     * Delete a statement from the statement list.
     *
     * The statement at the given index is removed. All existing gotos targeting
     * <b>above</b> index are decremented.
     *
     * <p>
     * The semantics of this operation is that deleting a <tt>skip</tt>
     * statement preserves the semantics of a program.
     *
     * @param index
     *            the index of the statement to be deleted. Must be non-negative
     *            and less than the length of the list.
     *
     * @throws TermException
     *             if adding integer literals to the environment fails
     * @throws IndexOutOfBoundsException
     *             if the index is negative or beyond the end of the statement
     *             list.
     */
    public void deleteAt(int index) throws TermException {
        if (index < 0 || index >= statements.size()) {
            throw new IndexOutOfBoundsException("Index outside the program boundaries");
        }

        updateGotoStatements(index + 1, -1);
        statements.remove(index);
        statementAnnotations.remove(index);
    }

    /**
     * Reads a statement from the statement list.
     *
     * @param index
     *            the index into the statement list
     *
     * @return the statement at index
     *
     * @throws IndexOutOfBoundsException
     *             if the index is negative or beyond the end of the statement
     *             list.
     */
    public Statement getStatementAt(int index) {
        if (index < 0 || index >= statements.size()) {
            throw new IndexOutOfBoundsException("Index outside the program boundaries");
        }

        return statements.get(index);
    }

    /**
     * Reads a statement annotation from the statement list.
     *
     * @param index
     *            the index into the statement list
     *
     * @return the annotation at index, null if not set
     *
     * @throws IndexOutOfBoundsException
     *             if the index is negative or beyond the end of the statement
     *             list.
     */
    public @Nullable String getAnnotationAt(int index) {
        if (index < 0 || index >= statementAnnotations.size()) {
            throw new IndexOutOfBoundsException("Index outside the program boundaries");
        }

        return statementAnnotations.get(index);
    }

    /**
     * Given the modified statement list and annotations, create a new program.
     *
     * <p>
     * The program has not yet been inserted into the environment. If you intend
     * to use it, call {@link Environment#addProgram(Program)}
     *
     * @param name
     *            the name to give to the new program.
     *
     * @return a freshly created immutable program.
     *
     * @throws EnvironmentException
     *             if the creation somehow fails.
     */
    public Program makeProgram(String name) throws EnvironmentException {
        Program p = new Program(name, sourceFile,
                statements, statementAnnotations, ASTLocatedElement.CREATED);
        return p;
    }

    /**
     * @return the length of the program, if one would assemble it right now.
     */
    public int getProgramLength() {
        return statements.size();
    }

    /**
     * Update goto statements: Every target at index or above is changed by
     * offset.
     *
     * @param index
     *            the index to change from
     * @param offset
     *            the offset to change by
     *
     * @throws TermException
     *             if adding integer literals to the environment fails
     */
    private void updateGotoStatements(int index, int offset) throws TermException {
        ListIterator<Statement> it = statements.listIterator();
        while (it.hasNext()) {
            Statement statement = it.next();
            if (statement instanceof GotoStatement) {
                GotoStatement gotoSt = (GotoStatement) statement;
                int srcLine = gotoSt.getSourceLineNumber();
                Term[] newTargets = updateGotoStatement(index, offset, gotoSt);
                if (newTargets != null) {
                    it.set(new GotoStatement(srcLine, newTargets));
                }
            }
        }
    }

    /**
     * Update a particular goto statement.
     *
     * The result is lazily created.
     *
     * @param index
     *            the index from which on to update
     * @param offset
     *            the offset to update by
     * @param gotoSt
     *            the goto statement to check
     *
     * @return the term[] the new array of targets or may be null if unchanged.
     *
     * @throws TermException
     *             if adding integer literals to the environment fails
     */
    private Term /*@Nullable*/ [] updateGotoStatement(int index, int offset, GotoStatement gotoSt)
            throws TermException {
        List<Term> orgTargets = gotoSt.getSubterms();
        Term /*@Nullable*/ [] newTargets = null;
        for (int i = 0; i < gotoSt.countSubterms(); i++) {
            int val = TermUtil.getIntLiteral(orgTargets.get(i));
            if (val >= index) {
                if (newTargets == null) {
                    newTargets = new Term[orgTargets.size()];
                    orgTargets.toArray(newTargets);
                }
                newTargets[i] = fromInt(val + offset);
            }
        }
        return newTargets;
    }

    /**
     * Make Term from integer value.
     *
     * @param number
     *            a non-negative number
     *
     * @return a term containing that number
     *
     * @throws TermException
     *             if adding integer literals to the environment fails
     */
    private Term fromInt(int number) throws TermException {
        Function f = env.getNumberLiteral(BigInteger.valueOf(number));
        return Application.getInst(f, Environment.getIntType(), new Term[0]);
    }

}
