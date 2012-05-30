/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.term.creation;

import java.util.List;

import nonnull.Nullable;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.BindableIdentifier;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.term.SchemaProgramTerm;
import de.uka.iti.pseudo.term.SchemaType;
import de.uka.iti.pseudo.term.SchemaUpdateTerm;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.TypeVariableBinding;
import de.uka.iti.pseudo.term.UnificationException;
import de.uka.iti.pseudo.term.Update;
import de.uka.iti.pseudo.term.UpdateTerm;
import de.uka.iti.pseudo.term.Variable;
import de.uka.iti.pseudo.term.statement.Assignment;
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
@SuppressWarnings({"nullness"})
public class RebuildingTermVisitor extends DefaultTermVisitor {

    /**
     * stores the result of a visitation.
     * 
     * May contain null to indicate that the last subterm has not changed due to 
     * the rebuilding. 
     */
    protected @Nullable Term resultingTerm;
    
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
                resultingTerm = Variable.getInst(variable.getName(), type);
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
                resultingTerm = SchemaVariable.getInst(schemaVariable.getName(), type);
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
            
            /*
             * sub classes may choose to do things with bound variables
             * as well. 
             */
            visitBindingVariable(binding);
            
            Term bindingReplacement = resultingTerm == null ? binding
                    .getVariable() : resultingTerm;

            if (!(bindingReplacement instanceof BindableIdentifier)) {
                throw new UnificationException(
                        "Only a variable or schema variable can be bound in a binder",
                        binding, bindingReplacement);
            }
            
            Type modifiedType = modifyType(binding.getType());
            if(args != null) {
                resultingTerm = Binding.getInst(binding.getBinder(), modifiedType,
                        (BindableIdentifier)bindingReplacement, args);
            } else if(!modifiedType.equals(binding.getType()) ||
                    !bindingReplacement.equals(binding.getVariable())) {
                args = Util.listToArray(binding.getSubterms(), Term.class);
                resultingTerm = Binding.getInst(binding.getBinder(), modifiedType,
                        (BindableIdentifier)bindingReplacement, args);
            } else {
                resultingTerm = null;
            }
        }
    }
    
    /*
     * a type variable binding is rebuilt if its argument or 
     * the type variable have changed
     */
    @Override
    public void visit(TypeVariableBinding tyVarBinding) throws TermException {
        defaultVisitTerm(tyVarBinding);
        if(resultingTerm == null) {
            boolean changed = false;
            
            Term subterm = tyVarBinding.getSubterm();
            subterm.visit(this);
            if(resultingTerm != null) {
                subterm = resultingTerm;
                changed = true;
            }
            
            /*
             * sub classes may choose to do things with bound variables
             * as well. 
             */
            Type boundType = tyVarBinding.getBoundType();
            Type modifiedType = modifyType(boundType);
            
            if (!(modifiedType instanceof TypeVariable)
                    && !(modifiedType instanceof SchemaType)) {
                throw new TermException("The bound type of a type quantification must be a type variable or schema type: " + modifiedType);
            }
            
            if(boundType != modifiedType) {
                changed = true;
                boundType = modifiedType;
            }

            if(changed) {
                resultingTerm = TypeVariableBinding.getInst(tyVarBinding.getKind(),
                        boundType, subterm);
            } else {
                resultingTerm = null;
            }
        }
    }
    
//    /**
//     * Some implementations may choose to visit the bound type variable and change it.
//     * 
//     * They can then override this method according to their needs.
//     * 
//     * The default behaviour is to NOT change the type variable
//     * 
//     * @param tyVarBinding binding to visit the bound type variable in.
//     */
//    protected TypeVariable visitBindingTypeVariable(TypeVariableBinding tyVarBinding) {
//        return tyVarBinding.getTypeVariable();
//    }

    /**
     * Some implementations may choose to visit the bound variable as a subterm
     * and others may not.
     * 
     * They can then override this method according to their needs.
     * It should return its result in resultingTerm which may be null if 
     * nothing should be changed or may contain a {@link BindableIdentifier}.
     * 
     * The default behaviour is to NOT visit the variable
     * 
     * @param binding binding to visit the bound variable in.
     */
    protected void visitBindingVariable(Binding binding) throws TermException {
        resultingTerm = null;
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
                resultingTerm = Application.getInst(application.getFunction(), 
                        modifiedType, args);
            } else if(!modifiedType.equals(application.getType())) {
                args = Util.listToArray(application.getSubterms(), Term.class);
                resultingTerm = Application.getInst(application.getFunction(), 
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
            updateTerm.getSubterm(0).visit(this);
            Term childResult = resultingTerm;
            
            List<Assignment> assignments = updateTerm.getAssignments();
            Assignment[] newAssignments = visitAssignments(assignments);
            
            if(newAssignments != null || childResult != null) {
                if(newAssignments == null)
                    newAssignments = Util.listToArray(assignments, Assignment.class);
                if(childResult == null)
                    childResult = updateTerm.getSubterm(0);
                Update update = new Update(newAssignments);
                resultingTerm = UpdateTerm.getInst(update, childResult);
            } else {
                resultingTerm = null;
            }
        }
    }
    
    /*
     * a literal program term is rebuilt if the suffix term is modified.
     * Types are always boolean
     */
    @Override
    public void visit(LiteralProgramTerm literalProgramTerm) throws TermException {
        defaultVisitTerm(literalProgramTerm);
        if(resultingTerm == null) {
            literalProgramTerm.getSuffixTerm().visit(this);
            Term childResult = resultingTerm;
            
            if(childResult != null) {
                resultingTerm = LiteralProgramTerm.getInst(
                        literalProgramTerm.getProgramIndex(), 
                        literalProgramTerm.getModality(),
                        literalProgramTerm.getProgram(), 
                        childResult);
            }
        }
    }
    
    @Override
    public void visit(SchemaProgramTerm schemaProgramTerm) throws TermException {
        defaultVisitTerm(schemaProgramTerm);
        if(resultingTerm == null) {
            schemaProgramTerm.getSuffixTerm().visit(this);
            Term childResult = resultingTerm;
            
            if(childResult != null) {
                resultingTerm = SchemaProgramTerm.getInst(
                        schemaProgramTerm.getSchemaVariable(), 
                        schemaProgramTerm.getModality(),
                        schemaProgramTerm.getMatchingStatement(), 
                        childResult);
            }
        }
    }
    
    /*
     * a schema update term is is rebuilt if the updated term is modified.
     * The type is not taken into consideration, this is always the type
     * of the child term.
     */
    public void visit(SchemaUpdateTerm schemaUpdateTerm) throws TermException {
        defaultVisitTerm(schemaUpdateTerm);
        if(resultingTerm == null) {
            schemaUpdateTerm.getSubterm(0).visit(this);
            boolean optional = schemaUpdateTerm.isOptional();
            if(resultingTerm != null) {
                resultingTerm = SchemaUpdateTerm.getInst(schemaUpdateTerm.getSchemaIdentifier(), optional, resultingTerm);
            }
        }
    }


    /*
     * An assignment list is updated if one assignment value is updated.
     * returns null if no value has been changed
     */
    private Assignment[] visitAssignments(
            List<Assignment> assignments) throws TermException {
        Assignment[] newAssignments = null;
        for(int i = 0; i < assignments.size(); i++) {
            assignments.get(i).getValue().visit(this);
            if(resultingTerm != null) {
                if(newAssignments == null) {
                    newAssignments = Util.listToArray(assignments, Assignment.class);
                }
                newAssignments[i] = new Assignment(assignments.get(i).getTarget(), resultingTerm);
            }
        }
        return newAssignments;
    }

}
