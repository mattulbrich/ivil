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
package de.uka.iti.pseudo.rule.meta;

import static de.uka.iti.pseudo.term.TypeVariable.ALPHA;
import static de.uka.iti.pseudo.term.TypeVariable.BETA;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.MetaFunction;
import de.uka.iti.pseudo.environment.Program;
import de.uka.iti.pseudo.environment.creation.ProgramChanger;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
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

    static class TermReplacer extends RebuildingTermVisitor {

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
        public void visit(Binding binding) throws TermException {
            if (binding.getVariable().equals(termToReplace))
                resultingTerm = null;
            else
                super.visit(binding);
        }

        // this might cause a problem with recursion, if a Program calls
        // itself with a LiteralProgramTerm; maybe this can be fixed by two
        // lists, one with the programs that are currently modified and one with
        // the terms that need change, but cannot be changed, as their programs
        // are currently under modification (Timm Felden)
        
        // Programs cannot recurse because they have to be registered one after
        // the other, only programs already registered can be referenced from a
        // program. [unless malicously constructed otherwise] (M.U.)
        @Override
        public void visit(LiteralProgramTerm node) throws TermException {
            ProgramChanger changer = new ProgramChanger(node.getProgram(), env);

            try {
                changer.replaceAll(termToReplace, replaceWith);

                Program p;
                env.addProgram(p = changer.makeProgram(env.createNewProgramName(node.getProgram().getName())));

                resultingTerm = new LiteralProgramTerm(node.getProgramIndex(), node.isTerminating(), p);

            } catch (EnvironmentException e) {
                e.printStackTrace();
                throw new TermException(e);
            }
        }

        public Term replace(Term termToReplace, Term replaceWith, Term replaceIn) throws TermException {
            this.termToReplace = termToReplace;
            this.replaceWith = replaceWith;
            replaceIn.visit(this);
            return resultingTerm == null ? replaceIn : resultingTerm;
        }

    }

}
