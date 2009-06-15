package de.uka.iti.pseudo.term;

import javax.crypto.spec.PSource;

import de.uka.iti.pseudo.term.statement.Statement;

public class ProgramUpdate {
    
    int position;
    Statement statement;

    public ProgramUpdate(int position, Statement statement) {
        this.position = position;
        this.statement = statement;
    }

    public String toString(boolean typed) {
        return null;
    }

    public int getPosition() {
        return position;
    }

    public Statement getStatement() {
        return statement;
    }

}
