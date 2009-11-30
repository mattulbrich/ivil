package de.uka.iti.pseudo.justify;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.Variable;
import de.uka.iti.pseudo.term.creation.RebuildingTermVisitor;

public class SchemaVarToVarVisitor extends RebuildingTermVisitor {

    private Map<SchemaVariable, Variable> map =
        new HashMap<SchemaVariable, Variable>();
    
    @Override 
    public void visit(SchemaVariable schemaVariable)
            throws TermException {
        Variable var = map.get(schemaVariable);
        if(var == null) {
            Type ty = schemaVariable.getType();
            String name = schemaVariable.getName();
            var = new Variable(name.substring(1), ty);
            map.put(schemaVariable, var);
        }
        resultingTerm = var;
    }
    

    /**
     * @return the resultingTerm
     */
    public Term getResultingTerm() {
        return resultingTerm;
    }

    public Collection<Variable> getVariables() {
        return map.values();
    }
    
}
