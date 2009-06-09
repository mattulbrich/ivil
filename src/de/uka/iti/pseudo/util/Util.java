package de.uka.iti.pseudo.util;


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


}
