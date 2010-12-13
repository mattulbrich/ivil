package de.uka.iti.pseudo.environment.creation;

import java.io.IOException;
import java.net.URL;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ParseException;
import de.uka.iti.pseudo.parser.Parser;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.util.Pair;

public class PFileEnvironmentCreationService extends EnvironmentCreationService {

    @Override
    public Pair<Environment, Term> createEnvironment(URL url)
            throws IOException, EnvironmentException {
        Parser fp = new Parser();

        try {
            EnvironmentMaker em = new EnvironmentMaker(fp, url);
            Environment env = em.getEnvironment();
            Term problemTerm = em.getProblemTerm();
            
            return Pair.make(env, problemTerm);
        } catch (ParseException e) {
            throw new EnvironmentException(e);
        } catch (ASTVisitException e) {
            throw new EnvironmentException(e);
        }
    }

    @Override
    public String getDefaultExtension() {
        return "p";
    }

    @Override
    public String getDescription() {
        return "ivil's own fileformat";
    }

}
