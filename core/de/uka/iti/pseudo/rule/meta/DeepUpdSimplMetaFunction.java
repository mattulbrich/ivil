package de.uka.iti.pseudo.rule.meta;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.UpdateTerm;
import de.uka.iti.pseudo.term.creation.RebuildingTermVisitor;

public class DeepUpdSimplMetaFunction extends AbstractUpdSimplMetaFunction {

    public DeepUpdSimplMetaFunction() {
        super("$$deepUpdSimpl");
    }

    @Override
    public Term evaluate(Application application, Environment env,
            RuleApplication ruleApp) throws TermException {
        
        Term arg = application.getSubterm(0);
        Visitor visitor = new Visitor();
        
        arg.visit(visitor);
        
        if(visitor.getResultingTerm() == null)
            throw new TermException("deep update simplification has nothing to do.");
        
        return visitor.getResultingTerm();
    }
    
    private static class Visitor extends RebuildingTermVisitor {
        
        @Override
        public void visit(UpdateTerm updateTerm) throws TermException {
            Term applied = applyUpdate(updateTerm);
            
            if(applied == updateTerm) {
                // nothing to be changed ... return null
                resultingTerm = null;
            } else {
                applied.visit(this);
                if(resultingTerm == null) {
                    // no further applications
                    resultingTerm = applied;
                }
            }
        }
        
        /**
         * @return the resultingTerm
         */
        public Term getResultingTerm() {
            return resultingTerm;
        }

        
    }

}
