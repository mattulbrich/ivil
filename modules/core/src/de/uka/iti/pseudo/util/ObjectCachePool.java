/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2010 Universitaet Karlsruhe, Germany
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.util;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import checkers.nullness.quals.NonNull;

/**
 * This class implements an object pool similar to the mechanism used in
 * {@link String#intern()}.
 * 
 * It helps ensuring that for immutable objects only one object is created and
 * kept in memory.
 * 
 * <p>
 * Typical usage looks like:
 * 
 * <pre>
 * class Immutable {
 *     Immutable(X x) { ... }
 * 
 *     public boolean equals(Object o) { ... }
 * 
 *     public int hashCode() { ... }}
 * 
 * class ImmutableFactory {
 *     ObjectCachePool pool = new ObjectCachePool();
 * 
 *     Immutable make(X x) {
 *         return pool.cache(new Immutable(x));
 *     }
 * }
 * </pre>
 * 
 * Please note that cached objects <b>must</b> override the methods
 * {@link Object#equals(Object)} and {@link Object#hashCode()}.
 * 
 * <p>
 * Internally a {@link WeakHashMap} is used. If the runtime environment does no
 * longer contain a reference to a representative, it is removed from the cache.
 * The cache does, hence, not keep unneeded objects.
 * 
 * <p>
 * The implementation is thread-safe.
 */
public final class ObjectCachePool {
    
    /**
     * The pool to store references in.
     */
    Map<Object, Object> thePool =
        Collections.synchronizedMap(new WeakHashMap<Object, Object>());
    
    // invariant key instanceof T ==> thePool.get(key) instanceof T;

    /**
     * Get the canonical representative of an object from the cache.
     * 
     * If there is no element in the cache witch is
     * {@linkplain Object#equals(Object) equal} to the argument, the argument is
     * added to the cache and its reference returned. If, however, an equal
     * object is found in the cache, that reference is returned.
     * 
     * If the argument is <code>null</code>, <code>null</code> is returned.
     * 
     * @param instance
     *            the instance
     * 
     * @return <code>null</code> iff argument was null, a reference {@code t}
     *         with {@code instance.equals(t)} otherwise.
     */
    @SuppressWarnings("unchecked")
    public <T> T cache(T instance) {
        
        if(instance == null)
            return null;
        
        Object result = thePool.get(instance);
        if(result == null) {
            thePool.put(instance, instance);
            result = instance;
        }
        return (T) result;
    }
    
    /**
     * Clear the cache.
     */
    public void clear() {
        thePool.clear();
    }
}
