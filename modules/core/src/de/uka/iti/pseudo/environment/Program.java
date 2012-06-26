/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.environment;

import java.net.URL;
import java.util.List;

import nonnull.DeepNonNull;
import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.statement.EndStatement;
import de.uka.iti.pseudo.term.statement.Statement;
import de.uka.iti.pseudo.term.statement.StatementVisitor;
import de.uka.iti.pseudo.util.Util;

/**
 * The Class Program is an immutable data class capturing a sequence of
 * statements and its text annotations.
 *
 * @see de.uka.iti.pseudo.environment.creation.ProgramChanger
 */
public final class Program {

    /**
     * The Constant OUT_OF_BOUNDS_STATEMENT is used when an index ouside the
     * bounds of the program indices is used.
     * It is an <code>assume false</code> statement
     */
    private static final Statement OUT_OF_BOUNDS_STATEMENT;
    static {
        try {
            // this statement is not associated with a line number (therefore -1)
            OUT_OF_BOUNDS_STATEMENT = new EndStatement(-1);
        } catch (TermException e) {
            // this cannot happen
            throw new Error(e);
        }
    }

    /**
     * The unique name of this program.
     */
    private final String name;

    /**
     * The source file associated to this program.
     * This may be null.
     */
    private @Nullable
    final URL sourceFile;

    /**
     * A reference to the declaration in the original AST.
     */
    private @NonNull
    final ASTLocatedElement declaration;

    /**
     * The sequence of statements as array.
     */
    private @DeepNonNull
    final Statement[] statements;

    /**
     * The statement annotations stored in an array of the same length.
     * Entries may be null, however.
     */
    private @Nullable
    final String /*@NonNull*/ [] statementAnnotations;

    //@ invariant statements.length == statementAnnotations.length;

    /**
     * Create a new program object.
     *
     * The number of statements and the number of statement annotations must be
     * equal.
     *
     * @param name
     *            name of the object
     * @param sourceFile
     *            reference to the source file
     * @param statements
     *            the sequence of statements
     * @param statementAnnotations
     *            the sequence of statement annotations. The collection may
     *            contain <code>null</code> values.
     * @param declaration
     *            the reference to the source declaration
     *
     * @throws EnvironmentException
     *             if the parameters do not specify a program
     */
    public Program(@NonNull String name,
            @Nullable URL sourceFile,
            List<Statement> statements,
            List</*@Nullable*/ String> statementAnnotations,
            @NonNull ASTLocatedElement declaration) throws EnvironmentException {
        this.statements = Util.listToArray(statements, Statement.class);
        this.statementAnnotations = Util.listToArray(statementAnnotations, String.class);
        this.declaration = declaration;
        this.sourceFile = sourceFile;
        this.name = name;

        assert statementAnnotations.size() == statements.size() :
            "illformed program: #annotations != #statements";

        assert Util.notNullArray(this.statements);
    }

    /**
     * Gets the text annotation for a statement at an index. The first statement
     * carries index 0.
     *
     * If the index is negative, an index-out-of-bounds exception is thrown. If
     * the index is above or equal to the number of statements,
     * <code>null</code> is returned.
     *
     * @param i
     *            index to retrieve statement for.
     *
     * @return the annotation for the statement at position i, may be null.
     *
     * @throws IndexOutOfBoundsException
     *             if <code>i &lt; 0 </code>
     */
    public @Nullable String getTextAnnotation(int i) {
        if(i < 0) {
            throw new IndexOutOfBoundsException();
        }

        if(i >= statements.length) {
            return null;
        }

        return statementAnnotations[i];
    }


    /**
     * Gets the reference to the AST declaration.
     *
     * @return the declaration reference
     */
    public @NonNull ASTLocatedElement getDeclaration() {
        return declaration;
    }

    /**
     * Gets the name of this program.
     *
     * @return the name of the program
     */
    public @NonNull String getName() {
        return name;
    }

    /*
     * returns the name.
     *
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override public String toString() {
        return name;
    }

    /**
     * Gets the sequence of statements as an unmodifiable list.
     *
     * @return an unmodifiable list of statements.
     */
    public List<Statement> getStatements() {
        return Util.readOnlyArrayList(statements);
    }

    /**
     * Visit all statements in this program by a statement visitor.
     *
     * The order is the natural order in the program
     *
     * @param visitor
     *            visitor to apply to statements
     * @throws TermException
     *             can be thrown by the visitor
     */
    public void visitStatements(@NonNull StatementVisitor visitor) throws TermException {
        for (Statement statement : statements) {
            statement.accept(visitor);
        }
    }

    /**
     * Count the statements in this program.
     *
     * This query method always returns the same number for this object.
     *
     * @return the number of statements (greater or equal 0)
     */
    public int countStatements() {
        return statements.length;
    }

    /**
     * Gets the statement at an index. The first statement carries index 0.
     *
     * If the index is negative, an index-out-of-bounds exception is thrown. If
     * the index is above or equal to the number of statements, the constant
     * {@link #OUT_OF_BOUNDS_STATEMENT} is returned.
     *
     * @param i
     *            index to retrieve statement for.
     *
     * @return the statement at position i.
     * @throws IndexOutOfBoundsException
     *             if <code>i &lt; 0 </code>
     */
    public @NonNull Statement getStatement(int i) {
        if(i < 0) {
            throw new IndexOutOfBoundsException();
        }

        if(i >= statements.length) {
            return OUT_OF_BOUNDS_STATEMENT;
        }

        return statements[i];
    }

    /**
     * Gets the sequence of text annotations as an unmodifiable list.
     *
     * @return an unmodifiable list of text annotations
     */
    public List</*@Nullable*/String> getTextAnnotations() {
        return Util.readOnlyArrayList(statementAnnotations);
    }

    /**
     * Gets the reference to the source file if set. If non set, return
     * <code>null</code>.
     *
     * @return the reference to the source file, may be null
     */
    public @Nullable URL getSourceFile() {
        return sourceFile;
    }

    /**
     * Dump this program to stdout. For debug purposes.
     */
    public void dump() {
        System.out.println("    Statements");
        for (int i = 0; i < statements.length; i++) {
            System.out.print("      " + i + ": " + statements[i]);
            if(statementAnnotations[i] != null) {
                System.out.print("; \"" + statementAnnotations[i] + "\"");
            }
            System.out.println();
        }
    }

}
