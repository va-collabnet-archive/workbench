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
package org.ihtsdo.translation.ui.event;

import java.util.List;

import org.dwfa.ace.api.I_GetConceptData;
import org.ihtsdo.project.I_ContextualizeDescription;
import org.ihtsdo.project.view.event.GenericEvent;

/**
 * The Class EmptyInboxItemSelectedEvent.
 */
public class UpdateSimilarityEvent extends GenericEvent {

	private I_ContextualizeDescription sourceFsnConcept;
	private I_ContextualizeDescription sourcePreferredConcept;
	private I_GetConceptData concept;
	private List<Integer> sourceIds;
	private int targetId;

	/**
	 * Instantiates a new empty inbox item selected event.
	 */
	public UpdateSimilarityEvent(I_ContextualizeDescription sourceFsnConcept, I_ContextualizeDescription sourcePreferredConcept, I_GetConceptData concept, List<Integer> sourceIds, int targetId) {
		super();
		this.sourceFsnConcept = sourceFsnConcept;
		this.sourcePreferredConcept = sourcePreferredConcept;
		this.concept = concept;
		this.sourceIds = sourceIds;
		this.targetId = targetId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ihtsdo.project.workflow.event.GenericEvent#getAssociatedType()
	 */
	@Override
	public EventType getAssociatedType() {
		return EventType.UPDATE_SIMILARITY_EVENT;
	}

	public I_ContextualizeDescription getSourceFsnConcept() {
		return sourceFsnConcept;
	}

	public void setSourceFsnConcept(I_ContextualizeDescription sourceFsnConcept) {
		this.sourceFsnConcept = sourceFsnConcept;
	}

	public I_ContextualizeDescription getSourcePreferredConcept() {
		return sourcePreferredConcept;
	}

	public void setSourcePreferredConcept(I_ContextualizeDescription sourcePreferredConcept) {
		this.sourcePreferredConcept = sourcePreferredConcept;
	}

	public I_GetConceptData getConcept() {
		return concept;
	}

	public void setConcept(I_GetConceptData concept) {
		this.concept = concept;
	}

	public List<Integer> getSourceIds() {
		return sourceIds;
	}

	public void setSourceIds(List<Integer> sourceIds) {
		this.sourceIds = sourceIds;
	}

	public int getTargetId() {
		return targetId;
	}

	public void setTargetId(int targetId) {
		this.targetId = targetId;
	}

}
