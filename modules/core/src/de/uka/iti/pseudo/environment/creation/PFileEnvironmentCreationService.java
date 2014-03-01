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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import nonnull.Nullable;

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
    public Pair<Environment, Map<String,Sequent>> createEnvironment(InputStream inputStream, URL url)
            throws IOException, EnvironmentException {
        Parser fp = new Parser();

        try {
            EnvironmentMaker em = new EnvironmentMaker(fp, inputStream, url);
            Environment env = em.getEnvironment();
            Map<String, Sequent> problemSequents = em.getProblemSequents();

            return Pair.make(env, problemSequents);

        } catch (ParseException e) {
            EnvironmentException resultEx = new EnvironmentException(e);
            Token problemtoken = e.currentToken.next;
            resultEx.setBeginLine(problemtoken.beginLine);
            resultEx.setEndLine(problemtoken.endLine);
            resultEx.setBeginColumn(problemtoken.beginColumn);
            resultEx.setEndColumn(problemtoken.endColumn);
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
            }

            resultEx.setCauseResource(getCauseLocation(e));

            throw resultEx;
        }
    }

    /**
     * Get the location of a {@link ASTVisitException} wrapped in this
     * exception, possibly several times.
     *
     * Descent to the {@linkplain #getCause() cause} of this exception while
     * exceptions in the chain are of type {@link ASTVisitException}. Return the
     * location of the exception embedded most deeply within this chain.
     *
     * If the cause of this exception is not a {@link ASTVisitException}, return
     * null.
     * @param e
     *
     * @return the located of the most embedded {@link ASTVisitException} along
     *         the cause chain, <code>null</code> if cause is not of that type.
     */
    public @Nullable String getCauseLocation(Throwable ex) {
        String result = null;
        while(ex != null) {
            Throwable cause = ex.getCause();

            if (cause instanceof ASTVisitException) {
                ASTVisitException astEx = (ASTVisitException) cause;
                result = astEx.getLocation().getLocation();
            }

            ex = cause;
        }
        return result;
    }

    @Override
    public String getDefaultExtension() {
        return "p";
    }

    @Override
    public String getDescription() {
        return "ivil's native format";
    }

}
