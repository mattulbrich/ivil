package de.uka.iti.pseudo.term;

import java.util.Iterator;
import java.util.Map;

import nonnull.NonNull;
import de.uka.iti.pseudo.environment.Sort;
import de.uka.iti.pseudo.term.creation.DefaultTypeVisitor;
import de.uka.iti.pseudo.term.creation.RebuildingTypeVisitor;
import de.uka.iti.pseudo.util.Log;
import de.uka.iti.pseudo.util.RewindMap;

/**
 * The Class TypeVariableUnifier is used to unify two types using Robinson's
 * algorithm (bidirectionally). MOST GENERAL
 *
 * This visitor is not for matching types during rule application but to compare
 * unschematic types. Types must hence not contain schema types.
 *
 * The result is stored in a map from type variables to types. If a schematic
 * type is encountered or two sorts are incomparable, a
 * {@link UnificationException} is thrown during unification.
 *
 * <p>
 * This class unifies {@link TypeVariable}s against applications. To unify
 * schema type against other types, use class
 * {@link de.uka.iti.pseudo.term.creation.TypeUnification}.
 *
 */
public class TypeVariableUnification {

    /**
     * A visitor to detect schema types.
     * It throws a {@link TermException} if it finds one.
     */
    private final static TypeVisitor<Void, TypeVariable> TYPEVAR_DETECTOR =
            new DefaultTypeVisitor<TypeVariable>() {
        @Override
        public Void visit(TypeVariable tv, TypeVariable searchFor) throws TermException {
            if(tv.equals(searchFor)) {
                throw new TermException("TypeVariable found!");
            }
            return null;
        }
    };

    private final RewindMap<TypeVariable, Type> typeVarMap =
            new RewindMap<TypeVariable, Type>();

    /**
     * This visitor is used to actually apply the substitution.
     */
    private final TypeVisitor<Type, Void> instantiator = new RebuildingTypeVisitor<Void>() {
        @Override
        public Type visit(TypeVariable typeVariable, Void arg) {
            Type replace = typeVarMap.get(typeVariable);
            if (replace != null) {
                return replace;
            } else {
                return typeVariable;
            }
        };
    };

    private final TypeVisitor<Void, Type> unifier =
            new TypeVisitor<Void, Type>() {

        @Override
        public Void visit(SchemaType schemaTypeVariable, Type argument) throws TermException {
            throw new TermException("Schema types must not appear " +
                    "in the unification of user types");
        }

        @Override
        public Void visit(TypeVariable typeVariable, Type argument) throws TermException {
            if (typeVarMap.containsKey(typeVariable)) {
                typeVarMap.get(typeVariable).accept(this, argument);
            } else {
                // type variable is not assigned
                // now add it to the map if occur checks passed and not schematic
                if(argument instanceof SchemaType) {
                    throw new TermException("Schema types must not appear " +
                            "in the unification of user types");
                }
                Type inst = instantiate(argument);
                if (occursIn(typeVariable, inst)) {
                    throw new UnificationException("Cannot unify (occur check)",
                            typeVariable, inst);
                }
                addMapping(typeVariable, inst);
            }
            return null;
        }

        @Override
        public Void visit(TypeApplication app, Type parameter)
                throws TermException {

            if(parameter instanceof TypeVariable) {
                visit((TypeVariable)parameter, app);

            } else if (parameter instanceof TypeApplication) {
                TypeApplication otherApp = (TypeApplication) parameter;
                Sort sort1 = app.getSort();
                Sort sort2 = otherApp.getSort();
                if(sort1 != sort2) {
                    throw new UnificationException("Incompatible sorts: " + sort1 + " and " +
                                sort2, app, parameter);
                }

                //
                // now, unify the arguments
                Iterator<Type> arg1 = app.getArguments().iterator();
                Iterator<Type> arg2 = otherApp.getArguments().iterator();
                while(arg1.hasNext()) {
                    arg1.next().accept(this, arg2.next());
                    assert arg1.hasNext() == arg2.hasNext() : "Both lists must have same length";
                }

            } else {
                throw new TermException("Schema types must not appear in " +
                        "the unification of user types");
            }

            return null;
        }
    };

    protected boolean occursIn(TypeVariable typeVariable, Type type) {
        try {
            type.accept(TYPEVAR_DETECTOR, typeVariable);
            return false;
        } catch (TermException e) {
            return true;
        }
    }

    /**
     * Adds a mapping.
     *
     * We have to update all existing mappings afterwards. We apply the new assignment
     * to all instantiations but to the one to type.
     *
     * @param tv the schema type variable.
     * @param type the type to instantiate for it.
     * @throws UnificationException apparently not thrown
     */
    protected void addMapping(final @NonNull TypeVariable tv,
            final @NonNull Type type) throws UnificationException {

        assert typeVarMap.get(tv) == null;
        assert !occursIn(tv, type);

        TypeVisitor<Type, Void> stvInst = new RebuildingTypeVisitor<Void>() {
            @Override
            public Type visit(TypeVariable typeVariable, Void param) {
                if(typeVariable.equals(tv)) {
                    return type;
                } else {
                    return typeVariable;
                }
            };
        };

        typeVarMap.put(tv, tv);
        for (Map.Entry<TypeVariable, Type> entry : typeVarMap.entrySet()) {
            Type res;
            try {
                res = entry.getValue().accept(stvInst, null);
            } catch (TermException e) {
                // not thrown in that code
                throw new Error(e);
            }
            entry.setValue(res);
        }
    }

    public void unify(Type type1, Type type2) throws TermException {
        int rewindPos = typeVarMap.getRewindPosition();
        try {
            type1.<Void,Type>accept(unifier, type2);
        } catch (TermException e) {
            Log.stacktrace(e);
            typeVarMap.rewindTo(rewindPos);
            throw e;
        }
    }

    public RewindMap<TypeVariable, Type> getMap() {
        return typeVarMap;
    }

    public Type instantiate(Type argument) throws TermException {
        return argument.accept(instantiator, null);
    }

}