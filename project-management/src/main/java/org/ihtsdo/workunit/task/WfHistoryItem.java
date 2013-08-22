package org.ihtsdo.workunit.task;

import org.ihtsdo.workunit.sif.SifAuthor;

public class WfHistoryItem {
	
	private long time;
	private SifAuthor author;
	private TaskStatus status; 

	public WfHistoryItem() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the time
	 */
	public long getTime() {
		return time;
	}

	/**
	 * @param time the time to set
	 */
	public void setTime(long time) {
		this.time = time;
	}

	/**
	 * @return the author
	 */
	public SifAuthor getAuthor() {
		return author;
	}

	/**
	 * @param author the author to set
	 */
	public void setAuthor(SifAuthor author) {
		this.author = author;
	}

	/**
	 * @return the status
	 */
	public TaskStatus getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(TaskStatus status) {
		this.status = status;
	}

}
