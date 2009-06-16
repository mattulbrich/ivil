package de.uka.iti.pseudo.environment;

import java.util.List;

import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.statement.EndStatement;
import de.uka.iti.pseudo.term.statement.Statement;
import de.uka.iti.pseudo.util.Util;

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
    
    private Statement[] statements;
    private SourceAnnotation[] sourceAnnotations;
    private LabelAnnotation[] labelAnnotations;
    
    public Program(List<Statement> statements,
            List<SourceAnnotation> sourceAnnotations,
            List<LabelAnnotation> labelAnnotations) throws EnvironmentException {
        this.statements = Util.listToArray(statements, Statement.class);
        this.sourceAnnotations = Util.listToArray(sourceAnnotations, SourceAnnotation.class);
        this.labelAnnotations = Util.listToArray(labelAnnotations, LabelAnnotation.class);
    }
    
    public Statement getStatement(int i) {
        if(i < 0)
            throw new IndexOutOfBoundsException();
        
        if(i >= statements.length)
            return OUT_OF_BOUNDS_STATEMENT;
        
        return statements[i];
    }

}
