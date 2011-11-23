package de.uka.iti.pseudo.environment.creation;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.parser.ASTElement;
import de.uka.iti.pseudo.parser.ASTLocatedElement;
import de.uka.iti.pseudo.parser.ASTVisitException;
import de.uka.iti.pseudo.parser.ParseException;
import de.uka.iti.pseudo.parser.Parser;
import de.uka.iti.pseudo.parser.Token;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.util.Pair;

public class PFileEnvironmentCreationService extends EnvironmentCreationService {

    @Override
    public Pair<Environment, Sequent> createEnvironment(InputStream inputStream, URL url)
            throws IOException, EnvironmentException {
        Parser fp = new Parser();

        try {
            EnvironmentMaker em = new EnvironmentMaker(fp, inputStream, url);
            Environment env = em.getEnvironment();
            Sequent problemSequent = em.getProblemSequent();
            
            return Pair.make(env, problemSequent);
            
        } catch (ParseException e) {
            EnvironmentException resultEx = new EnvironmentException(e);
            Token problemtoken = e.currentToken.next;
            resultEx.setBeginLine(problemtoken.beginLine);
            resultEx.setEndLine(problemtoken.endLine);
            resultEx.setBeginColumn(problemtoken.beginColumn);
            resultEx.setEndColumn(problemtoken.endColumn);
            resultEx.setResource(url.toString());
            throw resultEx;
            
        } catch (ASTVisitException e) {
            EnvironmentException resultEx = new EnvironmentException(e);
            ASTLocatedElement location = e.getLocation();
            
            if (location instanceof ASTElement) {
                ASTElement ast = (ASTElement)location;
                Token token = ast.getLocationToken();
                resultEx.setBeginLine(token.beginLine);
                resultEx.setEndLine(token.endLine);
                resultEx.setBeginColumn(token.beginColumn);
                resultEx.setEndColumn(token.endColumn);
                resultEx.setResource(ast.getFileName());
            }
            
            throw resultEx;
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
