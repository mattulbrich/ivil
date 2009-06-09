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

public class Environment {
    
    public static final Environment BUILT_IN_ENV = new Environment();
    
    private String resourceName;
    private @Nullable Environment parentEnvironment;
    
    private Map<String, Sort> sortMap = new LinkedHashMap<String, Sort>();
    private Map<String, Function> functionMap = new LinkedHashMap<String, Function>();
    private Map<String, Binder> binderMap = new LinkedHashMap<String, Binder>();
    private Map<String, FixOperator> infixMap = new LinkedHashMap<String, FixOperator>();
    private Map<String, FixOperator> prefixMap = new LinkedHashMap<String, FixOperator>();
    private Map<String, FixOperator> reverseFixityMap = new LinkedHashMap<String, FixOperator>();

    private List<Rule> rules = new ArrayList<Rule>();
    
    private Environment() {
        this.resourceName = "built-in";
        addBuiltIns();
    }
     
    public Environment(@NonNull String resourceName, @NonNull Environment parentEnvironment) {
        this.resourceName = resourceName;   
        this.parentEnvironment = parentEnvironment;
    }

    private void addBuiltIns() {
    	try {
			addSort(new Sort("int", 0, ASTLocatedElement.BUILTIN));
			addSort(new Sort("bool", 0, ASTLocatedElement.BUILTIN));
		} catch (EnvironmentException e) {
			throw new Error("Fatal during creation of interal elements", e);
		}
	}

    
    //
    // Sorts
    // 
	public void addSort(Sort sort) throws EnvironmentException {
        String name = sort.getName();
        Sort existing = getSort(name);
        
        if(existing != null) {
            throw EnvironmentException.definedTwice("Sort " + name, 
                    existing.getDeclaration(),
                    sort.getDeclaration());
        }
        
        sortMap.put(name, sort);
    }
    
    public @Nullable Sort getSort(String name) {
        Sort sort = sortMap.get(name);
        if(sort == null && parentEnvironment != null)
            sort = parentEnvironment.getSort(name);
        return sort;
    }
    
    public @NonNull Type getIntType() {
    	try {
            return new TypeApplication(getSort("int"));
        } catch (TermException e) {
            // "int" is presend since builtin
            throw new Error(e);
        }
    }
    
    public @NonNull Type getBoolType() {
        try {
            return new TypeApplication(getSort("bool"));
        } catch (TermException e) {
            // "int" is presend since builtin
            throw new Error(e);
        }
    }
    
    //
    // Functions
    //

    public void addFunction(Function function) throws EnvironmentException {
        String name = function.getName();
        Function existing = getFunction(name);
        
        if(existing != null) {
            throw EnvironmentException.definedTwice("Function " + name, 
                    existing.getDeclaration(),
                    function.getDeclaration());
        }
        
//        if(!checkNoFreeReturnTypeVariables(function.getResultType(), function.getArgumentTypes(), null))
//            throw new EnvironmentException("Function " + name + " has a free return typevariable. ",
//                    function.getDeclaration());
        
        functionMap.put(name, function);
    }
    
    public Function getFunction(String name) {
        Function function = functionMap.get(name);
        if(function == null && parentEnvironment != null)
            function = parentEnvironment.getFunction(name);
        return function;
    }
    
    public Function getNumberLiteral(String numberliteral) {
    	Function retval = getFunction(numberliteral);
    	if(retval == null) {
    		try {
				retval = new Function(numberliteral, mkType("int"), new Type[0], 
								true, false, ASTLocatedElement.BUILTIN);
				addFunction(retval);
			} catch (Exception e) {
				throw new Error("Fatal while creating number constant for " + numberliteral, e);
			}
    	}
    	return retval;
    }

    /**
     * check whether are no free type variables in the return type. there may be
     * free type variables in the argument types however.
     * 
     * This is done by ensuring that the number of type variables does not
     * increase when adding the type vars from the return type.
     * 
     * @param resultType
     *            non-null type reference that must contain less type variables
     *            than the arguments
     * @param argumentTypes
     *            a non-null array of type references
     * @param additionalType
     *            if there is a relevant type outside the argument types
     *            (variable type for binders) may be null
     * @return true iff no free typevars in result type
     */
    
    /*    Set('a) emptySet is a good example that this is not needed
    private boolean checkNoFreeReturnTypeVariables(Type resultType,
            Type[] argumentTypes, Type additionalType) {
        
        Set<String> typevars = new HashSet<String>();
        
        for (Type typeReference : argumentTypes) {
            typeReference.collectTypeVariables(typevars);
        }
        
        if(additionalType != null)
            additionalType.collectTypeVariables(typevars);
        
        int sizeBefore = typevars.size();
        
        resultType.collectTypeVariables(typevars);
        
        int sizeAfter = typevars.size();
        
        return sizeBefore == sizeAfter;
    }*/



    public void addInfixOperator(FixOperator infixOperator) throws EnvironmentException {
    	FixOperator existing = getInfixOperator(infixOperator.getOpIdentifier());
    	
    	if(existing != null) {
            throw EnvironmentException.definedTwice("Infix operator " + infixOperator.getOpIdentifier(), 
                    existing.getDeclaration(),
                    infixOperator.getDeclaration());
        }
    	
    	assert infixOperator.getArity() == 2;
    	
    	infixMap.put(infixOperator.getOpIdentifier(), infixOperator);
    	reverseFixityMap.put(infixOperator.getName(), infixOperator);
    }
    
	public FixOperator getInfixOperator(String opSymb) {
		FixOperator fixOperator = infixMap.get(opSymb);
		if(fixOperator == null && parentEnvironment != null)
		    fixOperator = parentEnvironment.getInfixOperator(opSymb);
        return fixOperator;
	}
	
    public void addPrefixOperator(FixOperator prefixOperator) throws EnvironmentException {
    	FixOperator existing = getPrefixOperator(prefixOperator.getOpIdentifier());
    	
    	if(existing != null) {
            throw EnvironmentException.definedTwice("Prefix operator " + prefixOperator.getOpIdentifier(), 
                    existing.getDeclaration(),
                    prefixOperator.getDeclaration());
        }
    	
    	assert prefixOperator.getArity() == 1;
    	
    	prefixMap.put(prefixOperator.getOpIdentifier(), prefixOperator);
    	reverseFixityMap.put(prefixOperator.getName(), prefixOperator);
    }

	
	public FixOperator getPrefixOperator(String opSymb) {
		FixOperator fixOperator = prefixMap.get(opSymb);
		if(fixOperator == null && parentEnvironment != null)
		    fixOperator = parentEnvironment.getPrefixOperator(opSymb);
        return fixOperator;
	}



    public void addBinder(Binder binder) throws EnvironmentException {
        String name = binder.getName();
        Binder existing = getBinder(name);
        
        if(existing != null) {
            throw EnvironmentException.definedTwice("Binder " + name, 
                    existing.getDeclaration(),
                    binder.getDeclaration());
        }
        
//        if(!checkNoFreeReturnTypeVariables(binder.getResultType(), 
//                binder.getArgumentTypes(), binder.getVarType()))
//            throw new EnvironmentException("Function " + name + " has a free return typevariable. ",
//                    binder.getDeclaration());
        
        binderMap.put(name, binder);
    }

    public Binder getBinder(String name) {
        Binder binder = binderMap.get(name);
        if(binder == null && parentEnvironment != null)
            binder = parentEnvironment.getBinder(name);
        return binder;
    }

    public Type mkType(String name, Type... domTy) throws EnvironmentException, TermException {
        // DOC
        
        Sort sort = getSort(name);
        
        if(sort == null) {
            throw new EnvironmentException("Sort " + name + " unknown");
        }
             
        return new TypeApplication(sort, domTy);
    }
    
    public void addRule(Rule rule) {
        rules.add(rule);
    }

    
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
        
        if(parentEnvironment != null) {
            System.out.print("extending ");
            parentEnvironment.dump();
        }
        
    }

    // null only for the built-in
    public  Environment getParent() {
        return parentEnvironment;
    }

    // used during creation
    public void setParent(@NonNull Environment environment) {
        parentEnvironment = environment;
    }

    // TODO parental lookup
    public FixOperator getReverseFixOperator(String fctname) {
        FixOperator fixOperator = reverseFixityMap.get(fctname);
        if(fixOperator == null && parentEnvironment != null)
            fixOperator = parentEnvironment.getReverseFixOperator(fctname);
        return fixOperator;
    }

}
