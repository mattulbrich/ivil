package de.uka.iti.pseudo.term;

import de.uka.iti.pseudo.term.statement.Statement;
import de.uka.iti.pseudo.util.Util;

public class SchemaProgram extends ProgramTerm {
    
    private Statement matchingStatement;

    public SchemaProgram(SchemaVariable schemaVariable, boolean terminating, Statement matchingStatement) throws TermException {
        super(new Term[] { schemaVariable }, terminating);
        this.matchingStatement = matchingStatement;
    }

    public boolean equals(Object object) {
        if (object instanceof SchemaProgram) {
            SchemaProgram sch = (SchemaProgram) object;
            return getSchemaVariable().equals(sch.getSchemaVariable()) && 
                Util.equalOrNull(matchingStatement, sch.matchingStatement);
        }
        return false;
    }

    protected String getContentString(boolean typed) {
        String res = getSchemaVariable().toString();
        if(hasMatchingStatement())
            res += ": " + matchingStatement.toString(typed);
        return res;
    }

    public boolean hasMatchingStatement() {
        return matchingStatement != null;
    }

    public void visit(TermVisitor visitor) throws TermException {
        visitor.visit(this);
    }

    public SchemaVariable getSchemaVariable() {
        return (SchemaVariable) getSubterm(0);
    }

    public Statement getMatchingStatement() {
        return matchingStatement;
    }

}
