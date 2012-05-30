/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.environment.boogie;

/**
 * Same as C++ std::pair
 * 
 * @author timm.felden@felden.com
 * 
 * @param <T1>
 * @param <T2>
 */
public final class Pair<T1, T2> {
    public final T1 first;
    public final T2 second;

    Pair(T1 first, T2 second) {
        this.first = first;
        this.second = second;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Pair) {
            Pair<T1, T2> p = (Pair<T1, T2>) obj;
            return first.equals(p.first) && second.equals(p.second);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return first.hashCode() + second.hashCode();
    }

    @Override
    public String toString() {
        return "Pair[" + first + "; " + second + "]";
    }
}
