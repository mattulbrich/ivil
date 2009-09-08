package de.uka.iti.pseudo.term.statement;

import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;

public class HavocStatement extends Statement {
    
    public HavocStatement(int sourceLineNumber, Term parameter) throws TermException {
        super(sourceLineNumber, parameter);
        
        if(!(parameter instanceof SchemaVariable)) {
            if(parameter instanceof Application) {
                Function f = ((Application)parameter).getFunction();
                if(!f.isAssignable())
                    throw new TermException("can havoc only an assignable function: " + parameter);
            } else {
                throw new TermException("can havoc only an assignables or schema variables: " + parameter);   
            }
        }
        
    }

    public String toString(boolean typed) {
        return 
            "havoc " + getSubterms().get(0).toString(typed);
    }
    
    public void visit(StatementVisitor visitor) throws TermException {
        visitor.visit(this);
    }

}
