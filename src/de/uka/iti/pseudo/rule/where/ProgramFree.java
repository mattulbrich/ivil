package de.uka.iti.pseudo.rule.where;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.WhereCondition;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.term.ModalityTerm;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.TermVisitor;
import de.uka.iti.pseudo.term.creation.DefaultTermVisitor;

// TODO Documentation needed
public class ProgramFree extends WhereCondition {

    public ProgramFree() {
        super("programFree");
    }

    @Override 
    public boolean check(Term[] formalArguments,
            Term[] actualArguments, RuleApplication ruleApp, ProofNode goal,
            Environment env) throws RuleException {
        
        return checkProgramFree(actualArguments[0]);

    }

    @Override 
    public void checkSyntax(Term[] arguments) throws RuleException {
        if(arguments.length != 1)
            throw new RuleException("notFreeIn expects exactly 1 argument");
    }

    private boolean checkProgramFree(Term term) {
        TermVisitor tv = new DefaultTermVisitor.DepthTermVisitor() {
          public void visit(ModalityTerm modalityTerm) throws TermException {
                throw new TermException("Modality found!");
            }
        };
        
        try {
            term.visit(tv);
            return true;
        } catch (TermException e) {
            return false;
        }
    }

    

}
