/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.rule.meta;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.uka.iti.pseudo.environment.Binder;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.environment.MetaFunction;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.BindableIdentifier;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.Update;
import de.uka.iti.pseudo.term.UpdateTerm;
import de.uka.iti.pseudo.term.Variable;
import de.uka.iti.pseudo.term.creation.DefaultTermVisitor;
import de.uka.iti.pseudo.term.statement.AssignmentStatement;

/**
 * The Class UpdSimplMetaFunction handles updates in front of terms.
 * 
 * <p>In contrast to the KeY system, simplifications are single steps.
 * 
 * <p>The following simplifications are built in (let x be an 
 * assignable symbol, v a variable)
 * <ul>
 * <li> <code>{U} f(t1,...,tn) --&gt; f( {U}t1, ..., {U}tn )</code>
 * <li> <code>{U} (\b v;t1;...;tn) --&gt; (\b v; {U}t1; ...; {U}tn)</code>
 * <li> <code>{... || x:=t || ...} x --&gt; t</code>
 * <li> <code>{U}x --&gt; x</code> if x is not assigned in U
 * <li> <code>{U}v --&gt; v</code>
 * <li> <code>{U}{V}t --&gt; {U' || V'}t</code> in which U' does not 
 * contain updates to assignables assigned in V and every value in V' 
 * is updated with U.
 * </ul>
 * 
 * See methods for more detail.
 */
public abstract class AbstractUpdSimplMetaFunction extends MetaFunction {

    /**
     * Instantiates a new update simplificator.
     */
    public AbstractUpdSimplMetaFunction(String name) {
        super(TypeVariable.ALPHA, name, TypeVariable.ALPHA);
    }

    // may return null
    protected static Term applyUpdate(UpdateTerm updTerm) throws TermException {
        Term updatedTerm = updTerm.getSubterm(0);
        Update update = updTerm.getUpdate();
        Visitor visitor = new Visitor(update);
        updatedTerm.visit(visitor);


        return visitor.resultTerm;
    }
    
    /*
     * Apply update to an assignable application term.
     * 
     * if the assignable is changed in the assignment list the assigned value is
     * returned, otherwise the unchanged (and un-updated) application itself is
     * returned
     */
    private static Term applyUpdateToAssignable(Update update,
            Application assignable) {
        
        assert assignable.getFunction().isAssignable();
         
        for (AssignmentStatement assStatement : update.getAssignments()) {
            if(assStatement.getTarget().equals(assignable))
                return assStatement.getValue();
        }
        
        return assignable;
    }
    
    /*
     * Distribute an update over an application.
     * 
     * Return a new application with all subterms updated.
     */
    private static Application distributeUpdate(Update update,
            Application application) throws TermException {
        
        assert !application.getFunction().isAssignable();
        
        Function f = application.getFunction();
        Type type = application.getType();
        Term[] args = new Term[application.countSubterms()];
        
        for (int i = 0; i < args.length; i++) {
            args[i] = new UpdateTerm(update, application.getSubterm(i));
        }
        
        return new Application(f, type, args);
    }
    
    /*
     * Distribute an update over a binder.
     * 
     * Return a new application with all subterms updated.
     */
    private static Binding distributeUpdateInBinding(Update update,
            Binding binding) throws TermException {
        
        Binder b = binding.getBinder();
        Type type = binding.getType();
        BindableIdentifier bi = binding.getVariable();
        Term[] args = new Term[binding.countSubterms()];
        for (int i = 0; i < args.length; i++) {
            args[i] = new UpdateTerm(update, binding.getSubterm(i));
        }
        
        return new Binding(b, type, bi, args);
    }
    
    /**
     * Combine two consecutive updates.
     * 
     * @param oldAss
     *            the old assignments (the outer ones)
     * @param updTerm
     *            the update term (the inner one)
     * 
     * @return the term
     * 
     * @throws TermException
     *             the term exception
     */
    private static Term combineUpdate(Update oldAss,
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
        for (AssignmentStatement ass : oldAss.getAssignments()) {
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
        
        return new UpdateTerm(new Update(result), updTerm.getSubterm(0));
    }
    
    /*
     * The visitor is used to do the case distinction and to handle
     * update erasure in front of variables.
     */
    private static class Visitor extends DefaultTermVisitor {
        
        private Term resultTerm = null;
        
        private Update update;

        private Visitor(Update update) {
            this.update = update;
        }

        public void visit(Binding binding) throws TermException {
            resultTerm = distributeUpdateInBinding(update, binding);
        }
        
        public void visit(Variable variable) throws TermException {
            resultTerm  = variable;
        }

        public void visit(Application application) throws TermException {
            Function f = application.getFunction();
            if(f.isAssignable()) {
                resultTerm = applyUpdateToAssignable(update, application);
            } else {
                resultTerm = distributeUpdate(update, application);
            }
        }

        public void visit(UpdateTerm updateTerm) throws TermException {
            resultTerm = combineUpdate(update, updateTerm);
        }

        protected void defaultVisitTerm(Term term) throws TermException {
            // Do nothing by default
        }

    }
}
