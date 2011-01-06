package de.uka.iti.pseudo.environment.boogie;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.environment.Sort;
import de.uka.iti.pseudo.parser.boogie.ASTElement;
import de.uka.iti.pseudo.parser.boogie.ASTVisitException;
import de.uka.iti.pseudo.parser.boogie.ast.BuiltInType;
import de.uka.iti.pseudo.parser.boogie.ast.MapAccessExpression;
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
import de.uka.iti.pseudo.term.TypeVariable;

/**
 * This type class is used to represent used types in the boogie file. It is
 * designed to allow for easy translation into the ivil type system.
 * 
 * @author timm.felden@felden.com
 * 
 */
public class UniversalType {

    // global instance for bool s
    static final UniversalType BOOL_T;
    // global instance for int s
    static final UniversalType INT_T;

    // to allow for nonnull attributes, this is used a lot
    static final UniversalType[] voidMap = new UniversalType[0];

    static {
        // massive improvement, as it prevents a lot of bools and ints to be
        // created

        UniversalType[] tmp = voidMap;
        BOOL_T = new UniversalType(false, "bool", null, tmp, tmp, tmp, null);
        INT_T = new UniversalType(false, "int", null, tmp, tmp, tmp, null);
    }

    /**
     * this is true iff this *type* is a placeholder for a polymorphic type
     * variable
     */
    final boolean isTypeVariable;

    /**
     * null iff this type represents a map, the typename else
     */
    final String name;

    /**
     * this field will be nonnull iff the type equals a type synonym
     */
    final String aliasname;

    /**
     * linkage of types is done using pointers
     */
    final UniversalType[] parameters;

    /**
     * paths that can be used to infer i'th parameter this uses heavily the
     * structure of generated ASTs: template and domain parameters will be the
     * i'th child; for range -1 will be used, as it is needed for checking but
     * not for inference.
     */
    private final List<InferencePath>[] paths;

    /**
     * if the type was created by a typeconstructor, there can be template
     * arguments; if the template arguments contain typevariables, the type is
     * used in a definition
     */
    final UniversalType[] templateArguments;

    /**
     * contains domaintypes
     */
    final UniversalType[] domain;

    /**
     * contains the range type. if a typesynonym is declared
     */
    final UniversalType range;

    /**
     * Ivil type translation, used to ensure there are no duplicates. For
     * typevariables.
     */
    private Type ivilType = null;

    /**
     * As instances of this class can be created from a lot of objects,
     * factories have been created to ensure intuitive usage and extension
     * 
     * @param isTypeVariable
     *            true iff the type represents a variable used as type parameter
     *            somewhere
     * 
     * @param name
     *            a readable name of this type; in general the name should be
     *            parsable to the defining type; exception to this rule are
     *            procedure types, as they use rangeCounts which have no boogie
     *            equivalent
     * 
     * @param aliasname
     *            used by type synonyms to allow for pretty printing
     * 
     * @param parameters
     *            list of type parameters declared at this level, all parameters
     *            need to be type variables; parameters will be reordered to
     *            make <a,b>... and <b,a>... equal
     * 
     * @param templTypes
     *            used for arguments to so called type constructors
     * 
     * @param domain
     *            the domain of a map type
     * 
     * @param range
     *            the range of a map type or function or procedure
     * 
     * @param rangeCount
     *            number of results of procedures; for simplicity reasons, this
     *            is also used for the other types with ranges
     */
    @SuppressWarnings("unchecked")
    private UniversalType(boolean isTypeVariable, String name, String aliasname, UniversalType[] parameters,
            UniversalType[] templateArguments, UniversalType[] domain, UniversalType range) {
        this.isTypeVariable = isTypeVariable;
        this.name = name;
        this.aliasname = aliasname;
        this.templateArguments = null != templateArguments ? templateArguments : voidMap;
        this.domain = null != domain ? domain : voidMap;
        this.range = range;

        // collect paths
        paths = new List[parameters.length];
        for (int i = 0; i < paths.length; i++) {
            paths[i] = InferencePath.getPaths(this, parameters[i]);
            if (paths[i].size() == 0)
                throw new IllegalArgumentException("Type variable " + parameters[i]
                        + " is not mentioned in the domain!");

            Collections.sort(paths[i]);
        }
        Arrays.sort(paths, InferencePath.listComparator);
        this.parameters = new UniversalType[parameters.length];
        for (int i = 0; i < parameters.length; i++)
            this.parameters[i] = paths[i].get(0).getVariable();

        // create translated typenames for polymorphic type variables
        for (int i = 0; i < parameters.length; i++)
            parameters[i].ivilType = new TypeVariable("polytype_" + i);
    }

    /**
     * Create typeparameter from String.
     * 
     * @param s
     *            name of the typeparameter
     */
    static UniversalType newTypeParameter(String s) {
        UniversalType[] tmp = voidMap;
        return new UniversalType(true, s, null, tmp, tmp, tmp, null);
    }

    /**
     * Create universal equivalent of built in types.
     * 
     * @param t
     */
    static UniversalType newBasicType(BuiltInType t) {
        if (t.isBool())
            return BOOL_T;
        else if (t.isInt())
            return INT_T;

        UniversalType[] tmp = voidMap;
        return new UniversalType(false, t.getPrettyName(), null, tmp, tmp, tmp, null);
    }

    /**
     * Creates a new bool.
     */
    static UniversalType newBool() {
        return BOOL_T;
    }

    /**
     * Creates a new int.
     */
    static UniversalType newInt() {
        return INT_T;
    }

    /**
     * Creates a new bitvector type.
     * 
     * @param dimension
     *            the dimension the new type will have
     * @return a fresh universal type
     */
    static UniversalType newBitvector(int dimension) {
        assert dimension >= 0;

        UniversalType[] tmp = voidMap;
        return new UniversalType(false, "bv" + dimension, null, tmp, tmp, tmp, null);
    }

    /**
     * Create a new anonymous template type definition.
     * 
     * @param name
     * @param argumentCount
     * @return
     */
    static UniversalType newTemplateType(String name, int argumentCount) {

        UniversalType[] tmp = voidMap, args = new UniversalType[argumentCount];
        UniversalType _ = newTypeParameter("_");
        for (int i = 0; i < args.length; i++)
            args[i] = _;

        return new UniversalType(false, name, null, tmp, args, tmp, null);
    }

    /**
     * Returns an instantiated template type.
     * 
     * @param definition
     * @param arguments
     * @return
     * @throws ASTVisitException
     */
    static UniversalType newTemplateType(UniversalType definition, List<UniversalType> arguments)
            throws ASTVisitException {

        // copy interisting arguments
        UniversalType[] args = definition.templateArguments.clone();
        int pos = 0;
        for (int i = 0; i < args.length; i++) {
            if (definition.templateArguments[i].isTypeVariable) {
                if (pos >= arguments.size())
                    throw new ASTVisitException("you have to supply more arguments");

                args[i] = arguments.get(pos);
                pos++;
            } else {
                args[i] = definition.templateArguments[i];
            }
        }

        UniversalType rval;
        if (null != definition.range)
            rval = new UniversalType(false, definition.name, definition.aliasname, definition.parameters, voidMap,
                    definition.domain, definition.range);
        else
            rval = new UniversalType(false, definition.name, definition.aliasname, definition.parameters, args,
                    definition.domain, definition.range);

        if (null != definition.aliasname) {
            // we have to replace occurances to pointers in
            // definitons.templateArguments by the supplied arguments

            rval = replaceInType(rval, definition.templateArguments, args);
        }

        return rval;
    }

    /**
     * Returns a new type synonym definition
     * 
     * @param alias
     * @param templateArguments
     * @param parent
     * @return
     */
    static UniversalType newTypeSynonym(String alias, List<UniversalType> templateArguments, UniversalType parent,
            Map<String, ASTElement> typeSpace) {

        List<UniversalType> args = new LinkedList<UniversalType>();

        int pos = 0;
        for (int i = 0; i < parent.templateArguments.length; i++) {
            if (!parent.templateArguments[i].isTypeVariable) {
                args.add(parent.templateArguments[i]);
            } else {
                if (pos >= templateArguments.size()) {
                    for (UniversalType t : parent.templateArguments)
                        System.out.println(t);

                    throw new IllegalArgumentException("you supplied not enaugh arguments for this synonym");
                }

                if (typeSpace.containsKey(templateArguments.get(pos).name))
                    args.add(templateArguments.get(pos));
                else
                    args.add(newTypeParameter(templateArguments.get(pos).name));
                pos++;
            }
        }
        for (; pos < templateArguments.size(); pos++)
            if (typeSpace.containsKey(templateArguments.get(pos).name))
                args.add(templateArguments.get(pos));
            else
                args.add(newTypeParameter(templateArguments.get(pos).name));

        return new UniversalType(false, parent.name, alias, parent.parameters, args.toArray(new UniversalType[args
                .size()]), parent.domain, parent.range);
    }

    /**
     * Create a map out of parameter, domain and range.
     * 
     * @param param
     * @param domain
     * @param range
     */
    static UniversalType newMap(List<UniversalType> parameters, List<UniversalType> domain, UniversalType range) {

        return new UniversalType(false, "", null, parameters.toArray(new UniversalType[parameters.size()]), null,
                domain.toArray(new UniversalType[domain.size()]), range);

    }

    /**
     * Infers parameter types
     * 
     * @param map
     *            base type
     * @param node
     *            Occurrence of type usage
     * 
     * @param state
     *            state is needed to get type information from nodes
     * 
     * @return type of the result of this access
     * 
     * @throws TypeSystemException
     *             thrown if type can not be inferred because wrong arguments
     *             were supplied
     */
    // FIXME check all inference points to make sure no problems like <a>[a, a]a
    // -> [int, bool]??? occur
    public static UniversalType newInferedType(UniversalType map, MapAccessExpression node,
            final EnvironmentCreationState state) throws TypeSystemException {
        if (0 == map.parameters.length)
            return map;

        UniversalType[] is = new UniversalType[map.parameters.length];

        for (int i = 0; i < map.parameters.length; i++) {
            is[i] = map.paths[i].get(0).inferType(node, state);
        }

        UniversalType rval = new UniversalType(false, map.name, map.aliasname, voidMap, map.templateArguments,
                map.domain, map.range);

        rval = replaceInType(rval, map.parameters, is);
        if (rval.range.isTypeVariable && rval.range == map.range)
            throw new TypeSystemException("\n" + node.getLocation() + " could not infer type for variable "
                    + rval.range);

        return rval;
    }

    /**
     * Usufull recursive occurance replacement for types used by template type
     * instantiation.
     * 
     * @param old
     *            the typeconstructor
     * @param was
     *            values to replace
     * @param is
     *            values that will replace was
     * @return the new type where all was elements are replaced by their
     *         respective is elements
     */
    private static UniversalType replaceInType(UniversalType old, UniversalType[] was, UniversalType[] is) {

        if (null == old)
            return old;

        boolean changes = false, touched;

        UniversalType[] parameters = old.parameters.clone(), templateArguments = old.templateArguments.clone(), domain = old.domain
                .clone();
        UniversalType range = old.range;

        for (int i = 0; i < old.parameters.length; i++) {
            touched = false;
            for (int j = 0; j < was.length; j++) {
                if (was[j] == parameters[i] || was[j].name.equals(parameters[i].name)) {
                    parameters[i] = is[j];
                    touched = true;
                    break;
                }
            }
            if (!touched)
                parameters[i] = replaceInType(parameters[i], was, is);
            if (parameters[i] != old.parameters[i])
                changes = true;
        }

        for (int i = 0; i < old.templateArguments.length; i++) {
            touched = false;
            for (int j = 0; j < was.length; j++) {
                if (was[j] == templateArguments[i] || was[j].name.equals(templateArguments[i].name)) {
                    templateArguments[i] = is[j];
                    touched = true;
                    break;
                }
            }
            if (!touched)
                templateArguments[i] = replaceInType(templateArguments[i], was, is);
            if (templateArguments[i] != old.templateArguments[i])
                changes = true;
        }
        for (int i = 0; i < old.domain.length; i++) {
            touched = false;
            for (int j = 0; j < was.length; j++) {
                if (was[j] == domain[i] || was[j].name.equals(domain[i].name)) {
                    domain[i] = is[j];
                    touched = true;
                    break;
                }
            }
            if (!touched)
                domain[i] = replaceInType(domain[i], was, is);
            if (domain[i] != old.domain[i])
                changes = true;
        }

        if (null != range) {
            touched = false;
            for (int j = 0; j < was.length; j++) {
                if (was[j] == range || was[j].name.equals(range.name)) {
                    range = is[j];
                    touched = true;
                    break;
                }
            }
            if (!touched)
                range = replaceInType(range, was, is);
            if (range != old.range)
                changes = true;
        }

        if (!changes)
            return old;
        else
            return new UniversalType(old.isTypeVariable, old.name, old.aliasname, parameters, templateArguments,
                    domain, range);
    }

    /**
     * This can be used e.g. to check a := [5]5. this statement is valid, if
     * [int]int is a subtype of a, thus a can be declared as "[int]int" or as
     * "<a>[a]int" or as "<a>[a]a"
     * 
     * @param t
     * @return true, iff one could completely replace t with this
     */
    public boolean compatible(UniversalType t) {

        // same types are compatible
        if (this == t)
            return true;

        // if one type is a type variable, both are compatible, as the variable
        // can be instantiated with a matching type
        if (this.isTypeVariable || t.isTypeVariable)
            return true;

        // if we have a mismatching number of arguments, parameters, ..., we
        // can never be compatible
        if (!(name.equals(name) && parameters.length == t.parameters.length
                && templateArguments.length == t.templateArguments.length && domain.length == t.domain.length))
            return false;

        if (range == null)
            return name.equals(t.name);

        // we are compatible, iff we can match paths of typevariables and our
        // children are compatible after we replaced typevariables in the
        // matched way

        if (parameters.length != 0) {
            for (int i = 0; i < paths.length; i++) {
                if (paths[i].size() != t.paths[i].size())
                    return false;

                for (int j = 0; j < paths[i].size(); j++)
                    if (0 != paths[i].get(j).compareTo(t.paths[i].get(j)))
                        return false;
            }
        }

        for (int i = 0; i < templateArguments.length; i++)
            if (!templateArguments[i].compatible(t.templateArguments[i]))
                return false;

        for (int i = 0; i < domain.length; i++)
            if (!domain[i].compatible(t.domain[i]))
                return false;

        if (range != null || t.range != null)
            if (range == null || t.range == null || !range.compatible(t.range))
                return false;

        return true;
    }

    /**
     * Gets the dimension out of an this, if this will evaluate to bitvector.
     * 
     * @return the dimension of the corresponding bitvector type
     * @throws IllegalArgumentException
     *             if this will not evaluate to bitvector
     */
    public int getBVDimension() throws IllegalArgumentException {
        String name;

        if (isTypeVariable)
            return 0;

        if (null == this.range)
            name = this.name;
        else {
            if (null != this.range.range)
                throw new IllegalArgumentException();

            if (range.isTypeVariable)
                return 0;

            name = this.range.name;
        }

        if (!name.subSequence(0, 2).equals("bv"))
            throw new IllegalArgumentException();

        try {
            return Integer.parseInt(name.substring(2));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * @return true iff this type is a bitvector
     */
    public boolean isBitvector() {
        if (isTypeVariable || !name.startsWith("bv"))
            return false;

        try {
            getBVDimension();
        } catch (IllegalArgumentException e) {
            return false;
        }

        return true;
    }

    /**
     * @return true iff there is a subtype, that has nonzero template arguments
     */
    public boolean isConstructor() {
        if (isTypeVariable)
            return false;

        if (null != domain)
            for (int i = 0; i < domain.length; i++)
                if (domain[i].isConstructor())
                    return true;

        if (null != range)
            return range.isConstructor();
        else
            return false;
    }

    // ! used only by this function to create unique type names
    static private AtomicInteger tvarcounter = new AtomicInteger(0);

    private void makeMapD(EnvironmentCreationState state) throws EnvironmentException, TermException {
        String map_t = "map" + domain.length;

        // create the sort ...
        state.env.addSort(new Sort(map_t, domain.length + 1, state.root));

        // ... types ...
        Type domainRange[] = new Type[domain.length + 1];
        Type mapDomainRange[] = new Type[domain.length + 2];
        Type mapDomain[] = new Type[domain.length + 1];
        Type mapType[] = new Type[domain.length + 1];

        for (int i = 0; i < domain.length; i++)
            mapType[i] = new TypeVariable("D" + i);
        mapType[mapType.length - 1] = new TypeVariable("R");

        mapDomainRange[0] = mapDomain[0] = state.env.mkType(map_t, mapType);

        for (int i = 0; i < domain.length; i++)
            domainRange[i] = mapDomain[i + 1] = mapDomainRange[i + 1] = new TypeVariable("d" + i);

        domainRange[domainRange.length - 1] = mapDomainRange[mapDomainRange.length - 1] = new TypeVariable("r");

        // ... functions ...
        state.env.addFunction(new Function(map_t + "_store", mapDomain[0], mapDomainRange, false, false, state.root));

        state.env.addFunction(new Function(map_t + "_load", domainRange[domainRange.length - 1], mapDomain, false,
                false, state.root));

        // ... and rules

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

        Type mt = state.env.mkType(map_t, map_drt);
        SchemaVariable m = new SchemaVariable("%m", mt);

        store_arg[0] = m;
        store_arg[store_arg.length - 1] = v;

        load_arg[0] = new Application(state.env.getFunction(map_t + "_store"), mt, store_arg);
        Term default_find = new Application(state.env.getFunction(map_t + "_load"), new SchemaType("rt"), load_arg);

        try { // /////////////// LOAD STORE SAME

            String name = map_t + "_load_store_same";

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

            args2[0] = new Application(state.env.getFunction(map_t + "_store"), mt, args1);
            Term find = new Application(state.env.getFunction(map_t + "_load"), vt, args2);

            List<GoalAction> actions = new LinkedList<GoalAction>();

            actions.add(new GoalAction("samegoal", null, false, v, new LinkedList<Term>(), new LinkedList<Term>()));

            Map<String, String> tags = new HashMap<String, String>();

            tags.put("rewrite", "concrete");

            state.env.addRule(new Rule(name, new LinkedList<LocatedTerm>(),
                    new LocatedTerm(find, MatchingLocation.BOTH), new LinkedList<WhereClause>(), actions, tags,
                    state.root));

        } catch (RuleException e) {
            e.printStackTrace();
            throw new EnvironmentException(e);
        }

        try { // /////////////// LOAD STORE SAME ASSUME

            String name = map_t + "_load_store_same_assume";

            Term args1[] = new Term[domain.length + 2];
            Term args2[] = new Term[domain.length + 1];

            Type drt[] = new Type[domain.length + 1];

            List<LocatedTerm> assumes = new LinkedList<LocatedTerm>();

            for (int i = 0; i < domain.length; i++) {
                drt[i] = new SchemaType("d" + i);
                args1[i + 1] = new SchemaVariable("%d" + i, drt[i]);
                args2[i + 1] = new SchemaVariable("%t" + i, drt[i]);

                assumes.add(new LocatedTerm(new Application(state.env.getFunction("$eq"), Environment.getBoolType(),
                        new Term[] { args1[i + 1], args2[i + 1] }), MatchingLocation.ANTECEDENT));
            }

            drt[domain.length] = vt;

            args1[0] = m;
            args1[args1.length - 1] = v;

            args2[0] = new Application(state.env.getFunction(map_t + "_store"), mt, args1);
            Term find = new Application(state.env.getFunction(map_t + "_load"), vt, args2);

            List<GoalAction> actions = new LinkedList<GoalAction>();

            actions.add(new GoalAction("samegoal", null, false, v, new LinkedList<Term>(), new LinkedList<Term>()));

            Map<String, String> tags = new HashMap<String, String>();

            tags.put("rewrite", "concrete");

            state.env.addRule(new Rule(name, assumes, new LocatedTerm(find, MatchingLocation.BOTH),
                    new LinkedList<WhereClause>(), actions, tags, state.root));

        } catch (RuleException e) {
            e.printStackTrace();
            throw new EnvironmentException(e);
        }

        try { // /////////////// LOAD STORE OTHER

            Term[] args3 = load_arg.clone();
            args3[0] = store_arg[0];

            List<GoalAction> actions = new LinkedList<GoalAction>();

            actions.add(new GoalAction("samegoal", null, false, new Application(state.env.getFunction(map_t + "_load"),
                    default_find.getType(), args3), new LinkedList<Term>(), new LinkedList<Term>()));

            Map<String, String> tags = new HashMap<String, String>();

            tags.put("rewrite", "concrete");

            // in order
            for (int i = 0; i < domain.length; i++) {
                String name = map_t + "_load_store_other_" + i;

                List<LocatedTerm> assumes = new LinkedList<LocatedTerm>();

                assumes.add(new LocatedTerm(new Application(state.env.getFunction("$eq"), Environment.getBoolType(),
                        new Term[] { load_arg[i + 1], store_arg[i + 1] }), MatchingLocation.SUCCEDENT));

                state.env.addRule(new Rule(name, assumes, new LocatedTerm(default_find, MatchingLocation.BOTH),
                        new LinkedList<WhereClause>(), actions, tags, state.root));

            }
            // reversed order
            for (int i = 0; i < domain.length; i++) {
                String name = map_t + "_load_store_other_r" + i;

                List<LocatedTerm> assumes = new LinkedList<LocatedTerm>();

                assumes.add(new LocatedTerm(new Application(state.env.getFunction("$eq"), Environment.getBoolType(),
                        new Term[] { store_arg[i + 1], load_arg[i + 1] }), MatchingLocation.SUCCEDENT));

                state.env.addRule(new Rule(name, assumes, new LocatedTerm(default_find, MatchingLocation.BOTH),
                        new LinkedList<WhereClause>(), actions, tags, state.root));

            }

        } catch (RuleException e) {
            e.printStackTrace();
            throw new EnvironmentException(e);
        }

        try { // /////////////// LOAD STORE CUT ON DOMAIN
            String name = map_t + "_load_store_cut_on_domain";

            Map<String, String> tags = new HashMap<String, String>();

            tags.put("rewrite", "split");

            List<Term> cut_same = new LinkedList<Term>();

            List<GoalAction> actions = new LinkedList<GoalAction>();

            for (int i = 0; i < domain.length; i++) {

                List<Term> cut = new LinkedList<Term>();
                cut.add((new Application(state.env.getFunction("$eq"), Environment.getBoolType(), new Term[] {
                        store_arg[i + 1], load_arg[i + 1] })));

                actions.add(new GoalAction("samegoal", "different @" + i, false, default_find, new LinkedList<Term>(),
                        cut));

                cut_same.add(cut.get(0));
            }

            actions.add(new GoalAction("samegoal", "same", false, default_find, cut_same, new LinkedList<Term>()));

            state.env.addRule(new Rule(name, new LinkedList<LocatedTerm>(), new LocatedTerm(default_find,
                    MatchingLocation.BOTH), new LinkedList<WhereClause>(), actions, tags, state.root));

        } catch (RuleException e) {
            e.printStackTrace();
            throw new EnvironmentException(e);
        }

        try { // /////////////// STORE LOAD SAME
            String name = map_t + "_store_load_same";

            Term argsL[] = new Term[domain.length + 1];
            Term argsS[] = new Term[domain.length + 2];

            Type drt[] = new Type[domain.length + 1];

            for (int i = 0; i < domain.length; i++) {
                drt[i] = new SchemaType("d" + i);
                argsL[i + 1] = argsS[i + 1] = new SchemaVariable("%d" + i, drt[i]);
            }

            drt[domain.length] = vt;

            argsS[0] = argsL[0] = m;

            argsS[argsL.length] = new Application(state.env.getFunction(map_t + "_load"), vt, argsL);
            Term find = new Application(state.env.getFunction(map_t + "_store"), mt, argsS);

            List<GoalAction> actions = new LinkedList<GoalAction>();

            actions.add(new GoalAction("samegoal", null, false, m, new LinkedList<Term>(), new LinkedList<Term>()));

            Map<String, String> tags = new HashMap<String, String>();

            tags.put("rewrite", "concrete");

            state.env.addRule(new Rule(name, new LinkedList<LocatedTerm>(),
                    new LocatedTerm(find, MatchingLocation.BOTH), new LinkedList<WhereClause>(), actions, tags,
                    state.root));

        } catch (RuleException e) {
            e.printStackTrace();
            throw new EnvironmentException(e);
        }

        try { // /////////////// STORE LOAD SAME ASSUME

            String name = map_t + "_store_load_same_assume";

            Term argsL[] = new Term[domain.length + 1];
            Term argsS[] = new Term[domain.length + 2];

            Type drt[] = new Type[domain.length + 1];

            List<LocatedTerm> assumes = new LinkedList<LocatedTerm>();

            for (int i = 0; i < domain.length; i++) {
                drt[i] = new SchemaType("d" + i);
                argsL[i + 1] = new SchemaVariable("%d" + i, drt[i]);
                argsS[i + 1] = new SchemaVariable("%t" + i, drt[i]);

                assumes.add(new LocatedTerm(new Application(state.env.getFunction("$eq"), Environment.getBoolType(),
                        new Term[] { argsL[i + 1], argsS[i + 1] }), MatchingLocation.ANTECEDENT));
            }

            drt[domain.length] = vt;

            argsS[0] = argsL[0] = m;

            argsS[argsL.length] = new Application(state.env.getFunction(map_t + "_load"), vt, argsL);
            Term find = new Application(state.env.getFunction(map_t + "_store"), mt, argsS);

            List<GoalAction> actions = new LinkedList<GoalAction>();

            actions.add(new GoalAction("samegoal", null, false, m, new LinkedList<Term>(), new LinkedList<Term>()));

            Map<String, String> tags = new HashMap<String, String>();

            tags.put("rewrite", "concrete");

            state.env.addRule(new Rule(name, assumes, new LocatedTerm(find, MatchingLocation.BOTH),
                    new LinkedList<WhereClause>(), actions, tags, state.root));

        } catch (RuleException e) {
            e.printStackTrace();
            throw new EnvironmentException(e);
        }
    }

    /**
     * Translates UniversalTypes into the ivil Type system.
     * 
     * @param state
     * @return will be nonnull, as soon as all types are supported
     * @throws EnvironmentException
     * @throws TermException
     */
    public Type toIvilType(EnvironmentCreationState state) throws EnvironmentException, TermException {
        if (null != ivilType)
            return ivilType;

        if (this == BOOL_T || name.equals("bool")) {
            ivilType = Environment.getBoolType();

        } else if (this == INT_T || name.equals("int")) {
            ivilType = Environment.getIntType();

        } else if (isTypeVariable) {
            ivilType = new TypeVariable("tvar_" + tvarcounter.getAndIncrement() + "__" + name);

        } else if (isBitvector()) {
            ivilType = state.env.mkType("bitvector");

        } else if (null == range) {
            Type[] args = new Type[templateArguments.length];
            for (int i = 0; i < templateArguments.length; i++) {
                args[i] = templateArguments[i].toIvilType(state);
                if (null == args[i])
                    return null;
            }

            ivilType = state.env.mkType("utt_" + name, args);
        } else {

            String map_t = "map" + domain.length;

            // ensure map<D> exists
            if (state.env.getSort(map_t) == null)
                makeMapD(state);

            Type domainRange[] = new Type[domain.length + 1];

            for (int i = 0; i < domain.length; i++)
                domainRange[i] = domain[i].toIvilType(state);

            domainRange[domainRange.length - 1] = range.toIvilType(state);

            ivilType = state.env.mkType(map_t, domainRange);
        }

        return ivilType;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        if (isTypeVariable)
            buf.append("'");

        if (null != aliasname) {
            buf.append(aliasname);
            buf.append(" = ");
        }

        buf.append(name);

        if (templateArguments.length != 0) {
            buf.append(" <<");
            for (int i = 0; i < templateArguments.length; i++) {
                if (i != 0)
                    buf.append(" ");
                buf.append(templateArguments[i]);
            }
            buf.append(">>");
        }

        // construct a nice name a la <...>[...]...
        if (!isTypeVariable && (0 != parameters.length || 0 != domain.length || range != null)) {
            if (0 != parameters.length) {

                buf.append("< ");
                for (int i = 0; i < parameters.length - 1; i++) {
                    buf.append(parameters[i].name);
                    buf.append(", ");
                }
                buf.append(parameters[parameters.length - 1].name);
                buf.append(" >");
            }

            buf.append("[");
            if (0 != domain.length) {
                buf.append(" ");
                for (int i = 0; i < domain.length - 1; i++) {
                    buf.append(domain[i].toString());
                    buf.append(", ");
                }
                buf.append(domain[domain.length - 1].toString());
                buf.append(" ");
            }
            buf.append("]");

            buf.append(range.toString());
        }

        return buf.toString();
    }
}
