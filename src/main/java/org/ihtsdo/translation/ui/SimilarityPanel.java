/*
 * Created by JFormDesigner on Tue Oct 19 20:35:00 GMT-03:00 2010
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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
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
public class SimilarityPanel extends JPanel {
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

	public SimilarityPanel() {
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
		label5.setIcon(IconUtilities.helpIcon);
		label5.setText("");
		label6.setIcon(IconUtilities.helpIcon);
		label6.setText("");

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
		table2.setModel(new DefaultTableModel());
	}

	public void updateTabs(String sourceFSN, I_GetConceptData concept, List<Integer> sourceIds, int targetId, TranslationProject translationProject, WorkListMember worklistMember) {
		ConfigTranslationModule confTrans = LanguageUtil.getDefaultTranslationConfig(translationProject);

		DefaultSimilaritySearchOption defSimSearch = confTrans.getDefaultSimilaritySearchOption();

		if (defSimSearch == null) {
			defSimSearch = DefaultSimilaritySearchOption.FSN;
		}
		switch (defSimSearch) {
		case FSN:
			rbFSN.setSelected(true);
			break;
		case PREFERRED:
			rbPref.setSelected(true);
			break;
		case BOTH:
			radioButton2.setSelected(true);
			break;
		default:
			System.out.println("none of the others");
			break;
		}

		clearLingGuidelines();
		clearTransMemory();
		clearSimilarities();
		this.project = translationProject;
		this.worklistMember = worklistMember;
		this.sourceFSN = sourceFSN;
		this.concept = concept;
		this.sourceIds = sourceIds;
		this.targetId = targetId;
		updateSimilarityTable(sourceFSN);
		updateTransMemoryTable(sourceFSN);
		updateGlossaryEnforcement();
	}

	/**
	 * Update glossary enforcement.
	 * 
	 * @param query
	 *            the query
	 */
	private void updateGlossaryEnforcement() {

		try {
			String results = LanguageUtil.getLinguisticGuidelines(concept);
			setLingGuidelinesHitsCount(results.split("<br><br>").length -1);
			// if (!results.isEmpty()){
			// tabbedPane1.setTitleAt(2,
			// "<html>Linguistic Guidelines<b><font color='red'>*</font></b></html>");
			// }
			// else{
			// tabbedPane1.setTitleAt(2, "<html>Linguistic Guidelines</html>");
			// }
			editorPane1.setText(results);
			editorPane1.revalidate();
			button3Clicked();
			button3Clicked();
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Update trans memory table.
	 * 
	 * @param query
	 *            the query
	 */
	private void updateTransMemoryTable(String query) {
		// TODO fix language parameters

		HashMap<String, String> results = DocumentManager.matchTranslationMemory(query);
		setTransMemoryHitsCount(results.entrySet().size());
		String[] columnNames = { "Pattern Text", "Translated to.." };
		String[][] data = null;
		DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int x, int y) {
				return false;
			}
		};

		if (results.isEmpty()) {
			tableModel.addRow(new String[] { "No results found", "No results found" });
		} else {
			for (String key : results.keySet()) {
				tableModel.addRow(new String[] { key, results.get(key) });
			}
		}
		table2.setModel(tableModel);
		similarityTable.setCellSelectionEnabled(true);
		similarityTable.setColumnSelectionAllowed(true);
		similarityTable.setRowSelectionAllowed(true);
		TableColumnModel cmodel = table2.getColumnModel();
		TextAreaRenderer textAreaRenderer = new TextAreaRenderer();
		cmodel.getColumn(0).setCellRenderer(textAreaRenderer);
		cmodel.getColumn(1).setCellRenderer(textAreaRenderer);
		table2.revalidate();
	}

	/**
	 * Update similarity table.
	 * 
	 * @param query
	 *            the query
	 */
	private void updateSimilarityTable(String query) {
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
		similarityTable.setCellSelectionEnabled(true);
		similarityTable.setColumnSelectionAllowed(true);
		similarityTable.setRowSelectionAllowed(true);
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

//		TableColumnModel cmodel = similarityTable.getColumnModel();
//		TextAreaRenderer textAreaRenderer = new TextAreaRenderer();
//		cmodel.getColumn(0).setCellRenderer(textAreaRenderer);
//		cmodel.getColumn(1).setCellRenderer(textAreaRenderer);

		similarityTable.revalidate();
	}

	private void searchButtonActionPreformed(ActionEvent e) {
		String query = searchTextField.getText();
		if (!query.trim().equals("")) {
			updateSimilarityTable(query);
		}
	}

	private void rbFSNActionPerformed(ActionEvent e) {
		updateSimilarityTable(sourceFSN);
		searchTextField.setText(sourceFSN);
	}

	private void rbPrefActionPerformed(ActionEvent e) {
		updateSimilarityTable(sourceFSN);
		searchTextField.setText(sourceFSN);
	}

	private void radioButton2ActionPerformed(ActionEvent e) {
		updateSimilarityTable(sourceFSN);
		searchTextField.setText(sourceFSN);
	}

	private void expandButtonActionPerformed(ActionEvent e) {
		expandButton.setVisible(false);
		similarityDialog = new JDialog();
		similarityDialog.setContentPane(similarityPanel);
		similarityDialog.setModal(true);
		similarityDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		similarityDialog.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				expandButton.setVisible(true);
				similarityDialog.dispose();
				add(similarityPanel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));
			}
		});
		similarityDialog.setSize(new Dimension(700, 550));
		Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension ventana = similarityDialog.getSize();
		similarityDialog.setLocation((pantalla.width - ventana.width) / 2, (pantalla.height - ventana.height) / 2);

		similarityDialog.setVisible(true);
		this.revalidate();
		this.repaint();
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
		similarityTable.setCellSelectionEnabled(true);
		similarityTable.setColumnSelectionAllowed(true);
		similarityTable.setRowSelectionAllowed(true);
		similarityTable.setModel(tableModel);
		similarityTable.revalidate();
	}

	private void clearLingGuidelines() {
		editorPane1.setText("");
		editorPane1.revalidate();
	}

	private void clearTransMemory() {
		String[] columnNames = { "Pattern Text", "Translated to.." };
		String[][] data = null;
		DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int x, int y) {
				return false;
			}
		};

		table2.setModel(tableModel);
		similarityTable.setCellSelectionEnabled(true);
		similarityTable.setColumnSelectionAllowed(true);
		similarityTable.setRowSelectionAllowed(true);
		table2.revalidate();
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

	private void label5MouseClicked(MouseEvent e) {
		try {
			HelpApi.openHelpForComponent("TRANSLATION_MEMORY");
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
	}

	private void label6MouseClicked(MouseEvent e) {
		try {
			HelpApi.openHelpForComponent("EDITORIAL_GUIDELINES");
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
	}

	private void button1ActionPerformed(ActionEvent e) {
		if (button1.getText().equals("Hide")) {
			((GridBagLayout)getLayout()).rowWeights[0] = 0.0;
			button1.setText("Show");
			refinePanel.setVisible(false);
			scrollPane2.setVisible(false);
			panel13.setVisible(false);
		} else {
			((GridBagLayout)getLayout()).rowWeights[0] = 1.0;
			button1.setText("Hide");
			refinePanel.setVisible(true);
			scrollPane2.setVisible(true);
			panel13.setVisible(true);
		}
	}

	private void button2ActionPerformed(ActionEvent e) {
		if (button2.getText().equals("Hide")) {
			((GridBagLayout)getLayout()).rowWeights[1] = 0.0;
			button2.setText("Show");
			scrollPane3.setVisible(false);
		} else {
			((GridBagLayout)getLayout()).rowWeights[1] = 1.0;
			button2.setText("Hide");
			scrollPane3.setVisible(true);
		}
	}

	private void button3ActionPerformed(ActionEvent e) {
		button3Clicked();
	}

	private void button3Clicked() {
		if (button3.getText().equals("Hide")) {
			((GridBagLayout)getLayout()).rowWeights[2] = 0.0;
			button3.setText("Show");
			scrollPane4.setVisible(false);
			
		} else {
			((GridBagLayout)getLayout()).rowWeights[2] = 1.0;
			button3.setText("Hide");
			scrollPane4.setVisible(true);
		}
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		similarityPanel = new JPanel();
		panel2 = new JPanel();
		label1 = new JLabel();
		button1 = new JButton();
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
		expandButton = new JButton();
		panel1 = new JPanel();
		panel3 = new JPanel();
		label2 = new JLabel();
		button2 = new JButton();
		label5 = new JLabel();
		scrollPane3 = new JScrollPane();
		table2 = new ZebraJTable();
		panel15 = new JPanel();
		panel4 = new JPanel();
		label3 = new JLabel();
		button3 = new JButton();
		label6 = new JLabel();
		scrollPane4 = new JScrollPane();
		editorPane1 = new JEditorPane();
		similPopUp = new JPopupMenu();
		copySourceItem = new JMenuItem();
		copyTargetItem = new JMenuItem();

		//======== this ========
		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {30, 30, 25, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {1.0, 1.0, 1.0, 1.0E-4};

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

				//---- button1 ----
				button1.setText("Hide");
				button1.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
				button1.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						button1ActionPerformed(e);
					}
				});
				panel2.add(button1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
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

				//---- expandButton ----
				expandButton.setText("E[x]pand");
				expandButton.setMnemonic('X');
				expandButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						expandButtonActionPerformed(e);
					}
				});
				panel13.add(expandButton, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
					new Insets(0, 0, 0, 0), 0, 0));
			}
			similarityPanel.add(panel13, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(similarityPanel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 0), 0, 0));

		//======== panel1 ========
		{
			panel1.setBorder(new CompoundBorder(
				new EtchedBorder(),
				new EmptyBorder(5, 5, 5, 5)));
			panel1.setLayout(new GridBagLayout());
			((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {0, 0};
			((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {0, 0, 0};
			((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
			((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 1.0, 1.0E-4};

			//======== panel3 ========
			{
				panel3.setLayout(new GridBagLayout());
				((GridBagLayout)panel3.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
				((GridBagLayout)panel3.getLayout()).rowHeights = new int[] {0, 0};
				((GridBagLayout)panel3.getLayout()).columnWeights = new double[] {1.0, 0.0, 0.0, 1.0E-4};
				((GridBagLayout)panel3.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

				//---- label2 ----
				label2.setText("Translation Memory");
				panel3.add(label2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));

				//---- button2 ----
				button2.setText("Hide");
				button2.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
				button2.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						button2ActionPerformed(e);
					}
				});
				panel3.add(button2, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));

				//---- label5 ----
				label5.setText("text");
				label5.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						label5MouseClicked(e);
					}
				});
				panel3.add(label5, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
			}
			panel1.add(panel3, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 0), 0, 0));

			//======== scrollPane3 ========
			{

				//---- table2 ----
				table2.setPreferredScrollableViewportSize(new Dimension(180, 200));
				table2.setFont(new Font("Verdana", Font.PLAIN, 12));
				scrollPane3.setViewportView(table2);
			}
			panel1.add(scrollPane3, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 0), 0, 0));

		//======== panel15 ========
		{
			panel15.setBorder(new CompoundBorder(
				new EtchedBorder(),
				new EmptyBorder(5, 5, 5, 5)));
			panel15.setLayout(new GridBagLayout());
			((GridBagLayout)panel15.getLayout()).columnWidths = new int[] {0, 0};
			((GridBagLayout)panel15.getLayout()).rowHeights = new int[] {0, 0, 0};
			((GridBagLayout)panel15.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
			((GridBagLayout)panel15.getLayout()).rowWeights = new double[] {0.0, 1.0, 1.0E-4};

			//======== panel4 ========
			{
				panel4.setLayout(new GridBagLayout());
				((GridBagLayout)panel4.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
				((GridBagLayout)panel4.getLayout()).rowHeights = new int[] {0, 0};
				((GridBagLayout)panel4.getLayout()).columnWeights = new double[] {1.0, 0.0, 0.0, 1.0E-4};
				((GridBagLayout)panel4.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

				//---- label3 ----
				label3.setText("Editorial Guidelines");
				panel4.add(label3, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));

				//---- button3 ----
				button3.setText("Hide");
				button3.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
				button3.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						button3ActionPerformed(e);
					}
				});
				panel4.add(button3, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 5), 0, 0));

				//---- label6 ----
				label6.setText("text");
				label6.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						label6MouseClicked(e);
					}
				});
				panel4.add(label6, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
			}
			panel15.add(panel4, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 0), 0, 0));

			//======== scrollPane4 ========
			{

				//---- editorPane1 ----
				editorPane1.setContentType("text/html");
				scrollPane4.setViewportView(editorPane1);
			}
			panel15.add(scrollPane4, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel15, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
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
	private JButton button1;
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
	private JButton expandButton;
	private JPanel panel1;
	private JPanel panel3;
	private JLabel label2;
	private JButton button2;
	private JLabel label5;
	private JScrollPane scrollPane3;
	private ZebraJTable table2;
	private JPanel panel15;
	private JPanel panel4;
	private JLabel label3;
	private JButton button3;
	private JLabel label6;
	private JScrollPane scrollPane4;
	private JEditorPane editorPane1;
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
