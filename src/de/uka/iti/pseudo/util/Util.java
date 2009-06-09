/*
 * This file is part of PSEUDO
 * Copyright (C) 2009 Universitaet Karlsruhe, Germany
 *    written by Mattias Ulbrich
 * 
 * The system is protected by the GNU General Public License. 
 * See LICENSE.TXT for details.
 */
package de.uka.iti.pseudo.util;

import java.util.List;


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

    public static <E> boolean replaceInList(List<E> list,
            E org, E replacement) {
        int index = list.indexOf(org);
        if(index != -1) {
            list.set(index, replacement);
            return true;
        }
        return false;
    }


}
