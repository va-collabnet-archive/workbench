/*
 * Created on Mar 18, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa.process;


/**
 * General purpose workflow exception. 
 * @author kec
 *
 */
public class WorkflowException extends Exception {

	/**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
	 * 
	 */
	public WorkflowException() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public WorkflowException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public WorkflowException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public WorkflowException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

}
