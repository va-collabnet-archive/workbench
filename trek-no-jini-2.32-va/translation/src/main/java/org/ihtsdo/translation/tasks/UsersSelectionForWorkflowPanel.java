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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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
import org.dwfa.bpa.BusinessProcess;
import org.dwfa.bpa.process.I_DefineTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.jini.TermEntry;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.FileLink;
import org.ihtsdo.project.FileLinkAPI;
import org.ihtsdo.project.ProjectPermissionsAPI;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.I_TerminologyProject;

/**
 * The Class UsersSelectionForWorkflowPanel.
 */
public class UsersSelectionForWorkflowPanel extends JPanel {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The translator combo. */
	public JComboBox translatorCombo;
	
	/** The fast track translator combo. */
	public JComboBox fastTrackTranslatorCombo;
	
	/** The reviewer1 combo. */
	public JComboBox reviewer1Combo;
	
	/** The reviewer2 combo. */
	public JComboBox reviewer2Combo;
	
	/** The sme combo. */
	public JComboBox smeCombo;
	
	/** The super sme combo. */
	public JComboBox superSmeCombo;
	
	/** The eb combo. */
	public JComboBox ebCombo;
	
	/** The bp combo. */
	public JComboBox bpCombo;
	
	/** The worklist name. */
	public JTextField worklistName;

	/** The name for work list. */
	public Boolean nameForWorkList = false;
	
	/** The business process. */
	public Boolean businessProcess = false;
	
	/** The translator. */
	public Boolean translator = false;
	
	/** The fast track translator. */
	public Boolean fastTrackTranslator = false;
	
	/** The reviewer1. */
	public Boolean reviewer1 = false;
	
	/** The reviewer2. */
	public Boolean reviewer2 = false;
	
	/** The sme. */
	public Boolean sme = false;
	
	/** The super sme. */
	public Boolean superSme = false;
	
	/** The editorial board. */
	public Boolean editorialBoard = false;
	
	/** The config. */
	public I_ConfigAceFrame config;
	
	/** The project. */
	public I_TerminologyProject project;
	
	/** The row. */
	private int row;
	
	/** The c. */
	private GridBagConstraints c;
	
	/** The roles panel. */
	private JPanel rolesPanel;
	
	/** The role trans. */
	private String roleTrans;
	
	/** The role fast trans. */
	private String roleFastTrans;
	
	/** The role rev1. */
	private String roleRev1;
	
	/** The role rev2. */
	private String roleRev2;
	
	/** The role sme. */
	private String roleSme;
	
	/** The role super sme. */
	private String roleSuperSme;
	
	/** The role ed b. */
	private String roleEdB;

	/**
	 * Instantiates a new users selection for workflow panel.
	 *
	 * @param project the project
	 * @param config the config
	 */
	public UsersSelectionForWorkflowPanel(I_TerminologyProject project, I_ConfigAceFrame config) {
		super();
		this.config = config;
		this.project = project;

		try {
			I_TermFactory tf = Terms.get();

			// TODO: simple permissions implementation, using the projects hierarchy root
			roleTrans = tf.getConcept(
					ArchitectonicAuxiliary.Concept.TRANSLATOR_ONE_TSP_ROLE.getUids()).toString();
			roleFastTrans = tf.getConcept(
					ArchitectonicAuxiliary.Concept.TRANSLATOR_FAST_TRACK_ROLE.getUids()).toString();
			roleRev1 = tf.getConcept(
					ArchitectonicAuxiliary.Concept.TRANSLATION_TSP_REVIEWER_ROLE.getUids()).toString();
			roleRev2 = tf.getConcept(
					ArchitectonicAuxiliary.Concept.TRANSLATION_TPO_REVIEWER_ROLE.getUids()).toString();
			roleSme = tf.getConcept(
					ArchitectonicAuxiliary.Concept.TRANSLATION_SME_ROLE.getUids()).toString();
			roleSuperSme = tf.getConcept(
					ArchitectonicAuxiliary.Concept.TRANSLATION_SME_ROLE.getUids()).toString();
			roleEdB = tf.getConcept(
					ArchitectonicAuxiliary.Concept.TRANSLATION_EDITORIAL_BOARD_ROLE.getUids()).toString();

			setLayout(new GridBagLayout());
			c = new GridBagConstraints();
			row = 0;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 0;
			c.gridy = row;
			c.weightx = 0.5;
			add(new JLabel("Worklist name: "), c);
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 1;
			c.gridy = row;
			c.weightx = 1;
			worklistName = new JTextField(30);
			add(worklistName, c);
			
			row = 1;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 0;
			c.gridy = row;
			c.weightx = 0.5;
			add(new JLabel("Business Process (Workflow): "),c);
			FileLinkAPI fileLinkApi = new FileLinkAPI(config);
			bpCombo = new JComboBox(
					fileLinkApi.getLinksForCategory(tf.getConcept(
							ArchitectonicAuxiliary.Concept.TRANSLATION_BUSINESS_PROCESS_CATEGORY.getUids())).toArray());
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 1;
			c.gridy = row;
			c.weightx = 1;
			add(bpCombo,c);

			rolesPanel = new JPanel();
			row = 2;
			c.fill = GridBagConstraints.BOTH;
			c.gridx = 0;
			c.gridy = row;
			c.gridwidth = 2;
			c.weightx = 1;
			add(rolesPanel,c);

			bpCombo.addActionListener(new ActionListener () {
				public void actionPerformed(ActionEvent e) {
					callUpdate();
				}
			});
			
			callUpdate();

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
	 * Call update.
	 */
	private void callUpdate() {
		FileLink fileLink = (FileLink)bpCombo.getSelectedItem();
		if (fileLink != null) {
			File file = fileLink.getFile();
			BusinessProcess bp = 
				TerminologyProjectDAO.getBusinessProcess(file);
			try {
				updateRolesList(bp);
			} catch (IllegalArgumentException e1) {
				e1.printStackTrace();
			} catch (TerminologyException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (IntrospectionException e1) {
				e1.printStackTrace();
			} catch (IllegalAccessException e1) {
				e1.printStackTrace();
			} catch (InvocationTargetException e1) {
				e1.printStackTrace();
			}
		}
	}
 
	/**
	 * Update roles list.
	 *
	 * @param bp the bp
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws IntrospectionException the introspection exception
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IllegalAccessException the illegal access exception
	 * @throws InvocationTargetException the invocation target exception
	 */
	private void updateRolesList(BusinessProcess bp) throws TerminologyException, IOException, IntrospectionException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		translator = false;
		fastTrackTranslator = false;
		reviewer1 = false;
		reviewer2 = false;
		sme = false;
		superSme = false;
		editorialBoard = false;
		I_TermFactory tf = Terms.get();
		ProjectPermissionsAPI permissionsApi = new ProjectPermissionsAPI(config);
		I_GetConceptData projectsRootConcept = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.PROJECTS_ROOT_HIERARCHY.getUids());


		HashMap<I_GetConceptData,JComboBox> hashMapRole =new HashMap<I_GetConceptData,JComboBox>();
		Collection<I_DefineTask> tasks = bp.getTasks();
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

		for (I_GetConceptData con:hashMapRole.keySet()){
			if (con.toString().equals(roleTrans)){
				translator = true;
			}else if (con.toString().equals(roleFastTrans)) {
				fastTrackTranslator = true;
			}else if (con.toString().equals(roleRev1)){
				reviewer1 = true;
			}else if (con.toString().equals(roleRev2)){
				reviewer2 = true;
			}else if (con.toString().equals(roleSme)){
				sme = true;
			}else if (con.toString().equals(roleSuperSme)){
				superSme = true;
			}else if (con.toString().equals(roleEdB)){
				editorialBoard = true;;
			}

		}

		remove(rolesPanel);
		rolesPanel = new JPanel();
		rolesPanel.setLayout(new GridBagLayout());
		GridBagConstraints c2 = new GridBagConstraints();
		int row2 = 0;

		if (translator) {

			c2.fill = GridBagConstraints.HORIZONTAL;
			c2.gridx = 0;
			c2.gridy = row2;
			c2.weightx = 0.5;
			rolesPanel.add(new JLabel("TSP Translator: "),c2);
			c2.fill = GridBagConstraints.HORIZONTAL;
			c2.gridx = 1;
			c2.gridy = row2;
			c2.weightx = 1;
			translatorCombo = new JComboBox(
					permissionsApi.getUsersInboxAddressesForRole(tf.getConcept(
							ArchitectonicAuxiliary.Concept.TRANSLATOR_ONE_TSP_ROLE.getUids()), 
							projectsRootConcept).toArray());
			rolesPanel.add(translatorCombo,c2);
		}
		if (fastTrackTranslator) {
			row2++;
			c2.fill = GridBagConstraints.HORIZONTAL;
			c2.gridx = 0;
			c2.gridy = row2;
			c2.weightx = 0.5;
			rolesPanel.add(new JLabel("TSP Fast Track Translator: "),c2);
			c2.fill = GridBagConstraints.HORIZONTAL;
			c2.gridx = 1;
			c2.gridy = row2;
			c2.weightx = 1;
			fastTrackTranslatorCombo = new JComboBox(
					permissionsApi.getUsersInboxAddressesForRole(tf.getConcept(
							ArchitectonicAuxiliary.Concept.TRANSLATOR_FAST_TRACK_ROLE.getUids()), 
							projectsRootConcept).toArray());
			rolesPanel.add(fastTrackTranslatorCombo,c2);
		}

		if (reviewer1) {
			row2++;
			c2.fill = GridBagConstraints.HORIZONTAL;
			c2.gridx = 0;
			c2.gridy = row2;
			c2.weightx = 0.5;
			rolesPanel.add(new JLabel("TSP Reviewer: "),c2);
			c2.fill = GridBagConstraints.HORIZONTAL;
			c2.gridx = 1;
			c2.gridy = row2;
			c2.weightx = 1;
			reviewer1Combo = new JComboBox(
					permissionsApi.getUsersInboxAddressesForRole(tf.getConcept(
							ArchitectonicAuxiliary.Concept.TRANSLATION_TSP_REVIEWER_ROLE.getUids()), 
							projectsRootConcept).toArray());
			rolesPanel.add(reviewer1Combo,c2);
		}

		if (reviewer2) {
			row2++;
			c2.fill = GridBagConstraints.HORIZONTAL;
			c2.gridx = 0;
			c2.gridy = row2;
			c2.weightx = 0.5;
			rolesPanel.add(new JLabel("TPO Reviewer: "),c2);
			c2.fill = GridBagConstraints.HORIZONTAL;
			c2.gridx = 1;
			c2.gridy = row2;
			c2.weightx = 1;
			reviewer2Combo = new JComboBox(
					permissionsApi.getUsersInboxAddressesForRole(tf.getConcept(
							ArchitectonicAuxiliary.Concept.TRANSLATION_TPO_REVIEWER_ROLE.getUids()), 
							projectsRootConcept).toArray());
			rolesPanel.add(reviewer2Combo,c2);
		}

		if (sme) {
			row2++;
			c2.fill = GridBagConstraints.HORIZONTAL;
			c2.gridx = 0;
			c2.gridy = row2;
			c2.weightx = 0.5;
			rolesPanel.add(new JLabel("SME: "),c2);
			c2.fill = GridBagConstraints.HORIZONTAL;
			c2.gridx = 1;
			c2.gridy = row2;
			c2.weightx = 1;
			smeCombo = new JComboBox(
					permissionsApi.getUsersInboxAddressesForRole(tf.getConcept(
							ArchitectonicAuxiliary.Concept.TRANSLATION_SME_ROLE.getUids()), 
							projectsRootConcept).toArray());
			rolesPanel.add(smeCombo,c2);
		}
		if (superSme) {
			row2++;
			c2.fill = GridBagConstraints.HORIZONTAL;
			c2.gridx = 0;
			c2.gridy = row2;
			c2.weightx = 0.5;
			rolesPanel.add(new JLabel("Super SME: "),c2);
			c2.fill = GridBagConstraints.HORIZONTAL;
			c2.gridx = 1;
			c2.gridy = row2;
			c2.weightx = 1;
			superSmeCombo = new JComboBox(
					permissionsApi.getUsersInboxAddressesForRole(tf.getConcept(
							ArchitectonicAuxiliary.Concept.TRANSLATION_SME_ROLE.getUids()), 
							projectsRootConcept).toArray());
			rolesPanel.add(superSmeCombo,c2);
		}

		if (editorialBoard) {
			row2++;
			c2.fill = GridBagConstraints.HORIZONTAL;
			c2.gridx = 0;
			c2.gridy = row2;
			c2.weightx = 0.5;
			rolesPanel.add(new JLabel("Editorial Board: "),c2);
			c2.fill = GridBagConstraints.HORIZONTAL;
			c2.gridx = 1;
			c2.gridy = row2;
			c2.weightx = 1;
			ebCombo = new JComboBox(
					permissionsApi.getUsersInboxAddressesForRole(tf.getConcept(
							ArchitectonicAuxiliary.Concept.TRANSLATION_EDITORIAL_BOARD_ROLE.getUids()), 
							projectsRootConcept).toArray());
			rolesPanel.add(ebCombo,c2);
		}
		rolesPanel.revalidate();
		add(rolesPanel, c);
		setSize(getWidth()+1, getHeight()+1);
		setSize(getWidth()-1, getHeight()-1);
		this.revalidate();
	}

	/**
	 * Gets the translator combo.
	 *
	 * @return the translator combo
	 */
	public JComboBox getTranslatorCombo() {
		return translatorCombo;
	}

	/**
	 * Sets the translator combo.
	 *
	 * @param translatorCombo the new translator combo
	 */
	public void setTranslatorCombo(JComboBox translatorCombo) {
		this.translatorCombo = translatorCombo;
	}

	/**
	 * Gets the reviewer1 combo.
	 *
	 * @return the reviewer1 combo
	 */
	public JComboBox getReviewer1Combo() {
		return reviewer1Combo;
	}

	/**
	 * Sets the reviewer1 combo.
	 *
	 * @param reviewer1Combo the new reviewer1 combo
	 */
	public void setReviewer1Combo(JComboBox reviewer1Combo) {
		this.reviewer1Combo = reviewer1Combo;
	}

	/**
	 * Gets the reviewer2 combo.
	 *
	 * @return the reviewer2 combo
	 */
	public JComboBox getReviewer2Combo() {
		return reviewer2Combo;
	}

	/**
	 * Sets the reviewer2 combo.
	 *
	 * @param reviewer2Combo the new reviewer2 combo
	 */
	public void setReviewer2Combo(JComboBox reviewer2Combo) {
		this.reviewer2Combo = reviewer2Combo;
	}

	/**
	 * Gets the sme combo.
	 *
	 * @return the sme combo
	 */
	public JComboBox getSmeCombo() {
		return smeCombo;
	}

	/**
	 * Sets the sme combo.
	 *
	 * @param smeCombo the new sme combo
	 */
	public void setSmeCombo(JComboBox smeCombo) {
		this.smeCombo = smeCombo;
	}

	/**
	 * Gets the eb combo.
	 *
	 * @return the eb combo
	 */
	public JComboBox getEbCombo() {
		return ebCombo;
	}

	/**
	 * Sets the eb combo.
	 *
	 * @param ebCombo the new eb combo
	 */
	public void setEbCombo(JComboBox ebCombo) {
		this.ebCombo = ebCombo;
	}

	/**
	 * Gets the bp combo.
	 *
	 * @return the bp combo
	 */
	public JComboBox getBpCombo() {
		return bpCombo;
	}

	/**
	 * Sets the bp combo.
	 *
	 * @param bpCombo the new bp combo
	 */
	public void setBpCombo(JComboBox bpCombo) {
		this.bpCombo = bpCombo;
	}

	/**
	 * Gets the translator.
	 *
	 * @return the translator
	 */
	public Boolean getTranslator() {
		return translator;
	}

	/**
	 * Sets the translator.
	 *
	 * @param translator the new translator
	 */
	public void setTranslator(Boolean translator) {
		this.translator = translator;
	}

	/**
	 * Gets the reviewer1.
	 *
	 * @return the reviewer1
	 */
	public Boolean getReviewer1() {
		return reviewer1;
	}

	/**
	 * Sets the reviewer1.
	 *
	 * @param reviewer1 the new reviewer1
	 */
	public void setReviewer1(Boolean reviewer1) {
		this.reviewer1 = reviewer1;
	}

	/**
	 * Gets the reviewer2.
	 *
	 * @return the reviewer2
	 */
	public Boolean getReviewer2() {
		return reviewer2;
	}

	/**
	 * Sets the reviewer2.
	 *
	 * @param reviewer2 the new reviewer2
	 */
	public void setReviewer2(Boolean reviewer2) {
		this.reviewer2 = reviewer2;
	}

	/**
	 * Gets the sme.
	 *
	 * @return the sme
	 */
	public Boolean getSme() {
		return sme;
	}

	/**
	 * Sets the sme.
	 *
	 * @param sme the new sme
	 */
	public void setSme(Boolean sme) {
		this.sme = sme;
	}

	/**
	 * Gets the editorial board.
	 *
	 * @return the editorial board
	 */
	public Boolean getEditorialBoard() {
		return editorialBoard;
	}

	/**
	 * Sets the editorial board.
	 *
	 * @param editorialBoard the new editorial board
	 */
	public void setEditorialBoard(Boolean editorialBoard) {
		this.editorialBoard = editorialBoard;
	}

	/**
	 * Gets the config.
	 *
	 * @return the config
	 */
	public I_ConfigAceFrame getConfig() {
		return config;
	}

	/**
	 * Sets the config.
	 *
	 * @param config the new config
	 */
	public void setConfig(I_ConfigAceFrame config) {
		this.config = config;
	}

	/**
	 * Gets the project.
	 *
	 * @return the project
	 */
	public I_TerminologyProject getProject() {
		return project;
	}

	/**
	 * Sets the project.
	 *
	 * @param project the new project
	 */
	public void setProject(I_TerminologyProject project) {
		this.project = project;
	}

	/**
	 * Gets the worklist name.
	 *
	 * @return the worklist name
	 */
	public JTextField getWorklistName() {
		return worklistName;
	}

	/**
	 * Sets the worklist name.
	 *
	 * @param worklistName the new worklist name
	 */
	public void setWorklistName(JTextField worklistName) {
		this.worklistName = worklistName;
	}

	/**
	 * Gets the business process.
	 *
	 * @return the business process
	 */
	public Boolean getBusinessProcess() {
		return businessProcess;
	}

	/**
	 * Sets the business process.
	 *
	 * @param businessProcess the new business process
	 */
	public void setBusinessProcess(Boolean businessProcess) {
		this.businessProcess = businessProcess;
	}

	/**
	 * Gets the name for work list.
	 *
	 * @return the name for work list
	 */
	public Boolean getNameForWorkList() {
		return nameForWorkList;
	}

	/**
	 * Sets the name for work list.
	 *
	 * @param nameForWorkList the new name for work list
	 */
	public void setNameForWorkList(Boolean nameForWorkList) {
		this.nameForWorkList = nameForWorkList;
	}

	/**
	 * Gets the fast track translator.
	 *
	 * @return the fast track translator
	 */
	public Boolean getFastTrackTranslator() {
		return fastTrackTranslator;
	}

	/**
	 * Sets the fast track translator.
	 *
	 * @param fastTrackTranslator the new fast track translator
	 */
	public void setFastTrackTranslator(Boolean fastTrackTranslator) {
		this.fastTrackTranslator = fastTrackTranslator;
	}

	/**
	 * Gets the super sme.
	 *
	 * @return the super sme
	 */
	public Boolean getSuperSme() {
		return superSme;
	}

	/**
	 * Sets the super sme.
	 *
	 * @param superSme the new super sme
	 */
	public void setSuperSme(Boolean superSme) {
		this.superSme = superSme;
	}

	/**
	 * Gets the fast track translator combo.
	 *
	 * @return the fast track translator combo
	 */
	public JComboBox getFastTrackTranslatorCombo() {
		return fastTrackTranslatorCombo;
	}

	/**
	 * Sets the fast track translator combo.
	 *
	 * @param fastTrackTranslatorCombo the new fast track translator combo
	 */
	public void setFastTrackTranslatorCombo(JComboBox fastTrackTranslatorCombo) {
		this.fastTrackTranslatorCombo = fastTrackTranslatorCombo;
	}

	/**
	 * Gets the super sme combo.
	 *
	 * @return the super sme combo
	 */
	public JComboBox getSuperSmeCombo() {
		return superSmeCombo;
	}

	/**
	 * Sets the super sme combo.
	 *
	 * @param superSmeCombo the new super sme combo
	 */
	public void setSuperSmeCombo(JComboBox superSmeCombo) {
		this.superSmeCombo = superSmeCombo;
	}


}
