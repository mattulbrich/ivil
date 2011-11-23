package de.uka.iti.pseudo.parser.boogie;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.boogie.EnvironmentCreationState;
import de.uka.iti.pseudo.environment.creation.EnvironmentCreationService;
import de.uka.iti.pseudo.term.Sequent;
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
    public Pair<Environment, Sequent> createEnvironment(InputStream inputStream, URL url) 
           throws IOException, EnvironmentException {
        try {
            BPLParser p = new BPLParser(inputStream);
            EnvironmentCreationState s = new EnvironmentCreationState(p.parse(url));

            return new Pair<Environment, Sequent>(s.make(), null);
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
        }
    }

}
