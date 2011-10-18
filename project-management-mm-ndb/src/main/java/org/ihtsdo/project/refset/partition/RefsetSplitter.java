package org.ihtsdo.project.refset.partition;

import java.util.ArrayList;
import java.util.List;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.Partition;
import org.ihtsdo.project.model.PartitionScheme;

public class RefsetSplitter {

	public RefsetSplitter() {
		super();
	}

	public List<Partition> splitRefset(PartitionScheme scheme, List<Integer> portions,
			String namePrefix, I_ConfigAceFrame config) throws Exception {
		List<Partition> partitions = new ArrayList<Partition>();
		Partition newPartition = null;
		int partitionNumber = 0;
		List<List<I_GetConceptData>> conceptsSets = calculatePartitions(scheme, portions, namePrefix, config);
		for (List<I_GetConceptData> conceptsSet : conceptsSets) {
			partitionNumber++;
			newPartition = TerminologyProjectDAO.createNewPartition(namePrefix + " " + partitionNumber, 
					scheme.getUids().iterator().next(), config);
			partitions.add(newPartition);
			if(newPartition != null){
				for (I_GetConceptData concept : conceptsSet) {
					TerminologyProjectDAO.addConceptAsPartitionMember(concept, 
							newPartition.getUids().iterator().next(), config);
				}
			}

		}
		//		int total = 0;
		//		for (Integer portion : portions) {
		//			total = total + portion;
		//		}
		//
		//		if (total != 100) {
		//			throw new Exception("Error, portions must sum 100%");
		//		}
		//
		//		List<I_GetConceptData> membersNotPartitioned = TerminologyProjectDAO.getMembersNotPartitioned(scheme, config);
		//
		//		int totalPartitioned = 0;
		//		int partitionNumber = 0;
		//		for (int portion : portions) {
		//			partitionNumber++;
		//			int totalInPortion = ((portion*membersNotPartitioned.size())/100);
		//			if (totalInPortion > 0) {
		//				newPartition = TerminologyProjectDAO.createNewPartition(namePrefix + " " + partitionNumber, 
		//						scheme.getUids().iterator().next(), config);
		//				partitions.add(newPartition);
		//				for (int i = 1;i<=totalInPortion;i++) {
		//					TerminologyProjectDAO.addConceptAsPartitionMember(membersNotPartitioned.get(totalPartitioned), 
		//							newPartition.getUids().iterator().next(), config);
		//					totalPartitioned++;
		//				}
		//			}
		//		}
		//
		//		for (int i = 1;i<=membersNotPartitioned.size()-totalPartitioned;i++) {
		//			TerminologyProjectDAO.addConceptAsPartitionMember(membersNotPartitioned.get(totalPartitioned), 
		//					newPartition.getUids().iterator().next(), config);
		//			totalPartitioned++;
		//		}
		return partitions;
	}

	public List<List<I_GetConceptData>> calculatePartitions(PartitionScheme scheme, List<Integer> portions,
			String namePrefix, I_ConfigAceFrame config) throws Exception {

		int total = 0;
		for (Integer portion : portions) {
			total = total + portion;
		}

		if (total != 100) {
			throw new Exception("Error, portions must sum 100%");
		}

		List<List<I_GetConceptData>> conceptsSets = new ArrayList<List<I_GetConceptData>>();

		List<I_GetConceptData> membersNotPartitioned = TerminologyProjectDAO.getMembersNotPartitioned(scheme, config);

		int totalPartitioned = 0;
		int partitionNumber = 0;
		for (int portion : portions) {
			partitionNumber++;
			int totalInPortion = ((portion*membersNotPartitioned.size())/100);
			if (totalInPortion > 0) {
				List<I_GetConceptData> conceptsSet = new ArrayList<I_GetConceptData>();
				for (int i = 1;i<=totalInPortion;i++) {
					conceptsSet.add(membersNotPartitioned.get(totalPartitioned));
					totalPartitioned++;
				}
				conceptsSets.add(conceptsSet);
			}
		}

		//for (int i = 1;i<=membersNotPartitioned.size()-totalPartitioned;i++) {
		while (totalPartitioned < membersNotPartitioned.size()) {
			conceptsSets.iterator().next().add(membersNotPartitioned.get(totalPartitioned));
			totalPartitioned++;
		}
		return conceptsSets;
	}

}
