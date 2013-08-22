package org.ihtsdo.workunit.task;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.ihtsdo.workunit.sif.SifChangeSet;
import org.ihtsdo.workunit.sif.SifComment;
import org.ihtsdo.workunit.sif.SifConcept;
import org.ihtsdo.workunit.sif.SifContext;

public class WorkItem {
	
	private UUID workItemId;
	private WorkList workList;
	private Activity activity;
	private int priority;
	private String description;
	private TaskResource resource;
	private TaskStatus status;
	private long dueDate;
	private List<Action> possibleActions;
	private ExecutionStatus executionStatus;
	private SifConcept attachedConceptBaseVersion;
	private SifContext context;
	private SifChangeSet changeset;
	private LinkedList<WfHistoryItem> history;
	private LinkedList<SifComment> comments;
	

	public WorkItem() {
		// TODO Auto-generated constructor stub
	}


	/**
	 * @return the workItemId
	 */
	public UUID getWorkItemId() {
		return workItemId;
	}


	/**
	 * @param workItemId the workItemId to set
	 */
	public void setWorkItemId(UUID workItemId) {
		this.workItemId = workItemId;
	}


	/**
	 * @return the workList
	 */
	public WorkList getWorkList() {
		return workList;
	}


	/**
	 * @param workList the workList to set
	 */
	public void setWorkList(WorkList workList) {
		this.workList = workList;
	}


	/**
	 * @return the activity
	 */
	public Activity getActivity() {
		return activity;
	}


	/**
	 * @param activity the activity to set
	 */
	public void setActivity(Activity activity) {
		this.activity = activity;
	}


	/**
	 * @return the priority
	 */
	public int getPriority() {
		return priority;
	}


	/**
	 * @param priority the priority to set
	 */
	public void setPriority(int priority) {
		this.priority = priority;
	}


	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}


	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}


	/**
	 * @return the resource
	 */
	public TaskResource getResource() {
		return resource;
	}


	/**
	 * @param resource the resource to set
	 */
	public void setResource(TaskResource resource) {
		this.resource = resource;
	}


	/**
	 * @return the status
	 */
	public TaskStatus getStatus() {
		return status;
	}


	/**
	 * @param status the status to set
	 */
	public void setStatus(TaskStatus status) {
		this.status = status;
	}


	/**
	 * @return the dueDate
	 */
	public long getDueDate() {
		return dueDate;
	}


	/**
	 * @param dueDate the dueDate to set
	 */
	public void setDueDate(long dueDate) {
		this.dueDate = dueDate;
	}


	/**
	 * @return the possibleActions
	 */
	public List<Action> getPossibleActions() {
		return possibleActions;
	}


	/**
	 * @param possibleActions the possibleActions to set
	 */
	public void setPossibleActions(List<Action> possibleActions) {
		this.possibleActions = possibleActions;
	}


	/**
	 * @return the executionStatus
	 */
	public ExecutionStatus getExecutionStatus() {
		return executionStatus;
	}


	/**
	 * @param executionStatus the executionStatus to set
	 */
	public void setExecutionStatus(ExecutionStatus executionStatus) {
		this.executionStatus = executionStatus;
	}


	/**
	 * @return the attachedConceptBaseVersion
	 */
	public SifConcept getAttachedConceptBaseVersion() {
		return attachedConceptBaseVersion;
	}


	/**
	 * @param attachedConceptBaseVersion the attachedConceptBaseVersion to set
	 */
	public void setAttachedConceptBaseVersion(SifConcept attachedConceptBaseVersion) {
		this.attachedConceptBaseVersion = attachedConceptBaseVersion;
	}


	/**
	 * @return the context
	 */
	public SifContext getContext() {
		return context;
	}


	/**
	 * @param context the context to set
	 */
	public void setContext(SifContext context) {
		this.context = context;
	}


	/**
	 * @return the changeset
	 */
	public SifChangeSet getChangeset() {
		return changeset;
	}


	/**
	 * @param changeset the changeset to set
	 */
	public void setChangeset(SifChangeSet changeset) {
		this.changeset = changeset;
	}


	/**
	 * @return the history
	 */
	public LinkedList<WfHistoryItem> getHistory() {
		return history;
	}


	/**
	 * @param history the history to set
	 */
	public void setHistory(LinkedList<WfHistoryItem> history) {
		this.history = history;
	}


	/**
	 * @return the comments
	 */
	public LinkedList<SifComment> getComments() {
		return comments;
	}


	/**
	 * @param comments the comments to set
	 */
	public void setComments(LinkedList<SifComment> comments) {
		this.comments = comments;
	}

}
