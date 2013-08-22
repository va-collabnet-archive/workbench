package org.ihtsdo.workunit.task;

public enum ResourceType {
	
	USER("user", 1),
	ROLE("role", 2),
	GROUP("group", 3),
	SOFTWARE("software", 4);
	
	private final String name;
	private final int id;
	
	private ResourceType(String name, int id) {
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
