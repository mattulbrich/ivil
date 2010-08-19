package de.uka.iti.pseudo.cmd;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import de.uka.iti.pseudo.auto.strategy.Strategy;
import de.uka.iti.pseudo.auto.strategy.StrategyManager;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentMaker;
import de.uka.iti.pseudo.environment.Program;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ParseException;
import de.uka.iti.pseudo.parser.Parser;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.TermVisitor;
import de.uka.iti.pseudo.term.creation.DefaultTermVisitor;
import de.uka.iti.pseudo.term.statement.Statement;
import de.uka.iti.pseudo.util.TimingOutTask;

public class AutomaticFileProver implements Callable<Result> {

    private File file;
    private Environment env;
    private Term problemTerm;
    private int timeout;
    private boolean relayToSource;
    private LiteralProgramTerm detectedProgramTerm;
    private TermVisitor programDetector = 
        new DefaultTermVisitor.DepthTermVisitor() {
        public void visit(LiteralProgramTerm literalProgramTerm) {
            detectedProgramTerm = literalProgramTerm;
        };
    };
    
    /**
     * @return the timeout
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * @param timeout the timeout to set
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public AutomaticFileProver(File file) throws ParseException, ASTVisitException, IOException {
        // TODO Auto-generated constructor stub
        
        this.file = file;

        Parser parser = new Parser();
        EnvironmentMaker em = new EnvironmentMaker(parser, file);
        env = em.getEnvironment();
        problemTerm = em.getProblemTerm();
        
        assert problemTerm == null ||
            problemTerm.getType().equals(Environment.getBoolType());
        
    }

    @Override
    public Result call() throws Exception {
        
        Proof proof = new Proof(problemTerm);
        
        StrategyManager strategyManager = new StrategyManager(proof, env);
        strategyManager.registerAllKnownStrategies();
        Strategy strategy = strategyManager.getSelectedStrategy();

        assert strategy != null;
        
        TimingOutTask timingOut = new TimingOutTask(timeout);
        timingOut.schedule();
        
        strategy.beginSearch();
        
        while(true) {
            
            if(Thread.interrupted() || timingOut.hasFinished()) {
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
        
        ArrayList<String> messages = new ArrayList<String>();
        for (ProofNode goal : openGoals) {
           
           LiteralProgramTerm pt = findProgramTerm(goal);
           while(pt == null && goal != null) {
               goal = goal.getParent();
               pt = findProgramTerm(goal);
           }
           
           if(pt != null) {
               int index = pt.getProgramIndex();
               Program program = pt.getProgram();
               
               Statement statement = program.getStatement(index);
               String annotation = program.getTextAnnotation(index);
               URL sourceFile = program.getSourceFile();
               int sourceLine = statement.getSourceLineNumber();
               
               String msg = annotation == null ? 
                       " statement: " + statement.toString() :
                       annotation;
               
               msg = sourceFile + ":" + sourceLine + ": " + msg;
               messages.add(msg);
           } else {
               messages.add("open goal w/o source reference");
           }
        }
        
        return new Result(false, file, messages);
    }

    /**
     * @return the relayToSource
     */
    public boolean isRelayToSource() {
        return relayToSource;
    }

    /**
     * @param relayToSource the relayToSource to set
     */
    public void setRelayToSource(boolean relayToSource) {
        this.relayToSource = relayToSource;
    }

    private LiteralProgramTerm findProgramTerm(ProofNode goal) {
        
        Sequent sequent = goal.getSequent();
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

    public boolean hasProblem() {
        return problemTerm != null;
    }

}
