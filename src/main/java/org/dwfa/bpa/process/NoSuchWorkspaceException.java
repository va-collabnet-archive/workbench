/*
 * Created on Mar 21, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa.process;

/**
 * Thrown if a specified workspace cannot be found. 
 * @author kec
 *
 */
public class NoSuchWorkspaceException extends Exception {

	/**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
	 * 
	 */
	public NoSuchWorkspaceException() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public NoSuchWorkspaceException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public NoSuchWorkspaceException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public NoSuchWorkspaceException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
	}
}
