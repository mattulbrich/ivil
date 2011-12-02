package de.uka.iti.pseudo.util;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;

public class ConcatenationList<E> extends AbstractList<E> {

    private final List<? extends E> firstList;
    private final List<? extends E> secondList;

    public ConcatenationList(List<? extends E> firstList, List<? extends E> secondList) {
        this.firstList = firstList;
        this.secondList = secondList;
    }
    
    @Override
    public E get(int index) {
        if(index > firstList.size()) {
            return secondList.get(index - firstList.size());
        } else {
            return firstList.get(index);
        }
    }

    @Override
    public int size() {
        return firstList.size() + secondList.size();
    }
    
    @Override
    public Iterator<E> iterator() {
        return new Itr();
    }
    
    private class Itr implements Iterator<E> {

        private boolean firstDone = false;
        private Iterator<? extends E> curItr = createIterator(firstList);
        
        @Override
        public boolean hasNext() {
            if(curItr.hasNext())
                return true;
            
            if(!firstDone) {
                firstDone = true;
                curItr = createIterator(secondList);
            }
            
            return curItr.hasNext();
        }

        @Override
        public E next() {
            return curItr.next();
        }

        @Override
        public void remove() {
            curItr.remove();
        }
        
        protected Iterator<? extends E> createIterator(List<? extends E> l) {
            return l.iterator();
        }
        
    }
    
}
