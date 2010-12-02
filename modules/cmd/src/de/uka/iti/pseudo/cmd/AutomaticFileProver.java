/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.cmd;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import de.uka.iti.pseudo.auto.strategy.Strategy;
import de.uka.iti.pseudo.auto.strategy.StrategyException;
import de.uka.iti.pseudo.auto.strategy.StrategyManager;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentMaker;
import de.uka.iti.pseudo.environment.Program;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ParseException;
import de.uka.iti.pseudo.parser.Parser;
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
import de.uka.iti.pseudo.term.Type;
import de.uka.iti.pseudo.term.Update;
import de.uka.iti.pseudo.term.creation.DefaultTermVisitor;
import de.uka.iti.pseudo.term.creation.TermInstantiator;
import de.uka.iti.pseudo.term.statement.Statement;
import de.uka.iti.pseudo.util.TextInstantiator;
import de.uka.iti.pseudo.util.TimingOutTask;

/**
 * This class allows to run ivil automatically over one particular ivil input
 * file (which contains a problem description).
 * 
 * It implements the {@link Callable} interfance and, hence, can be used to
 * delegate the task to run several files to a task queue (see
 * {@link ExecutorService}).
 * 
 * The result is returned in form of a Result object which contains a reference
 * to the file, whether the run was successful and status messages. <b>Note:</b>
 * This will change later when the requirements to the data in the results is
 * clearer.
 * 
 * @see Result
 * @author mattias ulbrich
 */
public class AutomaticFileProver implements Callable<Result> {

    /**
     * The file under inspection
     */
    private File file;
    
    /**
     * The environment extracted from the file.
     */
    private Environment env;
    
    /**
     * The problem term extracted from the file.
     */
    private Term problemTerm;
    
    /**
     * The timeout after which the search will be given up.
     */
    private int timeout = -1;
    
    /**
     * Relay error messages to source files.
     * (will disappear when result is more elaborate)
     */
    private boolean relayToSource;
    
    /**
     * Needed for visitation of sequents to detect modalities
     */
    private LiteralProgramTerm detectedProgramTerm;
    
    /**
     * Pretty printer for the environment
     */
    private PrettyPrint prettyPrint;
    
    /**
     * Visitor to detect program terms anywhere in a term
     */
    private TermVisitor programDetector = 
        new DefaultTermVisitor.DepthTermVisitor() {
        public void visit(LiteralProgramTerm literalProgramTerm) {
            detectedProgramTerm = literalProgramTerm;
        };
    };
    
    /**
     * returns the timeout set for this prover.
     * -1 means no timeout.
     * 
     * @return the timeout in milliseconds
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * the timeout set for this prover.
     * -1 means no timeout. A positive value a time span in seconds.
     * 
     * @param timeout the timeout to set
     */
    public void setTimeout(int timeout) {
        assert timeout == -1 || timeout > 0 : timeout;
        this.timeout = timeout;
    }

    /**
     * Instantiates a new automatic file prover.
     * 
     * @param file
     *            an ivil input file
     * 
     * @throws ParseException
     *             if parsing fails
     * @throws ASTVisitException
     *             if the semantic analysis fails
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public AutomaticFileProver(File file) throws ParseException, ASTVisitException, IOException {
        
        this.file = file;

        Parser parser = new Parser();
        EnvironmentMaker em = new EnvironmentMaker(parser, file);
        env = em.getEnvironment();
        problemTerm = em.getProblemTerm();
        
        prettyPrint = new PrettyPrint(env);
        
        assert problemTerm == null ||
            problemTerm.getType().equals(Environment.getBoolType());
        
    }

    /**
     * {@inheritDoc}
     * 
     * does the actual job.
     */
    @Override
    public Result call() throws TermException, StrategyException, ProofException {
        
        
        Proof proof = new Proof(problemTerm);
        
        StrategyManager strategyManager = new StrategyManager(proof, env);
        strategyManager.registerAllKnownStrategies();
        Strategy strategy = strategyManager.getSelectedStrategy();

        assert strategy != null;
        
        TimingOutTask timingOut = null;
        if(timeout > 0) {
            timingOut = new TimingOutTask(timeout * 1000);
            timingOut.schedule();
        }
        
        try {
            strategy.beginSearch();

            while(true) {

                if(Thread.interrupted() || (timingOut != null && timingOut.hasFinished())) {
                    return new Result(false, file, "timed out");
                }

                RuleApplication ruleApp = strategy.findRuleApplication();

                if(ruleApp == null) {
                    break;
                }

                proof.apply(ruleApp, env);
            }

            List<ProofNode> openGoals = proof.getOpenGoals();

            if(openGoals.isEmpty()) {
                // if(export) exportProof(proof);
                return new Result(true, file);
            }

            if(!relayToSource) {
                return new Result(false, file, 
                        openGoals.size() + " remaining open goal(s)");
            }

            ArrayList<String> messages = makeDetailedReport(openGoals);

            return new Result(false, file, messages);
        } finally {
            if(timingOut != null)
                timingOut.cancel();
            // clear the interruption flag of the thread in case it has appeared in the meantime 
            Thread.interrupted();
        }
    }

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
            LiteralProgramTerm pt = findProgramTerm(goal);
            while(pt == null && goal != null) {
                last = goal;
                goal = goal.getParent();
                pt = findProgramTerm(goal);
            }

            if(pt != null) {
                
                // the number of the branch
                
                int index = pt.getProgramIndex();
                Program program = pt.getProgram();

                Statement statement = program.getStatement(index);
                String annotation = program.getTextAnnotation(index);
                URL sourceFile = program.getSourceFile();
                int sourceLine = statement.getSourceLineNumber();

                StringBuilder msg = new StringBuilder();
                msg.append(sourceFile).append(":").append(sourceLine)
                        .append(":\n").append(
                                "   statement: "
                                        + prettyPrint.print(statement)
                                        + "\n");
                
                if (annotation != null) {
                    msg.append("   annotation: " + annotation + "\n");
                }
                
                if(last != null) {
                    int childIndex = goal.getChildren().indexOf(last);
                    assert childIndex >= 0;
                    String branchName = getBranch(goal, childIndex);
                    if (branchName != null) {
                        msg.append("   branch: ").append(
                                instantiateString(goal.getAppliedRuleApp(),
                                        branchName)).append("\n");
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
        if(childIndex == -1)
            return null;
        
        Rule rule = goal.getAppliedRuleApp().getRule();
        GoalAction action = rule.getGoalActions().get(childIndex);
        return action.getName();
    }
    
    /*
     * instantiate the schema variables in a string.
     * @see gui 
     * 
     */
    private String instantiateString(RuleApplication ruleApp, String string) {
        TextInstantiator textInst = new TextInstantiator(ruleApp);
        return textInst.replaceInString(string, prettyPrint);
    }

    /**
     * check whether this prover relates error messages to source code
     * 
     * @return <code>true</code> if it relates error messages to source code.
     */
    public boolean isRelayToSource() {
        return relayToSource;
    }

    /**
     * set whether this prover relates error messages to source code
     * 
     * @param value to set
     */
    public void setRelayToSource(boolean relayToSource) {
        this.relayToSource = relayToSource;
    }

    /**
     * Find a program term in a proof node.
     * 
     * @param node
     *            the proof node whose  sequent is to be inspected
     * 
     * @return a literal program term or null, if none found
     */
    private LiteralProgramTerm findProgramTerm(ProofNode node) {
        
        Sequent sequent = node.getSequent();
        detectedProgramTerm = null;
        
        try {
            for (Term term : sequent.getAntecedent()) {
                term.visit(programDetector);
                if(detectedProgramTerm != null)
                    return detectedProgramTerm;
            }
            
            for (Term term : sequent.getSuccedent()) {
                term.visit(programDetector );
                if(detectedProgramTerm != null)
                    return detectedProgramTerm;
            }
        } catch (TermException e) {
            // never thrown;
            throw new Error(e);
        }
        
        return null;
    }

    /**
     * Checks whether the file contains a problem description.
     * 
     * @return true, if there is a problem defined in {@link #file}.
     */
    public boolean hasProblem() {
        return problemTerm != null;
    }

}
