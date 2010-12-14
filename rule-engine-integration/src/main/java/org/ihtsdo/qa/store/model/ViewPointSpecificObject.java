package org.ihtsdo.qa.store.model;

import java.util.UUID;

public abstract class ViewPointSpecificObject {
	
	private UUID databaseUuid;
	private UUID pathUuid;
	private String viewPointTime;
	
	public ViewPointSpecificObject() {
		super();
	}
	
	public UUID getPathUuid() {
		return pathUuid;
	}

	public void setPathUuid(UUID pathUuid) {
		this.pathUuid = pathUuid;
	}

	public String getViewPointTime() {
		return viewPointTime;
	}

	public void setViewPointTime(String viewPointTime) {
		this.viewPointTime = viewPointTime;
	}

	public UUID getDatabaseUuid() {
		return databaseUuid;
	}

	public void setDatabaseUuid(UUID databaseUuid) {
		this.databaseUuid = databaseUuid;
	}

}
