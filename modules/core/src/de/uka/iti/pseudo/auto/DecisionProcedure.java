/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.auto;

import java.io.IOException;
import java.util.Map;

import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.Mappable;
import de.uka.iti.pseudo.proof.ProofException;
import de.uka.iti.pseudo.term.Sequent;
import de.uka.iti.pseudo.util.Pair;

// TODO DOC
public interface DecisionProcedure extends Mappable<String> {

    public static final String SERVICE_NAME = "decisionProcedure";

    public Pair<Result, String> solve(Sequent sequent, Environment env, Map<String, String> properties)
       throws ProofException, IOException, InterruptedException;

    enum Result { VALID, NOT_VALID, UNKNOWN };

}
