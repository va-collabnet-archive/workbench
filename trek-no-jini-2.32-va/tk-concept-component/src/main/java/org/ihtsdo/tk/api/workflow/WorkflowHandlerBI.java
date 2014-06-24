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
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.spec.ConceptSpec;

/**
 * The Interface WorkflowHandlerBI contains methods general to handling
 * workflow.
 */
public interface WorkflowHandlerBI {

    /**
     * Gets the available workflow actions for the given
     * <code>concept</code> based on the
     * <code>viewCoordinate</code>.
     *
     * @param concept the concept associated with the workflow
     * @param viewCoordinate the view coordinate specifying which versions are
     * active and inactive
     * @return the workflow actions available of the specified concept
     * @throws IOException signals that an I/O exception has occurred
     * @throws ContradictionException if more than one version is found for a given position or view coordinate
     */
    public Collection<? extends WorkflowHistoryJavaBeanBI> getAvailableWorkflowActions(ConceptVersionBI concept, ViewCoordinate viewCoordinate) throws IOException, ContradictionException;

    /**
     * Gets the uuids associated with the available workflow actions.
     *
     * @return a list of uuids for the available actions
     */
    public List<UUID> getAllAvailableWorkflowActionUids();

    /**
     * Checks if the workflow items,
     * <code>workflows</code>, have an action specified by the
     * <code>actionSpec</code>.
     *
     * @param workflows the workflow history java beans representing the workflow items
     * @param actionSpec the <code>ConceptSpec</code> representing the action in question
     * @return <code>true</code>, if any of the workflow workflows have the action
     * @throws IOException signals that an I/O exception has occurred
     */
    public boolean hasAction(Collection<? extends WorkflowHistoryJavaBeanBI> workflows, ConceptSpec actionSpec) throws IOException;

    /**
     * Checks if the workflow items,
     * <code>beans</code>, have an action specified by the
     * <code>actionUuid</code>.
     * 
     * @param workflows the workflow history java beans representing the workflow items
     * @param actionUuid the uuid representing the action in question
     * @return <code>true</code>, if any of the workflow beans have the action
     */
    public boolean hasAction(Collection<? extends WorkflowHistoryJavaBeanBI> workflows, UUID actionUuid);
}
