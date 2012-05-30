/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.auto.strategy.hint;

import java.util.List;

import de.uka.iti.pseudo.auto.strategy.StrategyException;
import de.uka.iti.pseudo.environment.Environment;
import de.uka.iti.pseudo.environment.Mappable;

/**
 * The Interface ProofHint is the entry point of the plugin "proofHint".
 * 
 * ProofHints have a single operation: Create a {@link HintRuleAppFinder} from a
 * list of hint arguments.
 * 
 * @see HintRuleAppFinder
 * 
 * @ivildoc "Proof hint" <h1>Proof hints</h1>
 * 
 * Proof hints allow the user to dynamically configure the strategy in use. They
 * are usually added as annotations to <tt>assert</tt> statements. The branch
 * handling the proof obligation for the asserted formula is then treated
 * differently to the rest of the proof. The exact nature of this different
 * behaviour depends on the configuration of the hints. They can take string
 * arguments.
 */
public interface ProofHint extends Mappable<String> {

    /**
     * Creates the a new rule app finder for the given parameters.
     * 
     * The first element of the list of arguments need to equal the
     * {@link #getKey() key} of this object
     * 
     * @param env
     *            the environment in which the creation takes place
     * 
     * @param arguments
     *            the hint arguments. The first elements needs to be the name
     * @return a freshly created object
     * 
     * @throws StrategyException
     *             The creation may choose to fail, for instance if the
     *             arguments are ill-formed.
     */
    public HintRuleAppFinder createRuleAppFinder(Environment env,
            List<String> arguments) throws StrategyException;
}
