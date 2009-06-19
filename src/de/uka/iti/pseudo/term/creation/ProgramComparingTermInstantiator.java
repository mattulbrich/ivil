package de.uka.iti.pseudo.term.creation;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.term.SchemaProgram;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.UnificationException;
import de.uka.iti.pseudo.term.statement.Statement;
import de.uka.iti.pseudo.util.Util;

// TODO DOC
public class ProgramComparingTermInstantiator extends TermInstantiator {

    private Environment env;

    public ProgramComparingTermInstantiator(Map<String, Term> termMap,
            Map<String, Type> typeMap, Environment env) {
        super(termMap, typeMap);
        
        this.env = env;
    }

    protected void checkSchemaProgramInstantiation(
            SchemaProgram schema, LiteralProgramTerm prog)
            throws TermException {
        super.checkSchemaProgramInstantiation(schema, prog);
        
        if(schema.hasMatchingStatement()) {
            Statement schemaStatement = schema.getMatchingStatement();
            Statement statement = prog.getStatement(env);
            
            if(statement.getClass() != schemaStatement.getClass()) 
                throw new UnificationException("Incomparable statement types", statement, schemaStatement);
            
            List<Term> schemaParams = schemaStatement.getSubterms();
            for (int i = 0; i < schemaParams.size(); i++) {
                Term t = schemaParams.get(i);
                t.visit(this);
                Term instantiated = resultingTerm == null ? t : resultingTerm;
                Term original = statement.getSubterms().get(i);
                if(!instantiated.equals(original)) {
                    throw new UnificationException("Incomparable statement parameter", instantiated, original);
                }
            }
        }
        
    }

}
