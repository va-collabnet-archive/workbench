package org.ihtsdo.translation.tasks;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.FileLinkAPI;
import org.ihtsdo.project.ProjectPermissionsAPI;
import org.ihtsdo.project.model.I_TerminologyProject;

public class UsersSelectionForWorkflowPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public JComboBox translatorCombo;
	public JComboBox reviewer1Combo;
	public JComboBox reviewer2Combo;
	public JComboBox smeCombo;
	public JComboBox ebCombo;
	public JComboBox bpCombo;
	public JTextField worklistName;
	
	public Boolean nameForWorkList = false;
	public Boolean businessProcess = false;
	public Boolean translator = false;
	public Boolean reviewer1 = false;
	public Boolean reviewer2 = false;
	public Boolean sme = false;
	public Boolean editorialBoard = false;
	public I_ConfigAceFrame config;
	public I_TerminologyProject project;

	public UsersSelectionForWorkflowPanel(boolean name, boolean businessProcess, boolean translator, boolean reviewer1, boolean reviewer2,
			boolean sme, boolean editorialBoard, I_TerminologyProject project, I_ConfigAceFrame config) {
		super();
		this.nameForWorkList = name;
		this.businessProcess = businessProcess;
		this.translator = translator;
		this.reviewer1 = reviewer1;
		this.reviewer2 = reviewer2;
		this.sme = sme;
		this.editorialBoard = editorialBoard;
		this.config = config;
		this.project = project;
		
		try {
			I_TermFactory tf = Terms.get();

			// TODO: simple permissions implementation, using the projects hierarchy root
			I_GetConceptData projectsRootConcept = tf.getConcept(ArchitectonicAuxiliary.Concept.PROJECTS_ROOT_HIERARCHY.getUids());

			ProjectPermissionsAPI permissionsApi = new ProjectPermissionsAPI(config);
			
			setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			int row = 0;
			if (name) {
				row++;
				c.fill = GridBagConstraints.HORIZONTAL;
				c.gridx = 0;
				c.gridy = 0;
				c.weightx = 0.5;
				add(new JLabel("Worklist name: "), c);
				c.fill = GridBagConstraints.HORIZONTAL;
				c.gridx = 1;
				c.gridy = 0;
				c.weightx = 1;
				worklistName = new JTextField(30);
				add(worklistName, c);
			}
			if (businessProcess) {
				row++;
				c.fill = GridBagConstraints.HORIZONTAL;
				c.gridx = 0;
				c.gridy = 1;
				c.weightx = 0.5;
				add(new JLabel("Business Process (Workflow): "),c);
				FileLinkAPI fileLinkApi = new FileLinkAPI(config);
				bpCombo = new JComboBox(
						fileLinkApi.getLinksForCategory(tf.getConcept(
								ArchitectonicAuxiliary.Concept.TRANSLATION_BUSINESS_PROCESS_CATEGORY.getUids())).toArray());
				c.fill = GridBagConstraints.HORIZONTAL;
				c.gridx = 1;
				c.gridy = 1;
				c.weightx = 1;
				add(bpCombo,c);
			}

			if (translator) {
				row++;
				c.fill = GridBagConstraints.HORIZONTAL;
				c.gridx = 0;
				c.gridy = row;
				c.weightx = 0.5;
				add(new JLabel("TSP Translator: "),c);
				c.fill = GridBagConstraints.HORIZONTAL;
				c.gridx = 1;
				c.gridy = row;
				c.weightx = 1;
				translatorCombo = new JComboBox(
						permissionsApi.getUsersInboxAddressesForRole(tf.getConcept(
								ArchitectonicAuxiliary.Concept.TRANSLATOR_ONE_TSP_ROLE.getUids()), 
								projectsRootConcept).toArray());
				add(translatorCombo,c);
			}

			if (reviewer1) {
				row++;
				c.fill = GridBagConstraints.HORIZONTAL;
				c.gridx = 0;
				c.gridy = row;
				c.weightx = 0.5;
				add(new JLabel("TSP Reviewer: "),c);
				c.fill = GridBagConstraints.HORIZONTAL;
				c.gridx = 1;
				c.gridy = row;
				c.weightx = 1;
				reviewer1Combo = new JComboBox(
						permissionsApi.getUsersInboxAddressesForRole(tf.getConcept(
								ArchitectonicAuxiliary.Concept.TRANSLATION_TSP_REVIEWER_ROLE.getUids()), 
								projectsRootConcept).toArray());
				add(reviewer1Combo,c);
			}

			if (reviewer2) {
				row++;
				c.fill = GridBagConstraints.HORIZONTAL;
				c.gridx = 0;
				c.gridy = row;
				c.weightx = 0.5;
				add(new JLabel("TPO Reviewer: "),c);
				c.fill = GridBagConstraints.HORIZONTAL;
				c.gridx = 1;
				c.gridy = row;
				c.weightx = 1;
				reviewer2Combo = new JComboBox(
						permissionsApi.getUsersInboxAddressesForRole(tf.getConcept(
								ArchitectonicAuxiliary.Concept.TRANSLATION_TPO_REVIEWER_ROLE.getUids()), 
								projectsRootConcept).toArray());
				add(reviewer2Combo,c);
			}

			if (sme) {
				row++;
				c.fill = GridBagConstraints.HORIZONTAL;
				c.gridx = 0;
				c.gridy = row;
				c.weightx = 0.5;
				add(new JLabel("SME: "),c);
				c.fill = GridBagConstraints.HORIZONTAL;
				c.gridx = 1;
				c.gridy = row;
				c.weightx = 1;
				smeCombo = new JComboBox(
						permissionsApi.getUsersInboxAddressesForRole(tf.getConcept(
								ArchitectonicAuxiliary.Concept.TRANSLATION_SME_ROLE.getUids()), 
								projectsRootConcept).toArray());
				add(smeCombo,c);
			}

			if (editorialBoard) {
				row++;
				c.fill = GridBagConstraints.HORIZONTAL;
				c.gridx = 0;
				c.gridy = row;
				c.weightx = 0.5;
				add(new JLabel("Editorial Board: "),c);
				c.fill = GridBagConstraints.HORIZONTAL;
				c.gridx = 1;
				c.gridy = row;
				c.weightx = 1;
				ebCombo = new JComboBox(
						permissionsApi.getUsersInboxAddressesForRole(tf.getConcept(
								ArchitectonicAuxiliary.Concept.TRANSLATION_EDITORIAL_BOARD_ROLE.getUids()), 
								projectsRootConcept).toArray());
				add(ebCombo,c);
			}

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

	public JComboBox getTranslatorCombo() {
		return translatorCombo;
	}

	public void setTranslatorCombo(JComboBox translatorCombo) {
		this.translatorCombo = translatorCombo;
	}

	public JComboBox getReviewer1Combo() {
		return reviewer1Combo;
	}

	public void setReviewer1Combo(JComboBox reviewer1Combo) {
		this.reviewer1Combo = reviewer1Combo;
	}

	public JComboBox getReviewer2Combo() {
		return reviewer2Combo;
	}

	public void setReviewer2Combo(JComboBox reviewer2Combo) {
		this.reviewer2Combo = reviewer2Combo;
	}

	public JComboBox getSmeCombo() {
		return smeCombo;
	}

	public void setSmeCombo(JComboBox smeCombo) {
		this.smeCombo = smeCombo;
	}

	public JComboBox getEbCombo() {
		return ebCombo;
	}

	public void setEbCombo(JComboBox ebCombo) {
		this.ebCombo = ebCombo;
	}

	public JComboBox getBpCombo() {
		return bpCombo;
	}

	public void setBpCombo(JComboBox bpCombo) {
		this.bpCombo = bpCombo;
	}

	public Boolean getTranslator() {
		return translator;
	}

	public void setTranslator(Boolean translator) {
		this.translator = translator;
	}

	public Boolean getReviewer1() {
		return reviewer1;
	}

	public void setReviewer1(Boolean reviewer1) {
		this.reviewer1 = reviewer1;
	}

	public Boolean getReviewer2() {
		return reviewer2;
	}

	public void setReviewer2(Boolean reviewer2) {
		this.reviewer2 = reviewer2;
	}

	public Boolean getSme() {
		return sme;
	}

	public void setSme(Boolean sme) {
		this.sme = sme;
	}

	public Boolean getEditorialBoard() {
		return editorialBoard;
	}

	public void setEditorialBoard(Boolean editorialBoard) {
		this.editorialBoard = editorialBoard;
	}

	public I_ConfigAceFrame getConfig() {
		return config;
	}

	public void setConfig(I_ConfigAceFrame config) {
		this.config = config;
	}

	public I_TerminologyProject getProject() {
		return project;
	}

	public void setProject(I_TerminologyProject project) {
		this.project = project;
	}

	public JTextField getWorklistName() {
		return worklistName;
	}

	public void setWorklistName(JTextField worklistName) {
		this.worklistName = worklistName;
	}

	public Boolean getBusinessProcess() {
		return businessProcess;
	}

	public void setBusinessProcess(Boolean businessProcess) {
		this.businessProcess = businessProcess;
	}

	public Boolean getNameForWorkList() {
		return nameForWorkList;
	}

	public void setNameForWorkList(Boolean nameForWorkList) {
		this.nameForWorkList = nameForWorkList;
	}


}
