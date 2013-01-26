/*
 * Copyright (c) 2010 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.project.workflow.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.refset.LanguageMembershipRefset;
import org.ihtsdo.project.refset.PromotionAndAssignmentRefset;
import org.ihtsdo.project.workflow.api.wf2.implementation.WfProcessDefinition;
import org.ihtsdo.project.workflow.api.wf2.implementation.WorkflowStore;
import org.ihtsdo.tk.workflow.api.WfActivityBI;
import org.ihtsdo.tk.workflow.api.WfActivityInstanceBI;
import org.ihtsdo.tk.workflow.api.WfProcessDefinitionBI;
import org.ihtsdo.tk.workflow.api.WfProcessInstanceBI;
import org.ihtsdo.tk.workflow.api.WfStateBI;
import org.ihtsdo.tk.workflow.api.WfUserBI;
import org.ihtsdo.tk.workflow.api.WorkflowStoreBI;

/**
 * The Class WfInstance.
 */
public class WfInstance implements Serializable, WfProcessInstanceBI {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The component id. */
	private UUID componentId = null;

	/** The work list. */
	private WorkList workList;

	/** The destination. */
	private WfUser destination;

	/** The wf definition. */
	private WorkflowDefinition wfDefinition;

	/** The status. */
	private WfState status;

	/** The properties. */
	private Map<String,Object> properties;

	/** The history. */
	private List<WfHistoryEntry> history;

	/** The component name. */
	private String componentName;

	/** The action report. */
	private ActionReport actionReport;

	/**
	 * The Enum ActionReport.
	 */
	public enum ActionReport {
		/** The CANCEL. */
		CANCEL, 
		/** The SAV e_ a s_ todo. */
		SAVE_AS_TODO, 
		/** The OUTBOX. */
		OUTBOX,
		/** The COMPLETE. */
		COMPLETE};

		/**
		 * Instantiates a new wf instance.
		 */
		public WfInstance() {
			super();
		}

		/**
		 * Instantiates a new wf instance.
		 *
		 * @param componentId the component id
		 * @param wfDefinition the wf definition
		 * @param status the status
		 * @param properties the properties
		 * @param history the history
		 */
		public WfInstance(UUID componentId, WorkflowDefinition wfDefinition, WfState state,
				Map<String, Object> properties, List<WfHistoryEntry> history) {
			super();
			this.componentId = componentId;
			this.wfDefinition = wfDefinition;
			this.status = state;
			this.properties = properties;
			this.history = history;
		}

		/**
		 * Gets the component id.
		 *
		 * @return the component id
		 */
		public UUID getComponentId() {
			return componentId;
		}

		/**
		 * Sets the component id.
		 *
		 * @param componentId the new component id
		 */
		public void setComponentId(UUID componentId) {
			this.componentId = componentId;
		}

		/**
		 * Gets the wf definition.
		 *
		 * @return the wf definition
		 */
		public WorkflowDefinition getWfDefinition() {
			return wfDefinition;
		}

		/**
		 * Sets the wf definition.
		 *
		 * @param wfDefinition the new wf definition
		 */
		public void setWfDefinition(WorkflowDefinition wfDefinition) {
			this.wfDefinition = wfDefinition;
		}

		/**
		 * Gets the status.
		 *
		 * @return the status
		 */
		public WfState getState() {
			return status;
		}

		/**
		 * Sets the status.
		 *
		 * @param status the new status
		 */
		public void setState(WfState state) {
			this.status = state;
		}

		/**
		 * Gets the properties.
		 *
		 * @return the properties
		 */
		public Map<String, Object> getProperties() {
			return properties;
		}

		/**
		 * Sets the properties.
		 *
		 * @param properties the properties
		 */
		public void setProperties(Map<String, Object> properties) {
			this.properties = properties;
		}

		/**
		 * Gets the work list.
		 *
		 * @return the work list
		 */
		public WorkList getWorkList() {
			return workList;
		}

		/**
		 * Sets the work list.
		 *
		 * @param workList the new work list
		 */
		public void setWorkList(WorkList workList) {
			this.workList = workList;
		}

		/**
		 * Gets the destination.
		 *
		 * @return the destination
		 */
		public WfUser getDestination() {
			return destination;
		}

		/**
		 * Sets the destination.
		 *
		 * @param destination the new destination
		 */
		public void setDestination(WfUser destination) {
			this.destination = destination;
		}

		/**
		 * Update instance status.
		 *
		 * @param instance the instance
		 * @param newState the new status
		 * @throws Exception the exception
		 */
		public static void updateInstanceState(WfInstance instance, WfState newState) throws Exception {
			I_TermFactory tf = Terms.get();
			I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
			WorkList workList = instance.getWorkList();
			PromotionAndAssignmentRefset pormAssigRefset = workList.getPromotionRefset(config);
			pormAssigRefset.setPromotionStatus(tf.uuidToNative(instance.componentId), 
					tf.uuidToNative(newState.getId()));
			instance.setState(newState);
			TranslationProject project = (TranslationProject) TerminologyProjectDAO.getProjectForWorklist(workList, config);
			I_GetConceptData targetLanguage = TerminologyProjectDAO.getTargetLanguageRefsetForProject(project, config);
			if (targetLanguage != null) {
				LanguageMembershipRefset targetLangRefset = new LanguageMembershipRefset(targetLanguage, config);
				targetLangRefset.getPromotionRefset(config).setPromotionStatus(tf.uuidToNative(instance.componentId), 
						tf.uuidToNative(newState.getId()));
			}
		}

		/**
		 * Update destination.
		 *
		 * @param instance the instance
		 * @param user the user
		 * @throws Exception the exception
		 */
		public static void updateDestination(WfInstance instance, WfUser user) throws Exception {
			I_TermFactory tf = Terms.get();
			I_ConfigAceFrame config = tf.getActiveAceFrameConfig();
			WorkList workList = instance.getWorkList();
			PromotionAndAssignmentRefset pormAssigRefset = workList.getPromotionRefset(config);
			pormAssigRefset.setDestination(tf.uuidToNative(instance.componentId), 
					tf.uuidToNative(user.getId()));
			instance.setDestination(user);
		}

		/**
		 * Gets the component name.
		 *
		 * @return the component name
		 */
		public String getComponentName() {
			return componentName;
		}

		/**
		 * Sets the component name.
		 *
		 * @param componentName the new component name
		 */
		public void setComponentName(String componentName) {
			this.componentName = componentName;
		}

		/**
		 * Gets the action report.
		 *
		 * @return the action report
		 */
		public ActionReport getActionReport() {
			return actionReport;
		}

		/**
		 * Sets the action report.
		 *
		 * @param actionReport the new action report
		 */
		public void setActionReport(ActionReport actionReport) {
			this.actionReport = actionReport;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if(obj instanceof WfInstance){
				WfInstance instance = (WfInstance)obj;
				try{
					return instance.getComponentId().equals(this.componentId) &&
							instance.getComponentName().equals(this.componentName) &&
							instance.getWorkList().equals(this.workList) &&
							instance.workList.getName().equals(this.workList.getName()) &&
							instance.getDestination().equals(this.destination) &&
							instance.getActionReport().equals(this.actionReport);
				}catch (NullPointerException npx){
					return false;
				}
			}else{
				return false;
			}
		}

		@Override
		public UUID getComponentPrimUuid() {
			return this.getComponentId();
		}

		@Override
		public WfProcessDefinitionBI getProcessDefinition() {
			return new WfProcessDefinition(getWfDefinition());
		}

		@Override
		public void setState(WfStateBI state) throws Exception {
			WfInstance.updateInstanceState(this, (WfState)state);
		}

		@Override
		public WfUserBI getAssignedUser() {
			return getDestination();
		}

		@Override
		public void setAssignedUser(WfUserBI user) {
			setDestination((WfUser)user);
		}

		@Override
		public Collection<WfActivityBI> getActivities(WfUserBI user) throws Exception {
			WorkflowStoreBI wf = new WorkflowStore();
			return wf.getActivities(this, user);
		}

		@Override
		public Collection<WfActivityBI> getActivitiesForOverrideMode() {
			return this.getProcessDefinition().getActivities();
		}

		@Override
		public Long getDueDate() {
			// TODO end of time
			return Long.MAX_VALUE;
		}

		@Override
		public Long getCreationDate() {
			// TODO origin of time
			return Long.MIN_VALUE;
		}

		@Override
		public Integer getPriority() {
			//TODO returns 3, not represented
			return 3;
		}

		@Override
		public boolean isActive() {
			return true;
		}

		@Override
		public boolean isCompleted() {
			return false;
		}

		/**
		 * @return the history
		 */
		public List<WfHistoryEntry> getHistory() {
			return history;
		}

		/**
		 * @param history the history to set
		 */
		public void setHistory(List<WfHistoryEntry> history) {
			this.history = history;
		}

		@Override
		public LinkedList<WfActivityInstanceBI> getActivityInstances() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean isPromoted() {
			// TODO Auto-generated method stub
			return false;
		}

}