/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.RandomAccess;

import javax.swing.Icon;

import nonnull.NonNull;
import de.uka.iti.pseudo.term.Term;

/**
 * This is a collection of static methods  
 */
public class Util {

    /**
     * An icon which can be used instead an image if the image cannot be loaded.
     * <p>When drawn it shows the string "??".
     */
    public static final Icon UNKNOWN_ICON = new Icon() {

        public int getIconHeight() {
            return 16;
        }

        public int getIconWidth() {
            return 16;
        }

        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(Color.red);
            g.drawString("??", x, y + 16);
        }

    };

	
	/**
     * join a list of objects into a string, separated by ", "
     * 
     * @param list
     *            some list
     * 
     * @return the concatenated string, separated by commas
     */
	public static String commatize(@NonNull List<?> list) {
	    return join(list, ", ");
	}
	
	/**
     * Join a list of objects separated by some string in between them.
     * 
     * @param list
     *            some list of objects
     * @param sep
     *            the separating string
     * 
     * @return the concatenation of the objects as strings.
     */
	public static String join(List<?> list, String sep) {
	    StringBuilder sb = new StringBuilder();
	    Iterator<?> it = list.iterator();
	    while(it.hasNext()) {
	        sb.append(it.next());
	        if(it.hasNext())
	            sb.append(sep);
	    }
	    return sb.toString();
	}

//    /**
//     * Join a list of objects separated by some string
//     * 
//     * @param strings the strings
//     * @param sep the sep
//     * 
//     * @return the string
//     */
//    public String join(String[] strings, String sep) {
//		StringBuffer sb = new StringBuffer();
//		for (int i = 0; i < strings.length; i++) {
//			sb.append(strings[i]);
//			if(i != strings.length-1)
//				sb.append(sep);
//		}
//		return sb.toString();
//	}

//    public static <E> boolean replaceInList(List<E> list,
//            E org, E replacement) {
//        int index = list.indexOf(org);
//        if(index != -1) {
//            list.set(index, replacement);
//            return true;
//        }
//        return false;
//    }
	

	/**
     * Wrap an immutable list object around an array. The elements in the array
     * can by no means be altered. 
     * 
     * <p>The result is closely related to {@link Arrays#asList(Object...)} but
     * is unmodifiable.
     * 
     * @param array
     *            some array
     * 
     * @return an immutable list wrapping the argument array.
     * 
     * @see Arrays#asList(Object...)
     */
	public static <E> List<E> readOnlyArrayList(@NonNull E[] array) {
	    return new ReadOnlyArrayList<E>(array);
	}
	
	private static class ReadOnlyArrayList<E> extends AbstractList<E> implements RandomAccess {
		E[] array;

		public ReadOnlyArrayList(E[] array) {
		    if(array == null)
		        throw new NullPointerException();
			this.array = array;
		}

		@Override
		public E get(int index) {
			return array[index];
		}

		@Override
		public int size() {
			return array.length;
		}
		
		@Override
		public E[] toArray() {
			return array.clone();
		}
		
		@Override 
		public int indexOf(Object o) {
            if (o == null) {
                for (int i = 0; i < array.length; i++)
                    if (array[i] == null)
                        return i;
            } else {
                for (int i = 0; i < array.length; i++)
                    if (o.equals(array[i]))
                        return i;
            }
            return -1;
        }

	    @Override 
	    public boolean contains(Object o) {
            return indexOf(o) != -1;
        }
		
	}

	/**
	 * List terms of a list of terms on several lines
	 * 
	 * @param terms the list of terms
	 * 
	 * @return the string consisting of a line per term
	 */
	public static String listTerms(List<Term> terms) {
		StringBuilder sb = new StringBuilder();
		int size = terms.size();
		for (int i = 0; i < size; i++) {
			sb.append(i + ": " + terms.get(i));
			if(i != size - 1)
				sb.append("\n");
		}
		return sb.toString();
	}

    /**
     * List the types of terms of a list of terms on several lines
     * 
     * @param terms
     *            the list of terms
     * 
     * @return the string consisting of a line per term
     */
	public static String listTypes(List<Term> subterms) {
		StringBuilder sb = new StringBuilder();
		int size = subterms.size();
		for (int i = 0; i < size; i++) {
			sb.append(i + ": " + subterms.get(i).getType());
			if(i != size - 1)
				sb.append("\n");
		}
		return sb.toString();
	}
	
    /**
     * Create an array containing the elements of a collection. The method name
     * is misleading.
     * 
     * <p>
     * This method is type safe. The type of the contents of the array must be
     * compatible with the types of the elements in the array.
     * 
     * @param collection
     *            the collection to be saved in an array.
     * 
     * @param clss
     *            the class of the array to create.
     * 
     * @return an array whose content type is the specified class, whose length
     *         is the size of the collection and whose contents is the one of
     *         the collection as if retrieved by
     *         {@link Collection#toArray(Object[])}.
     */
    @SuppressWarnings("unchecked") 
    public static <E> E[] listToArray(@NonNull Collection<? extends E> collection, @NonNull Class<E> clss) {
        E[] array = (E[]) java.lang.reflect.Array.newInstance(clss, collection.size());
        return collection.toArray(array);
    }
    
    /**
     * compares two references that may be null.
     * 
     * If both are null, true is returned. If only one is null, false is
     * returned. In any other case, o1.equals(o2) is called and its value
     * returned
     * 
     * @param o1
     *            object to compare
     * @param o2
     *            object to compare
     * 
     * @return true if both are null references or o1.equals(o2) holds on
     *         non-null references
     */
    public static boolean equalOrNull(Object o1, Object o2) {
        if(o1 == null)
            return o2 == null;
        else if(o2 == null)
            return false;
        else
            return o1.equals(o2);
            
    }
    
    /**
     * Strip quoting or similar characters from a string.
     * 
     * @param s
     *            some string with length >= 2
     * 
     * @return the string with first and last character removed
     */
    public static String stripQuotes(String s) {
        return s.substring(1, s.length() - 1);
    }
}
