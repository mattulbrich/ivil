/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.rule.meta;

import java.util.Set;

import de.uka.iti.pseudo.environment.AbstractMetaFunction;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.TypeVariableCollector;
import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.Variable;

// TODO Documentation needed
public class SkolemTypeMetaFunction extends AbstractMetaFunction {

    public static final String SKOLEMTYPE_NAME_PROPERTY = "skolemTypeName";

    public static final ASTLocatedElement SKOLEM = new ASTLocatedElement() {
        @Override
        public String getLocation() { return "SKOLEMISED"; }};

    public SkolemTypeMetaFunction() throws EnvironmentException {
        super(TypeVariable.ALPHA, "$$skolemType", TypeVariable.BETA);
    }

    @Override
    public Term evaluate(Application application, Environment env,
            RuleApplication ruleApp) throws TermException {

        Type type = application.getSubterm(0).getType();
        if(!(type instanceof TypeVariable)) {
            throw new TermException("Type skolemisation only possible for type variables: " + type);
        }

        TypeVariable typeVar = (TypeVariable) type;

        String property = SKOLEMTYPE_NAME_PROPERTY + "(" + application.getSubterm(0).getType().toString() + ")";
        String name = ruleApp.getProperties().get(property);
        TypeVariable newTypeVar;
        if(name == null) {
            if(ruleApp.hasMutableProperties()) {
                newTypeVar = calcSkolemName(typeVar, ruleApp);
                ruleApp.getProperties().put(property, newTypeVar.getVariableName());
            } else {
                throw new TermException("There is no type skolemisation stored for " + application);
            }
        } else {
            newTypeVar = TypeVariable.getInst(name);
        }

        return Variable.getInst("irrelevant", newTypeVar);
    }


    /*
     * Starting from the name of the typeVariable, return the first type
     * variable (iterate by adding suffixes) that does not appear in the
     * sequent.
     */
    private TypeVariable calcSkolemName(TypeVariable typeVar, RuleApplication ruleApp) {
        Sequent seq = ruleApp.getProofNode().getSequent();
        Set<TypeVariable> anteTypeVars = TypeVariableCollector.collectInTerms(seq.getAntecedent());
        Set<TypeVariable> succTypeVars = TypeVariableCollector.collectInTerms(seq.getSuccedent());

        String orgName = typeVar.getVariableName();
        int suffix = 1;
        while(anteTypeVars.contains(typeVar) || succTypeVars.contains(typeVar)) {
            typeVar = TypeVariable.getInst(orgName + suffix);
            suffix ++;
        }

        return typeVar;
    }

}
