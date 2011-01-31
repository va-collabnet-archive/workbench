package org.ihtsdo.qadb.data;


public abstract class ViewPointSpecificObject {
	
	private String databaseUuid;
	private String pathUuid;
	private String viewPointTime;
	
	public ViewPointSpecificObject() {
		super();
	}
	
	public String getPathUuid() {
		return pathUuid;
	}

	public void setPathUuid(String pathUuid) {
		this.pathUuid = pathUuid;
	}

	public String getViewPointTime() {
		return viewPointTime;
	}

	public void setViewPointTime(String viewPointTime) {
		this.viewPointTime = viewPointTime;
	}

	public String getDatabaseUuid() {
		return databaseUuid;
	}

	public void setDatabaseUuid(String databaseUuid) {
		this.databaseUuid = databaseUuid;
	}

}
