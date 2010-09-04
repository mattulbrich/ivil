package de.uka.iti.pseudo.gui.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Icon;
import de.uka.iti.pseudo.auto.strategy.Strategy;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.actions.BarManager.InitialisingAction;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.util.ExceptionDialog;

/**
 * This class is designed to implement stepwise execution on source line basis.
 * 
 * If the currently selected proof node is an open goal and getCodeLine returns
 * a value >= 0, the currently active strategy will be applied until all
 * children are either closed or getCodeLine returns a value different from the
 * initial one.
 */
public abstract class StepCodeAction extends BarAction implements
        PropertyChangeListener, InitialisingAction {
    
    private static final long serialVersionUID = 5444254542006126131L;

    public static class CodeLocation{
        public boolean isUnique;
        public int line;
        public Object program;

        public boolean equals(CodeLocation c) {
            return c.line == line && c.program.equals(program);
        }
    }

    private ProofNode selectedProofNode;

    public StepCodeAction(String name, Icon icon) {
        super(name, icon);
    }

    /**
     * Returns the first code location occurring on proof node.
     * 
     * @param node
     *            the node to be queried for code line information
     * 
     * @return null if no code location exists on the proof node; a valid code
     *         location else
     */
    protected abstract CodeLocation getCodeLocation(ProofNode node);

    @Override
    public void actionPerformed(ActionEvent e) {
        // has no effect on nodes with children
        if (null != selectedProofNode.getChildren())
            return; // if this effect is undesired, select the first open goal
                    // that has a line number

        CodeLocation loc = getCodeLocation(selectedProofNode);

        // you cannot step for a line, if you can't identify your line number
        // uniquely
        if (null == loc || !loc.isUnique)
            return;

        ProofCenter pc = getProofCenter();
        Strategy strategy = pc.getStrategyManager().getSelectedStrategy();
        Proof proof = pc.getProof();

        List<ProofNode> todo = new LinkedList<ProofNode>();
        todo.add(selectedProofNode);

        if (!proof.getLock().tryLock()) {
            ExceptionDialog.showExceptionDialog(getParentFrame(),
                    "Proof locked by another thread");
            return;
        }

        try {
            strategy.init(proof, pc.getEnvironment(), pc.getStrategyManager());
            strategy.beginSearch();

            ProofNode current = null;

            while (!todo.isEmpty()) {
                current = todo.remove(0);

                RuleApplication ra = strategy.findRuleApplication(current);

                if (ra != null) {
                    proof.apply(ra, pc.getEnvironment());
                    strategy.notifyRuleApplication(ra);

                    for (ProofNode node : current.getChildren()) {
                        CodeLocation next = getCodeLocation(node);
                        if (null == next || (next.equals(loc) && next.isUnique)) {
                            todo.add(node);
                        }
                    }
                } else
                    ExceptionDialog
                            .showExceptionDialog(getParentFrame(),
                                    "The currently selected proof strategy is to weak to do another step");
            }

            if (selectedProofNode.isClosed()) {
                if (proof.hasOpenGoals())
                    pc.fireSelectedProofNode(proof.getGoal(0));
                else
                    pc.fireSelectedProofNode(proof.getRoot());
            } else {
                // find first unclosed node
                current = selectedProofNode;
                while (current.getChildren() != null)
                    for (ProofNode child : current.getChildren())
                        if (!child.isClosed())
                            current = child;

                pc.fireSelectedProofNode(current);
            }

        } catch (Exception ex) {
            ExceptionDialog.showExceptionDialog(getParentFrame(), ex);
        } finally {
            strategy.endSearch();
            proof.getLock().unlock();
        }

    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (ProofCenter.SELECTED_PROOFNODE.equals(evt.getPropertyName()))
            selectedProofNode = (ProofNode) evt.getNewValue();
    }

    @Override
    public void initialised() {
        getProofCenter().addPropertyChangeListener(
                ProofCenter.SELECTED_PROOFNODE, this);
        selectedProofNode = getProofCenter().getProof().getRoot();
    }

}