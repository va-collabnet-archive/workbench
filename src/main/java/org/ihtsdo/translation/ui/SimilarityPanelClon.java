/*
 * Created by JFormDesigner on Tue Oct 19 20:35:00 GMT-03:00 2010
 */

package org.ihtsdo.translation.ui;

import java.awt.Color;
import java.awt.Container;
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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.document.DocumentManager;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.help.HelpApi;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.model.WorkListMember;
import org.ihtsdo.project.refset.LanguageMembershipRefset;
import org.ihtsdo.project.util.IconUtilities;
import org.ihtsdo.translation.LanguageUtil;
import org.ihtsdo.translation.SimilarityMatchedItem;
import org.ihtsdo.translation.ui.ConfigTranslationModule.DefaultSimilaritySearchOption;

/**
 * @author Guillermo Reynoso
 */
public class SimilarityPanelClon extends JPanel implements Serializable{
	private static final long serialVersionUID = -1458647596111502234L;
	private String sourceFSN;
	private I_GetConceptData fsn;
	private I_GetConceptData preferred;
	private List<Integer> sourceIds;
	private int targetId;
	private I_GetConceptData concept;
	private I_ConfigAceFrame config;
	private CustomTableColumnModel columnModel;
	private TranslationProject project;
	private WorkListMember worklistMember;
	private JDialog similarityDialog;
	private int similarityHitsCount = 0;
	private int transMemoryHitsCount = 0;
	private int lingGuidelinesHitsCount = 0;

	public SimilarityPanelClon() {
		initComponents();
		try {
			this.config = Terms.get().getActiveAceFrameConfig();
			fsn = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids());
			preferred = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids());
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
		this.concept = concept;
		this.sourceIds = sourceIds;
		this.targetId = targetId;
	}

	/**
	 * Update similarity table.
	 * 
	 * @param query
	 *            the query
	 */
	private void updateSimilarityTable(final String query) {
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
		
				List<SimilarityMatchedItem> results = LanguageUtil.getSimilarityResults(query, sourceIds, targetId, types);
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
			}
		});

		similarityTable.revalidate();
	}

	private void searchButtonActionPreformed(ActionEvent e) {
		String query = searchTextField.getText();
		if (!query.trim().equals("")) {
			updateSimilarityTable(query);
		}
	}

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
	 * of the worklist member
	 * 
	 * @param e
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
	
	private void copySourceItemActionPerformed(ActionEvent e) {
		int selectedRow = similarityTable.getSelectedRow();
		if (selectedRow >= 0) {
			String target = similarityTable.getValueAt(selectedRow, 0).toString();
			StringSelection strSel = new StringSelection(target);
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(strSel, strSel);
		}
	}

	private void copyTargetItemActionPerformed(ActionEvent e) {
		int selectedRow = similarityTable.getSelectedRow();
		if (selectedRow >= 0) {
			String target = similarityTable.getValueAt(selectedRow, 1).toString();
			StringSelection strSel = new StringSelection(target);
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(strSel, strSel);
		}
	}
	private void label4MouseClicked(MouseEvent e) {
		try {
			HelpApi.openHelpForComponent("SIMILARITY");
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
	}

	public void hideButtonsPanels(boolean fsn, boolean prefered, boolean both, boolean refine){
		rbFSN.setSelected(fsn);
		rbPref.setSelected(prefered);
		radioButton2.setSelected(both);
		refineCheckBox.setSelected(refine);
		updateSimilarityTable(sourceFSN);
	}

	private void rbFSNStateChanged(ChangeEvent e) {
		if(e.getSource() instanceof JRadioButton && ((JRadioButton)e.getSource()).isSelected()){
			updateSimilarityTable(sourceFSN);
			searchTextField.setText(sourceFSN);
		}
	}

	private void rbPrefStateChanged(ChangeEvent e) {
		if(e.getSource() instanceof JRadioButton && ((JRadioButton)e.getSource()).isSelected()){
			updateSimilarityTable(sourceFSN);
			searchTextField.setText(sourceFSN);
		}
	}

	private void radioButton2StateChanged(ChangeEvent e) {
		if(e.getSource() instanceof JRadioButton && ((JRadioButton)e.getSource()).isSelected()){
			updateSimilarityTable(sourceFSN);
			searchTextField.setText(sourceFSN);
		}
	}
	
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
				((GridBagLayout)panel13.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0};
				((GridBagLayout)panel13.getLayout()).rowHeights = new int[] {0, 0};
				((GridBagLayout)panel13.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0E-4};
				((GridBagLayout)panel13.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

				//---- rbFSN ----
				rbFSN.setText("FSN");
				rbFSN.setSelected(true);
				rbFSN.setBackground(new Color(238, 238, 238));
				rbFSN.addChangeListener(new ChangeListener() {
					@Override
					public void stateChanged(ChangeEvent e) {
						rbFSNStateChanged(e);
					}
				});
				panel13.add(rbFSN, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));

				//---- rbPref ----
				rbPref.setText("Preferred");
				rbPref.setBackground(new Color(238, 238, 238));
				rbPref.addChangeListener(new ChangeListener() {
					@Override
					public void stateChanged(ChangeEvent e) {
						rbPrefStateChanged(e);
					}
				});
				panel13.add(rbPref, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));

				//---- radioButton2 ----
				radioButton2.setText("Both");
				radioButton2.setBackground(new Color(238, 238, 238));
				radioButton2.addChangeListener(new ChangeListener() {
					@Override
					public void stateChanged(ChangeEvent e) {
						radioButton2StateChanged(e);
					}
				});
				panel13.add(radioButton2, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));

				//---- refineCheckBox ----
				refineCheckBox.setText("Refine");
				panel13.add(refineCheckBox, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
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
	private JPopupMenu similPopUp;
	private JMenuItem copySourceItem;
	private JMenuItem copyTargetItem;
	// JFormDesigner - End of variables declaration //GEN-END:variables

	public int getSimilarityHitsCount() {
		return similarityHitsCount;
	}

	public void setSimilarityHitsCount(int similarityHitsCount) {
		this.similarityHitsCount = similarityHitsCount;
	}

	public int getTransMemoryHitsCount() {
		return transMemoryHitsCount;
	}

	public void setTransMemoryHitsCount(int transMemoryHitsCount) {
		this.transMemoryHitsCount = transMemoryHitsCount;
	}

	public int getLingGuidelinesHitsCount() {
		return lingGuidelinesHitsCount;
	}

	public void setLingGuidelinesHitsCount(int lingGuidelinesHitsCount) {
		this.lingGuidelinesHitsCount = lingGuidelinesHitsCount;
	}
	
	public ZebraJTable getSimilarityTable() {
		return similarityTable;
	}
	
}
