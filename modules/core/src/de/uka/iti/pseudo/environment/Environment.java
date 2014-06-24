/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.environment;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import nonnull.NonNull;
import nonnull.Nullable;
import checkers.nullness.quals.LazyNonNull;
import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeApplication;
import de.uka.iti.pseudo.util.Dump;
import de.uka.iti.pseudo.util.Util;

/**
 * The Class Environment captures all defined entities for a proof environment
 * and provides lookup facilities for
 * <ul>
 * <li>sorts
 * <li>functions
 * <li>binders
 * <li>fix operators
 * <li>rules (and axiom rules)
 * <li>lemmas (and axioms)
 * <li>programs
 * <li>properties
 * </ul>
 *
 * Every environment has a parent environment which it extends. Lookups that it
 * cannot resolve are delegated to that parent.
 *
 * There is exactly one environment {@link #BUILT_IN_ENV} which does not have a
 * parent environment.
 *
 * An environment can be closed. After that, it is immutable. In particular,
 * creating a child environment closes an environment.
 *
 * Not all symbols are stored in Environments. Symbols created during a proof
 * are stored locally in the respective proof node that introduces them.
 *
 * @see SymbolTable
 */
public class Environment {

    /**
     * The environment BUILT_IN_ENV is an environment only containing what is
     * defined in {@link #addBuiltIns()}.
     */
    public static final Environment BUILT_IN_ENV = new Environment();

    // This is not included in the standard constructor
    // since meta functions might want access to BUILT_IN_ENV.
    static {
        BUILT_IN_ENV.addBuiltIns();
        BUILT_IN_ENV.setFixed();
        // needed for the dummy-url "none:built-in"
        Util.registerURLHandlers();
    }

    /**
     * The resource from which this environment has been read from.
     */
    private final String resourceName;

    /**
     * The parent environment. Null only for the built in environment
     */
    private @LazyNonNull Environment parentEnvironment;

    /**
     * Has this environment been fixed?
     */
    private boolean fixed;

    /**
     * The mappings from names (strings) to the various elements.
     * Checkstyle: OFF LineLength
     */
    private final Map<String, String> propertiesMap = new HashMap<String, String>();
    private final Map<String, Sort> sortMap = new LinkedHashMap<String, Sort>();
    private final Map<String, Function> functionMap = new LinkedHashMap<String, Function>();
    private final Map<String, Binder> binderMap = new LinkedHashMap<String, Binder>();
    private final Map<String, FixOperator> infixMap = new LinkedHashMap<String, FixOperator>();
    private final Map<String, Lemma> lemmaMap = new LinkedHashMap<String, Lemma>();
    private final Map<String, FixOperator> prefixMap = new LinkedHashMap<String, FixOperator>();
    private final Map<String, FixOperator> reverseFixityMap = new LinkedHashMap<String, FixOperator>();
    private final Map<String, Program> programMap = new LinkedHashMap<String, Program>();
    // plugin manager is created lazily
    private @Nullable PluginManager pluginManager = null;
    // literal map is created lazily.
    private @Nullable Map<BigInteger, NumberLiteral> numberMap = null;

    // Checkstyle: ON LineLength

    /**
     * The rules are kept as a sorted set and as a map.
     */
    private final List<Rule> rules = new ArrayList<Rule>();
    private final Map<String, Rule> ruleMap = new HashMap<String, Rule>();

    /**
     * Instantiates a new environment which only contains the built ins.
     */
    private Environment() {
        this.resourceName = "none:built-in";
        this.fixed = false;
    }

    /**
     * Instantiates a new environment with a given name and parent.
     *
     * The second parameter must be a valid URL string.
     *
     * @param resourceName
     *            the url of the resource where we have elements from
     * @param parentEnvironment
     *            the parent environment to use as fall back facility.
     * @throws EnvironmentException
     *             if the parent environment has not been fixed yet.
     */
    public Environment(@NonNull String resourceName,
            @NonNull Environment parentEnvironment) throws EnvironmentException {
        this.resourceName = resourceName;
        this.parentEnvironment = parentEnvironment;
        this.fixed = false;

        // to find errors in the usage of resourceName
        try {
            new URL(resourceName);
        } catch (MalformedURLException e) {
            throw new EnvironmentException("Resource must be a URL", e);
        }

        if (!parentEnvironment.isFixed()) {
            throw new EnvironmentException(
                    "An environment which is parent to another environment needs to be fixed");
        }
    }

    /**
     * Adds the built ins. These are those entities that would raise exceptions
     * in the very core libraries if they were absent.
     *
     * They are:
     * <ul>
     * <li>Types int and bool
     * <li>the constants true and false
     * </ul>
     */
    private void addBuiltIns() {
        try {
            addSort(new Sort("int", 0, ASTLocatedElement.BUILTIN));
            addSort(new Sort("bool", 0, ASTLocatedElement.BUILTIN));
            addFunction(new Function("true", getBoolType(), new Type[0], true,
                    false, ASTLocatedElement.BUILTIN));
            addFunction(new Function("false", getBoolType(), new Type[0], true,
                    false, ASTLocatedElement.BUILTIN));
        } catch (EnvironmentException e) {
            throw new Error("Fatal during creation of interal elements", e);
        }
    }

    /**
     * Registers all elements that have been defined through plugins.
     *
     * Currently this are only meta functions.
     *
     * @throws EnvironmentException
     *             if the addition of meta functions fails.
     */
    public void registerPlugins() throws EnvironmentException {
        PluginManager pm = getPluginManager();
        List<MetaFunction> metaFunctions = pm.getLocalPlugins(
                MetaFunction.SERVICE_NAME, MetaFunction.class);

        for (MetaFunction metaFunction : metaFunctions) {
            addFunction(metaFunction);
        }
    }

    /**
     * Gets the parent. This cannot return null. An exception is thrown if this
     * environment is the built-in environment which does not have a parent
     * environment.
     *
     * @return the parent environment
     * @throws EnvironmentException
     *             if <tt>this == {@link #BUILT_IN_ENV}</tt>
     */
    public @NonNull Environment getParent() throws EnvironmentException {
        if(parentEnvironment == null) {
            throw new EnvironmentException("This environment does not have a parent");
        }

        return parentEnvironment;
    }

    /**
     * Sets the parent environment. This must happen <b>before</b> any plugins,
     * sort, function, binder or rule has been added to the environment.
     *
     * @param environment
     *            the new parent environment
     *
     * @throws EnvironmentException
     *             if this environment has already been altered.
     */
    public void setParent(@NonNull Environment environment)
            throws EnvironmentException {
        if (!sortMap.isEmpty() || !functionMap.isEmpty()
                || !binderMap.isEmpty() || !rules.isEmpty()
                || pluginManager != null) {
            Dump.dumpEnv(this);
            throw new EnvironmentException(
                    "setting parent on inhabited environment forbidden");
        }
        parentEnvironment = environment;
        assert parentEnvironment.isFixed();
    }

    /**
     * set this environment as fixed.
     *
     * After this, no new symbols may be added to it. A call to
     * {@link #addBinder(Binder)}, {@link #addFunction(Function)}, ... yields an
     * {@link EnvironmentException}.
     */
    public void setFixed() {
        fixed = true;
    }

    /**
     * Has this environment already been fixed? Or may new symbols still be
     * declared?
     *
     * @return true if this environment has been fixed and does not allow to add
     *         new symbols
     */
    public boolean isFixed() {
        return fixed;
    }

    /**
     * get a program by name
     *
     * <p>
     * If no program is defined by this name, the call is delegated to the
     * parent environment. Null is returned if no program has been defined
     * within all reachable environments.
     *
     * @param programName
     *            name of the searched program
     *
     * @return a program or null
     */
    public @Nullable Program getProgram(@NonNull String programName) {
        Program program = programMap.get(programName);

        if (program == null && parentEnvironment != null) {
            program = parentEnvironment.getProgram(programName);
        }

        return program;
    }

    /**
     * Gets a program name which starts with the given prefix and which has not
     * yet been bound in the environment.
     *
     * A number of 0 or more ticks (') are appended to make the name unique.
     *
     * @param prefix
     *            the prefix of the name to return.
     *
     * @return the fresh program name
     *
     * @see SymbolTable#createNewProgramName(String)
     */
    public @NonNull String createNewProgramName(@NonNull String prefix) {
        while (getProgram(prefix) != null) {
            prefix += "'";
        }

        return prefix;
    }

    /**
     * gets a collection containing all programs of this environment and all
     * parenting environments.
     *
     * <p>
     * The result is a freshly created collection which you may modify.
     *
     * @return a freshly created list of all programs
     */
    public @NonNull List<Program> getAllPrograms() {
        List<Program> programs;

        if (parentEnvironment == null) {
            programs = new LinkedList<Program>();
        } else {
            programs = parentEnvironment.getAllPrograms();
        }

        programs.addAll(programMap.values());
        return programs;
    }

    /**
     * gets a collection containing all programs defined in this environment.
     * Programs defined in parent environments are not included.
     *
     * @return an unmodifiable list of the local programs
     */
    public @NonNull Collection<Program> getLocalPrograms() {
        return Collections.<Program>unmodifiableCollection(programMap.values());
    }

    /**
     * add a program of the environment.
     *
     * <p>
     * No program by the same name may already exist in this (or a parent)
     * environment
     *
     * @param program
     *            the program to add to the environment
     *
     * @throws EnvironmentException
     *             if the environment has already been fixed.
     */
    public void addProgram(@NonNull Program program)
            throws EnvironmentException {
        if (isFixed()) {
            throw new EnvironmentException(
                    "This environment has already been fixed, program cannot be set");
        }
        String programName = program.getName();
        Program existing = getProgram(programName);

        if (existing != null) {
            throw new EnvironmentException("A program with name " + programName
                    + " has already been defined at "
                    + existing.getDeclaration().getLocation());
        }

        programMap.put(programName, program);

    }

    //
    // ---------- Handling properties ----------
    //

    /**
     * Add a property; older definitions will be overwritten.
     *
     * @param name
     *            The name of the property, which describes the location of a
     *            setter that takes value
     * @param value
     *            the new value for the property
     */
    public void addProperty(String name, String value) {
        propertiesMap.put(name, value);
    }

    /**
     * Checks if this environment or a parant has given property.
     *
     * @param name
     *            The name of the property to be searched
     * @return true if property was specified
     */
    public boolean hasProperty(String name) {
        if(propertiesMap.containsKey(name)) {
            return true;
        } else if(null != parentEnvironment) {
            return parentEnvironment.hasProperty(name);
        } else {
            return false;
        }
    }

    /**
     * Retrieves one particular property from this environment.
     *
     * Delegate to the parent environment if there is one and the property is
     * not set in this environment
     *
     * @param name
     *            Name of the property to be retrieved
     *
     * @return <code>null</code> if property was not specified
     */
    public @Nullable String getProperty(@NonNull String name) {
        String rval = propertiesMap.get(name);
        if(rval == null && parentEnvironment != null) {
            rval = parentEnvironment.getProperty(name);
        }

        return rval;
    }

    /**
     * Retrieves a map of all locally defined properties.
     *
     * @return an unmodifiable map of properties.
     */
    public @NonNull Map<String, String> getLocalProperties() {
        return Collections.unmodifiableMap(propertiesMap);
    }

    /**
     * Retrieve a map all defined properties.
     *
     * It contains all local properties and all properties of all parents. If a
     * property is defined more than once, the most local definition wins.
     *
     * @return a freshly created property object
     */
    public @NonNull Map<String, String> getAllProperties() {
        Map<String, String> result = new HashMap<String, String>();
        addAllProperties(result);
        return result;
    }

    /*
     * Helper method to add the properties in the correct order:
     * First the parent's, then mine.
     */
    private void addAllProperties(Map<String, String> map) {
        if(parentEnvironment != null) {
            parentEnvironment.addAllProperties(map);
        }
        map.putAll(propertiesMap);
    }

    //
    // ---------- Handling sorts ----------
    //
    /**
     * Adds a sort.
     *
     * @param sort
     *            the sort
     *
     * @throws EnvironmentException
     *             a sort of this name already exists or the environment has
     *             been fixed.
     */
    public void addSort(@NonNull Sort sort) throws EnvironmentException {

        if (isFixed()) {
            throw new EnvironmentException(
                    "cannot add to this environment, it has been fixed already");
        }

        String name = sort.getName();
        Sort existing = getSort(name);

        if (existing != null) {
            throw new EnvironmentException("Sort " + name
                    + " has already been defined at "
                    + existing.getDeclaration().getLocation());
        }

        sortMap.put(name, sort);
    }

    /**
     * Gets a sort for a name. Delegates the lookup to the parent environment
     * (if present). Returns null if in no environment in the parent chain a
     * definition can be found.
     *
     * @param name
     *            the name to look up
     *
     * @return a sort with the name <code>name</code>, null if none found
     */
    public @Nullable Sort getSort(@NonNull String name) {
        Sort sort = sortMap.get(name);
        if (sort == null && parentEnvironment != null) {
            sort = parentEnvironment.getSort(name);
        }
        return sort;
    }

    /**
     * get the list of all sorts defined in this environment. Sorts visible in
     * this environment but defined in a parent environment are ignored.
     *
     * @return an unmodifiable collection of sorts
     */
    public Collection<Sort> getLocalSorts() {
        return sortMap.values();
    }

    /**
     * gets a collection containing all sort symbols of this environment and
     * all parenting environments.
     *
     * <p>
     * The result is a freshly created collection which you may modify.
     *
     * @return a freshly created list of all sort symbols
     */
    public List<Sort> getAllSorts() {
        List<Sort> sorts;

        if (parentEnvironment == null) {
            sorts = new ArrayList<Sort>();
        } else {
            sorts = parentEnvironment.getAllSorts();
        }

        sorts.addAll(sortMap.values());
        return sorts;
    }


    /**
     * Gets a sort name which starts with the given prefix and which has not yet
     * been bound in the environment.
     *
     * If <code>prefix</code> is no longer available as name, the smallest
     * natural number (starting at 1) will be appended which results in a name
     * which is not used for sort.
     *
     * @param prefix
     *            the prefix of the name to return.
     *
     * @return the fresh sort name
     */
    public @NonNull String createNewSortName(@NonNull String prefix) {
        String newName = prefix;
        for (int counter = 1; null != getSort(newName); counter++) {
            newName = prefix + counter;
        }

        return newName;
    }

    /**
     * Gets the int type which is always present.
     *
     * @return the type for integers
     */
    public static @NonNull Type getIntType() {
        try {
            Sort intSort = BUILT_IN_ENV.getSort("int");
            assert intSort != null : "nullness: guaranteed by construction of BUILT_IN_ENV";
            return TypeApplication.getInst(intSort);
        } catch (TermException e) {
            // "int" is present since builtin
            throw new Error(e);
        }
    }

    /**
     * Gets the bool type which is always present.
     *
     * @return the type for booleans
     */
    public static @NonNull Type getBoolType() {
        try {
            Sort boolSort = BUILT_IN_ENV.getSort("bool");
            assert boolSort != null : "nullness: guaranteed by construction of BUILT_IN_ENV";
            return TypeApplication.getInst(boolSort);
        } catch (TermException e) {
            // "boolean" is presend since builtin
            throw new Error(e);
        }
    }

    //
    // ---------- Handling functions ----------
    //

    /**
     * Adds the function if no function of that name is already present.
     *
     * @param function
     *            the function to add to the environment
     *
     * @throws EnvironmentException
     *             if a function by that name already exists or the environment
     *             has been fixed
     */
    public void addFunction(@NonNull Function function)
            throws EnvironmentException {

        if (isFixed()) {
            throw new EnvironmentException(
                    "cannot add to this environment, it has been fixed already");
        }

        String name = function.getName();
        Function existing = getFunction(name);

        if (existing != null) {
            throw new EnvironmentException("Function " + name
                    + " has already been defined at "
                    + existing.getDeclaration().getLocation());
        }

        functionMap.put(name, function);
    }

    /**
     * Gets a function for a name. Delegates the lookup to the parent
     * environment (if present). Returns null if in no environment in the parent
     * chain a definition can be found.
     *
     * @param name
     *            the name to look up
     *
     * @return a function with the name <code>name</code>, null if none found
     */
    public @Nullable Function getFunction(@NonNull String name) {
        Function function = functionMap.get(name);
        if (function == null && parentEnvironment != null) {
            function = parentEnvironment.getFunction(name);
        }
        return function;
    }

    /**
     * gets a collection containing all function symbols of this environment and
     * all parenting environments.
     *
     * <p>
     * The result is a freshly created collection which you may modify.
     *
     * @return a freshly created list of all function symbols
     */
    public @NonNull List<Function> getAllFunctions() {
        List<Function> functions;

        if (parentEnvironment == null) {
            functions = new ArrayList<Function>();
        } else {
            functions = parentEnvironment.getAllFunctions();
        }

        functions.addAll(functionMap.values());
        return functions;
    }

    /**
     * get the list of all functions defined in this environment. Functions
     * visible in this environment but defined in a parent environment are
     * ignored.
     *
     * @return an unmodifiable collection of functions
     */
    public @NonNull Collection<Function> getLocalFunctions() {
        return functionMap.values();
    }

    /**
     * Gets a number literal. It is dynamically added to the top level
     * environment if not already present.
     *
     * @param numberliteral
     *            the literal as string, must be a natural (>=0) number in
     *            decimal radix
     *
     * @return the number literal as a function
     */
    public @NonNull NumberLiteral getNumberLiteral(@NonNull String numberliteral) {
        return getNumberLiteral(new BigInteger(numberliteral));
    }

    /**
     * Gets a number literal. It is dynamically added to the top level
     * environment if not already present.
     *
     * @param numberliteral
     *            the number, a natural (>=0) number
     *
     * @return the number literal as a function
     */
    public @NonNull NumberLiteral getNumberLiteral(@NonNull int numberliteral) {
        return getNumberLiteral(BigInteger.valueOf(numberliteral));
    }

    /**
     * Gets a number literal. It is dynamically added to the top level
     * environment if not already present.
     *
     * @param value
     *            the literal, must be a non-negative number
     *
     * @return the number literal as a function
     */
    public @NonNull NumberLiteral getNumberLiteral(@NonNull BigInteger value) {

        // propagate up to toplevel
        if (parentEnvironment != null) {
            return parentEnvironment.getNumberLiteral(value);
        }

        if (numberMap == null) {
            numberMap = new HashMap<BigInteger, NumberLiteral>();
        }

        NumberLiteral nl = numberMap.get(value);
        if (nl == null) {
            try {
                nl = new NumberLiteral(value, this);
            } catch (Exception e) {
                throw new Error(
                        "Fatal error during creation of integer literal: "
                                + value, e);
            }
            numberMap.put(value, nl);
        }

        return nl;
    }

    /**
     * Gets a collection of all functions which are assignable.
     *
     * @return a freshly created collection of all assignables.
     */
    public @NonNull List<Function> getAllAssignables() {
        List<Function> result = new ArrayList<Function>();

        for (Environment env = this; env != null; env = env.parentEnvironment) {
            for (Function f : env.functionMap.values()) {
                if (f.isAssignable()) {
                    result.add(f);
                }
            }
        }

        return result;
    }

    /**
     * Gets the $interaction symbol which is always present
     *
     * @return the unique $interaction function symbol.
     */
    // public static @NonNull Function getInteractionSymbol() {
    // return BUILT_IN_ENV.getFunction("$interaction");
    // }

    /**
     * The constant true as a freshly created term.
     *
     * @return a fresh application of the constant true
     */
    public static @NonNull Term getTrue() {
        try {
            Function cnstTrue = BUILT_IN_ENV.getFunction("true");
            assert cnstTrue != null : "nullness: existence guaranteed by construction";
            return Application.getInst(cnstTrue, getBoolType());
        } catch (TermException e) {
            throw new Error(e);
        }
    }

    /**
     * The constant false as a freshly created term.
     *
     * @return a fresh application of the constant true
     */
    public static @NonNull Term getFalse() {
        try {
            Function cnstFalse= BUILT_IN_ENV.getFunction("false");
            assert cnstFalse != null : "nullness: existence guaranteed by construction";
            return Application.getInst(cnstFalse, getBoolType());
        } catch (TermException e) {
            throw new Error(e);
        }
    }

    /**
     * Adds an infix operator to the environment. The argument must be a binary
     * operator.
     *
     * @param infixOperator
     *            the infix operator to add
     *
     * @throws EnvironmentException
     *             if an infix operator for this operation has already been
     *             defined or the environment has been fixed
     */
    public void addInfixOperator(@NonNull FixOperator infixOperator)
            throws EnvironmentException {

        if (isFixed()) {
            throw new EnvironmentException(
                    "cannot add to this environment, it has been fixed already");
        }

        FixOperator existing = getInfixOperator(infixOperator.getOpIdentifier());

        if (existing != null) {
            throw new EnvironmentException("Infix operator "
                    + infixOperator.getOpIdentifier()
                    + " has already been defined at "
                    + existing.getDeclaration().getLocation());
        }

        assert infixOperator.getArity() == 2;

        infixMap.put(infixOperator.getOpIdentifier(), infixOperator);
        reverseFixityMap.put(infixOperator.getName(), infixOperator);
    }

    /**
     * Gets an infix operator. Delegates the lookup to the parent environment
     * (if present). Returns null if in no environment in the parent chain a
     * definition can be found.
     *
     * @param opSymb
     *            the operator symbolic name to retrieve a fix operator for
     *
     * @return the infix operator if found, null if not present in the
     *         repository.
     */
    public @Nullable FixOperator getInfixOperator(@NonNull String opSymb) {
        FixOperator fixOperator = infixMap.get(opSymb);
        if (fixOperator == null && parentEnvironment != null) {
            fixOperator = parentEnvironment.getInfixOperator(opSymb);
        }
        return fixOperator;
    }

    /**
     * Adds the prefix operator to the environment. The argument must be unary.
     *
     * @param prefixOperator
     *            the fix operator to add as prefix operator.
     *
     * @throws EnvironmentException
     *             iff the operator symbol has already been defined as a prefix
     *             operator or the environment has been fixed.
     */
    public void addPrefixOperator(@NonNull FixOperator prefixOperator)
            throws EnvironmentException {

        if (isFixed()) {
            throw new EnvironmentException(
                    "cannot add to this environment, it has been fixed already");
        }

        FixOperator existing = getPrefixOperator(prefixOperator
                .getOpIdentifier());

        if (existing != null) {
            throw new EnvironmentException("Prefix operator "
                    + prefixOperator.getOpIdentifier()
                    + " has already been defined at "
                    + existing.getDeclaration().getLocation());
        }

        assert prefixOperator.getArity() == 1;

        prefixMap.put(prefixOperator.getOpIdentifier(), prefixOperator);
        reverseFixityMap.put(prefixOperator.getName(), prefixOperator);
    }

    /**
     * Gets a prefix operator. Delegates the lookup to the parent environment
     * (if present). Returns null if in no environment in the parent chain a
     * definition can be found.
     *
     * @param opSymb
     *            the operator symbolic name to retrieve a fix operator for
     *
     * @return the prefix operator if found, null if not present in the
     *         repository.
     */
    public @Nullable FixOperator getPrefixOperator(@NonNull String opSymb) {
        FixOperator fixOperator = prefixMap.get(opSymb);
        if (fixOperator == null && parentEnvironment != null) {
            fixOperator = parentEnvironment.getPrefixOperator(opSymb);
        }
        return fixOperator;
    }

    /**
     * Gets a fix operator for a given function name.
     *
     * FixOperators must have function names also. We search for an operator for
     * a given function name.
     *
     * In case we do not find an operator, the call is delegated to the parent
     * environment, if there is one. This is a reverse look up, hence the name.
     *
     * @param fctname
     *            the function name
     *
     * @return the corresponding fix operator, null if none found.
     */
    public @Nullable FixOperator getReverseFixOperator(@NonNull String fctname) {
        FixOperator fixOperator = reverseFixityMap.get(fctname);
        if (fixOperator == null && parentEnvironment != null) {
            fixOperator = parentEnvironment.getReverseFixOperator(fctname);
        }
        return fixOperator;
    }

    //
    // ---------- Handling binders ----------
    //

    /**
     * Adds a binder to the environment.
     *
     * @param binder
     *            the binder to add
     *
     * @throws EnvironmentException
     *             iff a binder has already been defined for that name or the
     *             environment has been fixed
     */
    public void addBinder(@NonNull Binder binder) throws EnvironmentException {

        if (isFixed()) {
            throw new EnvironmentException(
                    "cannot add to this environment, it has been fixed already");
        }

        String name = binder.getName();
        Binder existing = getBinder(name);

        if (existing != null) {
            throw new EnvironmentException("Binder " + name
                    + " has already been defined at "
                    + existing.getDeclaration().getLocation());
        }

        // if(!checkNoFreeReturnTypeVariables(binder.getResultType(),
        // binder.getArgumentTypes(), binder.getVarType()))
        // throw new EnvironmentException("Function " + name + " has a free
        // return typevariable. ",
        // binder.getDeclaration());

        binderMap.put(name, binder);
    }

    /**
     * Gets a sort for a name. Delegates the lookup to the parent environment
     * (if present). Returns null if in no environment in the parent chain a
     * definition can be found.
     *
     * @param name
     *            the name to look up
     *
     * @return a sort with the name <code>name</code>, null if none found Gets
     *         the binder.
     */
    public @Nullable Binder getBinder(@NonNull String name) {
        Binder binder = binderMap.get(name);
        if (binder == null && parentEnvironment != null) {
            binder = parentEnvironment.getBinder(name);
        }
        return binder;
    }

    /**
     * gets a collection containing all binder symbols of this environment and
     * all parenting environments.
     *
     * <p>
     * The result is a freshly created collection which you may modify.
     *
     * @return a freshly created list of all binder symbols
     */
    public @NonNull List<Binder> getAllBinders() {
        List<Binder> binders;

        if (parentEnvironment == null) {
            binders = new ArrayList<Binder>();
        } else {
            binders = parentEnvironment.getAllBinders();
        }

        binders.addAll(binderMap.values());
        return binders;
    }

    /**
     * get the list of all binders defined in this environment. Binders
     * visible in this environment but defined in a parent environment are
     * ignored.
     *
     * @return an unmodifiable collection of binders
     */
    public @NonNull Collection<Binder> getLocalBinders() {
        return binderMap.values();
    }

    /**
     * Convenience method to produce type expressions.
     *
     * @param name
     *            name of the top level sort
     * @param domTy
     *            the arguments to the top level sort
     *
     * @return the generated type
     *
     * @throws EnvironmentException
     *             if no sort has been defined for the name.
     * @throws TermException
     *             if the arity does not match the definition of the sort.
     */
    public @NonNull Type mkType(@NonNull String name, Type... domTy)
            throws EnvironmentException, TermException {

        Sort sort = getSort(name);

        if (sort == null) {
            throw new EnvironmentException("Sort " + name + " unknown");
        }

        return TypeApplication.getInst(sort, domTy);
    }

    //
    // ---------- Handling rules ----------
    //

    /**
     * Adds a rule to the envoriment.
     *
     * There must not be a rule present under the same name.
     *
     * @param rule
     *            the rule to add
     * @throws EnvironmentException
     *             if there is already a rule by that name or if the environment
     *             has been fixed
     *
     */
    public void addRule(@NonNull Rule rule) throws EnvironmentException {

        if (isFixed()) {
            throw new EnvironmentException(
                    "cannot add to this environment, it has been fixed already");
        }

        String name = rule.getName();
        Rule existing = getRule(name);
        if (existing != null) {
            throw new EnvironmentException("Rule " + name
                    + " has already been defined at "
                    + existing.getDeclaration().getLocation());
        }

        rules.add(rule);
        ruleMap.put(name, rule);
    }

    /**
     * Gets a rule for a name. Delegates the lookup to the parent environment
     * (if present). Returns null if in no environment in the parent chain a
     * definition can be found.
     *
     * @param name
     *            the name to look up
     *
     * @return a rule with the name <code>name</code>, null if none found
     */
    public @Nullable Rule getRule(@NonNull String name) {
        Rule rule = ruleMap.get(name);
        if (rule == null && parentEnvironment != null) {
            rule = parentEnvironment.getRule(name);
        }
        return rule;
    }

    /**
     * get a list of all defined rules including those defined in a parent
     * environment.
     *
     * The result is a freshly created list which can be modified if you wish.
     *
     * @return a freshly created list of rules.
     */
    public @NonNull List<Rule> getAllRules() {
        List<Rule> retval;
        if (parentEnvironment == null) {
            retval = new ArrayList<Rule>();
        } else {
            retval = parentEnvironment.getAllRules();
        }
        retval.addAll(rules);
        return retval;
    }

    /**
     * get the list of all rules defined in this environment. Rules visible in
     * this environment but defined in a parent environment are ignored.
     *
     * @return an unmodifiable list of rules
     */
    public @NonNull List<Rule> getLocalRules() {
        return Collections.unmodifiableList(rules);
    }

    //
    // ---------- Handling lemmas ----------
    //

    /**
     * Adds an lemma to the environment.
     *
     * <p>
     * No lemma by the same name must already exist in this (or a parent)
     * environment.
     *
     * @param lemma
     *            the lemma to add to the environment
     *
     * @throws EnvironmentException
     *             if the environment has already been fixed.
     */
    public void addLemma(@NonNull Lemma lemma) throws EnvironmentException {
        if (isFixed()) {
            throw new EnvironmentException(
                    "cannot add to this environment, it has been fixed already");
        }

        String name = lemma.getName();
        Lemma existing = getLemma(name);
        if (existing != null) {
            throw new EnvironmentException("Lemma " + name
                    + " has already been defined at "
                    + existing.getDeclaration().getLocation());
        }

        lemmaMap.put(name, lemma);
    }

    /**
     * Gets an lemma by name.
     *
     * <p>
     * If no lemma is defined by this name, the call is delegated to the parent
     * environment. Null is returned if no lemma has been defined within all
     * reachable environments.
     *
     * @param name
     *            name of the requested lemma
     *
     * @return an lemma or null
     */
    public @Nullable Lemma getLemma(@NonNull String name) {
        Lemma lemma = lemmaMap.get(name);

        if (lemma == null && parentEnvironment != null) {
            lemma = parentEnvironment.getLemma(name);
        }

        assert lemma == null || name.equals(lemma.getName());

        return lemma;
    }

    /**
     * gets a collection containing all lemmas of this environment and
     * all parenting environments.
     *
     * <p>
     * The result is a freshly created collection which you may modify.
     * The lemmas are in "order of appearance".
     *
     * @return a freshly created list of all defined lemmas.
     */
    public @NonNull List<Lemma> getAllLemmas() {
        List<Lemma> lemmas;

        if (parentEnvironment == null) {
            lemmas = new ArrayList<Lemma>();
        } else {
            lemmas = parentEnvironment.getAllLemmas();
        }

        lemmas.addAll(lemmaMap.values());
        return lemmas;
    }

    /**
     * Gets the lemmas which are defined in this environment. The lemmas of the
     * parent environments are not gathered.
     *
     * @return an immutable collection to the locally defined lemmas
     */
    public @NonNull Collection<Lemma> getLocalLemmas() {
        return lemmaMap.values();
    }

    //
    // ---------- Handling plugins ----------
    //

    /**
     * Retrieve the plugin manager. If it has not yet been created, create it.
     * However, if this environment is already closed, do not create the manager
     * but throw an exception.
     *
     * @return the plugin manager for this environment.
     * @throws EnvironmentException
     *             if the environment has already been closed.
     */
    public @NonNull PluginManager getPluginManager() throws EnvironmentException {

        if (pluginManager == null) {

            if (isFixed()) {
                throw new EnvironmentException(
                        "cannot create the plugin manager for this environment, "
                                + "it has been fixed already");
            }

            PluginManager parentManger =
                parentEnvironment != null ?
                        parentEnvironment.pluginManager : null;

            pluginManager = new PluginManager(resourceName, parentManger);
        }

        return pluginManager;
    }

    /**
     * Debug dump resource inheritance hierarchiy to stdout.
     */
    public void dumpResourceHierarchy() {

        System.out.println(resourceName);

        if (parentEnvironment != null) {
            System.out.print(" extending ");
            parentEnvironment.dumpResourceHierarchy();
        }
    }



    /**
     * create a new symbol name which is not yet used.
     *
     * We append natural numbers starting with 1. The first one which is not yet
     * used is the candidate to choose.
     *
     * @param prefix
     *            the resulting function name will start with this prefix
     *
     * @return an identifier that can be used as a function name for this
     *         environment
     *
     * @see SymbolTable#createNewFunctionName(String)
     */
    public @NonNull String createNewFunctionName(@NonNull String prefix) {
        String newName = prefix;

        for (int counter = 1; null != getFunction(newName); counter++) {
            newName = prefix + counter;
        }

        return newName;
    }

    /**
     * create a new symbol name which is not yet used.
     *
     * We append natural numbers starting with 1. The first one which is not yet
     * used is the candidate to choose.
     *
     * @param prefix
     *            the resulting function name will start with this prefix
     *
     * @return an identifier that can be used as a lemma name for this
     *         environment
     */
    public @NonNull String createNewLemmaName(@NonNull String prefix) {
        String newName = prefix;

        for (int counter = 1; null != getLemma(newName); counter++) {
            newName = prefix + counter;
        }

        return newName;
    }

    /**
     * is there a direct or indirect parent which as the given string as
     * resource name.
     *
     * @param path
     *            resource to look up
     *
     * @return if this or any parent has the resource set to path
     */
    public boolean hasParentResource(@NonNull String path) {
        if (resourceName.equals(path)) {
            return true;
        }

        if (parentEnvironment != null) {
            return parentEnvironment.hasParentResource(path);
        }

        return false;
    }

    /**
     * get the stored name of this environment. This is either the filename or
     * some internal name.
     *
     * @return the resource name for this environment
     */
    public @NonNull String getResourceName() {
        return resourceName;
    }

    public Environment getCopyWithoutRulesAndLemmas() throws EnvironmentException {
        Environment result = new Environment(resourceName, parentEnvironment);
        result.binderMap.putAll(binderMap);
        result.functionMap.putAll(functionMap);
        result.infixMap.putAll(infixMap);
        result.numberMap.putAll(numberMap);
        // TODO Check this! Perhaps make a copy or fix it or ...
        result.pluginManager = this.pluginManager;
        result.prefixMap.putAll(prefixMap);
        result.programMap.putAll(programMap);
        result.propertiesMap.putAll(propertiesMap);
        result.sortMap.putAll(sortMap);
        return result;
    }

}
