package org.ihtsdo.qa.store.model;

import java.util.UUID;

public class QADatabase {
	
	private UUID databaseUuid;
	private String name;
	
	public QADatabase(UUID databaseUuid, String name) {
		super();
		this.databaseUuid = databaseUuid;
		this.name = name;
	}

	public UUID getDatabaseUuid() {
		return databaseUuid;
	}

	public void setDatabaseUuid(UUID databaseUuid) {
		this.databaseUuid = databaseUuid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String toString() {
		return name;
	}

}
