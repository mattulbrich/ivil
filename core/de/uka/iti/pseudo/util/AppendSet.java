package de.uka.iti.pseudo.util;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;

public class AppendSet<E> extends AbstractSet<E> implements Cloneable {
    
    private static class Node<E> {
        public Node(E value, Node<E> next) {
            this.value = value;
            this.next = next;
        }
        private E value;
        private Node<E> next;
    }
    
    private static class Itr<E> implements Iterator<E> {
        
        private Node<E> current;

        public Itr(Node<E> head) {
            current = head;
        }

        public boolean hasNext() {
            return current != null;
        }

        @Override
        public E next() {
            E value = current.value;
            current = current.next;
            return value;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
    }
    
    private Node<E> head = null;
    private int size = 0;

    @Override
    public Iterator<E> iterator() {
        return new Itr<E>(head);
    }

    @Override
    public int size() {
        return size;
    }
    
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

}
