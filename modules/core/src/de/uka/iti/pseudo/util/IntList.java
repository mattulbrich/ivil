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

import java.util.AbstractList;
import java.util.Arrays;

import nonnull.NonNull;

/**
 * The Class IntList implements a mutuable integer array of dynamic size.
 *
 * It implements the interface {@link java.util.List}{@code <Integer>} allowing
 * it to interoperate with instances of that type.
 *
 * The data is stored in a {@code int[]} array, however, and all operation are
 * kept free from boxing and unboxing if the interface methods are not used.
 */
public class IntList extends AbstractList<Integer> implements Cloneable {

    /**
     * An empty array to indicate an empty list.
     */
    private final static int[] EMPTY_ARRAY = new int[0];

    /**
     * The array storing the values. Intially this is the empty array.
     */
    private int[] array = EMPTY_ARRAY;

    /**
     * The actual number of entries in the list. Always:
     * {@code 0 <= size <= array.length}
     */
    private int size;

    /**
     * Ensure a certain capacity.
     *
     * @param minSize
     *            the minimum size to be ensured
     */
    private void ensureCapacity(int minSize) {
        modCount++;

        int oldCapacity = array.length;
        if (minSize > oldCapacity) {
            int newCapacity = (oldCapacity * 3)/2 + 1;
            if (newCapacity < minSize) {
                newCapacity = minSize;
            }

            // minCapacity is usually close to size, so this is a win:
            array = Arrays.copyOf(array, newCapacity);
        }
    }

    /**
     * Adds a value to the end of the list.
     *
     * @param value
     *            the value to add
     * @return <code>true</code>
     */
    public boolean add(int value) {
        ensureCapacity(size + 1);
        array[size] = value;
        size ++;
        return true;
    }

    /**
     * Inserts the specified value at the specified position in this list Shifts
     * the element currently at that position (if any) and any subsequent
     * elements to the right (adds one to their indices).
     *
     * @param index
     *            index at which the specified element is to be inserted
     * @param value
     *            value to be inserted
     * @throws IndexOutOfBoundsException
     *             if the index is out of range (
     *             <tt>index &lt; 0 || index &gt; size()</tt>)
     */
    public void add(int index, int value) {
        if (index > size || index < 0) {
            throw new IndexOutOfBoundsException(
                    "Index: "+index+", Size: "+size);
        }

        ensureCapacity(size + 1);
        System.arraycopy(array, index, array, index + 1,
                size - index);
        array[index] = value;
        size++;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * IntList does not support <code>null</code> values.
     *
     * @throws NullPointerException
     *             if the argument is <code>null</code>
     */
    @Override
    public boolean add(Integer e) {
        if(e == null) {
            throw new NullPointerException("IntList does not support null-values");
        }
        return add(e.intValue());
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * IntList does not support <code>null</code> values.
     *
     * @throws NullPointerException
     *             if the argument element is <code>null</code>
     */
    @Override
    public void add(int index, Integer element) {
        if(element == null) {
            throw new NullPointerException("IntList does not support null-values");
        }

        add(index, element.intValue());
    }

    /**
     * Appends all of the elements in the specified IntList to the end of this
     * list, in their original order.
     *
     * @param list
     *            another {@link IntList}
     * @throws NullPointerException
     *             if the specified collection is null
     */
    public void addAll(@NonNull IntList list) {
        ensureCapacity(size + list.size);
        System.arraycopy(list.array, 0, array, size, list.size);
        size = size + list.size;
    }

    /* (non-Javadoc)
     * @see java.util.AbstractList#clear()
     */
    @Override
    public void clear() {
        array = null;
        size = 0;
    }

    /* (non-Javadoc)
     * @see java.util.AbstractCollection#contains(java.lang.Object)
     */
    @Override
    public boolean contains(Object o) {
        if (o instanceof Integer) {
            int intVal = ((Integer) o).intValue();
            for (int i = 0; i < array.length; i++) {
                if(array[i] == intVal) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns <tt>true</tt> if this list contains all of the elements of the
     * specified integer list.
     *
     * @param c
     *            another {@link IntList}
     * @return <tt>true</tt> if this list contains all of the elements of the
     *         specified collection
     * @throws NullPointerException
     *             if the specified collection is null
     * @see #containsAll(java.util.Collection)
     */
    public boolean containsAll(IntList c) {
        for (int i = 0; i < c.size(); i++) {
            if(!contains(c.at(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * This operation always involves a boxing operation from <code>int</code>
     * to {@link Integer}.
     *
     * @see #at(int)
     */
    @Override
    public Integer get(int index) {
        return Integer.valueOf(array[index]);
    }

    /**
     * Returns the element at the specified position in this list. This
     * operation does not involve any boxing/unboxing operations.
     *
     * @param index
     *            index of the element to return
     * @return the element at the specified position in this list
     * @throws IndexOutOfBoundsException
     *             if the index is out of range (
     *             <tt>index &lt; 0 || index &gt;= size()</tt>)
     * @see #get(int)
     */
    public int at(int index) {
        return array[index];
    }

    /**
     * Returns the index of the first occurrence of the specified value in this
     * list, or -1 if this list does not contain the element. More formally,
     * returns the lowest index <tt>i</tt> such that <code>value == at(i)</code>
     * or -1 if there is no such index.
     *
     * @param value
     *            value to search for
     * @return the index of the first occurrence of the specified element in
     *         this list, or -1 if this list does not contain the element
     *
     * @see IntList#indexOf(Object)
     */
    public int indexOf(int value) {
        for (int i = 0; i < array.length; i++) {
            if(array[i] == value) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the index of the last occurrence of the specified value in this
     * list, or -1 if this list does not contain the element. More formally,
     * returns the highest index <tt>i</tt> such that
     * <code>value == at(i)</code> or -1 if there is no such index.
     *
     * @param value
     *            value to search for
     * @return the index of the last occurrence of the specified element in this
     *         list, or -1 if this list does not contain the element
     * @see IntList#lastIndexOf(Object)
     */
    public int lastIndexOf(int value) {
        for (int i = array.length - 1; i >= 0; i--) {
            if(array[i] == value) {
                return i;
            }
        }
        return -1;
    }

    /* (non-Javadoc)
     * @see java.util.AbstractCollection#remove(java.lang.Object)
     */
    @Override
    public boolean remove(Object element) {
        if (element instanceof Integer) {
            Integer intObj = (Integer) element;
            int intVal = intObj.intValue();
            return removeValue(intVal);
        }
        return false;
    }

    /**
     * Removes the first occurrence of the specified value from this list, if it
     * is present. If this list does not contain the value, it is unchanged.
     * More formally, removes the element with the lowest index <tt>i</tt> such
     * that <tt>value == at(i)</tt> (if such an element exists). Returns
     * <tt>true</tt> if this list contained the specified element (or
     * equivalently, if this list changed as a result of the call).
     *
     * @param intValue
     *            value to be removed from this list, if present
     * @return <tt>true</tt> if this list contained the specified element
     *
     * @see #remove(int)
     * @see #remove(Object)
     */
    private boolean removeValue(int intVal) {
        for (int index = 0; index < size; index++) {
            if (intVal == array[index]) {
                removeAt(index);
                return true;
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see java.util.AbstractList#remove(int)
     */
    @Override
    public Integer remove(int index) {
        modCount++;

        int oldValue = array[index];

        int numMoved = size - index - 1;
        if (numMoved > 0) {
            System.arraycopy(array, index+1, array, index,
                             numMoved);
        }
        return Integer.valueOf(oldValue);
    }

    /**
     * Removes the value at the specified position in this list (optional
     * operation). Shifts any subsequent elements to the left (subtracts one
     * from their indices). Returns the value that was removed from the list.
     *
     * <p>
     * Unlike {@link #remove(int)}, this returns a value not a boxing object.
     *
     * @param index
     *            the index of the element to be removed
     * @return the value previously at the specified position
     * @throws IndexOutOfBoundsException
     *             if the index is out of range (
     *             <tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    public int removeAt(int index) {
        modCount++;

        int oldValue = array[index];

        int numMoved = size - index - 1;
        if (numMoved > 0) {
            System.arraycopy(array, index+1, array, index,
                             numMoved);
        }
        return oldValue;
    }

    /* (non-Javadoc)
     * @see java.util.AbstractList#set(int, java.lang.Object)
     */
    @Override
    public Integer set(int index, Integer element) {
        if(element == null) {
            throw new NullPointerException("IntList does not support null-values");
        }

        return set(index, element.intValue());
    }

    /**
     * Replaces the value at the specified position in this list with the
     * specified value (optional operation).
     *
     * @param index
     *            index of the element to replace
     * @param value
     *            value to be stored at the specified position
     * @return the element previously at the specified position
     * @throws IndexOutOfBoundsException
     *             if the index is out of range (
     *             <tt>index &lt; 0 || index &gt;= size()</tt>)
     * @see #set(int, Integer)
     */

    public int set(int index, int value) {
        ensureCapacity(index + 1);
        int result = array[index];
        array[index] = value;
        return result;
    }

    /* (non-Javadoc)
     * @see java.util.AbstractCollection#size()
     */
    @Override
    public int size() {
        return size;
    }

    /* (non-Javadoc)
     * @see java.util.AbstractCollection#toArray()
     */
    @Override
    public Integer[] toArray() {
        Integer[] result = new Integer[size];
        for (int i = 0; i < array.length; i++) {
            result[i] = Integer.valueOf(array[i]);
        }
        return result;
    }

    /**
     * Returns an array containing all of the values in this list in proper
     * sequence (from first to last element).
     *
     * <p>The returned array will be "safe" in that no references to it are
     * maintained by this list.
     *
     * <p>This method acts as bridge between array-based and collection-based
     * APIs.
     *
     * <p>The length of the resulting array is {@link #size()}.
     *
     * @return an array containing all of the elements in this list in proper
     *         sequence
     * @see Arrays#asList(Object[])
     */
    public int[] toIntArray() {
        return Arrays.copyOf(array, size);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public IntList clone() {
        final IntList clone = new IntList();
        clone.array = Arrays.copyOf(array, size);
        clone.size = size;
        clone.modCount = 0;
        return clone;
    }

}
