package de.uka.iti.pseudo.rule.meta;

import java.util.List;

import nonnull.NonNull;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.environment.MetaFunction;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.meta.RefinementMarkInfoCollector.MarkInfo;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.term.Modality;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.creation.TermFactory;

public class RefinementModificationMetaFunction extends MetaFunction {

    private static final Type BOOL = Environment.getBoolType();

    public RefinementModificationMetaFunction() throws EnvironmentException {
        super(BOOL, "$$refinementPrgMod", BOOL);
    }

    @Override
    public Term evaluate(Application application, Environment env,
            RuleApplication ruleApp) throws TermException {

        LiteralProgramTerm programTerm;

        Function markConcrete = getMarkSymbol(env, "refinement.markConcrete");
        Function markAbstract = getMarkSymbol(env, "refinement.markConcrete");

        RefinementModifier modifier =
                new RefinementModifier(env, application.getSubterm(0),
                        markAbstract, markConcrete);

        return modifier.apply();
    }

    private Function getMarkSymbol(Environment env, String propName)
            throws TermException {
        String propValue = env.getProperty(propName);
        if (propValue == null) {
            throw new TermException("Property '" + propName
                    + "' must be set in environment");
        }

        Function f = env.getFunction(propValue);
        if (f == null) {
            throw new TermException("Property '" + propName
                    + "' must denote a function symbol");
        }

        if (!f.isAssignable()
                || !f.getResultType().equals(Environment.getIntType())) {
            throw new TermException("Function " + f.getName()
                    + " (used by property '" +
                    propName + "') must be an assignable integer function");
        }

        return f;
    }
}

final class RefinementModifier {

    private final @NonNull LiteralProgramTerm programTerm;
    private final @NonNull LiteralProgramTerm innerProgramTerm;
    private final @NonNull Term postcondition;

    private final @NonNull Function markConcreteProgvar;
    private final @NonNull Function markAbstractProgvar;
    private List<MarkInfo> markInfoConcrete;
    private List<MarkInfo> markInfoAbstract;
    private final Environment env;
    private final TermFactory tf;

    public RefinementModifier(Environment env, Term term, Function markAbstract,
            Function markConcrete) throws TermException {

        this.env = env;
        this.tf = new TermFactory(env);

        if (term instanceof LiteralProgramTerm) {
            this.programTerm = (LiteralProgramTerm) term;
        } else {
            throw new TermException("Argument needs to be a program term");
        }

        if (programTerm.getSubterm(0) instanceof LiteralProgramTerm) {
            innerProgramTerm = (LiteralProgramTerm) programTerm.getSubterm(0);
        } else {
            throw new TermException("Post-condition needs to be a program term");
        }

        if (programTerm.getModality() != Modality.BOX) {
            throw new TermException("Outer modality needs to be [.]");
        }

        if (innerProgramTerm.getModality() != Modality.DIAMOND) {
            throw new TermException("Outer modality needs to be [<.>]");
        }

        this.postcondition = innerProgramTerm.getSubterm(0);
        this.markAbstractProgvar = markAbstract;
        this.markConcreteProgvar = markConcrete;
    }

    public Term apply() throws TermException {
        assert markAbstractProgvar != null;
        assert markConcreteProgvar != null;

        this.markInfoConcrete =
                RefinementMarkInfoCollector.collectMarkAssignments(programTerm.getProgram(),
                        markConcreteProgvar);

        this.markInfoAbstract =
                RefinementMarkInfoCollector.collectMarkAssignments(innerProgramTerm.getProgram(),
                        markAbstractProgvar);

        // TODO ... be conservative ...
        // checkSameMarkLiterals();

        Term glue = prepareGlue();

        // XXX Work here
        return null;
    }

    private Term prepareGlue() throws TermException {
        Term result = tf.eq(tf.cons(markAbstractProgvar), tf.cons(markConcreteProgvar));

        // the abstract mark info contains the coupling invariants
        for (MarkInfo info : markInfoAbstract) {
            if(info.couplingInvariant != null) {
                Term eq = tf.eq(tf.cons(markAbstractProgvar), tf.number(info.literal));
                Term impl = tf.impl(eq, info.couplingInvariant);
                result = tf.and(result, impl);
            }
        }

        // and finally the postcondition (as "0")
        {
            Term eq = tf.eq(tf.cons(markAbstractProgvar), tf.number(0));
            Term impl = tf.impl(eq, postcondition);
            result = tf.and(result, impl);
        }

        return result;
    }

}

