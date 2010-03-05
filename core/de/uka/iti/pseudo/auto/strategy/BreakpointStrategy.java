/*
 * This file is part of PSEUDO
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.auto.strategy;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.Function;
import de.uka.iti.pseudo.environment.NumberLiteral;
import de.uka.iti.pseudo.environment.Program;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.proof.RuleApplicationFilter;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.term.Application;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.statement.GotoStatement;
import de.uka.iti.pseudo.term.statement.SkipStatement;
import de.uka.iti.pseudo.term.statement.Statement;

public class BreakpointStrategy extends AbstractStrategy implements RuleApplicationFilter {
    
    private BreakpointManager breakPointManager = new BreakpointManager();
    
    /**
     * The set of rules which we do consult
     */
    private static final String REWRITE_CATEGORY = "symbex";
    
    /*
     * The configurable properties
     */
    private boolean obeyProgramBreakpoints = true;
    private boolean obeySourceBreakpoints = true;
    private boolean stopAtSkip = false;
    private boolean stopAtLoop = false;
    private boolean stopAtJumpBack = true;

    private RewriteRuleCollection ruleCollection;
    
    private Set<LiteralProgramTerm> seenProgramTerms = new HashSet<LiteralProgramTerm>();
    
    @Override 
    public void init(Proof proof, Environment env, StrategyManager strategyManager)
            throws StrategyException {
        super.init(proof, env, strategyManager);
        try {
            ruleCollection = new RewriteRuleCollection(env.getAllRules(), REWRITE_CATEGORY, env);
            ruleCollection.setApplicationFilter(this);
        } catch (RuleException e) {
            throw new StrategyException("Cannot initialise BreakpointStrategy", e);
        }
    }

    @Override 
    public RuleApplication findRuleApplication(int goalNumber) {
        RuleApplication ra = ruleCollection.findRuleApplication(getProof(), goalNumber);
        return ra;
    }
    
    private boolean hasBreakpoint(LiteralProgramTerm progTerm) {
        //
        // check for stop at skip
        if(stopAtSkip) {
            Statement s = progTerm.getStatement();
            if(s instanceof SkipStatement)
                return true;
        }
        
        //
        // check for backward jumping
        if(stopAtJumpBack) {
            Statement s = progTerm.getStatement();
            if (s instanceof GotoStatement) {
                GotoStatement gotoStm = (GotoStatement) s;
                int currentIndex = progTerm.getProgramIndex();
                for (Term target : gotoStm.getSubterms()) {
                    int index = toInt(target);
                    if(index <= currentIndex)
                        return true;
                }
            }
        }
        
        //
        // check for unwanted looping
        if(stopAtLoop) {
            if(seenProgramTerms.contains(progTerm))
                return true;
        }
        seenProgramTerms.add(progTerm);
        
        Program program = progTerm.getProgram();
        int number = progTerm.getProgramIndex();

        //
        // check for program breakpoint
        if(obeyProgramBreakpoints) {
            if(breakPointManager.hasBreakpoint(program, number))
                return true;
        }

        //
        // check for source breakpoint:
        
        // we store in differs whether the statement in front of the current 
        // has a different source line number. only then the breakpoint is hit.
        File sourceFile = program.getSourceFile();
        if(sourceFile != null && obeySourceBreakpoints) {
            int sourceline = progTerm.getStatement().getSourceLineNumber();
            boolean differs = number == 0
                    || program.getStatement(number - 1).getSourceLineNumber() != sourceline;
            
            // bugfix: this "-1" is needed because line numbers actually start at 1
            if(differs && breakPointManager.hasBreakpoint(sourceFile, sourceline - 1))
                return true;
        }
        
        return false;
    }

    /**
     * Make integer from term.
     * 
     * @throws TermException
     *             if the term is not a number literal
     */
    private int toInt(Term term) {
        if (term instanceof Application) {
            Application appl = (Application) term;
            Function f = appl.getFunction();
            if (f instanceof NumberLiteral) {
                NumberLiteral literal = (NumberLiteral) f;
                return literal.getValue().intValue();
            }
        }
        throw new RuntimeException("The term " + term + " is not a number literal");
    }

    public BreakpointManager getBreakpointManager() {
        return breakPointManager;   
    }
   
    @Override 
    public String toString() {
        return "Breakpoint Strategy";
    }

    //
    // getter and setter
    //
    
    
    // due to ParameterSheet, we need get instead of is
    public boolean getObeyProgramBreakpoints() {
        return obeyProgramBreakpoints;
    }

    public void setObeyProgramBreakpoints(boolean obeyProgramBreakpoints) {
        this.obeyProgramBreakpoints = obeyProgramBreakpoints;
    }

    // due to ParameterSheet, we need get instead of is
    public boolean getObeySourceBreakpoints() {
        return obeySourceBreakpoints;
    }

    public void setObeySourceBreakpoints(boolean obeySourceBreakpoints) {
        this.obeySourceBreakpoints = obeySourceBreakpoints;
    }
    
    // due to ParameterSheet, we need get instead of is
    public boolean getStopAtSkip() {
        return stopAtSkip;
    }
    
    public void setStopAtSkip(boolean stopAtSkip) {
        this.stopAtSkip = stopAtSkip;
    }
    
    // due to ParameterSheet, we need get instead of is
    public boolean getStopAtLoop() {
        return stopAtLoop;
    }

    public void setStopAtLoop(boolean stopAtLoop) {
        this.stopAtLoop = stopAtLoop;
    }
    
    // due to ParameterSheet, we need get instead of is
    public boolean getStopAtJumpBack() {
        return stopAtJumpBack;
    }

    public void setStopAtJumpBack(boolean stopAtJumpBack) {
        this.stopAtJumpBack = stopAtJumpBack;
    }

    
    /**
     * Decide whether a rule application is to be applied or not.
     * 
     * We extract the program term and check whether it is at a 
     * breakpoint using {@link #hasBreakpoint(LiteralProgramTerm)}.
     * 
     * @return <code>false</code> iff at a breakpoint 
     */
    @Override 
    public boolean accepts(RuleApplication ruleApp) throws RuleException {
        int goal = ruleApp.getGoalNumber();
        Sequent sequent = getProof().getGoal(goal).getSequent();
        Term find;
        try {
            find = ruleApp.getFindSelector().selectSubterm(sequent);
        } catch (ProofException e) {
            throw new RuleException(e);
        }
        
        // updated program term ==> go for the wrapped program
        if (!(find instanceof LiteralProgramTerm)) {
            find = find.getSubterm(0);
        }
        
        if (find instanceof LiteralProgramTerm) {
            LiteralProgramTerm progTerm = (LiteralProgramTerm) find;
            if (hasBreakpoint(progTerm))
                return false;
        } else {
            throw new RuleException(
                    "Rules in 'symbex' MUST match a program term or updated program terms, this rule did not: "
                            + ruleApp.getRule().getName());
        }
        
        return true;
    }
}
