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
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.UpdateTerm;
import de.uka.iti.pseudo.term.Variable;
import de.uka.iti.pseudo.term.statement.AssignmentStatement;
import de.uka.iti.pseudo.util.Util;


/**
 * This class is the base class to visitors which change the structure of a term
 * locally.
 * 
 * Since the term datastructure is immutable, this mechanism must be used to
 * change a term, i.e. to produce a new term which is identical to the original
 * but at certain places.
 * 
 * The visitor visits into depth and only if a resulting item is not null,
 * reconstructs a parent item. It is hence guaranteed to reuse as much term
 * structure as possible.
 * 
 */

public class RebuildingTermVisitor extends DefaultTermVisitor {

    protected Term resultingTerm;
    
    protected void defaultVisitTerm(Term term) throws TermException {
        resultingTerm = null;
    }
    
    protected Type modifyType(Type type) throws TermException {
        return type;
    }
    
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
            Type modifiedType = modifyType(binding.getType());
            if(args != null) {
                resultingTerm = new Binding(binding.getBinder(), modifiedType,
                        binding.getVariable(), args);
            } else if(!modifiedType.equals(binding.getType())) {
                args = Util.listToArray(binding.getSubterms(), Term.class);
                resultingTerm = new Binding(binding.getBinder(), modifiedType,
                        binding.getVariable(), args);
            }else {
                resultingTerm = null;
            }
        }
    }

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
    
    public void visit(UpdateTerm updateTerm) throws TermException {
        defaultVisitTerm(updateTerm);
        if(resultingTerm == null) {
            AssignmentStatement[] newAssignments = null;
            List<AssignmentStatement> assignments = updateTerm.getAssignments();
            for(int i = 0; i < assignments.size(); i++) {
                assignments.get(i).getValue().visit(this);
                if(resultingTerm != null) {
                    if(newAssignments == null) {
                        newAssignments = Util.listToArray(assignments, AssignmentStatement.class);
                    }
                    newAssignments[i] = new AssignmentStatement(assignments.get(i).getTarget(), resultingTerm);
                }
            }
            
            if(newAssignments != null) {
                resultingTerm = new UpdateTerm(newAssignments, updateTerm.getSubterm(0));
            } else {
                resultingTerm = null;
            }
        }
    }

}
