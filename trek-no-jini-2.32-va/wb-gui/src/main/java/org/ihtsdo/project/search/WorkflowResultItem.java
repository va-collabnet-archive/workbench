package org.ihtsdo.project.search;

import java.util.UUID;

public class WorkflowResultItem {

	UUID conceptUuid;
	String conceptFSN;
	public WorkflowResultItem(UUID conceptUuid, String conceptFSN) {
		super();
		this.conceptUuid = conceptUuid;
		this.conceptFSN = conceptFSN;
	}
	public String toString(){
		return conceptFSN;
	}
	public UUID getConceptUuid() {
		return conceptUuid;
	}
}
