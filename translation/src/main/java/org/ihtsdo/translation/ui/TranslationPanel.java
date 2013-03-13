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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.ToolTipManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.config.AceFrame;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.ace.task.commit.AlertToDataConstraintFailure;
import org.dwfa.bpa.process.I_Work;
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
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.model.WorkListMember;
import org.ihtsdo.project.refset.LanguageMembershipRefset;
import org.ihtsdo.project.util.IconUtilities;
import org.ihtsdo.project.view.PanelHelperFactory;
import org.ihtsdo.project.view.TranslationHelperPanel;
import org.ihtsdo.project.view.details.WorklistMemberLogPanel;
import org.ihtsdo.project.view.issue.IssuesListPanel2;
import org.ihtsdo.project.workflow.api.WfComponentProvider;
import org.ihtsdo.project.workflow.api.WorkflowInterpreter;
import org.ihtsdo.project.workflow.model.WfAction;
import org.ihtsdo.project.workflow.model.WfInstance;
import org.ihtsdo.project.workflow.model.WfPermission;
import org.ihtsdo.project.workflow.model.WfRole;
import org.ihtsdo.project.workflow.model.WfUser;
import org.ihtsdo.project.workflow.model.WorkflowDefinition;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.RelAssertionType;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.translation.FSNGenerationException;
import org.ihtsdo.translation.LanguageUtil;
import org.ihtsdo.translation.TreeEditorObjectWrapper;
import org.ihtsdo.translation.ui.ConfigTranslationModule.EditingPanelOpenMode;
import org.ihtsdo.translation.ui.ConfigTranslationModule.EditorMode;
import org.ihtsdo.translation.ui.ConfigTranslationModule.TreeComponent;
import org.ihtsdo.translation.ui.translation.CommentsManagerPanel;

/**
 * The Class TranslationPanel.
 */
public class TranslationPanel extends JPanel {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The Constant ACTION_LAUNCHED. */
	public static final String ACTION_LAUNCHED = "Action launched";

	public static final String SEND_TO_OUTBOX_LAUNCHED = "Send to outbox";

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
	private I_GetConceptData snomedIsa;

	/** The inferred. */
	private int inferred;

	/** The canc action. */
	private WfAction cancAction;

	/** Source fsn concept */
	private I_ContextualizeDescription sourceFsnConcept;

	/** Source preferred concept */
	private I_ContextualizeDescription sourcePreferredConcept;

	private Set<Character> memonicKeys = new HashSet<Character>();

	/** The target text changed. */
	private boolean targetTextChanged = false;
	private UpdateUIWorker updateUiWorker;

	/**
	 * Instantiates a new translation concept editor.
	 * 
	 */
	public TranslationPanel() {
		sourceIds = new ArrayList<Integer>();
		I_ConfigAceFrame config = null;
		try {
			config = Terms.get().getActiveAceFrameConfig();
			inactive = Terms.get().getConcept(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getNid());
			fsn = Terms.get().getConcept(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid());
			preferred = Terms.get().getConcept(SnomedMetadataRf2.PREFERRED_RF2.getLenient().getNid());
			synonym = Terms.get().getConcept(SnomedMetadataRf2.SYNONYM_RF2.getLenient().getNid());
			snomedIsa = Terms.get().getConcept(UUID.fromString("c93a30b9-ba77-3adb-a9b8-4589c9f8fb25"));
			acceptable = Terms.get().getConcept(SnomedMetadataRf2.ACCEPTABLE_RF2.getLenient().getNid());
			active = Terms.get().getConcept(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid());
			definingChar = SnomedMetadataRf2.DEFINING_RELATIONSHIP_RF2.getLenient().getNid();
			inferred = SnomedMetadataRf2.INFERRED_RELATIONSHIP_RF2.getLenient().getNid();
			config.getDescTypes().add(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid());
			config.getDescTypes().add(SnomedMetadataRf2.SYNONYM_RF2.getLenient().getNid());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}

		initComponents();
		initializeMemonicKeys();
		targetTextField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent arg0) {
				targetTextChanged = true;
			}

			@Override
			public void insertUpdate(DocumentEvent arg0) {
				targetTextChanged = true;
			}

			@Override
			public void changedUpdate(DocumentEvent arg0) {
				targetTextChanged = true;
			}
		});

		// StyleSheet ss = new StyleSheet();
		// Color color = ss.stringToColor("#ffcccc");
		// UIManager.put("Table.alternateRowColor", color);

		label10.setIcon(IconUtilities.helpIcon);
		label10.setText("");
		label12.setIcon(IconUtilities.helpIcon);
		label12.setText("");
		label13.setIcon(IconUtilities.helpIcon);
		label13.setText("");

		// bDescIssue.setEnabled(false);
		DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel();
		comboBoxModel.addElement(synonym);
		comboBoxModel.addElement(fsn);

		comboBox1.setModel(comboBoxModel);
		comboBox1.setSelectedItem(synonym);
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
		saveAndAdd.setEnabled(true && !readOnlyMode);
		mAddPref.setEnabled(true && !readOnlyMode);
		// label4.setVisible(false);

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

		Dimension dimension = new Dimension(650, 350);
		termZoomDialog.setMaximumSize(dimension);
		termZoomDialog.setMinimumSize(dimension);
		termZoomDialog.setSize(dimension);

		cancAction = new WfAction();
		cancAction.setBusinessProcess(new File("sampleProcesses/CancelActionWithoutDestination.bp"));
		cancAction.setId(UUID.randomUUID());
		cancAction.setName("Cancel");
		cancAction.setConsequence(null);

	}

	private void initializeMemonicKeys() {
		memonicKeys = new HashSet<Character>();
		memonicKeys.add('G');
		memonicKeys.add('Y');
		memonicKeys.add('K');
		memonicKeys.add('F');
		memonicKeys.add('P');
		memonicKeys.add('D');
		memonicKeys.add('A');
		memonicKeys.add('U');
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

			mItema = new JMenuItem();
			mItema.setText("Copy to clipboard");
			mItema.setActionCommand("Copy to clipboard");
			mItema.addActionListener(mItemListener);
			menu.add(mItema);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseClicked(MouseEvent e) {

			if (e.getButton() == e.BUTTON3) {

				xPoint = e.getX();
				yPoint = e.getY();
				int row = tabSou.rowAtPoint(new Point(xPoint, yPoint));
				if (row >= 0 && row < tabSou.getRowCount()) {
					tabSou.setRowSelectionInterval(row, row);
				} else {
					tabSou.clearSelection();
				}
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
	 * The listener interface for receiving menuItem events. The class that is
	 * interested in processing a menuItem event implements this interface, and
	 * the object created with that class is registered with a component using
	 * the component's <code>addMenuItemListener<code> method. When
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

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent
		 * )
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
								// Terms.get().commit();
							}
							if (accEvent.getActionCommand().equals("Send as acceptable")) {
								contDescription.contextualizeThisDescription(targetLangRefset.getRefsetId(), acceptable.getConceptNid());
								// Terms.get().commit();
							}
							if (accEvent.getActionCommand().equals("Copy to clipboard")) {
								int row = tabSou.getSelectedRow();
								if (row >= 0) {
									ContextualizedDescription description = (ContextualizedDescription) tabSou.getModel().getValueAt(row, TableSourceColumn.TERM.ordinal());
									Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
									StringSelection strSel = new StringSelection(description.getText());
									clipboard.setContents(strSel, strSel);
								}
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
		 * @param contDescription
		 *            the new item
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
		 * @param name
		 *            the name
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
		 * @param name
		 *            the name
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
		if (detailsThread != null && detailsThread.isAlive()) {
			detailsThread.interrupt();
		}
		if (hierThread != null && hierThread.isAlive()) {
			hierThread.interrupt();
		}
		verifySavePending(null, false, false);
		clearForm(true);
	}

	/**
	 * Verifies changes to target description.
	 * 
	 * @param message
	 *            <\T> if null
	 * @param doVerify
	 *            the do verify
	 * @return true, if successful
	 */
	synchronized public boolean verifySavePending(String message, boolean doVerify, boolean directSave) {
		boolean bPendTerm = true;
		if (saveDesc) {
			if (doVerify) {
				if (targetTextField.getText().equals("") && tabTar.getRowCount() <= 0 && !alreadyVerified && message == null) {
					return selectOptionForNullTerms();
				} else {
					alreadyVerified = true;
				}
			}
			if (descriptionInEditor != null) {
				if (!(descriptionInEditor.getText().trim().equals(targetTextField.getText().trim()) && (descriptionInEditor.isInitialCaseSignificant() == rbYes.isSelected())
						&& descriptionInEditor.getAcceptabilityId() == ((I_GetConceptData) cmbAccep.getSelectedItem()).getConceptNid()
						&& ((descriptionInEditor.getExtensionStatusId() == active.getConceptNid() && rbAct.isSelected()) || (descriptionInEditor.getExtensionStatusId() != active.getConceptNid() && !rbAct.isSelected())) && ((descriptionInEditor
						.getTypeId() == fsn.getConceptNid() && fsn.equals((I_GetConceptData) comboBox1.getSelectedItem())) || (descriptionInEditor.getTypeId() != fsn.getConceptNid() && !fsn.equals((I_GetConceptData) comboBox1.getSelectedItem()))))) {
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

				int n = 1;
				if (!directSave) {
					n = JOptionPane.showOptionDialog(null, message1, "Unsaved data", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[1]);
				}
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
				} else if (!bPendTerm || directSave) {
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
				if (!collection.isEmpty()) {
					JOptionPane.showMessageDialog(LogWithAlerts.getActiveFrame(null), "There are uncommitted changes - please cancel or commit before continuing.", "", JOptionPane.ERROR_MESSAGE);
					return false;
				}
			}
		}
		return bPendTerm;
	}

	/**
	 * Select option for null terms.
	 * 
	 * @return true, if successful
	 */
	private boolean selectOptionForNullTerms() {
		alreadyVerified = true;
		Object[] options = { "Send empty translation", "Cancel" };
		int n = JOptionPane.showOptionDialog(null, "There is no translation in target language, would you like to continue?", "Unsaved data", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[1]);
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
		} else {
			if (saveDescActionPerformed()) {

				descriptionInEditor = null;
				targetTextField.setText("");
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * Sets the read only mode.
	 * 
	 * @param readOnly
	 *            the new read only mode
	 */
	private void setReadOnlyMode(boolean readOnly) {
		this.readOnlyMode = readOnly;
		rbInact.setEnabled(true && !readOnlyMode);
		rbYes.setEnabled(true && !readOnlyMode);
		bLaunch.setEnabled(true);
		cmbActions.setEnabled(true);
		button1.setEnabled(true);
	}

	/**
	 * Clear form.
	 * 
	 * @param clearAll
	 *            the clear all
	 */
	synchronized public void clearForm(boolean clearAll) {
		descriptionInEditor = null;
		comboBox1.setEnabled(false);
		cmbAccep.setEnabled(false);
		// label4.setVisible(true);
		// label4.setText("");
		targetTextField.setText("");
		targetTextField.setEnabled(false);
		rbYes.setSelected(false);
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
			//	issueListPanel.loadIssues(null, null, null);
			}
			tabbedPane3.setSelectedIndex(0);
			commentsManagerPanel.clearComments();
			setMnemoInit();
		}
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
				if (verifySavePending(null, false, false)) {
					descriptionInEditor = null;
					targetTextField.setText("");
					targetTextField.setEnabled(true && !readOnlyMode);
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
		if (verifySavePending(null, false, false)) {
			descriptionInEditor = null;
			targetTextField.setText("");
			targetTextField.setEnabled(true && !readOnlyMode);
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
		if (verifySavePending(null, false, false)) {
			descriptionInEditor = null;
			targetTextField.setText("");
			targetTextField.setEnabled(true && !readOnlyMode);
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
			if (confTransMod.isEnableSpellChecker() && targetTextChanged) {
				targetTextField.setText(DocumentManager.spellcheckPhrase(targetTextField.getText(), null, targetLangRefset.getLangCode(config)));
				targetTextChanged = false;
			}

			if (descriptionInEditor == null && !targetTextField.getText().trim().equals("") && rbAct.isSelected()) {
				descriptionInEditor = (ContextualizedDescription) ContextualizedDescription.createNewContextualizedDescription(concept.getConceptNid(), targetId, targetLangRefset.getLangCode(config));
			}
			if (descriptionInEditor != null) {

				String targetLangCode = "";
				try {
					targetLangCode = targetLangRefset.getLangCode(Terms.get().getActiveAceFrameConfig());
				} catch (TerminologyException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				descriptionInEditor.setText(targetTextField.getText());
				descriptionInEditor.setInitialCaseSignificant(rbYes.isSelected());

				// set description type like RF1
				if (targetLangCode.equals(descriptionInEditor.getLang())) {
					if (((I_GetConceptData) comboBox1.getSelectedItem()).equals(synonym)) {
						descriptionInEditor.setTypeId(synonym.getConceptNid());
					} else {
						descriptionInEditor.setTypeId(fsn.getConceptNid());
					}
				}
				// if some is wrong then all to retire
				if (rbInact.isSelected()) {
					if (!targetLangCode.equals(descriptionInEditor.getLang())) {
						descriptionInEditor.setExtensionStatusId(inactive.getConceptNid());
					} else {
						descriptionInEditor.setExtensionStatusId(inactive.getConceptNid());
						descriptionInEditor.setDescriptionStatusId(inactive.getConceptNid());
					}
				} else {
					if (!targetLangCode.equals(descriptionInEditor.getLang())) {
						descriptionInEditor.setExtensionStatusId(active.getConceptNid());
					} else {
						descriptionInEditor.setExtensionStatusId(active.getConceptNid());
						descriptionInEditor.setDescriptionStatusId(active.getConceptNid());
					}
				}
				descriptionInEditor.setAcceptabilityId(((I_GetConceptData) cmbAccep.getSelectedItem()).getConceptNid());

				result = descriptionInEditor.persistChanges();
				try {
					LanguageUtil.generateFSN(concept, sourceLangRefsets.iterator().next(), targetLangRefset, translationProject, config);

				} catch (FSNGenerationException e1) {
					e1.printStackTrace();

					JOptionPane.showOptionDialog(this, e1.getMessage(), "Warning", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, null, null);
				}
			}

			if (result) {
				clearForm(false);
			}

		} catch (IOException e1) {
			e1.printStackTrace();
			JOptionPane.showOptionDialog(this, e1.getMessage(), "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, null, null);

			return false;
		} catch (TerminologyException e1) {
			e1.printStackTrace();
			JOptionPane.showOptionDialog(this, e1.getMessage(), "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, null, null);

			return false;
		} catch (Exception e1) {
			e1.printStackTrace();
			JOptionPane.showOptionDialog(this, e1.getMessage(), "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, null, null);

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
		TranslationHelperPanel thp;
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
	 * Label10 mouse clicked.
	 * 
	 * @param e
	 *            the e
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
	 * @param e
	 *            the e
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
	 * @param e
	 *            the e
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
	 * @param e
	 *            the e
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
	 * Delete comment action performed.
	 * 
	 * @param e
	 *            the e
	 */
	private void deleteCommentActionPerformed(ActionEvent e) {

	}

	/**
	 * View comment action performed.
	 * 
	 * @param e
	 *            the e
	 */
	private void viewCommentActionPerformed(ActionEvent e) {
	}

	/**
	 * Target text field mouse clicked.
	 * 
	 * @param e
	 *            the e
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
	 * @param e
	 *            the e
	 */
	private void saveZoomButtonActionPerformed(ActionEvent e) {
		targetTextField.setText(zoomTextArea.getText());
		termZoomDialog.dispose();
		zoomTextArea.setText("");
	}

	/**
	 * Cancel zoom change action performed.
	 * 
	 * @param e
	 *            the e
	 */
	private void cancelZoomChangeActionPerformed(ActionEvent e) {
		termZoomDialog.dispose();
		zoomTextArea.setText("");
	}

	/**
	 * B launch action performed.
	 */
	private void bLaunchActionPerformed() {
		Object actionObj = cmbActions.getSelectedItem();
		if (actionObj instanceof WfAction) {
			WfAction action = (WfAction) cmbActions.getSelectedItem();
			if (action != null) {
				I_Work worker = null;
				try {
					if (targetTextField.getText().equals("") && tabTar.getRowCount() <= 0 && !alreadyVerified) {
						if (!selectOptionForNullTerms())
							return;
					}

					if (saveDescActionPerformed()) {
						descriptionInEditor = null;
						targetTextField.setText("");
					} else {
						return;
					}
					worker = Terms.get().getActiveAceFrameConfig().getWorker();
					WfInstance prevWfInstance = new WfInstance();
					prevWfInstance.setComponentId(instance.getComponentId());
					prevWfInstance.setDestination(instance.getDestination());
					prevWfInstance.setHistory(instance.getHistory());
					prevWfInstance.setProperties(instance.getProperties());
					prevWfInstance.setState(instance.getState());
					prevWfInstance.setWfDefinition(instance.getWfDefinition());
					prevWfInstance.setWorkList(instance.getWorkList());
					workflowInterpreter.doAction(instance, wfRole, action, worker);
					WfInstance newWfInstance = worklistMember.getWfInstance();
					newWfInstance.setActionReport(instance.getActionReport());
					clearForm(true);
					setReadOnlyMode(true);
					bLaunch.setEnabled(false);
					button1.setEnabled(false);
					firePropertyChange(TranslationPanel.ACTION_LAUNCHED, prevWfInstance, newWfInstance);
				} catch (TerminologyException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				} catch (IOException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		} else if (actionObj instanceof String && actionObj.toString().equals(WfAction.SEND_TO_OUTBOX)) {
			clearForm(true);
			setReadOnlyMode(true);
			bLaunch.setEnabled(false);
			button1.setEnabled(false);
			WfInstance prevWfInstance = new WfInstance();
			firePropertyChange(TranslationPanel.SEND_TO_OUTBOX_LAUNCHED, instance, prevWfInstance);
		}
	}

	/**
	 * Button1 action performed.
	 */
	private void button1ActionPerformed() {
		I_Work worker = null;
		try {
			worker = Terms.get().getActiveAceFrameConfig().getWorker();
			workflowInterpreter.doAction(instance, wfRole, cancAction, worker);
			WfInstance newWfInstance = instance;
			updateUI(instance, false);
			firePropertyChange(TranslationPanel.ACTION_LAUNCHED, instance, newWfInstance);
		} catch (TerminologyException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void saveAndAddActionPerformed(ActionEvent e) {
		if (verifySavePending(null, true, true)) {
			descriptionInEditor = null;
			targetTextField.setText("");
			targetTextField.setEnabled(true && !readOnlyMode);
			saveDesc = true;
			mSpellChk.setEnabled(true);
			comboBox1.setSelectedItem(synonym);
			cmbAccep.setSelectedItem(acceptable);
		}
	}

	private void sendMenuItemActionPerformed(ActionEvent e) {
		bLaunchActionPerformed();
	}

	private void saveSimpleActionPerformed(ActionEvent e) {
		I_ConfigAceFrame config = null;
		try {
			config = Terms.get().getActiveAceFrameConfig();
			ConfigTranslationModule confTransMod = LanguageUtil.getTranslationConfig(config);
			System.out.println(confTransMod.isEnableSpellChecker());
			if (confTransMod.isEnableSpellChecker() && targetTextChanged) {
				targetTextField.setText(DocumentManager.spellcheckPhrase(targetTextField.getText(), null, targetLangRefset.getLangCode(config)));
				targetTextChanged = false;
			}

			if (descriptionInEditor == null && !targetTextField.getText().trim().equals("") && rbAct.isSelected()) {
				descriptionInEditor = (ContextualizedDescription) ContextualizedDescription.createNewContextualizedDescription(concept.getConceptNid(), targetId, targetLangRefset.getLangCode(config));
			}
			if (descriptionInEditor != null) {

				String targetLangCode = "";
				try {
					targetLangCode = targetLangRefset.getLangCode(Terms.get().getActiveAceFrameConfig());
				} catch (TerminologyException ex) {
					ex.printStackTrace();
				} catch (IOException ex) {
					ex.printStackTrace();
				}

				descriptionInEditor.setText(targetTextField.getText());
				descriptionInEditor.setInitialCaseSignificant(rbYes.isSelected());

				// set description type like RF1
				if (targetLangCode.equals(descriptionInEditor.getLang())) {
					if (((I_GetConceptData) comboBox1.getSelectedItem()).equals(synonym)) {
						descriptionInEditor.setTypeId(synonym.getConceptNid());
					} else {
						descriptionInEditor.setTypeId(fsn.getConceptNid());
					}
				}
				// if some is wrong then all to retire
				if (rbInact.isSelected()) {
					if (!targetLangCode.equals(descriptionInEditor.getLang())) {
						descriptionInEditor.setExtensionStatusId(inactive.getConceptNid());
					} else {
						descriptionInEditor.setExtensionStatusId(inactive.getConceptNid());
						descriptionInEditor.setDescriptionStatusId(inactive.getConceptNid());
					}
				} else {
					if (!targetLangCode.equals(descriptionInEditor.getLang())) {
						descriptionInEditor.setExtensionStatusId(active.getConceptNid());
					} else {
						descriptionInEditor.setExtensionStatusId(active.getConceptNid());
						descriptionInEditor.setDescriptionStatusId(active.getConceptNid());
					}
				}
				descriptionInEditor.setAcceptabilityId(((I_GetConceptData) cmbAccep.getSelectedItem()).getConceptNid());

				descriptionInEditor.persistChanges();
				try {
					LanguageUtil.generateFSN(concept, sourceLangRefsets.iterator().next(), targetLangRefset, translationProject, config);

				} catch (FSNGenerationException e1) {
					e1.printStackTrace();

					JOptionPane.showOptionDialog(this, e1.getMessage(), "Warning", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, null, null);
				}
			}
			populateTargetTree();
		} catch (IOException e1) {
			e1.printStackTrace();
			JOptionPane.showOptionDialog(this, e1.getMessage(), "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, null, null);
		} catch (TerminologyException e1) {
			e1.printStackTrace();
			JOptionPane.showOptionDialog(this, e1.getMessage(), "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, null, null);
		} catch (Exception e1) {
			e1.printStackTrace();
			JOptionPane.showOptionDialog(this, e1.getMessage(), "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, null, null);
		}

	}

	/**
	 * The listener interface for receiving selection events. The class that is
	 * interested in processing a selection event implements this interface, and
	 * the object created with that class is registered with a component using
	 * the component's <code>addSelectionListener<code> method. When
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
					} else {
						System.out.println("************  descrpt null");
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
		panel1 = new JPanel();
		menuBar1 = new JMenuBar();
		menu1 = new JMenu();
		bAddFSN = new JMenuItem();
		mAddPref = new JMenuItem();
		mAddDesc = new JMenuItem();
		saveSimple = new JMenuItem();
		saveAndAdd = new JMenuItem();
		menu3 = new JMenu();
		mSpellChk = new JMenuItem();
		menu2 = new JMenu();
		mHist = new JMenuItem();
		mLog = new JMenuItem();
		menu4 = new JMenu();
		menuItem1 = new JMenuItem();
		actionMenu = new JMenu();
		sendMenuItem = new JMenuItem();
		label14 = new JLabel();
		label15 = new JLabel();
		label16 = new JLabel();
		label10 = new JLabel();
		splitPane3 = new JSplitPane();
		splitPane5 = new JSplitPane();
		panel9 = new JPanel();
		label9 = new JLabel();
		scrollPane4 = new JScrollPane();
		tabSou = new ZebraJTable();
		tabbedPane2 = new JTabbedPane();
		panel11 = new JPanel();
		commentsManagerPanel = new CommentsManagerPanel();
		splitPane7 = new JSplitPane();
		panel10 = new JPanel();
		panel13 = new JPanel();
		label11 = new JLabel();
		scrollPane9 = new JScrollPane();
		tabTar = new ZebraJTable();
		panel19 = new JPanel();
		label4 = new JLabel();
		cmbActions = new JComboBox();
		bLaunch = new JButton();
		button1 = new JButton();
		label12 = new JLabel();
		panel2 = new JPanel();
		panel15 = new JPanel();
		separator1 = new JSeparator();
		label2 = new JLabel();
		scrollPane5 = new JScrollPane();
		targetTextField = new JTextArea();
		panel18 = new JPanel();
		label1 = new JLabel();
		comboBox1 = new JComboBox();
		label7 = new JLabel();
		rbAct = new JRadioButton();
		rbInact = new JRadioButton();
		label5 = new JLabel();
		cmbAccep = new JComboBox();
		label3 = new JLabel();
		rbYes = new JRadioButton();
		rbNo = new JRadioButton();
		label13 = new JLabel();
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
		setLayout(new BorderLayout());

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
					bAddFSN.setText("Add FSN");
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
					mAddPref.setText("Add Preferred");
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
					mAddDesc.setText("Add Description");
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

					//---- saveSimple ----
					saveSimple.setText("Save");
					saveSimple.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							saveSimpleActionPerformed(e);
						}
					});
					menu1.add(saveSimple);

					//---- saveAndAdd ----
					saveAndAdd.setText("Save And Add Description");
					saveAndAdd.setMnemonic('A');
					saveAndAdd.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()|KeyEvent.SHIFT_MASK));
					saveAndAdd.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							saveAndAddActionPerformed(e);
						}
					});
					menu1.add(saveAndAdd);
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

				//======== actionMenu ========
				{
					actionMenu.setText("Act[i]on");
					actionMenu.setMnemonic('I');

					//---- sendMenuItem ----
					sendMenuItem.setText("Send");
					sendMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()|KeyEvent.SHIFT_MASK));
					sendMenuItem.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							sendMenuItemActionPerformed(e);
						}
					});
					actionMenu.add(sendMenuItem);
					actionMenu.addSeparator();
				}
				menuBar1.add(actionMenu);
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
		add(panel1, BorderLayout.NORTH);

		//======== splitPane3 ========
		{
			splitPane3.setResizeWeight(0.2);

			//======== splitPane5 ========
			{
				splitPane5.setResizeWeight(0.5);
				splitPane5.setOrientation(JSplitPane.VERTICAL_SPLIT);

				//======== panel9 ========
				{
					panel9.setBackground(new Color(238, 238, 238));
					panel9.setMinimumSize(new Dimension(25, 25));
					panel9.setPreferredSize(new Dimension(25, 25));
					panel9.setLayout(new GridBagLayout());
					((GridBagLayout)panel9.getLayout()).columnWidths = new int[] {0, 0};
					((GridBagLayout)panel9.getLayout()).rowHeights = new int[] {20, 90, 0};
					((GridBagLayout)panel9.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
					((GridBagLayout)panel9.getLayout()).rowWeights = new double[] {0.0, 1.0, 1.0E-4};

					//---- label9 ----
					label9.setText("Source Language");
					label9.setMaximumSize(new Dimension(25, 16));
					label9.setMinimumSize(new Dimension(25, 16));
					label9.setPreferredSize(new Dimension(25, 16));
					label9.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
					panel9.add(label9, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 5, 0), 0, 0));

					//======== scrollPane4 ========
					{
						scrollPane4.setPreferredSize(new Dimension(23, 27));

						//---- tabSou ----
						tabSou.setPreferredScrollableViewportSize(new Dimension(20, 20));
						tabSou.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
						scrollPane4.setViewportView(tabSou);
					}
					panel9.add(scrollPane4, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
				}
				splitPane5.setTopComponent(panel9);

				//======== tabbedPane2 ========
				{
					tabbedPane2.setMinimumSize(new Dimension(10, 10));
					tabbedPane2.setPreferredSize(new Dimension(10, 10));

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
					tabbedPane2.setMnemonicAt(0, 'U');
					tabbedPane2.addTab("Comments", commentsManagerPanel);

				}
				splitPane5.setBottomComponent(tabbedPane2);
			}
			splitPane3.setLeftComponent(splitPane5);

			//======== splitPane7 ========
			{
				splitPane7.setResizeWeight(0.5);
				splitPane7.setOrientation(JSplitPane.VERTICAL_SPLIT);
				splitPane7.setMinimumSize(new Dimension(10, 10));
				splitPane7.setPreferredSize(new Dimension(10, 10));

				//======== panel10 ========
				{
					panel10.setBorder(new EmptyBorder(5, 5, 5, 5));
					panel10.setLayout(new BorderLayout(5, 5));

					//======== panel13 ========
					{
						panel13.setLayout(new GridBagLayout());
						((GridBagLayout)panel13.getLayout()).columnWidths = new int[] {0, 0};
						((GridBagLayout)panel13.getLayout()).rowHeights = new int[] {0, 75, 0};
						((GridBagLayout)panel13.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
						((GridBagLayout)panel13.getLayout()).rowWeights = new double[] {0.0, 1.0, 1.0E-4};

						//---- label11 ----
						label11.setText("Target Language");
						label11.setBackground(new Color(238, 238, 238));
						label11.setMaximumSize(new Dimension(10, 16));
						label11.setMinimumSize(new Dimension(10, 16));
						label11.setPreferredSize(new Dimension(10, 16));
						label11.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
						panel13.add(label11, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 0), 0, 0));

						//======== scrollPane9 ========
						{
							scrollPane9.setPreferredSize(new Dimension(23, 120));
							scrollPane9.setMinimumSize(new Dimension(23, 120));

							//---- tabTar ----
							tabTar.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
							scrollPane9.setViewportView(tabTar);
						}
						panel13.add(scrollPane9, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 0), 0, 0));
					}
					panel10.add(panel13, BorderLayout.NORTH);

					//======== panel19 ========
					{
						panel19.setLayout(new GridBagLayout());
						((GridBagLayout)panel19.getLayout()).columnWidths = new int[] {77, 172, 0, 0, 0, 0};
						((GridBagLayout)panel19.getLayout()).rowHeights = new int[] {0, 0};
						((GridBagLayout)panel19.getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0, 0.0, 0.0, 1.0E-4};
						((GridBagLayout)panel19.getLayout()).rowWeights = new double[] {0.0, 1.0E-4};

						//---- label4 ----
						label4.setText("Action");
						label4.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
						panel19.add(label4, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 5), 0, 0));

						//---- cmbActions ----
						cmbActions.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
						panel19.add(cmbActions, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 5), 0, 0));

						//---- bLaunch ----
						bLaunch.setText("Save");
						bLaunch.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
						bLaunch.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								bLaunchActionPerformed();
							}
						});
						panel19.add(bLaunch, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
							new Insets(0, 0, 0, 5), 0, 0));

						//---- button1 ----
						button1.setText("Cancel");
						button1.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
						button1.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								button1ActionPerformed();
							}
						});
						panel19.add(button1, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 5), 0, 0));

						//---- label12 ----
						label12.addMouseListener(new MouseAdapter() {
							@Override
							public void mouseClicked(MouseEvent e) {
								label12MouseClicked(e);
							}
						});
						panel19.add(label12, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 0), 0, 0));
					}
					panel10.add(panel19, BorderLayout.SOUTH);

					//======== panel2 ========
					{
						panel2.setLayout(new GridBagLayout());
						((GridBagLayout)panel2.getLayout()).columnWidths = new int[] {0, 0};
						((GridBagLayout)panel2.getLayout()).rowHeights = new int[] {0, 0, 0};
						((GridBagLayout)panel2.getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
						((GridBagLayout)panel2.getLayout()).rowWeights = new double[] {1.0, 1.0, 1.0E-4};

						//======== panel15 ========
						{
							panel15.setMinimumSize(new Dimension(10, 20));
							panel15.setPreferredSize(new Dimension(10, 20));
							panel15.setLayout(new GridBagLayout());
							((GridBagLayout)panel15.getLayout()).columnWidths = new int[] {80, 0, 0};
							((GridBagLayout)panel15.getLayout()).rowHeights = new int[] {18, 0, 0};
							((GridBagLayout)panel15.getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0E-4};
							((GridBagLayout)panel15.getLayout()).rowWeights = new double[] {0.0, 1.0, 1.0E-4};
							panel15.add(separator1, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.BOTH,
								new Insets(0, 0, 5, 0), 0, 0));

							//---- label2 ----
							label2.setText("Term:");
							label2.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
							panel15.add(label2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
								GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
								new Insets(0, 0, 0, 5), 0, 0));

							//======== scrollPane5 ========
							{

								//---- targetTextField ----
								targetTextField.setRows(5);
								targetTextField.setLineWrap(true);
								targetTextField.setPreferredSize(new Dimension(0, 32));
								targetTextField.setMinimumSize(new Dimension(0, 32));
								targetTextField.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
								targetTextField.addMouseListener(new MouseAdapter() {
									@Override
									public void mouseClicked(MouseEvent e) {
										targetTextFieldMouseClicked(e);
									}
								});
								scrollPane5.setViewportView(targetTextField);
							}
							panel15.add(scrollPane5, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.BOTH,
								new Insets(0, 0, 0, 0), 0, 0));
						}
						panel2.add(panel15, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 5, 0), 0, 0));

						//======== panel18 ========
						{
							panel18.setPreferredSize(new Dimension(10, 12));
							panel18.setMinimumSize(new Dimension(10, 12));
							panel18.setLayout(new GridBagLayout());
							((GridBagLayout)panel18.getLayout()).columnWidths = new int[] {0, 248, 0, 0, 0, 0, 0};
							((GridBagLayout)panel18.getLayout()).rowHeights = new int[] {0, 0, 18, 0};
							((GridBagLayout)panel18.getLayout()).columnWeights = new double[] {0.0, 1.0, 1.0, 0.0, 1.0, 1.0, 1.0E-4};
							((GridBagLayout)panel18.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 1.0E-4};

							//---- label1 ----
							label1.setText("Term type");
							label1.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
							panel18.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.BOTH,
								new Insets(0, 0, 5, 5), 0, 0));

							//---- comboBox1 ----
							comboBox1.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
							panel18.add(comboBox1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.BOTH,
								new Insets(0, 0, 5, 5), 0, 0));

							//---- label7 ----
							label7.setText("Status:");
							label7.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
							panel18.add(label7, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.BOTH,
								new Insets(0, 0, 5, 5), 0, 0));

							//---- rbAct ----
							rbAct.setText("Active");
							rbAct.setSelected(true);
							rbAct.setBackground(new Color(238, 238, 238));
							rbAct.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
							panel18.add(rbAct, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.BOTH,
								new Insets(0, 0, 5, 5), 0, 0));

							//---- rbInact ----
							rbInact.setText("Inactive");
							rbInact.setBackground(new Color(238, 238, 238));
							rbInact.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
							panel18.add(rbInact, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.BOTH,
								new Insets(0, 0, 5, 0), 0, 0));

							//---- label5 ----
							label5.setText("Acceptability:");
							label5.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
							panel18.add(label5, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.BOTH,
								new Insets(0, 0, 5, 5), 0, 0));

							//---- cmbAccep ----
							cmbAccep.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
							panel18.add(cmbAccep, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.BOTH,
								new Insets(0, 0, 5, 5), 0, 0));

							//---- label3 ----
							label3.setText("Is case significant?");
							label3.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
							panel18.add(label3, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.BOTH,
								new Insets(0, 0, 5, 5), 0, 0));

							//---- rbYes ----
							rbYes.setText("Yes");
							rbYes.setBackground(new Color(238, 238, 238));
							rbYes.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
							panel18.add(rbYes, new GridBagConstraints(4, 1, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.BOTH,
								new Insets(0, 0, 5, 5), 0, 0));

							//---- rbNo ----
							rbNo.setSelected(true);
							rbNo.setText("No");
							rbNo.setBackground(new Color(238, 238, 238));
							rbNo.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
							panel18.add(rbNo, new GridBagConstraints(5, 1, 1, 1, 0.0, 0.0,
								GridBagConstraints.CENTER, GridBagConstraints.BOTH,
								new Insets(0, 0, 5, 0), 0, 0));

							//---- label13 ----
							label13.setText("text");
							label13.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
							label13.addMouseListener(new MouseAdapter() {
								@Override
								public void mouseClicked(MouseEvent e) {
									label13MouseClicked(e);
								}
							});
							panel18.add(label13, new GridBagConstraints(5, 2, 1, 1, 0.0, 0.0,
								GridBagConstraints.EAST, GridBagConstraints.VERTICAL,
								new Insets(0, 0, 0, 0), 0, 0));
						}
						panel2.add(panel18, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
							GridBagConstraints.CENTER, GridBagConstraints.BOTH,
							new Insets(0, 0, 0, 0), 0, 0));
					}
					panel10.add(panel2, BorderLayout.CENTER);
				}
				splitPane7.setTopComponent(panel10);

				//======== tabbedPane3 ========
				{
					tabbedPane3.setMinimumSize(new Dimension(10, 70));
					tabbedPane3.setPreferredSize(new Dimension(10, 70));

					//======== scrollPane7 ========
					{
						scrollPane7.setPreferredSize(new Dimension(23, 23));

						//---- tree3 ----
						tree3.setVisibleRowCount(4);
						tree3.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
						scrollPane7.setViewportView(tree3);
					}
					tabbedPane3.addTab("Concept Details", scrollPane7);

					tabbedPane3.addTab("Hierarchy", hierarchyNavigator1);
					tabbedPane3.setMnemonicAt(1, 'H');
				}
				splitPane7.setBottomComponent(tabbedPane3);
			}
			splitPane3.setRightComponent(splitPane7);
		}
		add(splitPane3, BorderLayout.CENTER);

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

		//---- buttonGroup3 ----
		ButtonGroup buttonGroup3 = new ButtonGroup();
		buttonGroup3.add(rbAct);
		buttonGroup3.add(rbInact);

		//---- buttonGroup1 ----
		ButtonGroup buttonGroup1 = new ButtonGroup();
		buttonGroup1.add(rbYes);
		buttonGroup1.add(rbNo);
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	private JPanel panel1;
	private JMenuBar menuBar1;
	private JMenu menu1;
	private JMenuItem bAddFSN;
	private JMenuItem mAddPref;
	private JMenuItem mAddDesc;
	private JMenuItem saveSimple;
	private JMenuItem saveAndAdd;
	private JMenu menu3;
	private JMenuItem mSpellChk;
	private JMenu menu2;
	private JMenuItem mHist;
	private JMenuItem mLog;
	private JMenu menu4;
	private JMenuItem menuItem1;
	private JMenu actionMenu;
	private JMenuItem sendMenuItem;
	private JLabel label14;
	private JLabel label15;
	private JLabel label16;
	private JLabel label10;
	private JSplitPane splitPane3;
	private JSplitPane splitPane5;
	private JPanel panel9;
	private JLabel label9;
	private JScrollPane scrollPane4;
	private ZebraJTable tabSou;
	private JTabbedPane tabbedPane2;
	private JPanel panel11;
	private CommentsManagerPanel commentsManagerPanel;
	private JSplitPane splitPane7;
	private JPanel panel10;
	private JPanel panel13;
	private JLabel label11;
	private JScrollPane scrollPane9;
	private ZebraJTable tabTar;
	private JPanel panel19;
	private JLabel label4;
	private JComboBox cmbActions;
	private JButton bLaunch;
	private JButton button1;
	private JLabel label12;
	private JPanel panel2;
	private JPanel panel15;
	private JSeparator separator1;
	private JLabel label2;
	private JScrollPane scrollPane5;
	private JTextArea targetTextField;
	private JPanel panel18;
	private JLabel label1;
	private JComboBox comboBox1;
	private JLabel label7;
	private JRadioButton rbAct;
	private JRadioButton rbInact;
	private JLabel label5;
	private JComboBox cmbAccep;
	private JLabel label3;
	private JRadioButton rbYes;
	private JRadioButton rbNo;
	private JLabel label13;
	private JTabbedPane tabbedPane3;
	private JScrollPane scrollPane7;
	private JTree tree3;
	private HierarchyNavigator hierarchyNavigator1;
	private JPopupMenu popupMenu1;
	private JMenuItem menuItem2;
	private JMenuItem menuItem3;
	private JDialog termZoomDialog;
	private JPanel panel12;
	private JScrollPane scrollPane2;
	private JTextArea zoomTextArea;
	private JButton saveZoomButton;
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

	/** The scrollp. */
	private JScrollPane scrollp;

	/** The workflow definition. */
	private WorkflowDefinition workflowDefinition;

	/** The workflow interpreter. */
	private WorkflowInterpreter workflowInterpreter;

	/** The component provider. */
	private WfComponentProvider componentProvider;

	/** The instance. */
	private WfInstance instance;

	/** The details thread. */
	private Thread detailsThread;

	/** The hier thread. */
	private Thread hierThread;

	/** The wf role. */
	private WfRole wfRole;

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
	 * @throws Exception
	 *             the exception
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
				for (I_GetConceptData langRefset : this.translationProject.getSourceLanguageRefsets()) {
					List<ContextualizedDescription> descriptions = LanguageUtil.getContextualizedDescriptions(concept.getConceptNid(), langRefset.getConceptNid(), true);

					boolean bSourceFSN = false;
					boolean bNewNode = false;
					for (I_ContextualizeDescription description : descriptions) {
						if (description.getLanguageExtension() != null && description.getLanguageRefsetId() == langRefset.getConceptNid()) {
							bNewNode = false;
							Object[] rowClass = new Object[2];
							Object[] termType_Status = new Object[2];
							Object[] row = new Object[TableSourceColumn.values().length];
							row[TableSourceColumn.TERM.ordinal()] = description;
							row[TableSourceColumn.LANGUAGE.ordinal()] = description.getLang();
							row[TableSourceColumn.ICS.ordinal()] = description.isInitialCaseSignificant();

							if (description.getExtensionStatusId() == inactive.getConceptNid() || description.getDescriptionStatusId() == inactive.getConceptNid()) {
								if (sourceCom.contains(ConfigTranslationModule.TreeComponent.RETIRED)) {
									rowClass[0] = TreeEditorObjectWrapper.NOTACCEPTABLE;
									// row[TableSourceColumn.ACCEPTABILITY.ordinal()]
									// = notAcceptable;
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

								sourceFsnConcept = description;

								if (sourceCom.contains(ConfigTranslationModule.TreeComponent.FSN)) {
									rowClass[0] = TreeEditorObjectWrapper.FSNDESCRIPTION;
									row[TableSourceColumn.ACCEPTABILITY.ordinal()] = preferred;
									termType_Status[0] = fsn;
									termType_Status[1] = active;
									row[TableSourceColumn.TERM_TYPE.ordinal()] = termType_Status;

									bNewNode = true;
								}
								if (!bSourceFSN) {
									bSourceFSN = true;
								}
							} else if (description.getAcceptabilityId() == acceptable.getConceptNid() && sourceCom.contains(ConfigTranslationModule.TreeComponent.SYNONYM)) {
								rowClass[0] = TreeEditorObjectWrapper.SYNONYMN;
								row[TableSourceColumn.ACCEPTABILITY.ordinal()] = acceptable;
								termType_Status[0] = this.synonym;
								termType_Status[1] = active;
								row[TableSourceColumn.TERM_TYPE.ordinal()] = termType_Status;
								bNewNode = true;
							} else if (description.getAcceptabilityId() == preferred.getConceptNid()) {
								sourcePreferredConcept = description;
								if (sourceCom.contains(ConfigTranslationModule.TreeComponent.PREFERRED)) {
									rowClass[0] = TreeEditorObjectWrapper.PREFERRED;
									row[TableSourceColumn.ACCEPTABILITY.ordinal()] = preferred;
									termType_Status[0] = this.synonym;
									termType_Status[1] = active;
									row[TableSourceColumn.TERM_TYPE.ordinal()] = termType_Status;
									bNewNode = true;
								}
							} else if (sourceCom.contains(ConfigTranslationModule.TreeComponent.RETIRED)) {
								rowClass[0] = TreeEditorObjectWrapper.SYNONYMN;
								// row[TableSourceColumn.ACCEPTABILITY.ordinal()]
								// = notAcceptable;
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

					/** update similarity table */
					SimilarityPanel similPanel = getSimilarityPanel();
					similPanel.updateTabs(sourceFsnConcept, sourcePreferredConcept, concept, sourceIds, targetId, translationProject, worklistMember);
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

//		if (isMemberLogOpen()) {
//			mLogActionPerformed();
//		}

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
	 * @throws Exception
	 *             the exception
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
				I_GetConceptData langRefset = this.translationProject.getTargetLanguageRefset();
				if (langRefset == null) {
					JOptionPane.showMessageDialog(new JDialog(), "Target language refset cannot be retrieved\nCheck project details", "Error", JOptionPane.ERROR_MESSAGE);
					throw new Exception("Target language refset cannot be retrieved.");
				}
				List<ContextualizedDescription> descriptions = LanguageUtil.getContextualizedDescriptions(concept.getConceptNid(), langRefset.getConceptNid(), true);

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

						if (description.getExtensionStatusId() == inactive.getConceptNid() || description.getDescriptionStatusId() == inactive.getConceptNid()) {
							if (targetCom.contains(ConfigTranslationModule.TreeComponent.RETIRED)) {
								rowClass[0] = TreeEditorObjectWrapper.NOTACCEPTABLE;
								row[TableTargetColumn.ACCEPTABILITY.ordinal()] = "";
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
							// row[TableSourceColumn.ACCEPTABILITY.ordinal()] =
							// notAcceptable;
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
	 * @throws Exception
	 *             the exception
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

				if (attributes.getStatusNid() == inactive.getConceptNid()) {
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

				List<I_RelTuple> relationships = (List<I_RelTuple>) concept.getSourceRelTuples(config.getAllowedStatus(), config.getDestRelTypes(), config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy(),
						config.getClassifierConcept().getNid(), RelAssertionType.INFERRED);

				List<DefaultMutableTreeNode> nodesToAdd = new ArrayList<DefaultMutableTreeNode>();

				HashMap<Integer, List<DefaultMutableTreeNode>> mapGroup = new HashMap<Integer, List<DefaultMutableTreeNode>>();
				List<DefaultMutableTreeNode> roleList = new ArrayList<DefaultMutableTreeNode>();
				int group = 0;
				for (I_RelTuple relationship : relationships) {
					I_GetConceptData targetConcept = tf.getConcept(relationship.getC2Id());
					I_GetConceptData typeConcept = tf.getConcept(relationship.getTypeNid());
					String label = typeConcept + ": " + targetConcept;

					if ((relationship.getTypeNid() == snomedIsa.getConceptNid()) || (relationship.getTypeNid() == ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid())) {
						attributes = targetConcept.getConceptAttributeTuples(config.getPrecedence(), config.getConflictResolutionStrategy()).iterator().next();
						DefaultMutableTreeNode supertypeNode = null;
						if (attributes.getStatusNid() == inactive.getConceptNid()) {
							supertypeNode = new DefaultMutableTreeNode(new TreeEditorObjectWrapper(label, IconUtilities.INACTIVE_PARENT, relationship.getMutablePart()));

						} else if (attributes.isDefined()) {
							supertypeNode = new DefaultMutableTreeNode(new TreeEditorObjectWrapper(label, IconUtilities.DEFINED_PARENT, relationship.getMutablePart()));
						} else {
							supertypeNode = new DefaultMutableTreeNode(new TreeEditorObjectWrapper(label, IconUtilities.PRIMITIVE_PARENT, relationship.getMutablePart()));

						}
						nodesToAdd.add(supertypeNode);
					} else {
						if (relationship.getGroup() == 0) {
							if (relationship.getCharacteristicId() == definingChar || relationship.getCharacteristicId() == inferred) {
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

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent
		 * (javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
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

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 1L;

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent
		 * (javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
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

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent
		 * (javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
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

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent
		 * (javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
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

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent
		 * (javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
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
	 * @param descrpt
	 *            the descrpt
	 * @param rowModel
	 *            the row model
	 */
	private void updatePropertiesPanel(ContextualizedDescription descrpt, int rowModel) {
		boolean update = false;
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
					&& ((descriptionInEditor.getExtensionStatusId() == active.getConceptNid() && rbAct.isSelected()) || (descriptionInEditor.getExtensionStatusId() != active.getConceptNid() && !rbAct.isSelected()))
					&& ((descriptionInEditor.getTypeId() == fsn.getConceptNid() && fsn.equals((I_GetConceptData) comboBox1.getSelectedItem())) || (descriptionInEditor.getTypeId() != fsn.getConceptNid() && !fsn.equals((I_GetConceptData) comboBox1
							.getSelectedItem())))) {
				update = true;
			} else {
				Object[] options = { "Discard unsaved data", "Cancel and continue editing" };
				int n = JOptionPane.showOptionDialog(null, "Do you want to save the change you made to the term in the editor panel?", "Unsaved data", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, // do
						// not
						// use
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
						saveDesc = true;
						mSpellChk.setEnabled(true);
						if (!langCode.equals(descrpt.getLang())) {
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
	 * @param translationProject
	 *            the translation project
	 * @param workListMember
	 *            the work list member
	 * @param role
	 *            the role
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
			if (role != null) {
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
			} else {
				targetTextField.setEnabled(true);
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
			Runnable detailsThr = new Runnable() {
				public void run() {
					try {
						populateDetailsTree();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};

			detailsThread = new Thread(detailsThr);
			detailsThread.start();

			Runnable hierThr = new Runnable() {
				public void run() {
					try {
						hierarchyNavigator1.setContainerPanel(tabbedPane3);
						hierarchyNavigator1.setFocusConcept(Ts.get().getConceptVersion(Terms.get().getActiveAceFrameConfig().getViewCoordinate(), concept.getConceptNid()));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			};

			hierThread = new Thread(hierThr);
			hierThread.start();

			sourceICS = LanguageUtil.getDefaultICS(concept, sourceLangRefsets.iterator().next(), targetLangRefset, config, translConfig);
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
				saveAndAdd.setEnabled(false);
				mAddPref.setEnabled(true && !readOnlyMode);
				comboBox1.setEnabled(false);
				cmbAccep.setEnabled(false);
			} else if (translConfig.getSelectedEditorMode().equals(ConfigTranslationModule.EditorMode.SYNONYMS_EDITOR)) {
				mAddDescActionPerformed();
				bAddFSN.setEnabled(false);
				mAddDesc.setEnabled(true);
				saveAndAdd.setEnabled(true);
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
				saveAndAdd.setEnabled(true && !readOnlyMode);
				mAddPref.setEnabled(true && !readOnlyMode);
				comboBox1.setEnabled(true && !readOnlyMode);
				cmbAccep.setEnabled(true && !readOnlyMode);
			}
			tabbedPane2.remove(0);
		//	commentsManagerPanel = new CommentsManagerPanel(this.role, targetLangRefset, this.worklistMember);
			tabbedPane2.insertTab("Comments", null, commentsManagerPanel, "Translation component comments", 0);
			tabbedPane2.setSelectedIndex(0);

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
			targetTextChanged = false;
			// mClose.setEnabled(false);
		} catch (Exception e) {
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
//			if (regis != null && regis.getUserId() != null && regis.getPassword() != null) {
//				Integer issuesTot = issueListPanel.loadIssues(concept, repo, regis);
//				if (issuesTot != null && issuesTot > 0) {
//					tabbedPane2.setTitleAt(1, "<html>Issues <font><style color=red>*</style></font></html>");
//				}
//			}
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
			if (config == null) {
				issueListPanel = new IssuesListPanel2(readOnlyMode);
			} else {
				issueListPanel = new IssuesListPanel2(config, readOnlyMode);
			}

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
	 * Sets the button mnemo.
	 * 
	 * @param btton
	 *            the new button mnemo
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
	 * @param sourceLangRefsets
	 *            the new source lang refsets
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
	 * @param targetLangRefset
	 *            the new target lang refset
	 */
	public void setTargetLangRefset(LanguageMembershipRefset targetLangRefset) {
		this.targetLangRefset = targetLangRefset;
		targetId = targetLangRefset.getRefsetId();
	}

	/**
	 * Autokeep in inbox.
	 */
	public void autokeepInInbox() {
		if (this.keepIIClass != null) {
			this.unloaded = false;
			this.keepIIClass.KeepInInbox();
		}
	}

	/**
	 * Sets the auto keep function.
	 * 
	 * @param thisAutoKeep
	 *            the new auto keep function
	 */
	public void setAutoKeepFunction(I_KeepTaskInInbox thisAutoKeep) {
		this.keepIIClass = thisAutoKeep;

	}

	/**
	 * Sets the unloaded.
	 * 
	 * @param b
	 *            the new unloaded
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

	/**
	 * Update ui.
	 * 
	 * @param instance
	 *            the instance
	 * @param readOnlyMode
	 *            the read only mode
	 */
	public void updateUI(WfInstance instance, boolean readOnlyMode) {
		this.instance = instance;
		if (updateUiWorker != null && !updateUiWorker.isDone()) {
			updateUiWorker.cancel(true);
			updateUiWorker = null;
		}
		updateUiWorker = new UpdateUIWorker(instance, readOnlyMode);
		updateUiWorker.execute();
	}

	/**
	 * Sets the possible actions.
	 * 
	 * @param actions
	 *            the new possible actions
	 */
	public void setPossibleActions(List<WfAction> actions) {

		actionMenu.removeAll();
		actionMenu.add(sendMenuItem);
		actionMenu.addSeparator();
		cmbActions.removeAllItems();

		for (WfAction action : actions) {
			cmbActions.addItem(action);
			JMenuItem actionItem = new JMenuItem();
			// ---- action menue item ----
			actionItem.setText(action.toString());
			actionItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					Object source = arg0.getSource();
					if (source instanceof JMenuItem) {
						JMenuItem sourceMenuItem = (JMenuItem) source;
						String actionText = sourceMenuItem.getText();
						int cmbActionItemCount = cmbActions.getItemCount();
						for (int i = 0; i < cmbActionItemCount; i++) {
							Object cmbActionItem = cmbActions.getItemAt(i);
							if (cmbActionItem.toString().equals(actionText)) {
								cmbActions.setSelectedIndex(i);
								break;
							}
						}

					}
				}
			});
			actionItem.setAccelerator(KeyStroke.getKeyStroke(getKeyEventForAction(action), Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | KeyEvent.SHIFT_MASK));
			actionMenu.add(actionItem);
		}
		if (actions.isEmpty()) {
			cmbActions.addItem(WfAction.SEND_TO_OUTBOX);
		}
		addDefaultActions();
	}

	private int getKeyEventForAction(WfAction action) {
		String actionName = action.getName();
		for (int i = 0; i < actionName.length(); i++) {
			if (!memonicKeys.contains(actionName.charAt(i))) {
				memonicKeys.add(actionName.charAt(i));
				return actionName.charAt(i);
			}
		}
		return KeyEvent.VK_S;
	}

	/**
	 * Adds the default actions.
	 */
	private void addDefaultActions() {
		WfAction satdAction = new WfAction();
		satdAction.setBusinessProcess(new File("sampleProcesses/SaveAsTodoActionWithoutDestination.bp"));
		satdAction.setId(UUID.randomUUID());
		satdAction.setName("Tag as todo");
		satdAction.setConsequence(null);
		cmbActions.addItem(satdAction);
	}

	class UpdateUIWorker extends SwingWorker<String, String> {
		private WfInstance instance;
		private boolean readOnlyMode;

		public UpdateUIWorker(WfInstance instance, boolean readOnlyMode) {
			super();
			this.instance = instance;
			this.readOnlyMode = readOnlyMode;
		}

		@Override
		protected String doInBackground() throws Exception {
			initializeMemonicKeys();
			setReadOnlyMode(this.readOnlyMode);
			I_ConfigAceFrame config;
			try {
				workflowDefinition = instance.getWfDefinition();

				config = Terms.get().getActiveAceFrameConfig();
				WorkList workList = instance.getWorkList();
				workflowInterpreter = WorkflowInterpreter.createWorkflowInterpreter(workList.getWorkflowDefinition());
				List<WfRole> roles = workflowInterpreter.getNextRole(instance, workList);
				componentProvider = new WfComponentProvider();

				WfUser user = componentProvider.userConceptToWfUser(config.getDbConfig().getUserConcept());
				List<WfPermission> perms = componentProvider.getPermissionsForUser(user);
				WfRole userRole = null;
				boolean bExists = false;
				I_GetConceptData roleConcept = null;
				for (WfRole role : roles) {
					for (WfPermission perm : perms) {
						if (role.toString().equals(perm.getRole().toString())) {
							userRole = role;
							bExists = true;
							break;
						}
					}
					if (bExists)
						break;
				}
				if (bExists) {
					roleConcept = Terms.get().getConcept(userRole.getId());
				}
				wfRole = userRole;
				translationProject = (TranslationProject) TerminologyProjectDAO.getProjectForWorklist(workList, config);

				I_GetConceptData component = Terms.get().getConcept(instance.getComponentId());
				WorkListMember workListMember = TerminologyProjectDAO.getWorkListMember(component, workList, config);
				updateUI(translationProject, workListMember, roleConcept);
				List<WfAction> actions = workflowInterpreter.getPossibleActions(instance, user);
				setPossibleActions(actions);

			} catch (TerminologyException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return "";
		}

	}
}
