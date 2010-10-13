/*
 * Created by JFormDesigner on Tue Aug 24 15:52:40 GMT-03:00 2010
 */

package org.ihtsdo.qa.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.RefsetPropertyMap;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.rules.RulesLibrary;
import org.ihtsdo.rules.common.CommonUtils;
import org.ihtsdo.rules.context.RulesDeploymentPackageReference;
import org.ihtsdo.rules.context.RulesDeploymentPackageReferenceHelper;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;

/**
 * @author Guillermo Reynoso
 */
public class DroolsEnumUpdatePanel extends JPanel {

	private static final long serialVersionUID = -8346611923623504647L;
	private DefaultListModel refsetListModel;
	private DefaultListModel droolsEnumListModel;
	public static final String REFSET_VIEWER_NAME = "Drools enumeration update";
	private ObjectTransferHandler conceptDnDHandler;
	private I_GetConceptData refset;
	private I_ConfigAceFrame config;
	private I_TermFactory tf;

	public DroolsEnumUpdatePanel(I_ConfigAceFrame config, I_GetConceptData refset) {
		this.config = config;
		this.refset = refset;
		initComponents();
		initCustomComponents();
	}

	public DroolsEnumUpdatePanel(I_ConfigAceFrame config, I_GetConceptData refset, I_TermFactory tf) {
		this.tf = tf;
		this.config = config;
		this.refset = refset;
		initComponents();
		initCustomComponents();
	}

	private void initCustomComponents() {
		RulesDeploymentPackageReferenceHelper rulesPackageHelper = new RulesDeploymentPackageReferenceHelper(config);
		// rulesPackageHelper.createNewRulesDeploymentPackage("Package reference one",
		// "http://208.109.105.1:8080/drools-guvnor/org.drools.guvnor.Guvnor/package/qa4/qa4Demo");

		droolsEnumListModel = new DefaultListModel();
		droolEnumList.setModel(droolsEnumListModel);

		refsetListModel = new DefaultListModel();
		refsetListModel.addListDataListener(new ListDataListener() {

			@Override
			public void intervalRemoved(ListDataEvent arg0) {
			}

			@Override
			public void intervalAdded(ListDataEvent arg0) {
				droolsEnumListModel.removeAllElements();
				updateRefsetDetailTable();
				refsetList.setToolTipText(refsetListModel.get(0).toString());
			}

			@Override
			public void contentsChanged(ListDataEvent arg0) {
			}
		});

		droolsEnumListModel.addListDataListener(new ListDataListener() {
			@Override
			public void intervalRemoved(ListDataEvent arg0) {
			}

			@Override
			public void intervalAdded(ListDataEvent evt) {
				if (evt.getIndex0() == evt.getIndex1()) {
					try {
						I_GetConceptData member = (I_GetConceptData) droolsEnumListModel.getElementAt(evt.getIndex0());
						I_GetConceptData selectedRefset = null;
						if (!refsetListModel.isEmpty() && ((I_GetConceptData) refsetListModel.getElementAt(0)) != null) {
							selectedRefset = ((I_GetConceptData) refsetListModel.getElementAt(0));
						} else {
							droolsEnumListModel.remove(evt.getIndex0());
							JOptionPane.showMessageDialog(DroolsEnumUpdatePanel.this, "No guvnor property description added.", "Warning", JOptionPane.WARNING_MESSAGE);
						}

						I_IntSet allowedTypes = Terms.get().newIntSet();
						allowedTypes.add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());

						Collection<? extends I_ExtendByRef> extensions = tf.getAllExtensionsForComponent(member.getConceptNid());
						boolean newMember = true;
						for (I_ExtendByRef extension : extensions) {
							if (extension.getRefsetId() == selectedRefset.getConceptNid()) {
								newMember = false;
								//already has the extension
								I_ExtendByRefPart lastPart = CommonUtils.getLastExtensionPart(extension);
								if (CommonUtils.isActive(lastPart.getStatusNid())) {
									// is already active
								} else {
									// is retired
									for (PathBI editPath : config.getEditingPathSet()) {
										I_ExtendByRefPart part = (I_ExtendByRefPart) lastPart.makeAnalog(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid(), editPath.getConceptNid(),
												Long.MAX_VALUE);
										extension.addVersion(part);
										tf.addUncommittedNoChecks(extension);
									}
									tf.addUncommittedNoChecks(selectedRefset);
									tf.addUncommittedNoChecks(member);
									tf.commit();
								} 
							} 
						}

						if (newMember) {
							// is new member
							tf.getRefsetHelper(config).newRefsetExtension(selectedRefset.getConceptNid(), member.getConceptNid(), EConcept.REFSET_TYPES.CID,
									new RefsetPropertyMap().with(RefsetPropertyMap.REFSET_PROPERTY.CID_ONE, member.getConceptNid()), config);
							tf.addUncommittedNoChecks(selectedRefset);
							tf.addUncommittedNoChecks(member);
							tf.commit();
						}


					} catch (TerminologyException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			@Override
			public void contentsChanged(ListDataEvent arg0) {
			}
		});

		try {
			List<RulesDeploymentPackageReference> packages = rulesPackageHelper.getAllRulesDeploymentPackages();
			rulesPackageCombo.addItem(null);
			for (RulesDeploymentPackageReference rulesDeploymentPackageReference : packages) {
				rulesPackageCombo.addItem(rulesDeploymentPackageReference);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		refsetList.setModel(refsetListModel);
		if (refset != null) {
			refsetListModel.addElement(refset);
		}

		refsetList.setName(ObjectTransferHandler.TARGET_LIST_NAME);
		refsetList.setMinimumSize(new Dimension(600, 25));
		refsetList.setPreferredSize(new Dimension(600, 25));
		refsetList.setMaximumSize(new Dimension(600, 25));
		refsetList.setBorder(new BevelBorder(BevelBorder.LOWERED));

		droolEnumList.setMinimumSize(new Dimension(600, 250));
		droolEnumList.setPreferredSize(new Dimension(600, 250));
		droolEnumList.setMaximumSize(new Dimension(600, 250));
		droolEnumList.setBorder(new BevelBorder(BevelBorder.LOWERED));

		conceptDnDHandler = new ObjectTransferHandler(this.config, null);

		refsetList.setTransferHandler(conceptDnDHandler);
		droolEnumList.setTransferHandler(conceptDnDHandler);

	}

	private void updateRefsetDetailTable() {
		if (tf == null) {
			tf = Terms.get();
		}
		try {
			I_GetConceptData selectedRefset = null;
			if (!refsetListModel.isEmpty() && ((I_GetConceptData) refsetListModel.getElementAt(0)) != null) {
				selectedRefset = ((I_GetConceptData) refsetListModel.getElementAt(0));
			}

			ConceptVersionBI refsetBI = Ts.get().getConceptVersion(config.getCoordinate(), selectedRefset.getUids());
			Collection<? extends DescriptionVersionBI> guvnorDescriptionsSize = refsetBI.getDescsActive(ArchitectonicAuxiliary.Concept.GUVNOR_ENUM_PROPERTY_DESC_TYPE.localize().getNid());
			if (guvnorDescriptionsSize.size() < 1 || guvnorDescriptionsSize.size() > 1) {
				JOptionPane.showMessageDialog(this, "Wrong guvnor property descriptions: ", "Warning", JOptionPane.WARNING_MESSAGE);
				refsetListModel.removeAllElements();
			}

			I_IntSet type = tf.newIntSet();
			type.add(RefsetAuxiliary.Concept.REFSET_TYPE_REL.localize().getNid());
			Set<? extends I_GetConceptData> targets = selectedRefset.getSourceRelTargets(type, config.getPrecedence(), config.getConflictResolutionStrategy());
			if (targets.size() > 1) {
				throw new Exception("Multiple targets");
			}

			if (targets.isEmpty()) {
				List<I_GetConceptData> members = new ArrayList<I_GetConceptData>();

				for (I_ExtendByRef member : tf.getRefsetExtensionMembers(selectedRefset.getNid())) {
					if(CommonUtils.isActive(CommonUtils.getLastExtensionPart(member).getStatusNid())){
						members.add(tf.getConcept(member.getComponentNid()));
					}
				}

				Collections.sort(members, new Comparator<I_GetConceptData>() {
					public int compare(I_GetConceptData f1, I_GetConceptData f2) {
						return f1.toString().compareTo(f2.toString());
					}
				});

				for (I_GetConceptData member : members) {
					droolsEnumListModel.addElement(member);
				}
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	private void closeButtonActionPerformed(ActionEvent e) {
		JPanel signpostPanel = config.getSignpostPanel();
		Component[] components = signpostPanel.getComponents();
		for (int i = 0; i < components.length; i++) {
			signpostPanel.remove(components[i]);
		}
		signpostPanel.revalidate();
		signpostPanel.repaint();
	}

	private void exportActionPerformed(ActionEvent e) {
		RulesDeploymentPackageReference package1 = (RulesDeploymentPackageReference) rulesPackageCombo.getSelectedItem();
		I_GetConceptData selectedRefset = null;
		if (!refsetListModel.isEmpty() && ((I_GetConceptData) refsetListModel.getElementAt(0)) != null) {
			selectedRefset = ((I_GetConceptData) refsetListModel.getElementAt(0));
		}
		if (rulesPackageCombo.getSelectedItem() != null && selectedRefset != null) {
			RulesLibrary.updateGuvnorEnumerations(selectedRefset, package1, config);
		} else {
			JOptionPane.showMessageDialog(this, "You must choose a refset and select a Deployment Package. ", "Warning", JOptionPane.WARNING_MESSAGE);
		}
	}

	private void removeButtonActionPerformed(ActionEvent e) {

		I_GetConceptData selectedRefset = null;
		if (!refsetListModel.isEmpty() && ((I_GetConceptData) refsetListModel.getElementAt(0)) != null) {
			selectedRefset = ((I_GetConceptData) refsetListModel.getElementAt(0));
		}

		I_GetConceptData selectedEnum = (I_GetConceptData) droolEnumList.getSelectedValue();
		if (selectedEnum != null) {
			droolsEnumListModel.removeElement(selectedEnum);
		}

		try {
			Collection<? extends I_ExtendByRef> extensions = tf.getAllExtensionsForComponent(selectedEnum.getConceptNid());
			for (I_ExtendByRef extension : extensions) {
				if (extension.getRefsetId() == selectedRefset.getConceptNid()) {
					I_ExtendByRefPart lastPart = CommonUtils.getLastExtensionPart(extension);
					for (PathBI editPath : config.getEditingPathSet()) {
						I_ExtendByRefPart part = (I_ExtendByRefPart) lastPart.makeAnalog(ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid(), editPath.getConceptNid(),
								Long.MAX_VALUE);
						extension.addVersion(part);
					}
					tf.addUncommittedNoChecks(selectedRefset);
					tf.addUncommittedNoChecks(selectedEnum);
					tf.commit();
				}
			}

		} catch (TerminologyException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		actionPanel = new JPanel();
		label1 = new JLabel();
		label2 = new JLabel();
		refsetList = new JList();
		rulesPackageCombo = new JComboBox();
		panel1 = new JPanel();
		export = new JButton();
		closeButton = new JButton();
		panel2 = new JPanel();
		label3 = new JLabel();
		scrollPane2 = new JScrollPane();
		droolEnumList = new JList();
		panel3 = new JPanel();
		removeButton = new JButton();

		//======== this ========
		setBorder(new EmptyBorder(10, 5, 5, 5));
		setLayout(new BorderLayout(5, 5));

		//======== actionPanel ========
		{
			actionPanel.setLayout(new GridBagLayout());
			((GridBagLayout)actionPanel.getLayout()).columnWidths = new int[] {246, 0, 0};
			((GridBagLayout)actionPanel.getLayout()).rowHeights = new int[] {0, 29, 0};
			((GridBagLayout)actionPanel.getLayout()).columnWeights = new double[] {1.0, 0.0, 1.0E-4};
			((GridBagLayout)actionPanel.getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0E-4};

			//---- label1 ----
			label1.setText("Guvnor property description");
			actionPanel.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 3, 3), 0, 0));

			//---- label2 ----
			label2.setText("Deployment package");
			actionPanel.add(label2, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 3, 0), 0, 0));

			//---- refsetList ----
			refsetList.setVisibleRowCount(1);
			refsetList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			actionPanel.add(refsetList, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(0, 0, 0, 3), 0, 0));
			actionPanel.add(rulesPackageCombo, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(actionPanel, BorderLayout.NORTH);

		//======== panel1 ========
		{
			panel1.setBorder(new MatteBorder(1, 0, 0, 0, Color.gray));
			panel1.setLayout(new FlowLayout(FlowLayout.RIGHT, 3, 3));

			//---- export ----
			export.setText("Export to Drools");
			export.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					exportActionPerformed(e);
				}
			});
			panel1.add(export);

			//---- closeButton ----
			closeButton.setText("close");
			closeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					closeButtonActionPerformed(e);
				}
			});
			panel1.add(closeButton);
		}
		add(panel1, BorderLayout.SOUTH);

		//======== panel2 ========
		{
			panel2.setLayout(new GridBagLayout());
			((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {0, 0};
			((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {0, 0, 0, 0};
			((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
			((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};

			//---- label3 ----
			label3.setText("Enumerations");
			panel2.add(label3, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 3, 0), 0, 0));

			//======== scrollPane2 ========
			{
				scrollPane2.setViewportView(droolEnumList);
			}
			panel2.add(scrollPane2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 3, 0), 0, 0));

			//======== panel3 ========
			{
				panel3.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

				//---- removeButton ----
				removeButton.setText("Remove");
				removeButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						removeButtonActionPerformed(e);
					}
				});
				panel3.add(removeButton);
			}
			panel2.add(panel3, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel2, BorderLayout.CENTER);
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	private JPanel actionPanel;
	private JLabel label1;
	private JLabel label2;
	private JList refsetList;
	private JComboBox rulesPackageCombo;
	private JPanel panel1;
	private JButton export;
	private JButton closeButton;
	private JPanel panel2;
	private JLabel label3;
	private JScrollPane scrollPane2;
	private JList droolEnumList;
	private JPanel panel3;
	private JButton removeButton;
	// JFormDesigner - End of variables declaration //GEN-END:variables
}
