package de.uka.iti.pseudo.auto.script;

import java.io.File;
import java.net.URL;

import de.uka.iti.pseudo.environment.ProofObligation;
import de.uka.iti.pseudo.environment.ProofObligationManager;
import de.uka.iti.pseudo.environment.creation.EnvironmentCreationService;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.util.Dump;

public class TestScript {

    static {
        System.getProperties().put("pseudo.logClass", "JavaUtilLogImplementation");
    }

    public static void main(String[] args) throws Exception {

        URL url = new File("examples/scripts.p").toURI().toURL();

        ProofObligationManager proofObMan =
                EnvironmentCreationService.createEnvironmentByExtension(url);

        ProofObligation po = proofObMan.getProofObligation("lemma:lemma1");

        Proof proof = po.initProof();
        ProofScript ps = po.getProofScript();

        ScriptedProofTree spt = new ScriptedProofTree(proof);

        spt.execute(ps);

        spt.dump(System.err);
    }

}
