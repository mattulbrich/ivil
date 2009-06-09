/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */

package de.uka.iti.pseudo.term;

import java.util.List;

import nonnull.NonNull;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.term.creation.TypeUnification;
import de.uka.iti.pseudo.util.Util;

/**
 * The Class Application encapsulates a term with a toplevel function symbol.
 * There may be argument terms but it may also be a constant w/o arguments.
 * 
 * The arguments are stored as subterms in the superclass.
 */
public class Application extends Term {

    /**
     * The function which is this term's top level symbol
     */
    private Function function;

    /**
     * Instantiates a new application term.
     * 
     * @param funct
     *            the function symbol to use
     * @param type
     *            the type to set for this term
     * @param subterms
     *            the arguments to the symbol
     * 
     * @throws TermException
     *             if the type check fails.
     */
    public Application(Function funct, Type type, Term[] subterms)
            throws TermException {
        super(subterms, type);
        this.function = funct;
        typeCheck();
    }

    /**
     * Instantiates a new constant w/o arguments.
     * 
     * @param funct
     *            the constant symbol
     * @param type
     *            the type to set
     * 
     * @throws TermException
     *            if type checking fails.
     */
    public Application(Function funct, Type type) throws TermException {
        super(type);
        this.function = funct;
        typeCheck();
    }
    
    /**
     * retrieve the top level symbol of this application
     * @return the top level function symbol
     */
    public @NonNull Function getFunction() {
        return function;
    }

    /**
     * Type check this application term. This includes:
     * <ol>
     * <li>check arity to match symbol definition
     * <li>check that given the argument terms, the term can be typed
     * <li>and that the result type is compatible with the typing result
     * </ol>
     *  
     * @throws TermException
     *             the term exception
     */
    private void typeCheck() throws TermException {

        if (countSubterms() != function.getArity()) {
            throw new TermException("Function " + function + " expects "
                    + function.getArity() + " arguments, but got:\n"
                    + Util.listTerms(getSubterms()));
        }

        TypeUnification unify = new TypeUnification();
        Type[] argumentTypes = function.getArgumentTypes();

        try {
            for (int i = 0; i < countSubterms(); i++) {
                unify.leftUnify(argumentTypes[i], TypeUnification
                        .makeVariant(getSubterm(i).getType()));
            }
            unify.leftUnify(function.getResultType(), TypeUnification
                    .makeVariant(getType()));
        } catch (UnificationException e) {
            throw new TermException("Term " + toString()
                    + " cannot be typed.\nFunction symbol: " + function
                    + "\nTypes of subterms:\n" + Util.listTypes(getSubterms()), e);
        }

    }

    @Override
    public String toString(boolean typed) {
        String retval = function.getName();
        if (countSubterms() > 0) {
            retval += "(";
            for (int i = 0; i < countSubterms(); i++) {
                retval += getSubterm(i).toString(typed);
                if (i != countSubterms() - 1)
                    retval += ",";
            }
            retval += ")";
        }
        if (typed)
            retval += " as " + getType();
        return retval;
    }
    
    @Override
    public void visit(TermVisitor visitor) throws TermException {
        visitor.visit(this);
    }
    
    /*
     * This term is equal to another term if it is a Application
     * and has the same function symbol and same arguments.
     */
    @Override 
    public boolean equals(@NonNull Object object) {
        if (object instanceof Application) {
            Application app = (Application) object;
            if(app.getFunction() != getFunction())
                return false;
            
            if(!app.getType().equals(getType()))
                return false;
            
            assert app.countSubterms() == countSubterms();
            
            for (int i = 0; i < countSubterms(); i++) {
                if(!app.getSubterm(i).equals(getSubterm(i)))
                    return false;
            }
            
            return true;
        }
        return false;
    }
}
