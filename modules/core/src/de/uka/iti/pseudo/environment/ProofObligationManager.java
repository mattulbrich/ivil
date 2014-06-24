package de.uka.iti.pseudo.environment;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import nonnull.NonNull;
import de.uka.iti.pseudo.auto.script.ProofScript;

public class ProofObligationManager {

    private static final String DEFAULT_PO_PROPERTY = "ProofObligation.default";

    private final Environment env;
    private final Map<String, ProofObligation> proofObligations;
    private final Map<String, ProofScript> scripts;

    private ProofObligation defaultProofObligation;

    public ProofObligationManager(Environment env, Map<String, ProofObligation> proofObligations,
            Map<String, ProofScript> scripts) {
        this.env = env;
        this.proofObligations = proofObligations;
        this.scripts = scripts;
    }

    public ProofObligationManager(Environment env,
            Map<String, ProofObligation> proofObligations) {
        this(env, proofObligations, Collections.<String, ProofScript>emptyMap());
    }

    public Environment getEnvironment() {
        return env;
    }

    public boolean hasProofObligations() {
        return proofObligations.size() > 0;
    }

    public void computeDefaultProofObligation() throws EnvironmentException {
        if(defaultProofObligation != null) {

        } else

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

    public boolean hasDefaultProofObligation() {
        return defaultProofObligation != null;
    }

    public @NonNull ProofObligation getDefaultProofObligation() {
        if(hasDefaultProofObligation()) {
            return defaultProofObligation;
        } else {
            throw new IllegalStateException();
        }
    }

    public Collection<ProofObligation> getAvailableProofObligations() {
        return proofObligations.values();
    }

    public String[] getAvailableProofObligationNames() {
        Collection<ProofObligation> available = getAvailableProofObligations();
        String[] result = new String[available.size()];
        int i = 0;
        for (ProofObligation po : available) {
            result[i++] = po.getName();
        }
        return result;
    }

    public ProofObligation getProofObligation(String name) {
        return proofObligations.get(name);
    }

    public Map<String, ProofScript> getProofScriplets() {
        return scripts;
    }


}
