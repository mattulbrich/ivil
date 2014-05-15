package de.uka.iti.pseudo.rule.meta;

import de.uka.iti.pseudo.environment.AbstractMetaFunction;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.TypeVariable;

public class RefinementModificationMetaFunction extends AbstractMetaFunction {

    private static final Type BOOL = Environment.getBoolType();

    public RefinementModificationMetaFunction() throws EnvironmentException {
        super(BOOL, "$$refinementPrgMod", BOOL, TypeVariable.ALPHA);
    }

    @Override
    public Term evaluate(Application application, Environment env,
            RuleApplication ruleApp) throws TermException {

        RefinementModifier modifier =
                new RefinementModifier(env,
                        application.getSubterm(0),
                        application.getSubterm(1));

        return modifier.apply();
    }

//    private Function getMarkSymbol(Environment env, String propName)
//            throws TermException {
//        String propValue = env.getProperty(propName);
//        if (propValue == null) {
//            throw new TermException("Property '" + propName
//                    + "' must be set in environment");
//        }
//
//        Function f = env.getFunction(propValue);
//        if (f == null) {
//            throw new TermException("Property '" + propName
//                    + "' must denote a function symbol");
//        }
//
//        if (!f.isAssignable()
//                || !f.getResultType().equals(Environment.getIntType())) {
//            throw new TermException("Function " + f.getName()
//                    + " (used by property '" +
//                    propName + "') must be an assignable integer function");
//        }
//
//        return f;
//    }
}



