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
package org.ihtsdo.translation.tasks;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.bpa.process.I_DefineTask;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.jini.TermEntry;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.ProjectPermissionsAPI;

/**
 * The Class UsersSelectionForWorkflowPanelExec.
 */
public class UsersSelectionForWorkflowPanelExec extends JPanel {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The config. */
	public I_ConfigAceFrame config;
	
	/** The hash map role. */
	public HashMap<I_GetConceptData,JComboBox>hashMapRole;

	/**
	 * Instantiates a new users selection for workflow panel exec.
	 *
	 * @param wfProcess the wf process
	 * @param config the config
	 */
	public UsersSelectionForWorkflowPanelExec(I_EncodeBusinessProcess wfProcess, I_ConfigAceFrame config) {
		super();
		this.config = config;		
		try {
			I_TermFactory tf = Terms.get();

			// TODO: simple permissions implementation, using the projects hierarchy root
			I_GetConceptData projectsRootConcept = tf.getConcept(ArchitectonicAuxiliary.Concept.PROJECTS_ROOT_HIERARCHY.getUids());

			ProjectPermissionsAPI permissionsApi = new ProjectPermissionsAPI(config);
			
			setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			int row = 0;
			hashMapRole =new HashMap<I_GetConceptData,JComboBox>();
			Collection<I_DefineTask> tasks = wfProcess.getTasks();
			TermEntry role=null;
			
			
			for(I_DefineTask task:tasks){
				PropertyDescriptor[] props = task.getBeanInfo().getPropertyDescriptors();
				for (PropertyDescriptor prop:props){
					if (prop.getName().equals("stepRole")){

						role=(TermEntry) prop.getReadMethod().invoke(task,(Object[]) null);

						if (role!=null){

							I_GetConceptData roleC=Terms.get().getConcept(role.ids);
							Set<String> usrList = permissionsApi.getUsersInboxAddressesForRole(roleC, 
									projectsRootConcept);
							usrList.add(config.getUsername() + ".inbox");
							hashMapRole.put(roleC,new JComboBox(usrList.toArray()));
						}
					}
				}
			}

			row++;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 0;
			c.gridy = row;
			c.weightx = 0.5;
			c.ipady=5;
			add(new JLabel(""),c);
			for (I_GetConceptData conc:hashMapRole.keySet())
			{
				row++;
				c.fill = GridBagConstraints.HORIZONTAL;
				c.gridx = 0;
				c.gridy = row;
				c.weightx = 0.5;
				add(new JLabel(conc.toString()),c);
				c.fill = GridBagConstraints.HORIZONTAL;
				c.gridx = 1;
				c.gridy = row;
				c.weightx = 1;
				add(hashMapRole.get(conc),c);				
			}
			row++;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 0;
			c.gridy = row;
			c.weightx = 0.5;
			c.ipady=5;
			add(new JLabel(""),c);
			this.revalidate();
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets the hash combo.
	 *
	 * @return the hash combo
	 */
	public HashMap<I_GetConceptData,JComboBox> getHashCombo() {
		return hashMapRole;
	}


}
