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
package org.ihtsdo.project.view.dnd;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.Partition;
import org.ihtsdo.project.view.TranslationHelperPanel;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationThreadingPolicy;

/**
 * The Class GetPartitionMember.
 */
public class GetPartitionMember implements I_GetItemForModel {

	/** The partition. */
	private Partition partition;

	/** The config. */
	private I_ConfigAceFrame config;

	/**
	 * Instantiates a new gets the partition member.
	 * 
	 * @param partition
	 *            the partition
	 * @param config
	 *            the config
	 */
	public GetPartitionMember(Partition partition, I_ConfigAceFrame config) {
		this.partition = partition;
		this.config = config;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ihtsdo.project.panel.dnd.I_GetItemForModel#getItemFromConcept(org
	 * .dwfa.ace.api.I_GetConceptData)
	 */
	@Override
	public Object getItemFromConcept(I_GetConceptData concept) throws Exception {

		TerminologyProjectDAO.addConceptAsPartitionMember(concept, partition,
				config);
		Terms.get().addUncommitted(partition.getConcept());
		partition.getConcept().commit(
				config.getDbConfig().getUserChangesChangeSetPolicy().convert(),
				ChangeSetGenerationThreadingPolicy.SINGLE_THREAD);
		TranslationHelperPanel.setFocusToProjectPanel();
		return TerminologyProjectDAO.getPartitionMember(concept,
				partition.getId(), config);

	}

}
