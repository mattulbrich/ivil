package de.uka.iti.pseudo.proof;

import nonnull.NonNull;

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
    boolean accepts(@NonNull RuleApplication ruleApp);

}
