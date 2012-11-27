/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.environment.creation;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nonnull.DeepNonNull;
import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.environment.Sort;
import de.uka.iti.pseudo.environment.TypeVariableCollector;
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
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeApplication;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.creation.TermMaker;
import de.uka.iti.pseudo.util.Log;
import de.uka.iti.pseudo.util.Util;

/**
 * This class is used to actually create the function symbols and rules for map
 * types.
 *
 * <p>
 * Map types are printed as {boundVars}[domain]range
 *
 * @author Timm Felden, Mattias Ulbrich
 */
public class MapTypeRuleCreator {

    private List<TypeVariable> boundVars;
    private List<Type> domain;
    private Type range;
    private final ASTLocatedElement declaringLocation;
    private Sort sort;
    private List<TypeVariable> argumentTypes;

    public void setSort(@NonNull Sort sort, @NonNull List<TypeVariable> argumentTypes) {
        this.sort = sort;
        this.argumentTypes = argumentTypes;
    }

    public void setBoundVariables(@NonNull List<TypeVariable> boundVars) {
        this.boundVars = boundVars;
    }

    public void setDomain(@NonNull List<Type> domain) {
        this.domain = domain;
    }

    public void setRange(@NonNull Type range) {
        this.range = range;
    }

    /**
     * Create a new map type.
     *
     * @throws ASTVisitException
     *             if type variables appear in domain or range.
     */
    public MapTypeRuleCreator(@NonNull ASTLocatedElement declaringLocation) throws ASTVisitException {
        this.declaringLocation = declaringLocation;
    }

    /**
     * Ensure that all fields have been set and that the data is consistent:
     * <ul>
     * <li>The domain and range do not contain schema types
     * <li>The argument types of the sort contain all free variables of domain
     * and range
     * </ul>
     */
    public void check() throws ASTVisitException {

        if(!TypeVariableCollector.collectSchema(domain).isEmpty()) {
            throw new ASTVisitException(
                    "Map type alias contains schema type(s) in domain",
                    declaringLocation);
        }

        if(!TypeVariableCollector.collectSchema(range).isEmpty()) {
            throw new ASTVisitException(
                    "Map type alias contains schema type(s) in range",
                    declaringLocation);
        }

        Set<TypeVariable> set = TypeVariableCollector.collect(domain);
        set.removeAll(boundVars);
        for (TypeVariable tv : set) {
            if (!argumentTypes.contains(tv)) {
                throw new ASTVisitException(
                        "Type variable " + tv
                                + " is used free in domain, but not " +
                                "within the parameters of the sort.",
                        declaringLocation);
            }
        }

        set = TypeVariableCollector.collect(range);
        set.removeAll(boundVars);
        for (TypeVariable tv : set) {
            if (!argumentTypes.contains(tv)) {
                throw new ASTVisitException(
                        "Type variable " + tv
                                + " is used free in range, but not " +
                                "within the parameters of the sort.",
                        declaringLocation);
            }
        }
    }

    /**
     * Transforms the map into a human readable string that can be parsed to
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
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(domain.get(i));
        }
        sb.append("]");
        sb.append(range);
        return sb.toString();
    }

    /**
     * two maps are equal, if they bind the same variables and have equal domain
     * and range. this is however not relevant after map flattening, because
     * maps, that have been flattened to different sorts will always behave as
     * if they were unequal.
     */
    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof MapTypeRuleCreator) {
            MapTypeRuleCreator m = (MapTypeRuleCreator) obj;

            // short cut introduced, because calculation of map equality is
            // expensive
            if (boundVars.size() != m.boundVars.size() || domain.size() != m.domain.size()) {
                return false;
            }

            if(!boundVars.equals(m.boundVars)) {
                return false;
            }

            if(!domain.equals(m.domain)) {
                return false;
            }

            return range.equals(m.range);
        }
        return false;
    }

    /**
     * very basic hash code
     */
    @Override
    public int hashCode() {
        return 100 * boundVars.size() + domain.size();
    }

    public List<TypeVariable> getBoundVars() {
        return Collections.unmodifiableList(boundVars);
    }

    public List<Type> getDomain() {
        return Collections.unmodifiableList(domain);
    }

    public Type getRange() {
        return range;
    }

    public void addFunctionSymbols(@NonNull Environment env) throws ASTVisitException {
        try {
            Type args[] = Util.listToArray(argumentTypes, Type.class);
            Type map_t = TypeApplication.getInst(sort, args);
            Type[] storeDomain = new Type[domain.size() + 2];
            Type[] loadDomain = new Type[domain.size() + 1];

            storeDomain[0] = map_t;
            loadDomain[0] = map_t;
            for (int i = 0; i < domain.size(); i++) {
                loadDomain[i + 1] = storeDomain[i + 1] = domain.get(i);
            }
            storeDomain[storeDomain.length - 1] = range;

            String loadName = "$load_" + sort.getName();
            env.addFunction(new Function(loadName, range, loadDomain, false, false, declaringLocation));

            String storeName = "$store_" + sort.getName();
            env.addFunction(new Function(storeName, map_t, storeDomain, false, false, declaringLocation));
        } catch (TermException e) {
            throw new ASTVisitException(
                    "Error while creating function symbols for map sort "
                            + sort, declaringLocation, e);
        } catch (EnvironmentException e) {
            throw new ASTVisitException(
                    "Error while creating function symbols for map sort "
                            + sort, declaringLocation, e);
        }
    }

    /**
     * create rules needed in order to handle objects of the created map type
     * efficiently
     */
    public void addRules(@NonNull Environment env) throws ASTVisitException {

        checkFunctionPresent(env, "cond", new Type[] { Environment.getBoolType(),
                TypeVariable.ALPHA, TypeVariable.ALPHA }, TypeVariable.ALPHA);

        String name = sort.getName();
        Function $load = env.getFunction("$load_" + name);
        Function $store = env.getFunction("$store_" + name);

        assert $load != null && $store != null : "Functions must already have been added";

        try {
            addLoadStoreSameRule(name, $load, $store, env);
            addLoadStoreAssumeRule(name, $load, $store, env);
            addLoadStoreOtherAssumeRule(name, $load, $store, env);
            addLoadStoreOtherTypeRule(name, $load, $store, env);
            addLoadStoreCondRule(name, $load, $store, env);
        } catch (ParseException e) {
            throw new ASTVisitException(
                    "Error while creating rules for map type " + name,
                    declaringLocation, e);
        } catch (RuleException e) {
            throw new ASTVisitException(
                    "Error while creating rules for map type " + name,
                    declaringLocation, e);
        } catch (EnvironmentException e) {
            throw new ASTVisitException(
                    "Error while creating rules for map type " + name,
                    declaringLocation, e);
        } catch (TermException e) {
            throw new ASTVisitException(
                    "Error while creating rules for map type " + name,
                    declaringLocation, e);
        }

    }

    private void checkFunctionPresent(@NonNull Environment env,
            @NonNull String name, @DeepNonNull Type[] argTypes,
            @NonNull TypeVariable resultType) throws ASTVisitException {

        Function f = env.getFunction(name);

        if (f == null) {
            throw new ASTVisitException("Function symbol '" + name
                    + "' not defined, but needed for map types.",
                    declaringLocation);
        }

        if(!Arrays.equals(f.getArgumentTypes(), argTypes) ||
                !resultType.equals(f.getResultType())) {
            throw new ASTVisitException("Function symbol '" + name
                    + "' has unexpected signature, but needed for map types.",
                    declaringLocation);
        }

    }



    private void addLoadStoreSameRule(String name, Function $load,
            Function $store, Environment env) throws ASTVisitException,
            ParseException, RuleException, EnvironmentException {
        // /////////////// LOAD STORE SAME
        String ruleName = name + "_load_store_same";
        // find: $load($store(%m, %D, %v), %D)
        // replace: %v

        // $load($store(%m, %D, %v), %D)
        StringBuilder sbFind = new StringBuilder();
        sbFind.append($load.getName()).append("(");
        sbFind.append($store.getName()).append("(%m, ");
        for (int i = 0; i < domain.size(); i++) {
            sbFind.append("%d").append(i).append(", ");
        }
        sbFind.append("%v)");
        for (int i = 0; i < domain.size(); i++) {
            sbFind.append(", ").append("%d").append(i);
        }
        sbFind.append(")");

        // %v
        StringBuilder sbReplace = new StringBuilder("%v");

        Term factory;
        factory = TermMaker.makeAndTypeTerm("cond(true," + sbFind + "," + sbReplace + ")", env);
        Term find, replace;

        find = factory.getSubterm(1);
        replace = factory.getSubterm(2);

        List<GoalAction> actions = new LinkedList<GoalAction>();

        actions.add(new GoalAction("samegoal", null, false, replace,
                Collections.<Term>emptyList(),
                Collections.<Term>emptyList()));

        Map<String, String> tags = new HashMap<String, String>();

        tags.put("rewrite", "concrete");

        Rule rule = new Rule(ruleName, Collections.<LocatedTerm>emptyList(),
                new LocatedTerm(find, MatchingLocation.BOTH),
                Collections.<WhereClause>emptyList(), actions, tags, declaringLocation);

        Log.log(Log.DEBUG, "Rule " + rule + " created");

        env.addRule(rule);
    }

    private void addLoadStoreAssumeRule(String name, Function $load,
            Function $store, Environment env) throws EnvironmentException,
            ParseException, ASTVisitException, RuleException, TermException {

        // /////////////// LOAD STORE SAME ASSUME
        String ruleName = name + "_load_store_same_assume";
        // find: $load($store(%m, %D, %v), %T)
        // ∀i. assume: %di = %ti |-
        // replace: %v

        // $load($store(%m, %D, %v), %D)
        Map<String, String> tags = new HashMap<String, String>();

        tags.put("rewrite", "concrete");
        if (domain.size() == 1) {
            tags.put("dragdrop", "8");
        }

        // $load($store(%m, %D, %v), %T)
        StringBuilder sbFind = new StringBuilder();
        sbFind.append($load.getName()).append("(");
        sbFind.append($store.getName()).append("(%m, ");
        for (int i = 0; i < domain.size(); i++) {
            sbFind.append("%d").append(i).append(", ");
        }
        sbFind.append("%v)");
        for (int i = 0; i < domain.size(); i++) {
            sbFind.append(", ").append("%t").append(i);
        }
        sbFind.append(")");

        // add equality to the condition, to ensure, that %D and %T have the
        // same types.
        // AND(%di = %ti)
        StringBuilder sbCond = new StringBuilder("true");
        for (int i = 0; i < domain.size(); i++) {
            sbCond.append(" & ").append("%d").append(i).append(" = ").append("%t").append(i);
        }

        Term factory;
        factory = TermMaker.makeAndTypeTerm("cond(" + sbCond + "," + sbFind + ", %v )", env);
        Term find, replace;

        find = factory.getSubterm(1);
        replace = factory.getSubterm(2);
        LinkedList<WhereClause> where = new LinkedList<WhereClause>();

        List<GoalAction> actions = new LinkedList<GoalAction>();

        actions
        .add(new GoalAction("samegoal", null, false, replace, new LinkedList<Term>(),
                new LinkedList<Term>()));

        List<LocatedTerm> assumes = new LinkedList<LocatedTerm>();

        for (int i = 0; i < domain.size(); i++) {
            assumes.add(new LocatedTerm(Application.getInst(env.getFunction("$eq"), Environment.getBoolType(),
                    new Term[] { find.getSubterm(0).getSubterm(i + 1), find.getSubterm(i + 1) }),
                    MatchingLocation.ANTECEDENT));
        }

        Rule rule = new Rule(ruleName, assumes, new LocatedTerm(find, MatchingLocation.BOTH), where, actions, tags,
                declaringLocation);
        Log.log(Log.DEBUG, "Rule " + rule + " created");
        env.addRule(rule);
    }

    private void addLoadStoreOtherAssumeRule(String name, Function $load,
            Function $store, Environment env) throws EnvironmentException,
            RuleException, ParseException, ASTVisitException, TermException {
        // /////////////// LOAD STORE OTHER ASSUME
        // creates #domain rules of the form:

        // find: $load($store(%m, %D, %v), %T)
        // ∃i. assume |- %di = %ti
        // replace: $load(%m, %T)

        LinkedList<WhereClause> where = new LinkedList<WhereClause>();

        Map<String, String> tags = new HashMap<String, String>();

        tags.put("rewrite", "fol simp");

        // any other rules has only one assumption and can therefore be a
        // drag & drop rule
        tags.put("dragdrop", "8");

        // $load($store(%m, %D, %v), %T)
        StringBuilder sbFind = new StringBuilder();
        sbFind.append($load.getName()).append("(");
        sbFind.append($store.getName()).append("(%m, ");
        for (int i = 0; i < domain.size(); i++) {
            sbFind.append("%d").append(i).append(", ");
        }
        sbFind.append("%v)");
        for (int i = 0; i < domain.size(); i++) {
            sbFind.append(", ").append("%t").append(i);
        }
        sbFind.append(")");

        // $load(%m, %T)
        StringBuilder sbReplace = new StringBuilder($load.getName());
        sbReplace.append("(%m");
        for (int i = 0; i < domain.size(); i++) {
            sbReplace.append(", ").append("%t").append(i);
        }
        sbReplace.append(")");

        // add equality to the condition, to ensure, that %D and %T have the
        // same types, the actual terms are not relevant
        // AND(%di = %ti)
        StringBuilder sbCond = new StringBuilder("true");
        for (int i = 0; i < domain.size(); i++) {
            sbCond.append(" & ").append("%d").append(i).append(" = ").append("%t").append(i);
        }

        Term factory;
        factory = TermMaker.makeAndTypeTerm("cond(" + sbCond + "," + sbFind + "," + sbReplace + ")", env);
        Term find, replace;

        find = factory.getSubterm(1);
        replace = factory.getSubterm(2);

        List<GoalAction> actions = new LinkedList<GoalAction>();

        actions
        .add(new GoalAction("samegoal", null, false, replace, new LinkedList<Term>(),
                new LinkedList<Term>()));

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

        tags.put("rewrite", "fol simp");

        // $load($store(%m, %D, %v), %T)
        StringBuilder sbFind = new StringBuilder();
        sbFind.append($load.getName()).append("(");
        sbFind.append($store.getName()).append("(%m, ");
        for (int i = 0; i < domain.size(); i++) {
            sbFind.append("%d").append(i).append(", ");
        }
        sbFind.append("%v)");
        for (int i = 0; i < domain.size(); i++) {
            sbFind.append(", ").append("%t").append(i);
        }
        sbFind.append(")");

        // $load(%m, %T)
        StringBuilder sbReplace = new StringBuilder($load.getName());
        sbReplace.append("(%m");
        for (int i = 0; i < domain.size(); i++) {
            sbReplace.append(", ").append("%t").append(i);
        }
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
        // replace: cond($weq(%D, %T), retype(%v), $load(%m, %T))

        Map<String, String> tags = new HashMap<String, String>();

        tags.put("rewrite", "fol simp");
        tags.put("asAxiom", "");

        List<Term> none = new LinkedList<Term>();

        List<GoalAction> actions = new LinkedList<GoalAction>();

        // $load($store(%m, %D, %v), %T)
        StringBuilder sbFind = new StringBuilder();
        sbFind.append($load.getName()).append("(");
        sbFind.append($store.getName()).append("(%m, ");
        for (int i = 0; i < domain.size(); i++) {
            sbFind.append("%d").append(i).append(", ");
        }
        sbFind.append("%v)");
        for (int i = 0; i < domain.size(); i++) {
            sbFind.append(", ").append("%t").append(i);
        }
        sbFind.append(")");

        // cond($weq(%D, %T), retype(%v), $load(%m, %T))
        StringBuilder sbReplace = new StringBuilder("cond(");
        if (0 == domain.size()) {
            sbReplace.append("true");
        }
        for (int i = 0; i < domain.size(); i++) {
            if (i > 0) {
                sbReplace.append("&");
            }
            sbReplace.append("$weq(%d").append(i).append(", ").append("%t").append(i).append(")");
        }
        sbReplace.append(", retype(%v), ").append($load.getName()).append("(%m");
        for (int i = 0; i < domain.size(); i++) {
            sbReplace.append(", ").append("%t").append(i);
        }
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