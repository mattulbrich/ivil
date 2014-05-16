/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.rule.where;

import java.util.List;
import java.util.Map;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.WhereCondition;
import de.uka.iti.pseudo.proof.FilterRuleApplication;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.proof.TermSelector;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.rule.meta.MetaEvaluator;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.Update;

// TODO Documentation needed
public class CanEvaluateMeta extends WhereCondition {

    public CanEvaluateMeta() {
        super("canEval");
    }

    @Override
    public boolean check(Term[] formalArguments,
            Term[] actualArguments, RuleApplication ruleApp,
            Environment env) throws RuleException {

        MetaEvaluator metaEval = new MetaEvaluator(ruleApp, env);

        try {
            actualArguments[0].visit(metaEval);
            return true;
        } catch (TermException e) {
            // I cannot apply the meta evaluator --> say no
            return false;
        }
    }

    @Override public void checkSyntax(Term[] arguments) throws RuleException {
        if(arguments.length != 1) {
            throw new RuleException("canEval expects exactly one argument");
        }
    }

//    /**
//     * This class wraps a rule application and makes its properties immutable.
//     */
//    private static class ProtectedRuleApplication extends FilterRuleApplication {
//
//        public ProtectedRuleApplication(RuleApplication app) {
//            super(app);
//        }
//
//        @Override
//        public boolean hasMutableProperties() {
//            return false;
//        }
//    }
}
