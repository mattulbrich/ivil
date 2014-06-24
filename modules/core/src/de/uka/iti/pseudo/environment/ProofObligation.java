package de.uka.iti.pseudo.environment;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uka.iti.pseudo.auto.script.ProofScript;
import de.uka.iti.pseudo.environment.creation.ruleextraction.RuleProblemExtractor;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.term.Modality;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;

public abstract class ProofObligation {

    public static class LemmaPO extends ProofObligation {

        public static final String PREFIX = "lemma:";

        private final Lemma lemma;

        public LemmaPO(Environment env, Lemma lemma, List<Object> available) {
            super(env, PREFIX + lemma.getName(), available);
            this.lemma = lemma;
        }

        @Override
        public Term getProblemTerm() throws EnvironmentException {
            return lemma.getTerm();
        }

    }

    public static class ProgramPO extends ProofObligation {

        public static final String SUFFIX_TOTAL = "_total";
        public static final String SUFFIX_PARTIAL = "_partial";
        public static final String PREFIX = "program:";

        private final Program program;
        private final Modality modality;

        public ProgramPO(Environment env, Program program, Modality modality) {
            super(env, PREFIX + program.getName() + getModalitySuffix(modality),
                    Collections.<Object>emptyList());
            assert modality == Modality.BOX || modality == Modality.BOX_TERMINATION;
            this.program = program;
            this.modality = modality;
        }

        private static String getModalitySuffix(Modality modality) {
            switch(modality) {
            case BOX: return SUFFIX_PARTIAL;
            case BOX_TERMINATION: return SUFFIX_TOTAL;
            default: return "-???"; // will be caught later ...
            }
        }

        @Override
        public Term getProblemTerm() throws EnvironmentException {
            final Term trueTerm = Environment.getTrue();
            try {
                LiteralProgramTerm progTerm =
                        LiteralProgramTerm.getInst(0, modality, program, trueTerm);
                return progTerm;
            } catch (TermException e) {
                throw new EnvironmentException(e);
            }
        }

        @Override
        public Environment getProofEnvironment() {
            return env;
        }
    }

    public static class RulePO extends ProofObligation {

        public static final String PREFIX = "rule:";
        private final Rule rule;

        public RulePO(Environment env, Rule rule, List<Object> available) {
            super(env, PREFIX + rule.getName(), available);
            this.rule = rule;
        }

        @Override
        public Term getProblemTerm() throws EnvironmentException {
            RuleProblemExtractor rpe = new RuleProblemExtractor(rule, env);
            try {
                return rpe.extractProblem();
            } catch (Exception e) {
                throw new EnvironmentException("Cannot create proof obligation for rule " +
                            rule.getName(), e);
            }
        }
    }

    protected final Environment env;
    private final String key;
    private final Set<Object> availableLemmasAndRules;
    private ProofScript proofScript;

    protected ProofObligation(Environment env, String key, List<Object> availableLemmasAndRules) {
        this.env = env;
        this.key = key;
        this.availableLemmasAndRules = new HashSet<Object>(availableLemmasAndRules);
    }

    public final String getName() {
        return key;
    }

    public abstract Term getProblemTerm() throws EnvironmentException;

    public Environment getProofEnvironment() throws EnvironmentException {

        Environment result = env.getCopyWithoutRulesAndLemmas();

        for (Rule rule : env.getLocalRules()) {
            if(availableLemmasAndRules.contains(rule)) {
                result.addRule(rule);
            }
        }

        for (Lemma lemma : env.getLocalLemmas()) {
            if(availableLemmasAndRules.contains(lemma)) {
                result.addLemma(lemma);
            }
        }

        return result;
    }

    public final Proof initProof() throws EnvironmentException {
        try {
            Proof result = new Proof(getProblemTerm(), getName(), getProofEnvironment());
            return result;
        } catch (TermException e) {
            throw new EnvironmentException(e);
        }
    }

    public final ProofScript getProofScript() {
        return proofScript;
    }

    public final void setProofScript(ProofScript proofScript) {
        assert proofScript.getObligationIdentifier().equals(getName());
        this.proofScript = proofScript;
    }

    @Override
    public String toString() {
        return "Proof Obligation for " + getName();
    }

}
