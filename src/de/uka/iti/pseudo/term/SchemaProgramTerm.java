package de.uka.iti.pseudo.term;

import de.uka.iti.pseudo.term.statement.Statement;
import de.uka.iti.pseudo.util.Util;

public class SchemaProgramTerm extends ProgramTerm {
    
    private String schemaIdentifier;
    private Statement matchingStatement;

    public SchemaProgramTerm(String image, boolean terminating, Statement matchingStatement) throws TermException {
        super(terminating);
        this.schemaIdentifier = image;
        this.matchingStatement = matchingStatement;
    }

    public boolean equals(Object object) {
        if (object instanceof SchemaProgramTerm) {
            SchemaProgramTerm sch = (SchemaProgramTerm) object;
            return schemaIdentifier.equals(sch.schemaIdentifier) && 
                Util.equalOrNull(matchingStatement, sch.matchingStatement);
        }
        return false;
    }

    protected String getContentString(boolean typed) {
        String res = schemaIdentifier;
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

    public String getSchemaIdentifier() {
        return schemaIdentifier;
    }

    public Statement getMatchingStatement() {
        return matchingStatement;
    }

}
