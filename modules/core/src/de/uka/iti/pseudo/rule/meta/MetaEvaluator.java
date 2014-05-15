/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.rule.meta;

import nonnull.NonNull;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.environment.LocalSymbolTable;
import de.uka.iti.pseudo.environment.MetaFunction;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.TermVisitor;
import de.uka.iti.pseudo.term.creation.RebuildingTermVisitor;


/**
 * The Class MetaEvaluator is a {@link TermVisitor} which is used to 
 * replace applications of {@link MetaFunction}s by their evaluations.
 * 
 * @see MetaFunction
 */
public class MetaEvaluator extends RebuildingTermVisitor {

    private final RuleApplication ruleApplication;
    private final Environment env;
    private LocalSymbolTable localSymbolTable;

    /**
     * Instantiates a new meta evaluator.
     * 
     * @param ruleApp
     *            the RuleApplication which will passed to the meta functions
     *            during the evaluation.
     * 
     * @param env
     *            the environment which will be passed to the meta functions
     *            during the evaluation.
     */
    public MetaEvaluator(RuleApplication ruleApp, Environment env) {
        this.ruleApplication = ruleApp;
        this.env = env;
        resetLocalSymbols();
    }

    /**
     * Replace all applications of meta functions by their 
     * substitution.
     * 
     * The substitution is obtained by calling the method
     * {@link MetaFunction#evaluate(de.uka.iti.pseudo.term.Application, de.uka.iti.pseudo.environment.Environment, RuleApplication)
     * 
     * @param term the term to replace meta function applications in
     * 
     * @return the argument term with replaced meta functions
     * @throws TermException 
     */
    public @NonNull Term evalutate(@NonNull Term term) throws TermException {
        term.visit(this);
        if(resultingTerm == null)
            return term;
        else
            return resultingTerm;
    }

    @Override 
    public void visit(Application application) throws TermException {
        super.visit(application);

        // take subterm replacement into consideration
        // but we know it always is of type Application
        if(resultingTerm != null)
           application = (Application) resultingTerm;

        Function function = application.getFunction();
        if (function instanceof MetaFunction) {
            MetaFunction metaFunct = (MetaFunction) function;
            resultingTerm = metaFunct.evaluate(application, this);
        }
    }

    public Environment getEnvironment() {
        return env;
    }

    public RuleApplication getRuleApplication() {
        return ruleApplication;
    }

    public LocalSymbolTable getLocalSymbolTable() {
        return localSymbolTable;
    }

    public void resetLocalSymbols() {
        localSymbolTable =
                new LocalSymbolTable(ruleApplication.getProofNode().getLocalSymbolTable());
    }

}
