/*
 * Created by JFormDesigner on Tue Mar 23 14:48:37 GMT-04:00 2010
 */

package org.ihtsdo.project.panel.details;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.*;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.border.*;
import javax.swing.event.*;

import org.dwfa.ace.TermComponentLabel;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.ihtsdo.project.ProjectPermissionsAPI;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.help.HelpApi;
import org.ihtsdo.project.model.PartitionScheme;
import org.ihtsdo.project.model.WorkSet;
import org.ihtsdo.project.model.WorkSetMember;
import org.ihtsdo.project.panel.TranslationHelperPanel;
import org.ihtsdo.project.panel.dnd.ObjectTransferHandler;
import org.ihtsdo.project.util.IconUtilities;

/**
 * @author Guillermo Reynoso
 */
public class WorkSetDetailsPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private WorkSet workSet;
	private I_ConfigAceFrame config;
	private TermComponentLabel sourceRefsetLabel;
	private DefaultListModel list3Model;
	private DefaultListModel list4Model;
	private DefaultListModel list1Model;
	private DefaultListModel list5Model;
	private DefaultListModel list2Model;

	ObjectTransferHandler ConceptDnDHandler;
	private String partitionName;

	public WorkSetDetailsPanel(WorkSet workSet, I_ConfigAceFrame config) {
		initComponents();
		this.workSet = workSet;
		this.config = config;
		I_TermFactory tf = Terms.get();
		try {
			label15.setIcon(IconUtilities.helpIcon);
			label15.setText("");
			pBarW.setVisible(false);
			pBarS.setVisible(false);
			pBarE.setVisible(false);
			pBarP.setVisible(false);
			textField1.setText(workSet.getName());
			label6.setText(workSet.getProject(config).getName());
			button7.setEnabled(false);
			list2Model = new DefaultListModel();
			list2Model.addListDataListener(new ListDataListener() {
				@Override
				public void intervalRemoved(ListDataEvent e) {}
				@Override
				public void intervalAdded(ListDataEvent e) {
					pBarS.setVisible(true);
					updateList1Content();
					pBarS.setVisible(false);
				}
				@Override
				public void contentsChanged(ListDataEvent e) {}
			});
			list2.setModel(list2Model);
			if(workSet != null && workSet.getSourceRefset() != null){
				list2Model.addElement(workSet.getSourceRefset());
			}
			list2.setName(ProjectDetailsPanel.TARGET_LIST_NAME);
			list2.setMaximumSize(new Dimension(300,25));
			list2.setMinimumSize(new Dimension(300,25));
			list2.setBorder(new BevelBorder(BevelBorder.LOWERED));
			ConceptDnDHandler =	new ObjectTransferHandler(this.config,null);
			list2.setTransferHandler(ConceptDnDHandler);

			sourceRefsetLabel = new TermComponentLabel();
			sourceRefsetLabel.setTermComponent(workSet.getSourceRefset());
			sourceRefsetLabel.setAlignmentX(LEFT_ALIGNMENT);
			//panel3.add(sourceLanguageLabel);

			list3Model = new DefaultListModel();
			for (I_GetConceptData exclusionRefset : workSet.getExclusionRefsets()) {
				list3Model.addElement(exclusionRefset);
			}
			list3.setModel(list3Model);
			ConceptDnDHandler=new  ObjectTransferHandler(this.config,null);
			list3.setTransferHandler(ConceptDnDHandler);
			list3.validate();

			if (workSet.getSourceRefset() != null) {
				updateList1Content();
			}

			updateList4Content();
			updateList5Content();

			ProjectPermissionsAPI permissionsApi = new ProjectPermissionsAPI(config);
			boolean isWorkSetManager = permissionsApi.checkPermissionForProject(
					config.getDbConfig().getUserConcept(), 
					tf.getConcept(ArchitectonicAuxiliary.Concept.PROJECTS_ROOT_HIERARCHY.localize().getNid()),
					tf.getConcept(ArchitectonicAuxiliary.Concept.WORKSET_MANAGER_ROLE.localize().getNid()));

			if (workSet.getName().startsWith("Maintenance -")) {
				isWorkSetManager = false;
			}

			if (!isWorkSetManager) {
				button1.setVisible(false);
				button4.setVisible(false);
				button5.setVisible(false);
				button6.setVisible(false);
				button7.setVisible(false);
				button8.setVisible(false);
				button10.setVisible(false);
				textField1.setEditable(false);
				list1.setEnabled(false);
				list3.setEnabled(false);
				sourceRefsetLabel.setEnabled(false);
			}

			boolean isPartitioningManager = TerminologyProjectDAO.checkPermissionForProject(
					config.getDbConfig().getUserConcept(), 
					tf.getConcept(ArchitectonicAuxiliary.Concept.PROJECTS_ROOT_HIERARCHY.localize().getNid()),
					tf.getConcept(ArchitectonicAuxiliary.Concept.PARTITIONING_MANAGER_ROLE.localize().getNid()), 
					config);

			if (workSet.getName().startsWith("Maintenance -")) {
				isPartitioningManager = false;
			}

			if (!isPartitioningManager) {
				button2.setVisible(false);
				button3.setVisible(false);
				button9.setVisible(false);
			}

		} catch (Exception e) {
			JOptionPane.showMessageDialog(this,
					e.getMessage(),
					"Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	private void updateList4Content() {
		list4Model = new DefaultListModel();
		try {
			List<WorkSetMember> members = workSet.getWorkSetMembers();

			Collections.sort(members,
					new Comparator<WorkSetMember>()
					{
				public int compare(WorkSetMember f1, WorkSetMember f2)
				{
					return f1.toString().compareTo(f2.toString());
				}
					});

			for (WorkSetMember member : members) {
				list4Model.addElement(member.getConcept());
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this,
					e.getMessage(),
					"Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		if(list1Model != null){
			worksetMembersCounter.setText("(" + list4Model.size() + " out of " + list1Model.size() + ")");
		}else{
			worksetMembersCounter.setText("(" + list4Model.size() + ")");
		}
		worksetMembersCounter.revalidate();
		list4.setModel(list4Model);
		list4.validate();
	}

	private void updateList5Content() {
		list5Model = new DefaultListModel();

		List<PartitionScheme> schemes = workSet.getPartitionSchemes(config);

		Collections.sort(schemes,
				new Comparator<PartitionScheme>()
				{
			public int compare(PartitionScheme f1, PartitionScheme f2)
			{
				return f1.toString().compareTo(f2.toString());
			}
				});

		for (PartitionScheme scheme : schemes) {
			list5Model.addElement(scheme);
		}
		list5.setModel(list5Model);
		list5.validate();
	}

	private void updateList1Content() {
		I_TermFactory tf = Terms.get();
		list1Model = new DefaultListModel();
		try {
			if (((I_GetConceptData)list2Model.getElementAt(0)) == null && workSet.getSourceRefset() == null) {
				throw new Exception("No source refset defined");
			}
			if (((I_GetConceptData)list2Model.getElementAt(0)) != null) {
				if (workSet.getSourceRefset() == null) {
					I_GetConceptData selectedTargetRefset = ((I_GetConceptData)list2Model.getElementAt(0));
					workSet.setSourceRefset(selectedTargetRefset);
				} else if (((I_GetConceptData)list2Model.getElementAt(0)).getNid() != 
					workSet.getSourceRefset().getNid()) {
					I_GetConceptData selectedTargetRefset = ((I_GetConceptData)list2Model.getElementAt(0));
					workSet.setSourceRefset(selectedTargetRefset);
				}
			}

			List<I_GetConceptData> members = new ArrayList<I_GetConceptData>();

			for (I_ExtendByRef member : tf.getRefsetExtensionMembers(workSet.getSourceRefset().getConceptNid())) {
				if (member.getTuples(null, config.getViewPositionSetReadOnly(), config.getPrecedence(), 
						config.getConflictResolutionStrategy()).iterator().next().getStatusNid() == 
					tf.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids())) {
					members.add(tf.getConcept(member.getComponentNid()));
				}
			}

			Collections.sort(members,
					new Comparator<I_GetConceptData>()
					{
						public int compare(I_GetConceptData f1, I_GetConceptData f2)
						{
							return f1.toString().compareTo(f2.toString());
						}
					});

			for (I_GetConceptData member : members) {
				list1Model.addElement(member);
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this,
					e.getMessage(),
					"Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		sourceRefsetCounter.setText("(" + list1Model.size() + ")");
		sourceRefsetCounter.revalidate();
		list1.setModel(list1Model);
		list1.validate();
	}

	private void textField1KeyTyped(KeyEvent e) {
		if (textField1.getText().equals(workSet.getName())) {
			button7.setEnabled(false);
		} else {
			button7.setEnabled(true);
		}
	}

	private void button5ActionPerformed(ActionEvent e) {
		try {
			pBarS.setVisible(true);
			if (workSet.getSourceRefset() == null) {
				if(!list2Model.isEmpty()){
					I_GetConceptData  selectedTargetRefset =(I_GetConceptData)list2Model.getElementAt(0);

					if (TerminologyProjectDAO.validateConceptAsRefset( selectedTargetRefset,this.config)){
						workSet.setSourceRefset(selectedTargetRefset);
					}
					else{
						throw new Exception("Refset not valid!");
					}
				}
			} else if (((I_GetConceptData)list2Model.getElementAt(0)).getNid() != workSet.getSourceRefset().getConceptNid()) {
				I_GetConceptData selectedTargetRefset = (I_GetConceptData)list2Model.getElementAt(0);

				if (TerminologyProjectDAO.validateConceptAsRefset( selectedTargetRefset,this.config)){
					workSet.setSourceRefset(selectedTargetRefset);
				}
				else{
					throw new Exception("Refset not valid!");
				}
			}
			Terms.get().commit();
			pBarS.setVisible(false);
			JOptionPane.showMessageDialog(this,
					"WorkSet saved!", 
					"Message", JOptionPane.INFORMATION_MESSAGE);
			TranslationHelperPanel.refreshProjectPanelNode(config);
		} catch (Exception e1) {
			pBarS.setVisible(false);
			JOptionPane.showMessageDialog(this,
					e1.getMessage(),
					"Error",
					JOptionPane.ERROR_MESSAGE);
			e1.printStackTrace();
		}
	}

	private void button1ActionPerformed(ActionEvent e) {

		int n = JOptionPane.showConfirmDialog(
				this,
				"Would you like to synchronize the WorkSet?",
				"Confirmation",
				JOptionPane.YES_NO_OPTION);

		if (n==0) {
			try {
				if (workSet.getSourceRefset() == null) {
					throw new Exception("Source refset field empty or not saved. Save a valid source refset before Sync.");
				}
				config.getChildrenExpandedNodes().clear();
				pBarS.setMinimum(0);
				pBarS.setMaximum(100);
				pBarS.setIndeterminate(true);
				pBarS.setVisible(true);
				pBarS.repaint();
				pBarS.revalidate();
				panel8.repaint();
				panel8.revalidate();
				repaint();
				revalidate();

				SwingUtilities.invokeLater(new Runnable(){
					public void run(){

						Thread appThr=new Thread(){
							public void run(){
								try {
									workSet.sync(config);
									Terms.get().commit();
								} catch (Exception e) {
									e.printStackTrace();
								}
								updateList4Content();
								pBarS.setVisible(false);
								JOptionPane.showMessageDialog(WorkSetDetailsPanel.this,
										"WorkSet synchronized!", 
										"Message", JOptionPane.INFORMATION_MESSAGE);

								SwingUtilities.invokeLater(new Runnable(){
									public void run(){
										TranslationHelperPanel.refreshProjectPanelNode(config);
									}
								});
							}
						};
						appThr.start();
					}
				});
			} catch (Exception e1) {
				e1.printStackTrace();
				JOptionPane.showMessageDialog(this,
						e1.getMessage(),
						"Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void button7ActionPerformed(ActionEvent e) {
		pBarW.setVisible(true);
		workSet.setName(textField1.getText());
		TerminologyProjectDAO.updateWorkSetMetadata(workSet, config);
		try {
			Terms.get().commit();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		button7.setEnabled(false);

		pBarW.setVisible(false);
		JOptionPane.showMessageDialog(this,
				"WorkSet saved!", 
				"Message", JOptionPane.INFORMATION_MESSAGE);
		TranslationHelperPanel.refreshProjectPanelNode(config);
	}

	private void list3KeyTyped(KeyEvent e) {
		String keyChar = String.valueOf(e.getKeyChar());
		if ("d".equals(keyChar)) {
			removeSelectedList3Items();
		}
	}

	private void removeSelectedList3Items() {
		if(list3.getSelectedIndices().length > 0) {
			int[] tmp = list3.getSelectedIndices();
			int[] selectedIndices = list3.getSelectedIndices();

			for (int i = tmp.length-1; i >=0; i--) {
				selectedIndices = list3.getSelectedIndices();
				list3Model.removeElementAt(selectedIndices[i]);
			} // end-for
		} //
	}

	private void button6ActionPerformed(ActionEvent e) {
		try {
			pBarE.setVisible(true);
			ListModel currentModel = list3.getModel();
			List<I_GetConceptData> currentExclusions = workSet.getExclusionRefsets();
			List<Integer> exclusionIds = new ArrayList<Integer>();
			for (I_GetConceptData exclusion : currentExclusions) {
				exclusionIds.add(exclusion.getConceptNid());
			}
			List<Integer> newExclusionIds = new ArrayList<Integer>();
			for (int i = 0;i < currentModel.getSize();i++) {
				I_GetConceptData listItem = (I_GetConceptData) currentModel.getElementAt(i);
				newExclusionIds.add(listItem.getConceptNid());
				if (!exclusionIds.contains(listItem.getConceptNid())) {
					TerminologyProjectDAO.addRefsetAsExclusion(workSet, listItem, config);
				}
			}
			Terms.get().commit();

			for (I_GetConceptData exclusion : currentExclusions) {
				if (!newExclusionIds.contains(exclusion.getConceptNid())) {
					TerminologyProjectDAO.removeRefsetAsExclusion(workSet, exclusion, config);
				}
			}
			Terms.get().commit();
			pBarE.setVisible(false);
			JOptionPane.showMessageDialog(this,
					"WorkSet saved!", 
					"Message", JOptionPane.INFORMATION_MESSAGE);
			TranslationHelperPanel.refreshProjectPanelNode(config);
		} catch (Exception e1) {
			JOptionPane.showMessageDialog(this,
					e1.getMessage(),
					"Error",
					JOptionPane.ERROR_MESSAGE);
			e1.printStackTrace();
		}
	}

	private void button4ActionPerformed(ActionEvent e) {
		// retire workSet
		int n = JOptionPane.showConfirmDialog(
				this,
				"Would you like to retire the workSet?",
				"Confirmation",
				JOptionPane.YES_NO_OPTION);

		if (n==0) {
			try {

				pBarW.setVisible(true);
				TerminologyProjectDAO.retireWorkSet(workSet, config);
				Terms.get().commit();
				TranslationHelperPanel.refreshProjectPanelParentNode(config);
				pBarW.setVisible(false);
				JOptionPane.showMessageDialog(this,
						"WorkSet retired!", 
						"Message", JOptionPane.INFORMATION_MESSAGE);
				TranslationHelperPanel.closeProjectDetailsTab(config);
			} catch (Exception e3) {
				JOptionPane.showMessageDialog(this,
						e3.getMessage(),
						"Error",
						JOptionPane.ERROR_MESSAGE);
				e3.printStackTrace();
			}
		}
	}

	private void button3ActionPerformed(ActionEvent e) {
		// Create one click partition
		partitionName = JOptionPane.showInputDialog(null, "Enter One-click Partition Name : ", 
				"", 1);
		if (partitionName != null) {
			try {
				if (workSet.getSourceRefset() == null) {
					throw new Exception("Source refset field empty or not saved. Save a valid source refset before Sync.");
				}
				config.getChildrenExpandedNodes().clear();
				pBarW.setMinimum(0);
				pBarW.setMaximum(100);
				pBarW.setIndeterminate(true);
				pBarW.setVisible(true);
				pBarW.repaint();
				pBarW.revalidate();
				panel7.repaint();
				panel7.revalidate();
				repaint();
				revalidate();
				SwingUtilities.invokeLater(new Runnable(){
					public void run(){

						Thread appThr=new Thread(){
							public void run(){
								try {
									if(TerminologyProjectDAO.createNewPartitionAndMembersFromWorkSet(partitionName, workSet, config) != null){
										Terms.get().commit();
										updateList5Content();
										pBarW.setVisible(false);
										JOptionPane.showMessageDialog(WorkSetDetailsPanel.this,
												"One click partition created!", 
												"Message", JOptionPane.INFORMATION_MESSAGE);

										SwingUtilities.invokeLater(new Runnable(){
											public void run(){
												TranslationHelperPanel.refreshProjectPanelNode(config);
											}
										});
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						};
						appThr.start();
					}
				});
			} catch (Exception e3) {
				e3.printStackTrace();
				JOptionPane.showMessageDialog(this,
						e3.getMessage(),
						"Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void button2ActionPerformed(ActionEvent e) {
		// Create partition scheme
		String partitionSchemeName = JOptionPane.showInputDialog(null, "Enter Partition Scheme Name : ", 
				"", 1);
		if (partitionSchemeName != null) {
			try {

				if (e.getSource().equals(button9)){
					pBarP.setVisible(true);

				}
				else{
					pBarW.setVisible(true);
				}
				if(TerminologyProjectDAO.createNewPartitionScheme(partitionSchemeName,workSet.getUids().iterator().next(), config) != null){
					Terms.get().commit();
					updateList5Content();
					if (e.getSource().equals(button9)){
						pBarP.setVisible(false);

					}
					else{
						pBarW.setVisible(false);
					}
					JOptionPane.showMessageDialog(this,
							"Partition Scheme created!", 
							"Message", JOptionPane.INFORMATION_MESSAGE);
					TranslationHelperPanel.refreshProjectPanelNode(config);
				}
			} catch (Exception e3) {
				JOptionPane.showMessageDialog(this,
						e3.getMessage(),
						"Error",
						JOptionPane.ERROR_MESSAGE);
				e3.printStackTrace();
			}
		}
	}

	private void button8ActionPerformed(ActionEvent e) {
		// update source refset list
		pBarS.setVisible(true);
		updateList1Content();
		pBarS.setVisible(false);
	}

	class PopUpList3 extends JPopupMenu implements ActionListener {
		private static final long serialVersionUID = 1L;
		JMenuItem anItem;
		public PopUpList3(){
			anItem = new JMenuItem("Remove selected items");
			anItem.addActionListener(this);
			add(anItem);
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			removeSelectedList3Items();
		}
	}

	private void doList3Pop(MouseEvent e){
		PopUpList3 menu = new PopUpList3();
		menu.show(e.getComponent(), e.getX(), e.getY());
	}


	private void list3MousePressed(MouseEvent e) {
		if (e.isPopupTrigger())
			doList3Pop(e);
	}

	private void list3MouseReleased(MouseEvent e) {
		if (e.isPopupTrigger())
			doList3Pop(e);
	}

	private void button10ActionPerformed(ActionEvent e) {
		removeSelectedList3Items();
	}

	private void list2ValueChanged(ListSelectionEvent e) {
		pBarS.setVisible(true);
		updateList1Content();
		pBarS.setVisible(false);
	}

	private void label15MouseClicked(MouseEvent e) {
		try {
			HelpApi.openHelpForComponent("WORKSET_DETAILS");
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		jTabbedPane1 = new JTabbedPane();
		panel0 = new JPanel();
		panel1 = new JPanel();
		label1 = new JLabel();
		label11 = new JLabel();
		label15 = new JLabel();
		panel2 = new JPanel();
		panel17 = new JPanel();
		label2 = new JLabel();
		textField1 = new JTextField();
		label3 = new JLabel();
		label6 = new JLabel();
		panel21 = new JPanel();
		panel16 = new JPanel();
		panel7 = new JPanel();
		button2 = new JButton();
		button3 = new JButton();
		button4 = new JButton();
		button7 = new JButton();
		pBarW = new JProgressBar();
		panel4 = new JPanel();
		panel8 = new JPanel();
		panel13 = new JPanel();
		label7 = new JLabel();
		list2 = new JList();
		panel6 = new JPanel();
		label12 = new JLabel();
		sourceRefsetCounter = new JLabel();
		scrollPane1 = new JScrollPane();
		list1 = new JList();
		label4 = new JLabel();
		panel11 = new JPanel();
		button5 = new JButton();
		button8 = new JButton();
		button1 = new JButton();
		pBarS = new JProgressBar();
		panel12 = new JPanel();
		label5 = new JLabel();
		scrollPane3 = new JScrollPane();
		list3 = new JList();
		label8 = new JLabel();
		panel9 = new JPanel();
		button6 = new JButton();
		button10 = new JButton();
		pBarE = new JProgressBar();
		panel19 = new JPanel();
		panel14 = new JPanel();
		panel10 = new JPanel();
		label9 = new JLabel();
		worksetMembersCounter = new JLabel();
		scrollPane4 = new JScrollPane();
		list4 = new JList();
		panel15 = new JPanel();
		label10 = new JLabel();
		panel23 = new JPanel();
		label13 = new JLabel();
		scrollPane5 = new JScrollPane();
		list5 = new JList();
		panel24 = new JPanel();
		label14 = new JLabel();
		panel5 = new JPanel();
		button9 = new JButton();
		pBarP = new JProgressBar();

		//======== this ========
		setBackground(new Color(238, 238, 238));
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

		//======== jTabbedPane1 ========
		{
			jTabbedPane1.setBackground(new Color(238, 238, 238));

			//======== panel0 ========
			{
				panel0.setLayout(new GridBagLayout());
				((GridBagLayout)panel0.getLayout()).columnWidths = new int[] {0, 0};
				((GridBagLayout)panel0.getLayout()).rowHeights = new int[] {0, 0, 0};
				((GridBagLayout)panel0.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
				((GridBagLayout)panel0.getLayout()).rowWeights = new double[] {1.0, 0.0, 1.0E-4};

				//======== panel1 ========
				{
					panel1.setLayout(new GridBagLayout());
					((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
					((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 0, 0};
					((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {1.0, 1.0, 0.0, 1.0E-4};
					((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 1.0, 1.0E-4};

					//---- label1 ----
					label1.setText("WorkSet details");
					label1.setFont(new Font("Lucida Grande", Font.BOLD, 14));
					panel1.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- label11 ----
					label11.setText("<html><body> Enter WorkSet name<br><br>  Press \u2018Save\u2019 for persisting changes<br><br>  Press \u2018Retire WorkSet\u2019 to retire this WorkSet. Workset must have no partitions, or retiring will not succeed<br><br>  Press \u2018New partition scheme\u2019  to start a new partition process for this workset<br><br>  Press \u2018Create one click partition\u2019  to create a new partition scheme, containing all the members of this workset, in just one step</html>");
					panel1.add(label11, new GridBagConstraints(1, 0, 1, 2, 0.0, 0.0,
						GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
						new Insets(0, 0, 0, 5), 0, 0));

					//---- label15 ----
					label15.setText("text");
					label15.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseClicked(MouseEvent e) {
							label15MouseClicked(e);
						}
					});
					panel1.add(label15, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 0), 0, 0));

					//======== panel2 ========
					{
						panel2.setLayout(new GridBagLayout());
						((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {328, 0};
						((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {0, 0, 0, 24, 0};
						((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
						((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0, 0.0, 1.0E-4};

						//======== panel17 ========
						{
							panel17.setLayout(new GridBagLayout());
							((GridBagLayout)panel17.getLayout()).columnWidths = new int[] {0, 0, 0};
							((GridBagLayout)panel17.getLayout()).rowHeights = new int[] {0, 0, 0};
							((GridBagLayout)panel17.getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
							((GridBagLayout)panel17.getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0E-4};

							//---- label2 ----
							label2.setText("Name:");
							panel17.add(label2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.BOTH,
								new Insets(0, 0, 5, 5), 0, 0));

							//---- textField1 ----
							textField1.addKeyListener(new KeyAdapter() {
								@Override
								public void keyTyped(KeyEvent e) {
									textField1KeyTyped(e);
								}
							});
							panel17.add(textField1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.BOTH,
								new Insets(0, 0, 5, 0), 0, 0));

							//---- label3 ----
							label3.setText("Project:");
							panel17.add(label3, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.BOTH,
								new Insets(0, 0, 0, 5), 0, 0));

							//---- label6 ----
							label6.setText("project name");
							panel17.add(label6, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.BOTH,
								new Insets(0, 0, 0, 0), 0, 0));
						}
						panel2.add(panel17, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 0), 0, 0));

						//======== panel21 ========
						{
							panel21.setLayout(new GridBagLayout());
							((GridBagLayout)panel21.getLayout()).columnWidths = new int[] {0, 0};
							((GridBagLayout)panel21.getLayout()).rowHeights = new int[] {0, 0};
							((GridBagLayout)panel21.getLayout()).columnWeights = new double[] {0.0, 1.0E-4};
							((GridBagLayout)panel21.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};
						}
						panel2.add(panel21, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
							GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
							new Insets(0, 0, 0, 0), 0, 0));
					}
					panel1.add(panel2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));

					//======== panel16 ========
					{
						panel16.setLayout(new GridBagLayout());
						((GridBagLayout)panel16.getLayout()).columnWidths = new int[] {248, 0};
						((GridBagLayout)panel16.getLayout()).rowHeights = new int[] {0, 0, 0};
						((GridBagLayout)panel16.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
						((GridBagLayout)panel16.getLayout()).rowWeights = new double[] {1.0, 0.0, 1.0E-4};
					}
					panel1.add(panel16, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
						new Insets(0, 0, 0, 5), 0, 0));
				}
				panel0.add(panel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

				//======== panel7 ========
				{
					panel7.setLayout(new GridBagLayout());
					((GridBagLayout)panel7.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0, 0, 0};
					((GridBagLayout)panel7.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel7.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0E-4};
					((GridBagLayout)panel7.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

					//---- button2 ----
					button2.setText("New partition scheme");
					button2.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
					button2.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							button2ActionPerformed(e);
						}
					});
					panel7.add(button2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));

					//---- button3 ----
					button3.setText("Create one click partition");
					button3.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
					button3.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							button3ActionPerformed(e);
						}
					});
					panel7.add(button3, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));

					//---- button4 ----
					button4.setText("Retire workSet");
					button4.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
					button4.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							button4ActionPerformed(e);
						}
					});
					panel7.add(button4, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));

					//---- button7 ----
					button7.setText("Save");
					button7.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
					button7.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							button7ActionPerformed(e);
						}
					});
					panel7.add(button7, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));

					//---- pBarW ----
					pBarW.setIndeterminate(true);
					panel7.add(pBarW, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));
				}
				panel0.add(panel7, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
			}
			jTabbedPane1.addTab("WorkSet", panel0);


			//======== panel4 ========
			{
				panel4.setLayout(new BorderLayout());

				//======== panel8 ========
				{
					panel8.setBackground(new Color(238, 238, 238));
					panel8.setBorder(new EmptyBorder(8, 5, 5, 5));
					panel8.setLayout(new GridBagLayout());
					((GridBagLayout)panel8.getLayout()).columnWidths = new int[] {0, 230, 0};
					((GridBagLayout)panel8.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0};
					((GridBagLayout)panel8.getLayout()).columnWeights = new double[] {1.0, 0.0, 1.0E-4};
					((GridBagLayout)panel8.getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0, 0.0, 1.0E-4};

					//======== panel13 ========
					{
						panel13.setLayout(new BoxLayout(panel13, BoxLayout.X_AXIS));

						//---- label7 ----
						label7.setText("Source refset:     ");
						panel13.add(label7);
						panel13.add(list2);
					}
					panel8.add(panel13, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//======== panel6 ========
					{
						panel6.setLayout(new GridBagLayout());
						((GridBagLayout)panel6.getLayout()).columnWidths = new int[] {158, 80, 0};
						((GridBagLayout)panel6.getLayout()).rowHeights = new int[] {0, 0};
						((GridBagLayout)panel6.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
						((GridBagLayout)panel6.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

						//---- label12 ----
						label12.setText("Source refset members:");
						panel6.add(label12, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 5), 0, 0));

						//---- sourceRefsetCounter ----
						sourceRefsetCounter.setText("(-)");
						panel6.add(sourceRefsetCounter, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 0), 0, 0));
					}
					panel8.add(panel6, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//======== scrollPane1 ========
					{
						scrollPane1.setViewportView(list1);
					}
					panel8.add(scrollPane1, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 5), 0, 0));

					//---- label4 ----
					label4.setText("<html><body> Drag and drop a refset into the source refset field<br><br>  Press \u2018Save\u2019 for persisting changes<br><br>  The list of members included in the workset can be refreshed upon clicking the 'Sync' button from the Source 'Refset' tab.  </html>");
					panel8.add(label4, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
						GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
						new Insets(0, 0, 5, 0), 0, 0));

					//======== panel11 ========
					{
						panel11.setLayout(new GridBagLayout());
						((GridBagLayout)panel11.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0, 0, 0};
						((GridBagLayout)panel11.getLayout()).rowHeights = new int[] {0, 0};
						((GridBagLayout)panel11.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
						((GridBagLayout)panel11.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

						//---- button5 ----
						button5.setText("Save");
						button5.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
						button5.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								button5ActionPerformed(e);
							}
						});
						panel11.add(button5, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 5), 0, 0));

						//---- button8 ----
						button8.setText("Refresh list");
						button8.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
						button8.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								button8ActionPerformed(e);
							}
						});
						panel11.add(button8, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 5), 0, 0));

						//---- button1 ----
						button1.setText("Sync");
						button1.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
						button1.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								button1ActionPerformed(e);
							}
						});
						panel11.add(button1, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 5), 0, 0));
					}
					panel8.add(panel11, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));

					//---- pBarS ----
					pBarS.setIndeterminate(true);
					panel8.add(pBarS, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				panel4.add(panel8, BorderLayout.CENTER);
			}
			jTabbedPane1.addTab("Source Refset", panel4);


			//======== panel12 ========
			{
				panel12.setLayout(new GridBagLayout());
				((GridBagLayout)panel12.getLayout()).columnWidths = new int[] {402, 221, 0};
				((GridBagLayout)panel12.getLayout()).rowHeights = new int[] {0, 0, 0, 0};
				((GridBagLayout)panel12.getLayout()).columnWeights = new double[] {1.0, 1.0, 1.0E-4};
				((GridBagLayout)panel12.getLayout()).rowWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};

				//---- label5 ----
				label5.setText("Exclusion refsets");
				panel12.add(label5, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//======== scrollPane3 ========
				{

					//---- list3 ----
					list3.addKeyListener(new KeyAdapter() {
						@Override
						public void keyTyped(KeyEvent e) {
							list3KeyTyped(e);
						}
					});
					list3.addMouseListener(new MouseAdapter() {
						@Override
						public void mousePressed(MouseEvent e) {
							list3MousePressed(e);
						}
						@Override
						public void mouseReleased(MouseEvent e) {
							list3MouseReleased(e);
						}
					});
					scrollPane3.setViewportView(list3);
				}
				panel12.add(scrollPane3, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//---- label8 ----
				label8.setText("<html><body>\nDrag and drop a refset to be excluded from the workset into the Exclusion refset field. More than one may be chosen<br><br>\n\nPress \u2018Save\u2019 for persisting changes<br><br>\n\nSelect an exclusion refset and type \u2018d\u2019 for removing it\n</html>");
				panel12.add(label8, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
					new Insets(0, 0, 5, 0), 0, 0));

				//======== panel9 ========
				{
					panel9.setLayout(new GridBagLayout());
					((GridBagLayout)panel9.getLayout()).columnWidths = new int[] {0, 0, 88, 0};
					((GridBagLayout)panel9.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel9.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
					((GridBagLayout)panel9.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

					//---- button6 ----
					button6.setText("Save");
					button6.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
					button6.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							button6ActionPerformed(e);
						}
					});
					panel9.add(button6, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));

					//---- button10 ----
					button10.setText("Remove selected refsets");
					button10.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
					button10.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							button10ActionPerformed(e);
						}
					});
					panel9.add(button10, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));

					//---- pBarE ----
					pBarE.setIndeterminate(true);
					panel9.add(pBarE, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				panel12.add(panel9, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));

				//======== panel19 ========
				{
					panel19.setLayout(new GridBagLayout());
					((GridBagLayout)panel19.getLayout()).columnWidths = new int[] {0, 0};
					((GridBagLayout)panel19.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel19.getLayout()).columnWeights = new double[] {0.0, 1.0E-4};
					((GridBagLayout)panel19.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};
				}
				panel12.add(panel19, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
					GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
					new Insets(0, 0, 0, 0), 0, 0));
			}
			jTabbedPane1.addTab("Exclusion Refsets", panel12);


			//======== panel14 ========
			{
				panel14.setLayout(new GridBagLayout());
				((GridBagLayout)panel14.getLayout()).columnWidths = new int[] {372, 206, 0};
				((GridBagLayout)panel14.getLayout()).rowHeights = new int[] {0, 0, 0};
				((GridBagLayout)panel14.getLayout()).columnWeights = new double[] {1.0, 1.0, 1.0E-4};
				((GridBagLayout)panel14.getLayout()).rowWeights = new double[] {0.0, 1.0, 1.0E-4};

				//======== panel10 ========
				{
					panel10.setLayout(new GridBagLayout());
					((GridBagLayout)panel10.getLayout()).columnWidths = new int[] {137, 0, 0};
					((GridBagLayout)panel10.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel10.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
					((GridBagLayout)panel10.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

					//---- label9 ----
					label9.setText("WorkSet members");
					panel10.add(label9, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 5), 0, 0));

					//---- worksetMembersCounter ----
					worksetMembersCounter.setText("(-)");
					panel10.add(worksetMembersCounter, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				panel14.add(panel10, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//======== scrollPane4 ========
				{
					scrollPane4.setViewportView(list4);
				}
				panel14.add(scrollPane4, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));

				//======== panel15 ========
				{
					panel15.setLayout(new GridBagLayout());
					((GridBagLayout)panel15.getLayout()).columnWidths = new int[] {0, 0};
					((GridBagLayout)panel15.getLayout()).rowHeights = new int[] {0, 0};
					((GridBagLayout)panel15.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
					((GridBagLayout)panel15.getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

					//---- label10 ----
					label10.setText("<html><body>\nThe list of workset members is displayed upon clicking \u2018Sync\u2019 button\n</html>");
					panel15.add(label10, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				panel14.add(panel15, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
			}
			jTabbedPane1.addTab("Members", panel14);


			//======== panel23 ========
			{
				panel23.setLayout(new GridBagLayout());
				((GridBagLayout)panel23.getLayout()).columnWidths = new int[] {470, 0, 0};
				((GridBagLayout)panel23.getLayout()).rowHeights = new int[] {0, 0, 0};
				((GridBagLayout)panel23.getLayout()).columnWeights = new double[] {1.0, 1.0, 1.0E-4};
				((GridBagLayout)panel23.getLayout()).rowWeights = new double[] {0.0, 1.0, 1.0E-4};

				//---- label13 ----
				label13.setText("Partition Schemes");
				panel23.add(label13, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 5), 0, 0));

				//======== scrollPane5 ========
				{
					scrollPane5.setViewportView(list5);
				}
				panel23.add(scrollPane5, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));

				//======== panel24 ========
				{
					panel24.setLayout(new GridBagLayout());
					((GridBagLayout)panel24.getLayout()).columnWidths = new int[] {222, 0};
					((GridBagLayout)panel24.getLayout()).rowHeights = new int[] {0, 0, 0};
					((GridBagLayout)panel24.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
					((GridBagLayout)panel24.getLayout()).rowWeights = new double[] {1.0, 0.0, 1.0E-4};

					//---- label14 ----
					label14.setText("<html><body>\nThe list of partition schemes will be displayed as new partitions schemes are created<br><br>\n\nCreate a new partition scheme by clicking the \u2018Add partition scheme\u2019 button\n</html>");
					panel24.add(label14, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
						new Insets(0, 0, 5, 0), 0, 0));

					//======== panel5 ========
					{
						panel5.setLayout(new GridBagLayout());
						((GridBagLayout)panel5.getLayout()).columnWidths = new int[] {0, 0, 0};
						((GridBagLayout)panel5.getLayout()).rowHeights = new int[] {0, 0};
						((GridBagLayout)panel5.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
						((GridBagLayout)panel5.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

						//---- button9 ----
						button9.setText("Add partition scheme");
						button9.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
						button9.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								button2ActionPerformed(e);
							}
						});
						panel5.add(button9, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 5), 0, 0));

						//---- pBarP ----
						pBarP.setIndeterminate(true);
						panel5.add(pBarP, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 0), 0, 0));
					}
					panel24.add(panel5, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				panel23.add(panel24, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
			}
			jTabbedPane1.addTab("Partition Schemes", panel23);

		}
		add(jTabbedPane1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 0), 0, 0));
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JTabbedPane jTabbedPane1;
	private JPanel panel0;
	private JPanel panel1;
	private JLabel label1;
	private JLabel label11;
	private JLabel label15;
	private JPanel panel2;
	private JPanel panel17;
	private JLabel label2;
	private JTextField textField1;
	private JLabel label3;
	private JLabel label6;
	private JPanel panel21;
	private JPanel panel16;
	private JPanel panel7;
	private JButton button2;
	private JButton button3;
	private JButton button4;
	private JButton button7;
	private JProgressBar pBarW;
	private JPanel panel4;
	private JPanel panel8;
	private JPanel panel13;
	private JLabel label7;
	private JList list2;
	private JPanel panel6;
	private JLabel label12;
	private JLabel sourceRefsetCounter;
	private JScrollPane scrollPane1;
	private JList list1;
	private JLabel label4;
	private JPanel panel11;
	private JButton button5;
	private JButton button8;
	private JButton button1;
	private JProgressBar pBarS;
	private JPanel panel12;
	private JLabel label5;
	private JScrollPane scrollPane3;
	private JList list3;
	private JLabel label8;
	private JPanel panel9;
	private JButton button6;
	private JButton button10;
	private JProgressBar pBarE;
	private JPanel panel19;
	private JPanel panel14;
	private JPanel panel10;
	private JLabel label9;
	private JLabel worksetMembersCounter;
	private JScrollPane scrollPane4;
	private JList list4;
	private JPanel panel15;
	private JLabel label10;
	private JPanel panel23;
	private JLabel label13;
	private JScrollPane scrollPane5;
	private JList list5;
	private JPanel panel24;
	private JLabel label14;
	private JPanel panel5;
	private JButton button9;
	private JProgressBar pBarP;
	// JFormDesigner - End of variables declaration  //GEN-END:variables
}
