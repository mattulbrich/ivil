package de.uka.iti.pseudo.rule;

import java.util.List;


public class GoalAction {
    
    enum Kind { CLOSE, COPY, NEW }

    private Kind kind;
    private GoalModification[] modifications;

    public GoalAction(String kind, List<GoalModification> mods) throws RuleException {
        
        if(kind.equals("closegoal")) {
            this.kind = Kind.CLOSE;
        } else if(kind.equals("newgoal")) {
            this.kind = Kind.NEW;
        } else if(kind.equals("copygoal")) {
            this.kind = Kind.COPY;
        } else
            throw new IllegalArgumentException();
        
        this.modifications = mods.toArray(new GoalModification[mods.size()]);
    }

    public void dump() {
        System.out.println("  action " + kind);
        
        for (GoalModification mod : modifications) {
            System.out.println("    " + mod);
        }
    }

}
