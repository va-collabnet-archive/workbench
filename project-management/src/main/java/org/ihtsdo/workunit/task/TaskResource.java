package org.ihtsdo.workunit.task;

import org.ihtsdo.workunit.sif.SifNamedElement;


public class TaskResource extends SifNamedElement {
	
	private ResourceType resourceType;

	public TaskResource() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the resourceType
	 */
	public ResourceType getResourceType() {
		return resourceType;
	}

	/**
	 * @param resourceType the resourceType to set
	 */
	public void setResourceType(ResourceType resourceType) {
		this.resourceType = resourceType;
	}

}
