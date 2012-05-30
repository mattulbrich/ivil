/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.proof;

import nonnull.NonNull;
import de.uka.iti.pseudo.rule.RuleException;

/**
 * Filter rule applications.
 * 
 * A RuleApplicationFilter allows to decide upon single rule applications
 * whether they are accepted or not.
 * 
 */
public interface RuleApplicationFilter {

    /**
     * decide whether the ruleApplication is to be accepted or not.
     * 
     * @param ruleApp rule application to check 
     * @return true if the application has been approved.
     */
    boolean accepts(@NonNull RuleApplication ruleApp) throws RuleException;

}
