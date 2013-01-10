package org.ihtsdo.translation.ui.translation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdVersion;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.project.ContextualizedDescription;
import org.ihtsdo.project.I_ContextualizeDescription;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.model.I_TerminologyProject;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.model.WorkListMember;
import org.ihtsdo.project.refset.LanguageMembershipRefset;
import org.ihtsdo.project.view.event.EventMediator;
import org.ihtsdo.project.view.event.GenericEvent.EventType;
import org.ihtsdo.project.view.tag.InboxTag;
import org.ihtsdo.project.view.tag.TagManager;
import org.ihtsdo.project.workflow.api.WfComponentProvider;
import org.ihtsdo.project.workflow.model.WfInstance;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.translation.FSNGenerationException;
import org.ihtsdo.translation.LanguageUtil;
import org.ihtsdo.translation.TreeEditorObjectWrapper;
import org.ihtsdo.translation.ui.ConfigTranslationModule;
import org.ihtsdo.translation.ui.ConfigTranslationModule.IcsGenerationStrategy;
import org.ihtsdo.translation.ui.ConfigTranslationModule.InboxColumn;
import org.ihtsdo.translation.ui.ConfigTranslationModule.TreeComponent;
import org.ihtsdo.translation.ui.event.AddDescriptionEvent;
import org.ihtsdo.translation.ui.event.AddDescriptionEventHandler;
import org.ihtsdo.translation.ui.event.AddFsnEvent;
import org.ihtsdo.translation.ui.event.AddFsnEventHandler;
import org.ihtsdo.translation.ui.event.AddPreferedDescriptionEvent;
import org.ihtsdo.translation.ui.event.AddPreferedDescriptionEventHandler;
import org.ihtsdo.translation.ui.event.ClearDescriptionPanelEvent;
import org.ihtsdo.translation.ui.event.DescriptionSavedEvent;
import org.ihtsdo.translation.ui.event.DescriptionSavedEventHandler;
import org.ihtsdo.translation.ui.event.SendAsAcceptableEvent;
import org.ihtsdo.translation.ui.event.SendAsAcceptableEventHandler;
import org.ihtsdo.translation.ui.event.SendAsPreferredEvent;
import org.ihtsdo.translation.ui.event.SendAsPreferredEventHandler;
import org.ihtsdo.translation.ui.event.TermChangedEvent;
import org.ihtsdo.translation.ui.event.TermChangedEventHandler;
import org.ihtsdo.translation.ui.event.UpdateSimilarityEvent;

public class LanguageDescriptionTableModel extends DefaultTableModel {
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -3295746462823927132L;

	/** The column count. */
	private int columnCount = InboxColumn.values().length;

	/** The data. */
	private LinkedList<Object[]> data = new LinkedList<Object[]>();

	/** The tag cache. */
	private HashMap<String, InboxTag> tagCache = new HashMap<String, InboxTag>();

	/** The preferred. */
	private I_GetConceptData preferred;

	/** The synonym. */
	private I_GetConceptData synonym;

	/** The fsn. */
	private I_GetConceptData fsn;

	/** The acceptable. */
	private I_GetConceptData acceptable;
	/** The inactive. */
	private I_GetConceptData inactive;
	/** The active. */
	private I_GetConceptData active;

	/** The tags. */
	public List<InboxTag> tags;

	/** The columns. */
	protected LinkedHashSet<TermTableColumn> columns;

	/** The config. */
	private I_ConfigAceFrame config;

	private boolean isSourceModel;

	private TranslationProject project;

	private WorkListMember worklistMember;

	protected ContextualizedDescription newDescription;

	protected UUID newWorklistUuid;

	private LanguageTermPanel termPanel;

	private I_GetConceptData targetLanguage;

	private List<I_GetConceptData> sourceLanguageRefsets;

	private LanguageMembershipRefset targetLangRefset;

	private LanguageMembershipRefset sourceLangRefset;

	private HashMap<Integer, String> hashAuthId;

	private ConfigTranslationModule translConfig;

	private ArrayList<I_GetConceptData> langRefsets;

	private TranslationTermTableReport result;

	private LinkedHashSet<TreeComponent> termCom;

	public final static String SNOMED_CORE_PATH_UID = "8c230474-9f11-30ce-9cad-185a96fd03a2";

	/**
	 * Instantiates a new inbox table model.
	 * 
	 * @param pBar
	 *            the bar
	 */
	public LanguageDescriptionTableModel(TranslationProject project, WorkListMember worklistMember, boolean isSourceModel, LanguageTermPanel termPanel) {
		super();
		try {
			this.isSourceModel = isSourceModel;
			this.termPanel = termPanel;
			this.project = project;
			this.worklistMember = worklistMember;
			I_TermFactory tf = Terms.get();
			config = tf.getActiveAceFrameConfig();
			preferred = tf.getConcept(SnomedMetadataRf2.PREFERRED_RF2.getLenient().getNid());
			fsn = tf.getConcept(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid());
			synonym = tf.getConcept(SnomedMetadataRf2.SYNONYM_RF2.getLenient().getNid());
			acceptable = tf.getConcept(SnomedMetadataRf2.ACCEPTABLE_RF2.getLenient().getNid());
			inactive = tf.getConcept(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getNid());
			active = tf.getConcept(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid());
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		refreshColumnsStruct();
		initEventListeners();
	}

	/**
	 * Fire table struct changed after calling this method.
	 */
	public void refreshColumnsStruct() {
		String[] columnNames = new String[TermTableColumn.values().length];
		columns = new LinkedHashSet<TermTableColumn>();
		for (int i = 0; i < TermTableColumn.values().length; i++) {
			columnNames[i] = TermTableColumn.values()[i].getColumnName();
			columns.add(TermTableColumn.values()[i]);
		}
	}

	/**
	 * Inits the event listeners.
	 */
	private void initEventListeners() {
		EventMediator mediator = EventMediator.getInstance();
		if (!isSourceModel) {
			mediator.suscribe(EventType.DESCRIPTION_SAVED, new DescriptionSavedEventHandler<DescriptionSavedEvent>(this) {
				@Override
				public void handleEvent(DescriptionSavedEvent event) {
					newDescription = event.getDescription();
					newWorklistUuid = event.getWorklist();
					Set<Integer> intset = new HashSet<Integer>();
					intset.add(newDescription.getConceptId());
					intset.add(newDescription.getDescId());
					if (!updateTerm(intset)) {
						int i = 0;
						for (Object[] integer : data) {
							ContextualizedDescription description = (ContextualizedDescription) integer[TermTableColumn.TERM.ordinal()];
							if (description.getDescId() == newDescription.getDescId()) {
								termPanel.selectRow(i);
								break;
							}
							i++;
						}
					}
					AceLog.getAppLog().info("Description saved");
				}
			});
			mediator.suscribe(EventType.TERM_CHANGED_EVENT, new TermChangedEventHandler<TermChangedEvent>(this) {
				@Override
				public void handleEvent(TermChangedEvent event) {
					updateTerm(event.getIntSet());
					AceLog.getAppLog().info("term change event");
				}
			});
			mediator.suscribe(EventType.ADD_FSN_EVENT, new AddFsnEventHandler<AddFsnEvent>(this) {
				@Override
				public void handleEvent(AddFsnEvent event) {
					try {
						ContextualizedDescription fsnDesc = null;
						I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
						try {
							fsnDesc = (ContextualizedDescription) LanguageUtil.generateFSN(worklistMember.getConcept(), sourceLangRefset, targetLangRefset, project, config);
						} catch (FSNGenerationException e1) {
							e1.printStackTrace();
							JOptionPane.showOptionDialog(new JFrame(), e1.getMessage(), "Warning", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, null, null);
						}
						if (fsnDesc == null) {
							Ts.get().removeTermChangeListener(TranslationTermChangeListener.getInstance());
							ContextualizedDescription newDesc = createDescription(fsn.getConceptNid(), preferred.getConceptNid());
							newDesc.persistChanges();
							newDescription = newDesc;
							EventMediator.getInstance().fireEvent(new DescriptionSavedEvent(newDesc, null));
							Ts.get().addTermChangeListener(TranslationTermChangeListener.getInstance());
						} else {
							Ts.get().removeTermChangeListener(TranslationTermChangeListener.getInstance());
							fsnDesc.persistChanges();
							EventMediator.getInstance().fireEvent(new DescriptionSavedEvent(fsnDesc, null));
							Ts.get().addTermChangeListener(TranslationTermChangeListener.getInstance());
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

			mediator.suscribe(EventType.ADD_PREFERRED_DESCRIPTION_EVENT, new AddPreferedDescriptionEventHandler<AddPreferedDescriptionEvent>(this) {
				@Override
				public void handleEvent(AddPreferedDescriptionEvent event) {
					try {
						Ts.get().removeTermChangeListener(TranslationTermChangeListener.getInstance());
						ContextualizedDescription newDesc = createDescription(synonym.getConceptNid(), preferred.getConceptNid());
						setDefaultPreferredTermText(newDesc);
						newDesc.persistChanges();
						newDescription = newDesc;
						EventMediator.getInstance().fireEvent(new DescriptionSavedEvent(newDesc, null));
						Ts.get().addTermChangeListener(TranslationTermChangeListener.getInstance());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			mediator.suscribe(EventType.ADD_DESCRIPTION_EVENT, new AddDescriptionEventHandler<AddDescriptionEvent>(this) {
				@Override
				public void handleEvent(AddDescriptionEvent event) {
					try {
						Ts.get().removeTermChangeListener(TranslationTermChangeListener.getInstance());
						ContextualizedDescription newDesc = createDescription(synonym.getConceptNid(), acceptable.getConceptNid());
						setDefaultPreferredTermText(newDesc);
						newDesc.persistChanges();
						newDescription = newDesc;
						EventMediator.getInstance().fireEvent(new DescriptionSavedEvent(newDesc, null));
						Ts.get().addTermChangeListener(TranslationTermChangeListener.getInstance());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			});

			mediator.suscribe(EventType.SEND_AS_PREFERRED, new SendAsPreferredEventHandler<SendAsPreferredEvent>(this) {
				@Override
				public void handleEvent(SendAsPreferredEvent event) {
					if (!preferredControlPass(event.getDescription())) {
						JOptionPane.showMessageDialog(new JFrame(), "Preferred description already exists", "Preferred", JOptionPane.WARNING_MESSAGE);
						return;
					}
					try {
						Ts.get().removeTermChangeListener(TranslationTermChangeListener.getInstance());
						ContextualizedDescription newdescription = (ContextualizedDescription) event.getDescription().contextualizeThisDescription(targetLangRefset.getRefsetId(), preferred.getConceptNid());
						newdescription.setExtensionStatusId(active.getConceptNid());
						newdescription.persistChanges();
						newDescription = newdescription;
						EventMediator.getInstance().fireEvent(new DescriptionSavedEvent(newdescription, null));
						Ts.get().addTermChangeListener(TranslationTermChangeListener.getInstance());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			});

			mediator.suscribe(EventType.SEND_AS_ACCEPTABLE, new SendAsAcceptableEventHandler<SendAsAcceptableEvent>(this) {
				@Override
				public void handleEvent(SendAsAcceptableEvent event) {
					try {
						Ts.get().removeTermChangeListener(TranslationTermChangeListener.getInstance());
						ContextualizedDescription newdescription = (ContextualizedDescription) event.getDescription().contextualizeThisDescription(targetLangRefset.getRefsetId(), acceptable.getConceptNid());
						newdescription.setExtensionStatusId(active.getConceptNid());
						newdescription.persistChanges();
						newDescription = newdescription;
						EventMediator.getInstance().fireEvent(new DescriptionSavedEvent(newdescription, null));
						Ts.get().addTermChangeListener(TranslationTermChangeListener.getInstance());
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			});
		}
	}

	protected boolean preferredControlPass(ContextualizedDescription contextualizedDescription) {
		List<ContextualizedDescription> descriptions;
		try {
			descriptions = LanguageUtil.getContextualizedDescriptions(worklistMember.getConcept().getConceptNid(), targetLanguage.getConceptNid(), true);
			for (I_ContextualizeDescription description : descriptions) {
				if (description.getLanguageExtension() != null && description.getLanguageRefsetId() == targetLanguage.getConceptNid()) {
					if (description.getTypeId() == contextualizedDescription.getTypeId() && description.getAcceptabilityId() == preferred.getConceptNid() && (description.getDescriptionStatusId() != inactive.getConceptNid() || description.getExtensionStatusId() != inactive.getConceptNid())) {
						return false;
					}
				}
			}
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	private void setDefaultPreferredTermText(ContextualizedDescription newDesc) throws Exception {
		ConfigTranslationModule cfg = new ConfigTranslationModule();
		try {
			cfg = LanguageUtil.getTranslationConfig(Terms.get().getActiveAceFrameConfig());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}
		if (cfg.getSelectedEditorMode().equals(ConfigTranslationModule.EditorMode.PREFERRED_TERM_EDITOR)) {
			String pref = LanguageUtil.getDefaultPreferredTermText(worklistMember.getConcept(), sourceLangRefset, targetLangRefset, config);
			if (!pref.equals("")) {
				newDesc.setText(pref);
			}
		}
	}

	private ContextualizedDescription createDescription(int typeId, int acceptabilityId) throws Exception {
		ConfigTranslationModule cfg = new ConfigTranslationModule();
		try {
			cfg = LanguageUtil.getTranslationConfig(Terms.get().getActiveAceFrameConfig());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}
		ContextualizedDescription newDesc = (ContextualizedDescription) ContextualizedDescription.createNewContextualizedDescription(worklistMember.getId(), targetLangRefset.getRefsetId(), targetLangRefset.getLangCode(config));
		newDesc.setText("New Description");
		newDesc.setAcceptabilityId(acceptabilityId);
		newDesc.setTypeId(typeId);
		IcsGenerationStrategy icsGenStrategy = cfg.getSelectedIcsGenerationStrategy();

		Boolean sourceICS;
		switch (icsGenStrategy) {
		case COPY_FROM_SOURCE:
			sourceICS = LanguageUtil.getDefaultICS(worklistMember.getConcept(), sourceLangRefset, targetLangRefset, config, cfg);
			if (sourceICS)
				newDesc.setInitialCaseSignificant(true);
			else
				newDesc.setInitialCaseSignificant(false);
			break;
		case NONE:
			newDesc.setInitialCaseSignificant(false);
			break;
		default:
			break;
		}
		newDesc.setDescriptionStatusId(active.getConceptNid());
		return newDesc;
	}

	private boolean updateTerm(Set<Integer> idSet) {
		boolean descriptionExists = false;
		if (!isSourceModel && newDescription != null && idSet.contains(newDescription.getConceptId())) {
			AceLog.getAppLog().info("updating term in target table");
			int rc = getRowCount();
			boolean descriptionAdded = true;
			for (int i = 0; i < rc; i++) {
				ContextualizedDescription description = (ContextualizedDescription) getValueAt(i, TermTableColumn.TERM.ordinal());
				if (idSet.contains(description.getDescId())) {
					descriptionAdded = false;
				}
			}
			for (int i = 0; i < rc && !descriptionAdded; i++) {
				ContextualizedDescription description = (ContextualizedDescription) getValueAt(i, TermTableColumn.TERM.ordinal());
				if (description != null && idSet.contains(description.getDescId())) {
					boolean descriptionUncommited = description.getDescriptionPart().getTime() == Long.MAX_VALUE || description.getLanguageExtensionPart().getTime() == Long.MAX_VALUE;
					setValueAt(descriptionUncommited, i, TermTableColumn.CHANGE.ordinal());
					ContextualizedDescription newDescription = null;
					try {
						newDescription = new ContextualizedDescription(description.getDescId(), description.getConceptId(), description.getLanguageRefsetId());
						// Generate new fsn
						if (description.getTypeId() == fsn.getConceptNid() && description.getDescriptionStatusId() == active.getConceptNid() && newDescription.getDescriptionStatusId() == inactive.getConceptNid()) {
							try {
								I_GetConceptData targetLanguage = project.getTargetLanguageRefset();
								LanguageMembershipRefset targetLangRefset = new LanguageMembershipRefset(targetLanguage, Terms.get().getActiveAceFrameConfig());
								LanguageMembershipRefset sourceLangRefset = new LanguageMembershipRefset(project.getSourceLanguageRefsets().iterator().next(), Terms.get().getActiveAceFrameConfig());
								LanguageUtil.generateFSN(description.getConcept(), sourceLangRefset, targetLangRefset, project, Terms.get().getActiveAceFrameConfig());
							} catch (FSNGenerationException e1) {
								e1.printStackTrace();
								JOptionPane.showOptionDialog(new JFrame(), e1.getMessage(), "Warning", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, null, null);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

					try {
						Object[] termAcceptability_Status = new Object[2];
						if (description.getExtensionStatusId() == inactive.getConceptNid()) {
							termAcceptability_Status[0] = false;
						} else {
							termAcceptability_Status[0] = true;
						}
						termAcceptability_Status[1] = "";
						setValueAt(newDescription, i, TermTableColumn.TERM.ordinal());
						if (newDescription.getAcceptabilityId() == acceptable.getConceptNid()) {
							termAcceptability_Status[1] = acceptable;
						} else if (newDescription.getAcceptabilityId() == preferred.getConceptNid()) {
							termAcceptability_Status[1] = preferred;
						}
						setValueAt(termAcceptability_Status, i, TermTableColumn.ACCEPTABILITY.ordinal());
						setValueAt(newDescription.isInitialCaseSignificant(), i, TermTableColumn.ICS.ordinal());

						// SET TERM TYPE STATUS
						LinkedHashSet<TreeComponent> termCom;
						ConfigTranslationModule translConfig = null;
						try {
							translConfig = LanguageUtil.getTranslationConfig(Terms.get().getActiveAceFrameConfig());
						} catch (IOException e) {
							e.printStackTrace();
						} catch (TerminologyException e) {
							e.printStackTrace();
						}
						termCom = translConfig.getTargetTreeComponents();

						Object[] termType_Status = new Object[3];
						if (getDescriptionId(description.getDescId(), Terms.get().getPath(UUID.fromString(SNOMED_CORE_PATH_UID)).getConceptNid()) != null) {
							termType_Status[2] = true;
						} else {
							termType_Status[2] = false;
						}

						if (newDescription.getDescriptionStatusId() == inactive.getConceptNid()) {
							termType_Status[1] = inactive;
							if (newDescription.getTypeId() == fsn.getConceptNid()) {
								termType_Status[0] = fsn;
							} else {
								termType_Status[0] = synonym;
							}
						} else if (newDescription.getTypeId() == fsn.getConceptNid()) {
							result.setSourceFsnConcept(newDescription);
							if (termCom.contains(ConfigTranslationModule.TreeComponent.FSN)) {
								termType_Status[0] = fsn;
								termType_Status[1] = active;
							}
						} else if (newDescription.getAcceptabilityId() == acceptable.getConceptNid() && termCom.contains(ConfigTranslationModule.TreeComponent.SYNONYM)) {
							termType_Status[0] = synonym;
							termType_Status[1] = active;
						} else if (newDescription.getAcceptabilityId() == preferred.getConceptNid()) {
							if (termCom.contains(ConfigTranslationModule.TreeComponent.PREFERRED)) {
								termType_Status[0] = synonym;
								termType_Status[1] = active;
							}
						} else if (termCom.contains(ConfigTranslationModule.TreeComponent.RETIRED)) {
							termType_Status[0] = synonym;
							termType_Status[1] = inactive;
						}

						setValueAt(termType_Status, i, TermTableColumn.TERM_TYPE.ordinal());

						descriptionExists = true;
						fireTableRowsUpdated(i, i);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			if (!descriptionExists) {
				try {
					updatePage();
					return descriptionExists;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return descriptionExists;
	}

	/**
	 * Gets the row.
	 * 
	 * @param rowNum
	 *            the row num
	 * @return the row
	 */
	public Object[] getRow(int rowNum) {
		return data.get(rowNum);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.DefaultTableModel#addRow(java.lang.Object[])
	 */
	@Override
	public void addRow(Object[] rowData) {
		data.add(rowData);
		fireTableDataChanged();
	}

	/**
	 * Gets the wf instance.
	 * 
	 * @param rowNum
	 *            the row num
	 * @return the wf instance
	 */
	public WfInstance getWfInstance(int rowNum) {
		ConfigTranslationModule cfg = new ConfigTranslationModule();
		try {
			cfg = LanguageUtil.getTranslationConfig(Terms.get().getActiveAceFrameConfig());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TerminologyException e) {
			e.printStackTrace();
		}
		return (WfInstance) data.get(rowNum)[cfg.getColumnsDisplayedInInbox().size() + 1];
	}

	/**
	 * Update table.
	 * 
	 * @param data
	 *            the data
	 */
	public void updateTable(Object[][] data) {
		int i = 0;
		this.data = new LinkedList<Object[]>();
		for (Object[] objects : data) {
			Object[] row = new Object[InboxColumn.values().length + 1];
			row[0] = i + 1;
			int j = 1;
			for (Object obj : objects) {
				row[j] = obj;
				j++;
			}
			this.data.add(row);
			i++;
		}
		fireTableDataChanged();
	}

	/**
	 * Clear table.
	 */
	public void clearTable() {
		data = new LinkedList<Object[]>();
		fireTableDataChanged();
	}

	/**
	 * Update page.
	 * 
	 * @param filterList
	 *            the filter list
	 * @return true, if successful
	 */
	public void updatePage() {
		data = new LinkedList<Object[]>();
		try {

			langRefsets = new ArrayList<I_GetConceptData>();
			translConfig = LanguageUtil.getTranslationConfig(Terms.get().getActiveAceFrameConfig());
			result = new TranslationTermTableReport();
			sourceLanguageRefsets = project.getSourceLanguageRefsets();
			if (isSourceModel) {
				termCom = translConfig.getSourceTreeComponents();
				langRefsets.addAll(sourceLanguageRefsets);
			} else {
				termCom = translConfig.getTargetTreeComponents();
				targetLanguage = project.getTargetLanguageRefset();
				langRefsets.add(targetLanguage);
			}
			sourceLangRefset = new LanguageMembershipRefset(project.getSourceLanguageRefsets().iterator().next(), Terms.get().getActiveAceFrameConfig());
			targetLangRefset = new LanguageMembershipRefset(project.getTargetLanguageRefset(), Terms.get().getActiveAceFrameConfig());
			// DefaultMutableTreeNode top = null;
			// translConfig=LanguageUtil.getTranslationConfig(Terms.get().getActiveAceFrameConfig());
			if (worklistMember != null) {
				try {
					for (I_GetConceptData langRefset : langRefsets) {
						I_IntSet allowedStatus = Terms.get().newIntSet();
						allowedStatus.add(SnomedMetadataRfx.getSTATUS_CURRENT_NID());
						if (translConfig.getTargetTreeComponents().contains(TreeComponent.RETIRED)) {
							allowedStatus.add(SnomedMetadataRfx.getSTATUS_RETIRED_NID());
						}
						List<ContextualizedDescription> descriptions = LanguageUtil.getContextualizedDescriptions(worklistMember.getConcept().getConceptNid(), langRefset.getConceptNid(), allowedStatus, config.getDescTypes(), config.getViewPositionSetReadOnly(), true);
						if (hashAuthId == null) {
							hashAuthId = new HashMap<Integer, String>();
						}
						boolean bSourceFSN = false;
						for (I_ContextualizeDescription description : descriptions) {
							if (description.getLanguageExtension() != null && description.getLanguageRefsetId() == langRefset.getConceptNid()) {
								bSourceFSN = addRowToData(langRefset, bSourceFSN, description);
							}
						}
						if (getRowCount() <= 0 && !isSourceModel) {
							// Clears description panel if there is no
							// descriptions
							// in target table.
							EventMediator.getInstance().fireEvent(new ClearDescriptionPanelEvent(true));
						}
					}
					List<Integer> sourceIds = new ArrayList<Integer>();
					for (I_GetConceptData sourceLang : sourceLanguageRefsets) {
						sourceIds.add(sourceLang.getConceptNid());
					}
					int targetId = project.getTargetLanguageRefset().getConceptNid();
					if (isSourceModel) {
						EventMediator.getInstance().fireEvent(new UpdateSimilarityEvent(result.getSourceFsnConcept(), result.getSourcePreferedConcept(), worklistMember.getConcept(), sourceIds, targetId));
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (TerminologyException e) {
					e.printStackTrace();
				}
			}
			Collections.sort(data, new ArrayComparator(TermTableColumn.ROW_CLASS.ordinal(), true));
			fireTableDataChanged();
			termPanel.selectRow(isSourceModel, translConfig);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean addRowToData(I_GetConceptData langRefset, boolean bSourceFSN, I_ContextualizeDescription description) throws IOException, TerminologyException {
		int authId;
		boolean bNewNode = false;
		Object[] termType_Status = new Object[3];
		Object[] termAcceptability_Status = new Object[2];
		Object[] row = new Object[columns.size()];
		if (getDescriptionId(description.getDescId(), Terms.get().getPath(UUID.fromString(SNOMED_CORE_PATH_UID)).getConceptNid()) != null) {
			termType_Status[2] = true;
		} else {
			termType_Status[2] = false;
		}

		if (description.getExtensionStatusId() == inactive.getConceptNid()) {
			termAcceptability_Status[0] = false;
		} else {
			termAcceptability_Status[0] = true;
		}
		termAcceptability_Status[1] = "";
		if (description.getAcceptabilityId() == acceptable.getConceptNid()) {
			termAcceptability_Status[1] = acceptable;
		} else if (description.getAcceptabilityId() == preferred.getConceptNid()) {
			termAcceptability_Status[1] = preferred;
		}
		boolean descriptionUncommited = description.getDescriptionPart().getTime() == Long.MAX_VALUE || description.getLanguageExtensionPart().getTime() == Long.MAX_VALUE;
		row[TermTableColumn.CHANGE.ordinal()] = descriptionUncommited;
		row[TermTableColumn.TERM.ordinal()] = description;
		row[TermTableColumn.LANGUAGE.ordinal()] = new String[] { langRefset.getUids().iterator().next().toString(), langRefset.getInitialText() };
		row[TermTableColumn.ICS.ordinal()] = description.isInitialCaseSignificant();
		if (description.getDescriptionStatusId() == inactive.getConceptNid()) {
			if (termCom.contains(ConfigTranslationModule.TreeComponent.RETIRED)) {
				row[TermTableColumn.ROW_CLASS.ordinal()] = TreeEditorObjectWrapper.NOTACCEPTABLE;
				termType_Status[1] = inactive;
				if (description.getTypeId() == fsn.getConceptNid()) {
					termType_Status[0] = fsn;
				} else {
					// row[TableSourceColumn.TERM_TYPE.ordinal()]=this.description;
					termType_Status[0] = synonym;
				}
				row[TermTableColumn.TERM_TYPE.ordinal()] = termType_Status;
				bNewNode = true;
			} else {
				return false;
			}
		} else if (description.getTypeId() == fsn.getConceptNid()) {
			result.setSourceFsnConcept(description);
			if (termCom.contains(ConfigTranslationModule.TreeComponent.FSN)) {
				row[TermTableColumn.ROW_CLASS.ordinal()] = TreeEditorObjectWrapper.FSNDESCRIPTION;
				termType_Status[0] = fsn;
				termType_Status[1] = active;
				row[TermTableColumn.TERM_TYPE.ordinal()] = termType_Status;
				result.setTargetFsn(description.getText());
				bNewNode = true;
			}
			if (!bSourceFSN) {
				bSourceFSN = true;
			}
		} else if (description.getAcceptabilityId() == acceptable.getConceptNid() && termCom.contains(ConfigTranslationModule.TreeComponent.SYNONYM)) {
			row[TermTableColumn.ROW_CLASS.ordinal()] = TreeEditorObjectWrapper.SYNONYMN;
			termType_Status[0] = synonym;
			termType_Status[1] = active;
			row[TermTableColumn.TERM_TYPE.ordinal()] = termType_Status;
			bNewNode = true;
		} else if (description.getAcceptabilityId() == preferred.getConceptNid()) {
			result.setSourcePreferedConcept(description);
			result.setTargetPreferred(description.getText());
			if (termCom.contains(ConfigTranslationModule.TreeComponent.PREFERRED)) {
				row[TermTableColumn.ROW_CLASS.ordinal()] = TreeEditorObjectWrapper.PREFERRED;
				termType_Status[0] = synonym;
				termType_Status[1] = active;
				row[TermTableColumn.TERM_TYPE.ordinal()] = termType_Status;
				bNewNode = true;
			}
		} else if (termCom.contains(ConfigTranslationModule.TreeComponent.RETIRED)) {
			row[TermTableColumn.ROW_CLASS.ordinal()] = TreeEditorObjectWrapper.SYNONYMN;
			termType_Status[0] = synonym;
			termType_Status[1] = inactive;
			row[TermTableColumn.TERM_TYPE.ordinal()] = termType_Status;
			bNewNode = true;
		}
		if (bNewNode) {
			if ((!isSourceModel && translConfig.getTargetTreeComponents().contains(ConfigTranslationModule.TreeComponent.AUTHOR_PATH)) || (isSourceModel && translConfig.getSourceTreeComponents().contains(ConfigTranslationModule.TreeComponent.AUTHOR_PATH))) {
				authId = description.getDescriptionVersioned().getLastTuple().getAuthorNid();
				String userConcept = "";
				if (hashAuthId.containsKey(authId))
					userConcept = hashAuthId.get(authId);
				else {
					I_GetConceptData conc = Terms.get().getConcept(authId);
					if (conc != null) {
						userConcept = conc.toString();
						hashAuthId.put(authId, userConcept);
					}
				}
				if (!userConcept.equals("")) {
					row[TermTableColumn.AUTHOR.ordinal()] = userConcept;
				}
			}
		}
		row[TermTableColumn.ACCEPTABILITY.ordinal()] = termAcceptability_Status;
		data.add(row);
		return bSourceFSN;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.DefaultTableModel#getColumnCount()
	 */
	public int getColumnCount() {
		ConfigTranslationModule cfg = null;
		try {
			cfg = LanguageUtil.getTranslationConfig(Terms.get().getActiveAceFrameConfig());
			if (!isSourceModel && cfg.getTargetTreeComponents().contains(TreeComponent.AUTHOR_PATH)) {
				this.columnCount = columns.size() - 1;
				return columnCount;
			} else if (isSourceModel && cfg.getSourceTreeComponents().contains(TreeComponent.AUTHOR_PATH)) {
				this.columnCount = columns.size() - 1;
				return columnCount;
			} else {
				this.columnCount = columns.size() - 2;
				return columnCount;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.columnCount = columns.size() - 1;
		return columnCount;
	}

	/**
	 * Sets the columns.
	 * 
	 * @param columns
	 *            the new columns
	 */
	public void setColumns(LinkedHashSet<TermTableColumn> columns) {
		this.columns = columns;
		fireTableStructureChanged();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.DefaultTableModel#setColumnCount(int)
	 */
	@Override
	public void setColumnCount(int columnCount) {
		this.columnCount = columnCount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.DefaultTableModel#getRowCount()
	 */
	public int getRowCount() {
		if (data != null && data.size() > 0) {
			return data.size();
		} else {
			return 0;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.DefaultTableModel#getColumnName(int)
	 */
	public String getColumnName(int col) {
		int i = 0;
		for (TermTableColumn inboxColumn : columns) {
			if (i == col) {
				return inboxColumn.getColumnName();
			}
			i++;
		}
		return "";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.DefaultTableModel#getValueAt(int, int)
	 */
	public Object getValueAt(int row, int col) {
		int i = 0;
		if (data != null && !data.isEmpty())
			for (TermTableColumn inboxColumn : columns) {
				if (i == col) {
					return data.get(row)[inboxColumn.ordinal()];
				} else if (col == InboxColumn.values().length) {
					return data.get(row)[InboxColumn.values().length];
				}
				i++;
			}
		return "";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.DefaultTableModel#removeRow(int)
	 */
	@Override
	public void removeRow(int row) {
		data.remove(row);
		fireTableRowsDeleted(row, row);
	}

	/*
	 * JTable uses this method to determine the default renderer/ editor for
	 * each cell. If we didn't implement this method, then the last column would
	 * contain text ("true"/"false"), rather than a check box.
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Class getColumnClass(int c) {
		if (getValueAt(0, c) != null) {
			return getValueAt(0, c).getClass();
		} else {
			return null;
		}
	}

	/*
	 * Don't need to implement this method unless your table's editable.
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
	 */
	public boolean isCellEditable(int row, int col) {
		return false;
	}

	/*
	 * Don't need to implement this method unless your table's data can change.
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.DefaultTableModel#setValueAt(java.lang.Object,
	 * int, int)
	 */
	public void setValueAt(Object value, int row, int col) {
		data.get(row)[col] = value;
		fireTableCellUpdated(row, col);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.DefaultTableModel#setRowCount(int)
	 */
	@Override
	public void setRowCount(int rowCount) {
		super.setRowCount(data.size());
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

	/**
	 * Update row.
	 * 
	 * @param currentRow
	 *            the current row
	 * @param modelRowNum
	 *            the model row num
	 * @param specialTag
	 *            the special tag
	 */
	public void updateRow(Object[] currentRow, int modelRowNum, boolean specialTag) {
		Object[] rowUpdated = null;
		WfInstance wfInstance = (WfInstance) currentRow[currentRow.length - 1];
		WfInstance wfInstanceUpdated = WfComponentProvider.getWfInstance(wfInstance.getComponentId());
		rowUpdated = createRow(wfInstanceUpdated, specialTag);
		data.remove(modelRowNum);
		if (rowUpdated != null) {
			data.add(modelRowNum, rowUpdated);
		}
		fireTableDataChanged();
	}

	/**
	 * Creates the row.
	 * 
	 * @param wfInstance
	 *            the wf instance
	 * @param specialTag
	 *            the special tag
	 * @return the object[]
	 */
	private Object[] createRow(WfInstance wfInstance, boolean specialTag) {
		Object[] row = null;
		String tagStr = "";
		try {
			tags = TagManager.getInstance().getAllTagsContent();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		if (tags != null) {
			for (InboxTag tag : tags) {
				boolean contains = false;
				String[] tagWorklistConceptUuids = TagManager.getTagWorklistConceptUuids(wfInstance);
				for (String[] uuidlist : tag.getUuidList()) {
					String[] wlanduuid = tagWorklistConceptUuids;
					if (uuidlist[InboxTag.TERM_WORKLIST_UUID_INDEX].equals(wlanduuid[InboxTag.TERM_WORKLIST_UUID_INDEX]) && uuidlist[InboxTag.TERM_UUID_INDEX].equals(wlanduuid[InboxTag.TERM_UUID_INDEX])) {
						contains = true;
						break;
					}
				}
				if (contains) {
					// wfInstance is tagged
					if ((tag.getTagName().equals(TagManager.OUTBOX) || tag.getTagName().equals(TagManager.TODO)) && !specialTag) {
						// Item tag is special, and tree item selected is not
						// specialtag
						return null;
					} else {
						// Item tag isnot special or tree item selected is not
						// outbox o todo
						tagStr = TagManager.getInstance().getHeader(tag.getTagName(), tag.getColor(), tag.getTextColor());
						tagCache.put(tagWorklistConceptUuids[InboxTag.TERM_WORKLIST_UUID_INDEX] + tagWorklistConceptUuids[InboxTag.TERM_UUID_INDEX], tag);
					}
				}
			}
		}
		String sourcePreferred = "";
		TranslationProject translationProject = null;
		I_TerminologyProject projectConcept;
		try {
			List<I_GetConceptData> langRefsets = null;
			List<ContextualizedDescription> descriptions = new ArrayList<ContextualizedDescription>();
			projectConcept = TerminologyProjectDAO.getProjectForWorklist(wfInstance.getWorkList(), config);
			translationProject = TerminologyProjectDAO.getTranslationProject(projectConcept.getConcept(), config);
			langRefsets = translationProject.getSourceLanguageRefsets();
			for (I_GetConceptData langRefset : langRefsets) {
				descriptions = LanguageUtil.getContextualizedDescriptions(Terms.get().getConcept(wfInstance.getComponentId()).getConceptNid(), langRefset.getConceptNid(), true);
				for (I_ContextualizeDescription description : descriptions) {
					if (description.getLanguageExtension() != null && description.getLanguageRefsetId() == langRefset.getConceptNid()) {
						if (description.getAcceptabilityId() == preferred.getConceptNid() && description.getTypeId() == synonym.getConceptNid()) {
							sourcePreferred = description.getText();
							if (!sourcePreferred.equals("")) {
								break;
							}
						}
					}
				}
				if (!sourcePreferred.equals("")) {
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		String targetFSN = "";
		String targetPreferred = "";

		try {
			I_GetConceptData langRefset = null;
			List<ContextualizedDescription> descriptions = new ArrayList<ContextualizedDescription>();
			langRefset = translationProject.getTargetLanguageRefset();
			descriptions = LanguageUtil.getContextualizedDescriptions(Terms.get().uuidToNative(wfInstance.getComponentId()), langRefset.getConceptNid(), true);
			for (I_ContextualizeDescription description : descriptions) {
				if (description.getLanguageExtension() != null && description.getLanguageRefsetId() == langRefset.getConceptNid()) {

					if (description.getTypeId() == fsn.getConceptNid()) {
						targetFSN = description.getText();
					} else if (description.getAcceptabilityId() == preferred.getConceptNid()) {
						targetPreferred = description.getText();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		row = new Object[InboxColumn.values().length + 1];
		String componentStr = tagStr + sourcePreferred;
		row[InboxColumn.SOURCE_PREFERRED.getColumnNumber()] = componentStr;
		row[InboxColumn.TARGET_FSN.getColumnNumber()] = targetFSN;
		// row[InboxColumn.STATUS_DATE.getColumnNumber()] = targetPreferred;
		row[InboxColumn.TARGET_PREFERRED.getColumnNumber()] = targetPreferred;
		row[InboxColumn.WORKLIST.getColumnNumber()] = wfInstance.getWorkList().getName();
		row[InboxColumn.DESTINATION.getColumnNumber()] = wfInstance.getDestination().getUsername();
		row[InboxColumn.STATUS.getColumnNumber()] = wfInstance.getState().getName();
		row[InboxColumn.values().length] = wfInstance;
		return row;
	}

	public Integer getPreferedRow() {
		int rc = getRowCount();
		for (int i = 0; i < rc; i++) {
			Integer termeditorwrapper = (Integer) getValueAt(i, TermTableColumn.ROW_CLASS.ordinal());
			if (termeditorwrapper.equals(TreeEditorObjectWrapper.PREFERRED)) {
				return i;
			}
		}
		return null;
	}

	public Integer getTargetFsnRow() {
		int rc = getRowCount();
		for (int i = 0; i < rc; i++) {
			Integer termeditorwrapper = (Integer) getValueAt(i, TermTableColumn.ROW_CLASS.ordinal());
			if (termeditorwrapper.equals(TreeEditorObjectWrapper.FSNDESCRIPTION)) {
				return i;
			}
		}
		return null;
	}
	//
	// private void addDescriptionToTable(I_ContextualizeDescription addedDesc)
	// throws IOException, TerminologyException {
	// addRowToData(targetLanguage, false, addedDesc);
	// Collections.sort(data, new
	// ArrayComparator(TermTableColumn.ROW_CLASS.ordinal(), true));
	// fireTableDataChanged();
	// int i = 0;
	// if (addedDesc.getLang().equals(targetLangRefset.getLangCode(config))) {
	// for (Object[] integer : data) {
	// ContextualizedDescription description = (ContextualizedDescription)
	// integer[TermTableColumn.TERM.ordinal()];
	// if (description.getDescId() == addedDesc.getDescId()) {
	// termPanel.selectRow(i);
	// break;
	// }
	// i++;
	// }
	// }
	// };
}

class ArrayComparator implements Comparator<Object[]> {
	private final int columnToSort;
	private final boolean ascending;

	public ArrayComparator(int columnToSort, boolean ascending) {
		this.columnToSort = columnToSort;
		this.ascending = ascending;
	}

	public int compare(Object[] c1, Object[] c2) {
		Integer c1int = (Integer) c1[columnToSort];
		Integer c2int = (Integer) c2[columnToSort];

		return ascending ? c1int.compareTo(c2int) : c1int.compareTo(c2int) * -1;
	}
}
