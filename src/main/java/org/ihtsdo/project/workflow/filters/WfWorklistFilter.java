package org.ihtsdo.project.workflow.filters;

import java.util.List;
import java.util.UUID;

import org.ihtsdo.project.workflow.model.WfInstance;

public class WfWorklistFilter implements WfSearchFilterBI {

	private List<UUID> worklistUUID;

	private WfWorklistFilter(List<UUID> worklistUUID) {
		super();
		this.worklistUUID = worklistUUID;
	}

	@Override
	public boolean filter(WfInstance instance) {
		return this.worklistUUID.equals(instance.getWorkListId());
	}

	public List<UUID> getWorklistUUID() {
		return worklistUUID;
	}

	public void setWorklistUUID(List<UUID> worklistUUID) {
		this.worklistUUID = worklistUUID;
	}
	
}
