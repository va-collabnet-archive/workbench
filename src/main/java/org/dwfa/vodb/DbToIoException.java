package org.dwfa.vodb;

import java.io.IOException;

import com.sleepycat.je.DatabaseException;

public class DbToIoException extends IOException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DbToIoException(DatabaseException ex) {
		super(ex.getMessage());
		initCause(ex);
	}

}
