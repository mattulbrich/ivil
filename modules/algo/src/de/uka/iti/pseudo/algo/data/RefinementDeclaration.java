/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.algo.data;

import java.util.HashMap;
import java.util.Map;

public class RefinementDeclaration {

    private final Map<String, String> couplingInvariantMap = new HashMap<String, String>();
    private final Map<String, String> couplingVariantMap = new HashMap<String, String>();
    private final String abstrAlgoName;
    private final String concrAlgoName;

    public RefinementDeclaration(String abstrProg, String concrProg) {
        this.abstrAlgoName = abstrProg;
        this.concrAlgoName = concrProg;
    }

    public void putCouplingInvariant(String key, String value) {
        if(couplingInvariantMap.containsKey(key)) {
            throw new IllegalStateException("Coupling invariant for " + key + " already defined");
        }
        couplingInvariantMap.put(key, value);
    }

    public void putCouplingVariant(String key, String value) {
        if(couplingVariantMap.containsKey(key)) {
            throw new IllegalStateException("Coupling variant for " + key + " already defined");
        }
        couplingVariantMap.put(key, value);
    }

    public String getCouplingInvariant(String name) {
        String result = couplingInvariantMap.get(name);
        if(result == null) {
            throw new IllegalStateException("Coupling invariant " + name + " not defined");
        }
        return result;
    }

    public String getCouplingVariant(String name) {
        String result = couplingVariantMap.get(name);
        if(result == null) {
            throw new IllegalStateException("Coupling variant " + name + " not defined");
        }
        return result;
    }

    public String getAbstrAlgoName() {
        return abstrAlgoName;
    }

    public String getConcrAlgoName() {
        return concrAlgoName;
    }

}
