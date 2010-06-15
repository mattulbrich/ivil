/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.environment;

import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.meta.MetaEvaluator;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;

/**
 * A meta-function describes a function symbol which is NOT part of the
 * signature but can be used to denote other terms which are constructed from
 * the arguments to the meta-function.
 * 
 * An implementation needs to provide a method
 * {@link #evaluate(Application, Environment, RuleApplication)} which returns
 * for the application of a meta-function a term constructed from the arguments
 * of the application.
 * 
 * @see MetaEvaluator
 */
public abstract class MetaFunction extends Function {
    
    /**
     * The plugin service under which meta-functions have to be registered.
     */
    public static final String SERVICE_NAME = "metaFunction";

    /**
     * Instantiates a new meta function.
     * 
     * The name of the function symbol MUST begin with "$$".
     * 
     * @param resultType
     *            the result type of the function symbol
     * @param name
     *            the name of the function symbol, starting with "$$"
     *            
     * @param argumentTypes
     *            the types of the arguments of the function symbol
     */
    public MetaFunction(Type resultType, String name, Type... argumentTypes) {
        super(name, resultType, argumentTypes, false, false, ASTLocatedElement.BUILTIN);
        
        assert name.startsWith("$$");
    }

    /**
     * Evaluate an application term for this meta-function resulting in another
     * term of the same type.
     * 
     * The environment and the rule application may be used to retrieve data
     * from or to store data into.
     * 
     * @param application
     *            an application with this meta function as top level symbol
     * @param env
     *            the environment
     * @param ruleApp
     *            the rule application under which this evaluation takes place
     * 
     * @return a term of the same type as the first argument.
     * 
     * @throws TermException
     *             if the arguments do not fulfil all requirements.
     */
    public abstract Term evaluate(Application application, Environment env, RuleApplication ruleApp)
       throws TermException;

}
