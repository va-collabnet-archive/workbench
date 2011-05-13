/*
 * Created by JFormDesigner on Tue Aug 24 15:52:40 GMT-03:00 2010
 */

package org.ihtsdo.project.panel;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.*;
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
import org.dwfa.bpa.process.Condition;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.ihtsdo.project.help.HelpApi;
import org.ihtsdo.project.panel.details.ProjectDetailsPanel;
import org.ihtsdo.project.panel.dnd.ObjectTransferHandler;
import org.ihtsdo.project.util.IconUtilities;

/**
 * @author Guillermo Reynoso
 */
public class RefsetViewerPanel extends JPanel {

	private static final long serialVersionUID = -8346611923623504647L;
	private DefaultListModel refsetListModel;
	private ObjectTransferHandler conceptDnDHandler;
	private I_GetConceptData refset;
	private I_ConfigAceFrame config;
	private DefaultTableModel refsetTableModel;

	public RefsetViewerPanel(I_ConfigAceFrame config) {
		initComponents();
		initCustomComponents();
		label2.setIcon(IconUtilities.helpIcon);
		label2.setText("");

		this.config = config;
	}

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
					if (member.getTuples(null, config.getViewPositionSetReadOnly(), config.getPrecedence(), 
							config.getConflictResolutionStrategy()).iterator().next().getStatusNid() == 
						tf.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids())) {
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
						refsetTableModel.addRow(new Object[] { tf.getConcept(lastTuple.getComponentId()), tf.getConcept(cidPart.getC1id()), cidPart.getStringValue() });
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
						refsetTableModel.addRow(new Object[] { tf.getConcept(lastTuple.getComponentId()), tf.getConcept(cidPart.getStringValue()) });
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
			e.printStackTrace();
		}
	}

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

	private void label2MouseClicked(MouseEvent e) {
		try {
			HelpApi.openHelpForComponent("REFSET_VIEWER");
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
	}

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
	private JScrollPane scrollPane2;
	private JTable refsetTable;
	private JPanel actionPanel;
	private JLabel label1;
	private JLabel label2;
	private JPanel panel1;
	private JList refsetList;
	private JLabel cuantityLable;
	private JButton closeButton;
	private JProgressBar progressBar;
	// JFormDesigner - End of variables declaration //GEN-END:variables
}
