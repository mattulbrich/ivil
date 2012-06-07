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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import nonnull.Nullable;

// TODO: Auto-generated Javadoc
/**
 * A set implementation providing a transactional rewind operation.
 *
 * <p>
 * This implementation wraps another set implementation and delegates all calls
 * to the wrapped object. For all modifying operations, information is stored to
 * be able undo the operation afterwards. (cf. Command Design Pattern). This
 * implementation is not thread-safe.
 *
 * <p>
 * The current position on the transaction timeline can be queried using
 * {@link #getRewindPosition()}. A transactional rollback can be performed using
 * {@link #rewindTo(int)} with a rewind position as argument.
 *
 * <h2>Example</h2>
 * The following code sniplet
 *
 * <pre>
 * RewindMap&lt;String, String&gt; map = new RewindMap&lt;String, String&gt;();
 * map.put(&quot;Hello&quot;, &quot;World&quot;);
 * int rewindPos = map.getRewindPosition();
 * map.put(&quot;Hello&quot;, &quot;New World&quot;);
 *
 * System.out.println(map.get(&quot;Hello&quot;));
 * map.rewindTo(rewindPos);
 * System.out.println(map.get(&quot;Hello&quot;));
 * map.rewindTo(0);
 * System.out.println(map.get(&quot;Hello&quot;));
 * </pre>
 *
 * will have the following output:
 *
 * <pre>
 * New World
 * World
 * null
 * </pre>
 *
 * @param <E>
 *            the element type
 */
public class RewindSet<E> implements Set<E> {

    /**
     * Helper class to implement a linked list of rewind transaction command.
     *
     * @param <E>
     *            the element type
     */
    private static class RewindTransaction<E> {

        /** The element to operate with. */
        private final E element;

        /** Pointer to the next history entry. */
        private final @Nullable
        RewindTransaction<E> next;

        /** The flag whether the value is present or not. */
        private final boolean present;

        /**
         * Instantiates a new rewind transaction.
         *
         * @param key
         *            the key
         * @param present
         *            the presence flag
         * @param next
         *            the next history element
         */
        public RewindTransaction(E key, boolean present, RewindTransaction<E> next) {
            this.element = key;
            this.present = present;
            this.next = next;
        }
    }

    /**
     * The wrapped set implementation.
     */
    private final Set<E> wrappedSet;

    /**
     * The rewind history to roll back at a later time.
     */
    private RewindTransaction<E> rewindHistory;

    /**
     * The size of the rewind history.
     */
    private int rewindSize;

    /**
     * Instantiates a new rewind set.
     *
     * <p>
     * A freshly created {@link HashSet} is used as set to which method calls
     * are delegated.
     */
    public RewindSet() {
        this(new HashSet<E>());
    }

    /**
     * Instantiates a new rewind set.
     *
     * <p>
     * The given argument is used as wrapped set to which method calls are
     * delegated.
     *
     * @param wrappedSet
     *            the delegation goal
     */
    public RewindSet(Set<E> wrappedSet) {
        this.wrappedSet = wrappedSet;
    }

    /**
     * Adds an entry to the history.
     *
     * The current state of the element is stored so that it can be restored
     * later.
     *
     * @param elem
     *            the element to store
     */
    private void addHistory(E elem) {
        rewindHistory = new RewindTransaction<E>(elem, contains(elem), rewindHistory);
        rewindSize++;
    }

    /**
     * Gets the current rewind position.
     *
     * <p>
     * The result can be used to a later call to {@link #rewindTo(int)}.
     *
     * @return a non-negative integer
     */
    public int getRewindPosition() {
        return rewindSize;
    }

    /**
     * Rewind to a former state of the map.
     *
     * <p>
     * The argument must not be negative or greater than the
     * {@link #getRewindPosition() current rewind position}.
     *
     * <p>
     * Usually, you will use the return value of an earlier call to
     * {@link #getRewindPosition()} here.
     *
     * @param position
     *            a non-negative integer, at most the current rewind position
     */
    public void rewindTo(int position) {
        if (position < 0) {
            throw new IllegalArgumentException("Should be non-negative: " + position);
        }

        if (position > rewindSize) {
            throw new IllegalArgumentException("Rewind request " + position
                    + " beyond transaction marker " + rewindSize);
        }

        while (rewindSize > position) {
            if (rewindHistory.present) {
                wrappedSet.add(rewindHistory.element);
            } else {
                wrappedSet.remove(rewindHistory.element);
            }
            rewindSize--;
            rewindHistory = rewindHistory.next;
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * This implementation can raise the rewind position by 1.
     */
    @Override
    public boolean add(E e) {
        if (!contains(e)) {
            addHistory(e);
            return wrappedSet.add(e);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * This implementation can raise the rewind position by 1.
     */
    @Override
    public boolean remove(Object o) {
        if (contains(o)) {
            addHistory((E) o);
            return wrappedSet.remove(o);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * This implementation can raise the rewind position by the number of
     * elements in c.
     */
    @Override
    public boolean addAll(Collection<? extends E> c) {
        boolean added = false;
        for (E e : c) {
            added |= add(e);
        }
        return added;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * This implementation throws an {@link UnsupportedOperationException}.
     */
    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * This implementation can raise the rewind position by the number of
     * elements in c.
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        boolean removed = false;
        for (Object elem : c) {
            removed |= remove(elem);
        }
        return removed;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * This implementation can raise the rewind position by the number of
     * elements in this set.
     */
    @Override
    public void clear() {
        for (E elem : this) {
            addHistory(elem);
        }
        wrappedSet.clear();
    }

    //
    // -- the remainder merely delegates
    //

    /*
     * (non-Javadoc)
     *
     * @see java.util.Set#size()
     */
    @Override
    public int size() {
        return wrappedSet.size();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Set#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return wrappedSet.isEmpty();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Set#contains(java.lang.Object)
     */
    @Override
    public boolean contains(Object o) {
        return wrappedSet.contains(o);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * This implementation returns an Iterator which does not support the
     * {@link #remove(Object)} operation.
     */
    @Override
    public Iterator<E> iterator() {
        return new NoRemoveIter<E>(wrappedSet.iterator());
    }

    /**
     * The Class NoRemoveIter is used to wrap an iterator and make it immutable.
     *
     * @param <E>
     *            the element type
     */
    private static class NoRemoveIter<E> implements Iterator<E> {

        /** The iterator to wrap. */
        private final Iterator<E> iterator;

        /**
         * Instantiates a new iterator.
         *
         * @param iterator
         *            the iterato to wrap
         */
        public NoRemoveIter(Iterator<E> iterator) {
            this.iterator = iterator;
        }

        /**
         * {@inheritDoc}
         *
         * <P>
         * This implementations throws a {@link UnsupportedOperationException}.
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        /*
         * (non-Javadoc)
         *
         * @see java.util.Iterator#hasNext()
         */
        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        /*
         * (non-Javadoc)
         *
         * @see java.util.Iterator#next()
         */
        @Override
        public E next() {
            return iterator.next();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Set#toArray()
     */
    @Override
    public Object[] toArray() {
        return wrappedSet.toArray();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Set#toArray(T[])
     */
    @Override
    public <T> T[] toArray(T[] a) {
        return wrappedSet.toArray(a);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Set#containsAll(java.util.Collection)
     */
    @Override
    public boolean containsAll(Collection<?> c) {
        return wrappedSet.containsAll(c);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        return wrappedSet.equals(o);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return wrappedSet.hashCode();
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * This implementation returns the {@link Object#toString()} result of the
     * wrapped set and appends (after a slash) the length of the rewind history.
     */
    @Override
    public String toString() {
        return wrappedSet.toString() + "/" + rewindSize;
    }
}