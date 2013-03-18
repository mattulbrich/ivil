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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

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
import de.uka.iti.pseudo.util.Log;
import de.uka.iti.pseudo.util.Pair;

/**
 * The super class to the smt actions.
 *
 * @author mattias ulbrich
 */
public class SMTAction extends BarAction implements InitialisingAction, PropertyChangeListener {

    /**
     * The solver used to determine the status. Retrieved from the rule named
     * {@value #CLOSE_RULE_NAME} using the key
     * {@link AskDecisionProcedure#KEY_DECISION_PROCEDURE}
     */
    private DecisionProcedure solver;

    /**
     * The timeout and other information to be used by the solver.
     *
     * Retrieved from the rule named {@value #CLOSE_RULE_NAME}
     */
    private Map<String, String> ruleProperties;

    /**
     * Cache to remember solvability of sequents.
     *
     * We use a weak hash map to allow freeing if space is needed.
     */
    private final Map<Sequent, Boolean> sequentStatus =
            Collections.synchronizedMap(new WeakHashMap<Sequent, Boolean>());

    /**
     * The proof element we are working on.
     */
    protected Proof proof;

    /**
     * The environment is needed to provide the rules.
     */
    private Environment env;

    /**
     * The rule to close by Z3.
     */
    private Rule closeRule;

    /**
     * The rule to be called to close goals with
     */
    private final String closeRuleName;

    /**
     * Instantiates a new AMT action.
     *
     * @param closeRuleName the name of the close rule
     */
    public SMTAction(String closeRuleName) {
        this.closeRuleName = closeRuleName;
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

        closeRule = env.getRule(closeRuleName);
        if (closeRule != null) {
            try {
                String proc = closeRule.getProperty(RuleTagConstants.KEY_DECISION_PROCEDURE);
                solver = env.getPluginManager().getPlugin(DecisionProcedure.SERVICE_NAME,
                        DecisionProcedure.class, proc);
                ruleProperties = closeRule.getProperties();
            } catch (Exception ex) {
                Log.log(Log.WARNING, "Cannot instantiate background decision procedure");
                ex.printStackTrace();
                closeRule = null;
            }
        }

        setEnabled(closeRule != null);
    }

    /**
     * get the rule which is responsible for doing the closing
     *
     * @return a rule of the environment
     */
    public Rule getCloseRule() {
        return closeRule;
    }

    /**
     * Asks the cache on the result of a sequent.
     *
     * @param sequent
     *            the sequent to be queried for
     * @return <code>null</code> for unknown, <code>true</code> for provable and
     *         <code>false</code> for not proveable
     */
    public @Nullable Boolean getStatus(Sequent sequent) {
        return sequentStatus.get(sequent);
    }

    /*
     * switch the button off when in proof elsewhere according to the settings,
     * activate or deactivate the background thread
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (ProofCenter.ONGOING_PROOF.equals(evt.getPropertyName())) {
            setEnabled(!(Boolean) evt.getNewValue() && solver != null);
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

    /**
     * The list of provable nodes. The default implementation returns the empty
     * list.
     *
     * @return The list of nodes which are known to be provable.
     */
    public List<ProofNode> getProvableNodes() {
        return Collections.emptyList();
    }

    /**
     * Checks if a node is provable.
     *
     * Runs the solver
     *
     * @param pn the proof node to check
     * @return <code>true</code>, iff is provable
     * @throws ProofException thrown by the solver
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws InterruptedException the interrupted exception
     */
    public boolean isProvable(ProofNode pn)
            throws ProofException, IOException, InterruptedException {
        Sequent sequent = pn.getSequent();
        Boolean cached = sequentStatus.get(sequent);
        if (cached != null) {
            Log.log(Log.VERBOSE, "Provability cache hit for " + pn + ": " + cached);
            return cached.booleanValue();
        } else {
            Pair<Result, String> result = solver.solve(sequent, env, ruleProperties);
            boolean proveable = result.fst() == Result.VALID;
            sequentStatus.put(sequent, proveable);
            Log.log(Log.VERBOSE, "Provability result for " + pn + ": " + result);
            return proveable;
        }
    }

    /**
     * Gets the title to be used for the progress window
     *
     * @return the window title
     */
    public String getWindowTitle() {
        return "";
    }


}
