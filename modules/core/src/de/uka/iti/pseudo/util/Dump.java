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
package de.uka.iti.pseudo.util;

import de.uka.iti.pseudo.proof.RuleApplication;

/**
 * A collection of static methods that can be used to dump internal information
 * for debug purposes.
 * 
 * Output to be used for regular reasons should be implemented closer to the
 * information.
 */
public class Dump {

    public static void dumpRuleApplication(RuleApplication ruleApp) {
        System.err.println(toString(ruleApp));
    }
    
    public static String toString(RuleApplication ruleApp) {
        StringBuilder sb = new StringBuilder();
        sb.append("Rule application : " + ruleApp);
        sb.append("\n Rule: " + ruleApp.getRule().getName());
        sb.append("\n Node number: " + ruleApp.getProofNode());
        sb.append("\n Find: " + ruleApp.getFindSelector());
        sb.append("\n Ass.: " + ruleApp.getAssumeSelectors());
        sb.append("\n Schema vars   : " + ruleApp.getSchemaVariableMapping());
        sb.append("\n Type variables: " + ruleApp.getTypeVariableMapping());
        sb.append("\n Schema updates: " + ruleApp.getSchemaUpdateMapping());
        sb.append("\n Properties: " + ruleApp.getProperties());
        return sb.toString();
    }
    
}
