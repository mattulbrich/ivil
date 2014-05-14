/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.environment;

import nonnull.NonNull;

/**
 * Any class implementing this interface provides a constant name for its instances.
 * This allows them to be added to dictionaries automatically.
 *
 * The name returned by {@link #getName()} must always be constant for an instance.
 */
public interface Named {

    /**
     * Gets the name under which this object can be retrieved from a map. It
     * must be unique amongst different objects which can end up in the same
     * map.
     *
     * The returned value must always be the same. (modulo equals)
     *
     * @return a nonnull reference
     */
    public @NonNull String getName();

}
