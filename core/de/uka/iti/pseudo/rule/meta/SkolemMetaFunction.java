/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.rule.meta;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.environment.MetaFunction;
import de.uka.iti.pseudo.environment.NumberLiteral;
import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.Variable;

// TODO Documentation needed
public class SkolemMetaFunction extends MetaFunction {
    
    public static final String SKOLEM_NAME_PROPERTY = "skolemName";
    
    public static final ASTLocatedElement SKOLEM = new ASTLocatedElement() {
        public String getLocation() { return "SKOLEMISED"; }};
        
    public SkolemMetaFunction() {
        super(TypeVariable.ALPHA, "$$skolem", TypeVariable.ALPHA);
    }

    @Override
    public Term evaluate(Application application, Environment env,
            RuleApplication ruleApp) throws TermException {
        
        String property = SKOLEM_NAME_PROPERTY + "(" + application.getSubterm(0).toString(true) + ")";
        String name = ruleApp.getProperties().get(property);
        if(name == null) {
            if(ruleApp.hasMutableProperties()) {
                name = calcSkolemName(application, env);
                ruleApp.getProperties().put(property, name);
            } else {
                throw new TermException("There is no skolemisation stored for " + application);
            }
        }
        
        Function newFunction = env.getFunction(name);
        if(newFunction == null) {
            newFunction = new Function(name, application.getType(), new Type[0], 
                    false, false, SKOLEM);

            try {
                env.addFunction(newFunction);
            } catch (EnvironmentException e) {
                throw new TermException(e);
            }
        }
        
        return new Application(newFunction, application.getType());
    }

    
    /*
     * If the skolemised term is either a variable or a function symbol
     * use its name as prefix. Otherwise fall back to "sk"
     */
    private String calcSkolemName(Application application, Environment env) {
        String prefix = "sk";
        Term term = application.getSubterm(0);
        if (term instanceof Application) {
            // try to use function symbol name to skolemise
            Function innerFunct = ((Application) term).getFunction();
            if(!(innerFunct instanceof NumberLiteral))
                prefix = innerFunct.getName();
            
        } else if (term instanceof Variable) {
            Variable var = (Variable) term;
            prefix = var.getName();
        } 
        
        return env.createNewFunctionName(prefix);
    }

}
