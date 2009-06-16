//package de.uka.iti.pseudo.rule.where;
//
//import de.uka.iti.pseudo.environment.Environment;
//import de.uka.iti.pseudo.environment.WhereCondition;
//import de.uka.iti.pseudo.proof.ProofException;
//import de.uka.iti.pseudo.proof.ProofNode;
//import de.uka.iti.pseudo.proof.RuleApplication;
//import de.uka.iti.pseudo.proof.TermSelector;
//import de.uka.iti.pseudo.rule.RuleException;
//import de.uka.iti.pseudo.term.Sequent;
//import de.uka.iti.pseudo.term.Term;
//
//// TODO Documentation needed
//public class TopLevel extends WhereCondition {
//
//    public TopLevel() {
//        super("toplevel");
//    }
//
//    @Override 
//    public boolean check(Term[] formalArguments,
//            Term[] actualArguments, RuleApplication ruleApp, ProofNode goal,
//            Environment env) throws RuleException {
//        try {
//            
//            return check(ruleApp.getFindSelector(), goal.getSequent());
//            
//        } catch (ProofException e) {
//            throw new RuleException(e);
//        }
//        
//    }
//    
//    /* intermediate step to allow testing */
//    boolean check(TermSelector select, Sequent sequent) throws ProofException {
//        
//        Term topTerm = select.selectTopterm(sequent);
//        Term subTerm = select.selectSubterm(sequent);
//        
//        return lookup(subTerm, topTerm);
//    }
//    
//    /*
//     * look for a subterm within a top term.
//     * 
//     * It should return true if the subterm is identical (not only equal) with
//     * a subterm of topTerm (or topTerm itself) but not within a modality or
//     * behind a modality.
//     * 
//     * This method is package visible to allow testing
//     */
//    private boolean lookup(Term subTerm, Term topTerm) {
//        
//        // identity not equality
//        if(subTerm == topTerm)
//            return true;
//        
//        if (topTerm instanceof ModalityTerm) {
//            return false;
//        }
//        
//        for (Term child : topTerm.getSubterms()) {
//            if(lookup(subTerm, child))
//                return true;
//        }
//        
//        return false;
//    }
//
//    @Override 
//    public void checkSyntax(Term[] arguments) throws RuleException {
//        if(arguments.length > 0)
//            throw new RuleException("toplevel expects no arguments");
//    }
//    
//}
