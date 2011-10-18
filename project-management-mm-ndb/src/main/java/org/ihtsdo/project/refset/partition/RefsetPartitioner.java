package org.ihtsdo.project.refset.partition;

import java.util.ArrayList;
import java.util.List;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.Partition;
import org.ihtsdo.project.model.PartitionScheme;

public abstract class RefsetPartitioner {
	
	public RefsetPartitioner() {
		super();
	}
	
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
	
	public Partition createPartition(PartitionScheme scheme, String name, I_ConfigAceFrame config) throws Exception {
		Partition newPartition = null;
		List<I_GetConceptData> membersToIncludeInNewPartition = getMembersToInclude(scheme, name, config);
		
		if (membersToIncludeInNewPartition.size() > 0) {
			newPartition = TerminologyProjectDAO.createNewPartition(name, 
					scheme.getUids().iterator().next(), config);
			if(newPartition != null){
				for (I_GetConceptData loopMember : membersToIncludeInNewPartition) {
					TerminologyProjectDAO.addConceptAsPartitionMember(loopMember, 
							newPartition.getUids().iterator().next(), config);
				}
			}
		}
		
		return newPartition;
	}
	
	protected abstract boolean evaluateMember(I_GetConceptData member, I_ConfigAceFrame config);

}
