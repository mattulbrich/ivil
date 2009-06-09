package de.uka.iti.pseudo.term;

import de.uka.iti.pseudo.term.AssignModality.AssignTarget;
import nonnull.NonNull;

// TODO DOC

public class SchemaVariable extends BindableIdentifier implements AssignTarget {

    public static final String SCHEMA_PREFIX = "%";
    
    private String name;

    public SchemaVariable(String name, Type type) throws TermException {
        super(type);
        this.name = name;
        if(!name.startsWith(SCHEMA_PREFIX))
            throw new TermException("Schema variables need to have a name starting with " + SCHEMA_PREFIX);
    }
    
    @Override
    public String toString(boolean typed) {
        String retval = name;
        if(typed)
            retval += " as " + getType();
        return retval;
    }

    @Override 
    public void visit(TermVisitor visitor) throws TermException {
        visitor.visit(this);
    }
    
    public String getName() {
        return name;
    }

    @Override 
    public boolean equals(@NonNull Object object) {
        if (object instanceof SchemaVariable) {
            SchemaVariable sv = (SchemaVariable) object;
            return sv.getName().equals(getName()) && getType().equals(sv.getType());
        }
        return false;
    }

    /* 
     * this is for AssignTarget and returns the same as getType()
     */
    public Type getResultType() {
        return getType();
    }

}
