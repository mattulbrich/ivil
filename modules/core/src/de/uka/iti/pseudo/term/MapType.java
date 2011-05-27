/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.term;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.environment.Sort;
import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.parser.file.MatchingLocation;
import de.uka.iti.pseudo.rule.GoalAction;
import de.uka.iti.pseudo.rule.LocatedTerm;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.rule.WhereClause;
import de.uka.iti.pseudo.term.creation.TypingContext;

/**
 * This class encapsulates a map type.
 * 
 * <p>
 * Map types are printed as {boundVars}[domain]range
 */
public class MapType extends Type {

    private final List<TypeVariable> boundVars;
    private final List<Type> domain;
    private final Type range;

    /**
     * Create a new map type.
     */
    public MapType(@NonNull List<TypeVariable> boundVars, @NonNull List<Type> domain, @NonNull Type range) {
        this.boundVars = boundVars;
        this.domain = domain;
        this.range = range;
    }

    /**
     * Transformes the map into a human readable string that can be parsed to
     * the same map type.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (boundVars.size() > 0) {
            sb.append("{");
            sb.append(boundVars.get(0));
            for (int i = 1; i < boundVars.size(); i++) {
                sb.append(", ");
                sb.append(boundVars.get(i));
            }
            sb.append("}");
        }
        sb.append("[");
        for (int i = 0; i < domain.size(); i++) {
            if (i > 0)
                sb.append(", ");
            sb.append(domain.get(i));
        }
        sb.append("]");
        sb.append(range);
        return sb.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * de.uka.iti.pseudo.term.Type#visit(de.uka.iti.pseudo.term.TypeVisitor)
     */
    @Override
    public <R, A> R accept(TypeVisitor<R, A> visitor, A parameter) throws TermException {
        assert false : "this type can not be visited";
        return null;
    }

    /**
     * Two type variables are equal iff their names are equal.
     */
    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof MapType) {
            MapType m = (MapType) obj;

            // short cut introduced, because calculation of map equality is
            // expensive
            if (boundVars.size() != m.boundVars.size() || domain.size() != m.domain.size())
                return false;

            // FIXME implement equality!!!
            return true;
        }
        return false;
    }

    /**
     * the hashcode of a type variable is the hash code of its name.S
     */
    @Override
    public int hashCode() {
        // TODO find something more useful here
        return 0;
    }

    public List<TypeVariable> getBoundVars() {
        return boundVars;
    }

    public List<Type> getDomain() {
        return domain;
    }

    /**
     * Creates a flat type, that does not contain any map types, as well as
     * registering rules and function symbols for the created type in the
     * environment.
     * 
     * @param env
     *            the target environment
     * 
     * @param name
     *            the desired name of the type; if null, a generic name will be
     *            used
     * 
     * @return a type that behaves as if it were a map type
     * 
     * @throws EnvironmentException
     * @throws TermException
     * 
     *             TODO move rule creation and the whole schema type related
     *             things into another function
     */
    public Type flatten(Environment env, String name) throws EnvironmentException, TermException {
        // get a location
        final ASTLocatedElement declaringLocation = null;
        
        if(null!=name && null!=env.getSort(name))
            throw new EnvironmentException("the sort " + name + " exists already");
        

        //create flat sub types
        Type flat_dom[] = new Type[domain.size()], flat_r;
        
        for (int i = 0; i < flat_dom.length; i++) {
            flat_dom[i] = domain.get(i);
            if (flat_dom[i] instanceof MapType)
                flat_dom[i] = ((MapType) flat_dom[i]).flatten(env, null);
        }
        flat_r = range instanceof MapType ? ((MapType) range).flatten(env, null) : range;


        // the list of type variables, that do occur unbound in this type
        Set<Type> freeVars = new HashSet<Type>();
        for (int i = 0; i < flat_dom.length; i++)
            collectFreeVars(flat_dom[i], freeVars);
        collectFreeVars(flat_r, freeVars);

        for (Type t : boundVars)
            freeVars.remove(t);
        
        // create map sort and type
        if (null == name)
            name = env.createNewSortName("map");
        env.addSort(new Sort(name, freeVars.size(), declaringLocation));

        // create function symbols
        Function $load, $store;
        {
            Type map_t = env.mkType(name, freeVars.toArray(new Type[freeVars.size()]));
            Type[] mapDomainRange = new Type[flat_dom.length + 2];
            Type[] mapDomain = new Type[flat_dom.length + 1];
            
            mapDomainRange[0] = mapDomain[0] = map_t;
            for (int i = 0; i < flat_dom.length; i++)
                mapDomainRange[i+1] = mapDomain[i+1] = flat_dom[i];
            
            mapDomainRange[flat_dom.length + 1] = flat_r;
            
            env.addFunction($load = new Function("$load_" + name, flat_r, mapDomain, false, false,
                    declaringLocation));

            env.addFunction($store = new Function("$store_" + name, map_t, mapDomainRange, false, false,
                    declaringLocation));
        }

        // create schema signatures for load and store
        Term[] load_sig, store_sig;
        {
            TypingContext t = new TypingContext();
            Type[] load_t = t.makeNewSignature($load.getResultType(), $load.getArgumentTypes());
            load_sig = new Term[load_t.length];
            for(int i = 0; i < load_sig.length; i++)
                if (load_t[i] instanceof SchemaType)
                    load_sig[i] = SchemaVariable.getInst("%" + ((SchemaType) load_t[i]).getVariableName(), load_t[i]);
                else
                    load_sig[i] = SchemaVariable.getInst("%" + load_t[i].toString(), load_t[i]);
            
            Type[] store_t = t.makeNewSignature($store.getResultType(), $store.getArgumentTypes());
            store_sig = new Term[store_t.length];
            for (int i = 0; i < store_sig.length; i++)
                if (store_t[i] instanceof SchemaType)
                    store_sig[i] = SchemaVariable
                            .getInst("%" + ((SchemaType) store_t[i]).getVariableName(), store_t[i]);
                else
                    store_sig[i] = SchemaVariable.getInst("%" + store_t[i].toString(), store_t[i]);
        }
        Type map_st = load_sig[1].getType();

        // create rules

        try { // /////////////// LOAD STORE COND, aka McCarthy axiom
            String rule = name + "_load_store_cond";

            Map<String, String> tags = new HashMap<String, String>();

            tags.put("rewrite", "split");

            List<Term> none = new LinkedList<Term>();

            List<GoalAction> actions = new LinkedList<GoalAction>();

            Term[] load_arg = new Term[flat_dom.length + 1];
            Term[] store_arg = new Term[flat_dom.length + 2];
            SchemaVariable v = SchemaVariable.getInst("%v", load_sig[0].getType());

            // TODO does this even work for deeper nested maps? we might want to
            // create schema variants of the mapDomainRange/mapDomain types
            store_arg[0] = SchemaVariable.getInst("%m", map_st);
            store_arg[store_arg.length - 1] = v;

            for (int i = 1; i <= flat_dom.length; i++) {
                load_arg[i] = load_sig[i + 1];
                store_arg[i] = null;
            }

            // needed to be correct in case of maps, that do not have a
            // domain
            Term condition = Environment.getTrue();

            for (int i = 0; i < flat_dom.length; i++)
                condition = Application.getInst(env.getFunction("$and"), BOOL_T, new Term[] {
                        Application.getInst(env.getFunction("$eq"), BOOL_T, new Term[] { store_arg[i + 1],
                                load_arg[i + 1] }), condition });

            load_arg[0] = Application.getInst($store, map_st, store_arg);

            Term load = Application.getInst($load, v.getType(), load_arg);

            Term replacement = Application.getInst(env.getFunction("cond"), v.getType(), new Term[] { condition, v,
                    load });

            Term find = Application.getInst($load, v.getType(), load_arg);

            actions.add(new GoalAction("samegoal", null, false, replacement, none, none));

            env.addRule(new Rule(rule, new LinkedList<LocatedTerm>(), new LocatedTerm(find, MatchingLocation.BOTH),
                    new LinkedList<WhereClause>(), actions, tags, declaringLocation));

        } catch (RuleException e) {
            e.printStackTrace();
            throw new EnvironmentException(e);
        }
        return env.mkType(name, freeVars.toArray(new Type[freeVars.size()]));
    }

    private final static Type BOOL_T = Environment.getBoolType();

    /**
     * collects the free type variables in a MapType free type
     */
    private static final void collectFreeVars(Type type, Set<Type> freeVars) {
        if(type instanceof TypeVariable)
            freeVars.add(type);
        else if(type instanceof TypeApplication)
            for (Type t : ((TypeApplication) type).getArguments())
                collectFreeVars(t, freeVars);
    }

}
