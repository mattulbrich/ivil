package de.uka.iti.pseudo.gui.actions.auto;

import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingWorker;

import de.uka.iti.pseudo.auto.strategy.Strategy;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.util.CompoundException;
import de.uka.iti.pseudo.util.ExceptionDialog;
import de.uka.iti.pseudo.util.Log;
import de.uka.iti.pseudo.util.PooledAutoProver;

public class ParallelAutoProofWorker extends SwingWorker<Void, Void> {
    private final List<ProofNode> nodes;
    private final PooledAutoProver pool;
    private final Strategy strategy;
    private final ProofCenter pc;
    private final Frame parentFrame;
    private final JDialog dialog;
    private final JLabel raCount, workCount, unclosableCount, timeElapsed;
    // start time; subtract 500ms for proper rounding
    private final long startTime = System.currentTimeMillis() - 500;

    private final int initialyClosableGoals;

    public ParallelAutoProofWorker(List<ProofNode> nodes, PooledAutoProver pool, ProofCenter pc, Strategy strategy,
            Frame frame) {
        this.nodes = nodes;
        this.pool = pool;
        this.pc = pc;
        this.strategy = strategy;
        parentFrame = frame;

        this.initialyClosableGoals = pc.getProof().getOpenGoals().size() - this.nodes.size();

        dialog = new JDialog(frame, "Auto proving...", true);
        dialog.setLayout(new GridLayout(5, 1, 10, 10));

        raCount = new JLabel("Rules applied: #########");
        workCount = new JLabel("Open goals: #####");
        unclosableCount = new JLabel("Unclosable goals: #####");
        timeElapsed = new JLabel("Running since #### seconds");

        dialog.add(raCount);
        dialog.add(workCount);
        dialog.add(unclosableCount);
        dialog.add(timeElapsed);
        dialog.setSize(dialog.getLayout().preferredLayoutSize(dialog));
        dialog.setLocation(frame.getWidth() / 2, frame.getHeight() / 2);
    }

    public void showDialog(){

        // stop auto proving if the user closed the stats dialog
        dialog.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                try {
                    pool.stopAutoProve(true);
                } catch (Exception ex) {
                    Log.stacktrace(ex);
                    ExceptionDialog.showExceptionDialog(parentFrame, ex);
                }
            }
        });
        if (!isDone())
            dialog.setVisible(true);
    }

    public Void doInBackground() {
        for (ProofNode node : nodes)
            pool.autoProve(node);

        // update the dialog while proving
        while (!pool.done()) {
            raCount.setText("Applications done: " + pool.getSuccessfullApplicationsCount());
            workCount.setText("Open goals: " + pool.getOpenGoalsCount());
            unclosableCount.setText("Unclosable goals: "
                    + ((pc.getProof().getOpenGoals().size() - pool.getOpenGoalsCount()) - initialyClosableGoals));
            timeElapsed.setText("Running since " + (System.currentTimeMillis() - startTime) / 1000 + " second"
                    + (((System.currentTimeMillis() - startTime) / 1000) == 1 ? "" : "s"));

            //Don't update faster then 100Hz
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                // Don't interrupt this thread
                e.printStackTrace();
            }
        }

        // get exceptions
        try {
            pool.waitAutoProve();
        } catch (Exception e) {
            Log.stacktrace(e);
            ExceptionDialog.showExceptionDialog(parentFrame, e);
        }

        return null;
    }

    public void done() {

        // close the dialog, as it is not interesting for the user any more
        dialog.setVisible(false);

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
