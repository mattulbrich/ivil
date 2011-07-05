package de.uka.iti.pseudo.environment.boogie;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.Sort;
import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.term.MapType;
import de.uka.iti.pseudo.term.SchemaType;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeApplication;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.TypeVisitor;

/**
 * This class maps map types to ivil types. Map types are normalised to ensure
 * correct treatment of type equivalence.
 * 
 * @author timm.felden@felden.com
 * 
 *         TODO document silently assumed invariants, which make this
 *         translation work
 */
public final class MapTypeDatabase {

    /**
     * This class is the unfolded representation of a map. It is used to compare
     * map types and to assign the
     * 
     * @author timm.felden@felden.com
     */
    static class BoogieMap extends MapType {
        
        public BoogieMap(List<TypeVariable> boundVars, List<Type> domain, Type range,
                ASTLocatedElement declaringLocation) {
            super(boundVars, domain, range, declaringLocation);
        }

        public Boolean accept(BoogieMapEqualityVisitor visitor, Type parameter) throws TermException {
            return visitor.visit(this, parameter);
        }

        static class BoogieMapEqualityVisitor implements TypeVisitor<Boolean, Type> {


            @Override
            public Boolean visit(TypeApplication app, Type parameter) throws TermException {
                if (!(parameter instanceof TypeApplication))
                    return false;
                
                TypeApplication p = (TypeApplication) parameter;
                
                if (!p.getSort().equals(app.getSort()))
                    return false;
                
                boolean result = true;
                for (int i = 0; i < p.getArguments().size() && result; i++)
                    result = app.getArguments().get(i).accept(this, p.getArguments().get(i));
                    
                return result;
            }

            public Boolean visit(BoogieMap map, Type parameter) throws TermException {
                if (!(parameter instanceof BoogieMap))
                    return false;

                BoogieMap p = (BoogieMap) parameter;

                if (p.boundVars.size() != map.boundVars.size() || p.domain.size() != map.domain.size())
                    return false;
                
                pushNames(map.boundVars);
                
                boolean result = map.range.accept(this, p.range);

                for (int i = 0; i < map.domain.size() && result; i++)
                    result = map.domain.get(i).accept(this, p.domain.get(i));

                popNames();

                return result;
            }

            @Override
            public Boolean visit(TypeVariable var, Type parameter) throws TermException {
                if (!(parameter instanceof TypeVariable))
                    return false;

                TypeVariable p = (TypeVariable) parameter;

                Map<TypeVariable, TypeVariable> map = containsName(var);
                if(null!=map){
                    if (null == map.get(var))
                        return null == map.put(var, p);
                    else
                        return map.get(var).equals(p);
                }
                else
                    return var.equals(p);
            }

            /**
             * @note schema types are not expected to occur in maps
             */
            @Override
            public Boolean visit(SchemaType schemaType, Type parameter) throws TermException {
                return schemaType.equals(parameter);
            }


            private final LinkedList<Map<TypeVariable, TypeVariable>> variableRenaming = new LinkedList<Map<TypeVariable, TypeVariable>>();

            private void pushNames(List<TypeVariable> boundVars) {
                HashMap<TypeVariable, TypeVariable> map = new HashMap<TypeVariable, TypeVariable>();
                for (TypeVariable v : boundVars)
                    map.put(v, null);

                variableRenaming.push(map);
            }

            private void popNames() {
                variableRenaming.pop();
            }

            private Map<TypeVariable, TypeVariable> containsName(TypeVariable var) {
                for (Map<TypeVariable, TypeVariable> m : variableRenaming)
                    if (m.containsKey(var))
                        return m;
                return null;
            }
        }

        @Override
        public boolean equals(Object object) {
            if (!(object instanceof BoogieMap))
                return false;

            BoogieMap m = (BoogieMap) object;
            try {
                return new BoogieMapEqualityVisitor().visit(this, m);
            } catch (TermException e) {
                return false;
            }
        }

        /**
         * In order to make equal map types equal, one has to do the following:
         * <ul>
         * <li>for each map a representing map is constructed
         * <li>the map mechanism is still used as intended, but in general more
         * arguments have to be added
         * <li>domain and range will be modified recursively according to:
         * <ul>
         * <li>bound type variables are treated as before
         * <li>unbound type variables don't get their own arguments any longer
         * <li>type applications, that do not contain bound type variables are
         * transformed into a new argument of the type constructor. In order to
         * create the desired type, the type application is used as respective
         * argument.
         * <li>type applications, that contain bound type variables contribute
         * to the structure of the map, as such map types can not be made equal
         * to other maps by using what ever arguments to unbound type variables
         * </ul>
         * </ul>
         * 
         * Some examples:
         * <ul>
         * <li>{@literal <a>[ref a]a => map : ref a-> a}
         * <li> {@literal <a>[ref aint]a => map(int->b) : ref a, b -> a}
         * <li> {@literal <b>[b]<a>[ref a b]a => map : b-> map'(b) where map'(b)
         * : ref a b -> a}
         * <li> {@literal [int]int => map(int->a, int->b) : a -> b * }
         * </ul>
         * 
         * @note (boogie style) map equality checks are still needed to
         *       guarantee that maps such as {@literal <a>[a]a} and
         *       {@literal<b>[b]b} are represented by the same map type
         */
        public Type flatten(final Environment env, final String desiredName, final MapTypeDatabase mapDB) throws EnvironmentException, TermException {
            
            // //
            // 1. create representing map type

            LinkedList<Type> omittedTypes = new LinkedList<Type>();
            // note created typevaribales are named "_<ommitedTypes.size()>"

            // note: no list for bound type variables is needed, as the same
            // variables will be bound
            LinkedList<Type> dom = new LinkedList<Type>();
            for (Type t : domain)
                dom.add(generalize(t, omittedTypes, env, mapDB));

            Type ran = generalize(range, omittedTypes, env, mapDB);

            // //
            // 2. flatten representing map type
            BoogieMap representation = new BoogieMap(boundVars, dom, ran, declaringLocation);
            TypeApplication flatRepresentation;

            if (mapDB.mapTo.containsKey(representation)) {
                flatRepresentation = (TypeApplication) mapDB.mapTo.get(representation);
            } else {
                flatRepresentation = (TypeApplication) new MapType(representation.getBoundVars(), representation
                        .getDomain(), representation.getRange(), declaringLocation).flatten(env, null);
                mapDB.mapTo.put(representation, flatRepresentation);
                mapDB.mapFrom.put(flatRepresentation.getSort(), representation);
            }

            // //
            // 3. create actual result type by inserting omitted branches into
            // arguments

            return env.mkType(flatRepresentation.getSort().getName(), omittedTypes
                    .toArray(new Type[omittedTypes.size()]));
        }

        /**
         * does the actual generalization needed in flatten.
         * 
         * @note new parameters are named _%i, as such variable names can not be
         *       the result of any legal type parameter name after escaping it
         *       propperly
         */
        private Type generalize(Type t, LinkedList<Type> omittedTypes, Environment env, MapTypeDatabase mapDB)
                throws EnvironmentException,
                TermException {
            if (t instanceof TypeVariable) {
                if (boundVars.contains(t)) {
                    return t;
                }

                // unbound type variable detected, add argument
                Type rval = TypeVariable.getInst("_" + omittedTypes.size());
                omittedTypes.add(t);
                return rval;

            } else if (t instanceof TypeApplication) {
                if (ApplicationContainsBoundVars(t)) {
                    TypeApplication app = (TypeApplication) t;
                    
                    Type[] args = new Type[app.getSort().getArity()];
                    for (int i = 0; i < args.length; i++)
                        args[i] = generalize(app.getArguments().get(i), omittedTypes, env, mapDB);
                    return env.mkType(app.getSort().getName(), args);

                } else {
                    Type rval = TypeVariable.getInst("_" + omittedTypes.size());
                    omittedTypes.add(t);
                    return rval;    
                }

            } else if (t instanceof BoogieMap) {
                return generalize(((BoogieMap) t).flatten(env, null, mapDB), omittedTypes, env, mapDB);
            } else {
                assert false : "generalization of unexpected type:" + t.getClass().getCanonicalName();
                return null;
            }
        }

        /**
         * checks if locally bound vars occur in a type. It is not relevant, if
         * type variables, that are bound by other maps occur. Because inner
         * variable declarations can not shadow outer variables, it is not
         * important to keep track of the list of bound variables, because the
         * relevant set of bound variables does not change.
         */
        private boolean ApplicationContainsBoundVars(Type t) {
            if (t instanceof TypeVariable) {
                return boundVars.contains(t);
            } else if (t instanceof TypeApplication) {
                for (Type s : ((TypeApplication) t).getArguments()) {
                    if (ApplicationContainsBoundVars(s))
                        return true;
                }
                return false;

            } else if (t instanceof BoogieMap) {
                BoogieMap m = (BoogieMap) t;
                for (Type s : m.domain)
                    if (ApplicationContainsBoundVars(s))
                        return true;
                
                return ApplicationContainsBoundVars(m.range);
            } else {
                assert false : "generalization of unexpected type:" + t.getClass().getCanonicalName();
                return false;
            }
        }

        /**
         * @note the hash code is very bad for boogie maps, as it is hard to
         *       guarantee that two equal maps have the same hash code for
         *       useful hash codes.
         */
        @Override
        public int hashCode() {
            return 100 * boundVars.size() + domain.size();
        }
    }

    /**
     * this direction is needed to add new map types
     */
    private final Map<BoogieMap, Type> mapTo = new HashMap<BoogieMap, Type>();
    private final Map<Sort, BoogieMap> mapFrom = new HashMap<Sort, BoogieMap>();

    private final Environment env;

    public MapTypeDatabase(Environment env) {
        this.env = env;
    }

    /**
     * @note Map DB has to be type parameter name agnostic, as this would break
     *       typing. If type parameter renaming is required, a mechanism is
     *       needed that renames type parameters, before(or during) the type map
     *       is built, by structure.
     * 
     * @param node
     *            the node that caused the creation of this type
     * @param state
     *            the state is used to create new rules, etc., for maybe created
     *            map types
     * 
     * 
     * @return a type with no arguments built from a sort map<%i>
     * @throws TypeSystemException
     *             If the defined type is ill-formed
     */
    public Type getType(Type[] domain, Type range, TypeVariable[] parameters, ASTLocatedElement node,
            EnvironmentCreationState state) throws TypeSystemException {

        // create an unfolded map
        BoogieMap entry = new BoogieMap(Arrays.asList(parameters), Arrays.asList(domain), range,
                node);

        // add a new map to the table
        return addMapType(entry, node);
    }

    /**
     * requires definition to be a map type
     * 
     * @return a copy of the domain array
     */
    public Type[] getDomain(Type definition) {
        assert hasType(definition) : "requires definition to be a map type";

        final List<Type> d = mapFrom.get(((TypeApplication) definition).getSort()).getDomain();

        return d.toArray(new Type[d.size()]);
    }

    /**
     * requires definition to be a map type
     * 
     * @return the range of definition
     */
    public Type getRange(Type definition) {
        assert hasType(definition) : "requires definition to be a map type";

        return mapFrom.get(((TypeApplication) definition).getSort()).getRange();
    }

    /**
     * requires definition to be a map type
     * 
     * @return the parameters of definition
     */
    public TypeVariable[] getParameters(Type definition) {
        assert hasType(definition) : "requires definition to be a map type";

        List<TypeVariable> p = mapFrom.get(((TypeApplication) definition).getSort()).getBoundVars();

        return p.toArray(new TypeVariable[p.size()]);
    }

    // used by inference path
    BoogieMap getBoogieMap(Type definition) {
        assert hasType(definition) : "requires definition to be a map type (was : " + definition + ")";

        return mapFrom.get(((TypeApplication) definition).getSort());
    }

    /**
     * Checks if the supplied type was generated by this database.
     * 
     * @param type
     * 
     * @return true iff the type refers to a type, that is known and created by
     *         this object
     */
    public boolean hasType(Type type) {
        if (type instanceof TypeApplication)
            return mapFrom.containsKey(((TypeApplication) type).getSort());
        return false;
    }

    /**
     * Creates sort, functions and rules for type and returns the ivil type.
     * 
     * @param type
     * @return the ivil type that can be used to represent the map
     * 
     * @throws TypeSystemException
     *             if type creation failed
     * 
     */
    private Type addMapType(BoogieMap type, ASTLocatedElement astLocatedElement) throws TypeSystemException {
        try {
            return type.flatten(env, null, this);
        } catch (EnvironmentException e) {
            e.printStackTrace();
            throw new TypeSystemException("type flattening failed", e);
        } catch (TermException e) {
            e.printStackTrace();
            throw new TypeSystemException("type flattening failed", e);
        }
    }

    @Override
    public String toString() {
        StringBuffer b = new StringBuffer();
        for (Sort s : mapFrom.keySet()) {
            b.append(s);
            b.append(" ==> ");
            b.append(mapFrom.get(s));
            b.append('\n');
        }
        return b.toString();
    }

}
