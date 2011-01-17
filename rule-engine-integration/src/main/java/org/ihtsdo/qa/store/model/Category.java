package org.ihtsdo.qa.store.model;

import java.util.UUID;

public class Category {
	private UUID categoryUuid;
	private String name;
	private String description;
	
	public Category(){
		super();
	}
	
	public Category(UUID severityUuid, String name, String description) {
		super();
		this.categoryUuid = severityUuid;
		this.name = name;
		this.description = description;
	}

	public UUID getCategoryUuid() {
		return categoryUuid;
	}

	public void setCategoryUuid(UUID categoryUuid) {
		this.categoryUuid = categoryUuid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public String toString() {
		return name;
	}
	
}
