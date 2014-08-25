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

package org.ihtsdo.project.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.table.DefaultTableModel;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCidString;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartStr;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.ihtsdo.project.help.HelpApi;
import org.ihtsdo.project.util.IconUtilities;
import org.ihtsdo.project.view.details.ProjectDetailsPanel;
import org.ihtsdo.project.view.dnd.ObjectTransferHandler;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;

/**
 * The Class RefsetViewerPanel.
 *
 * @author Guillermo Reynoso
 */
public class RefsetViewerPanel extends JPanel {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -8346611923623504647L;
	
	/** The refset list model. */
	private DefaultListModel refsetListModel;
	
	/** The concept dn d handler. */
	private ObjectTransferHandler conceptDnDHandler;
	
	/** The refset. */
	private I_GetConceptData refset;
	
	/** The config. */
	private I_ConfigAceFrame config;
	
	/** The refset table model. */
	private DefaultTableModel refsetTableModel;

	/**
	 * Instantiates a new refset viewer panel.
	 *
	 * @param config the config
	 */
	public RefsetViewerPanel(I_ConfigAceFrame config) {
		initComponents();
		initCustomComponents();
		label2.setIcon(IconUtilities.helpIcon);
		label2.setText("");

		this.config = config;
	}

	/**
	 * Inits the custom components.
	 */
	private void initCustomComponents() {
		progressBar.setVisible(false);

		refsetListModel = new DefaultListModel();
		refsetListModel.addListDataListener(new ListDataListener() {

			@Override
			public void intervalRemoved(ListDataEvent arg0) {
			}

			@Override
			public void intervalAdded(ListDataEvent arg0) {
				progressBar.setVisible(true);
				updateRefsetDetailTable();
				refsetList.setToolTipText(refsetListModel.get(0).toString());
				progressBar.setVisible(false);
			}

			@Override
			public void contentsChanged(ListDataEvent arg0) {
			}
		});
		refsetList.setModel(refsetListModel);
		if (refset != null) {
			refsetListModel.addElement(refset);
		}
		refsetList.setName(ProjectDetailsPanel.TARGET_LIST_NAME);
		refsetList.setMinimumSize(new Dimension(600, 25));
		refsetList.setPreferredSize(new Dimension(600, 25));
		refsetList.setMaximumSize(new Dimension(600, 25));
		refsetList.setBorder(new BevelBorder(BevelBorder.LOWERED));
		conceptDnDHandler = new ObjectTransferHandler(this.config, null);
		refsetList.setTransferHandler(conceptDnDHandler);

	}

	/**
	 * Update refset detail table.
	 */
	private void updateRefsetDetailTable() {
		I_TermFactory tf = Terms.get();
		try {
			I_GetConceptData selectedRefset = null;
			if (!refsetListModel.isEmpty() && ((I_GetConceptData) refsetListModel.getElementAt(0)) != null) {
				selectedRefset = ((I_GetConceptData) refsetListModel.getElementAt(0));
			}
			I_IntSet type = tf.newIntSet();
			type.add(RefsetAuxiliary.Concept.REFSET_TYPE_REL.localize().getNid());
			Set<? extends I_GetConceptData> targets = selectedRefset.getSourceRelTargets(type, config.getPrecedence(), config.getConflictResolutionStrategy());
			if (targets.size() > 1) {
				throw new Exception("Multiple targets");
			}

			if (targets.isEmpty()) {
				this.refsetTableModel = new DefaultTableModel();
				refsetTableModel.addColumn("Concept");

				refsetTable.setModel(refsetTableModel);

				List<I_GetConceptData> members = new ArrayList<I_GetConceptData>();

				for (I_ExtendByRef member : tf.getRefsetExtensionMembers(selectedRefset.getNid())) {
					int stat=member.getTuples(null, config.getViewPositionSetReadOnly(), config.getPrecedence(), 
							config.getConflictResolutionStrategy()).iterator().next().getStatusNid();
					if (stat == tf.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids())
							|| stat == SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid()) {
						members.add(tf.getConcept(member.getComponentNid()));
					}
					// System.out.println("*-*-* " +
					// tf.getConcept(member.getComponentNid()));
				}

				Collections.sort(members, new Comparator<I_GetConceptData>() {
					public int compare(I_GetConceptData f1, I_GetConceptData f2) {
						return f1.toString().compareTo(f2.toString());
					}
				});

				for (I_GetConceptData member : members) {
					refsetTableModel.addRow(new Object[] { member });
				}
			} else {
				I_GetConceptData target = targets.iterator().next();
				if (target.getConceptNid() == RefsetAuxiliary.Concept.CONCEPT_EXTENSION.localize().getNid()) {
					this.refsetTableModel = new DefaultTableModel();
					refsetTableModel.addColumn("Concept");
					refsetTableModel.addColumn("Cid");
					refsetTable.setModel(refsetTableModel);
					for (I_ExtendByRef member : tf.getRefsetExtensionMembers(selectedRefset.getNid())) {
						List<? extends I_ExtendByRefVersion> tuples = member.getTuples(config.getAllowedStatus(), config.getViewPositionSetReadOnly(), config.getPrecedence(), config
								.getConflictResolutionStrategy());
						if (tuples.size() > 1) {
							throw new Exception("More then one tuples");
						}
						I_ExtendByRefVersion lastTuple = tuples.iterator().next();
						I_ExtendByRefPartCid cidPart = (I_ExtendByRefPartCid) lastTuple.getMutablePart();
						refsetTableModel.addRow(new Object[] { tf.getConcept(lastTuple.getComponentId()), tf.getConcept(cidPart.getC1id()) });
					}
					refsetTable.validate();
				} else if (target.getConceptNid() == RefsetAuxiliary.Concept.CONCEPT_STRING_EXTENSION.localize().getNid()) {
					this.refsetTableModel = new DefaultTableModel();
					refsetTableModel.addColumn("Concept");
					refsetTableModel.addColumn("Cid");
					refsetTableModel.addColumn("Cid string");

					refsetTable.setModel(refsetTableModel);
					for (I_ExtendByRef member : tf.getRefsetExtensionMembers(selectedRefset.getNid())) {
						List<? extends I_ExtendByRefVersion> tuples = member.getTuples(config.getAllowedStatus(), config.getViewPositionSetReadOnly(), config.getPrecedence(), config
								.getConflictResolutionStrategy());
						if (tuples.size() > 1) {
							throw new Exception("More then one tuples");
						}
						I_ExtendByRefVersion lastTuple = tuples.iterator().next();
						I_ExtendByRefPartCidString cidPart = (I_ExtendByRefPartCidString) lastTuple.getMutablePart();
						refsetTableModel.addRow(new Object[] { tf.getConcept(lastTuple.getComponentId()), tf.getConcept(cidPart.getC1id()), cidPart.getString1Value() });
					}
					refsetTable.validate();
				} else if (target.getConceptNid() == RefsetAuxiliary.Concept.STRING_EXTENSION.localize().getNid()) {
					this.refsetTableModel = new DefaultTableModel();
					refsetTableModel.addColumn("Concept");
					refsetTableModel.addColumn("String value");

					refsetTable.setModel(refsetTableModel);
					for (I_ExtendByRef member : tf.getRefsetExtensionMembers(selectedRefset.getNid())) {
						List<? extends I_ExtendByRefVersion> tuples = member.getTuples(config.getAllowedStatus(), config.getViewPositionSetReadOnly(), config.getPrecedence(), config
								.getConflictResolutionStrategy());
						if (tuples.size() > 1) {
							throw new Exception("More then one tuples");
						}
						I_ExtendByRefVersion lastTuple = tuples.iterator().next();
						I_ExtendByRefPartStr cidPart = (I_ExtendByRefPartStr) lastTuple.getMutablePart();
						refsetTableModel.addRow(new Object[] { tf.getConcept(lastTuple.getComponentId()), tf.getConcept(cidPart.getString1Value()) });
					}
					refsetTable.validate();
				} else if (target.getConceptNid() == RefsetAuxiliary.Concept.LANGUAGE_ENUMERATION_EXTENSION.localize().getNid()) {
					this.refsetTableModel = new DefaultTableModel();
					refsetTableModel.addColumn("Concept");
					refsetTableModel.addColumn("Cid");
					refsetTable.setModel(refsetTableModel);
					for (I_ExtendByRef member : tf.getRefsetExtensionMembers(selectedRefset.getNid())) {
						List<? extends I_ExtendByRefVersion> tuples = member.getTuples(config.getAllowedStatus(), config.getViewPositionSetReadOnly(), config.getPrecedence(), config
								.getConflictResolutionStrategy());
						if (tuples.size() > 1) {
							throw new Exception("More then one tuples");
						}
						I_ExtendByRefVersion lastTuple = tuples.iterator().next();
						I_ExtendByRefPartCid cidPart = (I_ExtendByRefPartCid) lastTuple.getMutablePart();
						refsetTableModel.addRow(new Object[] { tf.getConcept(lastTuple.getComponentId()), tf.getConcept(cidPart.getC1id()) });
					}
				}

			}
			cuantityLable.setText("Total number of members: ( " + refsetTableModel.getRowCount() + " )");
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			AceLog.getAppLog().alertAndLogException(e);
		}
	}

	/**
	 * Close button action performed.
	 *
	 * @param e the e
	 */
	private void closeButtonActionPerformed(ActionEvent e) {
		try {
			TranslationHelperPanel thp = PanelHelperFactory.getTranslationHelperPanel();
			JTabbedPane tp = thp.getTabbedPanel();
			if (tp != null) {
				int tabCount = tp.getTabCount();
				for (int i = 0; i < tabCount; i++) {
					if (tp.getTitleAt(i).equals(TranslationHelperPanel.REFSET_VIEWER_NAME)) {
						tp.remove(i);
						tp.revalidate();
						tp.repaint();
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Label2 mouse clicked.
	 *
	 * @param e the e
	 */
	private void label2MouseClicked(MouseEvent e) {
		try {
			HelpApi.openHelpForComponent("REFSET_VIEWER");
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Inits the components.
	 */
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		scrollPane2 = new JScrollPane();
		refsetTable = new JTable();
		actionPanel = new JPanel();
		label1 = new JLabel();
		label2 = new JLabel();
		panel1 = new JPanel();
		refsetList = new JList();
		cuantityLable = new JLabel();
		closeButton = new JButton();
		progressBar = new JProgressBar();

		//======== this ========
		setBorder(new EmptyBorder(10, 5, 5, 5));
		setLayout(new BorderLayout(10, 10));

		//======== scrollPane2 ========
		{
			scrollPane2.setViewportView(refsetTable);
		}
		add(scrollPane2, BorderLayout.CENTER);

		//======== actionPanel ========
		{
			actionPanel.setLayout(new GridBagLayout());
			((GridBagLayout)actionPanel.getLayout()).columnWidths = new int[] {295, 0, 0};
			((GridBagLayout)actionPanel.getLayout()).rowHeights = new int[] {0, 29, 0};
			((GridBagLayout)actionPanel.getLayout()).columnWeights = new double[] {1.0, 0.0, 1.0E-4};
			((GridBagLayout)actionPanel.getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0E-4};

			//---- label1 ----
			label1.setText("Drag and drop a refset here to view the details");
			actionPanel.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.WEST, GridBagConstraints.VERTICAL,
					new Insets(0, 0, 5, 10), 0, 0));

			//---- label2 ----
			label2.setText("text");
			label2.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					label2MouseClicked(e);
				}
			});
			actionPanel.add(label2, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

			//======== panel1 ========
			{
				panel1.setLayout(new GridBagLayout());
				((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {177, 0, 0};
				((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 0};
				((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
				((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

				//---- refsetList ----
				refsetList.setVisibleRowCount(1);
				refsetList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				panel1.add(refsetList, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 10), 0, 0));
				panel1.add(cuantityLable, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
			}
			actionPanel.add(panel1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.WEST, GridBagConstraints.NONE,
					new Insets(0, 0, 0, 10), 0, 0));

			//---- closeButton ----
			closeButton.setText("Close");
			closeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					closeButtonActionPerformed(e);
				}
			});
			actionPanel.add(closeButton, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
					new Insets(0, 0, 0, 0), 0, 0));
		}
		add(actionPanel, BorderLayout.NORTH);

		//---- progressBar ----
		progressBar.setIndeterminate(true);
		add(progressBar, BorderLayout.SOUTH);
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	/** The scroll pane2. */
	private JScrollPane scrollPane2;
	
	/** The refset table. */
	private JTable refsetTable;
	
	/** The action panel. */
	private JPanel actionPanel;
	
	/** The label1. */
	private JLabel label1;
	
	/** The label2. */
	private JLabel label2;
	
	/** The panel1. */
	private JPanel panel1;
	
	/** The refset list. */
	private JList refsetList;
	
	/** The cuantity lable. */
	private JLabel cuantityLable;
	
	/** The close button. */
	private JButton closeButton;
	
	/** The progress bar. */
	private JProgressBar progressBar;
	// JFormDesigner - End of variables declaration //GEN-END:variables
}
