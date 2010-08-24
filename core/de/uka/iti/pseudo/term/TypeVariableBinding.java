package de.uka.iti.pseudo.term;

import de.uka.iti.pseudo.environment.Environment;

// TODO DOC

public class TypeVariableBinding extends Term {
    
    public static enum Kind {
        ALL("\\T_all"),
        EX("\\T_ex");
        public final String image;

        Kind(String image) {
            this.image = image;
        }
    };

    private Kind kind;
    private TypeVariable typeVariable;

    public TypeVariableBinding(Kind kind, TypeVariable typeVar, Term subterm) throws TermException {
        super(new Term[] { subterm }, Environment.getBoolType());
        
        if (!subterm.getType().equals(Environment.getBoolType())) {
            throw new TermException(
                    "TypeVariableBinding takes a boolean argument, not of type "
                            + subterm.getType());
        }
        
        this.kind = kind;
        this.typeVariable = typeVar;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof TypeVariableBinding){
            TypeVariableBinding tvb = (TypeVariableBinding) object;
            if(tvb.getKind() != getKind())
                return false;
            
            if(tvb.getTypeVariable().equals(getTypeVariable()))
                return false;
            
            if(!tvb.getSubterm(0).equals(getSubterm(0)))
                return false;
            
            return true;
        }
        return false;
    }

    /**
     * @return the kind
     */
    public Kind getKind() {
        return kind;
    }

    /**
     * @return the typeVariable
     */
    public TypeVariable getTypeVariable() {
        return typeVariable;
    }

    @Override
    public String toString(boolean typed) {
        
        return "(" + kind.image + " " + typeVariable + ";"
                + getSubterm(0).toString(typed) + (typed ? ") as bool" : ")");
    }

    @Override
    public void visit(TermVisitor visitor) throws TermException {
        visitor.visit(this);
    }

}
