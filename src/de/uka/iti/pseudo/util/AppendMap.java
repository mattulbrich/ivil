package de.uka.iti.pseudo.util;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Map.Entry;

public class AppendMap<K, V> extends AbstractMap<K, V> {
    
    private static class LinkedEntry<K,V> implements Entry<K,V> {
        
        V value;
        K key;
        LinkedEntry<K,V> next;

        public LinkedEntry(K key, V value, LinkedEntry<K, V> next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }

        public LinkedEntry(LinkedEntry<K, V> entry) {
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

        public V setValue(V value) {
            V old = this.value;
            this.value = value;
            return old;
        }
    }
    
    private LinkedEntry<K,V> head;
    private int size;
    
    public AppendMap() {
        this.size = 0;
        this.head = null;
    }
    
    public AppendMap(LinkedEntry<K, V> head, int size) {
        this.head = head;
        this.size = size;
    }

    public AppendMap<K,V> clone() {
        return new AppendMap<K,V>(head, size);
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
    
    // TODO What if the key is already present ??
    // --> do not increase size!
    // bad thing: O(n)
    public V put(K key, V value) {
        
        if(key == null)
            throw new NullPointerException("this map does not support null keys");
        
        if(containsKey(key)) {
            head = new LinkedEntry<K, V>(head);
            LinkedEntry<K, V> current = head;
            while(current.key != key) {
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
    
}
