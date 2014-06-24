package de.uka.iti.pseudo.environment.creation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.Lemma;
import de.uka.iti.pseudo.environment.Program;
import de.uka.iti.pseudo.environment.ProofObligation;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.Modality;

class EnvironmentProofObligationExtractor {

    private static final String PROOF_OBLIGATIONS_FOR_PROGRAMS_PROPERTY = "proofPrograms";

    private final Environment env;
    private Map<String, ProofObligation> proofObligations;
    private List<Object> availableLemmasAndRules;

    public EnvironmentProofObligationExtractor(Environment env) {
        this.env = env;
    }

    public Map<String, ProofObligation> extract() {

        if(proofObligations != null) {
            return proofObligations;
        }

        this.proofObligations = new HashMap<String, ProofObligation>();
        this.availableLemmasAndRules = new ArrayList<Object>();

        createRulePOs();
        createLemmaPOs();
        createProgramPOs();

        return proofObligations;
    }

    private void createLemmaPOs() {
        for (Lemma lemma : env.getLocalLemmas()) {
            if(!lemma.getDefinedProperties().contains("axiom")) {
                ProofObligation po = new ProofObligation.LemmaPO(env, lemma, availableLemmasAndRules);
                proofObligations.put(po.getName(), po);
            }
        }
    }

    private void createRulePOs() {
        for (Rule rule : env.getLocalRules()) {
            if(!rule.getDefinedProperties().contains("axiom")) {
                ProofObligation po = new ProofObligation.RulePO(env, rule, availableLemmasAndRules);
                proofObligations.put(po.getName(), po);
            }
        }
    }

    private void createProgramPOs() {
        for (Program program : env.getLocalPrograms()) {
            ProofObligation po = new ProofObligation.ProgramPO(env, program, Modality.BOX);
            proofObligations.put(po.getName(), po);
            po = new ProofObligation.ProgramPO(env, program, Modality.BOX_TERMINATION);
            proofObligations.put(po.getName(), po);
        }
    }

}
