package org.ihtsdo.workunit.task;

import java.net.URL;
import java.util.List;
import java.util.UUID;

import org.ihtsdo.workunit.sif.SifSource;

public class WorkList {
	
	private UUID worklistId;
	private String name;
	private long createdOn;
	private SifSource createdBy;
	private RequestSystem requestSystem;
	private Object requestId;
	private String instructions;
	private List<URL> links;
	
	public WorkList() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * @return the worklistId
	 */
	public UUID getWorklistId() {
		return worklistId;
	}
	/**
	 * @param worklistId the worklistId to set
	 */
	public void setWorklistId(UUID worklistId) {
		this.worklistId = worklistId;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the createdOn
	 */
	public long getCreatedOn() {
		return createdOn;
	}
	/**
	 * @param createdOn the createdOn to set
	 */
	public void setCreatedOn(long createdOn) {
		this.createdOn = createdOn;
	}
	/**
	 * @return the createdBy
	 */
	public SifSource getCreatedBy() {
		return createdBy;
	}
	/**
	 * @param createdBy the createdBy to set
	 */
	public void setCreatedBy(SifSource createdBy) {
		this.createdBy = createdBy;
	}
	/**
	 * @return the requestSystem
	 */
	public RequestSystem getRequestSystem() {
		return requestSystem;
	}
	/**
	 * @param requestSystem the requestSystem to set
	 */
	public void setRequestSystem(RequestSystem requestSystem) {
		this.requestSystem = requestSystem;
	}
	/**
	 * @return the requestId
	 */
	public Object getRequestId() {
		return requestId;
	}
	/**
	 * @param requestId the requestId to set
	 */
	public void setRequestId(Object requestId) {
		this.requestId = requestId;
	}

	/**
	 * @return the instructions
	 */
	public String getInstructions() {
		return instructions;
	}

	/**
	 * @param instructions the instructions to set
	 */
	public void setInstructions(String instructions) {
		this.instructions = instructions;
	}

	/**
	 * @return the links
	 */
	public List<URL> getLinks() {
		return links;
	}

	/**
	 * @param links the links to set
	 */
	public void setLinks(List<URL> links) {
		this.links = links;
	}
}
