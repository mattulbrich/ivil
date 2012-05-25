/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.gui.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.swing.Icon;
import javax.swing.SwingUtilities;

import nonnull.Nullable;
import de.uka.iti.pseudo.auto.DecisionProcedure;
import de.uka.iti.pseudo.auto.DecisionProcedure.Result;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.gui.ProofCenter;
import de.uka.iti.pseudo.gui.actions.BarManager.InitialisingAction;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.rule.RuleTagConstants;
import de.uka.iti.pseudo.rule.where.AskDecisionProcedure;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.util.GUIUtil;
import de.uka.iti.pseudo.util.Log;
import de.uka.iti.pseudo.util.NotificationEvent;
import de.uka.iti.pseudo.util.NotificationListener;
import de.uka.iti.pseudo.util.Pair;

// Class is final because thread is started in constructor which is evil
// for subclassing.
/**
 * This is the action which is on the SMT button.
 *
 * Pressing it while flashing will close all nodes, that are known to be
 * closable. Pressing it while not flashing will cause it to feed all open goals
 * to the SMT solver.
 */
@SuppressWarnings("serial")
public final class SMTBackgroundAction extends BarAction implements InitialisingAction, PropertyChangeListener,
        NotificationListener {

    /**
     * The property on ProofCenter that will be used to decide whether or not to
     * close the window after completion.
     */
    public static final String SMT_KEEPWINDOWOPEN_PROPERTY = "pseudo.smt.keepwindowopen";

    /**
     * The rule to be called to close goals with
     */
    private static final String CLOSE_RULE_NAME = "auto_smt_close";

    /**
     * The solver used to determine the status. Retrieved from the rule named
     * {@value #CLOSE_RULE_NAME} using the key
     * {@link AskDecisionProcedure#KEY_DECISION_PROCEDURE}
     */
    private DecisionProcedure solver;

    /**
     * The timeout to be used by the solver. Retrieved from the rule named
     * {@value #CLOSE_RULE_NAME} using the key
     * {@link AskDecisionProcedure#KEY_TIMEOUT}
     */
    private int timeout;

    /**
     * Cache to remember solvability of sequents.
     *
     * We use a weak hash map to allow freeing if space is needed.
     */
    private final Map<Sequent, Boolean> sequentStatus =
            Collections.synchronizedMap(new WeakHashMap<Sequent, Boolean>());

    /**
     * The nodes for which we that they can be proven using Z3.
     */
    private final List<ProofNode> provableNodes =
            Collections.synchronizedList(new LinkedList<ProofNode>());

    /**
     * The proof element we are working on.
     */
    private Proof proof;

    /**
     * The environment is needed to provide the rules.
     */
    private Environment env;

    /**
     * image resources.
     */
    private final Icon noflashImg;
    private final Icon flashImg;

    /**
     * This action can be made inactive
     */
    private boolean backgroundActive;

    /**
     * The rule to close by Z3.
     */
    private Rule closeRule;

    private SMTBackgroundThread thread;

    /**
     * Tooltip iff flashing
     */
    private static final String TOOLTIP_FLASHING =
            "Some goals can be closed by the SMT solver. Close them!";

    /**
     * Tooltip iff not flashing
     */
    private static final String TOOLTIP_NOT_FLASHING =
            "Run the STM solver on all open goals.";

    /*
     * Instantiates a new SMT background action.
     */
    public SMTBackgroundAction() {
        // make images and set the non-flashing one
        noflashImg = GUIUtil.makeIcon(getClass().getResource("img/smt.gif"));
        flashImg = GUIUtil.makeIcon(getClass().getResource("img/smt_flash.gif"));
        setFlashing(false);

        // we will set us enabled after initialisation
        setEnabled(false);
    }

    /*
     * retrieve the environment and read from it the necessary information, such
     * as the rule to apply, and the solver to use.
     */
    @Override
    public void initialised() {
        ProofCenter proofCenter = getProofCenter();

        proof = proofCenter.getProof();

        env = proofCenter.getEnvironment();
        proofCenter.addPropertyChangeListener(ProofCenter.ONGOING_PROOF, this);
        proofCenter.addPropertyChangeListener(SMTBackgroundThread.SMT_BACKGROUND_PROPERTY, this);
        proofCenter.addNotificationListener(ProofCenter.PROOFTREE_HAS_CHANGED, this);

        // Start bg process
        this.thread = new SMTBackgroundThread(this, proofCenter);
        thread.start();

        setBackgroundActive((Boolean) proofCenter.getProperty(SMTBackgroundThread.SMT_BACKGROUND_PROPERTY));

        closeRule = env.getRule(CLOSE_RULE_NAME);
        if (closeRule != null) {
            try {
                String className = closeRule.getProperty(RuleTagConstants.KEY_DECISION_PROCEDURE);
                solver = (DecisionProcedure) Class.forName(className).newInstance();
                timeout = Integer.parseInt(closeRule.getProperty(RuleTagConstants.KEY_TIMEOUT));
            } catch (Exception ex) {
                Log.log(Log.WARNING, "Cannot instantiate background decision procedure");
                ex.printStackTrace();
                closeRule = null;
            }
        }

        setEnabled(closeRule != null);

    }

    public Rule getCloseRule() {
        return closeRule;
    }

    /*
     * switch the button off when in proof elsewhere according to the settings,
     * activate or deactivate the background thread
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (ProofCenter.ONGOING_PROOF.equals(evt.getPropertyName())) {
            setEnabled(!(Boolean) evt.getNewValue() && solver != null);
        } else

        if (SMTBackgroundThread.SMT_BACKGROUND_PROPERTY.equals(evt.getPropertyName())) {
            setBackgroundActive((Boolean) evt.getNewValue());
        } else

        {
            assert false : "Case distinction failed";
        }
    }

    /*
     * Sets the background thread active or not.
     */
    private void setBackgroundActive(boolean act) {
        this.backgroundActive = act;
        if (backgroundActive) {
            thread.notifyContinue(proof.getOpenGoals());
        }
    }


    /*
     * flashing or non-flashing icon and change tooltip
     */
    private void setFlashing(boolean flashing) {
        setIcon(flashing ? flashImg : noflashImg);
        putValue(SHORT_DESCRIPTION, flashing ? TOOLTIP_FLASHING : TOOLTIP_NOT_FLASHING);
    }

    public boolean isProvable(ProofNode pn) throws ProofException, IOException {
        Sequent sequent = pn.getSequent();
        Boolean cached = sequentStatus.get(sequent);
        if (cached != null) {
            Log.log(Log.VERBOSE, "Provability cache hit for " + pn + ": " + cached);
            return cached.booleanValue();
        } else {
            Pair<Result, String> result = solver.solve(sequent, env, timeout);
            boolean proveable = result.fst() == Result.VALID;
            sequentStatus.put(sequent, proveable);
            Log.log(Log.VERBOSE, "Provability result for " + pn + ": " + result);
            return proveable;
        }
    }

    /*
     * Delegate the actual proving to a SwingWorker.
     * Set into ongoing proof mode beforehand.
     */
    @Override
    public void actionPerformed(ActionEvent actionEvt) {
        getProofCenter().firePropertyChange(ProofCenter.ONGOING_PROOF, true);
        SMTBackgroundWorker worker = new SMTBackgroundWorker(this, getProofCenter());
        worker.execute();
    }

    public boolean isBackgroundActive() {
        return backgroundActive;
    }

    public void addProvableNode(ProofNode pn) {
        provableNodes.add(pn);
        // TODO should happen on EventQueue
        setFlashing(true);
    }

    /**
     * the proof object has changed. change our structures accordingly:
     * <ul>
     * <li>remove nodes from provable if no longer a goal
     * <li>set jobs to all newly open goals
     * </ul>
     */
    @Override
    public void handleNotification(NotificationEvent event) {
        assert SwingUtilities.isEventDispatchThread();

        if (event.isSignal(ProofCenter.PROOFTREE_HAS_CHANGED)) {
            // no update while in automatic proof
            if ((Boolean) getProofCenter().getProperty(ProofCenter.ONGOING_PROOF)) {
                return;
            }

            List<ProofNode> openGoals = getProofCenter().getProof().getOpenGoals();
            provableNodes.retainAll(openGoals);

            setFlashing(!provableNodes.isEmpty());

            thread.notifyContinue(openGoals);
        }
    }

    public List<ProofNode> getProvableNodes() {
        return provableNodes;
    }

    public @Nullable Boolean getStatus(Sequent sequent) {
        return sequentStatus.get(sequent);
    }

}


