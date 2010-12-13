package de.uka.iti.pseudo.parser.boogie;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.creation.EnvironmentCreationService;
import de.uka.iti.pseudo.parser.boogie.environment.EnvironmentCreationState;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.util.Pair;

public final class BPLFileEnvironmentCreationService extends EnvironmentCreationService {

    @Override
    public String getDescription() {
        return "Boogie files";
    }

    @Override
    public String getDefaultExtension() {
        return "bpl";
    }

    @Override
    public Pair<Environment, Term> createEnvironment(URL url) throws IOException, EnvironmentException {
        try {
            BPLParser p = new BPLParser(new FileInputStream(new File(url.toURI())));
            EnvironmentCreationState s = new EnvironmentCreationState(p.parse(url.getFile()));

            return new Pair<Environment, Term>(s.make(), s.getProblem());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        } catch (ParseException e) {
            throw new EnvironmentException(e);
        }
    }

}
