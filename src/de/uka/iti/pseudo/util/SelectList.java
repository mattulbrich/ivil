package de.uka.iti.pseudo.util;

import java.util.AbstractList;
import java.util.List;

public class SelectList<E> extends AbstractList<E> {
	
	private Class<E> clss;
	private List<?> list;
	
	public SelectList(Class<E> clss, List<?> list) {
		assert clss != null;
		assert list != null;
		
		this.clss = clss;
		this.list = list;
	}
	
	// shorter than constructor
	public static <E> SelectList<E> select(Class<E> clss, List<?> list) {
		return new SelectList<E>(clss, list);
	}

	public E get(int index) {
		
		if(index < 0)
			throw new IllegalArgumentException(Integer.toString(index));
		
		int i = 0;
		for (Object	obj : list) {
			if(clss.isInstance(obj)) {
				i++;
				if(i > index) {
					return (E)obj;
				}
			}
		}
		
		throw new IndexOutOfBoundsException(Integer.toString(index));
	}

	public int size() {
		int size = 0;
		for (Object	obj : list) {
			if(clss.isInstance(obj)) {
				size++;
			}
		}
		return size;
	}
	
}
