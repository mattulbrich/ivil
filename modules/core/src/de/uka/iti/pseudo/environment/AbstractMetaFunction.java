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

import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.meta.MetaEvaluator;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;

/**
 * An AbstractMetaFunction provides an evaluation function with a different
 * signature. That provides less information. But originally, that was the
 * signature of evaluate and has only been changed later.
 *
 * In this respect, this class provides backward compatibility.
 */
public abstract class AbstractMetaFunction extends MetaFunction {

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
     *
     * @throws EnvironmentException
     *            if the symbol cannot be created
     */
    public AbstractMetaFunction(Type resultType, String name, Type... argumentTypes)
            throws EnvironmentException {
        super(resultType, name, argumentTypes);
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
     *             if the arguments do not fulfill all requirements.
     */
    public abstract Term evaluate(Application application, Environment env,
            RuleApplication ruleApp) throws TermException;

    /** {@inheritDoc}
     *
     * This class extracts values from the {@link MetaEvaluator} and calls the
     * method {@link #evaluate(Application, Environment, RuleApplication)} in
     * the style of old interface.
     */
    @Override
    public Term evaluate(Application application, MetaEvaluator metaEval) throws TermException {
        return evaluate(application, metaEval.getEnvironment(), metaEval.getRuleApplication());
    }

}
