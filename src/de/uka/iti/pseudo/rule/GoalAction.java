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


// TODO: Auto-generated Javadoc
// TODO DOC WIP
/**
 * The Class GoalAction encapsulates a list of action to take on a goal. Such an
 * action can start a new goal, copy the current goal or close the current goal.
 */
public class GoalAction {

    /**
     * The enumeration of kinds of actions.
     */
    public enum Kind {
        CLOSE, 
        COPY, 
        NEW
    }

    /**
     * The kind of this action
     */
    private @NonNull Kind kind;
    
    /**
     * The name of the action. It may be null if no name has been provided
     */
    private @Nullable String name;
    
    /**
     * The replace with.
     */
    private Term replaceWith;

    /**
     * The add antecedent.
     */
    private Term[] addAntecedent;

    /**
     * The add succedent.
     */
    private Term[] addSuccedent;

    /**
     * The remove original term.
     */
    private boolean removeOriginalTerm;

    /**
     * Gets the kind.
     * 
     * @return the kind
     */
    public Kind getKind() {
        return kind;
    }
    
    /**
     * Gets the replace with.
     * 
     * @return the replace with
     */
    public Term getReplaceWith() {
        return replaceWith;
    }

    /**
     * Gets the adds the antecedent.
     * 
     * @return the adds the antecedent
     */
    public List<Term> getAddAntecedent() {
        return Util.readOnlyArrayList(addAntecedent);
    }

    /**
     * Gets the adds the succedent.
     * 
     * @return the adds the succedent
     */
    public List<Term> getAddSuccedent() {
        return Util.readOnlyArrayList(addSuccedent);
    }

    /**
     * Instantiates a new goal action.
     * 
     * @param kindString the kind string
     * @param name the name
     * @param remove the remove
     * @param replaceWith the replace with
     * @param addAntecendent the add antecendent
     * @param addSuccendent the add succendent
     * 
     * @throws RuleException the rule exception
     */
    public GoalAction(String kindString, String name, boolean remove, Term replaceWith,
            List<Term> addAntecendent, List<Term> addSuccendent) throws RuleException {
        
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
            throw new RuleException("closeGoal actions may not contain add/replace elements");
        
        // no replace in NEW
        if(this.kind == Kind.NEW && replaceWith != null)
            throw new RuleException("newgoal actions may not contain replace elements");
        
        // remove only in COPY and not with BOTH
        if(this.kind != Kind.COPY && remove)
            throw new RuleException("remove may only used in samegoal actions");
        
        if(remove && replaceWith != null)
            throw new RuleException("a goal may not have both remove and replace");
        
        this.name = name;
        this.replaceWith = replaceWith;
        this.removeOriginalTerm = remove;
        this.addAntecedent = Util.listToArray(addAntecendent, Term.class);
        this.addSuccedent = Util.listToArray(addSuccendent, Term.class);
    }

    /**
     * Dump.
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
     * Gets the name.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Checks if is removes the original term.
     * 
     * @return true, if is removes the original term
     */
    public boolean isRemoveOriginalTerm() {
        return removeOriginalTerm;
    }

}
