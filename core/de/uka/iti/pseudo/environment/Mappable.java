/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.environment;

/**
 * Any class implementing this interface allows to store instances in a map
 * indexed by a key. The retrieval can then be done by the key which is usually
 * faster.
 */
public interface Mappable {
    
    /**
     * Gets the key under which this object can be retrieved from a map.
     * It must be unique for different objects.
     * 
     * @return a nonnull reference
     */
    public Object getKey();
    
}
