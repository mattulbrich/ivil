package de.uka.iti.pseudo.rule.meta;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.uka.iti.pseudo.environment.Binder;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.environment.MetaFunction;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.BindableIdentifier;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.UpdateTerm;
import de.uka.iti.pseudo.term.Variable;
import de.uka.iti.pseudo.term.creation.DefaultTermVisitor;
import de.uka.iti.pseudo.term.statement.AssignmentStatement;

// TODO Documentation needed
public class UpdSimplMetaFunction extends MetaFunction {

    public UpdSimplMetaFunction() {
        super(TypeVariable.ALPHA, "$$updSimpl", TypeVariable.ALPHA);
    }

    public Term evaluate(Application application, Environment env,
            RuleApplication ruleApp) throws TermException {
        
        Term arg = application.getSubterm(0);
        
        if (arg instanceof UpdateTerm) {
            UpdateTerm updTerm = (UpdateTerm) arg;
            Term updatedTerm = updTerm.getSubterm(0);
            List<AssignmentStatement> assignments = updTerm.getAssignments();
            Visitor visitor = new Visitor(assignments);
            updatedTerm.visit(visitor);
            
            if(visitor.resultTerm == null)
                throw new TermException("nothing to update");
            
            return visitor.resultTerm;
        } else {
            throw new TermException("Update Simplifier only applicable to updat terms");
        }
    }
    
    private static Term applyUpdate(List<AssignmentStatement> assignments,
            Application application) {
        
        for (AssignmentStatement assStatement : assignments) {
            if(assStatement.getTarget().equals(application))
                return assStatement.getValue();
        }
        
        return application;
    }
    
    private static Application distributeUpdate(List<AssignmentStatement> assignments,
            Application application) throws TermException {
        
        Function f = application.getFunction();
        Type type = application.getType();
        Term[] args = new Term[application.countSubterms()];
        
        for (int i = 0; i < args.length; i++) {
            args[i] = new UpdateTerm(assignments, application.getSubterm(i));
        }
        
        return new Application(f, type, args);
    }
    
    private static Binding distributeUpdateInBinding(List<AssignmentStatement> assignments,
            Binding binding) throws TermException {
        
        Binder b = binding.getBinder();
        Type type = binding.getType();
        BindableIdentifier bi = binding.getVariable();
        Term[] args = new Term[binding.countSubterms()];
        for (int i = 0; i < args.length; i++) {
            args[i] = new UpdateTerm(assignments, binding.getSubterm(i));
        }
        
        return new Binding(b, type, bi, args);
    }
    
    private static Term combineUpdate(List<AssignmentStatement> oldAss,
            UpdateTerm updTerm) throws TermException {
        
        List<AssignmentStatement> newAss = updTerm.getAssignments();
        
        // collect all updated vars of new 2nd
        Set<Term> overwritten = new HashSet<Term>();
        for (AssignmentStatement ass : newAss) {
            overwritten.add(ass.getTarget());
        }
        
        // create target update
        List<AssignmentStatement> result = new ArrayList<AssignmentStatement>();
        
        // go over all old updates
        for (AssignmentStatement ass : oldAss) {
            if(!overwritten.contains(ass.getTarget())) {
                result.add(ass);
            }
        }
        
        // add all new updates in which the old update is applied
        for (AssignmentStatement ass : newAss) {
            UpdateTerm value = new UpdateTerm(oldAss, ass.getValue());
            AssignmentStatement freshAss = new AssignmentStatement(ass.getTarget(), value);
            result.add(freshAss);
        }
        
        return new UpdateTerm(result, updTerm.getSubterm(0));
    }
    
    private static class Visitor extends DefaultTermVisitor {
        
        private Term resultTerm = null;
        private List<AssignmentStatement> assignments;

        public Visitor(List<AssignmentStatement> assignments) {
            this.assignments = assignments;
        }

        public void visit(Binding binding) throws TermException {
            resultTerm = distributeUpdateInBinding(assignments, binding);
        }
        
        public void visit(Variable variable) throws TermException {
            resultTerm  = variable;
        }

        public void visit(Application application) throws TermException {
            Function f = application.getFunction();
            if(f.isAssignable())
                resultTerm = applyUpdate(assignments, application);
            else {
                resultTerm = distributeUpdate(assignments, application);
            }
        }

        public void visit(UpdateTerm updateTerm) throws TermException {
            resultTerm = combineUpdate(assignments, updateTerm);
        }

        protected void defaultVisitTerm(Term term) throws TermException {
            // Do nothing by default
        }

    }
}
