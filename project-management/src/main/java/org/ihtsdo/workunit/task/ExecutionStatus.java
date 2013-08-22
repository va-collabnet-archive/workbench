package org.ihtsdo.workunit.task;

public enum ExecutionStatus {
	
	CREATED("created", 1),
	READY("ready", 2),
	RESERVED("reserved", 3),
	IN_PROGRESS("in progress", 4),
	SUSPENDED("suspended", 5),
	COMPLETED("completed", 6),
	FAILED("failed", 7),
	ERROR("error", 8),
	EXITED("exited", 9),
	OBSOLETE("obsolete", 10);
	
	private final String name;
	private final int id;
	
	private ExecutionStatus(String name, int id) {
		this.name = name;
		this.id = id;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}
