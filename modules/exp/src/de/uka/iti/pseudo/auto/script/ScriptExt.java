package de.uka.iti.pseudo.auto.script;

import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.extensions.ContextExtension;

public class ScriptExt implements ContextExtension {

    @Override
    public String getName() {
        return "script";
    }

    @Override
    public String getDescription() {
        return "Run script (exp)";
    }

    @Override
    public boolean shouldOffer(ProofCenter proofCenter) {
        String id = proofCenter.getProof().getObligationIdentifier();
        ProofScript ps = proofCenter.getStrategyManager().getScriptMap().get(id);
        return ps != null;
    }

    @Override
    public void run(ProofCenter proofCenter) throws Exception {
        String id = proofCenter.getProof().getObligationIdentifier();
        ProofScript ps = proofCenter.getStrategyManager().getScriptMap().get(id);
        assert ps != null : "Why was it offered??";

        proofCenter.firePropertyChange(ProofCenter.ONGOING_PROOF, true);

        ScriptedProofTree spt = new ScriptedProofTree(proofCenter.getProof());
        spt.execute(ps);

        proofCenter.fireProoftreeChangedNotification(true);
        proofCenter.firePropertyChange(ProofCenter.ONGOING_PROOF, false);
    }

}
