package de.uka.iti.pseudo.term;

import de.uka.iti.pseudo.term.statement.Statement;

public class ProgramUpdate {
    
    int position;
    Statement statement;

    public ProgramUpdate(int position, Statement statement) throws TermException {
        this.position = position;
        this.statement = statement;
        
        if(position < 0) {
            throw new TermException("program updates need positive position tags");
        }
    }

    public String toString(boolean typed) {
        return position + ":=" + statement.toString(typed);
    }

    public int getUpdatedIndex() {
        return position;
    }

    public Statement getStatement() {
        return statement;
    }

}
