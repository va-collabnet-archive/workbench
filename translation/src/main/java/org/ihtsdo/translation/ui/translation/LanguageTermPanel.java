package org.ihtsdo.translation.ui.translation;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.apache.xalan.trace.SelectionEvent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdVersion;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.config.AceFrame;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.issue.IssueRepoRegistration;
import org.ihtsdo.issue.issuerepository.IssueRepository;
import org.ihtsdo.issue.manager.IssueRepositoryDAO;
import org.ihtsdo.project.ContextualizedDescription;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.model.WorkListMember;
import org.ihtsdo.project.refset.LanguageMembershipRefset;
import org.ihtsdo.project.view.TranslationHelperPanel;
import org.ihtsdo.project.view.event.EventMediator;
import org.ihtsdo.project.view.event.GenericEvent.EventType;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.spec.ValidationException;
import org.ihtsdo.translation.ui.ConfigTranslationModule;
import org.ihtsdo.translation.ui.ConfigTranslationModule.EditingPanelOpenMode;
import org.ihtsdo.translation.ui.TranslationWlstMemberLogPanel;
import org.ihtsdo.translation.ui.ZebraJTable;
import org.ihtsdo.translation.ui.event.AddDescriptionEvent;
import org.ihtsdo.translation.ui.event.AddFsnEvent;
import org.ihtsdo.translation.ui.event.AddPreferedDescriptionEvent;
import org.ihtsdo.translation.ui.event.SelectTargetTermEvent;
import org.ihtsdo.translation.ui.event.SelectTargetTermEventHandler;
import org.ihtsdo.translation.ui.event.SendAsAcceptableEvent;
import org.ihtsdo.translation.ui.event.SendAsPreferredEvent;
import org.ihtsdo.translation.ui.event.TargetTableItemSelectedEvent;
import org.ihtsdo.translation.ui.renderer.AcceptabilityIconRenderer;
import org.ihtsdo.translation.ui.renderer.ICSIconRenderer;
import org.ihtsdo.translation.ui.renderer.LanguageIconRenderer;
import org.ihtsdo.translation.ui.renderer.TermTypeIconRenderer;
import org.ihtsdo.translation.ui.renderer.TextAreaEditor;
import org.ihtsdo.translation.ui.renderer.TextAreaRenderer;

/**
 * @author Guillermo Reynoso
 */
public class LanguageTermPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private TranslationHelperBI translationHelper = new TranslationHelper();
	/** The synonym. */
	private I_GetConceptData synonym;
	/** The fsn. */
	private I_GetConceptData fsn;
	/** The preferred. */
	private I_GetConceptData preferred;
	/** The acceptable. */
	private I_GetConceptData acceptable;
	/** The inactive. */
	private I_GetConceptData inactive;
	/** The active. */
	private I_GetConceptData active;
	private TranslationProject translationProject;
	private WorkListMember worklistMember;
	private boolean isSourceTable;
	private ConfigTranslationModule translConfig;
	private int previousRow = -1;
	private TranslationTermTableReport sourceTableResults;
	private Integer selectedRow = -1;
	protected ContextualizedDescription newDescription;
	protected UUID newWorklistUuid;
	private boolean readOnlyMode;
	private I_TermFactory tf;
	private I_ConfigAceFrame config;
	protected boolean canceled;
	private boolean standBy;
	public ContextualizedDescription previousDescription;
	private SelectionListener selectionListener;
	private LanguageDescriptionTableModel model;
	private LanguageMembershipRefset targetLangRefset;
	public final static String SNOMED_CORE_PATH_UID = "8c230474-9f11-30ce-9cad-185a96fd03a2";

	public LanguageTermPanel() {
		initComponents();
		tabSou.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		selectionListener = new SelectionListener(tabSou);
		tabSou.getSelectionModel().addListSelectionListener(selectionListener);
		suscribeToEvents();
		try {
			tf = Terms.get();
			config = tf.getActiveAceFrameConfig();
			inactive = tf.getConcept(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getNid());
			fsn = tf.getConcept(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid());
			preferred = tf.getConcept(SnomedMetadataRf2.PREFERRED_RF2.getLenient().getNid());
			synonym = tf.getConcept(SnomedMetadataRf2.SYNONYM_RF2.getLenient().getNid());
			acceptable = tf.getConcept(SnomedMetadataRf2.ACCEPTABLE_RF2.getLenient().getNid());
			active = tf.getConcept(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid());
		} catch (ValidationException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public LanguageTermPanel(boolean isSoruceTable) {
		initComponents();
		this.isSourceTable = isSoruceTable;
		tabSou.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		selectionListener = new SelectionListener(tabSou);
		tabSou.getSelectionModel().addListSelectionListener(selectionListener);
		suscribeToEvents();
		try {
			tf = Terms.get();
			config = tf.getActiveAceFrameConfig();
			inactive = tf.getConcept(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getNid());
			fsn = tf.getConcept(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid());
			preferred = tf.getConcept(SnomedMetadataRf2.PREFERRED_RF2.getLenient().getNid());
			synonym = tf.getConcept(SnomedMetadataRf2.SYNONYM_RF2.getLenient().getNid());
			acceptable = tf.getConcept(SnomedMetadataRf2.ACCEPTABLE_RF2.getLenient().getNid());
			active = tf.getConcept(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid());
		} catch (ValidationException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void suscribeToEvents() {
		EventMediator mediator = EventMediator.getInstance();
		mediator.suscribe(EventType.SELECT_TARGET_TABLE_EVENT, new SelectTargetTermEventHandler<SelectTargetTermEvent>(this) {
			@Override
			public void handleEvent(SelectTargetTermEvent event) {
				int selrow = tabSou.convertRowIndexToView(event.getRowNumber());
				if (selrow > -1) {
					tabSou.setRowSelectionInterval(selrow, selrow);
				}
			}
		});
	}

	public void clearAll() {
		model.clearTable();
	}

	public TranslationTermTableReport populateTable(TranslationProject project, WorkListMember worklistMember, boolean isSourceTable, ConfigTranslationModule translConfig, TranslationTermTableReport sourceTableResults, boolean readOnlyMode) throws Exception {
		if (!isSourceTable) {
			titleLabel.setText("Target Language");
		} else {
			titleLabel.setText("Source Language");
		}
		canceled = false;
		this.readOnlyMode = readOnlyMode;
		selectedRow = -1;
		TranslationTermTableReport result = updateTable(project, worklistMember, isSourceTable, translConfig, sourceTableResults);
		List<Integer> sourceIds = new ArrayList<Integer>();
		// for (I_GetConceptData sourceLang :
		// project.getSourceLanguageRefsets()) {
		// sourceIds.add(sourceLang.getConceptNid());
		// }
		// int targetId = project.getTargetLanguageRefset().getConceptNid();
		// if (isSourceTable) {
		// EventMediator.getInstance().fireEvent(new
		// UpdateSimilarityEvent(result.getSourceFsnConcept(),
		// result.getSourcePreferedConcept(), worklistMember.getConcept(),
		// sourceIds, targetId));
		// }
		return result;
	}

	private TranslationTermTableReport updateTable(TranslationProject project, WorkListMember worklistMember, boolean isSourceTable, ConfigTranslationModule translConfig, TranslationTermTableReport sourceTableResults) throws Exception {
		this.translConfig = translConfig;
		this.translationProject = project;
		this.isSourceTable = isSourceTable;
		this.worklistMember = worklistMember;
		targetLangRefset = new LanguageMembershipRefset(translationProject.getTargetLanguageRefset(), Terms.get().getActiveAceFrameConfig());
		model = new LanguageDescriptionTableModel(project, worklistMember, isSourceTable, this);
		tabSou.setModel(model);
		model.updatePage();
		TableColumnModel cmodel = tabSou.getColumnModel();
		TextAreaRenderer textAreaRenderer = new TextAreaRenderer();
		cmodel.getColumn(TermTableColumn.CHANGE.ordinal()).setCellRenderer(new ChangeCellRenderer());
		cmodel.getColumn(TermTableColumn.TERM.ordinal()).setCellRenderer(textAreaRenderer);
		cmodel.getColumn(TermTableColumn.TERM.ordinal()).setCellEditor(new TextAreaEditor());

		cmodel.getColumn(TermTableColumn.TERM_TYPE.ordinal()).setCellRenderer(new TermTypeIconRenderer());
		cmodel.getColumn(TermTableColumn.ACCEPTABILITY.ordinal()).setCellRenderer(new AcceptabilityIconRenderer());
		cmodel.getColumn(TermTableColumn.LANGUAGE.ordinal()).setCellRenderer(new LanguageIconRenderer());
		cmodel.getColumn(TermTableColumn.ICS.ordinal()).setCellRenderer(new ICSIconRenderer());

		cmodel.getColumn(TermTableColumn.CHANGE.ordinal()).setMinWidth(5);
		cmodel.getColumn(TermTableColumn.CHANGE.ordinal()).setPreferredWidth(5);
		cmodel.getColumn(TermTableColumn.CHANGE.ordinal()).setMaxWidth(5);
		// cmodel.getColumn(TermTableColumn.TERM.ordinal()).setMinWidth(maxTermWidth);
		cmodel.getColumn(TermTableColumn.TERM_TYPE.ordinal()).setMaxWidth(48);
		cmodel.getColumn(TermTableColumn.ACCEPTABILITY.ordinal()).setMaxWidth(48);
		cmodel.getColumn(TermTableColumn.ICS.ordinal()).setMaxWidth(48);
		cmodel.getColumn(TermTableColumn.LANGUAGE.ordinal()).setMaxWidth(48);

		// tabSou.setRowHeight(24);
		tabSou.setUpdateSelectionOnSort(true);
		tabSou.revalidate();

		if (!canceled) {
			selectRow(isSourceTable, translConfig);
		}
		if (isMemberLogOpen() && isSourceTable) {
			mLogActionPerformed();
		}
		return null;
	}

	public String getDescriptionId(int descriptionNid, int snomedCorePathNid) throws IOException, TerminologyException {

		Long descriptionId = null;
		I_Identify desc_Identify = Terms.get().getId(descriptionNid);
		List<? extends I_IdVersion> i_IdentifyList = desc_Identify.getIdVersions();
		if (i_IdentifyList.size() > 0) {
			for (int i = 0; i < i_IdentifyList.size(); i++) {
				I_IdVersion i_IdVersion = (I_IdVersion) i_IdentifyList.get(i);
				Object denotion = (Object) i_IdVersion.getDenotation();
				int snomedIntegerNid = i_IdVersion.getAuthorityNid();
				int arcAuxSnomedIntegerNid = ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.localize().getNid();
				int pathNid = i_IdVersion.getPathNid();
				if (pathNid == snomedCorePathNid && snomedIntegerNid == arcAuxSnomedIntegerNid) {
					descriptionId = (Long) denotion;
				}
			}
		}
		if (descriptionId == null)
			return null;

		return descriptionId.toString();
	}

	public void selectRow(int modelRowNum) {
		if (modelRowNum >= 0 && modelRowNum < tabSou.getRowCount()) {
			int viewRowIndex = tabSou.convertRowIndexToView(modelRowNum);
			tabSou.setRowSelectionInterval(viewRowIndex, viewRowIndex);
		}
	}

	public void selectRow(boolean isSourceTable, ConfigTranslationModule translConfig) {

		if (!isSourceTable) {
			if (selectedRow == -1) {
				Integer modelPreferdRow = model.getPreferedRow();
				Integer targetPreferredRow = null;
				if (modelPreferdRow != null) {
					targetPreferredRow = tabSou.convertRowIndexToView(modelPreferdRow);
				}

				if (translConfig.getSelectedEditorMode().equals(ConfigTranslationModule.EditorMode.PREFERRED_TERM_EDITOR) && !readOnlyMode) {
					if (targetPreferredRow == null) {
						EventMediator.getInstance().fireEvent(new AddPreferedDescriptionEvent());
					} else if (targetPreferredRow != null) {
						tabSou.setRowSelectionInterval(targetPreferredRow, targetPreferredRow);
						selectedRow = targetPreferredRow;
					}

				} else if (translConfig.getSelectedEditorMode().equals(ConfigTranslationModule.EditorMode.SYNONYMS_EDITOR) && !readOnlyMode) {
					EventMediator.getInstance().fireEvent(new AddDescriptionEvent());
				} else if (translConfig.getSelectedEditorMode().equals(ConfigTranslationModule.EditorMode.FULL_EDITOR) && !readOnlyMode) {
					if (translConfig.getEditingPanelOpenMode().equals(EditingPanelOpenMode.PREFFERD_TERM_MODE)) {
						if (targetPreferredRow == null) {
							EventMediator.getInstance().fireEvent(new AddPreferedDescriptionEvent());
						} else if (targetPreferredRow != null) {
							tabSou.setRowSelectionInterval(targetPreferredRow, targetPreferredRow);
							selectedRow = targetPreferredRow;
						}
					} else if (translConfig.getEditingPanelOpenMode().equals(EditingPanelOpenMode.FSN_TERM_MODE)) {
						if (model.getRowCount() == 0) {
							EventMediator.getInstance().fireEvent(new AddFsnEvent());
						} else {
							Integer modelTargetFsnRow = model.getTargetFsnRow();
							Integer targetFSNRow = null;
							if (modelTargetFsnRow != null) {
								targetFSNRow = tabSou.convertRowIndexToView(modelTargetFsnRow);
							}
							if (targetFSNRow != null) {
								tabSou.setRowSelectionInterval(targetFSNRow, targetFSNRow);
								selectedRow = targetFSNRow;
							}
						}
					}
				}
			} else if (selectedRow > -1 && selectedRow < tabSou.getRowCount()) {
				tabSou.setRowSelectionInterval(selectedRow, selectedRow);
			}
		}
	}

	public void setTitle(String title) {
		titleLabel.setText(title);
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

	private void sendAsPreferredActionPerformed(ActionEvent e) {
		int row = tabSou.getSelectedRow();
		int rowModel = tabSou.convertRowIndexToModel(row);
		DefaultTableModel model = (DefaultTableModel) tabSou.getModel();
		ContextualizedDescription description = (ContextualizedDescription) model.getValueAt(rowModel, TermTableColumn.TERM.ordinal());
		if (description.getDescriptionStatusId() == inactive.getConceptNid()) {
			return;
		}
		EventMediator.getInstance().fireEvent(new SendAsPreferredEvent(description));
	}

	private void sendAsAcceptableActionPerformed(ActionEvent e) {
		int row = tabSou.getSelectedRow();
		int rowModel = tabSou.convertRowIndexToModel(row);
		DefaultTableModel model = (DefaultTableModel) tabSou.getModel();
		ContextualizedDescription description = (ContextualizedDescription) model.getValueAt(rowModel, TermTableColumn.TERM.ordinal());
		if (description.getDescriptionStatusId() == inactive.getConceptNid()) {
			return;
		}
		EventMediator.getInstance().fireEvent(new SendAsAcceptableEvent(description));
	}

	private void copyToClipboardActionPerformed(ActionEvent e) {
		int row = tabSou.getSelectedRow();
		if (row >= 0) {
			ContextualizedDescription description = (ContextualizedDescription) tabSou.getModel().getValueAt(row, TermTableColumn.TERM.ordinal());
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			StringSelection strSel = new StringSelection(description.getText());
			clipboard.setContents(strSel, strSel);
		}
	}

	private void tabSouMouseClicked(MouseEvent e) {
		if (isSourceTable) {
			if (e.getButton() == MouseEvent.BUTTON3) {
				int xPoint = e.getX();
				int yPoint = e.getY();
				int row = tabSou.rowAtPoint(new Point(xPoint, yPoint));
				if (row >= 0 && row < tabSou.getRowCount()) {
					tabSou.setRowSelectionInterval(row, row);
				} else {
					tabSou.clearSelection();
				}
				if (row > -1) {
					sourcePopup.show(tabSou, xPoint, yPoint);
				}
			}
		}else{
			if (e.getButton() == MouseEvent.BUTTON3) {
				int xPoint = e.getX();
				int yPoint = e.getY();
				int row = tabSou.rowAtPoint(new Point(xPoint, yPoint));
				if (row >= 0 && row < tabSou.getRowCount()) {
					tabSou.setRowSelectionInterval(row, row);
				} else {
					tabSou.clearSelection();
				}
				if (row > -1) {
					targetPopup.show(tabSou, xPoint, yPoint);
				}
			}
		}
	}

	private void copyTargetToClipBoardActionPerformed(ActionEvent e) {
		int row = tabSou.getSelectedRow();
		if (row >= 0) {
			ContextualizedDescription description = (ContextualizedDescription) tabSou.getModel().getValueAt(row, TermTableColumn.TERM.ordinal());
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			StringSelection strSel = new StringSelection(description.getText());
			clipboard.setContents(strSel, strSel);
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
			if (isSourceTable) {
				return;
			}
			ListSelectionModel lsm = (ListSelectionModel) e.getSource();
			if (lsm.isSelectionEmpty()) {
				return;
			} else if (!(e.getSource() == table.getSelectionModel() && table.getRowSelectionAllowed())) {
				return;
			} else if (e.getValueIsAdjusting()) {
				return;
			} else {
				int first = table.getSelectedRow();
				if (first > -1) {
					int rowModel = table.convertRowIndexToModel(first);
					ContextualizedDescription descrpt = (ContextualizedDescription) table.getModel().getValueAt(rowModel, TermTableColumn.TERM.ordinal());

					if (translConfig.getSelectedEditorMode().equals(ConfigTranslationModule.EditorMode.PREFERRED_TERM_EDITOR)) {
						if (!(descrpt.getTypeId() == synonym.getConceptNid() && descrpt.getAcceptabilityId() == preferred.getConceptNid())) {
							int selrow = tabSou.convertRowIndexToView(previousRow);
							if (selrow > -1) {
								tabSou.setRowSelectionInterval(selrow, selrow);
							}
							return;
						}
					}
					if (translConfig.getSelectedEditorMode().equals(ConfigTranslationModule.EditorMode.SYNONYMS_EDITOR)) {
						if (!(descrpt.getTypeId() == synonym.getConceptNid() && descrpt.getAcceptabilityId() == acceptable.getConceptNid())) {
							int selrow = tabSou.convertRowIndexToView(previousRow);
							tabSou.setRowSelectionInterval(selrow, selrow);
							return;
						}
					}
					if (descrpt != null) {
						EventMediator.getInstance().fireEvent(new TargetTableItemSelectedEvent(descrpt, previousRow));
						if (previousRow > -1 && previousRow < tabSou.getRowCount()) {
							previousDescription = (ContextualizedDescription) tabSou.getModel().getValueAt(tabSou.convertRowIndexToModel(previousRow), TermTableColumn.TERM.ordinal());
						}
						previousRow = first;
					}
					selectedRow = tabSou.getSelectedRow();
				}
			}
		}

	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		titleLabel = new JLabel();
		scrollPane4 = new JScrollPane();
		tabSou = new ZebraJTable();
		sourcePopup = new JPopupMenu();
		sendAsPreferred = new JMenuItem();
		sendAsAcceptable = new JMenuItem();
		copyToClipboard = new JMenuItem();
		scrollPane1 = new JScrollPane();
		targetPopup = new JPopupMenu();
		copyTargetToClipBoard = new JMenuItem();

		//======== this ========
		setLayout(new GridBagLayout());
		((GridBagLayout)getLayout()).columnWidths = new int[] {0, 0};
		((GridBagLayout)getLayout()).rowHeights = new int[] {0, 0, 0};
		((GridBagLayout)getLayout()).columnWeights = new double[] {1.0, 1.0E-4};
		((GridBagLayout)getLayout()).rowWeights = new double[] {0.0, 1.0, 1.0E-4};

		//---- titleLabel ----
		titleLabel.setMaximumSize(new Dimension(25, 16));
		titleLabel.setMinimumSize(new Dimension(25, 16));
		titleLabel.setPreferredSize(new Dimension(25, 16));
		titleLabel.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
		add(titleLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 5, 0), 0, 0));

		//======== scrollPane4 ========
		{
			scrollPane4.setPreferredSize(new Dimension(23, 27));

			//---- tabSou ----
			tabSou.setPreferredScrollableViewportSize(new Dimension(20, 20));
			tabSou.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			tabSou.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					tabSouMouseClicked(e);
				}
			});
			scrollPane4.setViewportView(tabSou);
		}
		add(scrollPane4, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(0, 0, 0, 0), 0, 0));

		//======== sourcePopup ========
		{

			//---- sendAsPreferred ----
			sendAsPreferred.setText("Send as preferred");
			sendAsPreferred.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					sendAsPreferredActionPerformed(e);
				}
			});
			sourcePopup.add(sendAsPreferred);

			//---- sendAsAcceptable ----
			sendAsAcceptable.setText("Send as acceptable");
			sendAsAcceptable.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					sendAsAcceptableActionPerformed(e);
				}
			});
			sourcePopup.add(sendAsAcceptable);

			//---- copyToClipboard ----
			copyToClipboard.setText("Copy to clipboard");
			copyToClipboard.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					copyToClipboardActionPerformed(e);
				}
			});
			sourcePopup.add(copyToClipboard);
		}

		//======== targetPopup ========
		{

			//---- copyTargetToClipBoard ----
			copyTargetToClipBoard.setText("Copy to clipboard");
			copyTargetToClipBoard.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					copyTargetToClipBoardActionPerformed(e);
				}
			});
			targetPopup.add(copyTargetToClipBoard);
		}
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	private JLabel titleLabel;
	private JScrollPane scrollPane4;
	private ZebraJTable tabSou;
	private JPopupMenu sourcePopup;
	private JMenuItem sendAsPreferred;
	private JMenuItem sendAsAcceptable;
	private JMenuItem copyToClipboard;
	private JScrollPane scrollPane1;
	private JPopupMenu targetPopup;
	private JMenuItem copyTargetToClipBoard;
	// JFormDesigner - End of variables declaration //GEN-END:variables

	class ChangeCellRenderer extends JLabel implements TableCellRenderer {

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 1L;

		public ChangeCellRenderer() {
			setOpaque(true);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent
		 * (javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
		 */
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			if ((Boolean) value) {
				setBackground(Color.GREEN);
			} else {
				setBackground(Color.WHITE);
			}
			return this;
		}

	}
}

/**
 * The Enum TableSourceColumn.
 */
enum TermTableColumn {

	/** The Change indicator */
	CHANGE(""),
	/** The LANGUAGE. */
	LANGUAGE("Language"),
	/** The TER m_ type. */
	TERM_TYPE("Term type"),
	/** The ACCEPTABILITY. */
	ACCEPTABILITY("Acceptability"),
	/** The ICS. */
	ICS("ICS"),
	/** The TERM. */
	TERM("Term"), AUTHOR("Author"), ROW_CLASS("rowclass");

	/** The column name. */
	private final String columnName;

	/**
	 * Instantiates a new table source column.
	 * 
	 * @param name
	 *            the name
	 */
	private TermTableColumn(String name) {
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
