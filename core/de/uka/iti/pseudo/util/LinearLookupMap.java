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
package de.uka.iti.pseudo.util;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import nonnull.NonNull;

/**
 * The Class LinearLookupMap is a very space optimised implementation of the
 * {@link Map} interface.
 * 
 * <p>
 * It is immutable and is produced from an existing map. Its keys and values are
 * stored in two separate arrays.
 * 
 * <p>
 * Lookup is performed linearily which is rather inefficient, but this class is
 * meant to be used for rather small maps with only a handful of elements.
 * 
 * <p>
 * Iterating is quite space consuming as for every iteration step a Entry
 * element has to be created. Those elements are only temporary however.
 */
public class LinearLookupMap<K, V> implements Map<K, V> {

    /*
     * the keys and values are stored in two separate arrays of the same size
     */
    private K[] keys;
    private V[] values;
    
    /**
     * create a new lookup map containing the entries of another map.
     * 
     * @param original the list to copy the entries from
     */
    @SuppressWarnings("unchecked") 
    public LinearLookupMap(@NonNull Map<K,V> original) {
        
        Set<Entry<K, V>> entrySet = original.entrySet();
        
        keys = (K[])new Object[entrySet.size()];
        values = (V[])new Object[entrySet.size()];
        
        int i = 0;
        for (Entry<K, V> entry : entrySet) {
            keys[i] = entry.getKey();
            values[i] = entry.getValue();
            i++;
        }
    }
    
    //
    // the remainder is merely the straight forward implementation of the Map interface
    //

    public void clear() {
        throw new UnsupportedOperationException("this map is unmodifiable");
    }

    public boolean containsKey(Object key) {
        for (int i = 0; i < keys.length; i++) {
            if(keys[i].equals(key))
                return true;
        }
        return false;
    }

    public boolean containsValue(Object value) {
        for (int i = 0; i < values.length; i++) {
            if(Util.equalOrNull(values[i], value))
                return true;
        }
        return false;
    }

    public V get(Object key) {
        for (int i = 0; i < keys.length; i++) {
            if(keys[i].equals(key))
                return values[i];
        }
        return null;
    }

    public boolean isEmpty() {
        return keys.length == 0;
    }

    public Set<K> keySet() {
        return new HashSet<K>(Arrays.asList(keys));
    }

    public V put(K key, V value) {
        throw new UnsupportedOperationException("this map is unmodifiable");
    }

    public void putAll(Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException("this map is unmodifiable");
    }

    public V remove(Object key) {
        throw new UnsupportedOperationException("this map is unmodifiable");
    }

    public int size() {
        return keys.length;
    }

    public Collection<V> values() {
        return Util.readOnlyArrayList(values);
    }

    public Set<Map.Entry<K, V>> entrySet() {
        return new EntrySet();
    }
    
    private class EntrySet extends AbstractSet<Map.Entry<K,V>> {

        public Iterator<java.util.Map.Entry<K, V>> iterator() {
            return new Itr();
        }

        public int size() {
            return keys.length;
        }
        
        private class Itr implements Iterator<Entry<K, V>> {
            
            int next;

            public boolean hasNext() {
                return next < size();
            }

            public Entry<K, V> next() {
                Entry<K, V> retval = new AbstractMap.SimpleImmutableEntry<K, V>(keys[next], values[next]);
                next ++;
                return retval;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
            
        }
        
    }
    
    @Override 
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (int i = 0; i < keys.length; i++) {
            sb.append(keys[i]).append("=").append(values[i]);
            if(i < keys.length - 1)
                sb.append(", ");
        }
        sb.append("}");
        return sb.toString();
    }
}
