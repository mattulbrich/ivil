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

import nonnull.NonNull;
import de.uka.iti.pseudo.auto.strategy.RuleMatchTree;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.Rule;

/**
 * A collection of static methods that can be used to dump internal information
 * for debug purposes.
 *
 * Output to be used for regular reasons should be implemented closer to the
 * information.
 */
public class Dump {

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

    public static void dumpRuleMatchTree(RuleMatchTree tree) {
        System.err.println(toString(tree, 0));
    }

    private static String toString(RuleMatchTree tree, int indent) {
        String in = Util.duplicate(" ", indent);
        StringBuilder sb = new StringBuilder();
        sb.append(in + "* " + tree.getTerm() + "\n");
        sb.append(in + tree.getMatchingRules() + "\n");
        for (RuleMatchTree son : tree.getBranches()) {
            sb.append(toString(son, indent+1));
        }
        return sb.toString();
    }

    public static String toString(RuleMatchTree tree) {
       return toString(tree, 0);
    }

}
