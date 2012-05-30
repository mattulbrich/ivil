/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
//package de.uka.iti.pseudo.rule.where;
//
//import de.uka.iti.pseudo.environment.Environment;
//import de.uka.iti.pseudo.environment.WhereCondition;
//import de.uka.iti.pseudo.proof.ProofNode;
//import de.uka.iti.pseudo.proof.RuleApplication;
//import de.uka.iti.pseudo.rule.RuleException;
//import de.uka.iti.pseudo.term.LiteralProgramTerm;
//import de.uka.iti.pseudo.term.Term;
//import de.uka.iti.pseudo.term.statement.UpdateStatement;
//
//public class IsUpdateStatement extends WhereCondition {
//
//    public IsUpdateStatement() {
//        super("isUpdateStatement");
//    }
//
//    @Override
//    public boolean check(Term[] formalArguments, Term[] actualArguments, RuleApplication ruleApp, ProofNode goal,
//            Environment env) throws RuleException {
//
//        Term t = actualArguments[0];
//        if (t instanceof LiteralProgramTerm) {
//            LiteralProgramTerm p = (LiteralProgramTerm) t;
//            if (p.getStatement() instanceof UpdateStatement) {
//                return true;
//            }
//        }
//
//        return false;
//
//    }
//
//    @Override
//    public void checkSyntax(Term[] arguments) throws RuleException {
//        if (arguments.length != 1)
//            throw new RuleException("isUpdateStatment expects exactly 1 argument");
//    }
//}
