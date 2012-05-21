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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import nonnull.NonNull;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.Program;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.parser.file.ASTFile;
import de.uka.iti.pseudo.parser.file.ASTProblemSequent;
import de.uka.iti.pseudo.parser.term.ASTTerm;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.term.Modality;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.term.creation.TermMaker;
import de.uka.iti.pseudo.term.creation.TermMatcher;
import de.uka.iti.pseudo.util.SelectList;
import de.uka.iti.pseudo.util.settings.Settings;

/**
 * The Class EnvironmentProblemExtractor is used to extract problem descriptions
 * from an environment AST.
 *
 * It visits the {@link ASTProblemSequent} nodes and stores their valuation into
 * a map.
 *
 * If no problem description(s) are present in the environment, they are
 * generated from the programs of the environment, using the modality specified
 * by the settings (see {@link #TERMINATION_PROPERTY}).
 *
 * @see Environment
 */
public class EnvironmentProblemExtractor {

    /**
     * @ivildoc "Environment property/termination"
     *
     * If an environment does not contain a literal problem description, the
     * programs implicitly induce problem descriptions of the form
     * <tt>[0;P]</tt> for each program <tt>P</tt>.
     * <p>
     * The environment property <tt>ProofObligation.termination</tt> can be used
     * to specify whether these problems should include a termination proof
     * obligation (total correctness; using modality <tt>[[&middot;]]</tt>) or
     * should not consider termination (partial correctness; using modality
     * <tt>[&middot;]</tt>.
     *
     * <p>
     * Set it to <code>"true"</code> for total correctness, <code>"false"</code>
     * for partial correctness.
     */
    public static final String TERMINATION_PROPERTY = "ProofObligation.termination";

    /**
     * The Settings object stored locally
     */
    private static final Settings SETTINGS = Settings.getInstance();

    /**
     * An empty array of terms.
     */
    private static final Term[] NO_TERMS = new Term[0];

    /**
     * The map from names to problem sequents.
     *
     * We use a linked hash map to preserve the original order.
     */
    private final Map<String, Sequent> problemSequents =
            new LinkedHashMap<String, Sequent>();

    /**
     * The environment to work upon.
     */
    private final Environment env;

    /**
     * Instantiates a new environment problem extractor.
     *
     * @param env
     *            the environment to operate on
     */
    public EnvironmentProblemExtractor(@NonNull Environment env) {
        this.env = env;
    }

    /**
     * Handle the AST of a file.
     *
     * This method should only be called once per object! An additional call
     * will probably result in a strange return value.
     *
     * All problem sequent declarations are visited, the remainder is ignored.
     *
     * @param arg
     *            the AST of a file
     * @return a map from problem description names to sequents. May be empty.
     *
     * @throws ASTVisitException
     *             if some exception during conversion happens.
     */
    public Map<String,Sequent> handle(ASTFile arg) throws ASTVisitException {
        for(ASTProblemSequent ast : arg.getProblemSequents()) {
            handleProblemSequent(ast);
        }

        if(problemSequents.isEmpty()) {
            try {
                createProgramProblems();
            } catch (TermException e) {
                throw new ASTVisitException(e);
            }
        }

        return problemSequents;
    }

    /**
     * Creates the problems for the programs of the file, if no explicit problem
     * description is given
     */
    private void createProgramProblems() throws TermException {

        final Term trueTerm = Environment.getTrue();
        final boolean termination = isConsiderTermination();
        final Modality modality = termination ? Modality.BOX_TERMINATION : Modality.BOX;
        final String suffix = termination ? "_total" : "_partial";

        for (Program p : env.getAllPrograms()) {
            String name = p.getName();
            LiteralProgramTerm progTerm = LiteralProgramTerm.getInst(0, modality, p, trueTerm);
            Sequent sequent = new Sequent(NO_TERMS, new Term[] { progTerm });
            problemSequents.put(name + suffix, sequent);
        }
    }

    /**
     * Checks if is termination is to be considered for the generation of
     * implicit problems.
     */
    private boolean isConsiderTermination() {

        String property = env.getProperty(TERMINATION_PROPERTY);
        if(property != null) {
            return Boolean.parseBoolean(property);
        }

        return SETTINGS.getBoolean("pseudo.programTermination", true);
    }

    /**
     * Handle a single problem sequent description.
     *
     * Create a sequent from the given ast terms.
     *
     * Check that
     * <ol>
     * <li>An unnamed problem is the only problem</li>
     * <li>No two problems carry the same name</li>
     * </ol>
     *
     * @param seq
     *            the ast for the sequent decl
     *
     */
    private void handleProblemSequent(ASTProblemSequent seq) throws ASTVisitException {
        List<Term> ante = new ArrayList<Term>();
        List<Term> succ = new ArrayList<Term>();

        int i = 0;
        for (ASTTerm ast : SelectList.select(ASTTerm.class, seq.getChildren())) {
            Term term = TermMaker.makeTerm(ast, env);

            if(TermMatcher.containsSchematic(term)) {
                throw new ASTVisitException("Problem sequent contains schema type, " +
                        "schema variable or schema update in " + term, seq);
            }

            if(i < seq.getAntecedentCount()) {
                ante.add(term);
            } else {
                succ.add(term);
            }

            i++;
        }


        Sequent problemSequent;
        try {
            // constructor for sequent checks using ToplevelCheckVisitor
            problemSequent = new Sequent(ante, succ);
        } catch (TermException e) {
            throw new ASTVisitException(seq, e);
        }

        Token idTok = seq.getIdentifier();
        String identifier;
        if(idTok == null) {
            if(problemSequents.size() > 0) {
                throw new ASTVisitException("An unnamed problem must be the only problem in environment", seq);
            }
            identifier = "";
        } else {
            if(problemSequents.containsKey("")) {
                throw new ASTVisitException("An unnamed problem must be the only one in enviroment", seq);
            }
            identifier = idTok.image;
            if(problemSequents.containsKey(identifier)) {
                throw new ASTVisitException("A problem of name '" + identifier +
                        "' has already been defined in environment", seq);
            }
        }

        problemSequents.put(identifier, problemSequent);

    }

}
