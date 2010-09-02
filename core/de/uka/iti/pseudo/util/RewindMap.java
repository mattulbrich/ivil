package de.uka.iti.pseudo.util;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class RewindMap<K, V> extends AbstractMap<K,V> {

    private static class Entry<K,V> implements Map.Entry<K,V> {
        static enum Type { CLEAR, PUT, REMOVE };
        private K key;
        private V value;
        private boolean hidden;
        public Entry<K, V> hidesEntry;
        private Entry<K,V> next;
        private Type type;
        
        @Override
        public K getKey() {
            return key;
        }
        
        @Override
        public V getValue() {
            return value;
        }
        
        @Override
        public V setValue(V value) {
            V oldVal = this.value;
            this.value = value;
            return oldVal;
        }
        
        @Override
        public String toString() {
            return type + " " + getKey() + (hidden ? "(H)" : "") + "=" + getValue()
                    + (next != null ? ", " + next.toString() : ""); 
        }
    }
    
    private Entry<K,V> head = null;
    
    private int pos = 0;
    
    private int size = 0;
    
    private Entry<K,V> getEntry(Object key) {
        
        for(Entry<K,V> e = head; e != null; e = e.next) {
            if(e.type == Entry.Type.CLEAR)
                return null;
            
            if(e.key.equals(key)) {
                return e;
            }
        }
        
        return null;
    }
    
    public int getPosition() {
        return pos;
    }
    
    public void rewindTo(int p) {
        if(p < 0 || p > pos)
            throw new IndexOutOfBoundsException("position " + p + " not within 0.." + pos);
        
        if(p == 0) {
            head = null;
            pos = 0;
            size = 0;
        } else {
            while(pos > p) {
                if(head.hidesEntry != null)
                    head.hidesEntry.hidden = false;
                head = head.next;
                pos --;
            }
            size = recalcSize();
        }
        
        assert head == null || !head.hidden;
    }
    
    private int recalcSize() {
        int res = 0;
        
        for (Entry<K, V> e = head; e != null; e = e.next) {
            switch (e.type) {
            case CLEAR:
                return res;
            case PUT:
                if (!e.hidden) {
                    res++;
                }
                break;
            }
        }

        return res;
    }

    public boolean containsKey(Object key) {
        Entry<K, V> entry = getEntry(key);
        return entry != null;
    }

    public V get(Object key) {
        Entry<K, V> entry = getEntry(key);
        if(entry != null) {
            return entry.value;
        } else {
            return null;
        }
    }
    
    public void clear() {
        Entry<K,V> newEntry = new Entry<K, V>();
        newEntry.key = null;
        newEntry.hidden = false;
        newEntry.type = Entry.Type.CLEAR;
        newEntry.next = head;
        
        head = newEntry;
        pos ++;
        size = 0;
    }

    public V remove(Object key) {
        Entry<K, V> oldEntry = getEntry(key);
        
        if(oldEntry == null) {
            return null;
        }
        
        K kkey = oldEntry.getKey();
        
        Entry<K,V> newEntry = new Entry<K, V>();
        newEntry.key = kkey;
        newEntry.value = null;
        newEntry.hidden = false;
        newEntry.hidesEntry = oldEntry;
        newEntry.type = Entry.Type.REMOVE;
        newEntry.next = head;
        
        oldEntry.hidden = true;
        
        head = newEntry;
        pos ++;
        if(oldEntry.type != Entry.Type.REMOVE)
            size --;
        
        return oldEntry.getValue();
    }
    
    public V put(K key, V value) {
        Entry<K, V> oldEntry = getEntry(key);
        
        Entry<K,V> newEntry = new Entry<K, V>();
        newEntry.key = key;
        newEntry.value = value;
        newEntry.hidden = false;
        newEntry.hidesEntry = oldEntry;
        newEntry.type = Entry.Type.PUT;
        newEntry.next = head;
        
        head = newEntry;
        pos ++;
        
        if(oldEntry != null) {
            oldEntry.hidden = true;
            if(oldEntry.type == Entry.Type.REMOVE)
                size ++;
            return oldEntry.getValue();
        } else {
            size ++;
            return null;
        }
    }

    private class EntrySet extends AbstractSet<Map.Entry<K,V>> {

        @Override
        public Iterator<Map.Entry<K,V>> iterator() {
            return new Iterator<Map.Entry<K, V>>() {
                Entry<K,V> nextEntry = calcNext(head);
                
                @Override
                public boolean hasNext() {
                    return nextEntry != null;
                }
                
                private Entry<K,V> calcNext(Entry<K,V> entry) {
                    while(entry != null && (entry.hidden || entry.type == Entry.Type.REMOVE)) {
                        entry = entry.next;
                    }
                    
                    if(entry != null && entry.type == Entry.Type.CLEAR) {
                        return null;
                    } else {
                        return entry;
                    }
                }

                @Override
                public Entry<K, V> next() {
                    Entry<K, V> ret = nextEntry;
                    
                    if(ret == null)
                        throw new NoSuchElementException();

                    nextEntry = calcNext(nextEntry.next);

                    return ret;
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }};
        }

        @Override
        public int size() {
            return size;
        }
        
    }
    
    @Override
    public int size() {
        return size;
    }
    
    @Override
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        return new EntrySet();
    }
   

}