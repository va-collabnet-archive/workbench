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

package org.ihtsdo.translation.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.help.HelpApi;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.model.WorkListMember;
import org.ihtsdo.project.refset.LanguageMembershipRefset;
import org.ihtsdo.project.util.IconUtilities;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.translation.LanguageUtil;
import org.ihtsdo.translation.SimilarityMatchedItem;
import org.ihtsdo.translation.ui.ConfigTranslationModule.DefaultSimilaritySearchOption;

/**
 * The Class SimilarityPanelClon.
 * 
 * @author Guillermo Reynoso
 */
public class SimilarityPanelClon extends JPanel implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -1458647596111502234L;

	/** The source fsn. */
	private String sourceFSN;

	/** The fsn. */
	private I_GetConceptData fsn;

	/** The preferred. */
	private I_GetConceptData preferred;

	/** The source ids. */
	private List<Integer> sourceIds;

	/** The target id. */
	private int targetId;

	/** The config. */
	private I_ConfigAceFrame config;

	/** The column model. */
	private CustomTableColumnModel columnModel;

	/** The project. */
	private TranslationProject project;

	/** The worklist member. */
	private WorkListMember worklistMember;

	/** The similarity dialog. */
	private JDialog similarityDialog;

	/** The similarity hits count. */
	private int similarityHitsCount = 0;

	/** The trans memory hits count. */
	private int transMemoryHitsCount = 0;

	/** The ling guidelines hits count. */
	private int lingGuidelinesHitsCount = 0;

	private String query = "";

	/**
	 * Instantiates a new similarity panel clon.
	 */
	public SimilarityPanelClon() {
		initComponents();
		progressBar1.setVisible(false);
		try {
			this.config = Terms.get().getActiveAceFrameConfig();
			fsn = Terms.get().getConcept(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid());
			preferred = Terms.get().getConcept(SnomedMetadataRf2.PREFERRED_RF2.getLenient().getNid());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}

		label4.setIcon(IconUtilities.helpIcon);
		label4.setText("");

		refineCheckBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				if (!refineCheckBox.isSelected()) {
					refinePanel.setVisible(false);
					refinePanel.validate();
				} else {
					refinePanel.setVisible(true);
					refinePanel.validate();
				}
			}
		});

		searchButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				searchButtonActionPreformed(e);
			}
		});
		similarityTable.setModel(new DefaultTableModel());
		similarityTable.setCellSelectionEnabled(true);
		similarityTable.setColumnSelectionAllowed(true);
		similarityTable.setRowSelectionAllowed(true);
	}

	/**
	 * Update tabs.
	 * 
	 * @param sourceFSN
	 *            the source fsn
	 * @param concept
	 *            the concept
	 * @param sourceIds
	 *            the source ids
	 * @param targetId
	 *            the target id
	 * @param translationProject
	 *            the translation project
	 * @param worklistMember
	 *            the worklist member
	 */
	public void updateTabs(String sourceFSN, I_GetConceptData concept, List<Integer> sourceIds, int targetId, TranslationProject translationProject, WorkListMember worklistMember) {
		ConfigTranslationModule confTrans = LanguageUtil.getDefaultTranslationConfig(translationProject);

		DefaultSimilaritySearchOption defSimSearch = confTrans.getDefaultSimilaritySearchOption();

		if (defSimSearch == null) {
			defSimSearch = DefaultSimilaritySearchOption.FSN;
		}

		clearSimilarities();
		this.project = translationProject;
		this.worklistMember = worklistMember;
		this.sourceFSN = sourceFSN;
		this.sourceIds = sourceIds;
		this.targetId = targetId;
	}

	/**
	 * Update similarity table.
	 * 
	 * @param query
	 *            the query
	 */
	private void updateSimilarityTable(String queryStr) {
		this.query = queryStr;
		progressBar1.setVisible(true);
		progressBar1.revalidate();
		progressBar1.repaint();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				List<Integer> types = new ArrayList<Integer>();
				if (rbFSN.isSelected())
					types.add(fsn.getConceptNid());
				else if (rbPref.isSelected())
					types.add(preferred.getConceptNid());
				else {
					types.add(fsn.getConceptNid());
					types.add(preferred.getConceptNid());
				}

				List<SimilarityMatchedItem> results = LanguageUtil.getSimilarityResults(query, sourceIds, targetId, types, null);
				setSimilarityHitsCount(results.size());
				String[] columnNames;
				columnNames = new String[] { "Source Text", "Target Text", "Status", "Item" };
				String[][] data = null;
				DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
					private static final long serialVersionUID = 1L;

					public boolean isCellEditable(int x, int y) {
						return false;
					}
				};
				similarityTable.setModel(tableModel);
				columnModel = new CustomTableColumnModel();
				similarityTable.setColumnModel(columnModel);
				similarityTable.createDefaultColumnsFromModel();

				TableColumn column = columnModel.getColumnByModelIndex(3);
				columnModel.setColumnVisible(column, false);

				if (results.isEmpty()) {
					tableModel.addRow(new String[] { query, "No matches found" });
				} else {
					for (SimilarityMatchedItem item : results) {
						I_GetConceptData transStatus = null;
						try {
							I_GetConceptData targetLanguage = TerminologyProjectDAO.getTargetLanguageRefsetForProject(project, config);
							LanguageMembershipRefset targetLangRefset = new LanguageMembershipRefset(targetLanguage, config);
							transStatus = targetLangRefset.getPromotionRefset(config).getPromotionStatus(item.getConceptId(), config);
						} catch (Exception e) {
							e.printStackTrace();
						}

						String highlightedSourceText = "<html>" + item.getSourceText().toLowerCase();

						for (String word : query.toLowerCase().split("\\W")) {
							highlightedSourceText = highlightedSourceText.replace(word, "<font style='background-color: yellow;'>" + word + "</font>");
						}

						tableModel.addRow(new Object[] { highlightedSourceText, item.getTargetText(), transStatus, item });
					}
				}
				similarityTable.revalidate();
				progressBar1.setVisible(false);
			}
		});
	}

	/**
	 * Search button action preformed.
	 * 
	 * @param e
	 *            the e
	 */
	private void searchButtonActionPreformed(ActionEvent e) {
		String query = searchTextField.getText();
		if (!query.trim().equals("")) {
			updateSimilarityTable(query);
		}
	}

	/**
	 * Clear similarities.
	 */
	private void clearSimilarities() {
		String[] columnNames = { "Source Text", "Target Text" };
		String[][] data = null;
		DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int x, int y) {
				return false;
			}

		};
		similarityTable.setModel(tableModel);
		similarityTable.revalidate();
	}

	/**
	 * This method shows a worklist member's similarity concept, in the context
	 * of the worklist member.
	 * 
	 * @param e
	 *            the e
	 */
	private void table1MouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			DefaultTableModel tableModel = (DefaultTableModel) similarityTable.getModel();
			int selected = similarityTable.getSelectedRow();
			int columnCount = columnModel.getColumnCount(false);
			if (columnCount == 4) {
				Object val = tableModel.getValueAt(selected, 3);
				if (val instanceof SimilarityMatchedItem) {
					SimilarityMatchedItem item = (SimilarityMatchedItem) val;
					try {
						I_GetConceptData concept = Terms.get().getConcept(item.getConceptId());
						TranslationConceptEditorRO editorRO = new TranslationConceptEditorRO();

						JDialog ro = new JDialog(similarityDialog);
						ro.setContentPane(editorRO);
						ro.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
						ro.setSize(new Dimension(800, 650));
						Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
						Dimension ventana = ro.getSize();
						ro.setLocation((pantalla.width - ventana.width) / 2, (pantalla.height - ventana.height) / 2);
						WorkListMember wlMemberCopy = (WorkListMember) copy(worklistMember);
						wlMemberCopy.setId(item.getConceptId());
						wlMemberCopy.setUids(concept.getUUIDs());
						editorRO.updateUI(project, wlMemberCopy);
						ro.setVisible(true);
					} catch (TerminologyException e1) {
						e1.printStackTrace();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		} else if (e.getButton() == MouseEvent.BUTTON3) {
			int r = similarityTable.rowAtPoint(e.getPoint());
			if (r >= 0 && r < similarityTable.getRowCount()) {
				similarityTable.setRowSelectionInterval(r, r);
			} else {
				similarityTable.clearSelection();
			}

			int rowindex = similarityTable.getSelectedRow();
			if (rowindex < 0) {
				return;
			}
			similPopUp.show(e.getComponent(), e.getX(), e.getY());

		}

	}

	/**
	 * Copy.
	 * 
	 * @param orig
	 *            the orig
	 * @return the object
	 */
	public static Object copy(Object orig) {
		Object obj = null;
		try {
			// Write the object out to a byte array
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bos);
			out.writeObject(orig);
			out.flush();
			out.close();

			// Make an input stream from the byte array and read
			// a copy of the object back in.
			ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
			obj = in.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
		}
		return obj;
	}

	/**
	 * Copy source item action performed.
	 * 
	 * @param e
	 *            the e
	 */
	private void copySourceItemActionPerformed(ActionEvent e) {
		int selectedRow = similarityTable.getSelectedRow();
		if (selectedRow >= 0) {
			String target = similarityTable.getValueAt(selectedRow, 0).toString();
			StringSelection strSel = new StringSelection(target);
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(strSel, strSel);
		}
	}

	/**
	 * Copy target item action performed.
	 * 
	 * @param e
	 *            the e
	 */
	private void copyTargetItemActionPerformed(ActionEvent e) {
		int selectedRow = similarityTable.getSelectedRow();
		if (selectedRow >= 0) {
			String target = similarityTable.getValueAt(selectedRow, 1).toString();
			StringSelection strSel = new StringSelection(target);
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(strSel, strSel);
		}
	}

	/**
	 * Label4 mouse clicked.
	 * 
	 * @param e
	 *            the e
	 */
	private void label4MouseClicked(MouseEvent e) {
		try {
			HelpApi.openHelpForComponent("SIMILARITY");
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Hide buttons panels.
	 * 
	 * @param fsn
	 *            the fsn
	 * @param prefered
	 *            the prefered
	 * @param both
	 *            the both
	 * @param refine
	 *            the refine
	 */
	public void hideButtonsPanels(boolean fsn, boolean prefered, boolean both, boolean refine) {
		rbFSN.setSelected(fsn);
		rbPref.setSelected(prefered);
		radioButton2.setSelected(both);
		refineCheckBox.setSelected(refine);
		updateSimilarityTable(sourceFSN);
	}

	private void rbPrefActionPerformed(ActionEvent e) {
		updateSimilarityTable(sourceFSN);
		searchTextField.setText(sourceFSN);
	}

	private void rbFSNActionPerformed(ActionEvent e) {
		updateSimilarityTable(sourceFSN);
		searchTextField.setText(sourceFSN);
	}

	private void radioButton2ActionPerformed(ActionEvent e) {
		updateSimilarityTable(sourceFSN);
		searchTextField.setText(sourceFSN);
	}

	/**
	 * Inits the components.
	 */
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		similarityPanel = new JPanel();
		panel2 = new JPanel();
		label1 = new JLabel();
		label4 = new JLabel();
		refinePanel = new JPanel();
		searchTextField = new JTextField();
		searchButton = new JButton();
		scrollPane2 = new JScrollPane();
		similarityTable = new ZebraJTable();
		panel13 = new JPanel();
		rbFSN = new JRadioButton();
		rbPref = new JRadioButton();
		radioButton2 = new JRadioButton();
		refineCheckBox = new JCheckBox();
		progressBar1 = new JProgressBar();
		similPopUp = new JPopupMenu();
		copySourceItem = new JMenuItem();
		copyTargetItem = new JMenuItem();

		//======== this ========
		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

		//======== similarityPanel ========
		{
			similarityPanel.setBackground(new Color(238, 238, 238));
			similarityPanel.setBorder(new CompoundBorder(
				new EtchedBorder(),
				new EmptyBorder(5, 5, 5, 5)));
			similarityPanel.setLayout(new GridBagLayout());
			((GridBagLayout)similarityPanel.getLayout()).columnWidths = new int[] {0, 0};
			((GridBagLayout)similarityPanel.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0};
			((GridBagLayout)similarityPanel.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
			((GridBagLayout)similarityPanel.getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0, 0.0, 1.0E-4};

			//======== panel2 ========
			{
				panel2.setLayout(new GridBagLayout());
				((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
				((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {0, 0};
				((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {1.0, 0.0, 0.0, 1.0E-4};
				((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

				//---- label1 ----
				label1.setText("Similarity");
				label1.setHorizontalAlignment(SwingConstants.LEFT);
				panel2.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));

				//---- label4 ----
				label4.setText("text");
				label4.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						label4MouseClicked(e);
					}
				});
				panel2.add(label4, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
			}
			similarityPanel.add(panel2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 0), 0, 0));

			//======== refinePanel ========
			{
				refinePanel.setBackground(new Color(238, 238, 238));
				refinePanel.setLayout(new GridBagLayout());
				((GridBagLayout)refinePanel.getLayout()).columnWidths = new int[] {233, 0, 0};
				((GridBagLayout)refinePanel.getLayout()).rowHeights = new int[] {0, 0};
				((GridBagLayout)refinePanel.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0E-4};
				((GridBagLayout)refinePanel.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};
				refinePanel.setVisible(false);
				refinePanel.add(searchTextField, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));

				//---- searchButton ----
				searchButton.setAction(null);
				searchButton.setText("Sea[r]ch");
				searchButton.setMnemonic('R');
				refinePanel.add(searchButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
			}
			similarityPanel.add(refinePanel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 0), 0, 0));

			//======== scrollPane2 ========
			{

				//---- similarityTable ----
				similarityTable.setPreferredScrollableViewportSize(new Dimension(180, 200));
				similarityTable.setFont(new Font("Verdana", Font.PLAIN, 12));
				similarityTable.setModel(new DefaultTableModel());
				similarityTable.setCellSelectionEnabled(true);
				similarityTable.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						table1MouseClicked(e);
					}
				});
				scrollPane2.setViewportView(similarityTable);
			}
			similarityPanel.add(scrollPane2, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 0), 0, 0));

			//======== panel13 ========
			{
				panel13.setBackground(new Color(238, 238, 238));
				panel13.setLayout(new GridBagLayout());
				((GridBagLayout)panel13.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0, 0};
				((GridBagLayout)panel13.getLayout()).rowHeights = new int[] {0, 0};
				((GridBagLayout)panel13.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0, 1.0E-4};
				((GridBagLayout)panel13.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

				//---- rbFSN ----
				rbFSN.setText("FSN");
				rbFSN.setSelected(true);
				rbFSN.setBackground(new Color(238, 238, 238));
				rbFSN.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						rbFSNActionPerformed(e);
					}
				});
				panel13.add(rbFSN, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));

				//---- rbPref ----
				rbPref.setText("Preferred");
				rbPref.setBackground(new Color(238, 238, 238));
				rbPref.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						rbPrefActionPerformed(e);
					}
				});
				panel13.add(rbPref, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));

				//---- radioButton2 ----
				radioButton2.setText("Both");
				radioButton2.setBackground(new Color(238, 238, 238));
				radioButton2.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						radioButton2ActionPerformed(e);
					}
				});
				panel13.add(radioButton2, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));

				//---- refineCheckBox ----
				refineCheckBox.setText("Refine");
				panel13.add(refineCheckBox, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));
				panel13.add(progressBar1, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
			}
			similarityPanel.add(panel13, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(similarityPanel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 0), 0, 0));

		//======== similPopUp ========
		{

			//---- copySourceItem ----
			copySourceItem.setText("Copy source to clipboard");
			copySourceItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					copySourceItemActionPerformed(e);
				}
			});
			similPopUp.add(copySourceItem);

			//---- copyTargetItem ----
			copyTargetItem.setText("Copy target to clipboard");
			copyTargetItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					copyTargetItemActionPerformed(e);
				}
			});
			similPopUp.add(copyTargetItem);
		}

		//---- buttonGroup2 ----
		ButtonGroup buttonGroup2 = new ButtonGroup();
		buttonGroup2.add(rbFSN);
		buttonGroup2.add(rbPref);
		buttonGroup2.add(radioButton2);
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	private JPanel similarityPanel;
	private JPanel panel2;
	private JLabel label1;
	private JLabel label4;
	private JPanel refinePanel;
	private JTextField searchTextField;
	private JButton searchButton;
	private JScrollPane scrollPane2;
	private ZebraJTable similarityTable;
	private JPanel panel13;
	private JRadioButton rbFSN;
	private JRadioButton rbPref;
	private JRadioButton radioButton2;
	private JCheckBox refineCheckBox;
	private JProgressBar progressBar1;
	private JPopupMenu similPopUp;
	private JMenuItem copySourceItem;
	private JMenuItem copyTargetItem;
	// JFormDesigner - End of variables declaration //GEN-END:variables

	/**
	 * Gets the similarity hits count.
	 * 
	 * @return the similarity hits count
	 */
	public int getSimilarityHitsCount() {
		return similarityHitsCount;
	}

	/**
	 * Sets the similarity hits count.
	 * 
	 * @param similarityHitsCount
	 *            the new similarity hits count
	 */
	public void setSimilarityHitsCount(int similarityHitsCount) {
		this.similarityHitsCount = similarityHitsCount;
	}

	/**
	 * Gets the trans memory hits count.
	 * 
	 * @return the trans memory hits count
	 */
	public int getTransMemoryHitsCount() {
		return transMemoryHitsCount;
	}

	/**
	 * Sets the trans memory hits count.
	 * 
	 * @param transMemoryHitsCount
	 *            the new trans memory hits count
	 */
	public void setTransMemoryHitsCount(int transMemoryHitsCount) {
		this.transMemoryHitsCount = transMemoryHitsCount;
	}

	/**
	 * Gets the ling guidelines hits count.
	 * 
	 * @return the ling guidelines hits count
	 */
	public int getLingGuidelinesHitsCount() {
		return lingGuidelinesHitsCount;
	}

	/**
	 * Sets the ling guidelines hits count.
	 * 
	 * @param lingGuidelinesHitsCount
	 *            the new ling guidelines hits count
	 */
	public void setLingGuidelinesHitsCount(int lingGuidelinesHitsCount) {
		this.lingGuidelinesHitsCount = lingGuidelinesHitsCount;
	}

	/**
	 * Gets the similarity table.
	 * 
	 * @return the similarity table
	 */
	public ZebraJTable getSimilarityTable() {
		return similarityTable;
	}

}
