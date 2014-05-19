/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.util;

import java.util.List;
import java.util.Stack;

import nonnull.NonNull;
import de.uka.iti.pseudo.auto.script.ProofScript;
import de.uka.iti.pseudo.auto.script.ProofScriptNode;
import de.uka.iti.pseudo.environment.Axiom;
import de.uka.iti.pseudo.environment.Binder;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.environment.Program;
import de.uka.iti.pseudo.environment.Sort;
import de.uka.iti.pseudo.environment.SymbolTable;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.GoalAction;
import de.uka.iti.pseudo.rule.LocatedTerm;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.rule.WhereClause;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.statement.Statement;

/**
 * A collection of static methods that can be used to dump internal information
 * for debug purposes.
 *
 * Output to be used for regular reasons should be implemented closer to the
 * information.
 */
public final class Dump {

    private Dump() {
        throw new Error("Must not be instantiated");
    }

    /**
     * Debug dump a {@link Environment} to standard out.
     *
     * @param env
     *            the environment to describe.
     */
    public static void dumpEnv(Environment env) {

        System.out.println("Environment '" + env.getResourceName() + "':");
        try {
            Environment parent = env.getParent();
            if (parent != null) {
                System.out.print("extending " + parent.getResourceName());
            }
        } catch (EnvironmentException e) {
            Log.stacktrace(e);
            System.out.println(" ... cannot read parent environment");
        }


        System.out.println("Sorts:");
        for (Sort sort : env.getLocalSorts()) {
            System.out.println("  " + sort);
        }

        System.out.println("Functions:");
        for (Function function : env.getAllFunctions()) {
            System.out.println("  " + function);
        }

//        System.out.println("Infix Functions:");
//        for (String name : env.getAinfixMap.keySet()) {
//            System.out.println("  " + infixMap.get(name));
//        }
//
//        System.out.println("Prefix Functions:");
//        for (String name : prefixMap.keySet()) {
//            System.out.println("  " + prefixMap.get(name));
//        }

        System.out.println("Binders:");
        for (Binder binder : env.getLocalBinders()) {
            System.out.println("  " + binder);
        }

        System.out.println("Rules:");
        for (Rule rule : env.getLocalRules()) {
            Dump.dumpRule(rule);
        }

        System.out.println("Axioms:");
        for (Axiom axiom : env.getLocalAxioms()) {
            Dump.dumpAxiom(axiom);
        }

        System.out.println("Programs:");
        for (Program program : env.getLocalPrograms()) {
            System.out.println("  program " + program.getName());
            dumpProgram(program);
        }

    }

    /**
     * Dump a local symbol table to standard error.
     *
     * @param lst
     *            the local symbol table
     */
    public static void dumpSymbolTable(@NonNull SymbolTable lst) {
        System.err.println(toString(lst));
    }

    /**
     * Dump a local symbol table to a string.
     *
     * @param lst
     *            the local symbol table
     *
     * @return the string for the table
     */
    public static String toString(SymbolTable lst) {

        StringBuilder b = new StringBuilder();
        b.append("LocalSymbolTable\n");

        System.out.println(" Sorts:");
        for (Sort sort : lst.getSorts()) {
            b.append("  ").append(sort).append("\n");
        }

        System.out.println(" Functions:");
        for (Function function : lst.getFunctions()) {
            b.append("  ").append(function).append("\n");
        }

        System.out.println("Binders:");
        for (Binder binder : lst.getBinders()) {
            b.append("  ").append(binder).append("\n");
        }

        System.out.println("Programs:");
        for (Program prog : lst.getPrograms()) {
            b.append("  ").append(prog).append("\n");
        }

        return b.toString();
    }

    /**
     * Dump a rule application to standard error.
     *
     * @param ruleApp
     *            the rule application
     */
    public static void dumpRuleApplication(@NonNull RuleApplication ruleApp) {
        System.err.println(toString(ruleApp));
    }

    /**
     * Render a Rule application into a multiline string.
     *
     * @param ruleApp
     *            the rule app
     * @return the multiline string representation of the argument.
     */
    public static String toString(@NonNull RuleApplication ruleApp) {
        StringBuilder sb = new StringBuilder();
        sb.append("Rule application : " + ruleApp);
        Rule rule = ruleApp.getRule();
        sb.append("\n Rule: " + (rule == null ? "null" : rule.getName()));
        sb.append("\n Node number: " + ruleApp.getProofNode());
        sb.append("\n Find: " + ruleApp.getFindSelector());
        sb.append("\n Ass.: " + ruleApp.getAssumeSelectors());
        sb.append("\n Schema vars   : " + ruleApp.getSchemaVariableMapping());
        sb.append("\n Type variables: " + ruleApp.getTypeVariableMapping());
        sb.append("\n Schema updates: " + ruleApp.getSchemaUpdateMapping());
        sb.append("\n Properties: " + ruleApp.getProperties());
        return sb.toString();
    }

    /**
     * Dump this axiom to standard error. Used for debugging purposes.
     *
     * @param axiom
     *            axiom to print out
     */
    public static void dumpAxiom(Axiom axiom) {
        System.err.println("  Axiom " + axiom.getName());
        System.err.println("        " + axiom.getTerm());
    }

    /**
     * Dump the rule to Stdout.
     *
     * @param rule
     *            rule to dump to output.
     */
    public static void dumpRule(Rule rule) {

        System.out.println("  Rule " + rule.getName());

        LocatedTerm findClause = rule.getFindClause();
        if(findClause != null) {
            System.out.print("    Find: ");
            System.out.println(findClause);
        }

        System.out.println("    Assumptions:");
        for (LocatedTerm lt : rule.getAssumptions()) {
            System.out.println("      " + lt);
        }

        System.out.println("    Where clauses:");
        for (WhereClause wc : rule.getWhereClauses()) {
            System.out.println("      " + wc);
        }

        System.out.println("    Actions:");
        for (GoalAction ga : rule.getGoalActions()) {
            dumpGoalAction(ga);
        }
    }

    /**
     * Dump the goal action to stdout.
     *
     * @param goalAction
     *            the goal action do print out.
     */
    public static void dumpGoalAction(GoalAction goalAction) {
        String name = goalAction.getName();
        System.out.println("      action " + goalAction.getKind()
                + (name == null ? "" : " \""+name+"\""));

        Term replaceWith = goalAction.getReplaceWith();
        if(replaceWith != null) {
            System.out.println("        replace " + replaceWith);
        }

        for (Term t : goalAction.getAddAntecedent()) {
            System.out.println("        add " + t + " |-");
        }

        for (Term t : goalAction.getAddSuccedent()) {
            System.out.println("        add |- " +t);
        }
    }


    /**
     * Dump program to stdout. For debug purposes.
     *
     * @param program
     *            program to print out to output
     */
    public static void dumpProgram(Program program) {
        System.out.println("    Statements");
        List<Statement> statements = program.getStatements();
        List<String> statementAnnotations = program.getTextAnnotations();
        for (int i = 0; i < statements.size(); i++) {
            System.out.print("      " + i + ": " + statements.get(i));
            String annot = statementAnnotations.get(i);
            if(annot != null) {
                System.out.print("; \"" + annot + "\"");
            }
            System.out.println();
        }
    }

    /**
     * Dump a proof script to stdout. For debug purposes.
     *
     * @param ps
     *            proof script to dump
     */
    public static void dumpProofScript(ProofScript ps) {
        System.out.println("proof " + ps.getObligation());
        dumpProofScriptNode(ps.getRoot(), 0);
    }

    private static void dumpProofScriptNode(ProofScriptNode node, int indent) {
        System.out.print(Util.duplicate(" ", indent) + "(" + node.getCommand().getName() +
                " " + node.getArguments() );
        List<ProofScriptNode> children = node.getChildren();
        while(children.size() == 1) {
            node = children.get(0);
            System.out.println(";");
            System.out.print(Util.duplicate(" ", indent + 1) + node.getCommand().getName() +
                    " " + node.getArguments() );
            children = node.getChildren();
        }
        System.out.println();
        for (ProofScriptNode child : children) {
            dumpProofScriptNode(child, indent + 1);
        }
        System.out.println(Util.duplicate(" ", indent) + ")");
    }


}
