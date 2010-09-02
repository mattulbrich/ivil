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
package de.uka.iti.pseudo.term.creation;

import java.util.Stack;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.MetaFunction;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.SchemaProgramTerm;
import de.uka.iti.pseudo.term.SchemaUpdateTerm;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.TypeVariableBinding;
import de.uka.iti.pseudo.term.Variable;

/**
 * The Class ToplevelCheckVisitor allows to check whether 
 * a term is suitable for use in a sequent or a program.
 * 
 * No entities on available in schematic terms are allowed.
 * 
 * If the term does not have the following properties, a 
 * {@link TermException} is thrown:
 * <ul>
 * <li>it is of boolean type
 * <li>no schema variable is present as subterm
 * <li>no schema update is present
 * <li>no schema program term is present
 * <li>no schema variable is bound in a binding
 * <li>no "normal" type variable is bound in a type variable binding
 * <li>no meta function appears
 * <li>no free variable occur
 * <li>program terms have no matching statement
 * (like in <code>[number : statement]</code>)
 * </ul>
 * 
 * There may be free type variables (as in for instance)
 * <pre>
 *   $subset(empty as set('a), empty as set('a))
 * </pre>
 */
public class ToplevelCheckVisitor extends DefaultTermVisitor.DepthTermVisitor {

    private Stack<Variable> allowedVariables = new Stack<Variable>();

    public void check(Term term) throws TermException {
        term.visit(this);
        if(!term.getType().equals(Environment.getBoolType()))
            throw new TermException("Top level term does not have boolean type");
    }

    public void visit(SchemaProgramTerm schemaProgram) throws TermException {
        throw new TermException("Top level term contains schema program"
                + schemaProgram);
    }

    public void visit(SchemaVariable schemaVariable) throws TermException {
        throw new TermException("Top level term contains schema variable "
                + schemaVariable);
    }
    
    public void visit(SchemaUpdateTerm schemaUpdateTerm) throws TermException {
        throw new TermException("Top level term contains schema update in "
                + schemaUpdateTerm);
    }

    public void visit(Binding binding) throws TermException {
        if (binding.hasSchemaVariable())
            throw new TermException(
                    "Top level term contains bound schema variable"
                            + binding.getVariable());

        allowedVariables.push((Variable) binding.getVariable());
        super.visit(binding);
        allowedVariables.pop();
    }
    
    public void visit(Variable variable) throws TermException {
        if(!allowedVariables.contains(variable)) {
            throw new TermException("Top level term contains free variable " + variable);
        }
    }
    
    public void visit(Application application) throws TermException {

        if(application.getFunction() instanceof MetaFunction)
            throw new TermException("Top level term contains meta function " + application);
        
        super.visit(application);
    }
    
    @Override
    public void visit(TypeVariableBinding typeVariableBinding)  throws TermException {
        
        String variableName = typeVariableBinding.getTypeVariable().getVariableName();
        if(!variableName.startsWith(TypeVariable.BINDABLE_PREFIX))
            throw new TermException("Top level term contains ordinary bound type variable '" + variableName);
        
        super.visit(typeVariableBinding);
    }

}
