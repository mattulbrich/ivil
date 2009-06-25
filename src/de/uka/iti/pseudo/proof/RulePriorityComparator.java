/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.proof;

import java.util.Comparator;

import de.uka.iti.pseudo.rule.Rule;

/**
 * The Class RulePriorityComparator allows to sort rules by their priority
 * attribute.
 * 
 * The attributes are NOT converted to numbers but are compared as strings.
 * Therefore: Priority has to be a number between 100 and 999.
 */
public class RulePriorityComparator implements Comparator<Rule> {
    
    /**
     * The property that can be set in rule declarations
     */
    public static final String PRIORITY_PROPERTY = "prio";

    /* 
     * compare two Rule objects.
     * returns < 0 if r1.prio < r2.prio or only r2 is null
     * returns == 0 if r1.prio == r2.prio or both null
     * returns > 0 if r1.prio > r2.prio or only r1 is null
     */
    public int compare(Rule r1, Rule r2) {
        String s1 = r1.getProperty(PRIORITY_PROPERTY);
        String s2 = r2.getProperty(PRIORITY_PROPERTY);
        if(s1 == null) {
            return s2 == null ? 0 : 1;
        } else {
            return s2 == null ? -1 : s1.compareTo(s2);
        }
    }

}
