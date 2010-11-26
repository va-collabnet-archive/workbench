package org.ihtsdo.qa.store.model;

import java.util.Date;
import java.util.UUID;

public abstract class ViewPointSpecificObject {
	
	private String database;
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

	public String getDatabase() {
		return database;
	}

	public void setDatabase(String database) {
		this.database = database;
	}
	
	

}
