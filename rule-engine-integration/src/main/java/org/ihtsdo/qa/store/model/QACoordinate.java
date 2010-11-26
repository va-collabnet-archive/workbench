package org.ihtsdo.qa.store.model;

import java.util.Date;
import java.util.UUID;

public class QACoordinate extends ViewPointSpecificObject {

	public QACoordinate(String database, UUID pathUuid, String viewPointTime) {
		super();
		this.setDatabase(database);
		this.setPathUuid(pathUuid);
		this.setViewPointTime(viewPointTime);
	}

}
