/**
 * Copyright (c) 2009 International Health Terminology Standards Development
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
package org.dwfa.ace.task.data.checks;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import javax.swing.JList;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_ModelTerminologyList;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.refset.RefsetOverlapValidator;
import org.dwfa.ace.task.AceTaskUtil;
import org.dwfa.ace.task.WorkerAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;


@BeanList(specs = { @Spec(directory = "tasks/ace/data checks", type = BeanType.TASK_BEAN) })
public class VerifyRefsetMemberOverlaps extends AbstractTask{
		

	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
		try {
			I_ConfigAceFrame config = (I_ConfigAceFrame) worker
					.readAttachement(WorkerAttachmentKeys.ACE_FRAME_CONFIG.name());
			
			I_TermFactory termFactory = LocalVersionedTerminology.get();
						
			RefsetOverlapValidator rov = new RefsetOverlapValidator();
			rov.validate();
						
			if(rov.hasOverlaps()){
				/*
				 * Add members to list
				 */
				JList conceptList = config.getBatchConceptList();
				I_ModelTerminologyList model = (I_ModelTerminologyList) conceptList.getModel();
				model.clear();
				for(Integer i : rov.getOverlapedMemberComopnentIds()){
					model.addElement(termFactory.getConcept(i.intValue()));
				}//End for loop	
			}//End if
			
			config.showListView();

			return Condition.CONTINUE;
		} catch (IOException e) {
			throw new TaskFailedException(e);
		} catch (TerminologyException e) {
			throw new TaskFailedException(e);
		} catch (IllegalArgumentException e) {
			throw new TaskFailedException(e);
		} 		
	}//End method evaluate
	
	public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
		// Nothing to do...
	}//End method complete
	
	public Collection<Condition> getConditions() {
		return CONTINUE_CONDITION;
	}//End methos getConditions

	public int[] getDataContainerIds() {
		return new int[] {};
	}//End method getDataContainerIds
	
}//End class VerifyRefsetMemberOverlaps
