package org.dwfa.vodb;

import java.io.IOException;


public class ToIoException extends IOException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ToIoException(Exception ex) {
		super(ex.getMessage());
		initCause(ex);
	}

}
