package de.uka.iti.pseudo.parser.boogie;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.Program;
import de.uka.iti.pseudo.environment.boogie.EnvironmentCreationState;
import de.uka.iti.pseudo.environment.creation.EnvironmentCreationService;
import de.uka.iti.pseudo.term.LiteralProgramTerm;
import de.uka.iti.pseudo.term.Modality;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.util.Pair;
import de.uka.iti.pseudo.util.settings.Settings;

public final class BPLFileEnvironmentCreationService extends EnvironmentCreationService {

    private static final Term[] NO_TERMS = new Term[0];

    @Override
    public String getDescription() {
        return "Boogie files";
    }

    @Override
    public String getDefaultExtension() {
        return "bpl";
    }

    @Override
    public Pair<Environment, Map<String, Sequent>> createEnvironment(InputStream inputStream, URL url)
           throws IOException, EnvironmentException {
        try {
            BPLParser p = new BPLParser(inputStream);
            EnvironmentCreationState s = new EnvironmentCreationState(p.parse(url));

            Environment env = s.make();
            return Pair.make(env, createProgramProblems(env));
        } catch (ParseException e) {
            EnvironmentException envEx = new EnvironmentException(e);
            Token currentToken = e.currentToken;
            if(currentToken != null) {
                Token problemToken = currentToken.next;
                envEx.setBeginColumn(problemToken.beginColumn);
                envEx.setBeginLine(problemToken.beginLine);
                envEx.setEndColumn(problemToken.endColumn);
                envEx.setEndLine(problemToken.endLine);
            }
            envEx.setResource(url.toString());
            throw envEx;
        } catch (TermException e) {
            // should not be called actually
            throw new EnvironmentException("Error creating environment", e);
        }
    }

    private Map<String, Sequent> createProgramProblems(Environment env) throws TermException {

        final boolean termination =
                Settings.getInstance().getBoolean("pseudo.boogie.programTermination", true);
        Modality modality = termination ? Modality.BOX_TERMINATION : Modality.BOX;
        String suffix = termination ?"_total" : "_partial";

        Term trueTerm = Environment.getTrue();
        Map<String, Sequent> problemSequents = new HashMap<String, Sequent>();

        for (Program p : env.getAllPrograms()) {
            String name = p.getName();
            LiteralProgramTerm lpt = LiteralProgramTerm.getInst(0, modality, p, trueTerm);
            Sequent sequent = new Sequent(NO_TERMS, new Term[] { lpt });
            problemSequents.put(name + suffix, sequent);
        }

        return problemSequents;
    }
}
