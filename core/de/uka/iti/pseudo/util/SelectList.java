/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.util;

import java.util.AbstractList;
import java.util.LinkedList;
import java.util.List;

/**
 * A SelectList allows to select those objects out of a list of arbitrary
 * parametrisation which are compatible with a certain type.
 * 
 * <p>Typically a list is created in the following manner:
 * <pre>
 *   List<?> someList;
 *   // ...
 *   List<String> selectionList = SelectionList.select(String.class, someList);
 * </pre>
 * 
 * The internal selection list created upon demand.
 */

public class SelectList<E> extends AbstractList<E> {
	
    /** the class of which elements are selected */
	private Class<E> clss;
	
	/** the wrapped original list */
	private List<?> list;
	
	/** the internal list which holds the selected elements */
	private LinkedList<E> internalList;
	
	/**
     * create a new Sublist with elements of a certain class.
     * 
     * <p>
     * The resulting class contains the elements of the class <code>list</code>
     * which are instances of the class <code>clss</code> and only those. The
     * created class is immutable.
     * 
     * @param clss
     *            the class of which instances are to be selected
     * @param list
     *            the list from which a sublist is to be extracted.
     */
	public SelectList(Class<E> clss, List<?> list) {
		assert clss != null;
		assert list != null;
		
		this.clss = clss;
		this.list = list;
	}
	
	/**
     * create a new Sublist with elements of a certain class. This is a
     * convenience wrapper for the constructor call, but significantly shorter
     * since type inference can be applied and the type parameter E can be
     * omitted in many cases.
     * 
     * @param <E>
     *            the type of the extracted elements
     * @param clss
     *            the class of which instances are to be selected
     * @param list
     *            the list from which a sublist is to be extracted.
     * @return a freshly created selectList object
     * @see #SelectList(Class, List)
     */
	public static <E> SelectList<E> select(Class<E> clss, List<?> list) {
		return new SelectList<E>(clss, list);
	}

	public E get(int index) {
		
		if(internalList == null) {
		    blowUp();
		}
		
		return internalList.get(index);
	}

	public int size() {
        if(internalList == null) {
            blowUp();
        }
        return internalList.size();
	}

	/**
	 * create the selection list as the sublist of list which holds 
	 * all elements which are of type E. 
	 */
    // @SuppressWarnings("unchecked") 
    private void blowUp() {
        internalList = new LinkedList<E>();
        for (Object obj : list) {
            if(clss.isInstance(obj))
                internalList.add(clss.cast(obj));
        }
    }
	
}
