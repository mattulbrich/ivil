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
import java.util.List;
import java.util.Map;

import nonnull.NonNull;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.Lemma;
import de.uka.iti.pseudo.environment.Program;
import de.uka.iti.pseudo.environment.ProofObligation;
import de.uka.iti.pseudo.parser.ASTDefaultVisitor;
import de.uka.iti.pseudo.parser.ASTElement;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.file.ASTFile;
import de.uka.iti.pseudo.parser.file.ASTLemmaDeclaration;
import de.uka.iti.pseudo.parser.file.ASTProgramDeclaration;
import de.uka.iti.pseudo.parser.file.ASTRule;
import de.uka.iti.pseudo.rule.Rule;
import de.uka.iti.pseudo.rule.RuleTagConstants;
import de.uka.iti.pseudo.term.Modality;

/**
 * The vistor EnvironmentProofObligationExtractor extracts proof obligations
 * from an AST.
 *
 * To this end it traverses the toplevel AST nodes and records all rules, lemmas
 * and programs.
 *
 * Since the order of symbols is of importance for the proof environments, this
 * cannot be done on the final environment, but needs to be done on the AST.
 */
class EnvironmentProofObligationExtractor extends ASTDefaultVisitor {

    /**
     * The environment upon which to operate.
     * Parsing for it has already been finished.
     */
    private final Environment env;

    /**
     * The proof obligations storage.
     */
    private final Map<String, ProofObligation> proofObligations;

    /**
     * The list of lemmas and rules encountered so far.
     */
    private final List<Object> availableLemmasAndRules = new ArrayList<Object>();;

    /**
     * Instantiates a new environment proof obligation extractor.
     *
     * @param env the environment to consider
     * @param proofObligations the store for proof obligations
     */
    public EnvironmentProofObligationExtractor(@NonNull Environment env,
            @NonNull Map<String, ProofObligation> proofObligations) {
        this.env = env;
        this.proofObligations = proofObligations;
    }

    /*
     * do nothing by default.
     */
    @Override
    protected void visitDefault(ASTElement arg) throws ASTVisitException {
        // do nothing
    }

    /*
     * visit all children of a file node
     */
    @Override
    public void visit(ASTFile arg) throws ASTVisitException {
        for (ASTElement child : arg.getChildren()) {
            child.visit(this);
        }
    }

    /*
     * For a program add the total and the partial proof obligations.
     */
    @Override
    public void visit(ASTProgramDeclaration arg) throws ASTVisitException {

        String name = arg.getName().image;
        Program program = env.getProgram(name);

        ProofObligation po = new ProofObligation.ProgramPO(env, program, Modality.BOX);
        proofObligations.put(po.getName(), po);
        po = new ProofObligation.ProgramPO(env, program, Modality.BOX_TERMINATION);
        proofObligations.put(po.getName(), po);

    }

    /*
     * For a lemma, add the according proof obligation.
     * This proof obligation may see all available lemmas and rules
     * After the creation, add the lemma to the set of available items.
     */
    @Override
    public void visit(ASTLemmaDeclaration arg) throws ASTVisitException {

        String name = arg.getName().image;
        Lemma lemma = env.getLemma(name);
        assert lemma != null;

        if(!lemma.getDefinedProperties().contains("axiom")) {
            ProofObligation po = new ProofObligation.LemmaPO(env, lemma, availableLemmasAndRules);
            proofObligations.put(po.getName(), po);
        }

        availableLemmasAndRules.add(lemma);
    }

    /*
     * For a rule, add the according proof obligation.
     * This proof obligation may see all available lemmas and rules
     * After the creation, add the rule to the set of available items.
     *
     * A rule may induce an equally named lemma (see RuleAxiomExtractor).
     * Add this lemma also to the set of allowed symbols if needed.
     */
    @Override
    public void visit(ASTRule arg) {
        String name = arg.getName().image;

        Rule rule = env.getRule(name);
        assert rule != null;

        if(!rule.getDefinedProperties().contains("axiom")) {
            ProofObligation po = new ProofObligation.RulePO(env, rule, availableLemmasAndRules);
            proofObligations.put(po.getName(), po);
        }

        availableLemmasAndRules.add(rule);

        Lemma lemma = env.getLemma(name);
        if(lemma != null && lemma.getProperty(RuleTagConstants.KEY_GENERATED_AXIOM) != null) {
            availableLemmasAndRules.add(lemma);
        }
    }

}
