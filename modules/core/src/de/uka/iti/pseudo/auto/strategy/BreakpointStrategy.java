/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.auto.strategy;

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import nonnull.Nullable;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.Program;
import de.uka.iti.pseudo.proof.Proof;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.proof.ProofNode;
import de.uka.iti.pseudo.proof.RuleApplication;
import de.uka.iti.pseudo.proof.RuleApplicationFilter;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.term.CodeLocation;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.statement.GotoStatement;
import de.uka.iti.pseudo.term.statement.SkipStatement;
import de.uka.iti.pseudo.term.statement.Statement;
import de.uka.iti.pseudo.util.TermUtil;

/**
 * The Class BreakpointStrategy implements the symbolic execution aware of
 * cycles and breakpoints.
 *
 * There are number of parameters that can be set. See below for ivildoc
 * comments.
 *
 * If desired the execution stops if a breakpoint is hit.
 */
public class BreakpointStrategy extends AbstractStrategy implements
        RuleApplicationFilter {

    /**
     * The break point manager stores all set breakpoints. It is there that we
     * query for points to stop at.
     */
    private final BreakpointManager breakPointManager = new BreakpointManager();

    /**
     * For detecting loops, code locations must be calculated. They are cached
     * here.
     */
    private final Map<ProofNode, Collection<CodeLocation<Program>>> codeLocationCache =
            new HashMap<ProofNode, Collection<CodeLocation<Program>>>();

    /**
     * The default name of the set of rules which we consult.
     *
     * @ivildoc "Environment property/rewrite"
     *
     * The environment property
     * <tt>BreakpointStrategy.rewrite</tt> can be used to specify the rewrite category
     * which is to be used for symbolic execution. This is useful if an alternate set
     * of symbolic execution rules is to be installed.
     *
     * <p>If not specified the default "symbex" is used.
     */
    private static final String DEFAULT_REWRITE_CATEGORY = "symbex";

    /*
     * The configurable properties
     */
    /**
     * @ivildoc "Environment property/obeyProgramBreakpoints"
     *
     * The environment property
     * <tt>BreakpointStrategy.obeyProgramBreakpoints</tt> can be used to specify
     * whether symbolic execution breakpoints set on level of the ivil program
     * are to be enabled. The value can be "<code>true</code>" or "
     * <code>false</code>".
     */
    private boolean obeyProgramBreakpoints = true;

    /**
     * @ivildoc "Environment property/obeySourceBreakpoints"
     *
     * The environment property
     * <tt>BreakpointStrategy.obeySourceBreakpoints</tt> can be used to specify
     * whether symbolic execution breakpoints set on level of the source code
     * program are to be enabled. The value can be "<code>true</code>" or "
     * <code>false</code>".
     */
    private boolean obeySourceBreakpoints = true;

    /**
     * @ivildoc "Environment property/stopAtSkip"
     *
     * The environment property <tt>BreakpointStrategy.stopAtSkip</tt> can be
     * used to specify whether symbolic execution should stop whenever reaching
     * a <tt>skip</tt> statement. The value can be "<code>true</code>" or "
     * <code>false</code>".
     */
    private boolean stopAtSkip = false;

    /**
     * @ivildoc "Environment property/stopAtLoop"
     *
     * The environment property <tt>BreakpointStrategy.stopAtSkip</tt> can be
     * used to specify whether symbolic execution should stop whenever reaching
     * a statement which has been visited already earlier in the proof. The
     * value can be "<code>true</code>" or " <code>false</code>".
     */
    private boolean stopAtLoop = true;

    /**
     * @ivildoc "Environment property/stopAtLoop"
     *
     * The environment property <tt>BreakpointStrategy.stopAtSkip</tt> can be
     * used to specify whether symbolic execution should stop whenever reaching
     * a goto statement of which one target lies before the current location.
     * This happens usually only at the end of a loop. The value can be "
     * <code>true</code>" or " <code>false</code>".
     */
    private boolean stopAtJumpBack = false;

    private RewriteRuleCollection ruleCollection;

    @Override
    public void init(Proof proof, Environment env,
            StrategyManager strategyManager) throws StrategyException {
        super.init(proof, env, strategyManager);

        String rewriteCategory = DEFAULT_REWRITE_CATEGORY;
        // obey settings in env
        {
            String name;

            name = this.getClass().getSimpleName() + ".obeyProgramBreakpoints";
            if (env.hasProperty(name)) {
                obeyProgramBreakpoints = Boolean.parseBoolean(env
                        .getProperty(name));
            }

            name = this.getClass().getSimpleName() + ".obeySourceBreakpoints";
            if (env.hasProperty(name)) {
                obeySourceBreakpoints = Boolean.parseBoolean(env
                        .getProperty(name));
            }

            name = this.getClass().getSimpleName() + ".stopAtSkip";
            if (env.hasProperty(name)) {
                stopAtSkip = Boolean.parseBoolean(env.getProperty(name));
            }

            name = this.getClass().getSimpleName() + ".stopAtLoop";
            if (env.hasProperty(name)) {
                stopAtLoop = Boolean.parseBoolean(env.getProperty(name));
            }

            name = this.getClass().getSimpleName() + ".stopAtJumpBack";
            if (env.hasProperty(name)) {
                stopAtJumpBack = Boolean.parseBoolean(env.getProperty(name));
            }

            name = this.getClass().getSimpleName() + ".rewrite";
            if (env.hasProperty(name)) {
                rewriteCategory = env.getProperty(name);
            }
        }

        ruleCollection = new RewriteRuleCollection(env.getAllRules(), rewriteCategory, env);
        ruleCollection.setApplicationFilter(this);
        ruleCollection.deactivateCache();

    }

    @Override
    public @Nullable RuleApplication findRuleApplication(ProofNode target) throws InterruptedException {

        RuleApplication ra = ruleCollection.findRuleApplication(target);
        return ra;
    }

    /**
     * Decide whether a rule application is to be applied or not.
     *
     * We extract the program term and check whether it is at a breakpoint using
     * {@link #hasBreakpoint(LiteralProgramTerm)}.
     *
     * @return <code>false</code> iff at a breakpoint
     */
    @Override
    public boolean accepts(RuleApplication ruleApp) throws RuleException {
        ProofNode proofNode = ruleApp.getProofNode();
        Sequent sequent = proofNode.getSequent();
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
            if (hasBreakpoint(progTerm, proofNode)) {
                return false;
            }
        } else {
            throw new RuleException(
                    "Rules in 'symbex' MUST match a program term or updated program terms, this rule did not: "
                            + ruleApp.getRule().getName());
        }

        return true;
    }

    private boolean hasBreakpoint(LiteralProgramTerm progTerm, ProofNode proofNode) {
        //
        // check for stop at skip
        if (stopAtSkip) {
            Statement s = progTerm.getStatement();
            if (s instanceof SkipStatement) {
                return true;
            }
        }

        //
        // check for backward jumping
        if (stopAtJumpBack) {
            Statement s = progTerm.getStatement();
            if (s instanceof GotoStatement) {
                GotoStatement gotoStm = (GotoStatement) s;
                int currentIndex = progTerm.getProgramIndex();
                for (Term target : gotoStm.getSubterms()) {
                    try {
                        int index = TermUtil.getIntLiteral(target);
                        if (index <= currentIndex) {
                            return true;
                        }
                    } catch (TermException e) {
                        throw new Error(e);
                    }
                }
            }
        }

        //
        // check for unwanted looping
        if (stopAtLoop) {
            CodeLocation<Program> codeLoc = CodeLocation.fromTerm(progTerm);

            // find first parent w/o this codeLoc
            while(proofNode != null) {
                if(!getCodeLocations(proofNode).contains(codeLoc)) {
                    break;
                }

                proofNode = proofNode.getParent();
            }

            // find another (older) ancestor w/ the codeLoc
            while(proofNode != null) {
                if(getCodeLocations(proofNode).contains(codeLoc)) {
                    // found!
                    return true;
                }

                proofNode = proofNode.getParent();
            }
        }


        Program program = progTerm.getProgram();
        int number = progTerm.getProgramIndex();

        //
        // check for program breakpoint
        if (obeyProgramBreakpoints) {
            if (breakPointManager.hasBreakpoint(program, number)) {
                return true;
            }
        }

        //
        // check for source breakpoint:

        // we store in differs whether the statement in front of the current
        // has a different source line number. only then the breakpoint is hit.
        URL sourceFile = program.getSourceFile();
        if (sourceFile != null && obeySourceBreakpoints) {
            int sourceline = progTerm.getStatement().getSourceLineNumber();
            boolean differs = number == 0
                    || program.getStatement(number - 1).getSourceLineNumber() != sourceline;

            // bugfix: this "-1" is needed because line numbers actually start
            // at 1
            if (differs
                    && breakPointManager.hasBreakpoint(sourceFile,
                            sourceline - 1)) {
                return true;
            }
        }

        return false;
    }

//    /**
//     * When starting a new round of automated proving, forget about
//     * program terms that we have encountered in previous runs.
//     */
//    @Override
//    public void beginSearch() throws StrategyException {
//        super.beginSearch();
//    }

    private Collection<CodeLocation<Program>> getCodeLocations(ProofNode proofNode) {
        Collection<CodeLocation<Program>> result = codeLocationCache.get(proofNode);
        if(result == null) {
            result = CodeLocation.findCodeLocations(proofNode.getSequent());
            codeLocationCache.put(proofNode, result);
        }
        return result;
    }

    @Override
    public String toString() {
        return "Breakpoint Strategy";
    }

    //
    // getter and setter
    //

    public BreakpointManager getBreakpointManager() {
        return breakPointManager;
    }

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

    //
    // Helper function
    //

}