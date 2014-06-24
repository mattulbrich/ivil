/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.parser.boogie;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.EnvironmentException;
import de.uka.iti.pseudo.environment.Program;
import de.uka.iti.pseudo.environment.ProofObligation;
import de.uka.iti.pseudo.environment.ProofObligationManager;
import de.uka.iti.pseudo.environment.boogie.EnvironmentCreationState;
import de.uka.iti.pseudo.environment.creation.EnvironmentCreationService;
import de.uka.iti.pseudo.term.Modality;
import de.uka.iti.pseudo.term.Term;
import de.uka.iti.pseudo.term.TermException;
import de.uka.iti.pseudo.util.Pair;
import de.uka.iti.pseudo.util.settings.Settings;

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
    public ProofObligationManager createEnvironment(InputStream inputStream, URL url)
           throws IOException, EnvironmentException {
        try {
            BPLParser p = new BPLParser(inputStream);
            EnvironmentCreationState s = new EnvironmentCreationState(p.parse(url));

            Environment env = s.make();
            return new ProofObligationManager(env, createProgramProblems(env));
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
            throw envEx;
        } catch (TermException e) {
            // should not be called actually
            throw new EnvironmentException("Error creating environment", e);
        }
    }

    private Map<String, ProofObligation> createProgramProblems(Environment env) throws TermException {

        final boolean termination =
                Settings.getInstance().getBoolean("pseudo.boogie.programTermination", true);
        Modality modality = termination ? Modality.BOX_TERMINATION : Modality.BOX;

        Term trueTerm = Environment.getTrue();
        Map<String, ProofObligation> problemSequents = new HashMap<String, ProofObligation>();

        for (Program p : env.getAllPrograms()) {
            ProofObligation po = new ProofObligation.ProgramPO(env, p, modality);
            problemSequents.put(po.getName(), po);
        }

        return problemSequents;
    }
}
