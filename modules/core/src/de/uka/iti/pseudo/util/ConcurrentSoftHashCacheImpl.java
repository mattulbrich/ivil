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

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import nonnull.NonNull;
import nonnull.Nullable;

/**
 * The Class ConcurrentSoftHashCacheImpl implements a cache hash table.
 *
 * It allows storage of objects and later retrieval of them. It is similar to a
 * Map in which all keys and values coincide.
 *
 * This is useful to have a canonical representative for a number of equal
 * objects. You can add one to the cache using {@link #put(Object)} and later
 * retrieve that one by {@link #get(Object)} on an equal object.
 *
 * The class is designed for concurrency: The hash table is divided into
 * different parts which each have a lock on their own. Statistically the map is
 * hence blocking only in rare cases. Also: read accesses are never blocked.
 *
 * It uses {@link SoftReference} to store values. If garbage collection decides
 * to free the memory, this cache does not prevent it from doing so.
 *
 * The different parts of the hash table are called {@link #segments}. The class
 * {@link Segment} implements its behaviour. The lower
 * {@link #countSegmentsBits} bits of the hash code decide on the segment, and
 * the upper on the index within the table.
 *
 * The elements here are package readable for inspection by test cases. This
 * class is not meant for public usage. It is wrapped, for instance, by
 * {@link ObjectCachePool}.
 *
 * This implementation assumes that {@link Object#hashCode()} methods are
 * implemented efficiently (does not store hash values).
 *
 * @author mattias ulbrich
 *
 * @see ObjectCachePool
 * @see WeakHashMap
 * @see SoftReference
 * @see ConcurrentHashMap
 */
class ConcurrentSoftHashCacheImpl {

    final Segment segments[];
    final int maskSegmentBits;
    final int countSegmentsBits;
    final int initialSizePerSegmentBits;

    /*
     * An element of the linked list to resolve collisions
     */
    static class Chain extends SoftReference<Object> {
        volatile @Nullable Chain next;
        final @Nullable Chain /*@NonNull*/ [] table;
        final int index;

        Chain(Object referent, ReferenceQueue<Object> q,
                Chain[] table, int index) {
            super(referent, q);
            this.table = table;
            this.index = index;
            this.next = null;
        }

        int len() {
            return next == null ? 1 : (next.len() + 1);
        }
    }

    /*
     * A part of the split hash table.
     */
    class Segment {

        volatile Chain table[];
        volatile int tableBits;
        volatile int threshold;
        volatile int count;
        final ReferenceQueue<Object> queue;
        final Lock lock;

        Segment() {
            this.tableBits = initialSizePerSegmentBits;
            this.table = new Chain[1 << tableBits];
            // threshold is 3/4
            this.threshold = (table.length * 3) >> 2;
            this.count = 0;
            this.queue = new ReferenceQueue<Object>();
            this.lock = new ReentrantLock();
        }


        /*
         * get an object: Find the index in the table and iterate over the
         * linked list. return the value if equal to argument.
         */
        @Nullable Object get(Object comparison, int hash) {
            removeStale();

            Chain[] myTable = table;
            int index = (hash >> countSegmentsBits) & (myTable.length - 1);
            for(Chain c = myTable[index]; c != null; c = c.next) {
                Object value = c.get();
                if(value != null && value.equals(comparison)) {
                    return value;
                }
            }

            return null;
        }

        /*
         * add a value. It does not check whether there is already an instance
         * present.
         * Resizes the table if needed.
         */
        private void put(Object object, int hash) {
            removeStale();

            lock.lock();
            try {
                if(count > threshold) {
                    tableBits ++;
                    table = resizeTable(table, 1 << tableBits);
                    threshold = (table.length * 3) >> 2;
                }

                addElement(table, object, hash);
                count ++;
            } finally {
                lock.unlock();
            }
        }

        /*
         * Use the ReferenceQueue to remove all entries which have been garbage
         * collected.
         */
        void removeStale() {
            Chain stale;
            boolean locked = false;
            try {
                while ( (stale = (Chain) queue.poll()) != null) {

                    if(!locked) {
                        lock.lock();
                        locked = true;
                    }

                    Chain c = stale.table[stale.index];

                    // the head is to be removed
                    if(c == stale) {
                        stale.table[stale.index] = c.next;
                        count --;
                        return;
                    }

                    while (c != null) {

                        if(c.next == stale) {
                            c.next = stale.next;

                            // do NOT set next to null, concurrent traversing of the
                            // chain might happen!
                            // stale.next = null;  // Help GC
                            count --;
                            break;
                        }

                        c = c.next;
                    }
                }
            } finally {
                if(locked) {
                    lock.unlock();
                }
            }
        }

        /*
         * double the size of the table and re-add all entries.
         */
        Chain[] resizeTable(Chain[] table, int newSize) {
            Chain[] newTable = new Chain[newSize];
            for (Chain chain : table) {
                while(chain != null) {
                    Object value = chain.get();
                    if(value != null) {
                        addElement(newTable, value, value.hashCode());
                    }
                    chain = chain.next;
                }
            }
            return newTable;
        }

        /*
         * add an entry to its appropriate place by adding it to the linked
         * list.
         */
        void addElement(Chain[] table, Object object, int hash) {
            int index = (hash >> countSegmentsBits) & (table.length - 1);
            Chain next = new Chain(object, queue, table, index);

            if(table[index] == null) {
                table[index] = next;
                return;
            }

            Chain c;
            for(c = table[index]; c.next != null; c = c.next) {
                ;
            }
            assert c.next == null;
            c.next = next;
        }


        /*
         * empty the table, and resize it to minimum to save space.
         */
        void clear() {
            lock.lock();
            try {
                this.table = new Chain[1 << initialSizePerSegmentBits];
                this.threshold = (table.length * 3) >> 2;
                this.count = 0;
            } finally {
                lock.unlock();
            }
        }

    }

    /**
     * Instantiates a new concurrent soft hash cache.
     *
     * @param countSegmentsBits
     *            the number of bits to use for segment arbitration. The number
     *            of segments will be {@code 1 << countSegmentsBits}.
     * @param initialSizePerSegmentBits
     *            the number of bits to use for the initial size of each
     *            segment. The size will be {@code 1 <<
     *            initialSizePerSegmentBits}.
     */
    public ConcurrentSoftHashCacheImpl(int countSegmentsBits, int initialSizePerSegmentBits) {

        if(countSegmentsBits < 0 ||
                countSegmentsBits >= 10 ||
                initialSizePerSegmentBits <= 0) {
            throw new IllegalArgumentException();
        }

        this.countSegmentsBits = countSegmentsBits;
        this.initialSizePerSegmentBits = initialSizePerSegmentBits;
        this.maskSegmentBits = (1 << countSegmentsBits) - 1;
        this.segments = new Segment[maskSegmentBits + 1];

        for (int i = 0; i < segments.length; i++) {
            segments[i] = new Segment();
        }

    }

    /**
     * Put a reference into the cache.
     *
     * Only immutable objects with sensible {@link Object#equals(Object)} and
     * {@link Object#hashCode()} implementations should be added.
     *
     * @param object
     *            a non-null reference
     */
    public void put(@NonNull Object object) {
        if(object == null) {
            throw new NullPointerException("cache does not support null");
        }

        int hash = object.hashCode();
        int segmentNo = hash & maskSegmentBits;
        Segment segment = segments[segmentNo];

        segment.put(object, hash);
    }

    /**
     * Gets the representative for the argument.
     *
     * @param object
     *            a non-null reference
     *
     * @return an object which is {@linkplain Object#equals(Object) equal} to
     *         {@code object}, or <code>null</code> if no such element is in the
     *         cache.
     */
    public @Nullable Object get(@NonNull Object object) {
        if(object == null) {
            throw new NullPointerException("cache does not support null");
        }

        int hash = object.hashCode();
        int segmentNo = hash & maskSegmentBits;
        Segment segment = segments[segmentNo];

        return segment.get(object, hash);
    }

    /**
     * Gets the number of entries in this cache.
     *
     * @return a non-negative integer.
     */
    public int size() {
        int result = 0;
        for (int i = 0; i < segments.length; i++) {
            result += segments[i].count;
        }
        return result;
    }

    /**
     * Empties the cache. All segments are reset to their original sizes to save
     * memory.
     */
    public void clear() {
        for (int i = 0; i < segments.length; i++) {
            segments[i].clear();
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * Give a summary of the content of the map. Segments are printed separately.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (Segment segment : segments) {
            sb.append("(");
            for (Chain chain : segment.table) {
                if(chain != null) {
                    sb.append(" ").append(chain.index).append(":");
                    do {
                        sb.append(" ").append(chain.get());
                        chain = chain.next;
                    } while(chain != null);
                }
            }
            sb.append(")");
        }
        sb.append("}");

        return sb.toString();
    }

}