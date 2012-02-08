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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.ScrollPaneConstants;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.config.AceFrame;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.ace.task.commit.AlertToDataConstraintFailure;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.LogWithAlerts;
import org.ihtsdo.db.bdb.BdbCommitManager;
import org.ihtsdo.document.DocumentManager;
import org.ihtsdo.document.DocumentsSearchPanel;
import org.ihtsdo.issue.IssueRepoRegistration;
import org.ihtsdo.issue.issuerepository.IssueRepository;
import org.ihtsdo.issue.manager.IssueRepositoryDAO;
import org.ihtsdo.project.ContextualizedDescription;
import org.ihtsdo.project.I_ContextualizeDescription;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.help.HelpApi;
import org.ihtsdo.project.issue.manager.IssuesListPanel2;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.model.WorkListMember;
import org.ihtsdo.project.panel.PanelHelperFactory;
import org.ihtsdo.project.panel.TranslationHelperPanel;
import org.ihtsdo.project.panel.details.WorklistMemberLogPanel;
import org.ihtsdo.project.refset.Comment;
import org.ihtsdo.project.refset.CommentsRefset;
import org.ihtsdo.project.refset.LanguageMembershipRefset;
import org.ihtsdo.project.util.IconUtilities;
import org.ihtsdo.tk.api.RelAssertionType;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.translation.FSNGenerationException;
import org.ihtsdo.translation.LanguageUtil;
import org.ihtsdo.translation.TreeEditorObjectWrapper;
import org.ihtsdo.translation.ui.ConfigTranslationModule.EditingPanelOpenMode;
import org.ihtsdo.translation.ui.ConfigTranslationModule.EditorMode;
import org.ihtsdo.translation.ui.ConfigTranslationModule.TreeComponent;
import org.ihtsdo.translation.ui.translation.CommentPanel;
import org.ihtsdo.translation.ui.translation.NewCommentPanel;

/**
 * The Class TranslationConceptEditor6.
 */
public class TranslationConceptEditor6 extends JPanel {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	
	/** The Constant HEADER_SEPARATOR. */
	private static final String HEADER_SEPARATOR = " // ";
	
	/** The Constant COMMENT_HEADER_SEP. */
	private static final String COMMENT_HEADER_SEP = ": -";
	
	/** The Constant REFSET_COMMENT_NAME. */
	private static final Object REFSET_COMMENT_NAME = "Language comment";
	
	/** The Constant WORKLIST_COMMENT_NAME. */
	private static final Object WORKLIST_COMMENT_NAME = "Worklist comment";
	
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
	
	/** The read only mode. */
	private boolean readOnlyMode;
	
	/** The save desc. */
	private boolean saveDesc;
	
	/** The button5. */
	private JButton button5;
	
	/** The update ui thread. */
	private Thread updateUIThread;
	
	/** The already verified. */
	private boolean alreadyVerified;
	
	/** The Snomed_ isa. */
	private I_GetConceptData Snomed_Isa;
	
	/** The inferred. */
	private int inferred;

	/**
	 * Instantiates a new translation concept editor.
	 *
	 */
	public TranslationConceptEditor6() {
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
		cmbTarComm.addItem(REFSET_COMMENT_NAME);
		cmbTarComm.addItem(WORKLIST_COMMENT_NAME);

		label10.setIcon(IconUtilities.helpIcon);
		label10.setText("");
		label12.setIcon(IconUtilities.helpIcon);
		label12.setText("");
		label13.setIcon(IconUtilities.helpIcon);
		label13.setText("");
		label17.setIcon(IconUtilities.helpIcon);
		label17.setText("");

		tblComm.setSelectionBackground(Color.YELLOW);
		formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");

		// bDescIssue.setEnabled(false);
		DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel();
		comboBoxModel.addElement(fsn);
		comboBoxModel.addElement(synonym);

		comboBox1.setModel(comboBoxModel);
		comboBox1.setSelectedIndex(1);
		comboBox1.setEnabled(false);

		DefaultComboBoxModel comboBoxModel2 = new DefaultComboBoxModel();
		comboBoxModel2.addElement(preferred);
		comboBoxModel2.addElement(acceptable);

		cmbAccep.setModel(comboBoxModel2);
		cmbAccep.setSelectedIndex(1);
		cmbAccep.setEnabled(false);

		tree3.setCellRenderer(new DetailsIconRenderer());
		tree3.setRootVisible(true);
		tree3.setShowsRootHandles(false);

		setByCode = false;
		saveDesc = false;
		alreadyVerified = false;
		mSpellChk.setEnabled(false);
		mAddDesc.setEnabled(true && !readOnlyMode);
		mAddPref.setEnabled(true && !readOnlyMode);
		bKeep.setEnabled(false);
		bReview.setEnabled(false);
		bEscalate.setEnabled(false);
		label4.setVisible(false);

		tabSou.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tabTar.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tabSou.setModel(new DefaultTableModel());
		tabTar.setModel(new DefaultTableModel());
		tabTar.getSelectionModel().addListSelectionListener(new SelectionListener(tabTar));
		tabTar.setUpdateSelectionOnSort(false);
		tabSou.setUpdateSelectionOnSort(false);
		tabSou.addMouseListener(new SourceTableMouselistener());
		DefaultMutableTreeNode detailsRoot = new DefaultMutableTreeNode();
		tree3.setModel(new DefaultTreeModel(detailsRoot));

		ToolTipManager.sharedInstance().registerComponent(tree3);
		createIssuePanel();
		setMnemoInit();

		refTable.setContentType("text/html");
		refTable.setEditable(false);
		refTable.setOpaque(false);
		// populateTree();
		splitPane4.setResizeWeight(.4d);

		Dimension dimension = new Dimension(650, 350);
		termZoomDialog.setMaximumSize(dimension);
		termZoomDialog.setMinimumSize(dimension);
		termZoomDialog.setSize(dimension);
	}

	/**
	 * The Class SourceTableMouselistener.
	 */
	public class SourceTableMouselistener extends MouseAdapter {
		
		/** The menu. */
		private JPopupMenu menu;
		
		/** The m item. */
		private JMenuItem mItem;
		
		/** The m itema. */
		private JMenuItem mItema;
		
		/** The m item listener. */
		private MenuItemListener mItemListener;
		
		/** The x point. */
		private int xPoint;
		
		/** The y point. */
		private int yPoint;

		/**
		 * Instantiates a new source table mouselistener.
		 */
		SourceTableMouselistener() {
			mItemListener = new MenuItemListener();

			getMenu();
		}

		/**
		 * Gets the menu.
		 *
		 * @return the menu
		 */
		private void getMenu() {

			menu = new JPopupMenu();
			mItem = new JMenuItem();
			mItem.setText("Send as preferred");
			mItem.setActionCommand("Send as preferred");
			mItem.addActionListener(mItemListener);
			menu.add(mItem);
			mItema = new JMenuItem();
			mItema.setText("Send as acceptable");
			mItema.setActionCommand("Send as acceptable");
			mItema.addActionListener(mItemListener);
			menu.add(mItema);

		}

		/* (non-Javadoc)
		 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseClicked(MouseEvent e) {

			if (e.getButton() == e.BUTTON3) {

				xPoint = e.getX();
				yPoint = e.getY();
				int row = tabSou.rowAtPoint(new Point(xPoint, yPoint));
				if (row > -1) {
					int rowModel = tabSou.convertRowIndexToModel(row);
					DefaultTableModel model = (DefaultTableModel) tabSou.getModel();

					ContextualizedDescription description = (ContextualizedDescription) model.getValueAt(rowModel, TableSourceColumn.TERM.ordinal());

					if (description.getDescriptionStatusId() == inactive.getConceptNid()) {
						return;
					}
					mItemListener.setItem(description);
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							menu.show(tabSou, xPoint, yPoint);
						}
					});
				}
			}
		}
	}

	/**
	 * The listener interface for receiving menuItem events.
	 * The class that is interested in processing a menuItem
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addMenuItemListener<code> method. When
	 * the menuItem event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see MenuItemEvent
	 */
	class MenuItemListener implements ActionListener {

		/** The cont description. */
		private ContextualizedDescription contDescription;
		
		/** The acc event. */
		private ActionEvent accEvent;

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			if (contDescription != null) {
				this.accEvent = e;
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						try {
							if (accEvent.getActionCommand().equals("Send as preferred")) {
								contDescription.contextualizeThisDescription(targetLangRefset.getRefsetId(), preferred.getConceptNid());
								//Terms.get().commit();
							}
							if (accEvent.getActionCommand().equals("Send as acceptable")) {
								contDescription.contextualizeThisDescription(targetLangRefset.getRefsetId(), acceptable.getConceptNid());
								//Terms.get().commit();
							}

							try {
								populateTargetTree();
							} catch (Exception e1) {

								e1.printStackTrace();
							}
						} catch (TerminologyException e) {

							e.printStackTrace();
						} catch (IOException e) {

							e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						}

					}
				});
			}

		}

		/**
		 * Sets the item.
		 *
		 * @param contDescription the new item
		 */
		public void setItem(ContextualizedDescription contDescription) {
			this.contDescription = contDescription;
		}

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
	 * Gets the translation project config.
	 *
	 * @return the translation project config
	 */
	private ConfigTranslationModule getTranslationProjectConfig() {
		ConfigTranslationModule translProjConfig = null;
		if (this.translationProject != null) {
			translProjConfig = LanguageUtil.getDefaultTranslationConfig(this.translationProject);
		}

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
		assignedMnemo = "FPDYGKAXCRNLOIV";
	}

	/**
	 * Unload data.
	 */
	public void unloadData() {
		// ---- label14 ----
		label14.setText("S:-");
		label14.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
		panel1.add(label14, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

		// ---- label15 ----
		label15.setText("TM:-");
		label15.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
		panel1.add(label15, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

		// ---- label16 ----
		label16.setText("LG:-");
		label16.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
		panel1.add(label16, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

		if (updateUIThread != null && updateUIThread.isAlive()) {
			updateUIThread.interrupt();
		}
		verifySavePending(null, false);
		clearForm(true);
	}

	/**
	 * Verifies changes to target description.
	 *
	 * @param message <\T> if null
	 * @param doVerify the do verify
	 * @return true, if successful
	 */
	synchronized public boolean verifySavePending(String message, boolean doVerify) {
		boolean bPendTerm = true;
		if (saveDesc) {
			if (doVerify) {
				if (targetTextField.getText().equals("") && tabTar.getRowCount() <= 0 && !alreadyVerified && message == null) {
					alreadyVerified = true;
					Object[] options = { "Send empty translation", "Cancel" };
					int n = JOptionPane.showOptionDialog(null, "There is no translation in target language, would you like to continue?", "Unsaved data", JOptionPane.YES_NO_OPTION,
							JOptionPane.WARNING_MESSAGE, null, options, options[1]);
					bPendTerm = false;
					if (n == 1) {
						if (Terms.get().getUncommitted().size() > 0) {
							try {
								Terms.get().cancel();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						descriptionInEditor = null;
						targetTextField.setText("");
						alreadyVerified = false;
						return false;
					} else if (!bPendTerm) {
						if (saveDescActionPerformed()) {

							descriptionInEditor = null;
							targetTextField.setText("");
							return true;
						} else {
							return false;
						}

					}
				} else {
					alreadyVerified = true;
				}
			}
			if (descriptionInEditor != null) {
				if (!(descriptionInEditor.getText().trim().equals(targetTextField.getText().trim())
						&& (descriptionInEditor.isInitialCaseSignificant() == rbYes.isSelected())
						&& descriptionInEditor.getAcceptabilityId() == ((I_GetConceptData) cmbAccep.getSelectedItem()).getConceptNid()
						&& ((descriptionInEditor.getExtensionStatusId() == active.getConceptNid() && rbAct.isSelected()) || (descriptionInEditor.getExtensionStatusId() != active.getConceptNid() && !rbAct
								.isSelected())) && ((descriptionInEditor.getTypeId() == fsn.getConceptNid() && fsn.equals((I_GetConceptData) comboBox1.getSelectedItem())) || (descriptionInEditor
						.getTypeId() != fsn.getConceptNid() && !fsn.equals((I_GetConceptData) comboBox1.getSelectedItem()))))) {
					bPendTerm = false;
				}
			} else {
				if (!targetTextField.getText().trim().equals("")) {
					bPendTerm = false;
				}
			}
			if (!bPendTerm) {

				Object[] options = { "Discard unsaved data", "Save" };
				String message1 = "Do you want to save the change you made to the term in the editor panel?";
				if (message != null) {
					message1 = message;
				}
				int n = JOptionPane.showOptionDialog(null, message1, "Unsaved data", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, // do
						// not
						// use
						// a
						// custom Icon
						options, // the titles of buttons
						options[1]); // default button title
				if (n == 0) {
					if (Terms.get().getUncommitted().size() > 0) {
						try {
							Terms.get().cancel();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					descriptionInEditor = null;
					targetTextField.setText("");
					return true;
				} else if (!bPendTerm) {
					if (saveDescActionPerformed()) {

						descriptionInEditor = null;
						targetTextField.setText("");
						return true;
					} else {
						return false;
					}

				}
			}
			Collection<Collection<AlertToDataConstraintFailure>> values = BdbCommitManager.getDatacheckMap().values();
			for (Collection<AlertToDataConstraintFailure> collection : values) {
				if(!collection.isEmpty()){
					JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null), 
							"There are uncommitted changes - please cancel or commit before continuing.", 
							"", JOptionPane.ERROR_MESSAGE);
					return false;
				}
			}
		}
		return bPendTerm;
	}

	/**
	 * Gets the description in editor.
	 *
	 * @return the description in editor
	 */
	public ContextualizedDescription getDescriptionInEditor() {
		return descriptionInEditor;
	}

	/**
	 * Sets the read only mode.
	 *
	 * @param readOnly the new read only mode
	 */
	private void setReadOnlyMode(boolean readOnly) {
		this.readOnlyMode = readOnly;
		rbInact.setEnabled(true && !readOnlyMode);
		rbYes.setEnabled(true && !readOnlyMode);
	}

	/**
	 * Clear form.
	 *
	 * @param clearAll the clear all
	 */
	synchronized private void clearForm(boolean clearAll) {
		descriptionInEditor = null;
		comboBox1.setEnabled(false);
		cmbAccep.setEnabled(false);
		// label4.setVisible(true);
		// label4.setText("");
		targetTextField.setText("");
		targetTextField.setEnabled(false);
		rbYes.setSelected(false);
		panel2.revalidate();
		mSpellChk.setEnabled(false);
		// button5.setEnabled(false);
		// mAddDesc.setEnabled(true);
		// mAddPref.setEnabled(true);
		// mClose.setEnabled(true);
		// rbYes.setEnabled(false);
		// rbInact.setEnabled(false);
		// rbNo.setEnabled(false);
		// rbAct.setEnabled(false);
		tabTar.clearSelection();

		if (clearAll) {
			saveDesc = false;
			// DefaultMutableTreeNode root=new DefaultMutableTreeNode();
			// tree1.setModel(new DefaultTreeModel(root));
			// DefaultMutableTreeNode root2=new DefaultMutableTreeNode();
			// tree2.setModel(new DefaultTreeModel(root2));

			tabSou.setModel(new DefaultTableModel());
			tabTar.setModel(new DefaultTableModel());
			DefaultMutableTreeNode root3 = new DefaultMutableTreeNode();
			tree3.setModel(new DefaultTreeModel(root3));
			this.translationProject = null;
			this.concept = null;
			if (issueListPanel != null) {
				issueListPanel.loadIssues(null, null, null);
			}
			tabbedPane3.setSelectedIndex(0);
			clearComments();
			setMnemoInit();
		}
	}

	/**
	 * Retire action performed.
	 * 
	 * @param e
	 *            the e
	 */
	private void retireActionPerformed(ActionEvent e) {
		clearForm(false);
	}

	/**
	 * B add fsn action performed.
	 */
	private void bAddFSNActionPerformed() {
		try {
			ContextualizedDescription fsnDesc = null;
			I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
			try {
				fsnDesc = (ContextualizedDescription) LanguageUtil.generateFSN(concept, sourceLangRefsets.iterator().next(), targetLangRefset, translationProject, config);

			} catch (FSNGenerationException e1) {
				e1.printStackTrace();

				JOptionPane.showOptionDialog(this, e1.getMessage(), "Warning", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, null, null);
			}
			if (fsnDesc == null) {
				if (verifySavePending(null, false)) {
					descriptionInEditor = null;
					targetTextField.setText("");
					targetTextField.setEnabled(true && !readOnlyMode);
					panel2.revalidate();
					saveDesc = true;
					mSpellChk.setEnabled(true);
					// button5.setEnabled(true);
					// comboBox1.setEnabled(true);
					comboBox1.setSelectedItem(fsn);
					// cmbAccep.setEnabled(true);
					cmbAccep.setSelectedItem(preferred);
					// rbNo.setEnabled(true);
					// rbAct.setEnabled(true);
					// rbYes.setEnabled(true);
					// rbInact.setEnabled(true);
				}
			} else {
				populateTargetTree();
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
	 * Spellcheck action performed.
	 *
	 */
	private void mSpellChkActionPerformed() {
		AceFrameConfig config;
		try {
			config = (AceFrameConfig) Terms.get().getActiveAceFrameConfig();
			targetTextField.setText(DocumentManager.spellcheckPhrase(targetTextField.getText(), null, targetLangRefset.getLangCode(config)));
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	/**
	 * M add pref action performed.
	 */
	private void mAddPrefActionPerformed() {
		if (verifySavePending(null, false)) {
			descriptionInEditor = null;
			targetTextField.setText("");
			targetTextField.setEnabled(true && !readOnlyMode);
			panel2.revalidate();
			saveDesc = true;
			mSpellChk.setEnabled(true);
			comboBox1.setSelectedItem(synonym);
			cmbAccep.setSelectedItem(preferred);
		}
	}

	/**
	 * M add desc action performed.
	 */
	private void mAddDescActionPerformed() {
		if (verifySavePending(null, false)) {
			descriptionInEditor = null;
			targetTextField.setText("");
			targetTextField.setEnabled(true && !readOnlyMode);
			panel2.revalidate();
			saveDesc = true;
			mSpellChk.setEnabled(true);
			comboBox1.setSelectedItem(synonym);
			cmbAccep.setSelectedItem(acceptable);
		}
	}

	/**
	 * B keep action performed.
	 */
	private void bKeepActionPerformed() {
		clearForm(true);

		bKeep.setEnabled(false);
		bReview.setEnabled(false);
		bEscalate.setEnabled(false);
	}

	/**
	 * B review action performed.
	 */
	private void bReviewActionPerformed() {
		// clearAndRemove();
	}

	/**
	 * B escalate action performed.
	 */
	private void bEscalateActionPerformed() {
		// clearAndRemove();
	}

	/**
	 * Save comment.
	 *
	 * @param comment the comment
	 * @param commentType the comment type
	 * @param commentSubType the comment sub type
	 */
	private void saveComment(String comment, I_GetConceptData commentType, I_GetConceptData commentSubType) {
		I_ConfigAceFrame config = null;
		try {
			config = Terms.get().getActiveAceFrameConfig();
			String targetComm = (String) cmbTarComm.getSelectedItem();
			if (targetComm.equals(WORKLIST_COMMENT_NAME)) {
				String fullName = config.getDbConfig().getFullName();
				WorkList workList = TerminologyProjectDAO.getWorkList(Terms.get().getConcept(worklistMember.getWorkListUUID()), config);

				CommentsRefset commentsRefset = workList.getCommentsRefset(config);
				if (commentSubType != null) {
					commentsRefset.addComment(worklistMember.getId(), commentType.getConceptNid(), commentSubType.getConceptNid(),  role.toString() + HEADER_SEPARATOR + "<b>" + fullName + "</b>" + COMMENT_HEADER_SEP + comment);
				} else {
					commentsRefset.addComment(worklistMember.getId(), commentType.getConceptNid(),  role.toString() + HEADER_SEPARATOR + "<b>" + fullName + "</b>" + COMMENT_HEADER_SEP + comment);
				}

			} else {
				CommentsRefset commRefset = targetLangRefset.getCommentsRefset(config);
				String fullName = config.getDbConfig().getFullName();
				if (commentSubType != null) {
					commRefset.addComment(this.concept.getConceptNid(), commentType.getConceptNid(), commentSubType.getConceptNid(), role.toString() + HEADER_SEPARATOR + "<b>" + fullName + "</b>"
							+ COMMENT_HEADER_SEP + comment);
				} else {
					commRefset.addComment(this.concept.getConceptNid(), commentType.getConceptNid(), role.toString() + HEADER_SEPARATOR + "<b>" + fullName + "</b>" + COMMENT_HEADER_SEP + comment);
				}
			}
			// Terms.get().commit();

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
	 * Save desc action performed.
	 *
	 * @return true, if successful
	 */
	private boolean saveDescActionPerformed() {
		boolean result = true;
		I_ConfigAceFrame config = null;
		try {
			config = Terms.get().getActiveAceFrameConfig();
			ConfigTranslationModule confTransMod = LanguageUtil.getTranslationConfig(config);
			System.out.println(confTransMod.isEnableSpellChecker());
			if (confTransMod.isEnableSpellChecker()) {
				targetTextField.setText(DocumentManager.spellcheckPhrase(targetTextField.getText(), null, targetLangRefset.getLangCode(config)));
			}

			if (descriptionInEditor == null && !targetTextField.getText().trim().equals("") && rbAct.isSelected() ) {
				descriptionInEditor = (ContextualizedDescription) ContextualizedDescription.createNewContextualizedDescription(concept.getConceptNid(), targetId, targetLangRefset.getLangCode(config));

			}
			if (descriptionInEditor != null) {
				descriptionInEditor.setText(targetTextField.getText());
				descriptionInEditor.setInitialCaseSignificant(rbYes.isSelected());

				// set description type like RF1
				if (((I_GetConceptData) comboBox1.getSelectedItem()).equals(synonym)) {
					if ((((I_GetConceptData) cmbAccep.getSelectedItem()).equals(preferred))) {
						descriptionInEditor.setTypeId(synonym.getConceptNid());
					} else if ((((I_GetConceptData) cmbAccep.getSelectedItem()).equals(acceptable))) {
						descriptionInEditor.setTypeId(synonym.getConceptNid());
					}
				} else {
					descriptionInEditor.setTypeId(fsn.getConceptNid());
				}
				// if some is wrong then all to retire
				if  (rbInact.isSelected()) {
					descriptionInEditor.setExtensionStatusId(inactive.getConceptNid());
//					descriptionInEditor.setAcceptabilityId(notAcceptable.getConceptNid());

				} else {
					// TODO: Discuss how to handle retirement and re-activation
					// in liked descriptions form source language
					// if all current
					descriptionInEditor.setAcceptabilityId(((I_GetConceptData) cmbAccep.getSelectedItem()).getConceptNid());
					descriptionInEditor.setDescriptionStatusId(active.getConceptNid());
					descriptionInEditor.setExtensionStatusId(active.getConceptNid());
				}

				result = descriptionInEditor.persistChanges();
				try {
					 LanguageUtil.generateFSN(concept, sourceLangRefsets.iterator().next(), targetLangRefset, translationProject, config);

				} catch (FSNGenerationException e1) {
					e1.printStackTrace();

					JOptionPane.showOptionDialog(this, e1.getMessage(), "Warning", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, null, null);
				}
			}

			if (result)
				clearForm(false);

		} catch (IOException e1) {
			e1.printStackTrace();
			return false;
		} catch (TerminologyException e1) {
			e1.printStackTrace();
			return false;
		} catch (Exception e1) {
			e1.printStackTrace();
			return false;
		}

		try {
			populateTargetTree();
			// SwingUtilities.invokeLater(new Runnable() {
			// public void run() {
			// Timer timer = new Timer(1100, new setInboxPanelFocus());
			// timer.setRepeats(false);
			// timer.start();
			// }
			//
			// });
		} catch (Exception e1) {

			e1.printStackTrace();
		}
		return result;
	}

	/**
	 * M hist action performed.
	 */
	private void mHistActionPerformed() {
		org.ihtsdo.project.panel.TranslationHelperPanel thp;
		try {
			thp = PanelHelperFactory.getTranslationHelperPanel();
			JTabbedPane tp = thp.getTabbedPanel();
			if (tp != null) {
				I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
				IssueRepository repo = null;
				if (translationProject.getProjectIssueRepo() != null) {
					repo = IssueRepositoryDAO.getIssueRepository(translationProject.getProjectIssueRepo());
				}
				IssueRepoRegistration regis = null;
				WorklistMemberLogPanel wmlpanel = null;
				if (repo != null) {
					regis = IssueRepositoryDAO.getRepositoryRegistration(repo.getUuid(), config);
				}
				int tabCount = tp.getTabCount();
				for (int i = 0; i < tabCount; i++) {
					if (tp.getTitleAt(i).equals(TranslationHelperPanel.CONCEPT_VERSIONS_TAB_NAME)) {
						tp.setSelectedIndex(i);
						wmlpanel = (WorklistMemberLogPanel) tp.getComponentAt(i);
						wmlpanel.showMemberChanges(this.worklistMember, this.translationProject, repo, regis);
						thp.showTabbedPanel();
						return;
					}
				}
				wmlpanel = new WorklistMemberLogPanel();
				wmlpanel.showMemberChanges(this.worklistMember, this.translationProject, repo, regis);

				tp.addTab(TranslationHelperPanel.CONCEPT_VERSIONS_TAB_NAME, wmlpanel);
				tp.setSelectedIndex(tp.getTabCount() - 1);
				thp.showTabbedPanel();
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
	 * B add coment action performed.
	 */
	private void bAddComentActionPerformed() {
		showNewCommentPanel();
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
		saveComment(cPanel.getNewComment().trim(), cPanel.getCommentType(), cPanel.getCommentSubType());
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
		if (SwingUtilities.isLeftMouseButton(e)) {
			if (e.getClickCount() == 2) {
				viewComment();
			}
		}
		// Right mouse click
		else if (SwingUtilities.isRightMouseButton(e)) {
			// get the coordinates of the mouse click
			Point p = e.getPoint();

			// get the row index that contains that coordinate
			int rowNumber = tblComm.rowAtPoint(p);

			// Get the ListSelectionModel of the JTable
			ListSelectionModel model = tblComm.getSelectionModel();

			// set the selected interval of rows. Using the "rowNumber"
			// variable for the beginning and end selects only that one row.
			model.setSelectionInterval(rowNumber, rowNumber);
			popupMenu1.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	/**
	 * View comment.
	 */
	private void viewComment() {
		int row = tblComm.getSelectedRow();
		if (row > -1) {
			CommentPanel cp = new CommentPanel();
			String comm = (String) tblComm.getValueAt(row, 0) + "  -  " + tblComm.getValueAt(row, 1);
			String tmpDate=(String)tblComm.getValueAt(row, 1);
			String[] arrTmpDate=tmpDate.split(HEADER_SEPARATOR);
			String date = "";
			date=arrTmpDate[0];
			comm = comm.replace(htmlHeader, "");
			comm = comm.replace(htmlFooter, "");
			comm = comm.replace(endP, "");
			String[] arrComm = comm.split(COMMENT_HEADER_SEP);
			String header = arrComm[0];
			String[] headerComp = header.split(HEADER_SEPARATOR);
			String from = "";
			String role = "";
			String source = "";
			if (headerComp.length > 0) {
				source = headerComp[0];
				if (headerComp.length > 1) {
					role = headerComp[1];
					if (headerComp.length > 2) {
						from = headerComp[2];
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
	 * M log action performed.
	 */
	private void mLogActionPerformed() {
		try {
			TranslationWlstMemberLogPanel panel = getTranslMemberLogPanel();

			I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
			IssueRepository repo = null;
			if (translationProject.getProjectIssueRepo() != null) {
				repo = IssueRepositoryDAO.getIssueRepository(translationProject.getProjectIssueRepo());
			}
			IssueRepoRegistration regis = null;

			if (repo != null) {
				regis = IssueRepositoryDAO.getRepositoryRegistration(repo.getUuid(), config);
			}
			panel.showMemberChanges(this.worklistMember, this.translationProject, repo, regis);

		} catch (TerminologyException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	/**
	 * Gets the transl member log panel.
	 *
	 * @return the transl member log panel
	 */
	private TranslationWlstMemberLogPanel getTranslMemberLogPanel() {
		AceFrameConfig config;
		try {
			config = (AceFrameConfig) Terms.get().getActiveAceFrameConfig();

			AceFrame ace = config.getAceFrame();
			JTabbedPane tp = ace.getCdePanel().getLeftTabs();
			if (tp != null) {
				int tabCount = tp.getTabCount();
				for (int i = 0; i < tabCount; i++) {
					if (tp.getTitleAt(i).equals(TranslationHelperPanel.MEMBER_LOG_TAB_NAME)) {
						return (TranslationWlstMemberLogPanel) tp.getComponentAt(i);
					}
				}
				TranslationWlstMemberLogPanel uiPanel = new TranslationWlstMemberLogPanel();

				tp.addTab(TranslationHelperPanel.MEMBER_LOG_TAB_NAME, uiPanel);
				return uiPanel;
			}
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;

	}

	/**
	 * Checks if is member log open.
	 *
	 * @return true, if is member log open
	 */
	private boolean isMemberLogOpen() {
		AceFrameConfig config;
		try {
			config = (AceFrameConfig) Terms.get().getActiveAceFrameConfig();

			AceFrame ace = config.getAceFrame();
			JTabbedPane tp = ace.getCdePanel().getLeftTabs();
			if (tp != null) {
				int tabCount = tp.getTabCount();
				for (int i = 0; i < tabCount; i++) {
					if (tp.getTitleAt(i).equals(TranslationHelperPanel.MEMBER_LOG_TAB_NAME)) {
						return true;
					}
				}
			}
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;

	}

	/**
	 * Label10 mouse clicked.
	 *
	 * @param e the e
	 */
	private void label10MouseClicked(MouseEvent e) {
		try {
			HelpApi.openHelpForComponent("TRANSLATION_UI");
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Label12 mouse clicked.
	 *
	 * @param e the e
	 */
	private void label12MouseClicked(MouseEvent e) {
		try {
			HelpApi.openHelpForComponent("WORKFLOW_BUTTONS");
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Label13 mouse clicked.
	 *
	 * @param e the e
	 */
	private void label13MouseClicked(MouseEvent e) {
		try {
			HelpApi.openHelpForComponent("TRANSLATION_EDIT");
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Search documents action performed.
	 *
	 * @param e the e
	 */
	private void searchDocumentsActionPerformed(ActionEvent e) {
		try {
			TranslationHelperPanel thp = PanelHelperFactory.getTranslationHelperPanel();
			JTabbedPane tp = thp.getTabbedPanel();
			if (tp != null) {
				int tabCount = tp.getTabCount();
				for (int i = 0; i < tabCount; i++) {
					if (tp.getTitleAt(i).equals(TranslationHelperPanel.SEARCH_DOCS_TAB_NAME)) {
						tp.setSelectedIndex(i);
						thp.showTabbedPanel();
					}
				}
				JPanel panel = new JPanel();
				panel.setLayout(new BorderLayout());
				panel.add(new DocumentsSearchPanel(""), BorderLayout.CENTER);
				tp.addTab(TranslationHelperPanel.SEARCH_DOCS_TAB_NAME, panel);
				tp.setSelectedIndex(tp.getTabCount() - 1);
				thp.showTabbedPanel();
			}
		} catch (TerminologyException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Label17 mouse clicked.
	 *
	 * @param e the e
	 */
	private void label17MouseClicked(MouseEvent e) {
		try {
			HelpApi.openHelpForComponent("COMMENTS");
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Delete comment action performed.
	 *
	 * @param e the e
	 */
	private void deleteCommentActionPerformed(ActionEvent e) {
		Comment selectedComment = (Comment) tblComm.getModel().getValueAt(tblComm.getSelectedRow(), 1);
		if (selectedComment != null) {
			CommentsRefset.retireCommentsMember(selectedComment.getExtension());
		}
		getPreviousComments();
	}

	/**
	 * View comment action performed.
	 *
	 * @param e the e
	 */
	private void viewCommentActionPerformed(ActionEvent e) {
		viewComment();
	}

	/**
	 * Target text field mouse clicked.
	 *
	 * @param e the e
	 */
	private void targetTextFieldMouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2) {
			zoomTextArea.setText(targetTextField.getText());
			zoomTextArea.setEnabled(targetTextField.isEnabled());
			zoomTextArea.revalidate();
			zoomTextArea.repaint();
			termZoomDialog.setVisible(true);
			termZoomDialog.pack();
		}
	}

	/**
	 * Save zoom button action performed.
	 *
	 * @param e the e
	 */
	private void saveZoomButtonActionPerformed(ActionEvent e) {
		targetTextField.setText(zoomTextArea.getText());
		termZoomDialog.dispose();
		zoomTextArea.setText("");
	}

	/**
	 * Cancel zoom change action performed.
	 *
	 * @param e the e
	 */
	private void cancelZoomChangeActionPerformed(ActionEvent e) {
		termZoomDialog.dispose();
		zoomTextArea.setText("");
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
					System.out.println("************ getting descrpt");
					if (descrpt != null && !setByCode) {
						System.out.println("************ descrpt= " + descrpt.getText());
						updatePropertiesPanel(descrpt, rowModel);
					} else
						System.out.println("************  descrpt null");
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
		panel1 = new JPanel();
		menuBar1 = new JMenuBar();
		menu1 = new JMenu();
		bAddFSN = new JMenuItem();
		mAddPref = new JMenuItem();
		mAddDesc = new JMenuItem();
		menu3 = new JMenu();
		mSpellChk = new JMenuItem();
		menu2 = new JMenu();
		mHist = new JMenuItem();
		mLog = new JMenuItem();
		menu4 = new JMenu();
		menuItem1 = new JMenuItem();
		label14 = new JLabel();
		label15 = new JLabel();
		label16 = new JLabel();
		label10 = new JLabel();
		panel10 = new JPanel();
		splitPane2 = new JSplitPane();
		splitPane4 = new JSplitPane();
		panel9 = new JPanel();
		label9 = new JLabel();
		scrollPane1 = new JScrollPane();
		tabSou = new ZebraJTable();
		tabbedPane2 = new JTabbedPane();
		panel16 = new JPanel();
		panel17 = new JPanel();
		bAddComent = new JButton();
		cmbTarComm = new JComboBox();
		label17 = new JLabel();
		tabbedPane1 = new JTabbedPane();
		scrollPane9 = new JScrollPane();
		tblComm = new ZebraJTable();
		scrollPane8 = new JScrollPane();
		refTable = new JEditorPane();
		panel11 = new JPanel();
		splitPane1 = new JSplitPane();
		panel8 = new JPanel();
		label11 = new JLabel();
		scrollPane6 = new JScrollPane();
		tabTar = new ZebraJTable();
		panel2 = new JPanel();
		label2 = new JLabel();
		scrollPane5 = new JScrollPane();
		targetTextField = new JTextArea();
		label1 = new JLabel();
		panel7 = new JPanel();
		label4 = new JLabel();
		comboBox1 = new JComboBox();
		panel5 = new JPanel();
		label5 = new JLabel();
		cmbAccep = new JComboBox();
		label3 = new JLabel();
		panel4 = new JPanel();
		rbYes = new JRadioButton();
		label6 = new JLabel();
		rbNo = new JRadioButton();
		panel14 = new JPanel();
		label7 = new JLabel();
		rbAct = new JRadioButton();
		rbInact = new JRadioButton();
		panel3 = new JPanel();
		label13 = new JLabel();
		panel6 = new JPanel();
		buttonPanel = new JPanel();
		label12 = new JLabel();
		label8 = new JLabel();
		bKeep = new JButton();
		bReview = new JButton();
		bEscalate = new JButton();
		tabbedPane3 = new JTabbedPane();
		scrollPane7 = new JScrollPane();
		tree3 = new JTree();
		hierarchyNavigator1 = new HierarchyNavigator();
		popupMenu1 = new JPopupMenu();
		menuItem2 = new JMenuItem();
		menuItem3 = new JMenuItem();
		termZoomDialog = new JDialog();
		panel12 = new JPanel();
		scrollPane2 = new JScrollPane();
		zoomTextArea = new JTextArea();
		saveZoomButton = new JButton();
		cancelZoomChange = new JButton();

		//======== this ========
		setBackground(new Color(238, 238, 238));
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {35, 0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 1.0, 1.0E-4};

		//======== panel1 ========
		{
			panel1.setLayout(new GridBagLayout());
			((GridBagLayout)panel1.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0, 0};
			((GridBagLayout)panel1.getLayout()).rowHeights = new int[] {30, 0};
			((GridBagLayout)panel1.getLayout()).columnWeights = new double[] {1.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
			((GridBagLayout)panel1.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

			//======== menuBar1 ========
			{

				//======== menu1 ========
				{
					menu1.setText("E[d]it");
					menu1.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
					menu1.setMnemonic('D');

					//---- bAddFSN ----
					bAddFSN.setText("Add Concept FSN");
					bAddFSN.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
					bAddFSN.setMnemonic('F');
					bAddFSN.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()|KeyEvent.SHIFT_MASK));
					bAddFSN.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							bAddFSNActionPerformed();
						}
					});
					menu1.add(bAddFSN);

					//---- mAddPref ----
					mAddPref.setText("Add Concept Preferred");
					mAddPref.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
					mAddPref.setMnemonic('P');
					mAddPref.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()|KeyEvent.SHIFT_MASK));
					mAddPref.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							mAddPrefActionPerformed();
						}
					});
					menu1.add(mAddPref);

					//---- mAddDesc ----
					mAddDesc.setText("Add Concept Description");
					mAddDesc.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
					mAddDesc.setMnemonic('D');
					mAddDesc.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()|KeyEvent.SHIFT_MASK));
					mAddDesc.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							mAddDescActionPerformed();
						}
					});
					menu1.add(mAddDesc);
				}
				menuBar1.add(menu1);

				//======== menu3 ========
				{
					menu3.setText("[T]ools");
					menu3.setSelectedIcon(null);
					menu3.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
					menu3.setMnemonic('T');

					//---- mSpellChk ----
					mSpellChk.setText("Spellcheck");
					mSpellChk.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
					mSpellChk.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()|KeyEvent.SHIFT_MASK));
					mSpellChk.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							mSpellChkActionPerformed();
						}
					});
					menu3.add(mSpellChk);
				}
				menuBar1.add(menu3);

				//======== menu2 ========
				{
					menu2.setText("[V]iew");
					menu2.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
					menu2.setMnemonic('V');

					//---- mHist ----
					mHist.setText("History");
					mHist.setMnemonic('Y');
					mHist.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()|KeyEvent.SHIFT_MASK));
					mHist.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
					mHist.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							mHistActionPerformed();
						}
					});
					menu2.add(mHist);

					//---- mLog ----
					mLog.setText("Log");
					mLog.setMnemonic('G');
					mLog.setIcon(null);
					mLog.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()|KeyEvent.SHIFT_MASK));
					mLog.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
					mLog.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							mLogActionPerformed();
						}
					});
					menu2.add(mLog);
				}
				menuBar1.add(menu2);

				//======== menu4 ========
				{
					menu4.setText("[S]earch");
					menu4.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
					menu4.setMnemonic('S');

					//---- menuItem1 ----
					menuItem1.setText("Search Documents");
					menuItem1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()|KeyEvent.SHIFT_MASK));
					menuItem1.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
					menuItem1.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							searchDocumentsActionPerformed(e);
						}
					});
					menu4.add(menuItem1);
				}
				menuBar1.add(menu4);
			}
			panel1.add(menuBar1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- label14 ----
			label14.setText("S:-");
			label14.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			panel1.add(label14, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- label15 ----
			label15.setText("TM:-");
			label15.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			panel1.add(label15, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- label16 ----
			label16.setText("LG:-");
			label16.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			panel1.add(label16, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 5), 0, 0));

			//---- label10 ----
			label10.setText("text");
			label10.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					label10MouseClicked(e);
				}
			});
			panel1.add(label10, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 0), 0, 0));

		//======== panel10 ========
		{
			panel10.setLayout(new GridBagLayout());
			((GridBagLayout)panel10.getLayout()).columnWidths = new int[] {0, 0};
			((GridBagLayout)panel10.getLayout()).rowHeights = new int[] {0, 0};
			((GridBagLayout)panel10.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
			((GridBagLayout)panel10.getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

			//======== splitPane2 ========
			{
				splitPane2.setToolTipText("Drag to resize");
				splitPane2.setBackground(new Color(238, 238, 238));
				splitPane2.setResizeWeight(0.5);
				splitPane2.setOneTouchExpandable(true);

				//======== splitPane4 ========
				{
					splitPane4.setOrientation(JSplitPane.VERTICAL_SPLIT);
					splitPane4.setOneTouchExpandable(true);

					//======== panel9 ========
					{
						panel9.setBackground(new Color(238, 238, 238));
						panel9.setLayout(new GridBagLayout());
						((GridBagLayout)panel9.getLayout()).columnWidths = new int[] {0, 0};
						((GridBagLayout)panel9.getLayout()).rowHeights = new int[] {20, 90, 0};
						((GridBagLayout)panel9.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
						((GridBagLayout)panel9.getLayout()).rowWeights = new double[] {0.0, 1.0, 1.0E-4};

						//---- label9 ----
						label9.setText("Source Language");
						panel9.add(label9, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 0), 0, 0));

						//======== scrollPane1 ========
						{

							//---- tabSou ----
							tabSou.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
							tabSou.setBorder(LineBorder.createBlackLineBorder());
							tabSou.setAutoCreateRowSorter(true);
							scrollPane1.setViewportView(tabSou);
						}
						panel9.add(scrollPane1, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 0), 0, 0));
					}
					splitPane4.setTopComponent(panel9);

					//======== tabbedPane2 ========
					{

						//======== panel16 ========
						{
							panel16.setLayout(new GridBagLayout());
							((GridBagLayout)panel16.getLayout()).columnWidths = new int[] {0, 0};
							((GridBagLayout)panel16.getLayout()).rowHeights = new int[] {0, 0, 0};
							((GridBagLayout)panel16.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
							((GridBagLayout)panel16.getLayout()).rowWeights = new double[] {0.0, 1.0, 1.0E-4};

							//======== panel17 ========
							{
								panel17.setLayout(new GridBagLayout());
								((GridBagLayout)panel17.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
								((GridBagLayout)panel17.getLayout()).rowHeights = new int[] {0, 0};
								((GridBagLayout)panel17.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0, 1.0E-4};
								((GridBagLayout)panel17.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

								//---- bAddComent ----
								bAddComent.setText("[A]dd Comment");
								bAddComent.setMnemonic('A');
								bAddComent.addActionListener(new ActionListener() {
									@Override
									public void actionPerformed(ActionEvent e) {
										bAddComentActionPerformed();
									}
								});
								panel17.add(bAddComent, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
									GridBagConstraints.CENTER, GridBagConstraints.BOTH,
									new Insets(0, 0, 0, 5), 0, 0));
								panel17.add(cmbTarComm, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
									GridBagConstraints.CENTER, GridBagConstraints.BOTH,
									new Insets(0, 0, 0, 5), 0, 0));

								//---- label17 ----
								label17.setText("text");
								label17.addMouseListener(new MouseAdapter() {
									@Override
									public void mouseClicked(MouseEvent e) {
										label17MouseClicked(e);
									}
								});
								panel17.add(label17, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
									GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
									new Insets(0, 0, 0, 0), 0, 0));
							}
							panel16.add(panel17, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.BOTH,
								new Insets(0, 0, 5, 0), 0, 0));

							//======== tabbedPane1 ========
							{

								//======== scrollPane9 ========
								{

									//---- tblComm ----
									tblComm.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
									tblComm.addMouseListener(new MouseAdapter() {
										@Override
										public void mouseClicked(MouseEvent e) {
											tblCommMouseClicked(e);
										}
									});
									scrollPane9.setViewportView(tblComm);
								}
								tabbedPane1.addTab("Comments", scrollPane9);


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
								tabbedPane1.addTab("Web references", scrollPane8);

							}
							panel16.add(tabbedPane1, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.BOTH,
								new Insets(0, 0, 0, 0), 0, 0));
						}
						tabbedPane2.addTab("Comments", panel16);


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
						tabbedPane2.setMnemonicAt(1, 'U');
					}
					splitPane4.setBottomComponent(tabbedPane2);
				}
				splitPane2.setLeftComponent(splitPane4);

				//======== splitPane1 ========
				{
					splitPane1.setOrientation(JSplitPane.VERTICAL_SPLIT);
					splitPane1.setOneTouchExpandable(true);

					//======== panel8 ========
					{
						panel8.setBackground(new Color(238, 238, 238));
						panel8.setLayout(new GridBagLayout());
						((GridBagLayout)panel8.getLayout()).columnWidths = new int[] {0, 0};
						((GridBagLayout)panel8.getLayout()).rowHeights = new int[] {0, 105, 0, 34, 0};
						((GridBagLayout)panel8.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
						((GridBagLayout)panel8.getLayout()).rowWeights = new double[] {0.0, 1.0, 0.0, 0.0, 1.0E-4};

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
							tabTar.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
							tabTar.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
							tabTar.setBorder(LineBorder.createBlackLineBorder());
							scrollPane6.setViewportView(tabTar);
						}
						panel8.add(scrollPane6, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 0), 0, 0));

						//======== panel2 ========
						{
							panel2.setBorder(LineBorder.createBlackLineBorder());
							panel2.setBackground(new Color(238, 238, 238));
							panel2.setLayout(new GridBagLayout());
							((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 5, 0};
							((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {13, 26, 23, 23, 33, 0, 0, 0};
							((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {0.0, 0.0, 1.0, 1.0, 0.0, 1.0E-4};
							((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};

							//---- label2 ----
							label2.setText("Term:");
							panel2.add(label2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
								new Insets(0, 10, 5, 5), 0, 0));

							//======== scrollPane5 ========
							{

								//---- targetTextField ----
								targetTextField.setRows(2);
								targetTextField.setLineWrap(true);
								targetTextField.addMouseListener(new MouseAdapter() {
									@Override
									public void mouseClicked(MouseEvent e) {
										targetTextFieldMouseClicked(e);
									}
								});
								scrollPane5.setViewportView(targetTextField);
							}
							panel2.add(scrollPane5, new GridBagConstraints(1, 1, 3, 3, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.BOTH,
								new Insets(0, 0, 5, 5), 0, 0));

							//---- label1 ----
							label1.setText("Term Type:");
							panel2.add(label1, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
								new Insets(0, 10, 5, 5), 0, 0));

							//======== panel7 ========
							{
								panel7.setBackground(new Color(238, 238, 238));
								panel7.setLayout(new FlowLayout(FlowLayout.LEFT));
								panel7.add(label4);
								panel7.add(comboBox1);
							}
							panel2.add(panel7, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
								new Insets(0, 0, 5, 5), 0, 0));

							//======== panel5 ========
							{
								panel5.setBackground(new Color(238, 238, 238));
								panel5.setLayout(new GridBagLayout());
								((GridBagLayout)panel5.getLayout()).columnWidths = new int[] {15, 0, 0, 0};
								((GridBagLayout)panel5.getLayout()).rowHeights = new int[] {0, 0};
								((GridBagLayout)panel5.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
								((GridBagLayout)panel5.getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

								//---- label5 ----
								label5.setText("Acceptability:");
								panel5.add(label5, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
									GridBagConstraints.CENTER, GridBagConstraints.BOTH,
									new Insets(0, 0, 0, 5), 0, 0));
								panel5.add(cmbAccep, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
									GridBagConstraints.CENTER, GridBagConstraints.BOTH,
									new Insets(0, 0, 0, 0), 0, 0));
							}
							panel2.add(panel5, new GridBagConstraints(3, 4, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
								new Insets(0, 0, 5, 5), 0, 0));

							//---- label3 ----
							label3.setText("Is case significant ?");
							panel2.add(label3, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
								new Insets(0, 10, 5, 5), 0, 0));

							//======== panel4 ========
							{
								panel4.setBackground(new Color(238, 238, 238));
								panel4.setLayout(new GridBagLayout());
								((GridBagLayout)panel4.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0, 0};
								((GridBagLayout)panel4.getLayout()).rowHeights = new int[] {0, 0};
								((GridBagLayout)panel4.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
								((GridBagLayout)panel4.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

								//---- rbYes ----
								rbYes.setText("Yes");
								rbYes.setBackground(new Color(238, 238, 238));
								panel4.add(rbYes, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
									GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
									new Insets(0, 0, 0, 5), 0, 0));

								//---- label6 ----
								label6.setText("    ");
								panel4.add(label6, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
									GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
									new Insets(0, 0, 0, 5), 0, 0));

								//---- rbNo ----
								rbNo.setSelected(true);
								rbNo.setText("No");
								rbNo.setBackground(new Color(238, 238, 238));
								panel4.add(rbNo, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
									GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
									new Insets(0, 0, 0, 0), 0, 0));
							}
							panel2.add(panel4, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
								new Insets(0, 0, 5, 5), 0, 0));

							//======== panel14 ========
							{
								panel14.setBackground(new Color(238, 238, 238));
								panel14.setLayout(new GridBagLayout());
								((GridBagLayout)panel14.getLayout()).columnWidths = new int[] {15, 0, 0, 0, 0, 0, 0};
								((GridBagLayout)panel14.getLayout()).rowHeights = new int[] {0, 0};
								((GridBagLayout)panel14.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
								((GridBagLayout)panel14.getLayout()).rowWeights = new double[] {1.0, 1.0E-4};

								//---- label7 ----
								label7.setText("Status:");
								panel14.add(label7, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
									GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
									new Insets(0, 0, 0, 5), 0, 0));

								//---- rbAct ----
								rbAct.setText("Active");
								rbAct.setSelected(true);
								rbAct.setBackground(new Color(238, 238, 238));
								panel14.add(rbAct, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
									GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
									new Insets(0, 0, 0, 5), 0, 0));

								//---- rbInact ----
								rbInact.setText("Inactive");
								rbInact.setBackground(new Color(238, 238, 238));
								panel14.add(rbInact, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0,
									GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
									new Insets(0, 0, 0, 0), 0, 0));
							}
							panel2.add(panel14, new GridBagConstraints(3, 5, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
								new Insets(0, 0, 5, 5), 0, 0));

							//======== panel3 ========
							{
								panel3.setBackground(new Color(238, 238, 238));
								panel3.setLayout(new GridBagLayout());
								((GridBagLayout)panel3.getLayout()).columnWidths = new int[] {0, 0, 0, 0};
								((GridBagLayout)panel3.getLayout()).rowHeights = new int[] {0, 0};
								((GridBagLayout)panel3.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};
								((GridBagLayout)panel3.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

								//---- label13 ----
								label13.setText("text");
								label13.addMouseListener(new MouseAdapter() {
									@Override
									public void mouseClicked(MouseEvent e) {
										label13MouseClicked(e);
									}
								});
								panel3.add(label13, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
									GridBagConstraints.CENTER, GridBagConstraints.BOTH,
									new Insets(0, 0, 0, 0), 0, 0));
							}
							panel2.add(panel3, new GridBagConstraints(3, 6, 1, 1, 0.0, 0.0,
								GridBagConstraints.EAST, GridBagConstraints.NONE,
								new Insets(0, 0, 0, 5), 0, 0));
						}
						panel8.add(panel2, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 0), 0, 0));

						//======== panel6 ========
						{
							panel6.setBorder(LineBorder.createBlackLineBorder());
							panel6.setLayout(new BoxLayout(panel6, BoxLayout.Y_AXIS));

							//======== buttonPanel ========
							{
								buttonPanel.setBackground(new Color(238, 238, 238));
								buttonPanel.setLayout(new GridBagLayout());
								((GridBagLayout)buttonPanel.getLayout()).columnWidths = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0};
								((GridBagLayout)buttonPanel.getLayout()).rowHeights = new int[] {0, 0};
								((GridBagLayout)buttonPanel.getLayout()).columnWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0E-4};
								((GridBagLayout)buttonPanel.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

								//---- label12 ----
								label12.setText("text");
								label12.addMouseListener(new MouseAdapter() {
									@Override
									public void mouseClicked(MouseEvent e) {
										label12MouseClicked(e);
									}
								});
								buttonPanel.add(label12, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
									GridBagConstraints.CENTER, GridBagConstraints.BOTH,
									new Insets(0, 0, 0, 5), 0, 0));

								//---- label8 ----
								label8.setText("Workflow actions:      ");
								buttonPanel.add(label8, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
									GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
									new Insets(0, 0, 0, 5), 0, 0));

								//---- bKeep ----
								bKeep.setText("Keep in inbox");
								bKeep.setIcon(new ImageIcon("icons/cabinet.gif"));
								bKeep.addActionListener(new ActionListener() {
									@Override
									public void actionPerformed(ActionEvent e) {
										bKeepActionPerformed();
									}
								});
								buttonPanel.add(bKeep, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
									GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
									new Insets(0, 0, 0, 5), 0, 0));

								//---- bReview ----
								bReview.setText("Send to reviewer");
								bReview.setIcon(new ImageIcon("icons/reviewer.gif"));
								bReview.addActionListener(new ActionListener() {
									@Override
									public void actionPerformed(ActionEvent e) {
										bReviewActionPerformed();
									}
								});
								buttonPanel.add(bReview, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
									GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
									new Insets(0, 0, 0, 5), 0, 0));

								//---- bEscalate ----
								bEscalate.setText("Escalate");
								bEscalate.setIcon(new ImageIcon("icons/editor.gif"));
								bEscalate.addActionListener(new ActionListener() {
									@Override
									public void actionPerformed(ActionEvent e) {
										bEscalateActionPerformed();
									}
								});
								buttonPanel.add(bEscalate, new GridBagConstraints(6, 0, 1, 1, 0.0, 0.0,
									GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
									new Insets(0, 0, 0, 5), 0, 0));
							}
							panel6.add(buttonPanel);
						}
						panel8.add(panel6, new GridBagConstraints(0, 3, 1, 1, 1.0, 1.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 0), 0, 0));
					}
					splitPane1.setTopComponent(panel8);

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
					splitPane1.setBottomComponent(tabbedPane3);
				}
				splitPane2.setRightComponent(splitPane1);
			}
			panel10.add(splitPane2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel10, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 0), 0, 0));

		//======== popupMenu1 ========
		{

			//---- menuItem2 ----
			menuItem2.setText("Delete");
			menuItem2.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					deleteCommentActionPerformed(e);
				}
			});
			popupMenu1.add(menuItem2);

			//---- menuItem3 ----
			menuItem3.setText("View Comment");
			menuItem3.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					viewCommentActionPerformed(e);
				}
			});
			popupMenu1.add(menuItem3);
		}

		//======== termZoomDialog ========
		{
			termZoomDialog.setModal(true);
			Container termZoomDialogContentPane = termZoomDialog.getContentPane();
			termZoomDialogContentPane.setLayout(new BorderLayout());

			//======== panel12 ========
			{
				panel12.setBorder(new EmptyBorder(5, 5, 5, 5));
				panel12.setLayout(new GridBagLayout());
				((GridBagLayout)panel12.getLayout()).columnWidths = new int[] {0, 0, 0};
				((GridBagLayout)panel12.getLayout()).rowHeights = new int[] {0, 0, 0};
				((GridBagLayout)panel12.getLayout()).columnWeights = new double[] {1.0, 0.0, 1.0E-4};
				((GridBagLayout)panel12.getLayout()).rowWeights = new double[] {1.0, 0.0, 1.0E-4};

				//======== scrollPane2 ========
				{
					scrollPane2.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

					//---- zoomTextArea ----
					zoomTextArea.setLineWrap(true);
					scrollPane2.setViewportView(zoomTextArea);
				}
				panel12.add(scrollPane2, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 5, 0), 0, 0));

				//---- saveZoomButton ----
				saveZoomButton.setText("Save");
				saveZoomButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						saveZoomButtonActionPerformed(e);
					}
				});
				panel12.add(saveZoomButton, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
					new Insets(0, 0, 0, 5), 0, 0));

				//---- cancelZoomChange ----
				cancelZoomChange.setText("Cancel");
				cancelZoomChange.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cancelZoomChangeActionPerformed(e);
					}
				});
				panel12.add(cancelZoomChange, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
					GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
					new Insets(0, 0, 0, 0), 0, 0));
			}
			termZoomDialogContentPane.add(panel12, BorderLayout.CENTER);
			termZoomDialog.pack();
			termZoomDialog.setLocationRelativeTo(termZoomDialog.getOwner());
		}

		//---- buttonGroup1 ----
		ButtonGroup buttonGroup1 = new ButtonGroup();
		buttonGroup1.add(rbYes);
		buttonGroup1.add(rbNo);

		//---- buttonGroup3 ----
		ButtonGroup buttonGroup3 = new ButtonGroup();
		buttonGroup3.add(rbAct);
		buttonGroup3.add(rbInact);
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	/** The panel1. */
	private JPanel panel1;
	
	/** The menu bar1. */
	private JMenuBar menuBar1;
	
	/** The menu1. */
	private JMenu menu1;
	
	/** The b add fsn. */
	private JMenuItem bAddFSN;
	
	/** The m add pref. */
	private JMenuItem mAddPref;
	
	/** The m add desc. */
	private JMenuItem mAddDesc;
	
	/** The menu3. */
	private JMenu menu3;
	
	/** The m spell chk. */
	private JMenuItem mSpellChk;
	
	/** The menu2. */
	private JMenu menu2;
	
	/** The m hist. */
	private JMenuItem mHist;
	
	/** The m log. */
	private JMenuItem mLog;
	
	/** The menu4. */
	private JMenu menu4;
	
	/** The menu item1. */
	private JMenuItem menuItem1;
	
	/** The label14. */
	private JLabel label14;
	
	/** The label15. */
	private JLabel label15;
	
	/** The label16. */
	private JLabel label16;
	
	/** The label10. */
	private JLabel label10;
	
	/** The panel10. */
	private JPanel panel10;
	
	/** The split pane2. */
	private JSplitPane splitPane2;
	
	/** The split pane4. */
	private JSplitPane splitPane4;
	
	/** The panel9. */
	private JPanel panel9;
	
	/** The label9. */
	private JLabel label9;
	
	/** The scroll pane1. */
	private JScrollPane scrollPane1;
	
	/** The tab sou. */
	private ZebraJTable tabSou;
	
	/** The tabbed pane2. */
	private JTabbedPane tabbedPane2;
	
	/** The panel16. */
	private JPanel panel16;
	
	/** The panel17. */
	private JPanel panel17;
	
	/** The b add coment. */
	private JButton bAddComent;
	
	/** The cmb tar comm. */
	private JComboBox cmbTarComm;
	
	/** The label17. */
	private JLabel label17;
	
	/** The tabbed pane1. */
	private JTabbedPane tabbedPane1;
	
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
	
	/** The split pane1. */
	private JSplitPane splitPane1;
	
	/** The panel8. */
	private JPanel panel8;
	
	/** The label11. */
	private JLabel label11;
	
	/** The scroll pane6. */
	private JScrollPane scrollPane6;
	
	/** The tab tar. */
	private ZebraJTable tabTar;
	
	/** The panel2. */
	private JPanel panel2;
	
	/** The label2. */
	private JLabel label2;
	
	/** The scroll pane5. */
	private JScrollPane scrollPane5;
	
	/** The target text field. */
	private JTextArea targetTextField;
	
	/** The label1. */
	private JLabel label1;
	
	/** The panel7. */
	private JPanel panel7;
	
	/** The label4. */
	private JLabel label4;
	
	/** The combo box1. */
	private JComboBox comboBox1;
	
	/** The panel5. */
	private JPanel panel5;
	
	/** The label5. */
	private JLabel label5;
	
	/** The cmb accep. */
	private JComboBox cmbAccep;
	
	/** The label3. */
	private JLabel label3;
	
	/** The panel4. */
	private JPanel panel4;
	
	/** The rb yes. */
	private JRadioButton rbYes;
	
	/** The label6. */
	private JLabel label6;
	
	/** The rb no. */
	private JRadioButton rbNo;
	
	/** The panel14. */
	private JPanel panel14;
	
	/** The label7. */
	private JLabel label7;
	
	/** The rb act. */
	private JRadioButton rbAct;
	
	/** The rb inact. */
	private JRadioButton rbInact;
	
	/** The panel3. */
	private JPanel panel3;
	
	/** The label13. */
	private JLabel label13;
	
	/** The panel6. */
	private JPanel panel6;
	
	/** The button panel. */
	private JPanel buttonPanel;
	
	/** The label12. */
	private JLabel label12;
	
	/** The label8. */
	private JLabel label8;
	
	/** The b keep. */
	private JButton bKeep;
	
	/** The b review. */
	private JButton bReview;
	
	/** The b escalate. */
	private JButton bEscalate;
	
	/** The tabbed pane3. */
	private JTabbedPane tabbedPane3;
	
	/** The scroll pane7. */
	private JScrollPane scrollPane7;
	
	/** The tree3. */
	private JTree tree3;
	
	/** The hierarchy navigator1. */
	private HierarchyNavigator hierarchyNavigator1;
	
	/** The popup menu1. */
	private JPopupMenu popupMenu1;
	
	/** The menu item2. */
	private JMenuItem menuItem2;
	
	/** The menu item3. */
	private JMenuItem menuItem3;
	
	/** The term zoom dialog. */
	private JDialog termZoomDialog;
	
	/** The panel12. */
	private JPanel panel12;
	
	/** The scroll pane2. */
	private JScrollPane scrollPane2;
	
	/** The zoom text area. */
	private JTextArea zoomTextArea;
	
	/** The save zoom button. */
	private JButton saveZoomButton;
	
	/** The cancel zoom change. */
	private JButton cancelZoomChange;
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

	/** The target preferred. */
	private String targetPreferred;
	
	/** The target fsn. */
	private String targetFSN;


	/** The defining char. */
	private int definingChar = -1;
	
	/** The source fsn. */
	private String sourceFSN;
	
	/** The worklist member. */
	private WorkListMember worklistMember;
	
	/** The set by code. */
	private boolean setByCode;
	
	/** The source ics. */
	private boolean sourceICS;
	
	/** The keep ii class. */
	private I_KeepTaskInInbox keepIIClass;
	
	/** The unloaded. */
	private boolean unloaded;
	
	/** The role. */
	private I_GetConceptData role;
	
	/** The editing row. */
	private Integer editingRow;
	
	/** The target preferred row. */
	private Integer targetPreferredRow;
	
	/** The target fsn row. */
	private Integer targetFSNRow;
	
	/** The html footer. */
	private String htmlFooter = "</body></html>";
	
	/** The html header. */
	private String htmlHeader = "<html><body><font style='color:blue'>";
	
	/** The end p. */
	private String endP = "</font>";
	
	/** The scrollp. */
	private JScrollPane scrollp;

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
										termType_Status[0] = this.synonym;
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

									final SimilarityPanel similPanel = getSimilarityPanel();
									Runnable simil = new Runnable() {
										public void run() {
											similPanel.updateTabs(sourceFSN, concept, sourceIds, targetId, translationProject, worklistMember);
											int scount = similPanel.getSimilarityHitsCount();
											if (scount > 0) {
												label14.setForeground(Color.red);
											} else {
												label14.setForeground(Color.black);
											}
											label14.setText("S:" + scount);
											label14.revalidate();

											int tmcount = similPanel.getTransMemoryHitsCount();
											if (tmcount > 0) {
												label15.setForeground(Color.red);
											} else {
												label15.setForeground(Color.black);
											}
											label15.setText("TM:" + tmcount);
											label15.revalidate();

											int lgcount = similPanel.getLingGuidelinesHitsCount();
											if (lgcount > 0) {
												label16.setForeground(Color.red);
											} else {
												label16.setForeground(Color.black);
											}
											label16.setText("LG:" + lgcount);
											label16.revalidate();
										}
									};
									updateUIThread = new Thread(simil);
									updateUIThread.start();
									// Runnable tMemo = new Runnable() {
									// public void run() {
									// updateTransMemoryTable(sourceFSN);
									// }
									// };
									// tMemo.run();
									// Runnable gloss = new Runnable() {
									// public void run() {
									// // updateGlossaryEnforcement(sourceFSN);
									// }
									// };
									// gloss.run();
								}
							} else if (description.getAcceptabilityId() == acceptable.getConceptNid() && sourceCom.contains(ConfigTranslationModule.TreeComponent.SYNONYM)) {
								rowClass[0] = TreeEditorObjectWrapper.SYNONYMN;
								row[TableSourceColumn.ACCEPTABILITY.ordinal()] = acceptable;
								termType_Status[0] = this.synonym;
								termType_Status[1] = active;
								row[TableSourceColumn.TERM_TYPE.ordinal()] = termType_Status;
								bNewNode = true;
							} else if (description.getAcceptabilityId() == preferred.getConceptNid() && sourceCom.contains(ConfigTranslationModule.TreeComponent.PREFERRED)) {
								rowClass[0] = TreeEditorObjectWrapper.PREFERRED;
								row[TableSourceColumn.ACCEPTABILITY.ordinal()] = preferred;
								termType_Status[0] = this.synonym;
								termType_Status[1] = active;
								row[TableSourceColumn.TERM_TYPE.ordinal()] = termType_Status;
								bNewNode = true;
							} else if (sourceCom.contains(ConfigTranslationModule.TreeComponent.RETIRED)) {
								rowClass[0] = TreeEditorObjectWrapper.SYNONYMN;
//								row[TableSourceColumn.ACCEPTABILITY.ordinal()] = notAcceptable;
								termType_Status[0] = this.synonym;
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
		TermRenderer textAreaRenderer = new TermRenderer();
		cmodel.getColumn(TableSourceColumn.TERM.ordinal()).setCellRenderer(textAreaRenderer);
		cmodel.getColumn(TableSourceColumn.TERM_TYPE.ordinal()).setCellRenderer(new TermTypeIconRenderer());
		cmodel.getColumn(TableSourceColumn.ACCEPTABILITY.ordinal()).setCellRenderer(new AcceptabilityIconRenderer());
		cmodel.getColumn(TableSourceColumn.LANGUAGE.ordinal()).setCellRenderer(new LanguageIconRenderer());
		cmodel.getColumn(TableSourceColumn.ICS.ordinal()).setCellRenderer(new ICSIconRenderer());

		// double widthAvai = panel9.getPreferredSize().getWidth();
		// int termColAvai=(int) (widthAvai-100);
		// if (termColAvai>maxTermWidth)
		// cmodel.getColumn(TableSourceColumn.TERM.ordinal()).setPreferredWidth(termColAvai);
		// else
		// cmodel.getColumn(TableSourceColumn.TERM.ordinal()).setPreferredWidth(maxTermWidth);
		cmodel.getColumn(TableSourceColumn.TERM.ordinal()).setMinWidth(maxTermWidth);

		cmodel.getColumn(TableSourceColumn.TERM_TYPE.ordinal()).setMaxWidth(48);
		cmodel.getColumn(TableSourceColumn.ACCEPTABILITY.ordinal()).setMaxWidth(48);
		cmodel.getColumn(TableSourceColumn.ICS.ordinal()).setMaxWidth(48);
		cmodel.getColumn(TableSourceColumn.LANGUAGE.ordinal()).setMaxWidth(48);

		tabSou.setRowHeight(24);
		tabSou.setUpdateSelectionOnSort(true);
		tabSou.revalidate();

		if (isMemberLogOpen()) {
			mLogActionPerformed();
		}

	}

	/**
	 * Gets the similarity panel.
	 *
	 * @return the similarity panel
	 */
	private SimilarityPanel getSimilarityPanel() {

		AceFrameConfig config;
		try {
			config = (AceFrameConfig) Terms.get().getActiveAceFrameConfig();

			AceFrame ace = config.getAceFrame();
			JTabbedPane tp = ace.getCdePanel().getLeftTabs();
			if (tp != null) {
				int tabCount = tp.getTabCount();
				for (int i = 0; i < tabCount; i++) {
					if (tp.getTitleAt(i).equals(TranslationHelperPanel.SIMILARITY_TAB_NAME)) {
						// tp.setSelectedIndex(i);
						// tp.revalidate();
						// tp.repaint();
						return (SimilarityPanel) tp.getComponentAt(i);
					}
				}
				SimilarityPanel uiPanel = new SimilarityPanel();

				tp.addTab(TranslationHelperPanel.SIMILARITY_TAB_NAME, uiPanel);
				// tp.setSelectedIndex(tabCount);
				// tp.revalidate();
				// tp.repaint();
				return uiPanel;
			}
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Populate target tree.
	 *
	 * @throws Exception the exception
	 */
	private void populateTargetTree() throws Exception {
		I_TermFactory tf = Terms.get();
		// DefaultMutableTreeNode top = null;
		boolean bHasPref = false;
		boolean bHasFSN = false;
		bAddFSN.setEnabled(true && !readOnlyMode);
		targetPreferred = "";
		targetFSN = "";
		targetPreferredRow = null;
		targetFSNRow = null;
		int authId;
		String authorColName = "";
		int authorColPos = -1;
		int authorAdj = 0;
		int maxTermWidth = 0;
		HashMap<Integer, String> hashAuthId = new HashMap<Integer, String>();
		translConfig = LanguageUtil.getTranslationConfig(Terms.get().getActiveAceFrameConfig());
		translConfig = getTranslationProjectConfig();
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
									termType_Status[0] = this.synonym;
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
								targetFSN = description.getText();
							}
							bHasFSN = true;
						} else if (description.getAcceptabilityId() == acceptable.getConceptNid() && targetCom.contains(ConfigTranslationModule.TreeComponent.SYNONYM)) {
							rowClass[0] = TreeEditorObjectWrapper.SYNONYMN;
							row[TableSourceColumn.ACCEPTABILITY.ordinal()] = acceptable;
							termType_Status[0] = this.synonym;
							termType_Status[1] = active;
							row[TableSourceColumn.TERM_TYPE.ordinal()] = termType_Status;
							bNewNode = true;
						} else if (description.getAcceptabilityId() == preferred.getConceptNid() && targetCom.contains(ConfigTranslationModule.TreeComponent.PREFERRED)) {
							rowClass[0] = TreeEditorObjectWrapper.PREFERRED;
							row[TableSourceColumn.ACCEPTABILITY.ordinal()] = preferred;
							termType_Status[0] = this.synonym;
							termType_Status[1] = active;
							row[TableSourceColumn.TERM_TYPE.ordinal()] = termType_Status;
							bHasPref = true;
							targetPreferred = description.getText();

							bNewNode = true;
						} else if (targetCom.contains(ConfigTranslationModule.TreeComponent.RETIRED)) {
							rowClass[0] = TreeEditorObjectWrapper.SYNONYMN;
//							row[TableSourceColumn.ACCEPTABILITY.ordinal()] = notAcceptable;
							termType_Status[0] = this.synonym;
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
									// TODO There is an index out of band
									// exception here..!
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
								targetFSNRow = model.getRowCount() - 1;
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
								targetPreferredRow = model.getRowCount() - 1;
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
		TermRenderer textAreaRenderer = new TermRenderer();
		cmodel.getColumn(TableTargetColumn.TERM.ordinal()).setCellRenderer(textAreaRenderer);
		cmodel.getColumn(TableTargetColumn.TERM_TYPE.ordinal()).setCellRenderer(new TermTypeIconRenderer());
		cmodel.getColumn(TableTargetColumn.ACCEPTABILITY.ordinal()).setCellRenderer(new AcceptabilityIconRenderer());
		cmodel.getColumn(TableTargetColumn.LANGUAGE.ordinal()).setCellRenderer(new LanguageIconRenderer());
		cmodel.getColumn(TableTargetColumn.ICS.ordinal()).setCellRenderer(new ICSIconRenderer());

		cmodel.getColumn(TableTargetColumn.TERM.ordinal()).setMinWidth(maxTermWidth);
		cmodel.getColumn(TableTargetColumn.TERM_TYPE.ordinal()).setMaxWidth(48);
		cmodel.getColumn(TableTargetColumn.ACCEPTABILITY.ordinal()).setMaxWidth(48);
		cmodel.getColumn(TableTargetColumn.ICS.ordinal()).setMaxWidth(48);
		cmodel.getColumn(TableTargetColumn.LANGUAGE.ordinal()).setMaxWidth(48);
		tabTar.setRowHeight(24);
		tabTar.setUpdateSelectionOnSort(true);
		tabTar.revalidate();

		bAddFSN.setEnabled(!bHasFSN && bHasPref && !readOnlyMode);
		comboBox1.setEnabled(false);
		targetTextField.setVisible(true);
		targetTextField.setEnabled(false);
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

				Long concpetID = null;
				I_Identify identifier = concept.getIdentifier();
				List<? extends I_IdPart> parts = identifier.getMutableIdParts();
				Long lastTime = Long.MIN_VALUE;
				for (I_IdPart i_IdPart : parts) {
					if (i_IdPart.getAuthorityNid() == tf.uuidToNative(ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.getUids()) && i_IdPart.getTime() > lastTime) {
						lastTime = i_IdPart.getTime();
						concpetID = (Long) i_IdPart.getDenotation();
					}
				}
				DefaultMutableTreeNode conceptIdNode = new DefaultMutableTreeNode(new TreeEditorObjectWrapper("Concept ID: " + concpetID, IconUtilities.ID, concpetID));
				top.add(conceptIdNode);

				List<I_RelTuple> relationships = (List<I_RelTuple>) concept.getSourceRelTuples(config.getAllowedStatus(), config.getDestRelTypes(), config.getViewPositionSetReadOnly(),
						config.getPrecedence(), config.getConflictResolutionStrategy(), config.getClassifierConcept().getNid(), RelAssertionType.INFERRED);

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
					if (nodeObject.getType() == IconUtilities.DEFINED_PARENT || nodeObject.getType() == IconUtilities.PRIMITIVE_PARENT || nodeObject.getType() == IconUtilities.INACTIVE_PARENT) {
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

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 1L;

		/* (non-Javadoc)
		 * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
		 */
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

			JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			Object[] termType_status = (Object[]) value;
			String termType = termType_status[0].toString();
			String status = termType_status[1].toString();
			label.setIcon(IconUtilities.getIconForTermType_Status(termType, status));
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

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 1L;

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
	 * The Class TermRenderer.
	 */
	class TermRenderer extends DefaultTableCellRenderer {

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 1L;

		/* (non-Javadoc)
		 * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
		 */
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

			JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			label.setToolTipText(value.toString());
			return label;
		}

	}

	/**
	 * The Class LanguageIconRenderer.
	 */
	class LanguageIconRenderer extends DefaultTableCellRenderer {

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 1L;

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

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 1L;

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

		// DefaultMutableTreeNode node = (DefaultMutableTreeNode)
		// tree2.getLastSelectedPathComponent();

		// try {
		// translConfig=LanguageUtil.getTranslationConfig(Terms.get().getActiveAceFrameConfig());
		// } catch (IOException e1) {
		// e1.printStackTrace();
		// } catch (TerminologyException e1) {
		// e1.printStackTrace();
		// }
		// if (node != null) {
		// TreeEditorObjectWrapper nodeWrp =
		// (TreeEditorObjectWrapper)node.getUserObject();
		//
		// Object obj=nodeWrp.getUserObject();
		// if (obj instanceof ContextualizedDescription){
		if (translConfig.getSelectedEditorMode().equals(ConfigTranslationModule.EditorMode.PREFERRED_TERM_EDITOR)) {
			if (!(descrpt.getTypeId() == synonym.getConceptNid() && descrpt.getAcceptabilityId() == preferred.getConceptNid())) {
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
			if (!(descrpt.getTypeId() == synonym.getConceptNid() && descrpt.getAcceptabilityId() == acceptable.getConceptNid())) {
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
			if (descriptionInEditor.getText().trim().equals(targetTextField.getText().trim())
					&& (descriptionInEditor.isInitialCaseSignificant() == rbYes.isSelected())
					&& descriptionInEditor.getAcceptabilityId() == ((I_GetConceptData) cmbAccep.getSelectedItem()).getConceptNid()
					&& ((descriptionInEditor.getExtensionStatusId() == active.getConceptNid() && rbAct.isSelected()) || (descriptionInEditor.getExtensionStatusId() != active.getConceptNid() && !rbAct
							.isSelected()))
					&& ((descriptionInEditor.getTypeId() == fsn.getConceptNid() && fsn.equals((I_GetConceptData) comboBox1.getSelectedItem())) || (descriptionInEditor.getTypeId() != fsn
							.getConceptNid() && !fsn.equals((I_GetConceptData) comboBox1.getSelectedItem())))) {
				update = true;
			} else {
				Object[] options = { "Discard unsaved data", "Cancel and continue editing" };
				int n = JOptionPane.showOptionDialog(null, "Do you want to save the change you made to the term in the editor panel?", "Unsaved data", JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE, null, // do not use
															// a
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
		}

		if (update) {

			if (descrpt == null) {
				descriptionInEditor = null;
				// label4.setText("");
				targetTextField.setText("");
				rbYes.setSelected(false);
				panel2.revalidate();
				// saveDesc.setEnabled(false);
				mSpellChk.setEnabled(false);
				// button5.setEnabled(false);
				// mAddPref.setEnabled(false);
				// mAddDesc.setEnabled(true);
				// label4.setVisible(true);
				// comboBox1.setEnabled(true);
				// rbYes.setEnabled(true);
				// rbNo.setEnabled(true);
				// rbAct.setEnabled(true);
				// rbInact.setEnabled(true);
				// cmbAccep.setEnabled(true);
				targetTextField.setEnabled(false);
			} else {
				editingRow = rowModel;
				if (descrpt.getLanguageRefsetId() == targetId) {
					String langCode = "";
					try {
						langCode = targetLangRefset.getLangCode(Terms.get().getActiveAceFrameConfig());
					} catch (TerminologyException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					if (descrpt.getTypeId() == fsn.getConceptNid() || descrpt.getTypeId() == preferred.getConceptNid() || descrpt.getTypeId() == synonym.getConceptNid()) {
						try {
							if (fsn.getConceptNid() == descrpt.getTypeId()) {
								comboBox1.setSelectedItem(fsn);
							} else {
								comboBox1.setSelectedItem(synonym);
							}
							cmbAccep.setSelectedItem(Terms.get().getConcept(descrpt.getAcceptabilityId()));
						} catch (TerminologyException ex) {
							ex.printStackTrace();
						} catch (IOException ex) {
							ex.printStackTrace();
						}
						targetTextField.setEnabled(true && !readOnlyMode);
						// bDescIssue.setEnabled(false);
						descriptionInEditor = descrpt;
						// label4.setText(Terms.get().getConcept(descriptionInEditor.getTypeId()).toString());
						targetTextField.setText(descriptionInEditor.getText().trim());
						if (descriptionInEditor.isInitialCaseSignificant())
							rbYes.setSelected(true);
						else
							rbNo.setSelected(true);
						rbAct.setSelected(descriptionInEditor.getExtensionStatusId() == active.getConceptNid());
						panel2.revalidate();
						saveDesc = true;
						mSpellChk.setEnabled(true);
						if(!ArchitectonicAuxiliary.LANG_CODE.valueOf(langCode).getFormatedLanguageCode().equals(descrpt.getLang())){
							targetTextField.setEnabled(false);
						}
						// mAddPref.setEnabled(false);
						// mAddDesc.setEnabled(false);
						// button5.setEnabled(true);
						// label4.setVisible(true);
						// comboBox1.setEnabled(true);
						// cmbAccep.setEnabled(true);
						// rbAct.setEnabled(true);
						// rbInact.setEnabled(true);
						// rbYes.setEnabled(true);
						// rbNo.setEnabled(true);
					}
				}
			}
		}
		// }
		// }
	}

	/**
	 * Update ui.
	 *
	 * @param translationProject the translation project
	 * @param workListMember the work list member
	 * @param role the role
	 */
	public void updateUI(TranslationProject translationProject, WorkListMember workListMember, I_GetConceptData role) {
		// clearForm(true);
		try {
			alreadyVerified = false;
			this.translationProject = translationProject;
			this.role = role;
			translConfig = LanguageUtil.getTranslationConfig(Terms.get().getActiveAceFrameConfig());
			// if (translConfig.isProjectDefaultConfiguration())
			translConfig = getTranslationProjectConfig();

			HashMap<UUID, EditorMode> currentRoleConfiguration = translConfig.getTranslatorRoles();
			List<UUID> uidList = role.getUids();
			for (UUID uuid : uidList) {
				if (currentRoleConfiguration.containsKey(uuid)) {
					EditorMode selectedEditorModeForCurrentRole = currentRoleConfiguration.get(uuid);
					if (selectedEditorModeForCurrentRole.equals(EditorMode.READ_ONLY)) {
						targetTextField.setEnabled(false);
						targetTextField.setEditable(false);
						setReadOnlyMode(true);
					} else {
						targetTextField.setEnabled(true);
					}
				}
			}

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

			hierarchyNavigator1.setContainerPanel(tabbedPane3);
			hierarchyNavigator1.setFocusConcept(concept);

			sourceICS = LanguageUtil.getDefaultICS(concept, sourceLangRefsets.iterator().next(), targetLangRefset, config);
			if (sourceICS)
				rbYes.setSelected(true);
			else
				rbNo.setSelected(true);

			comboBox1.setEnabled(true && !readOnlyMode);
			cmbAccep.setEnabled(true && !readOnlyMode);
			if (translConfig.getSelectedEditorMode().equals(ConfigTranslationModule.EditorMode.PREFERRED_TERM_EDITOR)) {
				if (targetPreferred.equals("")) {
					mAddPrefActionPerformed();
					String pref = LanguageUtil.getDefaultPreferredTermText(concept, sourceLangRefsets.iterator().next(), targetLangRefset, config);
					targetTextField.setText(pref);
				} else {
					if (targetPreferredRow != null) {
						tabTar.setRowSelectionInterval(targetPreferredRow, targetPreferredRow);
					}
				}
				bAddFSN.setEnabled(false);
				mAddDesc.setEnabled(false);
				mAddPref.setEnabled(true && !readOnlyMode);
				comboBox1.setEnabled(false);
				cmbAccep.setEnabled(false);
			} else if (translConfig.getSelectedEditorMode().equals(ConfigTranslationModule.EditorMode.SYNONYMS_EDITOR)) {
				mAddDescActionPerformed();
				bAddFSN.setEnabled(false);
				mAddDesc.setEnabled(true);
				mAddPref.setEnabled(false);
				comboBox1.setEnabled(false);
				cmbAccep.setEnabled(false);
			} else if (translConfig.getSelectedEditorMode().equals(ConfigTranslationModule.EditorMode.FULL_EDITOR)) {
				if (translConfig.getEditingPanelOpenMode().equals(EditingPanelOpenMode.PREFFERD_TERM_MODE)) {
					if (targetPreferred.equals("")) {
						mAddPrefActionPerformed();
						String pref = LanguageUtil.getDefaultPreferredTermText(concept, sourceLangRefsets.iterator().next(), targetLangRefset, config);
						targetTextField.setText(pref);
					} else {
						if (targetPreferredRow != null) {
							tabTar.setRowSelectionInterval(targetPreferredRow, targetPreferredRow);
						}
					}
				} else if (translConfig.getEditingPanelOpenMode().equals(EditingPanelOpenMode.FSN_TERM_MODE)) {
					if (targetFSN.equals("")) {
						mAddPrefActionPerformed();
						comboBox1.setSelectedItem(fsn);
					} else {
						if (targetFSNRow != null) {
							tabTar.setRowSelectionInterval(targetFSNRow, targetFSNRow);
						}
					}
				}
				bAddFSN.setEnabled(true && !readOnlyMode);
				mAddDesc.setEnabled(true && !readOnlyMode);
				mAddPref.setEnabled(true && !readOnlyMode);
				comboBox1.setEnabled(true && !readOnlyMode);
				cmbAccep.setEnabled(true && !readOnlyMode);
			}
			getPreviousComments();

			getWebReferences();
			if (translationProject.getProjectIssueRepo() != null) {
				tabbedPane2.setTitleAt(1, "<html>Issues</html>");
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
				tabbedPane2.setTitleAt(1, "<html>Issues <font><style size=1>(Inactive)</style></font></html>");

			}
			targetTextField.requestFocusInWindow();
			targetTextField.setCaretPosition(targetTextField.getText().length());
			// mClose.setEnabled(false);
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
			HashMap<URL, String> urls = new HashMap<URL, String>();

			StringBuffer sb = new StringBuffer("");
			sb.append("<html><body>");
			int urlCount = 0;
			if (targetLangRefset != null) {
				urls = targetLangRefset.getCommentsRefset(config).getUrls(this.concept.getConceptNid());
				urlCount = urls.size();
				for (URL url : urls.keySet()) {
					sb.append("<a href=\"");
					sb.append(url.toString());
					sb.append("\">");
					sb.append(url.toString());
					sb.append("</a><br>");
				}
			}
			urls = TerminologyProjectDAO.getWorkList(Terms.get().getConcept(worklistMember.getWorkListUUID()), config).getCommentsRefset(config).getUrls(this.concept.getConceptNid());

			urlCount += urls.size();
			for (URL url : urls.keySet()) {
				sb.append("<a href=\"");
				sb.append(url.toString());
				sb.append("\">");
				sb.append(url.toString());
				sb.append("</a><br>");
			}
			sb.append("</body></html>");

			refTable.setText(sb.toString());
			if (urlCount > 0) {
				tabbedPane1.setTitleAt(1, "<html>Web references <b><font color='red'>(" + urlCount + ")</font></b></html>");
			} else {
				tabbedPane1.setTitleAt(1, "<html>Web references (0)</font></b></html>");
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
		if (issueListPanel == null) {
			createIssuePanel();
		}
		if (issueListPanel == null) {
			return;
		}

		I_ConfigAceFrame config;
		try {
			config = Terms.get().getActiveAceFrameConfig();
			IssueRepository repo = IssueRepositoryDAO.getIssueRepository(translationProject.getProjectIssueRepo());
			IssueRepoRegistration regis;
			regis = IssueRepositoryDAO.getRepositoryRegistration(repo.getUuid(), config);
			if (regis != null && regis.getUserId() != null && regis.getPassword() != null) {
				Integer issuesTot = issueListPanel.loadIssues(concept, repo, regis);
				if (issuesTot != null && issuesTot > 0) {
					tabbedPane2.setTitleAt(1, "<html>Issues <font><style color=red>*</style></font></html>");
				}
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
		tabbedPane1.setTitleAt(0, "<html>Comments</font></b></html>");
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

				public Class getColumnClass(int column) {
					Class returnValue;
					if ((column >= 0) && (column < getColumnCount()) && getRowCount() > 0) {
						returnValue = getValueAt(0, column).getClass();
					} else {
						returnValue = Object.class;
					}
					return returnValue;
				}
			};

			if (targetLangRefset != null) {
				commentsList = targetLangRefset.getCommentsRefset(config).getFullComments(concept.getConceptNid());
				for (int i = commentsList.size() - 1; i > -1; i--) {
					if (commentsList.get(i).getTypeCid() == commentsList.get(i).getSubTypeCid()) {
						tableModel.addRow(new Object[] { "Language refset: " + Terms.get().getConcept(commentsList.get(i).getTypeCid()) + "", formatComment(commentsList.get(i).getComment()) });
					} else {
						tableModel
								.addRow(new Object[] {
										"Language refset: " + Terms.get().getConcept(commentsList.get(i).getTypeCid()) + "/" + Terms.get().getConcept(commentsList.get(i).getSubTypeCid()),
										formatComment(commentsList.get(i).getComment()) });
					}
				}
			}

			commentsList = TerminologyProjectDAO.getWorkList(Terms.get().getConcept(worklistMember.getWorkListUUID()), config).getCommentsRefset(config).getFullComments(this.concept.getConceptNid());

			for (int i = commentsList.size() - 1; i > -1; i--) {
				if (commentsList.get(i).getTypeCid() == commentsList.get(i).getSubTypeCid()) {
					tableModel.addRow(new Object[] { "Worklist: " + Terms.get().getConcept(commentsList.get(i).getTypeCid()) + "", formatComment(commentsList.get(i).getComment()) });
				} else {
					tableModel.addRow(new Object[] { "Worklist: " + Terms.get().getConcept(commentsList.get(i).getTypeCid()) + "/" + Terms.get().getConcept(commentsList.get(i).getSubTypeCid()),
							formatComment(commentsList.get(i).getComment()) });
				}
			}

			tblComm.setModel(tableModel);
			RowSorter<TableModel> sorter = new TableRowSorter<TableModel>(tableModel);
			List sortKeys = new ArrayList();
			sortKeys.add(new RowSorter.SortKey(1, SortOrder.DESCENDING));
			sorter.setSortKeys(sortKeys);
			tblComm.setRowSorter(sorter);
			TableColumnModel cmodel = tblComm.getColumnModel();
			cmodel.getColumn(0).setMinWidth(120);
			cmodel.getColumn(0).setMaxWidth(145);
			EditorPaneRenderer textAreaRenderer = new EditorPaneRenderer();
			cmodel.getColumn(0).setCellRenderer(textAreaRenderer);
			cmodel.getColumn(1).setCellRenderer(textAreaRenderer);
			tblComm.setRowHeight(65);
			tblComm.revalidate();
			if (tblComm.getRowCount() > 0) {
				tabbedPane1.setTitleAt(0, "<html>Comments <b><font color='red'>(" + tblComm.getRowCount() + ")</font></b></html>");
			} else {
				tabbedPane1.setTitleAt(0, "<html>Comments (0)</font></b></html>");
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
	 * Sets the workflow buttons.
	 *
	 * @param buttons the new workflow buttons
	 */
	public void setWorkflowButtons(List<Component> buttons) {
		removeWorkflowButtons();
		addWorkflowButtons(buttons);
	}

	/**
	 * Adds the workflow buttons.
	 *
	 * @param buttons the buttons
	 */
	private void addWorkflowButtons(List<Component> buttons) {

		int columnsCount = (buttons.size() * 2) + 3;
		if (scrollp != null)
			panel6.remove(scrollp);
		else if (buttonPanel != null)
			panel6.remove(buttonPanel);
		buttonPanel = new JPanel();
		buttonPanel.setBackground(new Color(238, 238, 238));
		buttonPanel.setLayout(new GridBagLayout());

		int col = 0;
		int row = 0;
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = col;
		c.gridy = row;
		JLabel iconLabel = new JLabel("");
		iconLabel.setIcon(IconUtilities.media_step_forwardIcon);
		buttonPanel.add(iconLabel, c);
		col++;

		for (Component loopComponent : buttons) {
			setButtonMnemo(loopComponent);
			if (col == 4) {
				col = 1;
				row++;
			}
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = col;
			c.gridy = row;
			buttonPanel.add(loopComponent, c);
			col++;
		}

		// ---- button5 ----
		button5 = new JButton();
		button5.setText("Cancel");
		button5.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				retireActionPerformed(e);
			}
		});

		setButtonMnemo(button5);
		if (col == 4) {
			col = 1;
			row++;
		}
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = col;
		c.gridy = row;
		buttonPanel.add(button5, c);

		// ((GridBagLayout)buttonPanel.getLayout()).columnWidths = new
		// int[columnsCount] ;
		// ((GridBagLayout)buttonPanel.getLayout()).rowHeights = new int[] {0,
		// 0};
		// ((GridBagLayout)buttonPanel.getLayout()).columnWeights = new
		// double[columnsCount] ;
		//
		// ((GridBagLayout)buttonPanel.getLayout()).columnWeights[0]=1.0;
		// for (int i=1;i<columnsCount-1;i++){
		// ((GridBagLayout)buttonPanel.getLayout()).columnWeights[i]=0.0;
		// }
		// ((GridBagLayout)buttonPanel.getLayout()).columnWeights[columnsCount-1]=1.0E-4;
		//
		// ((GridBagLayout)buttonPanel.getLayout()).rowWeights = new double[]
		// {0.0, 1.0E-4};
		//
		// //---- label8 ----
		// label8=new JLabel();
		// label8.setText("Workflow actions:      ");
		// buttonPanel.add(label8, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
		// GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
		// new Insets(0, 0, 0, 5), 0, 0));
		// for (int i=0;i<buttons.size();i++){
		// Component btton=buttons.get(i);
		// setButtonMnemo(btton);
		// buttonPanel.add(btton, new GridBagConstraints((i+1) * 2 , 0, 1, 1,
		// 0.0, 0.0,
		// GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
		// new Insets(0, 0, 0, 5), 0, 0));
		// }
		scrollp = new JScrollPane();
		scrollp.setViewportView(buttonPanel);
		Dimension minimumSize = new Dimension(panel6.getWidth(), (row + 1) * (button5.getPreferredSize().height + 14));
		panel6.setMinimumSize(minimumSize);
		panel6.add(scrollp);
		panel6.revalidate();
		panel6.repaint();
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
	 * Removes the workflow buttons.
	 */
	public void removeWorkflowButtons() {
		if (buttonPanel != null) {
			for (int i = buttonPanel.getComponentCount() - 1; i > -1; i--) {

				Component comp = buttonPanel.getComponent(i);
				if (comp instanceof JButton) {
					for (ActionListener aListener : ((JButton) comp).getActionListeners()) {
						((JButton) comp).removeActionListener(aListener);
					}
				}
				buttonPanel.remove(comp);
				comp = null;
			}
		}

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
