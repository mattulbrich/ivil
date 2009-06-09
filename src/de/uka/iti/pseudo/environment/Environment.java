/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.environment;

import java.util.LinkedHashMap;
import java.util.Map;

import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeApplication;

public class Environment {
    
    private Map<String, Sort> sortMap = new LinkedHashMap<String, Sort>();
    private Map<String, Function> functionMap = new LinkedHashMap<String, Function>();
    private Map<String, Binder> binderMap = new LinkedHashMap<String, Binder>();
    private Map<String, FixOperator> infixMap = new LinkedHashMap<String, FixOperator>();
    private Map<String, FixOperator> prefixMap = new LinkedHashMap<String, FixOperator>();
    
    
    public Environment() {
    	addBuiltIns();
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
        return sortMap.get(name);
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
        return functionMap.get(name);
    }
    
    public Function getNumberLiteral(String numberliteral) {
    	Function retval = getFunction(numberliteral);
    	if(retval == null) {
    		try {
				retval = new Function(numberliteral, mkType("int"), new Type[0], 
								true, ASTLocatedElement.BUILTIN);
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
    }
    
	public FixOperator getInfixOperator(String opSymb) {
		return infixMap.get(opSymb);
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
    }

	
	public FixOperator getPrefixOperator(String opSymb) {
		return prefixMap.get(opSymb);
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
        return binderMap.get(name);
    }

    public Type mkType(String name, Type... domTy) throws EnvironmentException, TermException {
        // DOC
        
        Sort sort = getSort(name);
        
        if(sort == null) {
            throw new EnvironmentException("Sort " + name + " unknown");
        }
             
        return new TypeApplication(sort, domTy);
    }

    
    public void dump() {
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
    }



}
