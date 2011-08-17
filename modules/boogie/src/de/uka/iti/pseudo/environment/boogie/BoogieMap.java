package de.uka.iti.pseudo.environment.boogie;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.environment.Sort;
import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ParseException;
import de.uka.iti.pseudo.parser.file.MatchingLocation;
import de.uka.iti.pseudo.rule.GoalAction;
import de.uka.iti.pseudo.rule.LocatedTerm;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.rule.WhereClause;
import de.uka.iti.pseudo.rule.where.DifferentGroundTypes;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.SchemaType;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeApplication;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.TypeVisitor;
import de.uka.iti.pseudo.term.creation.TermMaker;
import de.uka.iti.pseudo.term.creation.TypingContext;
import de.uka.iti.pseudo.util.Log;

/**
 * This class is the unfolded representation of a map. It is used to compare map
 * types and to create representatives, which behave similar to maps created in
 * ivil p code.
 * 
 * TODO code cleanup
 * 
 * @author timm.felden@felden.com
 */
class BoogieMap extends Type {
    
    protected final List<TypeVariable> boundVars;
    protected final List<Type> domain;
    protected final Type range;
    protected final ASTLocatedElement declaringLocation;

    public BoogieMap(List<TypeVariable> boundVars, List<Type> domain, Type range,
            ASTLocatedElement declaringLocation) {
        this.boundVars = boundVars;
        this.domain = domain;
        this.range = range;
        this.declaringLocation = declaringLocation;
    }

    public Boolean accept(BoogieMap.BoogieMapEqualityVisitor visitor, Type parameter) throws TermException {
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
     * @note the hash code is very bad for boogie maps, as it is hard to
     *       guarantee that two equal maps have the same hash code for useful
     *       hash codes.
     */
    @Override
    public int hashCode() {
        return 100 * boundVars.size() + domain.size();
    }

    public List<TypeVariable> getBoundVars() {
        return boundVars;
    }

    public List<Type> getDomain() {
        return domain;
    }

    public Type getRange() {
        return range;
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
        sb.append(getRange());
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
    Type flatten(final Environment env, final String desiredName, final MapTypeDatabase mapDB)
            throws EnvironmentException, TermException {
        
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
            flatRepresentation = (TypeApplication) new BoogieMap(representation.getBoundVars(),
                    representation.getDomain(), representation.getRange(), declaringLocation).flatten(env, null);
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
     * Creates a flat type, that does not contain any map types, as well as
     * registering rules and function symbols for the created type in the
     * environment.
     * 
     * @param env
     *            the target environment
     * 
     * @param name
     *            the desired name of the type; if null, a generic name will
     *            be used
     * 
     * @return a type that behaves as if it were a map type
     * 
     * @throws EnvironmentException
     * @throws TermException
     * 
     *             TODO move rule creation and the whole schema type related
     *             things into another function
     */
    private Type flatten(Environment env, String name) throws EnvironmentException, TermException {

        if (null != name && null != env.getSort(name))
            throw new EnvironmentException("the sort " + name + " exists already");

        // create flat sub types
        Type flat_dom[] = new Type[domain.size()], flat_r;

        for (int i = 0; i < flat_dom.length; i++) {
            flat_dom[i] = domain.get(i);
            if (flat_dom[i] instanceof BoogieMap)
                flat_dom[i] = ((BoogieMap) flat_dom[i]).flatten(env, null);
        }
        flat_r = getRange() instanceof BoogieMap ? ((BoogieMap) getRange()).flatten(env, null) : getRange();

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
                mapDomainRange[i + 1] = mapDomain[i + 1] = flat_dom[i];

            mapDomainRange[flat_dom.length + 1] = flat_r;

            env.addFunction($load = new Function("$load_" + name, flat_r, mapDomain, false, false,
                    declaringLocation));

            env.addFunction($store = new Function("$store_" + name, map_t, mapDomainRange, false, false,
                    declaringLocation));
        }

        createRules(name, $load, $store, env);

        return env.mkType(name, freeVars.toArray(new Type[freeVars.size()]));
    }

    /**
     * collects the free type variables in a MapType free type
     */
    private static final void collectFreeVars(Type type, Set<Type> freeVars) {
        if (type instanceof TypeVariable)
            freeVars.add(type);
        else if (type instanceof TypeApplication)
            for (Type t : ((TypeApplication) type).getArguments())
                collectFreeVars(t, freeVars);
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
     * create rules needed in order to handle objects of the created map type
     * efficiently
     */
    private void createRules(String name, Function $load, Function $store, Environment env)
 throws EnvironmentException {

        assert $load != null && $store != null : "Functions must already have been added";

        try {
            addLoadStoreSameRule(name, $load, $store, env);
            addLoadStoreAssumeRule(name, $load, $store, env);
            addLoadStoreOtherAssumeRule(name, $load, $store, env);
            addLoadStoreOtherTypeRule(name, $load, $store, env);
            addLoadStoreCondRule(name, $load, $store, env);
        } catch (ParseException e) {
            throw new EnvironmentException("Error while creating rules for map type " + name + "@" + declaringLocation,
                    e);
        } catch (RuleException e) {
            throw new EnvironmentException("Error while creating rules for map type " + name + "@" + declaringLocation,
                    e);
        } catch (TermException e) {
            throw new EnvironmentException("Error while creating rules for map type " + name + "@" + declaringLocation,
                    e);
        } catch (ASTVisitException e) {
            throw new EnvironmentException("Error while creating rules for map type " + name + "@" + declaringLocation,
                    e);
        }
    }

    private void addLoadStoreSameRule(String name, Function $load, Function $store, Environment env)
            throws ASTVisitException, ParseException, RuleException, EnvironmentException {
        // /////////////// LOAD STORE SAME
        String ruleName = name + "_load_store_same";
        // find: $load($store(%m, %D, %v), %D)
        // replace: %v

        // $load($store(%m, %D, %v), %D)
        StringBuilder sbFind = new StringBuilder();
        sbFind.append($load.getName()).append("(");
        sbFind.append($store.getName()).append("(%m, ");
        for (int i = 0; i < domain.size(); i++)
            sbFind.append("%d").append(i).append(", ");
        sbFind.append("%v)");
        for (int i = 0; i < domain.size(); i++)
            sbFind.append(", ").append("%d").append(i);
        sbFind.append(")");

        // %v
        StringBuilder sbReplace = new StringBuilder("%v");

        Term factory;
        factory = TermMaker.makeAndTypeTerm("cond(true," + sbFind + "," + sbReplace + ")", env);
        Term find, replace;

        find = factory.getSubterm(1);
        replace = factory.getSubterm(2);

        List<GoalAction> actions = new LinkedList<GoalAction>();

        actions.add(new GoalAction("samegoal", null, false, replace, Collections.<Term> emptyList(), Collections
                .<Term> emptyList()));

        Map<String, String> tags = new HashMap<String, String>();

        tags.put("rewrite", "concrete");

        Rule rule = new Rule(ruleName, Collections.<LocatedTerm> emptyList(), new LocatedTerm(find,
                MatchingLocation.BOTH), Collections.<WhereClause> emptyList(), actions, tags, declaringLocation);

        Log.log(Log.DEBUG, "Rule " + rule + " created");

        env.addRule(rule);
    }

    private void addLoadStoreAssumeRule(String name, Function $load, Function $store, Environment env)
            throws EnvironmentException, ParseException, ASTVisitException, RuleException, TermException {

        // /////////////// LOAD STORE SAME ASSUME
        String ruleName = name + "_load_store_same_assume";
        // find: $load($store(%m, %D, %v), %T)
        // ∀i. assume: %di = %ti |-
        // replace: %v

        // $load($store(%m, %D, %v), %D)
        Map<String, String> tags = new HashMap<String, String>();

        tags.put("rewrite", "concrete");
        if (domain.size() == 1)
            tags.put("dragdrop", "8");

        // $load($store(%m, %D, %v), %T)
        StringBuilder sbFind = new StringBuilder();
        sbFind.append($load.getName()).append("(");
        sbFind.append($store.getName()).append("(%m, ");
        for (int i = 0; i < domain.size(); i++)
            sbFind.append("%d").append(i).append(", ");
        sbFind.append("%v)");
        for (int i = 0; i < domain.size(); i++)
            sbFind.append(", ").append("%t").append(i);
        sbFind.append(")");

        // add equality to the condition, to ensure, that %D and %T have the
        // same types.
        // AND(%di = %ti)
        StringBuilder sbCond = new StringBuilder("true");
        for (int i = 0; i < domain.size(); i++)
            sbCond.append(" & ").append("%d").append(i).append(" = ").append("%t").append(i);

        Term factory;
        factory = TermMaker.makeAndTypeTerm("cond(" + sbCond + "," + sbFind + ", %v )", env);
        Term find, replace;

        find = factory.getSubterm(1);
        replace = factory.getSubterm(2);
        LinkedList<WhereClause> where = new LinkedList<WhereClause>();

        List<GoalAction> actions = new LinkedList<GoalAction>();

        actions.add(new GoalAction("samegoal", null, false, replace, new LinkedList<Term>(), new LinkedList<Term>()));

        List<LocatedTerm> assumes = new LinkedList<LocatedTerm>();

        for (int i = 0; i < domain.size(); i++)
            assumes.add(new LocatedTerm(Application.getInst(env.getFunction("$eq"), Environment.getBoolType(),
                    new Term[] { find.getSubterm(0).getSubterm(i + 1), find.getSubterm(i + 1) }),
                    MatchingLocation.ANTECEDENT));

        Rule rule = new Rule(ruleName, assumes, new LocatedTerm(find, MatchingLocation.BOTH), where, actions, tags,
                declaringLocation);
        Log.log(Log.DEBUG, "Rule " + rule + " created");
        env.addRule(rule);
    }

    private void addLoadStoreOtherAssumeRule(String name, Function $load, Function $store, Environment env)
            throws EnvironmentException, RuleException, ParseException, ASTVisitException, TermException {
        // /////////////// LOAD STORE OTHER ASSUME
        // creates #domain rules of the form:

        // find: $load($store(%m, %D, %v), %T)
        // ∃i. assume |- %di = %ti
        // replace: $load(%m, %T)

        LinkedList<WhereClause> where = new LinkedList<WhereClause>();

        Map<String, String> tags = new HashMap<String, String>();

        tags.put("rewrite", "concrete");

        // any other rules has only one assumption and can therefore be a
        // drag & drop rule
        tags.put("dragdrop", "8");

        // $load($store(%m, %D, %v), %T)
        StringBuilder sbFind = new StringBuilder();
        sbFind.append($load.getName()).append("(");
        sbFind.append($store.getName()).append("(%m, ");
        for (int i = 0; i < domain.size(); i++)
            sbFind.append("%d").append(i).append(", ");
        sbFind.append("%v)");
        for (int i = 0; i < domain.size(); i++)
            sbFind.append(", ").append("%t").append(i);
        sbFind.append(")");

        // $load(%m, %T)
        StringBuilder sbReplace = new StringBuilder($load.getName());
        sbReplace.append("(%m");
        for (int i = 0; i < domain.size(); i++)
            sbReplace.append(", ").append("%t").append(i);
        sbReplace.append(")");

        // add equality to the condition, to ensure, that %D and %T have the
        // same types, the actual terms are not relevant
        // AND(%di = %ti)
        StringBuilder sbCond = new StringBuilder("true");
        for (int i = 0; i < domain.size(); i++)
            sbCond.append(" & ").append("%d").append(i).append(" = ").append("%t").append(i);

        Term factory;
        factory = TermMaker.makeAndTypeTerm("cond(" + sbCond + "," + sbFind + "," + sbReplace + ")", env);
        Term find, replace;

        find = factory.getSubterm(1);
        replace = factory.getSubterm(2);

        List<GoalAction> actions = new LinkedList<GoalAction>();

        actions.add(new GoalAction("samegoal", null, false, replace, new LinkedList<Term>(), new LinkedList<Term>()));

        for (int i = 0; i < domain.size(); i++) {
            String rule = name + "_load_store_other_assume_l" + i;

            List<LocatedTerm> assumes = new LinkedList<LocatedTerm>();

            assumes.add(new LocatedTerm(Application.getInst(env.getFunction("$eq"), Environment.getBoolType(),
                    new Term[] { find.getSubterm(0).getSubterm(i + 1), find.getSubterm(i + 1) }),
                    MatchingLocation.SUCCEDENT));

            env.addRule(new Rule(rule, assumes, new LocatedTerm(find, MatchingLocation.BOTH), where, actions, tags,
                    declaringLocation));
        }

        // %ti and %di might have reverse order; if we want to match, we
        // have to look at that, too.
        for (int i = 0; i < domain.size(); i++) {
            String ruleName = name + "_load_store_other_assume_r" + i;

            List<LocatedTerm> assumes = new LinkedList<LocatedTerm>();

            assumes.add(new LocatedTerm(Application.getInst(env.getFunction("$eq"), Environment.getBoolType(),
                    new Term[] { find.getSubterm(i + 1), find.getSubterm(0).getSubterm(i + 1) }),
                    MatchingLocation.SUCCEDENT));

            Rule rule = new Rule(ruleName, assumes, new LocatedTerm(find, MatchingLocation.BOTH), where, actions, tags,
                    declaringLocation);
            Log.log(Log.DEBUG, "Rule " + rule + " created");
            env.addRule(rule);
        }

    }

    private void addLoadStoreOtherTypeRule(String name, Function $load, Function $store, Environment env)
            throws EnvironmentException, RuleException, ParseException, ASTVisitException {
        // /////////////// LOAD STORE OTHER TYPE

        // creates #domain rules of the form:

        // find: $load($store(%m, %D, %v), %T)
        // ∃i. where: differentGroundTypes %di %ti
        // replace: $load(%m, %T)

        // and an additional rule for different types in range with the
        // where condition:

        // where: differentInEq %v $load($store(%m, %D, %v), %T)

        List<LocatedTerm> assumes = new LinkedList<LocatedTerm>();

        Map<String, String> tags = new HashMap<String, String>();

        tags.put("rewrite", "concrete");

        // $load($store(%m, %D, %v), %T)
        StringBuilder sbFind = new StringBuilder();
        sbFind.append($load.getName()).append("(");
        sbFind.append($store.getName()).append("(%m, ");
        for (int i = 0; i < domain.size(); i++)
            sbFind.append("%d").append(i).append(", ");
        sbFind.append("%v)");
        for (int i = 0; i < domain.size(); i++)
            sbFind.append(", ").append("%t").append(i);
        sbFind.append(")");

        // $load(%m, %T)
        StringBuilder sbReplace = new StringBuilder($load.getName());
        sbReplace.append("(%m");
        for (int i = 0; i < domain.size(); i++)
            sbReplace.append(", ").append("%t").append(i);
        sbReplace.append(")");

        Term factory;
        factory = TermMaker.makeAndTypeTerm("cond(true," + sbFind + "," + sbReplace + ")", env);

        Term find, replace;

        find = factory.getSubterm(1);
        replace = factory.getSubterm(2);

        List<GoalAction> actions = new LinkedList<GoalAction>();

        actions.add(new GoalAction("samegoal", null, false, replace, new LinkedList<Term>(), new LinkedList<Term>()));

        for (int i = 0; i < domain.size(); i++) {
            String rule = name + "_load_store_other_type_domain_" + i;

            LinkedList<WhereClause> where = new LinkedList<WhereClause>();

            // ensure %di and %ti have different types
            where.add(new WhereClause(DifferentGroundTypes.getWhereCondition(env, "differentGroundTypes"), false,
                    new Term[] { find.getSubterm(0).getSubterm(i + 1), find.getSubterm(i + 1) }));

            env.addRule(new Rule(rule, assumes, new LocatedTerm(find, MatchingLocation.BOTH), where, actions, tags,
                    declaringLocation));
        }

        // add rule for range type miss match
        String ruleName = name + "_load_store_other_type_range";

        LinkedList<WhereClause> where = new LinkedList<WhereClause>();

        where.add(new WhereClause(DifferentGroundTypes.getWhereCondition(env, "differentGroundTypes"), false,
                new Term[] { find.getSubterm(0).getSubterm(domain.size() + 1), find }));

        Rule rule = new Rule(ruleName, assumes, new LocatedTerm(find, MatchingLocation.BOTH), where, actions, tags,
                declaringLocation);
        Log.log(Log.DEBUG, "Rule " + rule + " created");
        env.addRule(rule);
    }

    private void addLoadStoreCondRule(String name, Function $load, Function $store, Environment env)
            throws EnvironmentException, RuleException, ParseException, ASTVisitException {
        // /////////////// LOAD STORE COND, aka McCarthy axiom
        String ruleName = name + "_load_store_cond";
        // find: $load($store(%m, %D, %v), %T)
        // replace: cond($weq(%D, %T), %v, $load(%m, %T))

        Map<String, String> tags = new HashMap<String, String>();

        tags.put("rewrite", "split");

        List<Term> none = new LinkedList<Term>();

        List<GoalAction> actions = new LinkedList<GoalAction>();

        // $load($store(%m, %D, %v), %T)
        StringBuilder sbFind = new StringBuilder();
        sbFind.append($load.getName()).append("(");
        sbFind.append($store.getName()).append("(%m, ");
        for (int i = 0; i < domain.size(); i++)
            sbFind.append("%d").append(i).append(", ");
        sbFind.append("%v)");
        for (int i = 0; i < domain.size(); i++)
            sbFind.append(", ").append("%t").append(i);
        sbFind.append(")");

        // cond($weq(%D, %T), %v, $load(%m, %T))
        StringBuilder sbReplace = new StringBuilder("cond(");
        if (0 == domain.size())
            sbReplace.append("true");
        for (int i = 0; i < domain.size(); i++) {
            if (i > 0)
                sbReplace.append("&");
            sbReplace.append("$weq(%d").append(i).append(", ").append("%t").append(i).append(")");
        }
        sbReplace.append(", %v, ").append($load.getName()).append("(%m");
        for (int i = 0; i < domain.size(); i++)
            sbReplace.append(", ").append("%t").append(i);
        sbReplace.append("))");

        Term factory;
        factory = TermMaker.makeAndTypeTerm("cond(true," + sbFind + "," + sbReplace + ")", env);
        Term find, replace;

        find = factory.getSubterm(1);
        replace = factory.getSubterm(2);

        actions.add(new GoalAction("samegoal", null, false, replace, none, none));

        Rule rule = new Rule(ruleName, new LinkedList<LocatedTerm>(), new LocatedTerm(find, MatchingLocation.BOTH),
                new LinkedList<WhereClause>(), actions, tags, declaringLocation);
        Log.log(Log.DEBUG, "Rule " + rule + " created");
        env.addRule(rule);
    }

}