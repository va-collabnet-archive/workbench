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

public class UsersSelectionForWorkflowPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public JComboBox translatorCombo;
	public JComboBox fastTrackTranslatorCombo;
	public JComboBox reviewer1Combo;
	public JComboBox reviewer2Combo;
	public JComboBox smeCombo;
	public JComboBox superSmeCombo;
	public JComboBox ebCombo;
	public JComboBox bpCombo;
	public JTextField worklistName;

	public Boolean nameForWorkList = false;
	public Boolean businessProcess = false;
	public Boolean translator = false;
	public Boolean fastTrackTranslator = false;
	public Boolean reviewer1 = false;
	public Boolean reviewer2 = false;
	public Boolean sme = false;
	public Boolean superSme = false;
	public Boolean editorialBoard = false;
	public I_ConfigAceFrame config;
	public I_TerminologyProject project;
	private int row;
	private GridBagConstraints c;
	private JPanel rolesPanel;
	private String roleTrans;
	private String roleFastTrans;
	private String roleRev1;
	private String roleRev2;
	private String roleSme;
	private String roleSuperSme;
	private String roleEdB;

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
					ArchitectonicAuxiliary.Concept.TRANSLATION_SUPER_SME_ROLE.getUids()).toString();
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
							ArchitectonicAuxiliary.Concept.TRANSLATION_SUPER_SME_ROLE.getUids()), 
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

	public Boolean getFastTrackTranslator() {
		return fastTrackTranslator;
	}

	public void setFastTrackTranslator(Boolean fastTrackTranslator) {
		this.fastTrackTranslator = fastTrackTranslator;
	}

	public Boolean getSuperSme() {
		return superSme;
	}

	public void setSuperSme(Boolean superSme) {
		this.superSme = superSme;
	}

	public JComboBox getFastTrackTranslatorCombo() {
		return fastTrackTranslatorCombo;
	}

	public void setFastTrackTranslatorCombo(JComboBox fastTrackTranslatorCombo) {
		this.fastTrackTranslatorCombo = fastTrackTranslatorCombo;
	}

	public JComboBox getSuperSmeCombo() {
		return superSmeCombo;
	}

	public void setSuperSmeCombo(JComboBox superSmeCombo) {
		this.superSmeCombo = superSmeCombo;
	}


}
