/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.gui.actions.auto;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.SwingWorker;

import de.uka.iti.pseudo.auto.strategy.Strategy;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.actions.BarAction;
import de.uka.iti.pseudo.gui.actions.BarManager.InitialisingAction;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.term.CodeLocation;
import de.uka.iti.pseudo.util.ExceptionDialog;
import de.uka.iti.pseudo.util.NotificationEvent;
import de.uka.iti.pseudo.util.NotificationListener;

/**
 * This class is designed to implement stepwise execution on source line basis.
 * 
 * If the currently selected proof node is an open goal and getCodeLine returns
 * a value >= 0, the currently active strategy will be applied until all
 * children are either closed or getCodeLine returns a value different from the
 * initial one.
 */
public abstract class StepCodeAction extends BarAction implements
        PropertyChangeListener, InitialisingAction, NotificationListener {
    
    /**
     * StepProofWorker has a field abort, that by default is false. If it is
     * ever set to true, the worker will abort its task as soon as possible.
     */
    static abstract class StepProofWorker extends SwingWorker<Void, Void> {
        boolean abort = false;
    }

    private static final long serialVersionUID = 5444254542006126131L;
    
    protected ProofNode selectedProofNode;

    /**
     * the pointer to the worker is only kept to tell an existing worker to
     * abort the computation
     */
    private StepProofWorker worker = null;

    public StepCodeAction(String name) {
        super(name);
    }

    /**
     * Returns the first code location occurring on proof node.
     * 
     * @param node
     *            the node to be queried for code line information
     * 
     * @return null if no unique code location exists on the proof node; a valid
     *         code location else
     */
    protected abstract CodeLocation getCodeLocation(ProofNode node);

    /**
     * This action takes the current proof node, queries its location in the
     * code and then tries to continue the proof automatically until all open
     * child nodes have a different code location.
     * 
     * <p>
     * The code location is calculated via getCodeLocation. If no valid code
     * location can be found for the current node, nothing is done.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        // has no effect on nodes with children
        if (null != selectedProofNode.getChildren())
            return; // if this effect is undesired, select the first open goal
                    // that has a line number

        final CodeLocation loc = getCodeLocation(selectedProofNode);

        // stepping should be disabled if no unique code location can be
        // identified
        assert (null != loc);

        final ProofCenter pc = getProofCenter();
        final Strategy strategy = pc.getStrategyManager().getSelectedStrategy();
        final Proof proof = pc.getProof();

        final List<ProofNode> todo = new LinkedList<ProofNode>();
        todo.add(selectedProofNode);

        pc.firePropertyChange(ProofCenter.ONGOING_PROOF, true);
        (worker = new StepProofWorker() {
            public Void doInBackground() {
                try {
                    strategy.beginSearch();

                    ProofNode current = null;

                    while (!(abort || todo.isEmpty())) {
                        current = todo.remove(0);

                        if (null != current.getChildren())
                            continue;

                        RuleApplication ra = strategy.findRuleApplication(current);

                        if (ra != null) {
                            proof.apply(ra, pc.getEnvironment());
                            strategy.notifyRuleApplication(ra);

                            for (ProofNode node : current.getChildren()) {
                                CodeLocation next = getCodeLocation(node);
                                if (null == next || next.equals(loc)) {
                                    todo.add(node);
                                }
                            }
                        } else
                            ExceptionDialog.showExceptionDialog(getParentFrame(),
                                    "The currently selected proof strategy is to weak to do another step");
                    }
                } catch (Exception e) {
                    ExceptionDialog.showExceptionDialog(getParentFrame(), e);
                }
                return null;
            }

            public void done() {
                pc.firePropertyChange(ProofCenter.ONGOING_PROOF, false);
                if (selectedProofNode.isClosed()) {
                    if (proof.hasOpenGoals())
                        pc.fireSelectedProofNode(proof.getOpenGoals().get(0));
                    else
                        pc.fireSelectedProofNode(proof.getRoot());
                } else {
                    // find first open node
                    ProofNode current = selectedProofNode;
                    while (current.getChildren() != null)
                        for (ProofNode child : current.getChildren())
                            if (!child.isClosed())
                                current = child;

                    pc.fireSelectedProofNode(current);
                }
                strategy.endSearch();
            }
        }).execute();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (ProofCenter.SELECTED_PROOFNODE.equals(evt.getPropertyName()))
        {
            selectedProofNode = (ProofNode) evt.getNewValue();

            setEnabled(null == selectedProofNode.getChildren() && null != getCodeLocation(selectedProofNode));
        }
        
        if (ProofCenter.ONGOING_PROOF.equals(evt.getPropertyName()))
            setEnabled(!(Boolean) evt.getNewValue());
    }

    @Override
    public void initialised() {
        ProofCenter proofCenter = getProofCenter();
        
        proofCenter.addPropertyChangeListener(
                ProofCenter.SELECTED_PROOFNODE, this);
        proofCenter.addPropertyChangeListener(
                ProofCenter.ONGOING_PROOF, this);
        proofCenter.addNotificationListener(
                ProofCenter.PROOFTREE_HAS_CHANGED, this);
        proofCenter.addNotificationListener(ProofCenter.STOP_REQUEST, this);
        
        selectedProofNode = proofCenter.getProof().getRoot();
    }

    

    @Override
    public void handleNotification(NotificationEvent evt) {
        if(evt.isSignal(ProofCenter.PROOFTREE_HAS_CHANGED)) {
            setEnabled(null == selectedProofNode.getChildren() && null != getCodeLocation(selectedProofNode));

        } else if(evt.isSignal(ProofCenter.STOP_REQUEST)){
            // if there is a worker, set its abort field to true; if the worker
            // was working, it will stop; if he already stopped, nothing will
            // happen
            if (null != worker)
                worker.abort = true;
        }
    }
}