/*
 * This file is part of
 *    ivil - Interactive Verification on Intermediate Language
 *
 * Copyright (C) 2009-2012 Karlsruhe Institute of Technology
 *
 * The system is protected by the GNU General Public License.
 * See LICENSE.TXT (distributed with this file) for details.
 */
package de.uka.iti.pseudo.util;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

import checkers.nullness.quals.AssertNonNullIfTrue;

import nonnull.Nullable;

/**
 * The Class AppendMap is an implementation of the Set interface which performs
 * well in a scenario in which
 * <ol>
 * <li>The number of entries to the set is rather small (&lt; 10)
 * <li>Snapshooting (i.e. calls to clone()) should be cheap
 * </ol>
 * 
 * AppendSets keep data as a singly-linked list of {@link Node} objects. Putting
 * a <i>new</i> element into the set is achieved by <b>prepending</b> a new
 * entry to the list of entries. If a value is already present, nothing is done.
 * Deletion is not supported.
 * 
 * <p>
 * These procedures ensure that an existing linked list is never changed.
 * Cloning can therefore be performed by referencing to the <b>same</b> (i.e.
 * identical) entry list.
 * 
 */
public class AppendSet<E> extends AbstractSet<E> implements Cloneable {
    
    /**
     * The internal class Node is a node in the singly linked list.
     */
    private static class Node<E> {
        
        public Node(E value, @Nullable Node<E> next) {
            this.value = value;
            this.next = next;
        }
        
        /**
         * The element in the set.
         */
        private final E value;
        
        /**
         * The next node in the list. <code>null</code> at the end of the list.
         */
        private final @Nullable Node<E> next;
    }

    /**
     * Simple iterator used for iterating this. The iterator is pretty agnostic
     * to concurrent changes.
     */
    private static class Itr<E> implements Iterator<E> {
        
        /**
         * The current node in the iteration.
         */
        private @Nullable Node<E> current;

        public Itr(@Nullable Node<E> head) {
            current = head;
        }

        @AssertNonNullIfTrue("current")
        public boolean hasNext() {
            return current != null;
        }

        @Override
        public E next() {
            if(!hasNext())
                throw new NoSuchElementException();
            
            E value = current.value;
            current = current.next;
            return value;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
    }
    
    /**
     * The beginning of the linked list.
     */
    private @Nullable Node<E> head = null;
    
    /**
     * The number of elments in this collection.
     */
    private int size = 0;

    @Override
    public Iterator<E> iterator() {
        return new Itr<E>(head);
    }

    @Override
    public int size() {
        return size;
    }

    /*
     * Enqueue an element by creating a new Node an prepending it to the list.
     * 
     * @see java.util.AbstractCollection#add(java.lang.Object)
     */
    @Override
    public boolean add(E e) {
        if(contains(e)) {
            return false;
        } else {
            head = new Node<E>(e, head);
            size ++;
            return true;
        }
    };
    
    @SuppressWarnings("unchecked")
    @Override
    public AppendSet<E> clone() {
        try {
            return (AppendSet<E>) super.clone();
        } catch (CloneNotSupportedException e) {
            // cannot appear
            throw new Error(e);
        }
    }
    
    /* 
     * To clear the set, just reset the head pointer and size. 
     * 
     * (non-Javadoc)
     * @see java.util.AbstractCollection#clear()
     */
    @Override
    public void clear() {
        size = 0;
        head = null;
    }

}
