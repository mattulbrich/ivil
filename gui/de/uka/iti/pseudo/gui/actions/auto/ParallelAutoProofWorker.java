package de.uka.iti.pseudo.gui.actions.auto;

import java.util.List;

import javax.swing.SwingWorker;

import de.uka.iti.pseudo.auto.strategy.Strategy;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.util.CompoundException;
import de.uka.iti.pseudo.util.Log;
import de.uka.iti.pseudo.util.PooledAutoProver;

public class ParallelAutoProofWorker extends SwingWorker<Void, Integer> {
    private final List<ProofNode> nodes;
    private final PooledAutoProver pool;
    private final Strategy strategy;
    private final ProofCenter pc;

    public ParallelAutoProofWorker(List<ProofNode> nodes, PooledAutoProver pool, ProofCenter pc, Strategy strategy) {
        this.nodes = nodes;
        this.pool = pool;
        this.pc = pc;
        this.strategy = strategy;
    }

    public Void doInBackground() {
        for (ProofNode node : nodes)
            pool.autoProve(node);

        try {
            pool.waitAutoProve();
        } catch (Exception e) {
            Log.stacktrace(e);
            // TODO ExceptionDialog.showExceptionDialog(getParentFrame(), e);
        }

        return null;
    }

    public void done() {
        strategy.endSearch();
        pc.firePropertyChange(ProofCenter.ONGOING_PROOF, false);
        // some listeners have been switched off, they might want to update now.
        pc.fireProoftreeChangedNotification(true);
    }

    /**
     * tell the worker to finish as soon as possible
     * 
     * @throws InterruptedException
     *             thrown from pool.stopAutoProve
     * @throws CompoundException
     *             thrown from pool.stopAutoProve
     */
    public void halt() throws CompoundException, InterruptedException {
        pool.stopAutoProve(true);
    }
}
