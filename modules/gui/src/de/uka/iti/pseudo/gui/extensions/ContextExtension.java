/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.gui.extensions;

import nonnull.NonNull;
import de.uka.iti.pseudo.environment.Mappable;
import de.uka.iti.pseudo.gui.ProofCenter;

/**
 * The interface ContextExtension is the entry point to an extension to the
 * context menus.
 *
 * Implement this interface and register your class as a plugin to extend the
 * context menus of proof tree, goal list and sequent view.
 *
 * @author mattias ulbrich
 */

public interface ContextExtension extends Mappable<String> {

    /**
     * Gets the name of this extension.
     *
     * Used as menu entry
     *
     * @return a constant string
     */
    @Override
    public @NonNull String getKey();

    /**
     * Gets the description of this macro.
     *
     * Used as tooltip.
     *
     * @return a constant string
     */
    public @NonNull String getDescription();

    /**
     * Should this extension be offered to the user?
     *
     * @param proofCenter
     *            the proof center to which the situation belongs
     *
     * @return <code>true</code>, if the extension should be offered to the user
     */
    public boolean shouldOffer(ProofCenter proofCenter);

    /**
     * Run this extension.
     *
     * This method is allowed change the proof by applying rules to it.
     *
     * This method will only called after a check that
     * {@link #shouldOffer(ProofCenter)} returned <code>true</code>.
     *
     * @param proofCenter
     *            the proof center to which the situation belongs
     */
    public void run(ProofCenter proofCenter) throws Exception;
}