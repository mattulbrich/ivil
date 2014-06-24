/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.environment.creation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uka.iti.pseudo.environment.Lemma;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.WhereCondition;
import de.uka.iti.pseudo.parser.ASTDefaultVisitor;
import de.uka.iti.pseudo.parser.ASTElement;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.parser.file.ASTGoalAction;
import de.uka.iti.pseudo.parser.file.ASTLemmaDeclaration;
import de.uka.iti.pseudo.parser.file.ASTLocatedTerm;
import de.uka.iti.pseudo.parser.file.ASTProgramDeclaration;
import de.uka.iti.pseudo.parser.file.ASTRule;
import de.uka.iti.pseudo.parser.file.ASTRuleAdd;
import de.uka.iti.pseudo.parser.file.ASTRuleAssume;
import de.uka.iti.pseudo.parser.file.ASTRuleFind;
import de.uka.iti.pseudo.parser.file.ASTRuleRemove;
import de.uka.iti.pseudo.parser.file.ASTRuleReplace;
import de.uka.iti.pseudo.parser.file.ASTWhereClause;
import de.uka.iti.pseudo.parser.file.MatchingLocation;
import de.uka.iti.pseudo.parser.term.ASTTerm;
import de.uka.iti.pseudo.rule.GoalAction;
import de.uka.iti.pseudo.rule.LocatedTerm;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.rule.RuleException;
import de.uka.iti.pseudo.rule.WhereClause;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.creation.TermMaker;
import de.uka.iti.pseudo.util.Pair;
import de.uka.iti.pseudo.util.SelectList;
import de.uka.iti.pseudo.util.Util;

/**
 * The visitor EnvironmentRuleDefinitionVisitor is used to render ASTs for rules
 * and lemmas into their equivalents for {@link Environment}s.
 */
public class EnvironmentRuleDefinitionVisitor extends ASTDefaultVisitor {

    /*
     * Results of various types are transferred during traversal using the
     * following fields:
     */
    /**
     * The intermediate resulting term.
     */
    private Term resultingTerm;

    /**
     * The intermediate resulting matching location.
     */
    private MatchingLocation resultingMatchingLocation;

    /**
     * The intermediate resulting whereclause.
     */
    private WhereClause resultingWhereclause;

    /**
     * The intermediate resulting goal action.
     */
    private GoalAction resultingGoalAction;

    /**
     * The environment to add rules and lemmas to.
     */
    private final Environment env;

    /**
     * Instantiates a new environment rule definition visitor.
     *
     * @param env the env
     */
    public EnvironmentRuleDefinitionVisitor(Environment env) {
        this.env = env;
    }

    /*
     * default behaviour
     *
     * visit children - depth
     */
    @Override
    protected void visitDefault(ASTElement arg) throws ASTVisitException {
        for (ASTElement child : arg.getChildren()) {
            child.visit(this);
        }
    }

    /*
     * Terms
     */
    @Override
    protected void visitDefaultTerm(ASTTerm arg) throws ASTVisitException {
        resultingTerm = TermMaker.makeTerm(arg, env);
    }

    /*
     * lemmas are similar to rules ... so handle them here
     */
    @Override
    public void visit(ASTLemmaDeclaration arg) throws ASTVisitException {


        String name = arg.getName().image;

        super.visit(arg);
        Term term = resultingTerm;
        Map<String, String> properties = new HashMap<String, String>();
        {
            for (Pair<Token, Token> prop : arg.getProperties()) {
                Token token = prop.snd();
                String value;
                if(token != null) {
                    value = Util.stripQuotes(token.image);
                } else {
                    value = "";
                }

                properties.put(prop.fst().image, value);
            }
        }

        try {
            Lemma axiom = new Lemma(name, term, properties, arg);
            env.addLemma(axiom);
        } catch (EnvironmentException e) {
            throw new ASTVisitException(arg, e);
        }

    }

    /*
     * collect all information for a rule definition and define it in env.
     *
     * The with parts are parsed first as they are local definitions.
     */
    @Override
    public void visit(ASTRule arg) throws ASTVisitException {

        try {

            String name = arg.getName().image;
            List<ASTElement> children = arg.getChildren();

            List<LocatedTerm> assumes = new ArrayList<LocatedTerm>();
            {
                List<ASTRuleAssume> assumeASTs = SelectList.select(
                        ASTRuleAssume.class, children);
                for (ASTRuleAssume assume : assumeASTs) {
                    assume.visit(this);
                    assumes.add(new LocatedTerm(resultingTerm,
                            resultingMatchingLocation));
                }
            }

            LocatedTerm find;
            {
                List<ASTRuleFind> findASTs = SelectList.select(
                        ASTRuleFind.class, children);
                if (findASTs.size() > 1) {
                    throw new ASTVisitException(
                            "There is more than one find clause in this rule.", arg);
                }

                if (findASTs.size() == 1) {
                    ASTRuleFind astFind = findASTs.get(0);
                    astFind.visit(this);
                    find = new LocatedTerm(resultingTerm, resultingMatchingLocation);
                } else {
                    find = null;
                }
            }

            List<WhereClause> wheres = new ArrayList<WhereClause>();
            {
                List<ASTWhereClause> whereASTs = SelectList.select(
                        ASTWhereClause.class, children);
                for (ASTWhereClause where : whereASTs) {
                    where.visit(this);
                    wheres.add(resultingWhereclause);
                }
            }

            List<GoalAction> actions = new ArrayList<GoalAction>();
            {
                List<ASTGoalAction> actionASTs = SelectList.select(
                        ASTGoalAction.class, children);
                for (ASTGoalAction action : actionASTs) {
                    action.visit(this);
                    actions.add(resultingGoalAction);
                }
            }

            Map<String, String> properties = new HashMap<String, String>();
            {
                for (Pair<Token, Token> prop : arg.getProperties()) {
                    Token token = prop.snd();
                    String value;
                    if(token != null) {
                        value = Util.stripQuotes(token.image);
                    } else {
                        value = "";
                    }

                    properties.put(prop.fst().image, value);
                }
                Token description = arg.getDescription();
                if(description != null) {
                    properties.put("description", Util.stripQuotes(description.image));
                }

            }

            try {
                Rule rule = new Rule(name, assumes, find, wheres, actions, properties, arg);
                env.addRule(rule);
            } catch (EnvironmentException e) {
                throw new ASTVisitException(arg, e);
            }

        } catch (RuleException e) {
            throw new ASTVisitException(arg, e);
        }
    }

    /*
     * make a where clause
     * report if where condition is unknown.
     */
    @Override
    public void visit(ASTWhereClause arg) throws ASTVisitException {

        String identifier = arg.getIdentifier().image;

        WhereCondition where;
        try {
            where = WhereCondition.getWhereCondition(env, identifier);
        } catch (EnvironmentException e) {
            throw new ASTVisitException(arg, e);
        }

        if (where == null) {
            throw new ASTVisitException("Unknown where condition: "
                    + identifier, arg);
        }

        List<ASTTerm> raws = SelectList.select(ASTTerm.class, arg
                .getChildren());
        Term[] terms = new Term[raws.size()];
        for (int i = 0; i < terms.length; i++) {
            raws.get(i).visit(this);
            terms[i] = resultingTerm;
        }

        try {
            resultingWhereclause = new WhereClause(where, arg.isInverted(), terms);
        } catch (RuleException e) {
            throw new ASTVisitException(arg, e);
        }

    }

    /*
     * collect children an make a new goal action
     */
    @Override
    public void visit(ASTGoalAction arg) throws ASTVisitException {
        super.visit(arg);

        Token kindToken = arg.getGoalKindToken();
        String kind;
        if(kindToken != null) {
            kind = kindToken.image;
        } else {
            // if no action target is specified, samegoal is the default
            kind = "samegoal";
        }
        Token nameToken = arg.getName();
        String name = nameToken == null ? null : Util.stripQuotes(nameToken.image);

        List<Term> addAntecendent = new ArrayList<Term>();
        List<Term> addSuccendent = new ArrayList<Term>();
        Term replaceWith = null;

        for(ASTRuleAdd add : SelectList.select(ASTRuleAdd.class, arg.getChildren())) {
            add.visit(this);
            if(resultingMatchingLocation == MatchingLocation.ANTECEDENT) {
                addAntecendent.add(resultingTerm);
            } else {
                addSuccendent.add(resultingTerm);
            }
        }

        for(ASTRuleReplace replace : SelectList.select(ASTRuleReplace.class, arg.getChildren())) {
            if(replaceWith != null) {
                throw new ASTVisitException("Goal actions must not contain " +
                        "more than one replace action", replace);
            }
            replace.visit(this);
            replaceWith = resultingTerm;
        }

        boolean hasRemove = !SelectList.select(ASTRuleRemove.class,
                arg.getChildren()).isEmpty();

        try {
            resultingGoalAction = new GoalAction(kind, name, hasRemove, replaceWith,
                    addAntecendent, addSuccendent);
        } catch (RuleException e) {
            throw new ASTVisitException(arg, e);
        }
    }

    /*
     * Type application
     */
    /*public void visit(ASTType arg) throws ASTVisitException {
        resultingTypeRef = TermMaker.makeType(arg, env);
    }*/

    /*
     * transform a ASTTerm to a Term.
     */
    /**
     * Visit.
     *
     * @param arg the arg
     * @throws ASTVisitException the aST visit exception
     */
    public void visit(ASTTerm arg) throws ASTVisitException {
        resultingTerm = TermMaker.makeTerm(arg, env);
    }

    /*
     * store the location for a located term.
     */
    @Override
    public void visit(ASTLocatedTerm arg) throws ASTVisitException {
        super.visit(arg);
        resultingMatchingLocation = arg.getMatchingLocation();
    }

    /*
     * ignore the program definition because it may contain identifier labels and
     */
    @Override
    public void visit(ASTProgramDeclaration arg) throws ASTVisitException {
        // do nothing
    }

    // Can this be commented out?
//    @Override
//    public void visit(ASTProperties plugins) throws ASTVisitException {
//    }
//
//    @Override
//    public void visit(ASTPropertiesDeclaration plugin) throws ASTVisitException {
//    }

}
