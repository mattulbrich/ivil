package de.uka.iti.pseudo.term;

public class SchemaVariable extends Term {

    private String name;

    public SchemaVariable(String name, Type type) {
        super(type);
        this.name = name;
    }
    
    @Override
    public String toString(boolean typed) {
        String retval = "%" + name;
        if(typed)
            retval += " as " + getType();
        return retval;
    }

    @Override
    protected void visit(TermVisitor visitor) {
        visitor.visit(this);
    }
    
    public String getName() {
        return name;
    }

}
