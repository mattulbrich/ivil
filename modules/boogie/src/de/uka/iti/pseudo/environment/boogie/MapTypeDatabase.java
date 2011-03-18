package de.uka.iti.pseudo.environment.boogie;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.environment.Sort;
import de.uka.iti.pseudo.parser.boogie.ASTElement;
import de.uka.iti.pseudo.parser.boogie.ast.MapType;
import de.uka.iti.pseudo.parser.file.MatchingLocation;
import de.uka.iti.pseudo.rule.GoalAction;
import de.uka.iti.pseudo.rule.LocatedTerm;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.rule.WhereClause;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.SchemaType;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVisitor;
import de.uka.iti.pseudo.term.creation.TypingContext;

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
    private static class UnfoldedMap extends Type {
        public final Type[] domain;
        public final Type range;

        public UnfoldedMap(Type[] domain, Type range) {
            this.domain = domain;
            this.range = range;
        }

        @Override
        public <R, A> R accept(TypeVisitor<R, A> visitor, A parameter) throws TermException {
            assert false : "can not be visited";
            return null;
        }

        @Override
        public boolean equals(Object object) {
            if (!(object instanceof UnfoldedMap))
                return false;

            UnfoldedMap m = (UnfoldedMap) object;

            // TODO parameter

            if (domain.equals(m.domain))
                return false;

            return range.equals(m.range);
        }

        @Override
        public String toString() {
            StringBuilder b = new StringBuilder();
            b.append('[');
            for (int i = 0; i < domain.length; i++) {
                if (i != 0)
                    b.append(',');
                b.append(domain[i]);
            }
            b.append(']');
            b.append(range);

            return b.toString();
        }

    }

    /**
     * this direction is needed to add new map types
     */
    private final Map<UnfoldedMap, Type> mapTo = new HashMap<UnfoldedMap, Type>();
    private final Map<Type, UnfoldedMap> mapFrom = new HashMap<Type, UnfoldedMap>();

    private final Environment env;

    public MapTypeDatabase(Environment env) {
        this.env = env;
    }

    public Type getType(MapType node, EnvironmentCreationState state) {

        Type[] domain = new Type[node.getDomain().size()];
        for (int i = 0; i < domain.length; i++)
            domain[i] = state.typeMap.get(node.getDomain().get(i));

        // create an unfolded map
        UnfoldedMap entry = new UnfoldedMap(domain, state.typeMap.get(node.getRange()));

        // look for the map in the table
        if (mapTo.containsKey(entry))
            return mapTo.get(entry);

        // add a new map to the table
        Type t = addMapType(entry, node);
        mapTo.put(entry, t);
        mapFrom.put(t, entry);
        return t;
    }

    public Type getRange(Type definition) {
        return mapFrom.get(definition).range;
    }

    /**
     * Returns a new signature generated using context
     */
    public Type[] getSignature(TypingContext context, Type definition) {
        UnfoldedMap map = mapFrom.get(definition);

        return context.makeNewSignature(map.range, map.domain);
    }

    /**
     * Creates sort, functions and rules for type and returns the ivil type.
     * 
     * @param type
     * @return the ivil type that can be used to represent the map
     */
    private Type addMapType(UnfoldedMap type, ASTElement node) {
        final Type[] domain = type.domain;

        try {
            // create name ...
            final String name = "map" + (1 + mapTo.size());

            // ... sort ...
            env.addSort(new Sort(name, 0, node));

            // ... types ...

            Type map_t = env.mkType(name);
            Type domainRange[] = new Type[domain.length + 1];
            Type mapDomainRange[] = new Type[domain.length + 2];
            Type mapDomain[] = new Type[domain.length + 1];
            Type curryMap[] = new Type[] { null }; // needed for lambda

            for (int i = domain.length - 1; i >= 0; i--) {
                if (null == curryMap[0])
                    curryMap[0] = env.mkType("map", new Type[] { domain[i], domain[domain.length - 1] });
                else
                    curryMap[0] = env.mkType("map", new Type[] { domain[i], curryMap[0] });
            }

            mapDomainRange[0] = mapDomain[0] = map_t;

            for (int i = 0; i < domain.length; i++)
                domainRange[i] = mapDomain[i + 1] = mapDomainRange[i + 1] = domain[i];

            domainRange[domainRange.length - 1] = mapDomainRange[mapDomainRange.length - 1] = type.range;

            // ... functions ...
            env.addFunction(new Function(name + "_store", map_t, mapDomainRange, false, false, node));

            env.addFunction(new Function(name + "_load", domainRange[domainRange.length - 1], mapDomain, false, false,
                    node));

            // used to uncurry lambda expressions; lambda expressions always
            // create maps with a domain larger then 0
            if (domain.length > 0)
                env.addFunction(new Function(name + "_curry", mapDomain[0], curryMap, false, false, node));


            // create some commonly used schema variables
            SchemaType vt = new SchemaType("v");
            SchemaVariable v = new SchemaVariable("%v", vt);

            Term store_arg[] = new Term[domain.length + 2];
            Term load_arg[] = new Term[domain.length + 1];

            Type map_drt[] = new Type[domain.length + 1];

            for (int i = 0; i < domain.length; i++) {
                map_drt[i] = new SchemaType("D" + i);
                store_arg[i + 1] = new SchemaVariable("%d1_" + i, new SchemaType("d1_" + i));
                load_arg[i + 1] = new SchemaVariable("%d2_" + i, new SchemaType("d2_" + i));
            }

            map_drt[domain.length] = new SchemaType("R");

            SchemaVariable m = new SchemaVariable("%m", map_t);

            store_arg[0] = m;
            store_arg[store_arg.length - 1] = v;

            load_arg[0] = new Application(env.getFunction(name + "_store"), map_t, store_arg);
            Term default_find = new Application(env.getFunction(name + "_load"), new SchemaType("rt"), load_arg);

            try { // /////////////// LOAD STORE SAME

                String rule = name + "_load_store_same";

                Term args1[] = new Term[domain.length + 2];
                Term args2[] = new Term[domain.length + 1];

                Type drt[] = new Type[domain.length + 1];

                for (int i = 0; i < domain.length; i++) {
                    drt[i] = new SchemaType("d" + i);
                    args1[i + 1] = args2[i + 1] = new SchemaVariable("%d" + i, drt[i]);
                }

                drt[domain.length] = vt;

                args1[0] = m;
                args1[args1.length - 1] = v;

                args2[0] = new Application(env.getFunction(name + "_store"), map_t, args1);
                Term find = new Application(env.getFunction(name + "_load"), vt, args2);

                List<GoalAction> actions = new LinkedList<GoalAction>();

                actions.add(new GoalAction("samegoal", null, false, v, new LinkedList<Term>(), new LinkedList<Term>()));

                Map<String, String> tags = new HashMap<String, String>();

                tags.put("rewrite", "concrete");

                env.addRule(new Rule(rule, new LinkedList<LocatedTerm>(), new LocatedTerm(find, MatchingLocation.BOTH),
                        new LinkedList<WhereClause>(), actions, tags, node));

            } catch (RuleException e) {
                e.printStackTrace();
                throw new EnvironmentException(e);
            }

            // TODO other rules

            return map_t;

        } catch (EnvironmentException e) {
            e.printStackTrace();
            assert false : "internal error";

        } catch (TermException e) {
            e.printStackTrace();
            assert false : "internal error";
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuffer b = new StringBuffer();
        for (Type t : mapTo.keySet()) {
            b.append(t);
            b.append(" ==> ");
            b.append(mapTo.get(t));
            b.append('\n');
        }
        return b.toString();
    }
}
