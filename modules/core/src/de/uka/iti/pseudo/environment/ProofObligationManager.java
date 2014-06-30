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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nonnull.DeepNonNull;
import nonnull.NonNull;
import nonnull.Nullable;
import de.uka.iti.pseudo.auto.script.ProofScript;
import de.uka.iti.pseudo.environment.ProofObligation.ProgramPO;
import de.uka.iti.pseudo.term.Modality;

/**
 * A proof obligation manager is a collection of proof obligations and of proof
 * scripts.
 */
public class ProofObligationManager {


    /**
     * The Constant DEFAULT_PO_PROPERTY.
     *
     * @ivildoc "Environment property/ProofObligation.default"
     *
     * If an environment has more than one proof obligation (several rules,
     * lemmas, programs), this property allows one to set the name of the proof
     * obligation to proof by default. In the GUI, this is the proof obligation
     * which is opened then automatically.
     *
     * <p>
     * On the commandline, proving all proof obligations does <i>not</i> reduce
     * to proving the default proof obligation.
     */
    public static final String DEFAULT_PO_PROPERTY = "ProofObligation.default";

    /**
     * The Constant PRORGRAM_PO_PROPERTY.
     *
     * @ivildoc "Environment property/ProofObligation.programs"
     *
     * If an environment contains programs, these give rise to proof
     * obligations. By default, these need not be discharged necessarily; that
     * is, they are excluded from "proof all obligation" calls.
     *
     * <p>
     * If you want to generate proof obligations for programs you can set
     * <tt>ProofObligation.programs</tt> to one of:
     * <ul>
     * <li><tt>partial</tt> for partial correctness or
     * <li><tt>total</tt> for total correctness.
     * </ul>
     *
     * <p>
     * This setting applies to all programs within an environment. If you only
     * want to generate a proof obligation for one particular program <i>P</i>
     * is is better to add a lemma
     *
     * <pre>
     * lemma P_partial
     * [0;P] true
     * </pre>
     *
     * instead with the same effect.
     */
    public static final String PROGRAM_PO_PROPERTY = "ProofObligation.programs";

    /**
     * The environment upon which this works.
     */
    private final Environment env;

    /**
     * The map of proof obligations.
     * They are stored under their names.
     */
    private final Map<String, ProofObligation> proofObligations;

    /**
     * The map of associated scripts.
     * Also stored by their names.
     */
    private final Map<String, ProofScript> scripts;

    /**
     * The default proof obligation.
     * As set using the key {@value #DEFAULT_PO_PROPERTY}.
     */
    private ProofObligation defaultProofObligation;

    /**
     * Instantiates a new proof obligation manager.
     *
     * @param env
     *            the environment to work with
     * @param proofObligations
     *            the proof obligation map to add to
     * @param scripts
     *            the script map to add to
     */
    public ProofObligationManager(@NonNull Environment env,
            @DeepNonNull Map<String, ProofObligation> proofObligations,
            @DeepNonNull Map<String, ProofScript> scripts) {
        this.env = env;
        this.proofObligations = proofObligations;
        this.scripts = scripts;
    }

    /**
     * Instantiates a new proof obligation manager.
     *
     * This assumes an empty (an immutable) map of associated proof scripts.
     *
     * @param env
     *            the environment to work with
     * @param proofObligations
     *            the proof obligation map to add to
     */
    public ProofObligationManager(Environment env,
            Map<String, ProofObligation> proofObligations) {
        this(env, proofObligations, Collections.<String, ProofScript>emptyMap());
    }

    /**
     * Gets the environment on which to the proof obligations on this manager
     * belong.
     *
     * <p>
     * <i>Caution</i> Proof environments for the proof obligations may have
     * different environment. Always get the environment for the proof you
     * are working with instead of this here if there is a proof object.
     *
     * @return the environment for this manager
     */
    public @NonNull Environment getEnvironment() {
        return env;
    }

    /**
     * Checks whether there are any proof obligations.
     *
     * @return <code>true</code>, if there is at least one proof obligation
     */
    public boolean hasProofObligations() {
        return proofObligations.size() > 0;
    }

    /*
     * Compute default proof obligation.
     */
    private void computeDefaultProofObligation() throws EnvironmentException {
        if(defaultProofObligation == null) {
            if(proofObligations.size() == 1) {
                defaultProofObligation = proofObligations.values().iterator().next();
                return;
            } else {
                String defaultPOProp = env.getProperty(DEFAULT_PO_PROPERTY);
                if(defaultPOProp != null) {
                    defaultProofObligation = proofObligations.get(defaultPOProp);
                    if(defaultProofObligation == null) {
                        throw new EnvironmentException("Unknown proof obligation '" +
                                defaultPOProp + "' set as default proof obligation.");
                    }
                }
            }
        }
    }

    /**
     * Checks whether the environment has a default proof obligation.
     *
     * @return true, if there is a default proof obligation defined.
     * @throws EnvironmentException
     *             if the default proof obligation is not known
     */
    public boolean hasDefaultProofObligation() throws EnvironmentException {
        computeDefaultProofObligation();
        return defaultProofObligation != null;
    }

    /**
     * Gets the default proof obligation defined via property
     * {@value #DEFAULT_PO_PROPERTY}.
     *
     * @return the default proof obligation
     * @throws EnvironmentException
     *             if there is no default obligation or an unknown name has been
     *             specified.
     */
    public @NonNull ProofObligation getDefaultProofObligation() throws EnvironmentException {
        if(hasDefaultProofObligation()) {
            return defaultProofObligation;
        } else {
            throw new EnvironmentException("Environment has no default proof obligation.");
        }
    }

    /**
     * Gets the collection of all available proof obligations.
     *
     * @return the available proof obligations
     */
    public Collection<ProofObligation> getAvailableProofObligations() {
        return proofObligations.values();
    }

    /**
     * Gets the collection of all relevant proof obligations.
     *
     * If the property {@value #PROGRAM_PO_PROPERTY} has not been specified this
     * excludes program POs. Otherwise it includes them according to the setting.
     *
     * @return the available proof obligations
     */
    public Collection<ProofObligation> getRelevantProofObligations() {

        Modality acceptedMod = null;
        String prop = env.getProperty(PROGRAM_PO_PROPERTY);
        if(prop != null) {
            if("total".equalsIgnoreCase(prop)) {
                acceptedMod = Modality.BOX_TERMINATION;
            } else if("partial".equalsIgnoreCase(prop)) {
                acceptedMod = Modality.BOX;
            }
        }

        List<ProofObligation> result = new ArrayList<ProofObligation>();
        for (ProofObligation po : proofObligations.values()) {
            if(po instanceof ProofObligation.ProgramPO) {
                ProofObligation.ProgramPO ppo = (ProgramPO) po;
                if(ppo.getModality() == acceptedMod) {
                    result.add(ppo);
                }
            } else {
                result.add(po);
            }
        }

        return result;
    }


    /**
     * Gets the available proof obligation names.
     *
     * @return the available proof obligation names
     */
    public String[] getAvailableProofObligationNames() {
        Collection<ProofObligation> available = getAvailableProofObligations();
        String[] result = new String[available.size()];
        int i = 0;
        for (ProofObligation po : available) {
            result[i++] = po.getName();
        }
        return result;
    }

    /**
     * Gets the names for the relevant proof obligations.
     *
     * @return the relevant proof obligation names
     */
    public String[] getRelevantProofObligationNames() {
        Collection<ProofObligation> relevant = getRelevantProofObligations();
        String[] result = new String[relevant.size()];
        int i = 0;
        for (ProofObligation po : relevant) {
            result[i++] = po.getName();
        }
        return result;
    }

    /**
     * Gets a named proof obligation.
     *
     * @param name
     *            name of the proof obligation to consider
     * @return the proof obligation by this name, or <code>null</code> if this
     *         does not exist.
     */
    public @Nullable ProofObligation getProofObligation(String name) {
        return proofObligations.get(name);
    }

    /**
     * Gets the map of associated proof scriplets.
     *
     * @return an unmodifiable the map proof scriplets
     */
    public Map<String, ProofScript> getProofScriplets() {
        return Collections.unmodifiableMap(scripts);
    }


}
