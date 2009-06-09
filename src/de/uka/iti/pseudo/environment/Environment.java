package de.uka.iti.pseudo.environment;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeApplication;

public class Environment {
    
    private Map<String, Sort> sortMap = new HashMap<String, Sort>();
    private Map<String, Function> functionMap = new HashMap<String, Function>();
    private Map<String, Binder> binderMap = new HashMap<String, Binder>();
    private Map<String, InfixOperator> infixMap = new HashMap<String, InfixOperator>();

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
    
    public Sort getSort(String name) {
        return sortMap.get(name);
    }

    public void addFunction(Function function) throws EnvironmentException {
        String name = function.getName();
        Function existing = getFunction(name);
        
        if(existing != null) {
            throw EnvironmentException.definedTwice("Function " + name, 
                    existing.getDeclaration(),
                    function.getDeclaration());
        }
        
        if(!checkNoFreeReturnTypeVariables(function.getResultType(), function.getArgumentTypes(), null))
            throw new EnvironmentException("Function " + name + " has a free return typevariable. ",
                    function.getDeclaration());
        
        functionMap.put(name, function);
    }
    
    public Function getFunction(String name) {
        return functionMap.get(name);
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
    }



    public void addInfixOperator(InfixOperator infixOperator) {
        // DOC
        // TODO Auto-generated method stub

    }

    public void addBinder(Binder binder) throws EnvironmentException {
        String name = binder.getName();
        Binder existing = getBinder(name);
        
        if(existing != null) {
            throw EnvironmentException.definedTwice("Binder " + name, 
                    existing.getDeclaration(),
                    binder.getDeclaration());
        }
        
        if(!checkNoFreeReturnTypeVariables(binder.getResultType(), 
                binder.getArgumentTypes(), binder.getVarType()))
            throw new EnvironmentException("Function " + name + " has a free return typevariable. ",
                    binder.getDeclaration());
        
        binderMap.put(name, binder);
    }

    public Binder getBinder(String name) {
        return binderMap.get(name);
    }

    public Type mkType(String name, Type[] domTy) throws EnvironmentException {
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
        
        System.out.println("Binders:");
        for (String name : binderMap.keySet()) {
            System.out.println("  " + binderMap.get(name));
        }
    }

}
