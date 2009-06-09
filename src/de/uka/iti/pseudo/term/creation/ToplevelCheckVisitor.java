/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.term.creation;

import java.util.Stack;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.MetaFunction;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Binding;
import de.uka.iti.pseudo.term.SchemaModality;
import de.uka.iti.pseudo.term.SchemaVariable;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Variable;

/**
 * The Class ToplevelCheckVisitor allows to check whether 
 * a term is suitable for top level use in a sequent.
 * 
 * If the term does not have the following properties, a 
 * {@link TermException} is thrown:
 * <ul>
 * <li>it is of boolean type
 * <li>no schema modality is present
 * <li>no schema variable is present as subterm
 * <li>no schema variable is bound in a binding
 * <li>no meta function appears
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

        if (term.getType().equals(Environment.getBoolType()))
            throw new TermException(
                    "Top level formula term which is not boolean");
    }

    public void visit(SchemaModality schemaModality) throws TermException {
        throw new TermException("Top level term contains schema modality"
                + schemaModality);
    }

    public void visit(SchemaVariable schemaVariable) throws TermException {
        throw new TermException("Top level term contains schema modality"
                + schemaVariable);
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
    
    public void visit(Application application) throws TermException {

        if(application.getFunction() instanceof MetaFunction)
            throw new TermException("Top level term contains meta function " + application);
        
        super.visit(application);
    }

}
