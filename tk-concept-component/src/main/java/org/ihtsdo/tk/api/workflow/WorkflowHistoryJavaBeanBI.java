/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ihtsdo.tk.api.workflow;

import java.io.IOException;
import java.util.UUID;

import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

/**
 * The Interface WorkflowHistoryJavaBeanBI contains methods for interacting with
 * a particular workflow item.
 */
public interface WorkflowHistoryJavaBeanBI {

    /**
     * Sets the concept associated with this workflow.
     *
     * @param conceptUuid the uuid representing the concept
     */
    public void setConcept(UUID conceptUuid);

    /**
     * Sets the uuid identifying this workflow.
     *
     * @param workflowUuid the uuid associated with this workflow
     */
    public void setWorkflowId(UUID workflowUuid);

    /**
     * Sets the path that this workflow is on.
     *
     * @param pathUuid the uuid representing the path
     */
    public void setPath(UUID pathUuid);

    /**
     * Sets the modeler/editor associated with this workflow.
     *
     * @param modelerUuid the uuid representing the editor
     */
    public void setModeler(UUID modelerUuid);

    /**
     * Sets the workflow state associated with this workflow.
     *
     * @param stateUuid the uuid representing the workflow state
     */
    public void setState(UUID stateUuid);

    /**
     * Sets the workflow action associated with this workflow.
     *
     * @param actionUuid the new action
     */
    public void setAction(UUID actionUuid);

    /**
     * Sets the fully specified name of the concept associated with this
     * workflow.
     *
     * @param text the fully specified name string
     */
    public void setFullySpecifiedName(String text);

    /**
     * Sets the effective time of this workflow.
     *
     * @param effectiveTime the long representing the effective time
     */
    public void setEffectiveTime(Long effectiveTime);

    /**
     * Sets the time of this workflow.
     *
     * @param workflowTime the long representing the time
     */
    public void setWorkflowTime(Long workflowTime);

    /**
     * Sets the nid identifying the membership of this workflow in the "history
     * workflow refset".
     *
     * @param refexMemberNid the workflow history member nid
     */
    public void setRefexMemberNid(int refexMemberNid);

    /**
     * Sets the workflow as auto approved.
     *
     * @param autoApprove set to <code>true</code> to indicate this workflow is
     * auto approved
     */
    public void setAutoApproved(boolean autoApprove);

    /**
     * Sets the workflow as overridden.
     *
     * @param override set to <code>true</code> to indicate this workflow is
     * overridden
     */
    public void setOverridden(boolean override);

    /**
     * Gets the concept associated with this workflow.
     *
     * @return the uuid representing the concept associated with this workflow
     */
    public UUID getConcept();

    /**
     * Gets the uuid identifying this workflow.
     *
     * @return the workflow uuid
     */
    public UUID getWorkflowId();

    /**
     * Gets the path associated with this workflow
     *
     * @return the uuid representing the path
     */
    public UUID getPath();

    /**
     * Gets the modeler/editor associated with this workflow.
     *
     * @return the uuid representing the editor
     */
    public UUID getModeler();

    /**
     * Gets the state associated with this workflow.
     *
     * @return the uuid representing the state
     */
    public UUID getState();

    /**
     * Gets the action associated with this workflow.
     *
     * @return the uuid representing the action
     */
    public UUID getAction();

    /**
     * Gets the fully specified name of the concept associated with this
     * workflow.
     *
     * @return the string representing the fully specified name
     */
    public String getFullySpecifiedName();

    /**
     * Gets the effective time associated with this workflow.
     *
     * @return the long representing the effective time
     */
    public Long getEffectiveTime();

    /**
     * Gets the workflow time associated with this workflow.
     *
     * @return the long representing the workflow time
     */
    public Long getWorkflowTime();

    /**
     * Checks if this workflow is auto approved.
     *
     * @return <code>true</code> is the workflow is auto approved
     */
    public boolean getAutoApproved();

    /**
     * Checks if this workflow is overridden.
     *
     * @return <code>true</code> is the workflow is overridden
     */
    public boolean getOverridden();

    /**
     * Gets the nid identifying the membership of this workflow in the "history
     * workflow refset".
     *
     * @return the workflow history member nid
     */
    public int getRefexMemberNid();

    /**
     * Gets a string representation of the workflow state for display in an
     * Arena panel.
     *
     * @param viewCoordinate the view coordinate specifying the version of the
     * concept to represent
     * @return a string representation of the workflow state
     * @throws IOException signals that an I/O exception has occurred
     */
    public String getStateForTitleBar(ViewCoordinate viewCoordinate) throws IOException;

    /**
     * Gets a string representation of the editor for display in an Arena panel.
     *
     * @param viewCoordinate the view coordinate specifying the version of the
     * concept to represent
     * @return a string representation of the editor
     * @throws IOException signals that an I/O exception has occurred
     */
    public String getModelerForTitleBar(ViewCoordinate viewCoordinate) throws IOException;

    /**
     * Returns a string representation of this <code>WorkflowHistoryJavaBean</code> object.
     *
     * @return a string representation of this <code>WorkflowHistoryJavaBean</code> object
     */
    @Override
    public String toString();
}
