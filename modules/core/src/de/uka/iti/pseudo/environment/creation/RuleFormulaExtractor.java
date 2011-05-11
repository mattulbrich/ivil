/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.environment.creation;

import java.util.List;

import nonnull.NonNull;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.parser.file.MatchingLocation;
import de.uka.iti.pseudo.rule.GoalAction;
import de.uka.iti.pseudo.rule.LocatedTerm;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.rule.GoalAction.Kind;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.creation.TermFactory;

/**
 * This class allows the transformation of a rule into its meaning formula.
 * 
 * The meaning formula is defined in this <a
 * href="http://www.cs.chalmers.se/~philipp/publications/lfm.pdf">paper</a> by
 * Bubel, Roth, and Ruemmer.
 * 
 * The result of this process still contains schema entities.
 * 
 * @see de.uka.iti.pseudo.justify.RuleProblemExtractor
 * @see RuleAxiomExtractor
 */
public class RuleFormulaExtractor {

    private static final Term FALSE = Environment.getFalse();
    private static final Term TRUE = Environment.getTrue();

    private TermFactory tf;

    /**
     * Instantiates a new rule formula extractor for an environment.
     * 
     * @param env
     *            the environment to use
     */
    public RuleFormulaExtractor(@NonNull Environment env) {
        this.tf = new TermFactory(env);
    }

    /**
     * Extract meaning formula.
     * 
     * @param rule
     *            the rule
     * 
     * @return the term
     * 
     * @throws TermException
     *             the term exception
     * @throws RuleException
     *             the rule exception
     */
    public Term extractMeaningFormula(Rule rule) throws TermException,
            RuleException {
        //
        // create the formula context from the assumptions.
        Term context = makeContext(rule);

        //
        // formulate rule as a term with schema vars.
        // incorporates the context.
        Term problem0;
        if (isRewrite(rule))
            problem0 = extractRewriteProblem(rule, context);
        else
            problem0 = extractLocatedProblem(rule, context);

        return problem0;

    }

    /**
     * Given a rule with located (or no) find term, extract the verification
     * condition from it. The resulting term will still contain all original
     * schema variables and type variables, which must be skolemised/substituted
     * later.
     * 
     * <p>
     * The resulting term looks like:
     * 
     * <pre>
     *     ( replace1 or OR(adds1))
     *   &amp; ...
     *   &amp; (  replacek or OR(addsk))
     *   -&gt;  ( find or context )
     * </pre>
     * 
     * in which the context has already been precalculated.
     * 
     * The find and replace terms may be negated if they appear on the
     * antecedent.
     * 
     * @param context
     * @param rule
     * 
     * @return the meaning formula of the rule, with schema variables
     */
    private Term extractLocatedProblem(Rule rule, Term context)
            throws RuleException, TermException {

        LocatedTerm findClause = rule.getFindClause();

        // having no find is not assuming anything --> empty sequence --> false
        if (findClause == null)
            findClause = new LocatedTerm(FALSE, MatchingLocation.SUCCEDENT);

        boolean findInAntecedent = findClause.getMatchingLocation() == MatchingLocation.ANTECEDENT;
        Term findTerm = findClause.getTerm();

        Term result = TRUE;
        List<GoalAction> actions = rule.getGoalActions();
        for (GoalAction action : actions) {

            if (action.getKind() != Kind.COPY)
                throw new RuleException(
                        "ProblemExtraction works only for copy goals at the moment");

            Term add = FALSE;
            for (Term t : action.getAddAntecedent()) {
                add = disj(add, tf.not(t));
            }
            for (Term t : action.getAddSuccedent()) {
                add = disj(add, t);

            }
            Term replace = action.getReplaceWith();

            // copy original term if not remove
            if (replace == null && !action.isRemoveOriginalTerm())
                replace = findTerm;

            if (replace != null) {
                if (findInAntecedent) {
                    add = disj(add, tf.not(replace));
                } else {
                    add = disj(add, replace);
                }
            }

            result = conj(result, add);
        }

        Term findAndContext = context;
        if (findInAntecedent) {
            findAndContext = disj(findAndContext, tf.not(findTerm));
        } else {
            findAndContext = disj(findAndContext, findTerm);
        }

        result = impl(result, findAndContext);
        return result;
    }

    /**
     * Given a rewrite rule, extract the verification condition from it. The
     * resulting term will still contain all original schema variables and type
     * variables, which must be skolemised/substituted later.
     * 
     * <p>
     * The resulting term looks like:
     * 
     * <pre>
     *     ( replace1 = find -&gt; OR(adds1))
     *   &amp; ...
     *   &amp; (  replacek = find -&gt; OR(addsk))
     *   -&gt;  context
     * </pre>
     * 
     * in which the context has already been precalculated.
     * 
     * @param context
     * @param rule
     * 
     * @return the meaning formula of the rule, with schema variables
     */
    private Term extractRewriteProblem(@NonNull Rule rule, @NonNull Term context)
            throws TermException, RuleException {

        Term find = rule.getFindClause().getTerm();
        Term result = TRUE;

        List<GoalAction> actions = rule.getGoalActions();
        for (GoalAction action : actions) {

            if (action.getKind() != Kind.COPY)
                throw new RuleException(
                        "FormulaExtraction works only for copy goals at the moment");

            Term add = FALSE;
            for (Term t : action.getAddAntecedent()) {
                add = disj(add, tf.not(t));
            }
            for (Term t : action.getAddSuccedent()) {
                add = disj(add, t);
            }

            Term replace = action.getReplaceWith();
            if (replace == null)
                replace = find;

            Term eq = eq(replace, find);
            Term imp = impl(eq, add);
            result = conj(result, imp);
        }

        result = impl(result, context);

        return result;
    }

    /**
     * Make the context term of a rule as a disjunction of all its assumptions (either
     * positive or negative).
     * 
     * @param rule
     *            the rule to extract the context from
     * 
     * @return the context. <code>false</code> if no assumptions are present
     * 
     */
    private Term makeContext(Rule rule) throws TermException, RuleException {

        Term context = FALSE;

        List<LocatedTerm> assumptions = rule.getAssumptions();
        for (LocatedTerm assume : assumptions) {
            switch (assume.getMatchingLocation()) {
            case ANTECEDENT:
                context = disj(context, tf.not(assume.getTerm()));
                break;
            case SUCCEDENT:
                context = disj(context, assume.getTerm());
                break;
            default:
                throw new RuleException("Error in assumption statement");
            }
        }
        return context;
    }
    
    
    /**
     * Checks if a rule is a rewrite rule.
     * 
     * That is the case if  the rule has a find clause which matches on both sides.
     * 
     * @param rule
     *            the rule
     * 
     * @return true, if the rule is rewrite
     */
    private boolean isRewrite(@NonNull Rule rule) {
        LocatedTerm find = rule.getFindClause();
        return find != null
                && find.getMatchingLocation() == MatchingLocation.BOTH;
    }

    /**
     * create a disjunction term.
     * 
     * If at least one of the arguments is syntactically <code>false</code>, the
     * result is the other term.
     * 
     * @param t1
     *            a boolean term.
     * @param t2
     *            another boolean term
     * @return t1 &or; t2 (or one of them)
     * @throws TermException
     *             if the terms are not boolean
     */
    private Term disj(@NonNull Term t1, @NonNull Term t2) throws TermException {
        if (t1 == FALSE)
            return t2;
        else if (t2 == FALSE)
            return t1;
        else
            return tf.or(t1, t2);
    }

    /**
     * create a conjunction term.
     * 
     * If at least one of the arguments is syntactically <code>true</code>, the
     * result is the other term.
     * 
     * @param t1
     *            a boolean term.
     * @param t2
     *            another boolean term
     * @return t1 &and; t2 (or true)
     * @throws TermException
     *             if the terms are not boolean
     */
    private @NonNull Term conj(@NonNull Term t1, @NonNull Term t2) throws TermException {
        if (t1 == TRUE)
            return t2;
        else if (t2 == TRUE)
            return t1;
        else
            return tf.and(t1, t2);
    }

    /**
     * create am implication term.
     * 
     * If the first argument is syntactically <code>true</code>, the result is
     * the other term, if the second is <code>false</code>, the result is the
     * first.
     * 
     * @param t1
     *            a boolean term.
     * @param t2
     *            another boolean term
     * @return t1 -&gt; t2 (or one of them)
     * @throws TermException
     *             if the terms are not boolean
     */
    private @NonNull Term impl(@NonNull Term t1, @NonNull Term t2) throws TermException {
        if (t1 == TRUE)
            return t2;
        else if (t2 == FALSE)
            return t1;
        else
            return tf.impl(t1, t2);
    }

    /**
     * create am equality term.
     * 
     * This can also be an equivalence if applied to boolean terms.
     * 
     * Hence, if one of the arguments is syntactically <code>true</code>, the
     * result is the other term.
     * 
     * @param t1
     *            a boolean term.
     * @param t2
     *            another boolean term
     * @return t1 = t2 (or one of them)
     */
    private @NonNull Term eq(@NonNull Term t1, @NonNull Term t2) throws TermException {
        if (TRUE.equals(t1))
            return t2;
        else if (TRUE.equals(t2))
            return t1;
        else
            return tf.eq(t1, t2);
    }

}
