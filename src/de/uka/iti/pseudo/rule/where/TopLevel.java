package de.uka.iti.pseudo.rule.where;

import com.sun.org.apache.xpath.internal.FoundIndex;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.WhereCondition;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.proof.TermSelector;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.UpdateTerm;
import de.uka.iti.pseudo.term.creation.DefaultTermVisitor;
import de.uka.iti.pseudo.term.statement.AssignmentStatement;

// TODO Documentation needed
public class TopLevel extends WhereCondition {

    public static class ToplevelVisitor extends DefaultTermVisitor {
        
        private int counter = 0;
        private int selected;
        private boolean found = false;
        
        public ToplevelVisitor(int selected) {
            this.selected = selected;
        }

        protected void defaultVisitTerm(Term term)
                throws TermException {
            if(counter == selected)
                found = true;
            else {
                for (Term t : term.getSubterms()) {
                    t.visit(this);
                    if(found)
                        break;
                }
            }
            counter ++;
        }
        
        public void visit(UpdateTerm updateTerm) throws TermException {
            
            for (AssignmentStatement ass : updateTerm.getAssignments()) {
                ass.getValue().visit(this);
            }
            
            if(found)
                return;
            
            updateTerm.getSubterm(0).visit(this);
            if(found)
                throw new TermException("Selected term is subterm of an update term");
        }
        
    }
    
    public TopLevel() {
        super("toplevel");
    }

    @Override 
    public boolean check(Term[] formalArguments,
            Term[] actualArguments, RuleApplication ruleApp, ProofNode goal,
            Environment env) throws RuleException {
        try {
            
            return check(ruleApp.getFindSelector(), goal.getSequent());
            
        } catch (ProofException e) {
            throw new RuleException(e);
        }
        
    }
    
    /* intermediate step to allow testing */
    boolean check(TermSelector select, Sequent sequent) throws ProofException {
        
        Term topTerm = select.selectTopterm(sequent);
        
        try {
            topTerm.visit(new ToplevelVisitor(select.getSubtermNo()));
            return true;
        } catch (TermException e) {
            return false;
        }
    }
    
    @Override 
    public void checkSyntax(Term[] arguments) throws RuleException {
        if(arguments.length > 0)
            throw new RuleException("toplevel expects no arguments");
    }
    
}
