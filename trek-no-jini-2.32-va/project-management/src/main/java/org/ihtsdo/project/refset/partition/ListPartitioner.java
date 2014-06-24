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

import java.util.HashSet;
import java.util.Set;

import javax.swing.JList;
import javax.swing.ListModel;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;

/**
 * The Class ListPartitioner.
 */
public class ListPartitioner extends RefsetPartitioner {

	/** The list. */
	JList list;
	
	/** The list ids. */
	Set <Integer> listIds;
	
	/**
	 * Instantiates a new list partitioner.
	 *
	 * @param list the list
	 */
	public ListPartitioner(JList list){
		this.list=list;
	}
	
	/* (non-Javadoc)
	 * @see org.ihtsdo.project.refset.partition.RefsetPartitioner#evaluateMember(org.dwfa.ace.api.I_GetConceptData, org.dwfa.ace.api.I_ConfigAceFrame)
	 */
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

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "Dropping concepts";
	}
}
