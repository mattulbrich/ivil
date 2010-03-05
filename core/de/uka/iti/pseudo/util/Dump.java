/*
 * This file is part of PSEUDO
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
        System.err.println("Rule application : " + ruleApp);
        System.err.println(" Rule: " + ruleApp.getRule().getName());
        System.err.println(" Goal: " + ruleApp.getGoalNumber());
        System.err.println(" Find: " + ruleApp.getFindSelector());
        System.err.println(" Ass.: " + ruleApp.getAssumeSelectors());
        System.err.println(" Schema vars   :" + ruleApp.getSchemaVariableMapping());
        System.err.println(" Type variables:" + ruleApp.getTypeVariableMapping());
        System.err.println(" Schema updates:" + ruleApp.getSchemaUpdateMapping());
        System.err.println(" Properties: " + ruleApp.getProperties());
    }
    
}
