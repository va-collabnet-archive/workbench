/*
 * Created on Mar 18, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa.process;

/**
 * Thrown if a task fails for any reason. This Exception should be used to 
 * encapsulate the original exception by all tasks. 
 * @author kec
 *
 */
public class TaskFailedException extends WorkflowException {

	/**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
	 * 
	 */
	public TaskFailedException() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message A message describing this exception. 
	 */
	public TaskFailedException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
     * @param message A message describing this exception. 
     * @param cause The cause of this exception. 
	 */
	public TaskFailedException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause The cause of this exception. 
	 */
	public TaskFailedException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

}
