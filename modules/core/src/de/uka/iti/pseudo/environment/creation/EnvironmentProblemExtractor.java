package de.uka.iti.pseudo.environment.creation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

// TODO DOC
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

    private static final Settings SETTINGS = Settings.getInstance();
    private static final Term[] NO_TERMS = new Term[0];
    private final Map<String, Sequent> problemSequents = new LinkedHashMap<String, Sequent>();
    private final Environment env;

    public EnvironmentProblemExtractor(Environment env) {
        this.env = env;
    }

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

    private boolean isConsiderTermination() {

        String property = env.getProperty(TERMINATION_PROPERTY);
        if(property != null) {
            return Boolean.parseBoolean(property);
        }

        return SETTINGS.getBoolean("pseudo.programTermination", true);
    }

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
