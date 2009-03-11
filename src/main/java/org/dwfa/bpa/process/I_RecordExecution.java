/*
 * Created on Mar 18, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa.process;

import java.io.Serializable;
import java.util.Date;

/**
 * Documentation of the completion of a task. 
 * @author kec
 *  
 */
public interface I_RecordExecution extends Serializable, Comparable<I_RecordExecution> {

	/**
	 * @return The date this task completed. 
	 */
	public Date getDate();

	/**
	 * @return The identifier of the task associated with this execution record. 
	 */
	public int getTaskId();

	/**
	 * @return The identifier of the worker that completed the associated task. 
	 */
	public String getWorkerId();

	/**
	 * @return A description of the worker that completed this task. 
	 */
	public String getWorkerDesc();

	/**
	 * @return The completion condition of the task. 
	 */
	public Condition getCondition();

}