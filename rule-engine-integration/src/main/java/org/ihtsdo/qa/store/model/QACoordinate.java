package org.ihtsdo.qa.store.model;

import java.util.UUID;

public class QACoordinate extends ViewPointSpecificObject {

	public QACoordinate(UUID databaseUuid, UUID pathUuid, String viewPointTime) {
		super();
		this.setDatabaseUuid(databaseUuid);
		this.setPathUuid(pathUuid);
		this.setViewPointTime(viewPointTime);
	}

}
