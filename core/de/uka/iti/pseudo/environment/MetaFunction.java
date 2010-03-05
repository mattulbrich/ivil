/*
 * This file is part of PSEUDO
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.environment;

import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;

// TODO Documentation badly needed
public abstract class MetaFunction extends Function {
    
    public static final String SERVICE_NAME = "metaFunction";

    public MetaFunction(Type resultType, String name, Type... argumentTypes) {
        super(name, resultType, argumentTypes, false, false, ASTLocatedElement.BUILTIN);
        
        assert name.startsWith("$$");
    }
    
    
    public abstract Term evaluate(Application application, Environment env, RuleApplication ruleApp)
       throws TermException;

}
