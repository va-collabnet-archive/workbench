package org.ihtsdo.project.refset.partition;

import java.util.HashSet;
import java.util.Set;

import javax.swing.JList;
import javax.swing.ListModel;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;

public class ListPartitioner extends RefsetPartitioner {

	JList list;
	Set <Integer> listIds;
	public ListPartitioner(JList list){
		this.list=list;
	}
	
	@Override
	protected boolean evaluateMember(I_GetConceptData member,
			I_ConfigAceFrame config) {
		if (listIds==null){
			ListModel model = list.getModel();
			listIds=new HashSet<Integer>();
			for (int i=0;i< model.getSize();i++) {
				I_GetConceptData con=(I_GetConceptData)model.getElementAt(i);
				listIds.add(con.getConceptNid());
			}
		}
		if (listIds.contains(member.getConceptNid()))
			return true;
		
		return false;
			
	}

	public String toString() {
		return "Dropping concepts";
	}
}
