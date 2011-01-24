package org.ihtsdo.project.panel.dnd;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.Partition;
import org.ihtsdo.project.panel.TranslationHelperPanel;

public class GetPartitionMember implements I_GetItemForModel {

	private Partition partition;
	private I_ConfigAceFrame config;
	public GetPartitionMember(Partition partition,I_ConfigAceFrame config){
		this.partition=partition;
		this.config=config;
	}
	@Override
	public Object getItemFromConcept(I_GetConceptData concept) throws Exception {

		TerminologyProjectDAO.addConceptAsPartitionMember(concept, 
				partition.getUids().iterator().next(), config);	
		Terms.get().commit();
		TranslationHelperPanel.setFocusToProjectPanel();
		return TerminologyProjectDAO.getPartitionMember(concept, partition.getId(), config);
		
	}
	

}
