/*
 * Created on Feb 13, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa.util;

import java.io.IOException;

/**
 * @author kec
 *
 */
public interface TaskWithProgress {
	/**
	 * Start the task.
	 */
	public void go();

	/**
	 * Called to find out how much work needs
	 * to be done.
	 */
	public int getLengthOfTask();

	/**
	 * Called  to find out how much has been done.
	 * @throws IOException
	 */
	public int getCurrent() throws IOException;

	public void stop() throws Exception;

	/**
	 * Called to find out if the task has completed.
	 */
	public boolean isDone();
    
    public String getTaskName();

	/**
	 * Returns the most recent status message, or null
	 * if there is no current status message.
	 * @throws IOException
	 */
	public String getMessage() throws IOException;
    
    /**
     * A guarded wait mechanism. See section 3.2.3 in Concurrent Programming in Java, 2nd edition. 
     * @throws InterruptedException
     *
     */
    
    public void waitTillDone() throws InterruptedException;
}