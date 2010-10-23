package de.uka.iti.pseudo.gui.actions.auto;

import java.awt.Container;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.WindowConstants;

import de.uka.iti.pseudo.auto.strategy.Strategy;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.util.CircleProgressIndicator;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.util.CompoundException;
import de.uka.iti.pseudo.util.ExceptionDialog;
import de.uka.iti.pseudo.util.Log;
import de.uka.iti.pseudo.util.PooledAutoProver;
import de.uka.iti.pseudo.util.settings.Settings;

public class ParallelAutoProofWorker extends SwingWorker<Void, Void> implements ActionListener {
    
    private final static int REFRESH_DELAY = 
        Settings.getInstance().getInteger("pseudo.auto.popup.refreshDelay", 100);
    
    private final List<ProofNode> nodes;
    private final PooledAutoProver pool;
    private final Strategy strategy;
    private final ProofCenter pc;
    private final Frame parentFrame;
    private final JDialog dialog;
    private final JLabel raCount, workCount, unclosableCount, timeElapsed;
    // start time; subtract 500ms for proper rounding
    private final long startTime = System.currentTimeMillis() - 500;
    
    private Timer timer = new Timer(REFRESH_DELAY, this);

    private final int initialyClosableGoals;

    public ParallelAutoProofWorker(List<ProofNode> nodes, PooledAutoProver pool, ProofCenter pc, Strategy strategy,
            Frame frame) {
        this.nodes = nodes;
        this.pool = pool;
        this.pc = pc;
        this.strategy = strategy;
        parentFrame = frame;

        this.initialyClosableGoals = pc.getProof().getOpenGoals().size() - this.nodes.size();

        dialog = new JDialog(frame, "Auto proving ...", true);
        Container cp = dialog.getContentPane();
        dialog.setLayout(new GridBagLayout());
        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        raCount = new JLabel("Rules applied: #########");
        workCount = new JLabel("Open goals: #####");
        unclosableCount = new JLabel("Unclosable goals: #####");
        timeElapsed = new JLabel("Running since #### seconds");

        cp.add(new CircleProgressIndicator(), new GridBagConstraints(0, 0, 1, 4, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(
                        2, 2, 5, 2), 0, 0));
        cp.add(raCount, new GridBagConstraints(1, 0, 1, 1, 1, 1,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(
                        2, 2, 5, 2), 0, 0));
        cp.add(workCount, new GridBagConstraints(1, 1, 1, 1, 1, 1,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(
                        2, 2, 5, 2), 0, 0));
        cp.add(unclosableCount, new GridBagConstraints(1, 2, 1, 1, 1, 1,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(
                        2, 2, 5, 2), 0, 0));
        cp.add(timeElapsed, new GridBagConstraints(1, 3, 1, 1, 0, 0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(
                        2, 2, 5, 2), 0, 0));
        
        JPanel buttons = new JPanel();
        cp.add(buttons, new GridBagConstraints(0, 4, 2, 1, 0, 0,
                GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(
                        10, 2, 2, 2), 0, 0));
        {
            JButton stop = new JButton("S T O P");
            stop.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        ParallelAutoProofWorker.this.pool.stopAutoProve(true);
                    } catch (Exception ex) {
                        Log.stacktrace(ex);
                        ExceptionDialog.showExceptionDialog(parentFrame, ex);
                    }
                    timer.stop();
                    dialog.setVisible(false);
                }
            });
            buttons.add(stop);
        }
        {
            // TODO Good idea or not?
            JButton bg = new JButton("Run in background");
            bg.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    timer.stop();
                    dialog.setVisible(false);
                }
            });
            buttons.add(bg);
        }
        
        dialog.pack();
        dialog.setResizable(false);
        
    }

    public void showDialog(){

        // stop auto proving if the user closed the stats dialog
//        dialog.addWindowListener(new WindowAdapter() {
//            public void windowClosing(WindowEvent we) {
//                try {
//                    pool.stopAutoProve(true);
//                } catch (Exception ex) {
//                    Log.stacktrace(ex);
//                    ExceptionDialog.showExceptionDialog(parentFrame, ex);
//                }
//            }
//        });
//        
        if (!isDone()) {
            dialog.setLocationRelativeTo(parentFrame);
            timer.start();
            dialog.setVisible(true);
        }
    }

    public Void doInBackground() {

        // get exceptions
        try {
            strategy.beginSearch();
            
            for (ProofNode node : nodes)
                pool.autoProve(node);
            
            pool.waitAutoProve();
            
            strategy.endSearch();
        } catch (Exception e) {
            Log.stacktrace(e);
            ExceptionDialog.showExceptionDialog(parentFrame, e);
        }

        return null;
    }
    
    
    /**
     * update the labels describing the state of the auto prove
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        raCount.setText("Applications done: " + pool.getSuccessfullApplicationsCount());
        workCount.setText("Open goals: " + pool.getOpenGoalsCount());
        unclosableCount.setText("Unclosable goals: "
                + ((pc.getProof().getOpenGoals().size() - pool.getOpenGoalsCount()) - initialyClosableGoals));
        timeElapsed.setText("Running since " + (System.currentTimeMillis() - startTime) / 1000 + " second"
                + (((System.currentTimeMillis() - startTime) / 1000) == 1 ? "" : "s"));
    }

    public void done() {

        // close the dialog, as it is not interesting for the user any more
        dialog.setVisible(false);
        timer.stop();

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
