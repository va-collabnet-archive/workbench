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
import javax.swing.JTextField;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.bpa.PropertyDescriptorWithTarget;
import org.dwfa.bpa.process.I_DefineTask;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.jini.TermEntry;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.FileLinkAPI;
import org.ihtsdo.project.ProjectPermissionsAPI;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.I_TerminologyProject;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.model.WorkListMember;
import org.ihtsdo.project.refset.PromotionRefset;

public class UsersSelectionForWorkflowPanelExec extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public I_ConfigAceFrame config;
	public HashMap<I_GetConceptData,JComboBox>hashMapRole;

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

	public HashMap<I_GetConceptData,JComboBox> getHashCombo() {
		return hashMapRole;
	}


}
