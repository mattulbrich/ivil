package de.uka.iti.pseudo.environment;

import java.util.List;

import nonnull.NonNull;

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
            OUT_OF_BOUNDS_STATEMENT = new EndStatement(Environment.getTrue());
        } catch (TermException e) {
            // this cannot happen
            throw new Error(e);
        }
    }
    
    private String name;
    
    private Statement[] statements;
    private SourceAnnotation[] sourceAnnotations;
    private LabelAnnotation[] labelAnnotations;
    private ASTLocatedElement declaration;
    
    public Program(@NonNull String name,
            List<Statement> statements,
            List<SourceAnnotation> sourceAnnotations,
            List<LabelAnnotation> labelAnnotations,
            ASTLocatedElement declaration) throws EnvironmentException {
        this.statements = Util.listToArray(statements, Statement.class);
        this.sourceAnnotations = Util.listToArray(sourceAnnotations, SourceAnnotation.class);
        this.labelAnnotations = Util.listToArray(labelAnnotations, LabelAnnotation.class);
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
    
    public List<SourceAnnotation> getSourceAnnotations() {
        return Util.readOnlyArrayList(sourceAnnotations);
    }
    
    public List<LabelAnnotation> getLabelAnnotations() {
        return Util.readOnlyArrayList(labelAnnotations);
    }

    public int countStatements() {
        return statements.length;
    }

    public ASTLocatedElement getDeclaration() {
        return declaration;
    }

    public void dump() {
        System.out.println("    Source annotations");
        for (SourceAnnotation ann : sourceAnnotations) {
            System.out.println("      " + ann + " -> " + ann.getStatementNo());
        }
        
        System.out.println("    Labels");
        for (LabelAnnotation ann : labelAnnotations) {
            System.out.println("      " + ann + " -> " + ann.getStatementNo());
        }

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

}
