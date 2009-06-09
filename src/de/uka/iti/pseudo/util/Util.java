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
import java.util.List;
import java.util.RandomAccess;

import de.uka.iti.pseudo.term.Term;


public class Util {
	
	public String join(String[] strings, String sep) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < strings.length; i++) {
			sb.append(strings[i]);
			if(i != strings.length-1)
				sb.append(sep);
		}
		return sb.toString();
	}

//    public static <E> boolean replaceInList(List<E> list,
//            E org, E replacement) {
//        int index = list.indexOf(org);
//        if(index != -1) {
//            list.set(index, replacement);
//            return true;
//        }
//        return false;
//    }
	

	public static <E> List<E> readOnlyArrayList(E[] array) {
		return new ReadOnlyArrayList<E>(array);
	}
	
	private static class ReadOnlyArrayList<E> extends AbstractList<E> implements RandomAccess {
		E[] array;

		public ReadOnlyArrayList(E[] array) {
			this.array = array;
		}

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
		
	}

	// TODO use pretty printer once there is one
	public static String listTerms(List<Term> subterms) {
		StringBuilder sb = new StringBuilder();
		int size = subterms.size();
		for (int i = 0; i < size; i++) {
			sb.append(i + ": " + subterms.get(i));
			if(i != size - 1)
				sb.append("\n");
		}
		return sb.toString();
	}

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
}
