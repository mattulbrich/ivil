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
package de.uka.iti.pseudo.environment;

import java.io.File;
import java.util.Collection;
import java.util.List;

import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.statement.EndStatement;
import de.uka.iti.pseudo.term.statement.Statement;
import de.uka.iti.pseudo.util.Util;

// TODO: Auto-generated Javadoc
/**
 * The Class Program captures a sequence of statements and 
 */
public class Program {
    
    /**
     * The Constant OUT_OF_BOUNDS_STATEMENT.
     */
    private static final Statement OUT_OF_BOUNDS_STATEMENT;
    static {
        try {
            // this statement is not associated with a line number (therefore -1)
            OUT_OF_BOUNDS_STATEMENT = new EndStatement(-1, Environment.getTrue());
        } catch (TermException e) {
            // this cannot happen
            throw new Error(e);
        }
    }
    
    /**
     * The name.
     */
    private String name;
    
    /**
     * The source file.
     */
    private File sourceFile;
    
    /**
     * The declaration.
     */
    private ASTLocatedElement declaration;
    
    /**
     * The statements.
     */
    private Statement[] statements;
    
    /**
     * The statement annotations.
     */
    private String[] statementAnnotations;

    
    /**
     * Instantiates a new program.
     * 
     * @param name
     *                the name
     * @param sourceFile
     *                the source file
     * @param statements
     *                the statements
     * @param statementAnnotations
     *                the statement annotations
     * @param declaration
     *                the declaration
     * 
     * @throws EnvironmentException
     *                 the environment exception
     */
    public Program(@NonNull String name, 
            @Nullable File sourceFile,
            List<Statement> statements,
            List<String> statementAnnotations,
            ASTLocatedElement declaration) throws EnvironmentException {
        this.statements = Util.listToArray(statements, Statement.class);
        this.statementAnnotations = Util.listToArray(statementAnnotations, String.class);
        this.declaration = declaration;
        this.sourceFile = sourceFile;
        this.name = name;
        
        assert statementAnnotations.size() == statements.size();
        assert Util.notNullArray(this.statements);
    }
    
    /**
     * Gets the statement.
     * 
     * @param i
     *                the i
     * 
     * @return the statement
     */
    public Statement getStatement(int i) {
        if(i < 0)
            throw new IndexOutOfBoundsException();
        
        if(i >= statements.length)
            return OUT_OF_BOUNDS_STATEMENT;
        
        return statements[i];
    }
    
    /**
     * Gets the text annotation.
     * 
     * @param i
     *                the i
     * 
     * @return the text annotation
     */
    public String getTextAnnotation(int i) {
        if(i < 0)
            throw new IndexOutOfBoundsException();
        
        if(i >= statements.length)
            return null;
        
        return statementAnnotations[i];
    }


    /**
     * Count statements.
     * 
     * @return the int
     */
    public int countStatements() {
        return statements.length;
    }

    /**
     * Gets the declaration.
     * 
     * @return the declaration
     */
    public ASTLocatedElement getDeclaration() {
        return declaration;
    }

    /**
     * Dump.
     */
    public void dump() {
        System.out.println("    Statements");
        for (int i = 0; i < statements.length; i++) {
            System.out.print("      " + i + ": " + statements[i]);
            if(statementAnnotations[i] != null)
                System.out.print("; \"" + statementAnnotations[i] + "\"");
            System.out.println();
        }
    }
    
    /**
     * Gets the name.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override public String toString() {
        return name;
    }

    /**
     * Gets the statements.
     * 
     * @return the statements
     */
    public List<Statement> getStatements() {
        return Util.readOnlyArrayList(statements);
    }
    
    /**
     * Gets the text annotations.
     * 
     * @return the text annotations
     */
    public List<String> getTextAnnotations() {
        return Util.readOnlyArrayList(statementAnnotations);
    }

    /**
     * Gets the source file.
     * 
     * @return the source file
     */
    public File getSourceFile() {
        return sourceFile;
    }

}
