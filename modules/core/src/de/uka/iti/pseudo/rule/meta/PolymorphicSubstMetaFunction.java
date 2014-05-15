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

import static de.uka.iti.pseudo.term.TypeVariable.ALPHA;
import static de.uka.iti.pseudo.term.TypeVariable.BETA;
import de.uka.iti.pseudo.environment.AbstractMetaFunction;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVariable;
import de.uka.iti.pseudo.term.Variable;

// TODO Documentation needed
public class PolymorphicSubstMetaFunction extends AbstractMetaFunction {

    private static final Type BOOL = Environment.getBoolType();

    private final SubstMetaFunction subst;
    private final TypeUnificationMetaFunction spec;

    public PolymorphicSubstMetaFunction() throws EnvironmentException {
        super(BOOL, "$$polymorphicSubst", ALPHA, BETA, BOOL);
        this.subst = new SubstMetaFunction();
        this.spec = new TypeUnificationMetaFunction();
    }

    @Override
    public Term evaluate(Application application, Environment env, RuleApplication ruleApp) throws TermException {

        Term toReplace = application.getSubterm(0);
        Term replaceWith = application.getSubterm(1);
        Term replaceIn = application.getSubterm(2);

        return evaluate(toReplace, replaceWith, replaceIn, env);
    }

    public Term evaluate(Term toReplace, Term replaceWith, Term replaceIn, Environment env) throws TermException {

        // ensures by signature of the meta function
        assert replaceIn.getType().equals(BOOL);

        if(!(toReplace instanceof Variable)) {
            throw new TermException("Only a variable can be substituted, not " + toReplace);
        }

        if(!(toReplace.getType() instanceof TypeVariable)) {
            throw new TermException("Only a type variable can be type-substituted, not " +
                        toReplace.getType());
        }

        Term toReplaceSpec = spec.evaluate(toReplace.getType(), replaceWith.getType(), toReplace);
        assert toReplaceSpec.getType().equals(replaceWith.getType());
        assert toReplaceSpec instanceof Variable;

        Term replaceInSpec = spec.evaluate(toReplace.getType(), replaceWith.getType(), replaceIn);
        assert replaceInSpec.getType().equals(BOOL);

        Term result = subst.evaluate(toReplaceSpec, replaceWith, replaceInSpec, env);

        return result;
    }

}
