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

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.text.JTextComponent;

import de.uka.iti.pseudo.auto.strategy.Strategy;
import de.uka.iti.pseudo.gui.Main;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.actions.BarManager.InitialisingAction;
import de.uka.iti.pseudo.gui.actions.io.LoadProblemAction;
import de.uka.iti.pseudo.gui.sequent.BracketMatchingTextArea;
import de.uka.iti.pseudo.gui.util.HistoryEditor;
import de.uka.iti.pseudo.proof.MutableRuleApplication;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.creation.TermMaker;
import de.uka.iti.pseudo.util.ExceptionDialog;
import de.uka.iti.pseudo.util.GUIUtil;
import de.uka.iti.pseudo.util.Log;
import de.uka.iti.pseudo.util.NotificationEvent;
import de.uka.iti.pseudo.util.NotificationListener;


// TODO DOC!
@SuppressWarnings("serial")
public class ConjectureAction extends BarAction implements InitialisingAction,
        PropertyChangeListener, Runnable, NotificationListener {

    private Rule cutRule;
    private String conjecture;

    public ConjectureAction() {
        super("Add a Conjecture",
                GUIUtil.makeIcon(LoadProblemAction.class.getResource("img/lightbulb_add.png")));

        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));
        putValue(SHORT_DESCRIPTION, "Add a hypothesis and prove it");
    }

    @Override
    public void initialised() {
        ProofCenter proofCenter = getProofCenter();

        cutRule = proofCenter.getEnvironment().getRule("cut");
        if(cutRule == null) {
            Log.log(Log.WARNING, "Rule 'cut' not found");
            setEnabled(false);
        } else {
            proofCenter.addPropertyChangeListener(ProofCenter.SELECTED_PROOFNODE, this);
            proofCenter.addPropertyChangeListener(ProofCenter.ONGOING_PROOF, this);
            proofCenter.addNotificationListener(ProofCenter.PROOFTREE_HAS_CHANGED, this);
        }

    }

    // TODO Do this on a task thread
    @Override
    public void actionPerformed(ActionEvent e) {

        conjecture = showInputDialog();

        if(conjecture == null) {
            return;
        }

        final ProofCenter proofCenter = getProofCenter();
         proofCenter.firePropertyChange(ProofCenter.ONGOING_PROOF, true);
        (new SwingWorker<Void, Void>() {
            @Override
            public Void doInBackground() {

                ProofNode currentProofNode = proofCenter.getCurrentProofNode();
                Strategy strategy = proofCenter.getStrategyManager().getSelectedStrategy();

                try {

                    Term term = TermMaker.makeAndTypeTerm(conjecture,
                            proofCenter.getEnvironment(),
                            currentProofNode.getLocalSymbolTable(),
                            "user input");

                    Main.getTermInputHistory().add(conjecture);

                    MutableRuleApplication ram = new MutableRuleApplication();
                    ram.setRule(cutRule);
                    ram.setProofNode(currentProofNode);
                    ram.getSchemaVariableMapping().put("%inst", term);
                    proofCenter.getProof().apply(ram, proofCenter.getEnvironment());

                    Queue<ProofNode> todo = new LinkedList<ProofNode>();
                    ProofNode topNode = currentProofNode.getChildren().get(1);
                    todo.add(topNode);

                    // init() is called upon creation of the strategy, and only
                    // once!
                    // strategy.init(proof, pc.getEnvironment(),
                    // pc.getStrategyManager());
                    strategy.beginSearch();

                    ProofNode current = null;

                    while (!todo.isEmpty()) {
                        current = todo.remove();

                        RuleApplication ra = strategy.findRuleApplication(current);

                        if (ra != null) {
                            proofCenter.apply(ra);
                            strategy.notifyRuleApplication(ra);

                            for (ProofNode node : current.getChildren()) {
                                todo.add(node);
                            }
                        } else if (current.getChildren() != null) {
                            for (ProofNode node : current.getChildren()) {
                                todo.add(node);
                            }
                        }
                    }

                    ProofNode next = currentProofNode.getChildren().get(0);
                    if (!topNode.isClosed()) {
                        int result = JOptionPane.showConfirmDialog(getParentFrame(),
                                "The proof branch cannot be closed. Keep it?", "Question", JOptionPane.YES_NO_OPTION);
                        if (result == JOptionPane.NO_OPTION) {
                            proofCenter.prune(currentProofNode);
                            next = currentProofNode;
                        }
                    }
                    proofCenter.fireSelectedProofNode(next);

                } catch (Exception ex) {
                    ExceptionDialog.showExceptionDialog(getParentFrame(), ex);
                } finally {
                    strategy.endSearch();

                }
                return null;
            }

            @Override
            public void done() {
                proofCenter.firePropertyChange(ProofCenter.ONGOING_PROOF, false);
                proofCenter.fireProoftreeChangedNotification(true);
            }
        }).execute();
    }

    private String showInputDialog() {

        final JTextComponent textField = new BracketMatchingTextArea();
        Object[] messages = {
                "Please enter the formula to add as hypothesis:",
                new HistoryEditor(Main.getTermInputHistory().getHistory(), textField),
        };

        JOptionPane pane = new JOptionPane(
                messages,
                JOptionPane.QUESTION_MESSAGE,
                JOptionPane.OK_CANCEL_OPTION);

        JDialog dialog = pane.createDialog(getParentFrame(), "Conjecture");
        dialog.addWindowFocusListener(new WindowAdapter() {
            private boolean gotFocus;

            @Override
            public void windowGainedFocus(WindowEvent e) {
                if (!gotFocus) {
                    textField.requestFocus();
                    gotFocus = true;
                }
            }
        });

        dialog.setVisible(true);
        Object selectedValue = pane.getValue();
        System.out.println(selectedValue);
        if(selectedValue == null || selectedValue.equals(JOptionPane.CANCEL_OPTION)) {
            return null;
        }

        return textField.getText();
    }

    @Override
    public void run() {

    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        reconsiderEnableState();
    }

    @Override
    public void handleNotification(NotificationEvent event) {
        reconsiderEnableState();
    }

    private void reconsiderEnableState() {
        ProofCenter proofCenter = getProofCenter();

        boolean ongoing = (Boolean) proofCenter.getProperty(ProofCenter.ONGOING_PROOF);
        ProofNode curProofNode = proofCenter.getCurrentProofNode();
        boolean isLeaf = curProofNode != null && curProofNode.getChildren() == null;

        setEnabled(!ongoing && isLeaf);
    }



}
