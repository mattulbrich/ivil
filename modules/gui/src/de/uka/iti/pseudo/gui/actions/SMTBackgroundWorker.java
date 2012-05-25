package de.uka.iti.pseudo.gui.actions;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;

import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.util.SwingWorker2;
import de.uka.iti.pseudo.proof.MutableRuleApplication;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.util.ExceptionDialog;
import de.uka.iti.pseudo.util.Log;
import de.uka.iti.pseudo.util.NotificationEvent;
import de.uka.iti.pseudo.util.NotificationListener;
import de.uka.iti.pseudo.util.Pair;

/**
 * A two-thread worker which acts when the action is performed.
 *
 * It has two modes:
 * <ol>
 * <li>Flashing mode: Whenever there are entries in {@link #provableNodes},
 * only apply the according rules. This is not very time consuming since the
 * SMT solver needs not run a second time.
 * <li>Non-flashing mode: If there are no known closable goals, apply the
 * SMT to all available goals with a longer timeout. Open a window which
 * reports on the progress.
 * </ol>
 *
 */
 class SMTBackgroundWorker extends SwingWorker2<Void, String>
        implements NotificationListener {

    /**
     * The labels into which the results are to be reported.
     */
    private final List<JLabel> resultLabels = new LinkedList<JLabel>();

    /**
     * The dialog which reports about the progress.
     */
    private JDialog dialog;

    private final SMTBackgroundAction action;

    private final ProofCenter proofCenter;

    private ArrayList<ProofNode> openGoals;

    /**
     * This worker should listen to stop requests (if run in background)
     */
    public SMTBackgroundWorker(SMTBackgroundAction action, ProofCenter proofCenter) {
        this.action = action;
        this.proofCenter = proofCenter;
        proofCenter.addNotificationListener(ProofCenter.STOP_REQUEST, this);
    }

    @Override
    protected Void doInBackground() throws Exception {

        assert (Boolean)proofCenter.getProperty(ProofCenter.ONGOING_PROOF);

        List<ProofNode> provableNodes = new ArrayList<ProofNode>(action.getProvableNodes());
        if (!provableNodes.isEmpty()) {

            // automatic rules: do not bother with window & rules, just do it.
            for (ProofNode node : provableNodes) {
                // the node might have been worked on already somehow,
                // as this is a multi-threaded environment
                if (node.getChildren() != null) {
                    continue;
                }

                MutableRuleApplication ra = new MutableRuleApplication();
                ra.setProofNode(node);
                ra.setRule(action.getCloseRule());
                try {
                    proofCenter.apply(ra);
                } catch (Exception e) {
                    ExceptionDialog.showExceptionDialog(action.getParentFrame(), e);
                }
            }

            // In done() we call this which settles it:
            // proofCenter.fireProoftreeChangedNotification(false);

        } else {

            // bugfix: clone the list first
            openGoals = new ArrayList<ProofNode>(proofCenter.getProof().getOpenGoals());

            // trigger the window;
            publish((String)null);

            for (ProofNode proofNode : openGoals) {

                // check for cache hit
                boolean proveable = action.isProvable(proofNode);

                if(proveable) {
                    MutableRuleApplication ra = new MutableRuleApplication();
                    ra.setProofNode(proofNode);
                    ra.setRule(action.getCloseRule());

                    try {
                        proofCenter.apply(ra);
                        publish("CLOSED");
                    } catch (ProofException e) {
                        publish("exception");
                        throw e;
                        }
                    } else {
                        publish("open");
                    }

                }
            }

        return null;
    }

    /**
     * in the end: indicate end of ongoing proof, fire tree change and
     * unregister as notification listener.
     */
    @Override
    protected void done() {

        Exception innerException = getException();
        if(innerException != null) {
            ExceptionDialog.showExceptionDialog(action.getParentFrame(), innerException);
        }

        if(dialog != null &&
                proofCenter.getProperty(SMTBackgroundAction.SMT_KEEPWINDOWOPEN_PROPERTY) != Boolean.TRUE) {
            dialog.dispose();
        }

        proofCenter.firePropertyChange(ProofCenter.ONGOING_PROOF, false);
        proofCenter.fireProoftreeChangedNotification(true);
        proofCenter.removeNotificationListener(ProofCenter.STOP_REQUEST, this);
    }

    /**
     * provide feedback on closed goals. {@link #doInBackground()} reports
     * boolean values: True if a goal is closable, false if not.
     */
    @Override
    protected void process(List<String> chunks) {

        if(dialog == null) {
            makeProgressWindow();
        }

        for (String result : chunks) {
            if(result == null) {
                // a null is sent to trigger the window
                continue;
            }
            JLabel label = resultLabels.remove(0);
            label.setText(label.getText() + " " + result);
        }
    }


    protected void makeProgressWindow() {
        dialog = new JDialog(action.getParentFrame(), "Applying the SMT solver", true);

        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        JPanel panel = new JPanel();
        {
            panel.setLayout(new GridBagLayout());
            dialog.getContentPane().add(panel);
            panel.setBorder(BorderFactory.createTitledBorder("Open goals"));
        }

        int count = openGoals.size();
        for (int i = 0; i < count; i++) {
            ProofNode goal = openGoals.get(i);
            String text = "Node " + goal.getNumber();
            Boolean status = action.getStatus(goal.getSequent());
            if(status != null) {
                text += status ? " (cached VALID)" : " (cached open)";
            }
            JLabel label = new JLabel(text + ":");
            label.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
            resultLabels.add(label);
            panel.add(label,
                    new GridBagConstraints(0, i, 1, 1, 0, 1,
                            GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(
                                    2, 2, 2, 2), 0, 0));
        }
        {
            JCheckBox keepOpen = new JCheckBox("Keep open after completion");
            keepOpen.setSelected(proofCenter.getProperty(SMTBackgroundAction.SMT_KEEPWINDOWOPEN_PROPERTY) == Boolean.TRUE);
            keepOpen.addItemListener(new ItemListener() {
                @Override public void itemStateChanged(ItemEvent e) {
                    Log.enter(e);
                    boolean selectionState = e.getStateChange() == ItemEvent.SELECTED;
                    proofCenter.firePropertyChange(SMTBackgroundAction.SMT_KEEPWINDOWOPEN_PROPERTY, selectionState);
                }
            });
            panel.add(keepOpen, new GridBagConstraints(0, count, 1, 1, 0, 0,
                    GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(
                            10, 2, 2, 2), 0, 0));
        }

        JPanel buttons = new JPanel();
        panel.add(buttons, new GridBagConstraints(0, count+1, 1, 1, 0, 0,
                GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(
                        5, 2, 2, 2), 0, 0));
        {
            JButton stop = new JButton("Stop/Close");
            stop.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(isDone()) {
                        dialog.dispose();
                    } else {
                        cancel(true);
                    }
                }
            });
            buttons.add(stop);
        }
        {
            // TODO Have a skip button to jump to next goal.
            JButton skip = new JButton("Skip");
            buttons.add(skip);
            skip.setEnabled(false);
        }
        {
            JButton bg = new JButton("Background");
            bg.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dialog.setVisible(false);
                }
            });
            buttons.add(bg);
        }

        dialog.pack();
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(action.getParentFrame());
        dialog.setVisible(true);
    }

    @Override
    public void handleNotification(NotificationEvent event) {
        Log.enter(event);
        cancel(true);
    }

}
