/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.rule;

import java.util.List;

import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.util.Util;


/**
 * The Class GoalAction encapsulates a list of action to take on a goal. Such an
 * action can start a new goal, copy the current goal or close the current goal.
 * 
 * Goal actions are immutable.
 * 
 * An action may contain a replace item and several add items for the antecedent
 * and for the succedent.
 * 
 * A value of null for {@link #replaceWith} implies that the found term is not
 * replaced but left untouched. If the originally found term is to be deleted
 * set the {@link #removeOriginalTerm} flag.
 */
public class GoalAction {

    /**
     * The enumeration of kinds of actions. Their string representations are
     * "closegoal", "samegoal", "newgoal".
     */
    public enum Kind {
        CLOSE, 
        COPY, 
        NEW
    }

    /**
     * The kind of this action.
     */
    private @NonNull Kind kind;
    
    /**
     * The name of the action. It may be null if no name has been provided
     */
    private @Nullable String name;
    
    /**
     * The term to replace the found term with.
     */
    private Term replaceWith;

    /**
     * The terms to be added to the antecedent.
     */
    private Term[] addAntecedent;

    /**
     * The terms to be added to the succedent.
     */
    private Term[] addSuccedent;

    /**
     * The flag whether or not the found term is to be deleted.
     */
    private boolean removeOriginalTerm;

    /**
     * Gets the kind of this action
     * 
     * @return the kind
     */
    public @NonNull Kind getKind() {
        return kind;
    }
    
    /**
     * Gets the term with which the found term is to be replaced.
     * 
     * @return the replace with
     */
    public @Nullable Term getReplaceWith() {
        return replaceWith;
    }

    /**
     * Gets a readonly list of the terms to be added on the antecedent side.
     * 
     * @return the list of terms to add to the antecedent
     */
    public List<Term> getAddAntecedent() {
        return Util.readOnlyArrayList(addAntecedent);
    }


    /**
     * Gets a readonly list of the terms to be added on the succedent side.
     * 
     * @return the list of terms to add to the succdent
     */
    public List<Term> getAddSuccedent() {
        return Util.readOnlyArrayList(addSuccedent);
    }

    /**
     * Instantiates a new goal action.
     * 
     * <p>The kind is encoded in a string which must be either "closegoal",
     * "newgoal", or "samegoal".
     * 
     * @param kindString the kind of the token
     * @param name the name of the the action (optional) 
     * @param replaceWith the replacement for the original term, null if the original term is to be removed
     * @param addAntecendent the terms to be added to antecendent
     * @param addSuccendent the terms to be added to succendent
     * 
     * @throws RuleException the rule exception
     */
    public GoalAction(@NonNull String kindString, @Nullable String name, 
            boolean remove, @Nullable Term replaceWith, List<Term> addAntecendent, 
            List<Term> addSuccendent) throws RuleException {
        
        if (kindString.equals("closegoal")) {
            this.kind = Kind.CLOSE;
        } else if (kindString.equals("newgoal")) {
            this.kind = Kind.NEW;
        } else if (kindString.equals("samegoal")) {
            this.kind = Kind.COPY;
        } else
            throw new IllegalArgumentException(kindString);

        // CLOSE implies empty
        if(this.kind == Kind.CLOSE && (!addAntecendent.isEmpty() || !addSuccendent.isEmpty() || replaceWith != null))
            throw new RuleException("closeGoal actions must not contain add/replace elements");
        
        // no replace in NEW
        if(this.kind == Kind.NEW && replaceWith != null)
            throw new RuleException("newgoal actions must not contain replace elements");
        
        // remove only in COPY and not with BOTH
        if(this.kind != Kind.COPY && remove)
            throw new RuleException("remove must only used in samegoal actions");
        
        if(remove && replaceWith != null)
            throw new RuleException("a goal must not have both remove and replace");
        
        this.name = name;
        this.replaceWith = replaceWith;
        this.removeOriginalTerm = remove;
        this.addAntecedent = Util.listToArray(addAntecendent, Term.class);
        this.addSuccedent = Util.listToArray(addSuccendent, Term.class);
    }

    /**
     * Dump the goal action to stdout.
     */
    public void dump() {
        System.out.println("      action " + kind + (name == null ? "" : " \""+name+"\""));
        
        if(replaceWith != null)
            System.out.println("        replace " + replaceWith);

        for (Term t : addAntecedent) {
            System.out.println("        add " + t + " |-");
        }
        
        for (Term t : addSuccedent) {
            System.out.println("        add |- " +t);
        }
    }

    /**
     * Gets the name of this goal action. If the name is not specified,
     * null is returned
     * 
     * @return the name of the action, possibly null
     */
    public @Nullable String getName() {
        return name;
    }

    /**
     * Checks if this goal action is to remove the original term from the sequent.
     * This can only be the case in a samegoal action.
     * 
     * @return true, if this is to remove the original term
     */
    public boolean isRemoveOriginalTerm() {
        return removeOriginalTerm;
    }
}
