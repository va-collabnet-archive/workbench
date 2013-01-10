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
import org.dwfa.ace.api.Terms;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.PartitionMember;
import org.ihtsdo.project.view.TranslationHelperPanel;


/**
 * The Class RemovePartitionMember.
 */
public class RemovePartitionMember implements I_RemoveItemFromModel {


	/** The config. */
	private I_ConfigAceFrame config;
	
	/**
	 * Instantiates a new removes the partition member.
	 *
	 * @param config the config
	 */
	public RemovePartitionMember(I_ConfigAceFrame config){
		this.config=config;
	}
	
	/* (non-Javadoc)
	 * @see org.ihtsdo.project.panel.dnd.I_RemoveItemFromModel#removeItemFromObject(java.lang.Object)
	 */
	@Override
	public void removeItemFromObject(Object obj) throws Exception {
		if (obj instanceof PartitionMember){
			TerminologyProjectDAO.retirePartitionMember((PartitionMember)obj, config);
			Terms.get().commit();
			TranslationHelperPanel.setFocusToProjectPanel();
		}

	}

}
