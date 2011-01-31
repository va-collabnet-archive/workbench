package org.ihtsdo.qadb.data;

import java.util.UUID;

public class QACoordinate extends ViewPointSpecificObject {

	public QACoordinate(String databaseUuid, String pathUuid, String viewPointTime) {
		super();
		this.setDatabaseUuid(databaseUuid);
		this.setPathUuid(pathUuid);
		this.setViewPointTime(viewPointTime);
	}

}
