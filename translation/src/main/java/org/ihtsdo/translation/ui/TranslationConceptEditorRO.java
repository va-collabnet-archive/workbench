/**
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
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.border.LineBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import org.apache.xalan.trace.SelectionEvent;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.document.DocumentManager;
import org.ihtsdo.issue.IssueRepoRegistration;
import org.ihtsdo.issue.issuerepository.IssueRepository;
import org.ihtsdo.issue.manager.IssueRepositoryDAO;
import org.ihtsdo.project.ContextualizedDescription;
import org.ihtsdo.project.I_ContextualizeDescription;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.model.WorkListMember;
import org.ihtsdo.project.refset.Comment;
import org.ihtsdo.project.refset.CommentsRefset;
import org.ihtsdo.project.refset.LanguageMembershipRefset;
import org.ihtsdo.project.util.IconUtilities;
import org.ihtsdo.project.view.issue.IssuesListPanel2;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.translation.LanguageUtil;
import org.ihtsdo.translation.SimilarityMatchedItem;
import org.ihtsdo.translation.TreeEditorObjectWrapper;
import org.ihtsdo.translation.ui.ConfigTranslationModule.TreeComponent;
import org.ihtsdo.translation.ui.translation.CommentPanel;
import org.ihtsdo.translation.ui.translation.NewCommentPanel;

/**
 * The Class TranslationConceptEditor.
 */
public class TranslationConceptEditorRO extends JPanel {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The Constant HEADER_SEPARATOR. */
	private static final String HEADER_SEPARATOR = " // ";
	
	/** The Constant COMMENT_HEADER_SEP. */
	private static final String COMMENT_HEADER_SEP = ": -";
	
	/** The translation project. */
	private TranslationProject translationProject;
	
	/** The synonym. */
	private I_GetConceptData synonym;
	
	/** The fsn. */
	private I_GetConceptData fsn;
	
	/** The preferred. */
	private I_GetConceptData preferred;
	
	/** The source ids. */
	private List<Integer> sourceIds;
	
	/** The target id. */
	private int targetId;
	
	/** The acceptable. */
	private I_GetConceptData acceptable;
	
	/** The source lang refsets. */
	private Set<LanguageMembershipRefset> sourceLangRefsets;
	
	/** The target lang refset. */
	private LanguageMembershipRefset targetLangRefset;
	
	/** The formatter. */
	private SimpleDateFormat formatter;
	
	/** The description. */
	private I_GetConceptData description;
	
	/** The inactive. */
	private I_GetConceptData inactive;
	
	/** The active. */
	private I_GetConceptData active;
	
	/** The issue list panel. */
	private IssuesListPanel2 issueListPanel;
	
	/** The transl config. */
	private ConfigTranslationModule translConfig;
	
	/** The assigned mnemo. */
	private String assignedMnemo;
	
	/** The Snomed_ isa. */
	private I_GetConceptData Snomed_Isa;
	
	/** The inferred. */
	private int inferred;

	/**
	 * Instantiates a new translation concept editor.
	 *
	 */
	public TranslationConceptEditorRO() {
		sourceIds = new ArrayList<Integer>();
		I_ConfigAceFrame config = null;
		try {
			config = Terms.get().getActiveAceFrameConfig();
			inactive = Terms.get().getConcept(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getNid());
			fsn = Terms.get().getConcept(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid());
			preferred = Terms.get().getConcept(SnomedMetadataRf2.PREFERRED_RF2.getLenient().getNid());
			synonym = Terms.get().getConcept(SnomedMetadataRf2.SYNONYM_RF2.getLenient().getNid());
			Snomed_Isa= Terms.get().getConcept(UUID.fromString("c93a30b9-ba77-3adb-a9b8-4589c9f8fb25"));
			acceptable = Terms.get().getConcept(SnomedMetadataRf2.ACCEPTABLE_RF2.getLenient().getNid());
			active = Terms.get().getConcept(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid());
			definingChar = SnomedMetadataRf2.DEFINING_RELATIONSHIP_RF2.getLenient().getNid();
			inferred=SnomedMetadataRf2.INFERRED_RELATIONSHIP_RF2.getLenient().getNid();
			config.getDescTypes().add(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid());
			config.getDescTypes().add(SnomedMetadataRf2.SYNONYM_RF2.getLenient().getNid());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}

		initComponents();

		
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

		formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

		// bDescIssue.setEnabled(false);
		DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel();
		comboBoxModel.addElement(fsn);
		comboBoxModel.addElement(description);

		DefaultComboBoxModel comboBoxModel2 = new DefaultComboBoxModel();
		comboBoxModel2.addElement(preferred);
		comboBoxModel2.addElement(acceptable);

		tree3.setCellRenderer(new DetailsIconRenderer());
		tree3.setRootVisible(true);
		tree3.setShowsRootHandles(false);

		setByCode = false;

		tabSou.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tabTar.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tabSou.setModel(new DefaultTableModel());
		tabTar.setModel(new DefaultTableModel());
		tabTar.getSelectionModel().addListSelectionListener(new SelectionListener(tabTar));
		tabTar.setUpdateSelectionOnSort(false);
		tabSou.setUpdateSelectionOnSort(false);
		DefaultMutableTreeNode detailsRoot = new DefaultMutableTreeNode();
		tree3.setModel(new DefaultTreeModel(detailsRoot));

		ToolTipManager.sharedInstance().registerComponent(tree3);
		createIssuePanel();
		setMnemoInit();

		refTable.setContentType("text/html");
		refTable.setEditable(false);
		refTable.setOpaque(false);
	}
	
	/**
	 * The Enum TableSourceColumn.
	 */
	enum TableSourceColumn {
		
		/** The LANGUAGE. */
		LANGUAGE("Language"), 
 /** The TER m_ type. */
 TERM_TYPE("Term type"), 
 /** The ACCEPTABILITY. */
 ACCEPTABILITY("Acceptability"), 
 /** The ICS. */
 ICS("ICS"), 
 /** The TERM. */
 TERM("Term");

		/** The column name. */
		private final String columnName;

		/**
		 * Instantiates a new table source column.
		 *
		 * @param name the name
		 */
		private TableSourceColumn(String name) {
			this.columnName = name;
		}

		/**
		 * Gets the column name.
		 *
		 * @return the column name
		 */
		public String getColumnName() {
			return this.columnName;
		}
	}

	/**
	 * The Enum TableTargetColumn.
	 */
	enum TableTargetColumn {
		
		/** The LANGUAGE. */
		LANGUAGE("Language"), 
 /** The TER m_ type. */
 TERM_TYPE("Term type"), 
 /** The ACCEPTABILITY. */
 ACCEPTABILITY("Acceptability"), 
 /** The ICS. */
 ICS("ICS"), 
 /** The TERM. */
 TERM("Term");

		/** The column name. */
		private final String columnName;

		/**
		 * Instantiates a new table target column.
		 *
		 * @param name the name
		 */
		private TableTargetColumn(String name) {
			this.columnName = name;
		}

		/**
		 * Gets the column name.
		 *
		 * @return the column name
		 */
		public String getColumnName() {
			return this.columnName;
		}
	}

	/**
	 * Search button action preformed.
	 *
	 * @param e the e
	 */
	private void searchButtonActionPreformed(ActionEvent e) {
		String query = searchTextField.getText();
		if (!query.trim().equals("")) {
			updateSimilarityTable(query);
		}
	}

	/**
	 * Gets the translation project config.
	 *
	 * @return the translation project config
	 */
	private ConfigTranslationModule getTranslationProjectConfig() {
		ConfigTranslationModule translProjConfig = null;
		if (this.translationProject != null)
			translProjConfig = LanguageUtil.getDefaultTranslationConfig(this.translationProject);

		if (translProjConfig == null) {
			return translConfig;
		}
		translProjConfig.setColumnsDisplayedInInbox(translConfig.getColumnsDisplayedInInbox());
		translProjConfig.setAutoOpenNextInboxItem(translConfig.isAutoOpenNextInboxItem());
		translProjConfig.setSourceTreeComponents(translConfig.getSourceTreeComponents());
		translProjConfig.setTargetTreeComponents(translConfig.getTargetTreeComponents());
		return translProjConfig;
	}

	/**
	 * Sets the mnemo init.
	 */
	private void setMnemoInit() {
		assignedMnemo = "FPDHIAVUMGOL";
	}


	/**
	 * Rb fsn action performed.
	 *
	 * @param e the e
	 */
	private void rbFSNActionPerformed(ActionEvent e) {
		updateSimilarityTable(sourceFSN);
		searchTextField.setText(sourceFSN);
	}

	/**
	 * Rb pref action performed.
	 *
	 * @param e the e
	 */
	private void rbPrefActionPerformed(ActionEvent e) {
		updateSimilarityTable(sourceFSN);
		searchTextField.setText(sourceFSN);
	}

	/**
	 * Radio button2 action performed.
	 *
	 * @param e the e
	 */
	private void radioButton2ActionPerformed(ActionEvent e) {
		updateSimilarityTable(sourceFSN);
		searchTextField.setText(sourceFSN);
	}

	/**
	 * Save comment.
	 *
	 * @param comment the comment
	 */
	private void saveComment(String comment) {
		I_ConfigAceFrame config = null;
		try {
			config = Terms.get().getActiveAceFrameConfig();
			CommentsRefset commRefset = targetLangRefset.getCommentsRefset(config);
			String fullName = config.getDbConfig().getFullName();
			commRefset.addComment(this.concept.getConceptNid(), role.toString() + HEADER_SEPARATOR + fullName + COMMENT_HEADER_SEP + comment);
			Terms.get().commit();

		} catch (TerminologyException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		} catch (Exception e) {

			e.printStackTrace();
		}
		getPreviousComments();
		getWebReferences();

		try {
			populateTargetTree();
		} catch (Exception e1) {

			e1.printStackTrace();
		}
	}

	/**
	 * Show new comment panel.
	 */
	public void showNewCommentPanel() {

		NewCommentPanel cPanel;
		cPanel = new NewCommentPanel();

		int action = JOptionPane.showOptionDialog(null, cPanel, "Enter new comment", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);

		this.requestFocus();

		if (action == JOptionPane.CANCEL_OPTION) {
			return;
		}
		if (cPanel.getNewComment().trim().equals("")) {
			message("Cannot add a blank comment.");
			return;
		}
		saveComment(cPanel.getNewComment().trim());
	}

	/**
	 * Message.
	 * 
	 * @param string
	 *            the string
	 */
	private void message(String string) {

		JOptionPane.showOptionDialog(this, string, "Information", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
	}

	/**
	 * Ref table hyperlink update.
	 *
	 * @param hle the hle
	 */
	private void refTableHyperlinkUpdate(HyperlinkEvent hle) {
		if (HyperlinkEvent.EventType.ACTIVATED.equals(hle.getEventType())) {
			System.out.println("Opening: " + hle.getURL());
			System.out.println("Path: " + hle.getURL().getHost() + hle.getURL().getPath());
			try {
				Desktop desktop = null;
				if (Desktop.isDesktopSupported()) {
					desktop = Desktop.getDesktop();
					// String absoluteUrl = hle.getURL().getProtocol() + "://" +
					// new File(".").getAbsolutePath();
					// absoluteUrl = absoluteUrl.substring(0,
					// absoluteUrl.length() -1);
					// absoluteUrl = absoluteUrl +
					// hle.getURL().getHost().replace(" ", "%20");
					// absoluteUrl = absoluteUrl +
					// hle.getURL().getPath().replace(" ", "%20");
					// absoluteUrl = absoluteUrl.trim() + "#search=" +
					// queryField.getText().trim().replace(" ", "%20") + "";

					// System.out.println("URL: " + absoluteUrl);
					desktop.browse(new URI(hle.getURL().toString()));
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}

		}
	}

	/**
	 * Tbl comm mouse clicked.
	 *
	 * @param e the e
	 */
	private void tblCommMouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			viewComment();
		}
	}

	/**
	 * View comment.
	 */
	private void viewComment() {
		int row = tblComm.getSelectedRow();
		if (row > -1) {
			CommentPanel cp = new CommentPanel();
			String comm = (String) tblComm.getValueAt(row, 0);
			comm = comm.replace(htmlHeader, "");
			comm = comm.replace(htmlFooter, "");
			comm = comm.replace(endP, "");
			String[] arrComm = comm.split(COMMENT_HEADER_SEP);
			String header = arrComm[0];
			String[] headerComp = header.split(HEADER_SEPARATOR);
			String from = "";
			String role = "";
			String date = "";
			String source = "";
			if (headerComp.length > 0) {
				source = headerComp[0];
				if (headerComp.length > 1) {
					date = headerComp[1];
					if (headerComp.length > 2) {
						role = headerComp[2];
						if (headerComp.length > 3) {
							from = headerComp[3];
						} else {
							from = headerComp[2];
						}
					} else {
						from = headerComp[0];
					}
				} else {
					from = headerComp[0];
				}
			}
			// int sepLen=HEADER_SEPARATOR.length();
			// if (header.length()>0){
			// int toIndex=0;
			// toIndex=header.indexOf(HEADER_SEPARATOR, toIndex);
			// if (toIndex>-1){
			// source= getTextFromHeader(header,toIndex);
			// toIndex= header.indexOf(HEADER_SEPARATOR, toIndex + sepLen);
			// if (toIndex>-1){
			// date= getTextFromHeader(header,toIndex);
			// from=header.substring(toIndex + sepLen);
			// toIndex= header.indexOf(HEADER_SEPARATOR, toIndex +sepLen);
			// if (toIndex>-1){
			// role= getTextFromHeader(header,toIndex);
			// toIndex= header.indexOf(HEADER_SEPARATOR, toIndex + sepLen);
			// from=header.substring(toIndex + sepLen);
			// }
			// }
			// }
			// }else{
			// from =header;
			// }
			//			
			cp.setFrom(from);
			cp.setRole(role);
			cp.setDate(date);
			cp.setSource(source);
			int index = comm.indexOf(COMMENT_HEADER_SEP);
			cp.setComment(comm.substring(index + COMMENT_HEADER_SEP.length()));

			JOptionPane.showMessageDialog(null, cp, "Comment", JOptionPane.INFORMATION_MESSAGE);

			this.requestFocus();

		}

	}

	/**
	 * The listener interface for receiving selection events.
	 * The class that is interested in processing a selection
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addSelectionListener<code> method. When
	 * the selection event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see SelectionEvent
	 */
	class SelectionListener implements ListSelectionListener {

		/** The table. */
		JTable table;

		// It is necessary to keep the table since it is not possible
		// to determine the table from the event's source
		/**
		 * Instantiates a new selection listener.
		 * 
		 * @param table
		 *            the table
		 */
		SelectionListener(JTable table) {
			this.table = table;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * javax.swing.event.ListSelectionListener#valueChanged(javax.swing.
		 * event.ListSelectionEvent)
		 */
		public void valueChanged(ListSelectionEvent e) {
			// If cell selection is enabled, both row and column change events
			// are fired
			if (!(e.getSource() == table.getSelectionModel() && table.getRowSelectionAllowed())) {
				return;
			}

			if (e.getValueIsAdjusting()) {
				// The mouse button has not yet been released
				return;
			} else {
				int first = table.getSelectedRow();
				if (first > -1) {
					int rowModel = table.convertRowIndexToModel(first);
					ContextualizedDescription descrpt = (ContextualizedDescription) table.getModel().getValueAt(rowModel, TableTargetColumn.TERM.ordinal());

					if (descrpt != null && !setByCode) {
						updatePropertiesPanel(descrpt, rowModel);
					}
				}

			}
		}

	}

	/**
	 * Inits the components.
	 */
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		panel10 = new JPanel();
		splitPane3 = new JSplitPane();
		splitPane2 = new JSplitPane();
		panel9 = new JPanel();
		label9 = new JLabel();
		scrollPane1 = new JScrollPane();
		tabSou = new ZebraJTable();
		tabbedPane3 = new JTabbedPane();
		scrollPane7 = new JScrollPane();
		tree3 = new JTree();
		hierarchyNavigator1 = new HierarchyNavigator();
		panel8 = new JPanel();
		label11 = new JLabel();
		scrollPane6 = new JScrollPane();
		tabTar = new ZebraJTable();
		splitPane1 = new JSplitPane();
		tabbedPane1 = new JTabbedPane();
		panel12 = new JPanel();
		refinePanel = new JPanel();
		searchTextField = new JTextField();
		searchButton = new JButton();
		scrollPane2 = new JScrollPane();
		table1 = new ZebraJTable();
		panel13 = new JPanel();
		rbFSN = new JRadioButton();
		rbPref = new JRadioButton();
		radioButton2 = new JRadioButton();
		refineCheckBox = new JCheckBox();
		scrollPane3 = new JScrollPane();
		table2 = new ZebraJTable();
		panel15 = new JPanel();
		scrollPane4 = new JScrollPane();
		editorPane1 = new JEditorPane();
		tabbedPane2 = new JTabbedPane();
		panel16 = new JPanel();
		scrollPane9 = new JScrollPane();
		tblComm = new ZebraJTable();
		scrollPane8 = new JScrollPane();
		refTable = new JEditorPane();
		panel11 = new JPanel();

		//======== this ========
		setBackground(new Color(238, 238, 238));
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

		//======== panel10 ========
		{
			panel10.setLayout(new GridBagLayout());
			((GridBagLayout)panel10.getLayout()).columnWidths = new int[] {0, 0};
			((GridBagLayout)panel10.getLayout()).rowHeights = new int[] {0, 0};
			((GridBagLayout)panel10.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
			((GridBagLayout)panel10.getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

			//======== splitPane3 ========
			{
				splitPane3.setOrientation(JSplitPane.VERTICAL_SPLIT);
				splitPane3.setOneTouchExpandable(true);
				splitPane3.setResizeWeight(1.0);

				//======== splitPane2 ========
				{
					splitPane2.setToolTipText("Drag to resize");
					splitPane2.setBackground(new Color(238, 238, 238));

					//======== panel9 ========
					{
						panel9.setBackground(new Color(238, 238, 238));
						panel9.setLayout(new GridBagLayout());
						((GridBagLayout)panel9.getLayout()).columnWidths = new int[] {441, 0};
						((GridBagLayout)panel9.getLayout()).rowHeights = new int[] {20, 0, 0, 0};
						((GridBagLayout)panel9.getLayout()).columnWeights = new double[] {0.0, 1.0E-4};
						((GridBagLayout)panel9.getLayout()).rowWeights = new double[] {0.0, 1.0, 1.0, 1.0E-4};

						//---- label9 ----
						label9.setText("Source Language");
						panel9.add(label9, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 0), 0, 0));

						//======== scrollPane1 ========
						{

							//---- tabSou ----
							tabSou.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
							tabSou.setBorder(LineBorder.createBlackLineBorder());
							tabSou.setAutoCreateRowSorter(true);
							scrollPane1.setViewportView(tabSou);
						}
						panel9.add(scrollPane1, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 0), 0, 0));

						//======== tabbedPane3 ========
						{

							//======== scrollPane7 ========
							{

								//---- tree3 ----
								tree3.setVisibleRowCount(4);
								scrollPane7.setViewportView(tree3);
							}
							tabbedPane3.addTab("Concept Details", scrollPane7);

							tabbedPane3.addTab("Hierarchy", hierarchyNavigator1);
							tabbedPane3.setMnemonicAt(1, 'H');
						}
						panel9.add(tabbedPane3, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 0), 0, 0));
					}
					splitPane2.setLeftComponent(panel9);

					//======== panel8 ========
					{
						panel8.setBackground(new Color(238, 238, 238));
						panel8.setLayout(new GridBagLayout());
						((GridBagLayout)panel8.getLayout()).columnWidths = new int[] {0, 0};
						((GridBagLayout)panel8.getLayout()).rowHeights = new int[] {0, 100, 0};
						((GridBagLayout)panel8.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
						((GridBagLayout)panel8.getLayout()).rowWeights = new double[] {0.0, 1.0, 1.0E-4};

						//---- label11 ----
						label11.setText("Target Language");
						label11.setBackground(new Color(238, 238, 238));
						panel8.add(label11, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 0), 0, 0));

						//======== scrollPane6 ========
						{

							//---- tabTar ----
							tabTar.setAutoCreateRowSorter(true);
							tabTar.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
							tabTar.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
							tabTar.setBorder(LineBorder.createBlackLineBorder());
							scrollPane6.setViewportView(tabTar);
						}
						panel8.add(scrollPane6, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 0), 0, 0));
					}
					splitPane2.setRightComponent(panel8);
				}
				splitPane3.setTopComponent(splitPane2);

				//======== splitPane1 ========
				{
					splitPane1.setBackground(new Color(238, 238, 238));
					splitPane1.setResizeWeight(1.0);
					splitPane1.setToolTipText("Drag to resize");

					//======== tabbedPane1 ========
					{
						tabbedPane1.setFont(new Font("Verdana", Font.PLAIN, 13));

						//======== panel12 ========
						{
							panel12.setBackground(new Color(238, 238, 238));
							panel12.setLayout(new GridBagLayout());
							((GridBagLayout)panel12.getLayout()).columnWidths = new int[] {0, 0};
							((GridBagLayout)panel12.getLayout()).rowHeights = new int[] {0, 0, 0, 0};
							((GridBagLayout)panel12.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
							((GridBagLayout)panel12.getLayout()).rowWeights = new double[] {0.0, 1.0, 0.0, 1.0E-4};

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
								searchButton.setText("Search");
								refinePanel.add(searchButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
									GridBagConstraints.CENTER, GridBagConstraints.BOTH,
									new Insets(0, 0, 0, 0), 0, 0));
							}
							panel12.add(refinePanel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.BOTH,
								new Insets(0, 0, 5, 0), 0, 0));

							//======== scrollPane2 ========
							{

								//---- table1 ----
								table1.setPreferredScrollableViewportSize(new Dimension(180, 200));
								table1.setBackground(new Color(239, 235, 222));
								table1.setFont(new Font("Verdana", Font.PLAIN, 12));
								scrollPane2.setViewportView(table1);
							}
							panel12.add(scrollPane2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
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
								rbFSN.setBackground(new Color(200, 233, 249));
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
								rbPref.setBackground(new Color(200, 233, 249));
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
								radioButton2.setBackground(new Color(200, 233, 249));
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
									new Insets(0, 0, 0, 0), 0, 0));
							}
							panel12.add(panel13, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.BOTH,
								new Insets(0, 0, 0, 0), 0, 0));
						}
						tabbedPane1.addTab("Similarity", panel12);
						tabbedPane1.setMnemonicAt(0, 'L');

						//======== scrollPane3 ========
						{

							//---- table2 ----
							table2.setPreferredScrollableViewportSize(new Dimension(180, 200));
							table2.setFont(new Font("Verdana", Font.PLAIN, 12));
							scrollPane3.setViewportView(table2);
						}
						tabbedPane1.addTab("Translation Memory", scrollPane3);
						tabbedPane1.setMnemonicAt(1, 'M');

						//======== panel15 ========
						{
							panel15.setLayout(new GridBagLayout());
							((GridBagLayout)panel15.getLayout()).columnWidths = new int[] {0, 0};
							((GridBagLayout)panel15.getLayout()).rowHeights = new int[] {0, 0};
							((GridBagLayout)panel15.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
							((GridBagLayout)panel15.getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

							//======== scrollPane4 ========
							{

								//---- editorPane1 ----
								editorPane1.setContentType("text/html");
								scrollPane4.setViewportView(editorPane1);
							}
							panel15.add(scrollPane4, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.BOTH,
								new Insets(0, 0, 0, 0), 0, 0));
						}
						tabbedPane1.addTab("Linguistic Guidelines", panel15);
						tabbedPane1.setMnemonicAt(2, 'G');
					}
					splitPane1.setLeftComponent(tabbedPane1);

					//======== tabbedPane2 ========
					{

						//======== panel16 ========
						{
							panel16.setLayout(new GridBagLayout());
							((GridBagLayout)panel16.getLayout()).columnWidths = new int[] {0, 0};
							((GridBagLayout)panel16.getLayout()).rowHeights = new int[] {0, 0};
							((GridBagLayout)panel16.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
							((GridBagLayout)panel16.getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

							//======== scrollPane9 ========
							{

								//---- tblComm ----
								tblComm.addMouseListener(new MouseAdapter() {
									@Override
									public void mouseClicked(MouseEvent e) {
										tblCommMouseClicked(e);
									}
								});
								scrollPane9.setViewportView(tblComm);
							}
							panel16.add(scrollPane9, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.BOTH,
								new Insets(0, 0, 0, 0), 0, 0));
						}
						tabbedPane2.addTab("Comments", panel16);


						//======== scrollPane8 ========
						{

							//---- refTable ----
							refTable.setEditable(false);
							refTable.addHyperlinkListener(new HyperlinkListener() {
								@Override
								public void hyperlinkUpdate(HyperlinkEvent e) {
									refTableHyperlinkUpdate(e);
								}
							});
							scrollPane8.setViewportView(refTable);
						}
						tabbedPane2.addTab("Web references", scrollPane8);


						//======== panel11 ========
						{
							panel11.setBackground(Color.white);
							panel11.setLayout(new GridBagLayout());
							((GridBagLayout)panel11.getLayout()).columnWidths = new int[] {0, 0};
							((GridBagLayout)panel11.getLayout()).rowHeights = new int[] {0, 0};
							((GridBagLayout)panel11.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
							((GridBagLayout)panel11.getLayout()).rowWeights = new double[] {1.0, 1.0E-4};
						}
						tabbedPane2.addTab("Issues", panel11);
						tabbedPane2.setMnemonicAt(2, 'U');
					}
					splitPane1.setRightComponent(tabbedPane2);
				}
				splitPane3.setBottomComponent(splitPane1);
			}
			panel10.add(splitPane3, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel10, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 0), 0, 0));

		//---- buttonGroup2 ----
		ButtonGroup buttonGroup2 = new ButtonGroup();
		buttonGroup2.add(rbFSN);
		buttonGroup2.add(rbPref);
		buttonGroup2.add(radioButton2);
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	/** The panel10. */
	private JPanel panel10;
	
	/** The split pane3. */
	private JSplitPane splitPane3;
	
	/** The split pane2. */
	private JSplitPane splitPane2;
	
	/** The panel9. */
	private JPanel panel9;
	
	/** The label9. */
	private JLabel label9;
	
	/** The scroll pane1. */
	private JScrollPane scrollPane1;
	
	/** The tab sou. */
	private ZebraJTable tabSou;
	
	/** The tabbed pane3. */
	private JTabbedPane tabbedPane3;
	
	/** The scroll pane7. */
	private JScrollPane scrollPane7;
	
	/** The tree3. */
	private JTree tree3;
	
	/** The hierarchy navigator1. */
	private HierarchyNavigator hierarchyNavigator1;
	
	/** The panel8. */
	private JPanel panel8;
	
	/** The label11. */
	private JLabel label11;
	
	/** The scroll pane6. */
	private JScrollPane scrollPane6;
	
	/** The tab tar. */
	private ZebraJTable tabTar;
	
	/** The split pane1. */
	private JSplitPane splitPane1;
	
	/** The tabbed pane1. */
	private JTabbedPane tabbedPane1;
	
	/** The panel12. */
	private JPanel panel12;
	
	/** The refine panel. */
	private JPanel refinePanel;
	
	/** The search text field. */
	private JTextField searchTextField;
	
	/** The search button. */
	private JButton searchButton;
	
	/** The scroll pane2. */
	private JScrollPane scrollPane2;
	
	/** The table1. */
	private ZebraJTable table1;
	
	/** The panel13. */
	private JPanel panel13;
	
	/** The rb fsn. */
	private JRadioButton rbFSN;
	
	/** The rb pref. */
	private JRadioButton rbPref;
	
	/** The radio button2. */
	private JRadioButton radioButton2;
	
	/** The refine check box. */
	private JCheckBox refineCheckBox;
	
	/** The scroll pane3. */
	private JScrollPane scrollPane3;
	
	/** The table2. */
	private ZebraJTable table2;
	
	/** The panel15. */
	private JPanel panel15;
	
	/** The scroll pane4. */
	private JScrollPane scrollPane4;
	
	/** The editor pane1. */
	private JEditorPane editorPane1;
	
	/** The tabbed pane2. */
	private JTabbedPane tabbedPane2;
	
	/** The panel16. */
	private JPanel panel16;
	
	/** The scroll pane9. */
	private JScrollPane scrollPane9;
	
	/** The tbl comm. */
	private ZebraJTable tblComm;
	
	/** The scroll pane8. */
	private JScrollPane scrollPane8;
	
	/** The ref table. */
	private JEditorPane refTable;
	
	/** The panel11. */
	private JPanel panel11;
	// JFormDesigner - End of variables declaration //GEN-END:variables

	/** The concept. */
	private I_GetConceptData concept;

	//	
	// /** The source lang code. */
	// private String sourceLangCode;
	//	
	// /** The target lang code. */
	// private String targetLangCode;
	//	
	/** The description in editor. */
	private ContextualizedDescription descriptionInEditor;

	/** The source sem tag. */
	private int definingChar = -1;
	
	/** The source fsn. */
	private String sourceFSN;
	
	/** The worklist member. */
	private WorkListMember worklistMember;
	
	/** The set by code. */
	private boolean setByCode;
	
	/** The keep ii class. */
	private I_KeepTaskInInbox keepIIClass;
	
	/** The unloaded. */
	private boolean unloaded;
	
	/** The role. */
	private I_GetConceptData role;
	
	/** The editing row. */
	private Integer editingRow;
	
	/** The html footer. */
	private String htmlFooter = "</body></html>";
	
	/** The html header. */
	private String htmlHeader = "<html><body><font style='color:blue'>";
	
	/** The end p. */
	private String endP = "</font>";

	/**
	 * Gets the concept.
	 * 
	 * @return the concept
	 */
	public I_GetConceptData getConcept() {
		return concept;
	}

	/**
	 * Sets the concept.
	 * 
	 * @param concept
	 *            the new concept
	 */
	public void setConcept(I_GetConceptData concept) {
		this.concept = concept;
	}

	/**
	 * Populate tree.
	 *
	 * @throws Exception the exception
	 */
	private void populateSourceTree() throws Exception {
		// DefaultMutableTreeNode top = null;
		// translConfig=LanguageUtil.getTranslationConfig(Terms.get().getActiveAceFrameConfig());
		int maxTermWidth = 0;
		LinkedHashSet<TreeComponent> sourceCom = translConfig.getSourceTreeComponents();
		Object[][] data = null;
		String[] columnNames = new String[TableSourceColumn.values().length];
		for (int i = 0; i < TableSourceColumn.values().length; i++) {
			columnNames[i] = TableSourceColumn.values()[i].getColumnName();
		}

		DefaultTableModel model = new DefaultTableModel(data, columnNames) {
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int x, int y) {
				return false;
			}
		};
		List<Object[]> tmpRows = new ArrayList<Object[]>();
		if (concept != null) {
			try {
				// top = new DefaultMutableTreeNode(new
				// TreeEditorObjectWrapper(concept.toString(),
				// TreeEditorObjectWrapper.CONCEPT, concept));
				for (I_GetConceptData langRefset : this.translationProject.getSourceLanguageRefsets()) {
					List<ContextualizedDescription> descriptions = LanguageUtil.getContextualizedDescriptions(concept.getConceptNid(), langRefset.getConceptNid(), true);

					// DefaultMutableTreeNode groupLang = new
					// DefaultMutableTreeNode(
					// new TreeEditorObjectWrapper(langRefset.getInitialText(),
					// TreeEditorObjectWrapper.FOLDER,langRefset ));

					// List<DefaultMutableTreeNode> nodesToAdd = new
					// ArrayList<DefaultMutableTreeNode>();

					boolean bSourceFSN = false;
					boolean bNewNode = false;
					for (I_ContextualizeDescription description : descriptions) {
						if (description.getLanguageExtension() != null && description.getLanguageRefsetId() == langRefset.getConceptNid()) {
							bNewNode = false;
							Object[] rowClass = new Object[2];
							Object[] row = new Object[TableSourceColumn.values().length];
							Object[] termType_Status = new Object[2];
							row[TableSourceColumn.TERM.ordinal()] = description;
							row[TableSourceColumn.LANGUAGE.ordinal()] = description.getLang();
							row[TableSourceColumn.ICS.ordinal()] = description.isInitialCaseSignificant();

							if ( description.getExtensionStatusId() == inactive.getConceptNid()
									|| description.getDescriptionStatusId() == inactive.getConceptNid()) {
								if (sourceCom.contains(ConfigTranslationModule.TreeComponent.RETIRED)) {
									rowClass[0] = TreeEditorObjectWrapper.NOTACCEPTABLE;
//									row[TableSourceColumn.ACCEPTABILITY.ordinal()] = notAcceptable;
									termType_Status[1] = inactive;
									if (description.getTypeId() == fsn.getConceptNid()) {
										termType_Status[0] = fsn;
									} else {
										// row[TableSourceColumn.TERM_TYPE.ordinal()]=this.description;
										termType_Status[0] = this.description;
									}
									row[TableSourceColumn.TERM_TYPE.ordinal()] = termType_Status;
									bNewNode = true;
								}
							} else if (description.getTypeId() == fsn.getConceptNid()) {
								if (sourceCom.contains(ConfigTranslationModule.TreeComponent.FSN)) {
									rowClass[0] = TreeEditorObjectWrapper.FSNDESCRIPTION;
									row[TableSourceColumn.ACCEPTABILITY.ordinal()] = preferred;
									termType_Status[0] = fsn;
									termType_Status[1] = active;
									row[TableSourceColumn.TERM_TYPE.ordinal()] = termType_Status;

									bNewNode = true;
								}
								int semtagLocation = description.getText().lastIndexOf("(");
								if (semtagLocation == -1)
									semtagLocation = description.getText().length();

								if (!bSourceFSN) {
									bSourceFSN = true;
									sourceFSN = description.getText().substring(0, semtagLocation);
									updateTransMemoryTable(sourceFSN);
								
								}
							} else if (description.getAcceptabilityId() == acceptable.getConceptNid() && sourceCom.contains(ConfigTranslationModule.TreeComponent.SYNONYM)) {
								rowClass[0] = TreeEditorObjectWrapper.SYNONYMN;
								row[TableSourceColumn.ACCEPTABILITY.ordinal()] = acceptable;
								termType_Status[0] = this.description;
								termType_Status[1] = active;
								row[TableSourceColumn.TERM_TYPE.ordinal()] = termType_Status;
								bNewNode = true;
							} else if (description.getAcceptabilityId() == preferred.getConceptNid() && sourceCom.contains(ConfigTranslationModule.TreeComponent.PREFERRED)) {
								rowClass[0] = TreeEditorObjectWrapper.PREFERRED;
								row[TableSourceColumn.ACCEPTABILITY.ordinal()] = preferred;
								termType_Status[0] = this.description;
								termType_Status[1] = active;
								row[TableSourceColumn.TERM_TYPE.ordinal()] = termType_Status;
								bNewNode = true;
							} else if (sourceCom.contains(ConfigTranslationModule.TreeComponent.RETIRED)) {
								rowClass[0] = TreeEditorObjectWrapper.SYNONYMN;
//								row[TableSourceColumn.ACCEPTABILITY.ordinal()] = notAcceptable;
								termType_Status[0] = this.description;
								termType_Status[1] = inactive;
								row[TableSourceColumn.TERM_TYPE.ordinal()] = termType_Status;
								bNewNode = true;
							}
							if (bNewNode) {
								rowClass[1] = row;
								tmpRows.add(rowClass);
							}
						}
					}
					Font fontTmp = tabSou.getFont();
					FontMetrics fontMetrics = new FontMetrics(fontTmp) {
					};
					Rectangle2D bounds;
					for (ConfigTranslationModule.TreeComponent tComp : sourceCom) {

						switch (tComp) {
						case FSN:
							for (Object[] rowClass : tmpRows) {
								if ((Integer) rowClass[0] == TreeEditorObjectWrapper.FSNDESCRIPTION) {
									Object[] row = (Object[]) rowClass[1];
									bounds = fontMetrics.getStringBounds(row[TableSourceColumn.TERM.ordinal()].toString(), null);
									int tmpWidth = (int) bounds.getWidth();
									if (maxTermWidth < tmpWidth) {
										maxTermWidth = tmpWidth;
									}
									model.addRow(row);
								}
							}
							break;
						case PREFERRED:

							for (Object[] rowClass : tmpRows) {
								if ((Integer) rowClass[0] == TreeEditorObjectWrapper.PREFERRED) {
									Object[] row = (Object[]) rowClass[1];
									bounds = fontMetrics.getStringBounds(row[TableSourceColumn.TERM.ordinal()].toString(), null);
									int tmpWidth = (int) bounds.getWidth();
									if (maxTermWidth < tmpWidth) {
										maxTermWidth = tmpWidth;
									}
									model.addRow(row);
								}
							}
							break;
						case SYNONYM:
							for (Object[] rowClass : tmpRows) {
								if ((Integer) rowClass[0] == TreeEditorObjectWrapper.SYNONYMN) {
									Object[] row = (Object[]) rowClass[1];
									bounds = fontMetrics.getStringBounds(row[TableSourceColumn.TERM.ordinal()].toString(), null);
									int tmpWidth = (int) bounds.getWidth();
									if (maxTermWidth < tmpWidth) {
										maxTermWidth = tmpWidth;
									}
									model.addRow(row);
								}
							}
							break;
						case RETIRED:
							for (Object[] rowClass : tmpRows) {
								if ((Integer) rowClass[0] == TreeEditorObjectWrapper.NOTACCEPTABLE) {
									Object[] row = (Object[]) rowClass[1];
									bounds = fontMetrics.getStringBounds(row[TableSourceColumn.TERM.ordinal()].toString(), null);
									int tmpWidth = (int) bounds.getWidth();
									if (maxTermWidth < tmpWidth) {
										maxTermWidth = tmpWidth;
									}
									model.addRow(row);
								}
							}
							break;
						}
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			} catch (TerminologyException e) {
				e.printStackTrace();
			}
		}

		tabSou.setModel(model);
		if (maxTermWidth == 0)
			maxTermWidth = 200;
		else
			maxTermWidth += 10;
		TableColumnModel cmodel = tabSou.getColumnModel();
		TextAreaRenderer textAreaRenderer = new TextAreaRenderer();
		cmodel.getColumn(TableSourceColumn.TERM.ordinal()).setCellRenderer(textAreaRenderer);
		cmodel.getColumn(TableSourceColumn.TERM_TYPE.ordinal()).setCellRenderer(new TermTypeIconRenderer());
		cmodel.getColumn(TableSourceColumn.ACCEPTABILITY.ordinal()).setCellRenderer(new AcceptabilityIconRenderer());
		cmodel.getColumn(TableSourceColumn.LANGUAGE.ordinal()).setCellRenderer(new LanguageIconRenderer());
		cmodel.getColumn(TableSourceColumn.ICS.ordinal()).setCellRenderer(new ICSIconRenderer());

		double widthAvai = panel9.getPreferredSize().getWidth();
		int termColAvai = (int) (widthAvai - 100);
		if (termColAvai > maxTermWidth)
			cmodel.getColumn(TableSourceColumn.TERM.ordinal()).setPreferredWidth(termColAvai);
		else
			cmodel.getColumn(TableSourceColumn.TERM.ordinal()).setPreferredWidth(maxTermWidth);
		cmodel.getColumn(TableSourceColumn.TERM.ordinal()).setMinWidth(maxTermWidth);

		cmodel.getColumn(TableSourceColumn.TERM_TYPE.ordinal()).setPreferredWidth(24);
		cmodel.getColumn(TableSourceColumn.ACCEPTABILITY.ordinal()).setPreferredWidth(24);
		cmodel.getColumn(TableSourceColumn.ICS.ordinal()).setPreferredWidth(24);
		cmodel.getColumn(TableSourceColumn.LANGUAGE.ordinal()).setPreferredWidth(24);

		tabSou.setRowHeight(24);
		tabSou.setUpdateSelectionOnSort(true);
		tabSou.revalidate();

	}

	/**
	 * Populate target tree.
	 *
	 * @throws Exception the exception
	 */
	private void populateTargetTree() throws Exception {
		I_TermFactory tf = Terms.get();
		int authId;
		String authorColName = "";
		int authorColPos = -1;
		int authorAdj = 0;
		int maxTermWidth = 0;
		HashMap<Integer, String> hashAuthId = new HashMap<Integer, String>();
		LinkedHashSet<TreeComponent> targetCom = translConfig.getTargetTreeComponents();

		if (translConfig.getTargetTreeComponents().contains(ConfigTranslationModule.TreeComponent.AUTHOR_PATH)) {
			authorAdj = 1;
			authorColPos = TableTargetColumn.values().length;
			authorColName = "Author";
		}
		Object[][] data = null;
		String[] columnNames = new String[TableTargetColumn.values().length + authorAdj];
		for (int i = 0; i < TableTargetColumn.values().length; i++) {
			columnNames[i] = TableTargetColumn.values()[i].getColumnName();
		}
		if (authorAdj == 1) {
			columnNames[authorColPos] = authorColName;
		}
		DefaultTableModel model = new DefaultTableModel(data, columnNames) {
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int x, int y) {
				return false;
			}
		};
		List<Object[]> tmpRows = new ArrayList<Object[]>();
		if (concept != null) {
			try {
				// top = new DefaultMutableTreeNode(new
				// TreeEditorObjectWrapper(concept.toString(),
				// TreeEditorObjectWrapper.CONCEPT, concept));
				I_GetConceptData langRefset = this.translationProject.getTargetLanguageRefset();
				if (langRefset == null) {
					JOptionPane.showMessageDialog(new JDialog(), "Target language refset cannot be retrieved\nCheck project details", "Error", JOptionPane.ERROR_MESSAGE);
					throw new Exception("Target language refset cannot be retrieved.");
				}
				List<ContextualizedDescription> descriptions = LanguageUtil.getContextualizedDescriptions(concept.getConceptNid(), langRefset.getConceptNid(), true);

				// DefaultMutableTreeNode groupLang = new
				// DefaultMutableTreeNode(
				// new TreeEditorObjectWrapper(langRefset.getInitialText(),
				// TreeEditorObjectWrapper.FOLDER,langRefset ));

				// List<DefaultMutableTreeNode> nodesToAdd = new
				// ArrayList<DefaultMutableTreeNode>();

				boolean bNewNode = false;
				for (I_ContextualizeDescription description : descriptions) {
					if (description.getLanguageExtension() != null && description.getLanguageRefsetId() == langRefset.getConceptNid()) {
						// DefaultMutableTreeNode descriptionNode = null;
						bNewNode = false;
						Object[] row = new Object[TableTargetColumn.values().length];

						Object[] rowClass = new Object[2];
						Object[] termType_Status = new Object[2];
						row[TableTargetColumn.TERM.ordinal()] = description;
						row[TableTargetColumn.LANGUAGE.ordinal()] = description.getLang();
						row[TableTargetColumn.ICS.ordinal()] = description.isInitialCaseSignificant();

						if ( description.getExtensionStatusId() == inactive.getConceptNid()
								|| description.getDescriptionStatusId() == inactive.getConceptNid()) {
							if (targetCom.contains(ConfigTranslationModule.TreeComponent.RETIRED)) {
								rowClass[0] = TreeEditorObjectWrapper.NOTACCEPTABLE;
//								row[TableTargetColumn.ACCEPTABILITY.ordinal()] = notAcceptable;
								termType_Status[1] = inactive;
								if (description.getTypeId() == fsn.getConceptNid()) {
									termType_Status[0] = fsn;
								} else {
									termType_Status[0] = this.description;
								}
								row[TableSourceColumn.TERM_TYPE.ordinal()] = termType_Status;
								bNewNode = true;
							}
						} else if (description.getTypeId() == fsn.getConceptNid()) {
							if (targetCom.contains(ConfigTranslationModule.TreeComponent.FSN)) {
								rowClass[0] = TreeEditorObjectWrapper.FSNDESCRIPTION;
								row[TableSourceColumn.ACCEPTABILITY.ordinal()] = preferred;
								termType_Status[0] = fsn;
								termType_Status[1] = active;
								row[TableSourceColumn.TERM_TYPE.ordinal()] = termType_Status;
								bNewNode = true;
								bNewNode = true;
							}
						} else if (description.getAcceptabilityId() == acceptable.getConceptNid() && targetCom.contains(ConfigTranslationModule.TreeComponent.SYNONYM)) {
							rowClass[0] = TreeEditorObjectWrapper.SYNONYMN;
							row[TableSourceColumn.ACCEPTABILITY.ordinal()] = acceptable;
							termType_Status[0] = this.description;
							termType_Status[1] = active;
							row[TableSourceColumn.TERM_TYPE.ordinal()] = termType_Status;
							bNewNode = true;
						} else if (description.getAcceptabilityId() == preferred.getConceptNid() && targetCom.contains(ConfigTranslationModule.TreeComponent.PREFERRED)) {
							rowClass[0] = TreeEditorObjectWrapper.PREFERRED;
							row[TableSourceColumn.ACCEPTABILITY.ordinal()] = preferred;
							termType_Status[0] = this.description;
							termType_Status[1] = active;
							row[TableSourceColumn.TERM_TYPE.ordinal()] = termType_Status;

							bNewNode = true;
						} else if (targetCom.contains(ConfigTranslationModule.TreeComponent.RETIRED)) {
							rowClass[0] = TreeEditorObjectWrapper.SYNONYMN;
//							row[TableSourceColumn.ACCEPTABILITY.ordinal()] = notAcceptable;
							termType_Status[0] = this.description;
							termType_Status[1] = inactive;
							row[TableSourceColumn.TERM_TYPE.ordinal()] = termType_Status;
							bNewNode = true;
						}
						if (bNewNode) {
							if (translConfig.getTargetTreeComponents().contains(ConfigTranslationModule.TreeComponent.AUTHOR_PATH)) {

								authId = description.getDescriptionVersioned().getLastTuple().getAuthorNid();
								String userConcept = "";
								if (hashAuthId.containsKey(authId))
									userConcept = hashAuthId.get(authId);
								else {
									I_GetConceptData conc = tf.getConcept(authId);
									if (conc != null) {
										userConcept = conc.toString();
										hashAuthId.put(authId, userConcept);
									}
								}
								if (!userConcept.equals("")) {
									row[authorColPos] = userConcept;

								}

							}
							rowClass[1] = row;
							tmpRows.add(rowClass);
						}
					}
				}
				Font fontTmp = tabTar.getFont();
				FontMetrics fontMetrics = new FontMetrics(fontTmp) {
				};
				Rectangle2D bounds;
				for (ConfigTranslationModule.TreeComponent tComp : targetCom) {

					switch (tComp) {
					case FSN:
						for (Object[] rowClass : tmpRows) {
							if ((Integer) rowClass[0] == TreeEditorObjectWrapper.FSNDESCRIPTION) {
								Object[] row = (Object[]) rowClass[1];
								bounds = fontMetrics.getStringBounds(row[TableTargetColumn.TERM.ordinal()].toString(), null);
								int tmpWidth = (int) bounds.getWidth();
								if (maxTermWidth < tmpWidth) {
									maxTermWidth = tmpWidth;
								}
								model.addRow(row);
							}
						}
						break;
					case PREFERRED:
						for (Object[] rowClass : tmpRows) {
							if ((Integer) rowClass[0] == TreeEditorObjectWrapper.PREFERRED) {
								Object[] row = (Object[]) rowClass[1];
								bounds = fontMetrics.getStringBounds(row[TableTargetColumn.TERM.ordinal()].toString(), null);
								int tmpWidth = (int) bounds.getWidth();
								if (maxTermWidth < tmpWidth) {
									maxTermWidth = tmpWidth;
								}
								model.addRow(row);
							}
						}
						break;
					case SYNONYM:
						for (Object[] rowClass : tmpRows) {
							if ((Integer) rowClass[0] == TreeEditorObjectWrapper.SYNONYMN) {
								Object[] row = (Object[]) rowClass[1];
								bounds = fontMetrics.getStringBounds(row[TableTargetColumn.TERM.ordinal()].toString(), null);
								int tmpWidth = (int) bounds.getWidth();
								if (maxTermWidth < tmpWidth) {
									maxTermWidth = tmpWidth;
								}
								model.addRow(row);
							}
						}
						break;
					case RETIRED:
						for (Object[] rowClass : tmpRows) {
							if ((Integer) rowClass[0] == TreeEditorObjectWrapper.NOTACCEPTABLE) {
								Object[] row = (Object[]) rowClass[1];
								bounds = fontMetrics.getStringBounds(row[TableTargetColumn.TERM.ordinal()].toString(), null);
								int tmpWidth = (int) bounds.getWidth();
								if (maxTermWidth < tmpWidth) {
									maxTermWidth = tmpWidth;
								}
								model.addRow(row);
							}
						}
						break;
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			} catch (TerminologyException e) {
				e.printStackTrace();
			}
		}

		tabTar.setModel(model);
		if (maxTermWidth == 0)
			maxTermWidth = 200;
		else
			maxTermWidth += 10;
		TableColumnModel cmodel = tabTar.getColumnModel();
		TextAreaRenderer textAreaRenderer = new TextAreaRenderer();
		cmodel.getColumn(TableTargetColumn.TERM.ordinal()).setCellRenderer(textAreaRenderer);
		cmodel.getColumn(TableTargetColumn.TERM_TYPE.ordinal()).setCellRenderer(new TermTypeIconRenderer());
		cmodel.getColumn(TableTargetColumn.ACCEPTABILITY.ordinal()).setCellRenderer(new AcceptabilityIconRenderer());
		cmodel.getColumn(TableTargetColumn.LANGUAGE.ordinal()).setCellRenderer(new LanguageIconRenderer());
		cmodel.getColumn(TableTargetColumn.ICS.ordinal()).setCellRenderer(new ICSIconRenderer());

		double widthAvai = panel8.getPreferredSize().getWidth();

		double widthAdj = 0;
		if (authorAdj == 1) {
			widthAdj = cmodel.getColumn(authorColPos).getWidth();
		}
		int termColAvai = (int) (widthAvai - 100 - widthAdj);
		if (termColAvai > maxTermWidth)
			cmodel.getColumn(TableTargetColumn.TERM.ordinal()).setPreferredWidth(termColAvai);
		// if
		// (cmodel.getColumn(TableTargetColumn.TERM.ordinal()).getWidth()<maxTermWidth)
		else
			cmodel.getColumn(TableTargetColumn.TERM.ordinal()).setPreferredWidth(maxTermWidth);
		cmodel.getColumn(TableTargetColumn.TERM.ordinal()).setMinWidth(maxTermWidth);
		cmodel.getColumn(TableTargetColumn.TERM_TYPE.ordinal()).setPreferredWidth(24);
		cmodel.getColumn(TableTargetColumn.ACCEPTABILITY.ordinal()).setPreferredWidth(24);
		cmodel.getColumn(TableTargetColumn.ICS.ordinal()).setPreferredWidth(24);
		cmodel.getColumn(TableTargetColumn.LANGUAGE.ordinal()).setPreferredWidth(24);
		tabTar.setRowHeight(24);
		tabTar.setUpdateSelectionOnSort(true);
		tabTar.revalidate();

	}

	/**
	 * Gets the max term width.
	 *
	 * @param font the font
	 * @param string the string
	 * @return the max term width
	 */
	private int getMaxTermWidth(Font font, String string) {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * Populate details tree.
	 *
	 * @throws Exception the exception
	 */
	@SuppressWarnings("unchecked")
	private void populateDetailsTree() throws Exception {
		I_TermFactory tf = Terms.get();
		DefaultMutableTreeNode top = null;
		if (concept != null) {
			try {
				I_ConfigAceFrame config = tf.getActiveAceFrameConfig();

				I_ConceptAttributeTuple attributes = null;
				attributes = concept.getConceptAttributeTuples(config.getPrecedence(), config.getConflictResolutionStrategy()).iterator().next();
				if (attributes.getStatusNid()==inactive.getConceptNid()) {
							top = new DefaultMutableTreeNode(new TreeEditorObjectWrapper(concept.toString(), IconUtilities.INACTIVE, concept));
				} else if (attributes.isDefined()) {
					top = new DefaultMutableTreeNode(new TreeEditorObjectWrapper(concept.toString(), IconUtilities.DEFINED, concept));
				} else {
					top = new DefaultMutableTreeNode(new TreeEditorObjectWrapper(concept.toString(), IconUtilities.PRIMITIVE, concept));
				}

				List<I_RelTuple> relationships = (List<I_RelTuple>) concept.getSourceRelTuples(null, null, config.getViewPositionSetReadOnly(), config.getPrecedence(), config
						.getConflictResolutionStrategy());

				List<DefaultMutableTreeNode> nodesToAdd = new ArrayList<DefaultMutableTreeNode>();

				HashMap<Integer, List<DefaultMutableTreeNode>> mapGroup = new HashMap<Integer, List<DefaultMutableTreeNode>>();
				List<DefaultMutableTreeNode> roleList = new ArrayList<DefaultMutableTreeNode>();
				int group = 0;
				for (I_RelTuple relationship : relationships) {
					I_GetConceptData targetConcept = tf.getConcept(relationship.getC2Id());
					I_GetConceptData typeConcept = tf.getConcept(relationship.getTypeNid());
					String label = typeConcept + ": " + targetConcept;

					if ((relationship.getTypeNid() == Snomed_Isa.getConceptNid()) || (relationship.getTypeNid() == ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid())) {
						attributes = targetConcept.getConceptAttributeTuples(config.getPrecedence(), config.getConflictResolutionStrategy()).iterator().next();
						DefaultMutableTreeNode supertypeNode = null;
						if (attributes.getStatusNid()==inactive.getConceptNid()) {
							supertypeNode = new DefaultMutableTreeNode(new TreeEditorObjectWrapper(label, IconUtilities.INACTIVE_PARENT, relationship.getMutablePart()));

						} else if (attributes.isDefined()) {
							supertypeNode = new DefaultMutableTreeNode(new TreeEditorObjectWrapper(label, IconUtilities.DEFINED_PARENT, relationship.getMutablePart()));
						} else {
							supertypeNode = new DefaultMutableTreeNode(new TreeEditorObjectWrapper(label, IconUtilities.PRIMITIVE_PARENT, relationship.getMutablePart()));

						}
						nodesToAdd.add(supertypeNode);
					} else {
						if (relationship.getGroup() == 0) {
							if (relationship.getCharacteristicId() == definingChar 
									|| relationship.getCharacteristicId() == inferred) {
								DefaultMutableTreeNode roleNode = new DefaultMutableTreeNode(new TreeEditorObjectWrapper(label, IconUtilities.ROLE, relationship.getMutablePart()));
								nodesToAdd.add(roleNode);
							} else {
								DefaultMutableTreeNode roleNode = new DefaultMutableTreeNode(new TreeEditorObjectWrapper(label, IconUtilities.ASSOCIATION, relationship.getMutablePart()));
								nodesToAdd.add(roleNode);
							}
						} else {
							group = relationship.getGroup();
							if (mapGroup.containsKey(group)) {
								roleList = mapGroup.get(group);
							} else {
								roleList = new ArrayList<DefaultMutableTreeNode>();
							}

							roleList.add(new DefaultMutableTreeNode(new TreeEditorObjectWrapper(label, IconUtilities.ROLE, relationship.getMutablePart())));
							mapGroup.put(group, roleList);
						}
					}
				}

				for (DefaultMutableTreeNode loopNode : nodesToAdd) {
					TreeEditorObjectWrapper nodeObject = (TreeEditorObjectWrapper) loopNode.getUserObject();
					if (nodeObject.getType() == IconUtilities.DEFINED_PARENT || nodeObject.getType() == IconUtilities.PRIMITIVE_PARENT
							|| nodeObject.getType() == IconUtilities.INACTIVE_PARENT) {
						top.add(loopNode);
					}
				}

				for (DefaultMutableTreeNode loopNode : nodesToAdd) {
					TreeEditorObjectWrapper nodeObject = (TreeEditorObjectWrapper) loopNode.getUserObject();
					if (nodeObject.getType() == IconUtilities.ROLE) {
						top.add(loopNode);
					}
				}
				for (int key : mapGroup.keySet()) {
					List<DefaultMutableTreeNode> lRoles = (List<DefaultMutableTreeNode>) mapGroup.get(key);
					DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode(new TreeEditorObjectWrapper("Group:" + key, IconUtilities.ROLEGROUP, lRoles));
					for (DefaultMutableTreeNode rNode : lRoles) {
						groupNode.add(rNode);
					}
					top.add(groupNode);
				}
				for (DefaultMutableTreeNode loopNode : nodesToAdd) {
					TreeEditorObjectWrapper nodeObject = (TreeEditorObjectWrapper) loopNode.getUserObject();
					if (nodeObject.getType() == IconUtilities.ASSOCIATION) {
						top.add(loopNode);
					}
				}

			} catch (IOException e) {
				e.printStackTrace();
			} catch (TerminologyException e) {
				e.printStackTrace();
			}
			DefaultTreeModel treeModel = new DefaultTreeModel(top);

			tree3.setModel(treeModel);

			for (int i = 0; i < tree3.getRowCount(); i++) {
				tree3.expandRow(i);
			}
			tree3.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			tree3.revalidate();
		}
	}

	/**
	 * The Class TermTypeIconRenderer.
	 */
	class TermTypeIconRenderer extends DefaultTableCellRenderer {

		/* (non-Javadoc)
		 * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
		 */
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

			JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			Object[] termType_status = (Object[]) value;
			String termType = termType_status[0].toString();
			String status = termType_status[1].toString();
			//label.setIcon(IconUtilities.getIconForTermType_Status(termType, status));
			label.setText("");
			label.setToolTipText(status + " " + termType);
			label.setHorizontalAlignment(CENTER);

			return label;
		}

	}

	/**
	 * The Class AcceptabilityIconRenderer.
	 */
	class AcceptabilityIconRenderer extends DefaultTableCellRenderer {

		/* (non-Javadoc)
		 * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
		 */
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

			JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			label.setIcon(IconUtilities.getIconForAcceptability(value.toString()));
			label.setText("");
			label.setToolTipText(value.toString());
			label.setHorizontalAlignment(CENTER);
			return label;
		}

	}

	/**
	 * The Class LanguageIconRenderer.
	 */
	class LanguageIconRenderer extends DefaultTableCellRenderer {

		/* (non-Javadoc)
		 * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
		 */
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

			JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			label.setIcon(IconUtilities.getIconForLanguage(value.toString()));
			label.setText("");
			label.setToolTipText(value.toString());
			label.setHorizontalAlignment(CENTER);

			return label;
		}

	}

	/**
	 * The Class ICSIconRenderer.
	 */
	class ICSIconRenderer extends DefaultTableCellRenderer {

		/* (non-Javadoc)
		 * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
		 */
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

			JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			label.setIcon(IconUtilities.getIconForICS((Boolean) value));
			label.setText("");
			label.setToolTipText(value.toString());
			label.setHorizontalAlignment(CENTER);

			return label;
		}

	}

	/**
	 * Update properties panel.
	 *
	 * @param descrpt the descrpt
	 * @param rowModel the row model
	 */
	private void updatePropertiesPanel(ContextualizedDescription descrpt, int rowModel) {
		boolean update = false;

		if (translConfig.getSelectedEditorMode().equals(ConfigTranslationModule.EditorMode.PREFERRED_TERM_EDITOR)) {
			if (descrpt.getTypeId() != preferred.getConceptNid()) {
				if (editingRow != null) {
					setByCode = true;
					int selrow = tabTar.convertRowIndexToView(editingRow);
					tabTar.setRowSelectionInterval(selrow, selrow);
					setByCode = false;
				}
				return;
			}
		}
		if (translConfig.getSelectedEditorMode().equals(ConfigTranslationModule.EditorMode.SYNONYMS_EDITOR)) {
			if (descrpt.getTypeId() != synonym.getConceptNid()) {
				if (editingRow != null) {
					setByCode = true;
					int selrow = tabTar.convertRowIndexToView(editingRow);
					tabTar.setRowSelectionInterval(selrow, selrow);
					setByCode = false;
				}
				return;
			}
		}
		if (descriptionInEditor == null) {
			update = true;
		} else {
			Object[] options = { "Discard unsaved data", "Cancel and continue editing" };
			int n = JOptionPane.showOptionDialog(null, "Do you want to save the change you made to the term in the editor panel?", "Unsaved data", JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE, null, // do not use a
					// custom Icon
					options, // the titles of buttons
					options[1]); // default button title
			if (n == 0) {
				update = true;
			} else {
				update = false;
				if (editingRow != null) {
					setByCode = true;
					int selrow = tabTar.convertRowIndexToView(editingRow);
					tabTar.setRowSelectionInterval(selrow, selrow);
					setByCode = false;
				}
			}
		}

		if (update) {

			if (descrpt == null) {
				descriptionInEditor = null;
			} else {
				editingRow = rowModel;
				if (descrpt.getLanguageRefsetId() == targetId) {
					if (descrpt.getTypeId() == fsn.getConceptNid() || descrpt.getTypeId() == preferred.getConceptNid() || descrpt.getTypeId() == synonym.getConceptNid()) {
						descriptionInEditor = descrpt;
					}
				}
			}
		}
	}

	/**
	 * Update similarity table.
	 * 
	 * @param query
	 *            the query
	 */
	private void updateSimilarityTable(String query) {
		List<Integer> types= new ArrayList<Integer>();
		if (rbFSN.isSelected())
			types.add(fsn.getConceptNid());
		else if (rbPref.isSelected())
			types.add(preferred.getConceptNid());
		else {
			types.add(fsn.getConceptNid());
			types.add(preferred.getConceptNid());
		}

		List<SimilarityMatchedItem> results = LanguageUtil.getSimilarityResults(query, sourceIds, targetId, types,null);
		String[] columnNames = { "Source Text", "Target Text" };
		String[][] data = null;
		DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int x, int y) {
				return false;
			}
		};

		if (results.isEmpty()) {
			tableModel.addRow(new String[] { query, "No matches found" });
		} else {
			for (SimilarityMatchedItem item : results) {
				tableModel.addRow(new String[] { item.getSourceText(), item.getTargetText() });
			}
		}
		table1.setModel(tableModel);
		TableColumnModel cmodel = table1.getColumnModel();
		TextAreaRenderer textAreaRenderer = new TextAreaRenderer();
		cmodel.getColumn(0).setCellRenderer(textAreaRenderer);
		cmodel.getColumn(1).setCellRenderer(textAreaRenderer);
		table1.revalidate();
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
			tabbedPane1.setTitleAt(1, "<html>Translation Memory</html>");
		} else {
			for (String key : results.keySet()) {
				tableModel.addRow(new String[] { key, results.get(key) });
			}
			tabbedPane1.setTitleAt(1, "<html>Translation Memory<b><font color='red'>*</font></b></html>");
		}
		table2.setModel(tableModel);
		TableColumnModel cmodel = table2.getColumnModel();
		TextAreaRenderer textAreaRenderer = new TextAreaRenderer();
		cmodel.getColumn(0).setCellRenderer(textAreaRenderer);
		cmodel.getColumn(1).setCellRenderer(textAreaRenderer);
		table2.revalidate();
	}

	/**
	 * Update ui.
	 *
	 * @param translationProject the translation project
	 * @param workListMember the work list member
	 */
	public void updateUI(TranslationProject translationProject, WorkListMember workListMember) {
		// clearForm(true);
		try {
			this.translationProject = translationProject;
			translConfig = LanguageUtil.getTranslationConfig(Terms.get().getActiveAceFrameConfig());
			// if (translConfig.isProjectDefaultConfiguration())
			translConfig = getTranslationProjectConfig();
			this.concept = workListMember.getConcept();
			this.worklistMember = workListMember;
			sourceLangRefsets = new HashSet<LanguageMembershipRefset>();
			sourceIds = new ArrayList<Integer>();
			targetId = -1;
			I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
			List<I_GetConceptData> sourceLangConcepts = translationProject.getSourceLanguageRefsets();
			if (sourceLangConcepts != null) {
				for (I_GetConceptData sourceLangConcept : sourceLangConcepts) {
					sourceLangRefsets.add(new LanguageMembershipRefset(sourceLangConcept, config));
					sourceIds.add(sourceLangConcept.getConceptNid());
				}
			}
			I_GetConceptData targetLangConcept = translationProject.getTargetLanguageRefset();
			if (targetLangConcept != null) {
				targetLangRefset = new LanguageMembershipRefset(targetLangConcept, config);
				targetId = targetLangConcept.getConceptNid();
			}
			populateSourceTree();
			populateTargetTree();
			populateDetailsTree();
			hierarchyNavigator1.setFocusConcept(Ts.get().getConceptVersion(config.getViewCoordinate(), concept.getConceptNid()));

			getPreviousComments();
			getWebReferences();
			if (translationProject.getProjectIssueRepo() != null) {
				// Object
				// flag=config.getDbConfig().getProperty(TranslationHelperPanel.EXTERNAL_ISSUES_ACTIVE_PROPERTY_FLAG);
				// if (flag!=null && flag.toString().equals("true")){
				tabbedPane2.setTitleAt(2, "<html>Issues</html>");
				Thread appthr = new Thread() {
					synchronized public void run() {
						SwingUtilities.invokeLater(new Runnable() {
							synchronized @Override
							public void run() {
								loadIssues();

							}

						});
					}

				};
				appthr.start();
			} else {
				tabbedPane2.setTitleAt(2, "<html>Issues <font><style size=1>(Inactive)</style></font></html>");

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * Gets the web references.
	 *
	 * @return the web references
	 */
	private void getWebReferences() {
		I_ConfigAceFrame config;
		try {
			config = Terms.get().getActiveAceFrameConfig();
			HashMap<URL, String>urls=new HashMap<URL, String>();

			StringBuffer sb=new StringBuffer("");
			sb.append("<html><body>");
			int urlCount=0;
			if (targetLangRefset!=null){
				urls=targetLangRefset.getCommentsRefset(config).getUrls(this.concept.getConceptNid());	
				urlCount=urls.size();
				for (URL url:urls.keySet()) {
					sb.append("<a href=\"");
					sb.append( url.toString() );
					sb.append("\">");
					sb.append(url.toString());
					sb.append("</a><br>");
				}
			}
			urls=TerminologyProjectDAO.getWorkList(Terms.get().getConcept(worklistMember.getWorkListUUID()), config).getCommentsRefset(config).getUrls(this.concept.getConceptNid());

			urlCount+=urls.size();
			for (URL url:urls.keySet()) {
				sb.append("<a href=\"");
				sb.append( url.toString() );
				sb.append("\">");
				sb.append(url.toString());
				sb.append("</a><br>");
			}
			sb.append("</body></html>");
			
			refTable.setText(sb.toString());
			if (urlCount>0){
				tabbedPane2.setTitleAt(1, "<html>Web references <b><font color='red'>(" + urlCount + ")</font></b></html>");
			}else {
				tabbedPane2.setTitleAt(1, "<html>Web references (0)</font></b></html>");
			}
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * Load issues.
	 */
	protected void loadIssues() {
		if (issueListPanel==null){
			createIssuePanel();
		}
		if (issueListPanel==null){
			return;
		}

		I_ConfigAceFrame config;
		try {
			config = Terms.get().getActiveAceFrameConfig();
			IssueRepository repo= IssueRepositoryDAO.getIssueRepository(translationProject.getProjectIssueRepo()); 
			IssueRepoRegistration regis;
			regis=IssueRepositoryDAO.getRepositoryRegistration(repo.getUuid(), config);
			if (regis!=null && regis.getUserId()!= null && regis.getPassword()!=null){
				//issueListPanel.loadIssues(concept,repo,regis);
			}
		} catch (TerminologyException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}

	/**
	 * Creates the issue panel.
	 */
	private void createIssuePanel() {
		I_ConfigAceFrame config;
		try {
			config = Terms.get().getActiveAceFrameConfig();
			if (config == null)
				issueListPanel = new IssuesListPanel2(false);
			else
				issueListPanel = new IssuesListPanel2(config,false);

			panel11.add(issueListPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Clear comments.
	 */
	private void clearComments() {
		String[] columnNames = { "Comment" };
		String[][] data = null;
		DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int x, int y) {
				return false;
			}
		};

		tblComm.setModel(tableModel);
		tblComm.revalidate();
		tabbedPane2.setTitleAt(0, "<html>Comments</font></b></html>");
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
		table1.setModel(tableModel);
		table1.revalidate();
	}

	/**
	 * Clear ling guidelines.
	 */
	private void clearLingGuidelines() {
		tabbedPane1.setTitleAt(2, "<html>Linguistic Guidelines</html>");
		editorPane1.setText("");
		editorPane1.revalidate();
	}

	/**
	 * Clear trans memory.
	 */
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
		tabbedPane1.setTitleAt(1, "<html>Translation Memory</html>");
		table2.revalidate();
	}

	/**
	 * Gets the previous comments.
	 *
	 * @return the previous comments
	 */
	private void getPreviousComments() {
		I_ConfigAceFrame config;
		try {
			List<Comment> commentsList = new ArrayList<Comment>();
			config = Terms.get().getActiveAceFrameConfig();
			String[] columnNames = { "Comment type", "Comment" };
			String[][] data = null;
			DefaultTableModel tableModel = new DefaultTableModel(data, columnNames) {
				private static final long serialVersionUID = 1L;

				public boolean isCellEditable(int x, int y) {
					return false;
				}
			};
			

			if (targetLangRefset != null) {
				commentsList = targetLangRefset.getCommentsRefset(config).getFullComments(concept.getConceptNid());
				for (int i = commentsList.size() - 1; i > -1; i--) {
					if (commentsList.get(i).getTypeCid() == commentsList.get(i).getSubTypeCid()) {
						tableModel.addRow(new Object[] {"Language refset: " +  Terms.get().getConcept(commentsList.get(i).getTypeCid()) + "", formatComment(commentsList.get(i).getComment()) });
					} else {
						tableModel.addRow(new Object[] {"Language refset: " + Terms.get().getConcept(commentsList.get(i).getTypeCid()) + "/" + Terms.get().getConcept(commentsList.get(i).getSubTypeCid()),
								formatComment(commentsList.get(i).getComment()) });
					}
				}
			}

			commentsList = TerminologyProjectDAO.getWorkList(Terms.get().getConcept(worklistMember.getWorkListUUID()), config).getCommentsRefset(config).getFullComments(
					this.concept.getConceptNid());

			for (int i = commentsList.size() - 1; i > -1; i--) {
				if (commentsList.get(i).getTypeCid() == commentsList.get(i).getSubTypeCid()) {
					tableModel.addRow(new Object[] {"Worklist: " + Terms.get().getConcept(commentsList.get(i).getTypeCid()) + "", formatComment(commentsList.get(i).getComment()) });
				} else {
					tableModel.addRow(new Object[] {"Worklist: " + Terms.get().getConcept(commentsList.get(i).getTypeCid()) + "/" + Terms.get().getConcept(commentsList.get(i).getSubTypeCid()),
							formatComment(commentsList.get(i).getComment()) });
				}
			}

			tblComm.setModel(tableModel);
			TableColumnModel cmodel = tblComm.getColumnModel();
			cmodel.getColumn(0).setMinWidth(120);
			cmodel.getColumn(0).setMaxWidth(145);
			EditorPaneRenderer textAreaRenderer = new EditorPaneRenderer();
			cmodel.getColumn(0).setCellRenderer(textAreaRenderer);
			cmodel.getColumn(1).setCellRenderer(textAreaRenderer);
			tblComm.setRowHeight(65);
			tblComm.revalidate();
			if (tblComm.getRowCount() > 0) {
				tabbedPane2.setTitleAt(0, "<html>Comments <b><font color='red'>(" + tblComm.getRowCount() + ")</font></b></html>");
			} else {
				tabbedPane2.setTitleAt(0, "<html>Comments (0)</font></b></html>");
			}
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 * Format comment.
	 *
	 * @param comment the comment
	 * @return the string
	 */
	private String formatComment(String comment) {
		long thickVer;
		thickVer = Long.parseLong(comment.substring(comment.trim().lastIndexOf(" ") + 1));
		String strDate = formatter.format(thickVer);
		String tmp = comment.substring(0, comment.lastIndexOf(" - Time:"));
		if (tmp.indexOf(COMMENT_HEADER_SEP) > -1) {
			tmp = tmp.replace(COMMENT_HEADER_SEP, endP + COMMENT_HEADER_SEP) + htmlFooter;
			return htmlHeader + "<I>" + strDate + "</I>" + HEADER_SEPARATOR + tmp;
		}
		return htmlHeader + "<I>" + strDate + "</I>" + COMMENT_HEADER_SEP + tmp;

	}

	/**
	 * Sets the button mnemo.
	 *
	 * @param btton the new button mnemo
	 */
	private void setButtonMnemo(Component btton) {
		if (btton instanceof JButton) {
			String buttName = ((JButton) btton).getText();
			for (int i = 0; i < buttName.length(); i++) {
				String MnemChar = buttName.substring(i, i + 1).toUpperCase();
				if (!MnemChar.equals(" ") && assignedMnemo.indexOf(MnemChar) < 0) {
					assignedMnemo = assignedMnemo + MnemChar;
					((JButton) btton).setMnemonic(MnemChar.charAt(0));
					break;
				}
			}
		}
	}

	/**
	 * Update glossary enforcement.
	 * 
	 * @param query
	 *            the query
	 */
	private void updateGlossaryEnforcement(String query) {

//		try {
//			//String results = LanguageUtil.getLinguisticGuidelines(concept);
//			if (!results.isEmpty()) {
//				tabbedPane1.setTitleAt(2, "<html>Linguistic Guidelines<b><font color='red'>*</font></b></html>");
//			} else {
//				tabbedPane1.setTitleAt(2, "<html>Linguistic Guidelines</html>");
//			}
//			editorPane1.setText(results);
//			editorPane1.revalidate();
//		} catch (TerminologyException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

	/**
	 * Gets the source lang refsets.
	 *
	 * @return the source lang refsets
	 */
	public Set<LanguageMembershipRefset> getSourceLangRefsets() {
		return sourceLangRefsets;
	}

	/**
	 * Sets the source lang refsets.
	 *
	 * @param sourceLangRefsets the new source lang refsets
	 */
	public void setSourceLangRefsets(Set<LanguageMembershipRefset> sourceLangRefsets) {
		this.sourceLangRefsets = sourceLangRefsets;
		sourceIds = new ArrayList<Integer>();
		for (LanguageMembershipRefset sourceRef : sourceLangRefsets) {
			sourceIds.add(sourceRef.getRefsetId());
		}
	}

	/**
	 * Gets the target lang refset.
	 *
	 * @return the target lang refset
	 */
	public LanguageMembershipRefset getTargetLangRefset() {
		return targetLangRefset;
	}

	/**
	 * Sets the target lang refset.
	 *
	 * @param targetLangRefset the new target lang refset
	 */
	public void setTargetLangRefset(LanguageMembershipRefset targetLangRefset) {
		this.targetLangRefset = targetLangRefset;
		targetId = targetLangRefset.getRefsetId();
	}

	/**
	 * Autokeep in inbox.
	 */
	public void AutokeepInInbox() {
		if (this.keepIIClass != null) {
			this.unloaded = false;
			this.keepIIClass.KeepInInbox();
		}
	}

	/**
	 * Sets the auto keep function.
	 *
	 * @param thisAutoKeep the new auto keep function
	 */
	public void setAutoKeepFunction(I_KeepTaskInInbox thisAutoKeep) {
		this.keepIIClass = thisAutoKeep;

	}

	/**
	 * Sets the unloaded.
	 *
	 * @param b the new unloaded
	 */
	public void setUnloaded(boolean b) {
		this.unloaded = b;

	}

	/**
	 * Gets the unloaded.
	 *
	 * @return the unloaded
	 */
	public boolean getUnloaded() {
		return this.unloaded;
	}
}
