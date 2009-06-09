/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.environment;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeApplication;
import de.uka.iti.pseudo.term.TypeVariable;

/**
 * The Class Environment captures definitions and provides lookup facilities for
 * <ul>
 * <li>sorts
 * <li>functions
 * <li>binders
 * <li>fix operators
 * <li>rules
 * </ul>
 * 
 * Every enviroment has a parent environment and delegates lookups that it
 * cannot resolve to that parent before return null
 */
public class Environment {

    /**
     * The environment BUILT_IN_ENV is an environment only containing what is
     * defined in {@link #addBuiltIns()}.
     */
    public static final Environment BUILT_IN_ENV = new Environment();

    /**
     * The resource name from which this environment is from
     */
    private String resourceName;

    /**
     * The parent environment. Null only for the built in environment
     */
    private @Nullable Environment parentEnvironment;
    
    /**
     * Has this environment been fixed?
     */
    private boolean fixed;

    /**
     * The mappings from names (strings) to the various elements
     */
    private Map<String, Sort> sortMap = new LinkedHashMap<String, Sort>();
    private Map<String, Function> functionMap = new LinkedHashMap<String, Function>();
    private Map<String, Binder> binderMap = new LinkedHashMap<String, Binder>();
    private Map<String, FixOperator> infixMap = new LinkedHashMap<String, FixOperator>();
    private Map<String, FixOperator> prefixMap = new LinkedHashMap<String, FixOperator>();
    private Map<String, FixOperator> reverseFixityMap = new LinkedHashMap<String, FixOperator>();
    // literal map is created lazily.
    private Map<BigInteger, NumberLiteral> numberMap = null;

    /**
     * The rules are kept as a sorted set and as a map
     */
    private List<Rule> rules = new ArrayList<Rule>();

    /**
     * Instantiates a new environment which only contains the built ins.
     */
    private Environment() {
        this.resourceName = "built-in";
        addBuiltIns();
    }

    /**
     * Instantiates a new environment with a given name and parent
     * 
     * @param resourceName
     *            the name of the resource where we have elements from
     * @param parentEnvironment
     *            the parent environment to use as fall back facility.
     */
    public Environment(@NonNull String resourceName,
            @NonNull Environment parentEnvironment) {
        this.resourceName = resourceName;
        this.parentEnvironment = parentEnvironment;
    }

    /**
     * Adds the built ins. They are:
     * <ul>
     * <li>Types int and bool
     * <li>The $interaction marker function symbol
     * <li>All defined {@link MetaFunction}s
     * </ul>
     */
    private void addBuiltIns() {
        try {
            addSort(new Sort("int", 0, ASTLocatedElement.BUILTIN));
            addSort(new Sort("bool", 0, ASTLocatedElement.BUILTIN));
            addFunction(new Function("$interaction", new TypeVariable("arb"), new Type[0], false, false, ASTLocatedElement.BUILTIN));
            for (MetaFunction metaFunction : MetaFunction.META_FUNCTIONS) {
                addFunction(metaFunction);
            }
        } catch (EnvironmentException e) {
            throw new Error("Fatal during creation of interal elements", e);
        }
    }

    /**
     * Gets the parent. This may return null. This is the case for the initial
     * "built-ins-only" environment.
     * 
     * @return the parent environment, possibly null
     */
    public Environment getParent() {
        return parentEnvironment;
    }

    /**
     * Sets the parent environment. This must happen <b>before</b> any sort,
     * function, binder or rule has been added to the environment
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
                || !binderMap.isEmpty() || !rules.isEmpty()) {
            dump();
            throw new EnvironmentException(
                    "setting parent on inhabited environment forbidden");
        }
        parentEnvironment = environment;
    }
    
    /**
     * set this environment as fixed. After this, no new symbols may be added to
     * it. A call to {@link #addBinder(Binder)}, {@link #addFunction(Function)},
     * ... yields an {@link EnvironmentException}.
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
    public void addSort(Sort sort) throws EnvironmentException {
        
        if (isFixed())
            throw new EnvironmentException(
                    "cannot add to this environment, it has been fixed already");
        
        String name = sort.getName();
        Sort existing = getSort(name);

        if (existing != null) {
            throw new EnvironmentException("Sort " + name
                    + " has already been defined at "
                    + existing.getDeclaration());
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
    public @Nullable Sort getSort(String name) {
        Sort sort = sortMap.get(name);
        if (sort == null && parentEnvironment != null)
            sort = parentEnvironment.getSort(name);
        return sort;
    }

    /**
     * Gets the int type which is alway present.
     * 
     * @return the type for integers
     */
    public static @NonNull Type getIntType() {
        try {
            return new TypeApplication(BUILT_IN_ENV.getSort("int"));
        } catch (TermException e) {
            // "int" is presend since builtin
            throw new Error(e);
        }
    }

    /**
     * Gets the bool type which is always present
     * 
     * @return the type for booleans
     */
    public static @NonNull Type getBoolType() {
        try {
            return new TypeApplication(BUILT_IN_ENV.getSort("bool"));
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
    public void addFunction(Function function) throws EnvironmentException {
        
        if (isFixed())
            throw new EnvironmentException(
                    "cannot add to this environment, it has been fixed already");
        
        String name = function.getName();
        Function existing = getFunction(name);

        if (existing != null) {
            throw new EnvironmentException("Function " + name
                    + " has already been defined at "
                    + existing.getDeclaration());
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
    public @Nullable Function getFunction(String name) {
        Function function = functionMap.get(name);
        if (function == null && parentEnvironment != null)
            function = parentEnvironment.getFunction(name);
        return function;
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
    public @NonNull NumberLiteral getNumberLiteral(String numberliteral) {
        return getNumberLiteral(new BigInteger(numberliteral));
    }
    
    /**
     * Gets a number literal. It is dynamically added to the top level
     * environment if not already present.
     * 
     * @param numberliteral
     *            the literal, must be a positive number
     * 
     * @return the number literal as a function
     */
    public @NonNull NumberLiteral getNumberLiteral(BigInteger value) {
        
        // propagate up to toplevel
        if(parentEnvironment != null)
            return parentEnvironment.getNumberLiteral(value);

        if(numberMap == null) {
            numberMap = new HashMap<BigInteger, NumberLiteral>();
        }
        
        NumberLiteral nl = numberMap.get(value);
        if(nl == null) {
            try {
                nl = new NumberLiteral(value, this);
            } catch (Exception e) {
                throw new Error("Fatal error during creation of integer literal: " + value, e);
            }
            numberMap.put(value, nl);
        }
        
        return nl;
    }
    
    /**
     * Gets the $interaction symbol which is always present
     * 
     * @return the unique $interaction function symbol.
     */
    public static @NonNull Function getInteractionSymbol() {
        return BUILT_IN_ENV.getFunction("$interaction");
    }


    /**
     * Adds an infix operator to the environment. The argument must be a binary operator.
     * 
     * @param infixOperator
     *            the infix operator to add
     * 
     * @throws EnvironmentException
     *             if an infix operator for this operation has already been defined
     *             or the environment has been fixed
     */
    public void addInfixOperator(FixOperator infixOperator)
            throws EnvironmentException {
        
        if (isFixed())
            throw new EnvironmentException(
                    "cannot add to this environment, it has been fixed already");
        
        FixOperator existing = getInfixOperator(infixOperator.getOpIdentifier());

        if (existing != null) {
            throw new EnvironmentException("Infix operator "
                    + infixOperator.getOpIdentifier()
                    + " has already been defined at "
                    + existing.getDeclaration());
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
    public FixOperator getInfixOperator(String opSymb) {
        FixOperator fixOperator = infixMap.get(opSymb);
        if (fixOperator == null && parentEnvironment != null)
            fixOperator = parentEnvironment.getInfixOperator(opSymb);
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
        
        if (isFixed())
            throw new EnvironmentException(
                    "cannot add to this environment, it has been fixed already");
        
        FixOperator existing = getPrefixOperator(prefixOperator
                .getOpIdentifier());

        if (existing != null) {
            throw new EnvironmentException("Prefix operator "
                    + prefixOperator.getOpIdentifier()
                    + " has already been defined at "
                    + existing.getDeclaration());
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
    public FixOperator getPrefixOperator(String opSymb) {
        FixOperator fixOperator = prefixMap.get(opSymb);
        if (fixOperator == null && parentEnvironment != null)
            fixOperator = parentEnvironment.getPrefixOperator(opSymb);
        return fixOperator;
    }

    /**
     * Gets the reverse fix operator.
     * 
     * @param fctname
     *            the fctname
     * 
     * @return the reverse fix operator
     */
    public FixOperator getReverseFixOperator(String fctname) {
        FixOperator fixOperator = reverseFixityMap.get(fctname);
        if (fixOperator == null && parentEnvironment != null)
            fixOperator = parentEnvironment.getReverseFixOperator(fctname);
        return fixOperator;
    }

    //
    // ---------- Handling binders ----------
    // 

    /**
     * Adds a binder to the environment
     * 
     * @param binder
     *            the binder to add
     * 
     * @throws EnvironmentException
     *             iff a binder has already been defined for that name or the
     *             environment has been fixed
     */
    public void addBinder(Binder binder) throws EnvironmentException {
        
        if (isFixed())
            throw new EnvironmentException(
                    "cannot add to this environment, it has been fixed already");
        
        String name = binder.getName();
        Binder existing = getBinder(name);

        if (existing != null) {
            throw new EnvironmentException("Binder " + name
                    + " has already been defined at "
                    + existing.getDeclaration());
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
     * @return a sort with the name <code>name</code>, null if none found
     *         Gets the binder.
     * 
     * @param name
     *            the name
     * 
     * @return the binder
     */
    public Binder getBinder(String name) {
        Binder binder = binderMap.get(name);
        if (binder == null && parentEnvironment != null)
            binder = parentEnvironment.getBinder(name);
        return binder;
    }

    /**
     * Convenience mathod to produce Type expressions.
     * 
     * @param name
     *            name of the toplevel sort
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
    public Type mkType(String name, Type... domTy) throws EnvironmentException,
            TermException {

        Sort sort = getSort(name);

        if (sort == null) {
            throw new EnvironmentException("Sort " + name + " unknown");
        }

        return new TypeApplication(sort, domTy);
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
     * @throws EnvironmentException if there is already a rule by that name or if the environment has been fixed
     * 
     */
    public void addRule(@NonNull Rule rule) throws EnvironmentException {
        
        if (isFixed())
            throw new EnvironmentException(
                    "cannot add to this environment, it has been fixed already");
        
        rules.add(rule);
    }
    
    /**
     * get a list of all defined rules including those defined in a parent environment.
     * 
     * @return a freshly created list of rules.
     */
    public @NonNull List<Rule> getAllRules() {
        List<Rule> retval;
        if(parentEnvironment == null) {
            retval = new ArrayList<Rule>();
        } else {
            retval = parentEnvironment.getAllRules();
        }
        retval.addAll(rules);
        return retval;
    }

    /**
     * Debug dump to stdout.
     */
    public void dump() {

        System.out.println("Environment '" + resourceName + "':");

        System.out.println("Sorts:");
        for (String name : sortMap.keySet()) {
            System.out.println("  " + sortMap.get(name));
        }

        System.out.println("Functions:");
        for (String name : functionMap.keySet()) {
            System.out.println("  " + functionMap.get(name));
        }

        System.out.println("Infix Functions:");
        for (String name : infixMap.keySet()) {
            System.out.println("  " + infixMap.get(name));
        }

        System.out.println("Prefix Functions:");
        for (String name : prefixMap.keySet()) {
            System.out.println("  " + prefixMap.get(name));
        }

        System.out.println("Binders:");
        for (String name : binderMap.keySet()) {
            System.out.println("  " + binderMap.get(name));
        }

        System.out.println("Rules:");
        for (Rule rule : rules) {
            rule.dump();
        }

        if (parentEnvironment != null) {
            System.out.print("extending ");
            parentEnvironment.dump();
        }

    }
    
    /**
     * create a new symbol name which is not yet used.
     * 
     * @param prefix
     *            the resulting function name will start with this prefix
     * @return an identifier that can be used as a function name for this
     *         environment
     */
    public String createNewFunctionName(String prefix) {
        String newName;
        int counter = 1;
        boolean exists;
        do {
            newName = prefix + counter;
            counter ++;
            exists = getFunction(newName) != null;
        } while(exists);
        
        return newName;
    }

    private int skolemCounter = 1;
    
    @Deprecated
    public Function createNewSkolemConst(Type type) {
        // TODO method documentation
        
        assert !isFixed();
        
        String newName;
        Function existing;
        do {
            newName = "sk" + skolemCounter;
            skolemCounter ++;
            existing = getFunction(newName);
        } while(existing != null);
        
        Function newFunction = new Function(newName, type, new Type[0], 
                false, false, ASTLocatedElement.BUILTIN);
        
        try {
            addFunction(newFunction);
        } catch (EnvironmentException e) {
            throw new Error("Internal error: Skolem creation fails", e);
        }
        
        return newFunction;
    }


}
