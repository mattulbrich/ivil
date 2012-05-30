/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
//package de.uka.iti.pseudo.rule.meta;
//
//import de.uka.iti.pseudo.environment.Environment;
//import de.uka.iti.pseudo.environment.EnvironmentException;
//import de.uka.iti.pseudo.environment.MetaFunction;
//import de.uka.iti.pseudo.proof.RuleApplication;
//import de.uka.iti.pseudo.term.Application;
//import de.uka.iti.pseudo.term.LiteralProgramTerm;
//import de.uka.iti.pseudo.term.Term;
//import de.uka.iti.pseudo.term.TermException;
//import de.uka.iti.pseudo.term.TypeVariable;
//import de.uka.iti.pseudo.term.UpdateTerm;
//import de.uka.iti.pseudo.term.statement.UpdateStatement;
//
//public class ApplyUpdateStatement extends MetaFunction {
//
//    public ApplyUpdateStatement() throws EnvironmentException {
//        super(TypeVariable.ALPHA, "$$applyUpdateStatement", TypeVariable.ALPHA);
//    }
//
//    @Override
//    public Term evaluate(Application application, Environment env, RuleApplication ruleApp) throws TermException {
//
//        Term arg = application.getSubterm(0);
//
//        if (arg instanceof LiteralProgramTerm) {
//            LiteralProgramTerm progTerm = (LiteralProgramTerm) arg;
//            if (progTerm.getStatement() instanceof UpdateStatement) {
//                UpdateStatement up = (UpdateStatement) progTerm.getStatement();
//
//                return new UpdateTerm(up.getUpdate(), new LiteralProgramTerm(progTerm.getProgramIndex() + 1, progTerm));
//            }
//        }
//
//        throw new TermException("The argument needs to be a LiteralProgramTerm that points to an UpdateStatement "
//                + arg);
//    }
//
//}
