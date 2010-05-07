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
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import nonnull.NonNull;

import de.uka.iti.pseudo.term.Type;

/**
 * The Class AppendMap is an implementation of the Map interface which performs
 * well in a scenario in which
 * <ol>
 * <li>The number of entries to the map is rather small (&lt; 10)
 * <li>The values for keys are seldomly changed.
 * <li>Snapshooting (i.e. calls to clone()) should be cheap
 * </ol>
 * 
 * AppendMaps keeps data as a singly-linked list of {@link LinkedEntry} objects.
 * Putting a new key into the map is achieved by <b>prepending</b> a new entry
 * to the list of entries. Overwriting the value for a key is performed by
 * partially copying the list and changing the entry in the copied list, always
 * leaving the original list untouched. Deletion is not supported.
 * 
 * <p>
 * These procedures ensure that an existing linked list is never changed.
 * Cloning can therefore be performed by referencing to the <b>same</b> (i.e.
 * identical) entry list.
 * 
 */
public class AppendMap<K, V> extends AbstractMap<K, V> implements Cloneable {
    
    /**
     * The Class LinkedEntry provides the means to implement the singly-linked list of entries
     */
    private static class LinkedEntry<K,V> implements Entry<K,V> {
        
        /*
         * The key / value pair
         */
        private K key;
        private V value;
        
        /*
         * The reference to the next entry in the list. Null if it is the tail
         */
        private LinkedEntry<K,V> next;

        /*
         * Instantiates a new linked entry from some values.
         */
        private LinkedEntry(K key, V value, LinkedEntry<K, V> next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }

        /*
         * Instantiates a new linked entry which is a shallow copy of an existing entry. 
         */
        private LinkedEntry(LinkedEntry<K, V> entry) {
            this.value = entry.value;
            this.key = entry.key;
            this.next = entry.next;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        /**
         * this entry implementation does not support pubilc setting of values.
         * It might corrupt the data.
         */
        public V setValue(V value) {
            throw new UnsupportedOperationException();
        }
    }
    
    /**
     * The head of the linked list of entries
     */
    private LinkedEntry<K,V> head;
    
    //@ invariant head == null ==> size == 0;
    
    /**
     * The length of the list starting in head
     * (actually a redundant field)
     */
    private int size;
    
    /**
     * Creates a an empty map.
     */
    public AppendMap() {
        this.size = 0;
        this.head = null;
    }
    
    /**
     * create a map with initial content.
     * Add all entries from <code>map</code> into this map.
     * @param map a map of the same type
     */
    public AppendMap(@NonNull Map<K, V> map) {
        this();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    public Set<Entry<K, V>> entrySet() {
        return new AbstractSet<Entry<K,V>>() {
            public Iterator<java.util.Map.Entry<K, V>> iterator() {
                return new Iterator<Entry<K,V>>() {
                    LinkedEntry<K,V> current = head; 
                    public boolean hasNext() {
                        return current != null;
                    }

                    public java.util.Map.Entry<K, V> next() {
                        if(current == null)
                            throw new NoSuchElementException();
                        
                        Entry<K, V> result = current;
                        current = current.next;
                        return result;
                    }

                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }

            public int size() {
                return size;
            } 
        };
    }
    
    // this is O(n) in any case.
    /**
     * store a key/value pair into the map.
     * 
     * <p>
     * If the key is not yet present prepend a new entry to the list. If it is
     * already present clone the list upto the key's entry and then alter the
     * entry in the copied list.
     * 
     * @param key
     *            non-null key
     * @param value
     *            nullable key
     */
    public V put(K key, V value) {
        
        if(key == null)
            throw new NullPointerException("this map does not support null keys");
        
        if(containsKey(key)) {
            head = new LinkedEntry<K, V>(head);
            LinkedEntry<K, V> current = head;
            while(!current.key.equals(key)) {
                current.next = new LinkedEntry<K, V>(current.next);
                current = current.next;
            }
            
            assert current != null;
            
            V old = current.value;
            current.value = value;
            return old;
        } else {
            head = new LinkedEntry<K, V>(key, value, head);
            size ++;
            return null;            
        }
    };
    
    @SuppressWarnings("unchecked") 
    public AppendMap<K,V> clone() {
        try {
            return (AppendMap<K, V>) super.clone();
        } catch (CloneNotSupportedException e) {
            // cannot appear
            throw new Error(e);
        }
    }
    
    public void clear() {
        head = null;
        size = 0;
    }
}
