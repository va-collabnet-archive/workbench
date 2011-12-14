package org.ihtsdo.project.workflow.model;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.refset.PromotionAndAssignmentRefset;

public class WfInstance {
	
	private UUID componentId = null;
	private UUID workListId;
	private WfUser destination;
	private WorkflowDefinition wfDefinition;
	private WfState state;
	private Map<String,Object> properties;
	private List<WfHistoryEntry> history;
	
	public WfInstance() {
		super();
	}

	public WfInstance(UUID componentId, WorkflowDefinition wfDefinition, WfState state,
			Map<String, Object> properties, List<WfHistoryEntry> history) {
		super();
		this.componentId = componentId;
		this.wfDefinition = wfDefinition;
		this.state = state;
		this.properties = properties;
		this.history = history;
	}

	public UUID getComponentId() {
		return componentId;
	}

	public void setComponentId(UUID componentId) {
		this.componentId = componentId;
	}

	public WorkflowDefinition getWfDefinition() {
		return wfDefinition;
	}

	public void setWfDefinition(WorkflowDefinition wfDefinition) {
		this.wfDefinition = wfDefinition;
	}

	public WfState getState() {
		return state;
	}

	public void setState(WfState state) {
		this.state = state;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

	public List<WfHistoryEntry> getHistory() {
		return history;
	}

	public void setHistory(List<WfHistoryEntry> history) {
		this.history = history;
	}

	public UUID getWorkListId() {
		return workListId;
	}

	public void setWorkListId(UUID workListId) {
		this.workListId = workListId;
	}

	public WfUser getDestination() {
		return destination;
	}

	public void setDestination(WfUser destination) {
		this.destination = destination;
	}
	
	public static void updateInstanceState(WfInstance instance, WfState newState) throws Exception {
		I_TermFactory tf = Terms.get();
		I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
		WorkList workList = TerminologyProjectDAO.getWorkList(tf.getConcept(instance.workListId), config);
		PromotionAndAssignmentRefset pormAssigRefset = workList.getPromotionRefset(config);
		pormAssigRefset.setPromotionStatus(tf.uuidToNative(instance.componentId), 
				tf.uuidToNative(newState.getId()));
		instance.setState(newState);
	}
	
	public static void updateInstanceUser(WfInstance instance, WfUser user) throws Exception {
		I_TermFactory tf = Terms.get();
		I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
		WorkList workList = TerminologyProjectDAO.getWorkList(tf.getConcept(instance.workListId), config);
		PromotionAndAssignmentRefset pormAssigRefset = workList.getPromotionRefset(config);
		pormAssigRefset.setDestination(tf.uuidToNative(instance.componentId), 
				tf.uuidToNative(user.getId()));
		instance.setDestination(user);
	}

}
