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
package org.ihtsdo.project.refset.partition;

import java.util.ArrayList;
import java.util.List;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.Partition;
import org.ihtsdo.project.model.PartitionScheme;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationThreadingPolicy;

/**
 * The Class RefsetPartitioner.
 */
public abstract class RefsetPartitioner {
	
	/**
	 * Instantiates a new refset partitioner.
	 */
	public RefsetPartitioner() {
		super();
	}
	
	/**
	 * Gets the members to include.
	 *
	 * @param scheme the scheme
	 * @param name the name
	 * @param config the config
	 * @return the members to include
	 * @throws Exception the exception
	 */
	public List<I_GetConceptData> getMembersToInclude(PartitionScheme scheme,  String name, I_ConfigAceFrame config) throws Exception {
		List<I_GetConceptData> membersNotPartitioned = TerminologyProjectDAO.getMembersNotPartitioned(scheme, config);
		List<I_GetConceptData> membersToIncludeInNewPartition = new ArrayList<I_GetConceptData>();
		
		for (I_GetConceptData loopMember : membersNotPartitioned) {
			if (evaluateMember(loopMember, config)) {
				membersToIncludeInNewPartition.add(loopMember);
			}
		}
		return membersToIncludeInNewPartition;
	}
	
	/**
	 * Creates the partition.
	 *
	 * @param scheme the scheme
	 * @param name the name
	 * @param config the config
	 * @return the partition
	 * @throws Exception the exception
	 */
	public Partition createPartition(PartitionScheme scheme, String name, I_ConfigAceFrame config) throws Exception {
		Partition newPartition = null;
		List<I_GetConceptData> membersToIncludeInNewPartition = getMembersToInclude(scheme, name, config);
		
		if (membersToIncludeInNewPartition.size() > 0) {
			newPartition = TerminologyProjectDAO.createNewPartition(name, 
					scheme.getUids().iterator().next(), config);
			if(newPartition != null){
				for (I_GetConceptData loopMember : membersToIncludeInNewPartition) {
					TerminologyProjectDAO.addConceptAsPartitionMember(loopMember, 
							newPartition, config);
				}
				Terms.get().addUncommittedNoChecks(newPartition.getConcept());
				newPartition.getConcept().commit(ChangeSetGenerationPolicy.INCREMENTAL, ChangeSetGenerationThreadingPolicy.SINGLE_THREAD);
			}
		}
		
		return newPartition;
	}
	
	/**
	 * Evaluate member.
	 *
	 * @param member the member
	 * @param config the config
	 * @return true, if successful
	 */
	protected abstract boolean evaluateMember(I_GetConceptData member, I_ConfigAceFrame config);

}
