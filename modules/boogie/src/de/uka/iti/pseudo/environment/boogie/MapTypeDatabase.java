package de.uka.iti.pseudo.environment.boogie;

import java.util.Arrays;
import java.util.HashMap;
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

            // TODO polymorphic equality

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
                
                // TODO update variable mapping
                
                boolean result = map.range.accept(this, p.range);

                for (int i = 0; i < map.domain.size() && result; i++)
                    result = map.domain.get(i).accept(this, p.domain.get(i));

                return result;
            }

            @Override
            public Boolean visit(TypeVariable var, Type parameter) throws TermException {
                if (!(parameter instanceof TypeVariable))
                    return false;

                TypeVariable p = (TypeVariable) parameter;

                // TODO

                return var.equals(p);
            }

            @Override
            public Boolean visit(SchemaType schemaType, Type parameter) throws TermException {
                // can this even happen?
                return schemaType.equals(parameter);
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

        // look for the map in the table
        if (mapTo.containsKey(entry))
            return mapTo.get(entry);

        // add a new map to the table
        Type t = addMapType(entry, node);
        mapTo.put(entry, t);
        mapFrom.put(((TypeApplication) t).getSort(), entry);
        return t;
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
     */
    private Type addMapType(BoogieMap type, ASTLocatedElement astLocatedElement) throws TypeSystemException {
        try {
            return type.flatten(env, null);
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
            b.append("(#").append(s.getArity()).append(")");
            b.append(" ==> ");
            b.append(mapFrom.get(s));
            b.append('\n');
        }
        return b.toString();
    }

}
