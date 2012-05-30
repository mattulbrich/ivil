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

import static de.uka.iti.pseudo.term.TypeVariable.ALPHA;
import static de.uka.iti.pseudo.term.TypeVariable.BETA;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.MetaFunction;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Variable;
import de.uka.iti.pseudo.term.creation.RebuildingTermVisitor;

// TODO Documentation needed
public class SubstMetaFunction extends MetaFunction {

    public SubstMetaFunction() throws EnvironmentException {
        // toReplace, replaceWith, replaceIn
        super(ALPHA, "$$subst", BETA, BETA, ALPHA);
    }

    @Override
    public Term evaluate(Application application, Environment env, RuleApplication ruleApp) throws TermException {

        Term toReplace = application.getSubterm(0);
        Term replaceWith = application.getSubterm(1);
        Term replaceIn = application.getSubterm(2);
        return evaluate(toReplace, replaceWith, replaceIn, env);

    }

    public Term evaluate(Term toReplace, Term replaceWith, Term replaceIn, Environment env) throws TermException {

        if (!(toReplace instanceof Variable)) {
            throw new TermException("only variables can be replaced");
        }

        TermReplacer tr = new TermReplacer(env);
        return tr.replace(toReplace, replaceWith, replaceIn);
    }

    static class TermReplacer extends RebuildingTermVisitor  {

        private Term termToReplace;
        private Term replaceWith;
        private final Environment env;

        public TermReplacer(Environment env) {
            this.env = env;
        }

        @Override
        protected void defaultVisitTerm(Term term) throws TermException {
            if (term.equals(termToReplace)) {
                resultingTerm = replaceWith;
            } else {
                resultingTerm = null;
            }
        }

        /*
         * In case that the variable to be replaced is quantified again, the
         * second binding is to be ignored. For instance: (\forall x; x > 0 &
         * (\forall x; x < 0))
         *
         * Here, instantiation is to not touch the second quantification.
         */
        @Override
        public void visit(Binding binding) throws TermException {
            if (binding.getVariable().equals(termToReplace)) {
                resultingTerm = null;
            } else {
                super.visit(binding);
            }
        }

        // a program can no longer contain free variables:
        // no special treatment needed!
        // @Override
        // public void visit(LiteralProgramTerm node) throws TermException {

        public Term replace(Term termToReplace, Term replaceWith, Term replaceIn) throws TermException {
            this.termToReplace = termToReplace;
            this.replaceWith = replaceWith;
            replaceIn.visit(this);
            return resultingTerm == null ? replaceIn : resultingTerm;
        }

//        @Override
//        public ReplacingCloneableTermVisitor copy() {
//            try {
//                return (ReplacingCloneableTermVisitor) clone();
//            } catch (CloneNotSupportedException e) {
//                e.printStackTrace();
//                return null;
//            }
//        }

    }

}
