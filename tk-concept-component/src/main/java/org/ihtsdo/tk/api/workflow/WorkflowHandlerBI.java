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
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.spec.ConceptSpec;

// TODO: Auto-generated Javadoc
/**
 * The Interface WorkflowHandlerBI.
 */
public interface WorkflowHandlerBI {
	
	/**
	 * Gets the available workflow actions.
	 *
	 * @param concept the concept
	 * @param ViewCoordinate the view coordinate
	 * @return the available workflow actions
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ContradictionException the contradiction exception
	 */
	public Collection<? extends WorkflowHistoryJavaBeanBI> getAvailableWorkflowActions(ConceptVersionBI concept, ViewCoordinate ViewCoordinate) throws IOException, ContradictionException;

	/**
	 * Gets the all available workflow action uids.
	 *
	 * @return the all available workflow action uids
	 */
	public List<UUID> getAllAvailableWorkflowActionUids(); 

	/**
	 * Checks for action.
	 *
	 * @param beans the beans
	 * @param actionSpec the action spec
	 * @return true, if successful
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ContradictionException the contradiction exception
	 */
	public boolean hasAction(Collection<? extends WorkflowHistoryJavaBeanBI> beans, ConceptSpec actionSpec) throws IOException, ContradictionException;

	/**
	 * Checks if is active action.
	 *
	 * @param possibleActions the possible actions
	 * @param actionUuid the action uuid
	 * @return true, if is active action
	 */
	public boolean isActiveAction(Collection<? extends WorkflowHistoryJavaBeanBI> possibleActions, UUID actionUuid);
}
