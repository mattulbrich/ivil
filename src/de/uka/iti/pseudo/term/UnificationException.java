package de.uka.iti.pseudo.term;

import java.util.LinkedList;
import java.util.List;

public class UnificationException extends TermException {
    
    private List<String> details = new LinkedList<String>();

	public UnificationException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public UnificationException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public UnificationException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public UnificationException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public UnificationException(String message, Type t1, Type t2) {
	    this(message);
	    addDetail("Type 1: " + t1);
	    addDetail("Type 2: " + t2);
	}

	public void addDetail(String detail) {
	    details.add(detail);
	}

	public String getDetailedMessage() {
	    StringBuilder sb = new StringBuilder();
	    sb.append(getMessage());
	    for (String detail : details) {
            sb.append("\n").append(detail);
        }
		return sb.toString();
	}

}
