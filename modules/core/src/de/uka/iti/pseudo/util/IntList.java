package de.uka.iti.pseudo.util;

import java.util.AbstractList;
import java.util.Arrays;

public class IntList extends AbstractList<Integer> {

    private static int EMPTY_ARRAY[] = new int[0];
    private int[] array = EMPTY_ARRAY;
    private int size;
    
    private void ensureCapacity(int minSize) {
        modCount++;
        
        int oldCapacity = array.length;
        if (minSize > oldCapacity) {
            int newCapacity = (oldCapacity * 3)/2 + 1;
            if (newCapacity < minSize)
                newCapacity = minSize;
                    
            // minCapacity is usually close to size, so this is a win:
            array = Arrays.copyOf(array, newCapacity);
        }
    }
    
    public boolean add(int value) {
        ensureCapacity(size + 1);
        array[size] = value;
        size ++;
        return true;
    }
    
    @Override
    public boolean add(Integer e) {
        if(e == null)
            throw new NullPointerException("IntList does not support null-values");
        return add(e.intValue());
    }

    @Override
    public void add(int index, Integer element) {
        if(element == null)
            throw new NullPointerException("IntList does not support null-values");
        
        add(index, element.intValue());
    }
    
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
    
    public void addAll(IntList list) {
        ensureCapacity(size + list.size);
        System.arraycopy(list.array, 0, array, size, list.size);
        size = size + list.size;
    }

    @Override
    public void clear() {
        array = null;
        size = 0;
    }

    @Override
    public boolean contains(Object o) {
        if (o instanceof Integer) {
            int intVal = ((Integer) o).intValue();
            for (int i = 0; i < array.length; i++) {
                if(array[i] == intVal)
                    return true;
            }
        }
        return false;
    }

    public boolean containsAll(IntList c) {
        for (int i = 0; i < c.size(); i++) {
            if(!contains(c.at(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Integer get(int index) {
        return Integer.valueOf(array[index]);
    }
    
    public int at(int index) {
        return array[index];
    }
    
    public int indexOf(int value) {
        for (int i = 0; i < array.length; i++) {
            if(array[i] == value)
                return i;
        }
        return -1;
    }
    
    public int lastIndexOf(int value) {
        for (int i = array.length-1; i>=0; i--) {
            if(array[i] == value)
                return i;
        }
        return -1;
    }

    @Override
    public boolean remove(Object element) {
        if (element instanceof Integer) {
            Integer intObj = (Integer) element;
            int intVal = intObj.intValue();
            return removeValue(intVal);
        }
        return false;
    }

    private boolean removeValue(int intVal) {
        for (int index = 0; index < size; index++) {
            if (intVal == array[index]) {
                removeAt(index);
                return true;
            }
        }
        return false;
    }

    @Override
    public Integer remove(int index) {
        modCount++;
        
        int oldValue = array[index];

        int numMoved = size - index - 1;
        if (numMoved > 0)
            System.arraycopy(array, index+1, array, index,
                             numMoved);
        return Integer.valueOf(oldValue);
    }
    
    public int removeAt(int index) {
        modCount++;
        
        int oldValue = array[index];

        int numMoved = size - index - 1;
        if (numMoved > 0)
            System.arraycopy(array, index+1, array, index,
                             numMoved);
        return oldValue;
    }

    public Integer set(int index, Integer element) {
        if(element == null)
            throw new NullPointerException("IntList does not support null-values");
        
        return set(index, element.intValue());
    }
    
    public int set(int index, int value) {
        ensureCapacity(index + 1);
        return array[index] = value;
    }

    @Override
    public int size() {
        return size;
    }

    public Integer[] toArray() {
        Integer[] result = new Integer[size];
        for (int i = 0; i < array.length; i++) {
            result[i] = Integer.valueOf(array[i]);
        }
        return result;
    }
    
    public int[] toIntArray() { 
        return Arrays.copyOf(array, size);
    }
    
    public IntList clone() {
        final IntList clone = new IntList();
        clone.array = Arrays.copyOf(array, size);
        clone.size = size;
        clone.modCount = 0;
        return clone;
    }

}
