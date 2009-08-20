package de.uka.iti.pseudo.environment;

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
    private String sourceFile;
    private ASTLocatedElement declaration;
    
    private Statement[] statements;

    
    public Program(@NonNull String name, 
            @Nullable String sourceFile,
            List<Statement> statements,
            ASTLocatedElement declaration) throws EnvironmentException {
        this.statements = Util.listToArray(statements, Statement.class);
        this.declaration = declaration;
        this.name = name;
    }
    
    public Statement getStatement(int i) {
        if(i < 0)
            throw new IndexOutOfBoundsException();
        
        if(i >= statements.length)
            return OUT_OF_BOUNDS_STATEMENT;
        
        return statements[i];
    }
    
//    public List<LabelAnnotation> getLabelAnnotations() {
//        return Util.readOnlyArrayList(labelAnnotations);
//    }

    public int countStatements() {
        return statements.length;
    }

    public ASTLocatedElement getDeclaration() {
        return declaration;
    }

    public void dump() {
        System.out.println("    Source annotations");

//        System.out.println("    Labels");
//        for (LabelAnnotation ann : labelAnnotations) {
//            System.out.println("      " + ann + " -> " + ann.getStatementNo());
//        }

        System.out.println("    Statements");
        int i = 0;
        for (Statement st : statements) {
            System.out.println("      " + i++ + ": " + st);
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

    public String getSourceFile() {
        return sourceFile;
    }

}
