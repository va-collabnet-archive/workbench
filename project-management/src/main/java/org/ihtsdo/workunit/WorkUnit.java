package org.ihtsdo.workunit;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.ihtsdo.tk.workflow.api.WfActivityBI;
import org.ihtsdo.tk.workflow.api.WfActivityInstanceBI;
import org.ihtsdo.tk.workflow.api.WfCommentBI;
import org.ihtsdo.tk.workflow.api.WfStateBI;
import org.ihtsdo.tk.workflow.api.WfUserBI;

public class WorkUnit {
	
	private UUID id;
	private UUID workListId;
	private String worklistName;
	private WfStateBI state;
	private Map<WfActivityBI,WfStateBI> possibleActivities;
	private WfUserBI assignedUser;
	private List<WfActivityInstanceBI> workflowHistory;
	private List<WfCommentBI> comments;
	private List<WuFocusConcept> focusConcepts;
	private String instructions;
	private Map<String,URL> links;
	private WuProfile profile;
	
	

	public WorkUnit() {
	}

	/**
	 * @return the id
	 */
	public UUID getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(UUID id) {
		this.id = id;
	}

	/**
	 * @return the workListId
	 */
	public UUID getWorkListId() {
		return workListId;
	}

	/**
	 * @param workListId the workListId to set
	 */
	public void setWorkListId(UUID workListId) {
		this.workListId = workListId;
	}

	/**
	 * @return the worklistName
	 */
	public String getWorklistName() {
		return worklistName;
	}

	/**
	 * @param worklistName the worklistName to set
	 */
	public void setWorklistName(String worklistName) {
		this.worklistName = worklistName;
	}

	/**
	 * @return the state
	 */
	public WfStateBI getState() {
		return state;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(WfStateBI state) {
		this.state = state;
	}

	/**
	 * @return the possibleActivities
	 */
	public Map<WfActivityBI, WfStateBI> getPossibleActivities() {
		return possibleActivities;
	}

	/**
	 * @param possibleActivities the possibleActivities to set
	 */
	public void setPossibleActivities(Map<WfActivityBI, WfStateBI> possibleActivities) {
		this.possibleActivities = possibleActivities;
	}

	/**
	 * @return the assignedUser
	 */
	public WfUserBI getAssignedUser() {
		return assignedUser;
	}

	/**
	 * @param assignedUser the assignedUser to set
	 */
	public void setAssignedUser(WfUserBI assignedUser) {
		this.assignedUser = assignedUser;
	}

	/**
	 * @return the workflowHistory
	 */
	public List<WfActivityInstanceBI> getWorkflowHistory() {
		return workflowHistory;
	}

	/**
	 * @param workflowHistory the workflowHistory to set
	 */
	public void setWorkflowHistory(List<WfActivityInstanceBI> workflowHistory) {
		this.workflowHistory = workflowHistory;
	}

	/**
	 * @return the comments
	 */
	public List<WfCommentBI> getComments() {
		return comments;
	}

	/**
	 * @param comments the comments to set
	 */
	public void setComments(List<WfCommentBI> comments) {
		this.comments = comments;
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
	public Map<String, URL> getLinks() {
		return links;
	}

	/**
	 * @param links the links to set
	 */
	public void setLinks(Map<String, URL> links) {
		this.links = links;
	}

	/**
	 * @return the wuProfile
	 */
	public WuProfile getProfile() {
		return profile;
	}

	/**
	 * @param wuProfile the wuProfile to set
	 */
	public void setProfile(WuProfile profile) {
		this.profile = profile;
	}

	/**
	 * @return the focusConcepts
	 */
	public List<WuFocusConcept> getFocusConcepts() {
		return focusConcepts;
	}

	/**
	 * @param focusConcepts the focusConcepts to set
	 */
	public void setFocusConcepts(List<WuFocusConcept> focusConcepts) {
		this.focusConcepts = focusConcepts;
	}

}
