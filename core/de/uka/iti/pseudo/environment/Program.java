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

// TODO
public class Program {
    
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
    
    private String name;
    private File sourceFile;
    private ASTLocatedElement declaration;
    
    private Statement[] statements;
    private String[] statementAnnotations;

    
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
        assert Util.notNullArray(this.statementAnnotations);
    }
    
    public Statement getStatement(int i) {
        if(i < 0)
            throw new IndexOutOfBoundsException();
        
        if(i >= statements.length)
            return OUT_OF_BOUNDS_STATEMENT;
        
        return statements[i];
    }
    
    public String getTextAnnotation(int i) {
        if(i < 0)
            throw new IndexOutOfBoundsException();
        
        if(i >= statements.length)
            return null;
        
        return statementAnnotations[i];
    }


    public int countStatements() {
        return statements.length;
    }

    public ASTLocatedElement getDeclaration() {
        return declaration;
    }

    public void dump() {
        System.out.println("    Statements");
        for (int i = 0; i < statements.length; i++) {
            System.out.print("      " + i + ": " + statements[i]);
            if(statementAnnotations[i] != null)
                System.out.print("; \"" + statementAnnotations[i] + "\"");
            System.out.println();
        }
    }
    
    public String getName() {
        return name;
    }
    
    @Override public String toString() {
        return name;
    }

    public List<Statement> getStatements() {
        return Util.readOnlyArrayList(statements);
    }
    
    public List<String> getTextAnnotations() {
        return Util.readOnlyArrayList(statementAnnotations);
    }

    public File getSourceFile() {
        return sourceFile;
    }

}
