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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import nonnull.NonNull;
import nonnull.Nullable;

/**
 * A map implementation providing a transactional rewind operation.
 *
 * <p>
 * This implementation wraps another map implementation and delegates all calls
 * to the wrapped object. For all modifying operations, information is stored to
 * be able undo the operation afterwards. (cf. Command Design Pattern). The
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
 *   New World
 *   World
 *   null
 * </pre>
 *
 * @param <K>
 *            the type of the keys in the map
 * @param <V>
 *            the type of the values in the map
 */
public class RewindMap<K, V> implements Map<K, V> {

    /**
     * Helper class to implement a linked list of rewind transaction command.
     *
     * @param <K>
     *            the type of the key
     * @param <V>
     *            the type of the value
     */
    private static class RewindTransaction<K, V> {

        /**
         * The key to operate with.
         */
        private final K key;

        /**
         * The value to reset.
         */
        private final V value;

        /**
         * A pointer to the next record. <code>null</code> if this is the earliest
         * in the history.
         */
        private final RewindTransaction<K, V> next;

        /**
         * Presence state of the key.
         *
         * <code>true</code> if the key is present in the map (requires a put),
         * <code>false</code> if the key is absent (requires a remove)
         */
        private final boolean present;

        /**
         * Instantiates a new rewind transaction.
         *
         * @param key
         *            the key
         * @param present
         *            is the key present
         * @param value
         *            the value
         * @param next
         *            the next record, may be null
         */
        public RewindTransaction(K key, boolean present, V value,
                @Nullable RewindTransaction<K, V> next) {
            super();
            this.key = key;
            this.present = present;
            this.value = value;
            this.next = next;
        }
    }

    /**
     * The wrapped map.
     */
    private final @NonNull Map<K, V> wrappedMap;

    /**
     * The rewind history, used to restore old states of the map.
     */
    private @Nullable RewindTransaction<K, V> rewindHistory;

    /**
     * Number of elements in the rewindHistory.
     */
    private int rewindSize;

    /**
     * Instantiates a new rewind.
     *
     * The given argument is used as wrapped map to which method calls are
     * delegated.
     *
     * @param wrappedMap an arbitrary map.
     */
    public RewindMap(Map<K, V> wrappedMap) {
        this.wrappedMap = wrappedMap;
    }

    /**
     * Instantiates a new rewind map.
     *
     * <p>
     * A freshly created {@link HashMap} is used as map to which method calls
     * are delegated.
     */
    public RewindMap() {
        this(new HashMap<K, V>());
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
            throw new IllegalArgumentException("Should be non-negative: "
                    + position);
        }

        if (position > rewindSize) {
            throw new IllegalArgumentException("Rewind request " + position
                    + " beyond transaction position " + rewindSize);
        }

        while (rewindSize > position) {
            if (rewindHistory.present) {
                wrappedMap.put(rewindHistory.key, rewindHistory.value);
            } else {
                wrappedMap.remove(rewindHistory.key);
            }
            rewindSize--;
            rewindHistory = rewindHistory.next;
        }
    }

    /**
     * Adds an entry to the history.
     *
     * The current state of the key is stored so that it can be restored later.
     *
     * @param key
     *            the key to store
     */
    private void addHistory(K key) {
        rewindHistory = new RewindTransaction<K, V>(key, containsKey(key),
                get(key), rewindHistory);
        rewindSize++;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * This implementation can raise the rewind position by 1.
     */
    @Override
    public V put(K key, V value) {
        addHistory(key);
        return wrappedMap.put(key, value);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * This implementation will raise the rewind position by 1 if
     * the key is present in the map.
     */
    @Override
    public V remove(Object key) {
        if (containsKey(key)) {
            addHistory((K) key);
        }
        return wrappedMap.remove(key);
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * This implementation will raise the rewind position by the number of
     * entries in <code>m</code>.
     */
    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * This implementation will raise the rewind position by the number of
     * entries in the map.
     */
    @Override
    public void clear() {
        for (K key : keySet()) {
            addHistory(key);
        }
        wrappedMap.clear();
    }

    //
    // - the remainder is only delegating methods
    //

    /*
     * (non-Javadoc)
     *
     * @see java.util.Map#size()
     */
    @Override
    public int size() {
        return wrappedMap.size();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Map#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return wrappedMap.isEmpty();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    @Override
    public boolean containsKey(Object key) {
        return wrappedMap.containsKey(key);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    @Override
    public boolean containsValue(Object value) {
        return wrappedMap.containsValue(value);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Map#get(java.lang.Object)
     */
    @Override
    public V get(Object key) {
        return wrappedMap.get(key);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Map#keySet()
     */
    @Override
    public Set<K> keySet() {
        return Collections.unmodifiableSet(wrappedMap.keySet());
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Map#values()
     */
    @Override
    public Collection<V> values() {
        return Collections.unmodifiableCollection(wrappedMap.values());
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Map#entrySet()
     */
    @Override
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        return Collections.unmodifiableSet(wrappedMap.entrySet());
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        return wrappedMap.equals(o);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return wrappedMap.hashCode();
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * This implementation returns the {@link Object#toString()} result of the
     * wrapped map and appends (after a slash) the length of the rewind history.
     */
    @Override
    public String toString() {
        return wrappedMap.toString() + "/" + rewindSize;
    }
}
