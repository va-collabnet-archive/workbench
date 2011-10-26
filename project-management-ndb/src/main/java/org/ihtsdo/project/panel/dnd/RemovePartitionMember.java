package org.ihtsdo.project.panel.dnd;

import java.io.IOException;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.PartitionMember;
import org.ihtsdo.project.panel.TranslationHelperPanel;


public class RemovePartitionMember implements I_RemoveItemFromModel {


	private I_ConfigAceFrame config;
	public RemovePartitionMember(I_ConfigAceFrame config){
		this.config=config;
	}
	@Override
	public void removeItemFromObject(Object obj) throws Exception {
		if (obj instanceof PartitionMember){
			TerminologyProjectDAO.retirePartitionMember((PartitionMember)obj, config);
			Terms.get().commit();
			TranslationHelperPanel.setFocusToProjectPanel();
		}

	}

}
