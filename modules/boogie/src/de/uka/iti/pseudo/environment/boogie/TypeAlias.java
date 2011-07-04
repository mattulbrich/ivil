package de.uka.iti.pseudo.environment.boogie;

import java.util.List;

import de.uka.iti.pseudo.environment.Sort;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeApplication;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.TypeVisitor;
import de.uka.iti.pseudo.term.creation.RebuildingTypeVisitor;

public class TypeAlias extends Type {

    private final Type definition;
    private final RebuildingTypeVisitor<Type[]> visitor;
    private final Type[] typeParameters;

    /**
     * 
     * @param typeParameters
     * @param definition
     * 
     * @param state
     *            is needed to look into map types and create new ones if needed
     */
    public TypeAlias(final Type[] typeParameters, Type definition, final EnvironmentCreationState state) {
        this.definition = definition;
        this.typeParameters = typeParameters;

        // type parameters need to be type variables
        for (int i = 0; i < typeParameters.length; i++) {
            assert typeParameters[i] instanceof TypeVariable : "illformed formal type parameters for TypeAlias";

            // ensure no parameter equals another
            for (int j = i + 1; j < typeParameters.length; j++)
                if (typeParameters[i].equals(typeParameters[j]))
                    throw new IllegalArgumentException("One can not create a TypeAlias with two equal arguments.["
                            + this + "]");
        }

        // type rebuilding just replaces type variables by respective parameters
        this.visitor = new RebuildingTypeVisitor<Type[]>() {
            @Override
            public Type visit(TypeApplication target, Type[] parameter) throws TermException {
                /*
                 * if (state.mapDB.hasType(target)) { // we have to look into a
                 * map Type[] domain = state.mapDB.getDomain(target); Type range
                 * = state.mapDB.getRange(target);
                 * 
                 * for (int i = 0; i < domain.length; i++) domain[i] =
                 * domain[i].accept(this, parameter);
                 * 
                 * range = range.accept(this, parameter);
                 * 
                 * try { return state.mapDB.getType(domain, range,
                 * state.mapDB.getParameters(target), target.getSort()
                 * .getDeclaration(), state); } catch (TypeSystemException e) {
                 * e.printStackTrace(); assert false : "internal error: " + e;
                 * return null; } } else
                 */ {

                    Sort sort = target.getSort();
                    List<Type> arguments = target.getArguments();
                    Type result[] = new Type[arguments.size()];
                    for (int i = 0; i < result.length; i++) {
                        result[i] = arguments.get(i).accept(this, parameter);
                    }

                    return TypeApplication.getInst(sort, result);
                }
            }
            @Override
            public Type visit(TypeVariable typeVariable, Type[] parameter) throws TermException {
                for (int i = 0; i < parameter.length; i++)
                    if (typeVariable.equals(typeParameters[i]))
                        return parameter[i];

                return typeVariable;
            }

        };
    }

    /**
     * Constructs a usable type where the parameters are used as constructor
     * arguments.
     * 
     * @param parameters
     * 
     * @return the type constructed by this alias and the parameters
     * 
     * @throws TermException
     *             can be created if type construction fails
     */
    public Type constructFrom(final Type[] parameters) throws TermException {
        if (parameters.length != typeParameters.length)
            throw new IllegalArgumentException("Missmatch in parameter length: got " + parameters.length
                    + ", but expected " + typeParameters.length);

        return definition.accept(visitor, parameters);
    }

    @Override
    public boolean equals(Object object) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append("[");
        for (int i = 0; i < typeParameters.length; i++) {
            if (i != 0)
                b.append(", ");
            b.append(typeParameters[i]);
        }
        b.append("]-> ");
        b.append(definition);

        return b.toString();
    }

    @Override
    public <R, A> R accept(TypeVisitor<R, A> visitor, A parameter) throws TermException {
        assert false : "a TypeAlias should not be visited";
        return null;
    }

}
