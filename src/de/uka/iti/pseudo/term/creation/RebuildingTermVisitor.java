/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.term.creation;

import java.util.List;

import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.BindableIdentifier;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.UnificationException;
import de.uka.iti.pseudo.term.UpdateTerm;
import de.uka.iti.pseudo.term.Variable;
import de.uka.iti.pseudo.term.statement.AssignmentStatement;
import de.uka.iti.pseudo.util.Util;

/**
 * This class is the base class to visitors which change the structure of a term
 * locally.
 * 
 * Since the term datastructure is immutable, this mechanism must be used to
 * partially change a term, i.e. to produce a new term which is identical to the
 * original but at certain places.
 * 
 * The visitor visits into depth and only if a resulting item is not null,
 * reconstructs a parent item. It is hence guaranteed to reuse as much term
 * structure as possible.
 * 
 * The mechanism does not traverse schema programs terms
 */

public class RebuildingTermVisitor extends DefaultTermVisitor {

    /**
     * stores the result of a visitation.
     * 
     * May contain null to indicate that the last subterm has not changed due to 
     * the rebuilding. 
     */
    protected Term resultingTerm;
    
    /**
     * {@inheritDoc}
     * 
     * the default action is to not change the term.
     */
    protected void defaultVisitTerm(Term term) throws TermException {
        resultingTerm = null;
    }

    /**
     * Implement this method if your visitation needs to adapt types of terms
     * also (for instance type variable instantiation).
     * 
     * One may for instance change change the type of an application but not the
     * function symbol (in case of polymorphism)
     * 
     * This implementation just returns the argument
     * 
     * @param type
     *            the type to potentially modify
     * @return the modified type
     * @throws TermException
     *             may be thrown by an implementation
     */
    protected Type modifyType(Type type) throws TermException {
        return type;
    }
    
    /*
     * a variable is rebuilt only if the type modification changes the type.
     */
    @Override
    public void visit(Variable variable) throws TermException {
        defaultVisitTerm(variable);
        if(resultingTerm == null) {
            Type varType = variable.getType();
            Type type = modifyType(varType);
            if(!type.equals(varType))
                resultingTerm = new Variable(variable.getName(), type);
        }
    }

    /*
     * a schema variable is rebuilt only if the 
     * type modification changes the type.
     */
    @Override
    public void visit(SchemaVariable schemaVariable) throws TermException {
        defaultVisitTerm(schemaVariable);
        if(resultingTerm == null) {
            Type varType = schemaVariable.getType();
            Type type = modifyType(varType);
            if(!type.equals(varType))
                resultingTerm = new SchemaVariable(schemaVariable.getName(), type);
        }
    }
    
    /*
     * a binding is rebuilt if any of its arguments or the type have changed
     */
    @Override
    public void visit(Binding binding) throws TermException {
        defaultVisitTerm(binding);
        if(resultingTerm == null) {
            Term[] args = null;
            for(int i = 0; i < binding.countSubterms(); i++) {
                binding.getSubterm(i).visit(this);
                if(resultingTerm != null) {
                    if(args == null) {
                        args = Util.listToArray(binding.getSubterms(), Term.class);
                    }
                    args[i] = resultingTerm;
                }
            }
            
            binding.getVariable().visit(this);
            Term bindingReplacement = resultingTerm == null ? binding
                    .getVariable() : resultingTerm;

            if (!(bindingReplacement instanceof BindableIdentifier)) {
                throw new UnificationException(
                        "Only a variable or schema variable can be instantiated into bindings with schemas",
                        binding, bindingReplacement);
            }
            
            Type modifiedType = modifyType(binding.getType());
            if(args != null) {
                resultingTerm = new Binding(binding.getBinder(), modifiedType,
                        (BindableIdentifier)bindingReplacement, args);
            } else if(!modifiedType.equals(binding.getType()) ||
                    bindingReplacement.equals(binding.getVariable())) {
                args = Util.listToArray(binding.getSubterms(), Term.class);
                resultingTerm = new Binding(binding.getBinder(), modifiedType,
                        (BindableIdentifier)bindingReplacement, args);
            } else {
                resultingTerm = null;
            }
        }
    }

    /*
     * an application is rebuilt if any of its arguments or the type have changed
     */
    @Override
    public void visit(Application application) throws TermException {
        defaultVisitTerm(application);
        if(resultingTerm == null) {
            Term[] args = null;
            for(int i = 0; i < application.countSubterms(); i++) {
                application.getSubterm(i).visit(this);
                if(resultingTerm != null) {
                    if(args == null) {
                        args = Util.listToArray(application.getSubterms(), Term.class);
                    }
                    args[i] = resultingTerm;
                }
            }
            Type modifiedType = modifyType(application.getType());
            if(args != null) {
                resultingTerm = new Application(application.getFunction(), 
                        modifiedType, args);
            } else if(!modifiedType.equals(application.getType())) {
                args = Util.listToArray(application.getSubterms(), Term.class);
                resultingTerm = new Application(application.getFunction(), 
                        modifiedType, args);
            } else {
                resultingTerm = null;
            }
        }
    }
    
    /*
     * an update term is is rebuilt if any of its assignments is modified
     * or if the updated term is modified.
     * The type is not taken into consideration, this is always the type
     * of the child term.
     */
    @Override
    public void visit(UpdateTerm updateTerm) throws TermException {
        defaultVisitTerm(updateTerm);
        if(resultingTerm == null) {
            List<AssignmentStatement> assignments = updateTerm.getAssignments();
            AssignmentStatement[] newAssignments = visitAssignments(assignments);
            
            updateTerm.getSubterm(0).visit(this);
            Term childResult = resultingTerm;
            
            if(newAssignments != null || childResult != null) {
                if(newAssignments == null)
                    newAssignments = Util.listToArray(assignments, AssignmentStatement.class);
                if(childResult == null)
                    childResult = updateTerm.getSubterm(0);
                resultingTerm = new UpdateTerm(newAssignments, childResult);
            } else {
                resultingTerm = null;
            }
        }
    }

    /*
     * An assignment list is updated if one assignment value is updated. 
     */
    private AssignmentStatement[] visitAssignments(
            List<AssignmentStatement> assignments) throws TermException {
        AssignmentStatement[] newAssignments = null;
        for(int i = 0; i < assignments.size(); i++) {
            assignments.get(i).getValue().visit(this);
            if(resultingTerm != null) {
                if(newAssignments == null) {
                    newAssignments = Util.listToArray(assignments, AssignmentStatement.class);
                }
                newAssignments[i] = new AssignmentStatement(assignments.get(i).getTarget(), resultingTerm);
            }
        }
        return newAssignments;
    }
    

}
