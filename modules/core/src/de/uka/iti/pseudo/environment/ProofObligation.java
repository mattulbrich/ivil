/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */

package de.uka.iti.pseudo.environment;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nonnull.DeepNonNull;
import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.auto.script.ProofScript;
import de.uka.iti.pseudo.environment.creation.ruleextraction.RuleProblemExtractor;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.term.Modality;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;

/**
 * The Class ProofObligation captures a problem which can/must be proved.
 *
 * Every proof obligation has a name which is unique in its environment. To
 * prevent name clashes between programs, lemmas and rules an according prefix
 * (like "lemma:") is added to the name.
 *
 * The instantiation of the proof from an obligation is implemented in three
 * subclasses that handle the cases differently.
 *
 * To every proof obligation a {@link ProofScript} can be set and retrieved. The
 * name of the proof obligation referred to by the proof script must be the name
 * of this proof obligation.
 *
 * For some proof obligations, the environment for the proof to construct needs
 * to be reduced to knowledge defined prior to the definition of the element to
 * be proved. Otherwise circular proofs would be possible.
 */
public abstract class ProofObligation {

    /**
     * Use this constant if a string is needed pointing to an unknown
     * {@link ProofObligation}.
     */
    public static final String NO_PROOF_OBLIGATION = "none";

    /**
     * Proof obligations for lemmas.
     *
     * From the environment, the elements defined below the element need to be
     * eliminated.
     *
     * The lemma itself is the proof obligation formula.
     */
    public static class LemmaPO extends ProofObligation {

        /**
         * The PREFIX for lemmas is "lemma:".
         */
        public static final String PREFIX = "lemma:";

        /**
         * The lemma to prove itself.
         */
        private final @NonNull Lemma lemma;

        /**
         * Instantiates a new proof obligation.
         *
         * The list of available symbols defines which rules and lemmas will be
         * present for the proof of this obligation to avoid cyclic proofs.
         *
         * @param env
         *            the original environment. Is not modified.
         * @param lemma
         *            the lemma to prove
         * @param available
         *            a list of {@link Lemma}s and {@link Rule}s
         */
        public LemmaPO(@NonNull Environment env, @NonNull Lemma lemma,
                @DeepNonNull List<Object> available) {
            super(env, PREFIX + lemma.getName(), available);
            this.lemma = lemma;
        }

        @Override
        public Term getProblemTerm() throws EnvironmentException {
            return lemma.getTerm();
        }

    }

    /**
     * Proof obligations for programs.
     *
     * The environment needs not be changed for programs since proved programs
     * are never used as lemmas elsewhere. They can, hence, be placed at the end
     * of the dependency graph.
     *
     * This proof obligation is parametrised with a {@link Modality} to indicate
     * whether partial or total correcteness is investigated.
     */
    public static class ProgramPO extends ProofObligation {

        /**
         * The suffix for total correctness. Appended to the name of the program.
         */
        public static final String SUFFIX_TOTAL = "_total";

        /**
         * The suffix for partial correctness. Appended to the name of the program.
         */
        public static final String SUFFIX_PARTIAL = "_partial";

        /**
         * The prefix for programs is "program:".
         */
        public static final String PREFIX = "program:";

        /**
         * The program for which proof obligations are created.
         */
        private final Program program;

        /**
         * The modality under which proof obligations are created.
         * Either BOX or MOX_TERMINATION.
         */
        private final Modality modality;

        /**
         * Instantiates a new proof obligation.
         *
         * @param env
         *            the environment for the proof
         * @param program
         *            the program to proof
         * @param modality
         *            either {@link Modality#BOX} or
         *            {@link Modality#BOX_TERMINATING}
         */
        public ProgramPO(Environment env, Program program, Modality modality) {
            super(env, PREFIX + program.getName() + getModalitySuffix(modality),
                    Collections.<Object>emptyList());
            this.program = program;
            this.modality = modality;

            assert modality == Modality.BOX || modality == Modality.BOX_TERMINATION;
        }

        /*
         * compute the suffix for a modality.
         */
        private static String getModalitySuffix(Modality modality) {
            switch(modality) {
            case BOX:
                return SUFFIX_PARTIAL;
            case BOX_TERMINATION:
                return SUFFIX_TOTAL;
            default:
                return "-???"; // will be caught later ...
            }
        }

        /**
         * {@inheritDoc}
         *
         * <p>
         * This implementation takes the program and the modality and constructs
         * the program term starting in statement 0 and with postcondition true.
         */
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

        /**
         * {@inheritDoc}
         *
         * <p> In this case, the environment needs not be modified.
         */
        @Override
        public Environment getProofEnvironment() {
            return env;
        }

        /**
         * Gets the modality for ths proof obligation for the program.
         * @return either BOX or BOX_TERMINATION
         */
        public Modality getModality() {
            return modality;
        }
    }

    /**
     * Proof obligations for rules.
     *
     * From the environment, the elements defined below the element need to be
     * eliminated.
     *
     * The rule is transformed to its meaning formula using a
     * {@link RuleProblemExtractor}.
     *
     * @see RuleProblemExtractor
     */
    public static class RulePO extends ProofObligation {

        /**
         * The prefix for rule obligations is "rule:".
         */
        public static final String PREFIX = "rule:";

        /**
         * The actual rule.
         */
        private final Rule rule;

        /**
         * Instantiates a new rule proof obligation.
         *
         *
         * The list of available symbols defines which rules and lemmas will be
         * present for the proof of this obligation to avoid cyclic proofs.
         *
         * @param env
         *            the original environment. Is not modified.
         * @param rule
         *            the rule to prove
         * @param available
         *            a list of {@link Lemma}s and {@link Rule}s/**
         */
        public RulePO(Environment env, Rule rule, List<Object> available) {
            super(env, PREFIX + rule.getName(), available);
            this.rule = rule;
        }

        /**
         * {@inheritDoc}
         * <p>In this case the problem term is the meaning formula of the rule.
         */
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

    /**
     * The environment in which this proof obligation exists. This is not
     * necessarily the same environment in which the proof will be conducted as
     * some rules and lemmas may need to be removed.
     */
    protected @NonNull final Environment env;

    /**
     * The key of this proof obligation. It is unique within the environment.
     */
    private final String key;

    /**
     * The available lemmas and rules for this proof obligation.
     *
     * If the environment needs to be restricted, this list holds all
     * {@link Lemma}s and {@link Rule}s which are to be copied while all
     * other lemmas and rules are dropped.
     */
    private final Set<Object> availableLemmasAndRules;

    /**
     * The proof script for this obligation.
     * Needs not be set.
     */
    private @Nullable ProofScript proofScript;

    /**
     * Instantiates a new proof obligation.
     *
     * The list of object presented here needs not be immutable. A snapshot is
     * taken of it and stored internally. The list may evolve afterwards.
     *
     * @param env
     *            the original environment; will not be modified.
     * @param key
     *            the unique key of this obligation
     * @param availableLemmasAndRules
     *            the available lemmas and rules
     */
    protected ProofObligation(Environment env, String key, List<Object> availableLemmasAndRules) {
        this.env = env;
        this.key = key;
        this.availableLemmasAndRules = new HashSet<Object>(availableLemmasAndRules);
    }

    /**
     * Gets the name of this proof obligation.
     *
     * It is unique within {@link #env}.
     *
     * @return the name of this proof obligation
     */
    public final @NonNull String getName() {
        return key;
    }

    /**
     * Gets the problem term that is the starting point for a proof.
     *
     * @return the toplevel boolean term for this proof obligation
     * @throws EnvironmentException
     *             if somethings goes wrong while constructing it.
     */
    public abstract @NonNull Term getProblemTerm() throws EnvironmentException;

    /**
     * Get or construct the proof environment.
     *
     * This may involve some computations. If the proof obligation needs a
     * restricted environment, the original environment {@link #env} is taken,
     * and only the supported lemmas and rules from
     * {@link #availableLemmasAndRules} are readded to it.
     *
     * @return the environment in which to conduct the proof.
     *
     * @throws EnvironmentException
     *             if the construction fails
     */
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

    /**
     * Initialise a fresh proof for this obligation.
     *
     * The proof will base on the appropriate environment.
     *
     * @return a freshly created proof.
     *
     * @throws EnvironmentException
     *             if the construction fails.
     */
    public final Proof initProof() throws EnvironmentException {
        try {
            Proof result = new Proof(getProblemTerm(), getName(), getProofEnvironment());
            return result;
        } catch (TermException e) {
            throw new EnvironmentException(e);
        }
    }

    /**
     * Gets the proof script for this obligation.
     *
     * The script is guaranteed to refer to this proof obligation by identifier.
     *
     * @return the proof script
     */
    public final @Nullable ProofScript getProofScript() {
        return proofScript;
    }

    /**
     * Sets the proof script for this proof obligation.
     *
     * The script must refer to this proof obligation by identifier.
     *
     * @param proofScript
     *            the new proof script
     */
    public final void setProofScript(@NonNull ProofScript proofScript) {
        assert proofScript.getObligationIdentifier().equals(getName());
        this.proofScript = proofScript;
    }

    @Override
    public String toString() {
        return "Proof Obligation for " + getName();
    }

}
