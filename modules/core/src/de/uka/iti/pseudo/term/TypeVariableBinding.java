package de.uka.iti.pseudo.term;

import nonnull.NonNull;
import nonnull.Nullable;
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
    private Type boundType;

    public TypeVariableBinding(Kind kind, Type boundType, Term subterm) throws TermException {
        super(new Term[] { subterm }, Environment.getBoolType());
        
        if (!subterm.getType().equals(Environment.getBoolType())) {
            throw new TermException(
                    "TypeVariableBinding takes a boolean argument, not of type "
                            + subterm.getType());
        }
        
        if (!(boundType instanceof TypeVariable || boundType instanceof SchemaType)) {
            throw new TermException(
                    "TypeVariableBinding binds a type variable or a schema type, not "
                            + boundType);
        }

        this.kind = kind;
        this.boundType = boundType;
    }

    @Override
    public boolean equals(@Nullable Object object) {
        if (object instanceof TypeVariableBinding){
            TypeVariableBinding tvb = (TypeVariableBinding) object;
            if(tvb.getKind() != getKind())
                return false;
            
            if(!tvb.getBoundType().equals(getBoundType()))
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

    public Type getBoundType() {
        return boundType;
    }

    @Override
    public String toString(boolean typed) {
        
        return "(" + kind.image + " " + boundType + ";"
                + getSubterm(0).toString(typed) + (typed ? ") as bool" : ")");
    }

    @Override
    public void visit(TermVisitor visitor) throws TermException {
        visitor.visit(this);
    }

    /**
     * type variable bindings only have one subterm. return it
     * @return the only subterm of the type variable binding
     */
    public @NonNull Term getSubterm() {
        return getSubterm(0);
    }

}
