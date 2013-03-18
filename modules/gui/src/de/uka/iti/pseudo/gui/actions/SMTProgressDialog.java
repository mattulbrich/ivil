/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.gui.actions;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;

import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.util.Log;
import de.uka.iti.pseudo.util.Pair;

/**
 * This dialog is used to present results of queries to smt solvers.
 */
public class SMTProgressDialog extends JDialog {

    /**
     * The property on ProofCenter that will be used to decide whether or not to
     * close the window after completion.
     */
    public static final String SMT_KEEPWINDOWOPEN_PROPERTY = "pseudo.smt.keepwindowopen";

    /**
     * The labels into which the results are to be reported.
     */
    private final Map<Integer, JLabel> resultLabels =
            new HashMap<Integer, JLabel>();

    private final ProofCenter proofCenter;

    /**
     * Set to true as soon as the worker has finished its job.
     */
    private boolean workerFinished;


    /**
     * Instantiates a new smt progress dialog.
     *
     * @param owner the owner frame
     * @param title the title for the dialog
     * @param proofCenter the proof center to use
     */
    public SMTProgressDialog(Frame owner, String title, ProofCenter proofCenter) {
        super(owner, title, false);
        this.proofCenter = proofCenter;
    }

    /**
     * Initialise and show the dialog.
     */
    public void showDialog() {
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        JPanel panel = new JPanel();
        {
            panel.setLayout(new GridBagLayout());
            this.getContentPane().add(panel);
            panel.setBorder(BorderFactory.createTitledBorder("Open goals"));
        }
        {
            JPanel goalLabels = new JPanel();
            {
                GridLayout gridLayout = new GridLayout(0,1);
                goalLabels.setLayout(gridLayout);
                JScrollPane scroll = new JScrollPane(goalLabels);
                panel.add(scroll,
                        new GridBagConstraints(0, 0, 1, 1, 1., 1.,
                                GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(
                                        2, 2, 2, 2), 0, 0));
            }
//            List<ProofNode> openGoals = proofCenter.getProof().getOpenGoals();
//            int count = openGoals.size();
//            for (int i = 0; i < count; i++) {
//                ProofNode goal = openGoals.get(i);
            for (ProofNode goal : proofCenter.getProof().getOpenGoals()) {
                String text = "Node " + goal.getNumber();
                JLabel label = new JLabel(text);
                label.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                        BorderFactory.createEmptyBorder(5, 2, 5, 2)));
                resultLabels.put(goal.getNumber(), label);
                goalLabels.add(label);
            }
        }
        {
            JCheckBox keepOpen = new JCheckBox("Keep open after completion");
            keepOpen.setSelected(proofCenter.getProperty(SMT_KEEPWINDOWOPEN_PROPERTY) == Boolean.TRUE);
            keepOpen.addItemListener(new ItemListener() {
                @Override public void itemStateChanged(ItemEvent e) {
                    Log.enter(e);
                    boolean selectionState = e.getStateChange() == ItemEvent.SELECTED;
                    proofCenter.firePropertyChange(SMT_KEEPWINDOWOPEN_PROPERTY, selectionState);
                }
            });
            panel.add(keepOpen, new GridBagConstraints(0, 1, 1, 1, 0, 0,
                    GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(
                            10, 2, 2, 2), 0, 0));
        }

        JPanel buttons = new JPanel();
        panel.add(buttons, new GridBagConstraints(0, 2, 1, 1, 0, 0,
                GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(
                        5, 2, 2, 2), 0, 0));
        {
            JButton stop = new JButton("Stop/Close");
            stop.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(workerFinished) {
                        dispose();
                    } else {
                        Log.log(Log.TRACE, "Requesting cancel");
                        proofCenter.fireNotification(ProofCenter.STOP_REQUEST);
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
                    setVisible(false);
                }
            });
            buttons.add(bg);
        }
        Dimension d = panel.getPreferredSize();
        d.height = Math.min(400, d.height);
        panel.setPreferredSize(d);
        this.pack();
        this.setResizable(true);
        this.setLocationRelativeTo(getParent());
        this.setVisible(true);
    }

    /**
     * Indicate that the worker has finished.
     */
    public void finished() {
        workerFinished = true;
    }

    /**
     * Update some results of the worker in the list.
     *
     * @param chunks a list of pairs of proof node numbers and status texts
     */
    public void addResults(List<Pair<Integer, String>> chunks) {
        for (Pair<Integer, String> result : chunks) {
            Integer index = result.fst();
            JLabel label = resultLabels.get(index);
            String text = "Node " + index + ": " + result.snd();
            label.setText(text);

            // scroll the label visible
            label.scrollRectToVisible(new Rectangle(label.getSize()));
        }

    }

}
