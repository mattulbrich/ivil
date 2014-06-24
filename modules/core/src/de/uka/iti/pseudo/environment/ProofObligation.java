package de.uka.iti.pseudo.environment;

import de.uka.iti.pseudo.auto.script.ProofScript;
import de.uka.iti.pseudo.environment.creation.ruleextraction.RuleProblemExtractor;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.term.Modality;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;

public abstract class ProofObligation {

    public static final String DEFAULT_PO_PROPERTY = "dfsafasd";

    public static class LemmaPO extends ProofObligation {

        public static final String PREFIX = "lemma:";

        private final Lemma lemma;

        public LemmaPO(Environment env, Lemma lemma) {
            super(env, PREFIX + lemma.getName());
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
            super(env, PREFIX + program.getName() + getModalitySuffix(modality));
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

    }

    public static class RulePO extends ProofObligation {

        public static final String PREFIX = "rule:";
        private final Rule rule;

        public RulePO(Environment env, Rule rule) {
            super(env, PREFIX + rule.getName());
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
    private ProofScript proofScript;

    protected ProofObligation(Environment env, String key) {
        this.env = env;
        this.key = key;
    }

    public final String getName() {
        return key;
    }

    public abstract Term getProblemTerm() throws EnvironmentException;

    public final Proof initProof() throws EnvironmentException {
        try {
            Proof result = new Proof(getProblemTerm(), getName(), env);
            return result;
        } catch (TermException e) {
            throw new EnvironmentException(e);
        }
    }

    public ProofScript getProofScript() {
        return proofScript;
    }

    public void setProofScript(ProofScript proofScript) {
        assert proofScript.getObligationIdentifier().equals(getName());
        this.proofScript = proofScript;
    }

    @Override
    public String toString() {
        return "Proof Obligation for " + getName();
    }

}
