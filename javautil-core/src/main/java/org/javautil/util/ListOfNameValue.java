package org.javautil.util;

import java.util.ArrayList;

public class ListOfNameValue extends ArrayList<NameValue> {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public ListOfNameValue() {
		super();
	}
	
	public String toString() {
	    StringBuilder sb = new StringBuilder();
	    for (NameValue nv : this) {
	        sb.append(nv);
	        sb.append("\n"); 
	    }
	    return sb.toString();
	}

}
