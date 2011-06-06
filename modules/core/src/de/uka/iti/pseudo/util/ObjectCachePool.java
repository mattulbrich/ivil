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

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
 * The map is synchronised using R/W locks, allowing for concurrent writes.
 *  
 * TODO Get rid of the soft references. ...
 * 
 * <p>
 * The implementation is thread-safe.
 */
public final class ObjectCachePool {
    
    /**
     * The pool to store references in.
     */
    private Map<Object, SoftReference<Object>> thePool =
        new WeakHashMap<Object, SoftReference<Object>>();
    
    // invariant key instanceof T ==> thePool.get(key) instanceof T;
    
    private ReadWriteLock lock =
        new ReentrantReadWriteLock();

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
        
        lock.readLock().lock();
        try {
            Reference ref = thePool.get(instance);
            Object result;
            if(ref == null)
                result = null;
            else
                result = ref.get();
                
            if(result == null) {
                lock.writeLock().lock();
                try {
                    thePool.put(instance, new SoftReference(instance));
                } finally {
                    lock.writeLock().unlock();
                }
                result = instance;
            }
            return (T) result;
        } finally {
            lock.readLock().unlock();
        }
        
    }
    
    /**
     * Clear the cache.
     */
    public void clear() {
        thePool.clear();
    }
}
