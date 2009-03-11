package org.dwfa.tapi;

import java.io.IOException;
import java.sql.SQLException;

public class SqlToIOException extends IOException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SqlToIOException(SQLException ex) {
		super(ex.getMessage());
		initCause(ex);
	}

}
