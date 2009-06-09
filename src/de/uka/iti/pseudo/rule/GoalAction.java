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


// TODO DOC
/**
 * The Class GoalAction encapsulates a list of action to take on a goal. Such an
 * action can start a new goal, copy the current goal or close the current goal.
 */
public class GoalAction {

    /**
     * The enumeration of kinds of actions.
     */
    public enum Kind {
        CLOSE, COPY, NEW
    }

    private @NonNull Kind kind;
    
    private @Nullable String name;
    
    private Term replaceWith;

    private Term[] addAntecedent;

    private Term[] addSuccedent;

    public Kind getKind() {
        return kind;
    }
    
    public Term getReplaceWith() {
        return replaceWith;
    }

    public List<Term> getAddAntecedent() {
        return Util.readOnlyArrayList(addAntecedent);
    }

    public List<Term> getAddSuccedent() {
        return Util.readOnlyArrayList(addSuccedent);
    }

    public GoalAction(String kindString, String name, Term replaceWith,
            List<Term> addAntecendent, List<Term> addSuccendent) throws RuleException {
        
        if (kindString.equals("closegoal")) {
            this.kind = Kind.CLOSE;
        } else if (kindString.equals("newgoal")) {
            this.kind = Kind.NEW;
        } else if (kindString.equals("samegoal")) {
            this.kind = Kind.COPY;
        } else
            throw new IllegalArgumentException();

        // CLOSE implies empty
        if(this.kind == Kind.CLOSE && (!addAntecendent.isEmpty() || !addSuccendent.isEmpty() || replaceWith != null))
            throw new RuleException("closeGoal actions may not contain add/replace elements");
        
        // no replace in NEW
        if(this.kind == Kind.NEW && replaceWith != null)
            throw new RuleException("newgoal actions may not contain replace elements");
        
        this.name = name;
        this.replaceWith = replaceWith;
        this.addAntecedent = Util.listToArray(addAntecendent, Term.class);
        this.addSuccedent = Util.listToArray(addSuccendent, Term.class);
    }

    public void dump() {
        System.out.println("      action " + kind + (name == null ? "" : "\""+name+"\""));
        
        if(replaceWith != null)
            System.out.println("        replace " + replaceWith);

        for (Term t : addAntecedent) {
            System.out.println("        add " + t + " |-");
        }
        
        for (Term t : addSuccedent) {
            System.out.println("        add |- " +t);
        }
    }

    public String getName() {
        return name;
    }

}
