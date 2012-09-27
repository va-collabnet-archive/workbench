/**
 * Copyright (c) 2012 International Health Terminology Standards Development
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
package org.ihtsdo.tk.api.workflow;

import java.io.IOException;
import java.util.UUID;

import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

// TODO: Auto-generated Javadoc
/**
 * The Interface WorkflowHistoryJavaBeanBI.
 */
public interface WorkflowHistoryJavaBeanBI {
	
	/**
	 * Sets the concept.
	 *
	 * @param conceptUuid the new concept
	 */
	public void setConcept(UUID conceptUuid);
	
	/**
	 * Sets the workflow id.
	 *
	 * @param workflowUuid the new workflow id
	 */
	public void setWorkflowId(UUID workflowUuid);
	
	/**
	 * Sets the path.
	 *
	 * @param pathUuid the new path
	 */
	public void setPath(UUID pathUuid);
	
	/**
	 * Sets the modeler.
	 *
	 * @param modelerUuid the new modeler
	 */
	public void setModeler(UUID modelerUuid);
	
	/**
	 * Sets the state.
	 *
	 * @param stateUuid the new state
	 */
	public void setState(UUID stateUuid);
	
	/**
	 * Sets the action.
	 *
	 * @param actionUuid the new action
	 */
	public void setAction(UUID actionUuid);
	
	/**
	 * Sets the fully specified name.
	 *
	 * @param text the new fully specified name
	 */
	public void setFullySpecifiedName(String text);
	
	/**
	 * Sets the effective time.
	 *
	 * @param effectiveTime the new effective time
	 */
	public void setEffectiveTime(Long effectiveTime);
	
	/**
	 * Sets the workflow time.
	 *
	 * @param workflowTime the new workflow time
	 */
	public void setWorkflowTime(Long workflowTime);
	
	/**
	 * Sets the refex member nid.
	 *
	 * @param refexMemberNid the new refex member nid
	 */
	public void setRefexMemberNid(int refexMemberNid);
	
	/**
	 * Sets the auto approved.
	 *
	 * @param autoApprove the new auto approved
	 */
	public void setAutoApproved(boolean autoApprove);
	
	/**
	 * Sets the overridden.
	 *
	 * @param override the new overridden
	 */
	public void setOverridden(boolean override);
	
	/**
	 * Gets the concept.
	 *
	 * @return the concept
	 */
	public UUID getConcept();
	
	/**
	 * Gets the workflow id.
	 *
	 * @return the workflow id
	 */
	public UUID getWorkflowId();
	
	/**
	 * Gets the path.
	 *
	 * @return the path
	 */
	public UUID getPath();
	
	/**
	 * Gets the modeler.
	 *
	 * @return the modeler
	 */
	public UUID getModeler();
	
	/**
	 * Gets the state.
	 *
	 * @return the state
	 */
	public UUID getState();
	
	/**
	 * Gets the action.
	 *
	 * @return the action
	 */
	public UUID getAction();
	
	/**
	 * Gets the fully specified name.
	 *
	 * @return the fully specified name
	 */
	public String getFullySpecifiedName();
	
	/**
	 * Gets the effective time.
	 *
	 * @return the effective time
	 */
	public Long getEffectiveTime();
	
	/**
	 * Gets the workflow time.
	 *
	 * @return the workflow time
	 */
	public Long getWorkflowTime();
	
	/**
	 * Gets the auto approved.
	 *
	 * @return the auto approved
	 */
	public boolean getAutoApproved();
	
	/**
	 * Gets the overridden.
	 *
	 * @return the overridden
	 */
	public boolean getOverridden();
	
	/**
	 * Gets the refex member nid.
	 *
	 * @return the refex member nid
	 */
	public int getRefexMemberNid();
	
	/**
	 * Gets the state for title bar.
	 *
	 * @param viewCoordinate the view coordinate
	 * @return the state for title bar
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public String getStateForTitleBar(ViewCoordinate viewCoordinate) throws IOException;
	
	/**
	 * Gets the modeler for title bar.
	 *
	 * @param viewCoordinate the view coordinate
	 * @return the modeler for title bar
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public String getModelerForTitleBar(ViewCoordinate viewCoordinate) throws IOException;
	
	/**
	 * To string.
	 *
	 * @return the string
	 */
	public String toString();
}
