/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.environment;

import java.util.ArrayList;
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

// TODO: Auto-generated Javadoc

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
     * The mappings from names (strings) to the various elements
     */
    private Map<String, Sort> sortMap = new LinkedHashMap<String, Sort>();
    private Map<String, Function> functionMap = new LinkedHashMap<String, Function>();
    private Map<String, Binder> binderMap = new LinkedHashMap<String, Binder>();
    private Map<String, FixOperator> infixMap = new LinkedHashMap<String, FixOperator>();
    private Map<String, FixOperator> prefixMap = new LinkedHashMap<String, FixOperator>();
    private Map<String, FixOperator> reverseFixityMap = new LinkedHashMap<String, FixOperator>();

    /**
     * The rules are kept as a list
     * 
     * TODO have a map here also?
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

    /*
     * Adds the built ins.
     */
    private void addBuiltIns() {
        try {
            addSort(new Sort("int", 0, ASTLocatedElement.BUILTIN));
            addSort(new Sort("bool", 0, ASTLocatedElement.BUILTIN));
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
                    "setting name on inhabited environment forbidden");
        }
        parentEnvironment = environment;
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
     *             a sort of this name already exists.
     */
    public void addSort(Sort sort) throws EnvironmentException {
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
    public @NonNull Type getIntType() {
        try {
            return new TypeApplication(getSort("int"));
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
    public @NonNull Type getBoolType() {
        try {
            return new TypeApplication(getSort("bool"));
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
     *             if a function by that name already exists.
     */
    public void addFunction(Function function) throws EnvironmentException {
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
     * Gets the number literal. It is dynamically added to the environment if
     * not already present.
     * 
     * @param numberliteral
     *            the numberliteral, must be a natural (>=0) number in decimal
     * 
     * @return the number literal as a function
     */
    public @NonNull Function getNumberLiteral(String numberliteral) {
        
        assert Integer.parseInt(numberliteral) >= 0;
        
        Function retval = getFunction(numberliteral);
        if (retval == null) {
            try {
                retval = new Function(numberliteral, mkType("int"),
                        new Type[0], true, false, ASTLocatedElement.BUILTIN);
                addFunction(retval);
            } catch (Exception e) {
                throw new Error("Fatal while creating number constant for "
                        + numberliteral, e);
            }
        }
        return retval;
    }

    /**
     * Adds an infix operator to the environment.
     * 
     * @param infixOperator
     *            the infix operator
     * 
     * @throws EnvironmentException
     *             if an infix operator for this operation has already been defined
     */
    public void addInfixOperator(FixOperator infixOperator)
            throws EnvironmentException {
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
    
    // TODO: Auto-generated Javadoc from here on downwards.
    
    /**
     * Gets the infix operator.
     * 
     * @param opSymb
     *            the op symb
     * 
     * @return the infix operator
     */
    public FixOperator getInfixOperator(String opSymb) {
        FixOperator fixOperator = infixMap.get(opSymb);
        if (fixOperator == null && parentEnvironment != null)
            fixOperator = parentEnvironment.getInfixOperator(opSymb);
        return fixOperator;
    }

    /**
     * Adds the prefix operator.
     * 
     * @param prefixOperator
     *            the prefix operator
     * 
     * @throws EnvironmentException
     *             the environment exception
     */
    public void addPrefixOperator(FixOperator prefixOperator)
            throws EnvironmentException {
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
     * Gets the prefix operator.
     * 
     * @param opSymb
     *            the op symb
     * 
     * @return the prefix operator
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
     * Adds the binder.
     * 
     * @param binder
     *            the binder
     * 
     * @throws EnvironmentException
     *             the environment exception
     */
    public void addBinder(Binder binder) throws EnvironmentException {
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
     * Mk type.
     * 
     * @param name
     *            the name
     * @param domTy
     *            the dom ty
     * 
     * @return the type
     * 
     * @throws EnvironmentException
     *             the environment exception
     * @throws TermException
     *             the term exception
     */
    public Type mkType(String name, Type... domTy) throws EnvironmentException,
            TermException {
        // DOC

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
     * Adds the rule.
     * 
     * @param rule
     *            the rule
     */
    public void addRule(Rule rule) {
        rules.add(rule);
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

}
