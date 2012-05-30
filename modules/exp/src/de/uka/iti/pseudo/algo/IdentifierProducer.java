/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.algo;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class IdentifierProducer is used to generate unique statement labels by
 * counting up.
 *
 * There is a counter for every prefix.
 */
public class IdentifierProducer {

    private final Map<String, Integer> counterMap = new HashMap<String, Integer>();

    public String makeIdentifier(String type) {

        Integer counter = counterMap.get(type);
        if(counter == null) {
            counter = 0;
        }

        String result = type + counter;

        counterMap.put(type, counter+1);

        return result;
    }

}
