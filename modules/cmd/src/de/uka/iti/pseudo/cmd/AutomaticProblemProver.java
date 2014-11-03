/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */

package de.uka.iti.pseudo.cmd;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import nonnull.NonNull;
import de.uka.iti.pseudo.auto.script.ProofScript;
import de.uka.iti.pseudo.auto.script.ProofScriptCommand;
import de.uka.iti.pseudo.auto.script.ProofScriptNode;
import de.uka.iti.pseudo.auto.strategy.Strategy;
import de.uka.iti.pseudo.auto.strategy.StrategyException;
import de.uka.iti.pseudo.auto.strategy.StrategyManager;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.Program;
import de.uka.iti.pseudo.environment.ProofObligation;
import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.prettyprint.PrettyPrint;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.rule.GoalAction;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.TermVisitor;
import de.uka.iti.pseudo.term.creation.DefaultTermVisitor;
import de.uka.iti.pseudo.term.statement.Statement;
import de.uka.iti.pseudo.util.Log;
import de.uka.iti.pseudo.util.TextInstantiator;
import de.uka.iti.pseudo.util.TimingOutTask;

/**
 * This class allows running ivil automatically over one particular ivil problem
 * taken from a file (which contains the problem description).
 *
 * It is usually created by a {@link FileProblemProverBuilder}.
 *
 * It implements the {@link Callable} interface and, hence, can be used to
 * delegate the task to run several files to a task queue (see
 * {@link ExecutorService}).
 *
 * The result is returned in form of a Result object which contains a reference
 * to the file, whether the run was successful and status messages. <b>Note:</b>
 * This will change later when the requirements to the data in the results is
 * clearer.
 *
 * @see FileProblemProverBuilder
 * @see Result
 * @author mattias ulbrich
 */
public class AutomaticProblemProver implements Callable<Result> {

    /**
     * The file under inspection
     */
    private final File file;

    /**
     * The environment extracted from {@link #file}.
     */
    private Environment env;

    /**
     * The sequent of the problem under inspection
     */

    /**
     * The name of the problem under inspection
     */

    /**
     * Relay error messages to source files. (will disappear when result is more
     * elaborate)
     */
    private final boolean relayToSource;

    /**
     * The timeout (in seconds) after which the search will be given up.
     */
    private int timeout = -1;

    /**
     * The rule applications limit after which the search will be given up.
     */
    private int ruleApplicationLimit = 0;

    /**
     * Needed for visitation of sequents to detect modalities
     */
    private LiteralProgramTerm detectedProgramTerm;

    /**
     * Pretty printer for the environment.
     */
    private PrettyPrint prettyPrint;

    /**
     * Visitor to detect program terms anywhere in a term
     */
    private final TermVisitor programDetector = new DefaultTermVisitor.DepthTermVisitor() {
        @Override
        public void visit(LiteralProgramTerm literalProgramTerm) {
            detectedProgramTerm = literalProgramTerm;
        };
    };

    private final ProofObligation proofObligation;

    /**
     * Make a detailed report on the result.
     *
     * This will change when the information to be stored in result is clearer
     *
     * @param openGoals
     *            the goals left open after the run
     *
     * @return a list of string messages.
     */
    private ArrayList<String> makeDetailedReport(List<ProofNode> openGoals) {
        ArrayList<String> messages = new ArrayList<String>();
        for (ProofNode goal : openGoals) {

            ProofNode last = null;
            LiteralProgramTerm pt = null;
            while (pt == null && goal != null) {
                pt = findProgramTerm(goal);
                last = goal;
                goal = goal.getParent();
            }

            if (pt != null) {

                // the number of the branch

                int index = pt.getProgramIndex();
                Program program = pt.getProgram();

                Statement statement = program.getStatement(index);
                String annotation = program.getTextAnnotation(index);
                URL sourceFile = program.getSourceFile();
                int sourceLine = statement.getSourceLineNumber();

                StringBuilder msg = new StringBuilder();
                msg.append(sourceFile)
                        .append(":")
                        .append(sourceLine)
                        .append(":\n")
                        .append("   statement: " + prettyPrint.print(statement)
                                + "\n");

                if (annotation != null) {
                    msg.append("   annotation: " + annotation + "\n");
                }

                if (last != null) {
                    int childIndex = goal.getChildren().indexOf(last);
                    assert childIndex >= 0;
                    String branchName = getBranch(goal, childIndex);
                    if (branchName != null) {
                        msg.append("   branch: ")
                                .append(instantiateString(
                                        goal.getAppliedRuleApp(), branchName))
                                .append("\n");
                    }
                }

                messages.add(msg.toString());

            } else {
                messages.add("open goal w/o source reference");
            }
        }
        return messages;
    }

    /**
     * Find a program term in a proof node.
     *
     * @param node
     *            the proof node whose sequent is to be inspected
     *
     * @return a literal program term or null, if none found
     */
    private LiteralProgramTerm findProgramTerm(ProofNode node) {

        Sequent sequent = node.getSequent();
        detectedProgramTerm = null;

        try {
            for (Term term : sequent.getAntecedent()) {
                term.visit(programDetector);
                if (detectedProgramTerm != null) {
                    return detectedProgramTerm;
                }
            }

            for (Term term : sequent.getSuccedent()) {
                term.visit(programDetector);
                if (detectedProgramTerm != null) {
                    return detectedProgramTerm;
                }
            }
        } catch (TermException e) {
            // never thrown;
            throw new Error(e);
        }

        return null;
    }

    /*
     * instantiate the schema variables in a string.
     *
     * @see gui
     */
    private String instantiateString(RuleApplication ruleApp, String string) {
        TextInstantiator textInst = new TextInstantiator(ruleApp);
        return textInst.replaceInString(string, prettyPrint);
    }

    /**
     * Gets the name of branch at an index.
     *
     * The applied rule is inspected for that
     *
     * @param goal
     *            the goal
     * @param childIndex
     *            the child index
     *
     * @return the name of the branch
     */
    private String getBranch(ProofNode goal, int childIndex) {
        if (childIndex == -1) {
            return null;
        }

        Rule rule = goal.getAppliedRuleApp().getRule();
        GoalAction action = rule.getGoalActions().get(childIndex);
        return action.getName();
    }



    /*
     * @param file
     * @param env
     * @param prettyPrint
     * @param name
     * @param problemSequent
     * @param relayToSource
     * @param timeout
     * @param ruleApplicationLimit
     */


    /**
     * Instantiates a new automatic problem prover from the given parameters.
     *
     * @param file
     *            the file from which the problem comes
     * @param env
     *            the environment
     * @param prettyPrint
     *            the pretty printer to use
     * @param name
     *            the name of the problem
     * @param problemSequent
     *            the problem sequent
     * @param relayToSource
     *            should reporting relay to source code?
     * @param timeout
     *            the timeout in seconds
     * @param ruleApplicationLimit
     *            the rule application limit
     * @throws EnvironmentException
     */
    public AutomaticProblemProver(@NonNull File file,
            @NonNull ProofObligation proofObligation,
            boolean relayToSource, int timeout, int ruleApplicationLimit) {
        super();
        this.file = file;
        this.proofObligation = proofObligation;
        this.relayToSource = relayToSource;
        this.timeout = timeout;
        this.ruleApplicationLimit = ruleApplicationLimit;
    }

    /**
     * {@inheritDoc}
     *
     * does the actual job.
     */
    @Override
    public Result call() throws TermException, StrategyException,
            ProofException, EnvironmentException {

        Proof proof = proofObligation.initProof();
        String name = proofObligation.getName();

        env = proof.getEnvironment();
        prettyPrint = new PrettyPrint(env);
        ProofScript proofScript = proofObligation.getProofScript();

        if(proofScript == null) {
            proofScript = makeDefaultProofScript(name);
        }


        StrategyManager strategyManager = new StrategyManager(proof, env);
        strategyManager.registerAllKnownStrategies();
        Strategy strategy = strategyManager.getSelectedStrategy();

        assert strategy != null;

        TimingOutTask timingOut = null;
        if (timeout > 0) {
            timingOut = new TimingOutTask(timeout * 1000);
            timingOut.schedule();
        }

        try {
            strategy.beginSearch();

            for (int count = 0;; count++) {

                if (Thread.interrupted()
                        || (timingOut != null && timingOut.hasFinished())) {
                    return new Result(false, file, name, "timed out");
                }

                if (ruleApplicationLimit != 0 && ruleApplicationLimit < count) {
                    return new Result(false, file, name, "rule application limit reached");
                }

                RuleApplication ruleApp;
                try {
                    ruleApp = strategy.findRuleApplication();
                } catch (InterruptedException e) {
                    Log.log(Log.DEBUG, "Automatic prove search has been interrupted");
                    return new Result(false, file, name, "timed out");
                }

                if (ruleApp == null) {
                    break;
                }

                proof.apply(ruleApp);
                strategy.notifyRuleApplication(ruleApp);
            }

            List<ProofNode> openGoals = proof.getOpenGoals();

            // TODO incorporate name into the result

            if (openGoals.isEmpty()) {
                // if(export) exportProof(proof);
                return new Result(true, file, name);
            }

            if (!relayToSource) {
                return new Result(false, file, name, openGoals.size()
                        + " remaining open goal(s)");
            }

            ArrayList<String> messages = makeDetailedReport(openGoals);

            return new Result(false, file, name, messages);
        } finally {
            if (timingOut != null) {
                timingOut.cancel();
            }
            // clear the interruption flag of the thread in case it has appeared
            // in the meantime
            Thread.interrupted();
        }

    }

    private ProofScript makeDefaultProofScript(String name) throws EnvironmentException {

        ProofScriptNode psn = new ProofScriptNode(
                ProofScriptCommand.AUTO_COMMAND,
                Collections.<String, String>emptyMap(),
                Collections.<ProofScriptNode>emptyList(),
                new ASTLocatedElement.Fixed("Auto-proof for " + name));

        ProofScript ps = new ProofScript(name, psn);

        return ps;
    }

    /**
     * check whether this prover relates error messages to source code
     *
     * @return <code>true</code> if it relates error messages to source code.
     */
    public boolean isRelayToSource() {
        return relayToSource;
    }


}
