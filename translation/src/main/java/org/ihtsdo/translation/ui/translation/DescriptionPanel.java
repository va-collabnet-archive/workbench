/*
 * Created by JFormDesigner on Tue Mar 06 16:15:08 GMT-03:00 2012
 */

package org.ihtsdo.translation.ui.translation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IdVersion;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.config.AceFrameConfig;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.document.DocumentManager;
import org.ihtsdo.project.ContextualizedDescription;
import org.ihtsdo.project.I_ContextualizeDescription;
import org.ihtsdo.project.TerminologyProjectDAO;
import org.ihtsdo.project.help.HelpApi;
import org.ihtsdo.project.model.TranslationProject;
import org.ihtsdo.project.model.WorkList;
import org.ihtsdo.project.model.WorkListMember;
import org.ihtsdo.project.refset.LanguageMembershipRefset;
import org.ihtsdo.project.util.IconUtilities;
import org.ihtsdo.project.view.event.EventMediator;
import org.ihtsdo.project.view.event.GenericEvent.EventType;
import org.ihtsdo.project.workflow.api.WfComponentProvider;
import org.ihtsdo.project.workflow.api.WorkflowInterpreter;
import org.ihtsdo.project.workflow.model.WfAction;
import org.ihtsdo.project.workflow.model.WfInstance;
import org.ihtsdo.project.workflow.model.WfPermission;
import org.ihtsdo.project.workflow.model.WfRole;
import org.ihtsdo.project.workflow.model.WfUser;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationThreadingPolicy;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.translation.FSNGenerationException;
import org.ihtsdo.translation.LanguageUtil;
import org.ihtsdo.translation.ui.ConfigTranslationModule;
import org.ihtsdo.translation.ui.ConfigTranslationModule.EditorMode;
import org.ihtsdo.translation.ui.event.CancelAllPanelEvent;
import org.ihtsdo.translation.ui.event.ClearDescriptionPanelEvent;
import org.ihtsdo.translation.ui.event.ClearDescriptionPanelEventHandler;
import org.ihtsdo.translation.ui.event.DescriptionSavedEvent;
import org.ihtsdo.translation.ui.event.FireSaveEvent;
import org.ihtsdo.translation.ui.event.FireSaveEventHandler;
import org.ihtsdo.translation.ui.event.SaveDescriptionEvent;
import org.ihtsdo.translation.ui.event.SpellcheckEvent;
import org.ihtsdo.translation.ui.event.SpellcheckEventHandler;
import org.ihtsdo.translation.ui.event.TargetTableItemSelectedEvent;
import org.ihtsdo.translation.ui.event.TargetTableItemSelectedEventHandler;

/**
 * @author Guillermo Reynoso
 */
public class DescriptionPanel extends JPanel {
	private static final long serialVersionUID = -5368641320712644175L;
	/** The synonym. */
	private I_GetConceptData synonym;
	/** The fsn. */
	private I_GetConceptData fsn;
	/** The preferred. */
	private I_GetConceptData preferred;
	/** The acceptable. */
	private I_GetConceptData acceptable;
	private I_GetConceptData active;
	/** The inactive. */
	private I_GetConceptData inactive;
	private org.ihtsdo.translation.ui.translation.DescriptionPanel.UpdateUIWorker updateUiWorker;
	private WorkListMember instance;
	private ConfigTranslationModule translConfig;
	/** The canc action. */
	private WfAction cancAction;
	private ContextualizedDescription descriptionInEditor;
	private I_ConfigAceFrame config;
	private I_GetConceptData targetLanguage;
	private LanguageMembershipRefset targetLangRefset;
	private LanguageMembershipRefset sourceLangRefset;

	private boolean keepWaitingForChanges = true;
	private boolean changeHappend = false;
	private boolean updating = false;
	private int typingTime = 0;
	boolean stillTypeing;
	private DocumentListener documentListener;
	private boolean saveing;
	private ChangePersisterWorker changePersisterWorker;
	private ContextualizedDescription prevDesInEditor = null;
	public final static String SNOMED_CORE_PATH_UID = "8c230474-9f11-30ce-9cad-185a96fd03a2";
	private static String lastAction = "";

	public DescriptionPanel() {
		initComponents();
		initCustomComponents();
	}

	public DescriptionPanel(ConfigTranslationModule translConfig) {
		initComponents();
		initCustomComponents();
	}

	private void initCustomComponents() {
		suscribeToEvents();
		try {
			fsn = Terms.get().getConcept(SnomedMetadataRf2.FULLY_SPECIFIED_NAME_RF2.getLenient().getNid());
			preferred = Terms.get().getConcept(SnomedMetadataRf2.PREFERRED_RF2.getLenient().getNid());
			synonym = Terms.get().getConcept(SnomedMetadataRf2.SYNONYM_RF2.getLenient().getNid());
			acceptable = Terms.get().getConcept(SnomedMetadataRf2.ACCEPTABLE_RF2.getLenient().getNid());
			active = Terms.get().getConcept(SnomedMetadataRf2.ACTIVE_VALUE_RF2.getLenient().getNid());
			inactive = Terms.get().getConcept(SnomedMetadataRf2.INACTIVE_VALUE_RF2.getLenient().getNid());

			cancAction = new WfAction();
			cancAction.setBusinessProcess(new File("sampleProcesses/CancelActionWithoutDestination.bp"));
			cancAction.setId(UUID.randomUUID());
			cancAction.setName("Cancel");
			cancAction.setConsequence(null);

			if (changePersisterWorker != null && !changePersisterWorker.isDone()) {
				changePersisterWorker.cancel(true);
				changePersisterWorker = null;
			}

			changePersisterWorker = new ChangePersisterWorker();
			changePersisterWorker.execute();

			documentListener = new DocumentListener() {
				@Override
				public void removeUpdate(DocumentEvent e) {
					if (descriptionInEditor != null) {
						descriptionInEditor.setText(targetTextField.getText());
						typingTime = 100;
						changeHappend = true && !updating && !saveing;
					}
				}

				@Override
				public void insertUpdate(DocumentEvent e) {
					if (descriptionInEditor != null) {
						descriptionInEditor.setText(targetTextField.getText());
						typingTime = 100;
						changeHappend = true && !updating && !saveing;
					}
				}

				@Override
				public void changedUpdate(DocumentEvent arg0) {
				}
			};

			targetTextField.getDocument().addDocumentListener(documentListener);

			label13.setIcon(IconUtilities.helpIcon);
			label13.setText("");
			label12.setIcon(IconUtilities.helpIcon);
			label12.setText("");

			comboBox1.addItem(synonym);
			comboBox1.addItem(fsn);
			comboBox1.setSelectedItem(synonym);
			comboBox1.setEnabled(false);

			cmbAccep.addItem(preferred);
			cmbAccep.addItem(acceptable);
			cmbAccep.setSelectedIndex(1);
			cmbAccep.setEnabled(false);

			configureDescriptionPanel();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	class ChangePersisterWorker extends SwingWorker<String, String> {
		@Override
		protected String doInBackground() throws Exception {
			while (keepWaitingForChanges) {
				try {
					Thread.currentThread().setName("Change persister");
					Thread.sleep(20);
					if (isCancelled()) {
						break;
					}
					if (changeHappend) {
						while (typingTime > 0) {
							Thread.sleep(20);
							typingTime = typingTime - 20;
							if (isCancelled()) {
								break;
							}
						}
						if (isCancelled()) {
							break;
						}
						partialPersist();
						changeHappend = false;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return null;
		}
	}

	private void configureDescriptionPanel() {
		if (translConfig != null) {
			if (translConfig.getSelectedEditorMode().equals(ConfigTranslationModule.EditorMode.PREFERRED_TERM_EDITOR)) {
				comboBox1.setEnabled(false);
				cmbAccep.setEnabled(false);
			} else if (translConfig.getSelectedEditorMode().equals(ConfigTranslationModule.EditorMode.SYNONYMS_EDITOR)) {
				comboBox1.setEnabled(false);
				cmbAccep.setEnabled(false);
			} else if (translConfig.getSelectedEditorMode().equals(ConfigTranslationModule.EditorMode.FULL_EDITOR)) {
				comboBox1.setEnabled(true && !readOnlyMode);
				cmbAccep.setEnabled(true && !readOnlyMode);
			}
		}
	}

	public void setTranslConfig(ConfigTranslationModule translConfig) {
		this.translConfig = translConfig;
	}

	private void partialPersist() {
		updateDescriptionInEditor();
		try {
			if (descriptionInEditor != null) {
				AceLog.getAppLog().info("Persisting changes");
				Ts.get().removeTermChangeListener(TranslationTermChangeListener.getInstance());
				descriptionInEditor.persistChanges(true, false);
				EventMediator.getInstance().fireEvent(new DescriptionSavedEvent(descriptionInEditor, instance.getWorkListUUID()));
				Ts.get().addTermChangeListener(TranslationTermChangeListener.getInstance());
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	private void suscribeToEvents() {
		EventMediator mediator = EventMediator.getInstance();

		mediator.suscribe(EventType.FIRE_SAVE, new FireSaveEventHandler<FireSaveEvent>(this) {
			@Override
			public void handleEvent(FireSaveEvent event) {
				bLaunchActionPerformed();
			}
		});

		mediator.suscribe(EventType.TARGET_TABLE_ITEM_SELECTED, new TargetTableItemSelectedEventHandler<TargetTableItemSelectedEvent>(this) {
			@Override
			public void handleEvent(TargetTableItemSelectedEvent event) {
				ContextualizedDescription description = event.getDescription();
				int lastSelection = event.getLast();
				updatePropertiesPanel(description, lastSelection);
			}
		});

		mediator.suscribe(EventType.SPELLCHECK_EVENT, new SpellcheckEventHandler<SpellcheckEvent>(this) {
			@Override
			public void handleEvent(SpellcheckEvent event) {
				if (!doSpellCheck()) {
					spellcheckok.setVisible(true);
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							try {
								Thread.sleep(2000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							spellcheckok.setVisible(false);
						}
					});
				}
			}

		});

		mediator.suscribe(EventType.CLEAR_DESCRIPTION_PANEL_EVENT, new ClearDescriptionPanelEventHandler<ClearDescriptionPanelEvent>(this) {
			@Override
			public void handleEvent(ClearDescriptionPanelEvent event) {
				clearForm();
				setReadOnlyMode(event.getReadOnlyMode());
			}
		});
	}

	protected boolean fsnControlPass() {
		List<ContextualizedDescription> descriptions;
		try {
			descriptions = LanguageUtil.getContextualizedDescriptions(instance.getConcept().getConceptNid(), targetLanguage.getConceptNid(), true);
			for (I_ContextualizeDescription description : descriptions) {
				if (description.getLanguageExtension() != null && description.getLanguageRefsetId() == targetLanguage.getConceptNid()) {
					if (description.getTypeId() == fsn.getConceptNid() && (LanguageUtil.isActive(description.getDescriptionStatusId()) || LanguageUtil.isActive(description.getExtensionStatusId()))) {
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

	protected boolean preferredControlPass(ContextualizedDescription contextualizedDescription) {
		List<ContextualizedDescription> descriptions;
		try {
			descriptions = LanguageUtil.getContextualizedDescriptions(instance.getConcept().getConceptNid(), targetLanguage.getConceptNid(), true);
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

	protected void updateDescriptionInEditor() {
		AceLog.getAppLog().info("updating Description in editor");
		if (descriptionInEditor == null && !targetTextField.getText().trim().equals("") && rbAct.isSelected()) {
			try {
				AceLog.getAppLog().info("creating new contextualized description. this will fire addUncommittedNoChecks");
				descriptionInEditor = (ContextualizedDescription) ContextualizedDescription.createNewContextualizedDescription(instance.getId(), targetLangRefset.getRefsetId(), targetLangRefset.getLangCode(config));
			} catch (Exception e) {
				e.printStackTrace();
			}
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
			if (!descriptionInEditor.getText().equals(targetTextField.getText())) {
				descriptionInEditor.setText(targetTextField.getText());
			}
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
			if (rbInact.isSelected() && targetLangCode.equals(descriptionInEditor.getLang())) {
				descriptionInEditor.setDescriptionStatusId(inactive.getConceptNid());
			}
			if (extInactive.isSelected()) {
				descriptionInEditor.setExtensionStatusId(inactive.getConceptNid());
			}
			if (rbAct.isSelected() && targetLangCode.equals(descriptionInEditor.getLang())) {
				descriptionInEditor.setDescriptionStatusId(active.getConceptNid());
			}
			if (extActive.isSelected()) {
				descriptionInEditor.setExtensionStatusId(active.getConceptNid());
			}

			if (descriptionInEditor.getAcceptabilityId() != ((I_GetConceptData) cmbAccep.getSelectedItem()).getConceptNid()) {
				descriptionInEditor.setAcceptabilityId(((I_GetConceptData) cmbAccep.getSelectedItem()).getConceptNid());
			}
		}
	}

	private boolean doSpellCheck() {
		AceFrameConfig config;
		try {
			if (!targetTextField.getText().trim().equals("") && !readOnlyMode) {
				config = (AceFrameConfig) Terms.get().getActiveAceFrameConfig();
				String spellcheckPhrase = DocumentManager.spellcheckPhrase(targetTextField.getText(), null, targetLangRefset.getLangCode(config));
				if (spellcheckPhrase.equals(targetTextField.getText())) {
					return false;
				} else {
					targetTextField.setText(spellcheckPhrase);
					descriptionInEditor.setText(spellcheckPhrase);
					try {
						descriptionInEditor.persistChanges();
					} catch (Exception e) {
						e.printStackTrace();
					}
					return true;
				}
			} else {
				return true;
			}
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	private boolean targetTextChanged() {
		return descriptionInEditor.getText().trim().equals(targetTextField.getText().trim());
	}

	/**
	 * Update properties panel.
	 * 
	 * @param descrpt
	 *            the descrpt
	 * @param rowModel
	 *            the row model
	 */
	private void updatePropertiesPanel(ContextualizedDescription descrpt, int last) {
		updating = true;
		try {
			if (descriptionInEditor != null) {
				updateDescriptionInEditor();
			}
			if (descrpt == null) {
				descriptionInEditor = null;
				targetTextField.setText("");
				rbYes.setSelected(false);
				targetTextField.setEnabled(false);
			} else {
				updateDescriptionPanel(descrpt);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			changeHappend = false;
			updating = false;
		}
	}

	private void updateDescriptionPanel(ContextualizedDescription descrpt) {
		// if(prevDesInEditor != null &&
		// !prevDesInEditor.getText().equals(descrpt.getText())){
		// doSpellCheckLast();
		// }
		String langCode = "";
		try {
			langCode = targetLangRefset.getLangCode(Terms.get().getActiveAceFrameConfig());
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
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
			prevDesInEditor = descriptionInEditor;
			descriptionInEditor = descrpt;
			// label4.setText(Terms.get().getConcept(descriptionInEditor.getTypeId()).toString());
			if (descriptionInEditor.getText().equalsIgnoreCase("new description")) {
				targetTextField.setText(descriptionInEditor.getText().trim());
				targetTextField.selectAll();
			} else {
				targetTextField.setText(descriptionInEditor.getText().trim());
			}
			targetTextField.grabFocus();
			if (descriptionInEditor.isInitialCaseSignificant()) {
				rbYes.setSelected(true);
			} else {
				rbNo.setSelected(true);
			}
			if (descriptionInEditor.getDescriptionStatusId() == active.getConceptNid()) {
				rbAct.setSelected(true);
			} else {
				rbInact.setSelected(true);
			}
			if (descriptionInEditor.getExtensionStatusId() == active.getConceptNid()) {
				extActive.setSelected(true);
			} else {
				extInactive.setSelected(true);
			}
		}
		configureDescriptionPanel();
		setReadOnlyMode(readOnlyMode);

		if (!langCode.equals(descrpt.getLang())) {
			targetTextField.setEnabled(false);
			setReadOnlyMode(true);
		} else {
			setReadOnlyMode(readOnlyMode);
			try {
				if (getDescriptionId(descrpt.getDescId(), Terms.get().getPath(UUID.fromString(SNOMED_CORE_PATH_UID)).getConceptNid()) != null) {
					targetTextField.setEnabled(false);
				} else {
					targetTextField.setEnabled(true && !readOnlyMode);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (TerminologyException e) {
				e.printStackTrace();
			}
		}

		bLaunch.setEnabled(true);
		cancelButton.setEnabled(true);
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

	// private void doSpellCheckLast() {
	// try {
	// translConfig = LanguageUtil.getTranslationConfig(config);
	// } catch (IOException e1) {
	// e1.printStackTrace();
	// }
	// if (translConfig.isEnableSpellChecker()) {
	// try {
	// // term change listener wont run if stand by
	// ContextualizedDescription descToSpellCheck = new
	// ContextualizedDescription(prevDesInEditor.getDescId(),
	// prevDesInEditor.getConceptId(), prevDesInEditor.getLanguageRefsetId());
	// String spellcheckPhrase =
	// DocumentManager.spellcheckPhrase(prevDesInEditor.getText(), null,
	// targetLangRefset.getLangCode(config));
	// descToSpellCheck.setText(spellcheckPhrase);
	// EventMediator.getInstance().fireEvent(new
	// SpellCheckedEvent(descToSpellCheck));
	// } catch (IOException e) {
	// e.printStackTrace();
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	// }

	private void targetTextFieldMouseClicked(MouseEvent e) {
		if (targetTextField.isEnabled()) {
			if (e.getClickCount() == 2) {
				zoomTextArea.setText(targetTextField.getText());
				zoomTextArea.setEnabled(targetTextField.isEnabled());
				zoomTextArea.revalidate();
				zoomTextArea.repaint();

				Dimension dimension = new Dimension(650, 350);
				termZoomDialog.setMaximumSize(dimension);
				termZoomDialog.setMinimumSize(dimension);
				termZoomDialog.setSize(dimension);
				termZoomDialog.setVisible(true);
				termZoomDialog.pack();
			}
		}
	}

	private void label13MouseClicked(MouseEvent e) {
		try {
			HelpApi.openHelpForComponent("TRANSLATION_EDIT");
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
	}

	private void bLaunchActionPerformed() {
		try {
			saveing = true;
			Ts.get().removeTermChangeListener(TranslationTermChangeListener.getInstance());

			Object actionObj = cmbActions.getSelectedItem();
			WfInstance wfInstance = instance.getWfInstance();
			if (actionObj instanceof WfAction) {
				boolean emptyDescription = isEmptyDescription(instance);

				Object[] emptyTranslOptions = { "Send empty translation", "Cancel" };
				if (emptyDescription) {
					int n = JOptionPane.showOptionDialog(null, "There is no translation in target language, would you like to continue?", "Unsaved data", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, emptyTranslOptions, emptyTranslOptions[1]);
					if (n == 1) {
						if (Terms.get().getUncommitted().size() > 0) {
							try {
								Terms.get().cancel();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						//descriptionInEditor = null;
						targetTextField.setText("");
						return;
					}
				}

				ConfigTranslationModule userPrefs = LanguageUtil.getTranslationConfig(config);

				if (translConfig.getSelectedEditorMode().equals(EditorMode.PREFERRED_TERM_EDITOR) && userPrefs.isEnableSpellChecker()) {
					doSpellCheck();
				}

				if (fsnControlPass()) {
					try {
						LanguageUtil.generateFSN(instance.getConcept(), sourceLangRefset, targetLangRefset, translationProject, config);
					} catch (FSNGenerationException e1) {
						e1.printStackTrace();
						JOptionPane.showOptionDialog(DescriptionPanel.this, e1.getMessage(), "Warning", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, null, null);
					}
				}

				if(descriptionInEditor != null){
					descriptionInEditor.persistChanges(true, false);
				}

				WfAction action = (WfAction) cmbActions.getSelectedItem();
				if (action != null) {
					lastAction = action.getName();

					I_Work worker = null;
					if (!commitInstance()) {
						Ts.get().addTermChangeListener(TranslationTermChangeListener.getInstance());
						setReadOnlyMode(readOnlyMode);
						saveing = false;
						return;
					}

					worker = Terms.get().getActiveAceFrameConfig().getWorker();
					WfInstance prevWfInstance = new WfInstance();
					prevWfInstance.setComponentId(wfInstance.getComponentId());
					prevWfInstance.setDestination(wfInstance.getDestination());
					prevWfInstance.setHistory(wfInstance.getHistory());
					prevWfInstance.setProperties(wfInstance.getProperties());
					prevWfInstance.setState(wfInstance.getState());
					prevWfInstance.setWfDefinition(wfInstance.getWfDefinition());
					prevWfInstance.setWorkList(wfInstance.getWorkList());
					if (!workflowInterpreter.doAction(wfInstance, wfRole, action, worker)) {
						Ts.get().addTermChangeListener(TranslationTermChangeListener.getInstance());
						setReadOnlyMode(readOnlyMode);
						saveing = false;
						return;
					}

					WfInstance newWfInstance = wfInstance;
					newWfInstance.setActionReport(wfInstance.getActionReport());
					// clearForm(true);
					setReadOnlyMode(true);
					bLaunch.setEnabled(false);
					cancelButton.setEnabled(false);
					if (commitInstance()) {
						EventMediator.getInstance().fireEvent(new SaveDescriptionEvent(prevWfInstance, newWfInstance, SaveDescriptionEvent.ActionType.ACTION_LAUNCHED));
					} else {
						Ts.get().addTermChangeListener(TranslationTermChangeListener.getInstance());
						setReadOnlyMode(readOnlyMode);
						saveing = false;
						return;
					}
				}
			} else if (actionObj instanceof String && actionObj.toString().equals(WfAction.SEND_TO_OUTBOX)) {
				// clearForm(true);
				setReadOnlyMode(true);
				bLaunch.setEnabled(false);
				cancelButton.setEnabled(false);
				WfInstance prevWfInstance = new WfInstance();
				EventMediator.getInstance().fireEvent(new SaveDescriptionEvent(wfInstance, prevWfInstance, SaveDescriptionEvent.ActionType.SEND_TO_OUTBOX_LAUNCHED));
			} else if (actionObj instanceof String && actionObj.toString().equals(WfAction.NO_ACTION)) {
				commitInstance();
			} else if (actionObj instanceof String && actionObj.toString().equals(WfAction.NEXT_ITEM)) {
				if (commitInstance()) {
					setReadOnlyMode(true);
					bLaunch.setEnabled(false);
					cancelButton.setEnabled(false);
					WfInstance prevWfInstance = new WfInstance();
					EventMediator.getInstance().fireEvent(new SaveDescriptionEvent(wfInstance, prevWfInstance, SaveDescriptionEvent.ActionType.NO_ACTION));
				} else {
					Ts.get().addTermChangeListener(TranslationTermChangeListener.getInstance());
					setReadOnlyMode(readOnlyMode);
					saveing = false;
					return;
				}
			}
		} catch (TerminologyException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		} catch (Exception e) {
			e.printStackTrace();
		}
		saveing = false;
	}

	private boolean isEmptyDescription(WorkListMember instance) {
		boolean isEmpty = true;
		List<ContextualizedDescription> targetDescriptions;
		try {
			targetDescriptions = LanguageUtil.getContextualizedDescriptions(instance.getConcept().getConceptNid(), targetLangRefset.getRefsetId(), true);
			for (ContextualizedDescription loopDescription : targetDescriptions) {
				if (LanguageUtil.isActive(loopDescription.getDescriptionStatusId()) && LanguageUtil.isActive(loopDescription.getExtensionStatusId()) && loopDescription.getLanguageExtension() != null) {
					isEmpty = false;
				}
			}
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return isEmpty;
	}

	private boolean commitInstance() throws IOException {
		return instance.getConcept().commit(ChangeSetGenerationPolicy.INCREMENTAL, ChangeSetGenerationThreadingPolicy.SINGLE_THREAD);
	}

	private void cancelButtonActionPerformed() {
		try {
			I_GetConceptData cToCancel = instance.getConcept();
			//EventMediator.getInstance().fireEvent(new CancelAllPanelEvent());
			cToCancel.cancel();
			updateDescriptionInEditor();
			//clearForm();
			//setReadOnlyMode(true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

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
	 * Update ui.
	 * 
	 * @param instance
	 *            the instance
	 * @param readOnlyMode
	 *            the read only mode
	 * @param translConfig
	 * @param isOutbox
	 */
	public void updateDescriptionPanel(WorkListMember instance, boolean readOnlyMode, ConfigTranslationModule translConfig, boolean isOutbox) {
		this.instance = instance;
		this.readOnlyMode = readOnlyMode;
		this.translConfig = translConfig;
		prevDesInEditor = null;
		try {
			config = Terms.get().getActiveAceFrameConfig();
			translationProject = (TranslationProject) TerminologyProjectDAO.getProjectForWorklist(instance.getWfInstance().getWorkList(), config);
			targetLanguage = translationProject.getTargetLanguageRefset();
			targetLangRefset = new LanguageMembershipRefset(targetLanguage, config);
			sourceLangRefset = new LanguageMembershipRefset(translationProject.getSourceLanguageRefsets().iterator().next(), config);
			if (updateUiWorker != null && !updateUiWorker.isDone()) {
				updateUiWorker.cancel(true);
				updateUiWorker = null;
			}
			updateUiWorker = new UpdateUIWorker(instance, isOutbox);
			updateUiWorker.execute();
		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void rbActStateChanged(ChangeEvent e) {
		changeHappend = true && !updating && !saveing;
	}

	private void rbYesStateChanged(ChangeEvent e) {
		changeHappend = true && !updating && !saveing;
	}

	private void comboBox1ItemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			changeHappend = true && !updating && !saveing;
		}
	}

	private void cmbAccepItemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
			changeHappend = true && !updating && !saveing;
		}
	}

	private void rbInactStateChanged(ChangeEvent e) {
		changeHappend = true && !updating && !saveing;
	}

	private void rbNoStateChanged(ChangeEvent e) {
		changeHappend = true && !updating && !saveing;
	}

	private void saveZoomButtonActionPerformed(ActionEvent e) {
		targetTextField.setText(zoomTextArea.getText());
		termZoomDialog.dispose();
		zoomTextArea.setText("");
	}

	private void cancelZoomChangeActionPerformed(ActionEvent e) {
		termZoomDialog.dispose();
		zoomTextArea.setText("");
	}

	private void extActiveStateChanged(ChangeEvent e) {
		changeHappend = true && !updating && !saveing;
	}

	private void extInactiveStateChanged(ChangeEvent e) {
		changeHappend = true && !updating && !saveing;
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		panel2 = new JPanel();
		panel15 = new JPanel();
		separator1 = new JSeparator();
		label2 = new JLabel();
		scrollPane5 = new JScrollPane();
		targetTextField = new JTextArea();
		spellcheckok = new JLabel();
		panel18 = new JPanel();
		label1 = new JLabel();
		comboBox1 = new JComboBox();
		label7 = new JLabel();
		rbAct = new JRadioButton();
		rbInact = new JRadioButton();
		label5 = new JLabel();
		cmbAccep = new JComboBox();
		label6 = new JLabel();
		extActive = new JRadioButton();
		extInactive = new JRadioButton();
		label3 = new JLabel();
		rbYes = new JRadioButton();
		rbNo = new JRadioButton();
		label13 = new JLabel();
		panel19 = new JPanel();
		label4 = new JLabel();
		cmbActions = new JComboBox();
		bLaunch = new JButton();
		cancelButton = new JButton();
		label12 = new JLabel();
		termZoomDialog = new JDialog();
		panel12 = new JPanel();
		scrollPane2 = new JScrollPane();
		zoomTextArea = new JTextArea();
		saveZoomButton = new JButton();
		cancelZoomChange = new JButton();

		// ======== this ========
		setLayout(new BorderLayout());

		// ======== panel2 ========
		{
			panel2.setLayout(new GridBagLayout());
			((GridBagLayout) panel2.getLayout()).columnWidths = new int[] { 0, 0 };
			((GridBagLayout) panel2.getLayout()).rowHeights = new int[] { 0, 0, 0 };
			((GridBagLayout) panel2.getLayout()).columnWeights = new double[] { 1.0, 1.0E-4 };
			((GridBagLayout) panel2.getLayout()).rowWeights = new double[] { 1.0, 1.0, 1.0E-4 };

			// ======== panel15 ========
			{
				panel15.setMinimumSize(new Dimension(10, 20));
				panel15.setPreferredSize(new Dimension(10, 20));
				panel15.setLayout(new GridBagLayout());
				((GridBagLayout) panel15.getLayout()).columnWidths = new int[] { 80, 0, 0 };
				((GridBagLayout) panel15.getLayout()).rowHeights = new int[] { 18, 0, 0, 0 };
				((GridBagLayout) panel15.getLayout()).columnWeights = new double[] { 0.0, 1.0, 1.0E-4 };
				((GridBagLayout) panel15.getLayout()).rowWeights = new double[] { 0.0, 0.0, 1.0, 1.0E-4 };
				panel15.add(separator1, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));

				// ---- label2 ----
				label2.setText("Term:");
				label2.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
				panel15.add(label2, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 5, 5), 0, 0));

				// ======== scrollPane5 ========
				{

					// ---- targetTextField ----
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
				panel15.add(scrollPane5, new GridBagConstraints(1, 1, 1, 2, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

				// ---- spellcheckok ----
				spellcheckok.setText("Spellcheck OK");
				spellcheckok.setFont(new Font("Lucida Grande", Font.BOLD, 11));
				spellcheckok.setBackground(new Color(238, 238, 238));
				spellcheckok.setForeground(new Color(15, 98, 25));
				spellcheckok.setVisible(false);
				panel15.add(spellcheckok, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 5), 0, 0));
			}
			panel2.add(panel15, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));

			// ======== panel18 ========
			{
				panel18.setPreferredSize(new Dimension(10, 12));
				panel18.setMinimumSize(new Dimension(10, 12));
				panel18.setLayout(new GridBagLayout());
				((GridBagLayout) panel18.getLayout()).columnWidths = new int[] { 0, 248, 0, 0, 0, 0, 0, 0 };
				((GridBagLayout) panel18.getLayout()).rowHeights = new int[] { 0, 0, 18, 0 };
				((GridBagLayout) panel18.getLayout()).columnWeights = new double[] { 0.0, 1.0, 1.0, 0.0, 1.0, 1.0, 0.0, 1.0E-4 };
				((GridBagLayout) panel18.getLayout()).rowWeights = new double[] { 0.0, 0.0, 0.0, 1.0E-4 };

				// ---- label1 ----
				label1.setText("Term type");
				label1.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
				panel18.add(label1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));

				// ---- comboBox1 ----
				comboBox1.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
				comboBox1.addItemListener(new ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent e) {
						comboBox1ItemStateChanged(e);
					}
				});
				panel18.add(comboBox1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));

				// ---- label7 ----
				label7.setText("Description:");
				label7.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
				panel18.add(label7, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));

				// ---- rbAct ----
				rbAct.setText("Active");
				rbAct.setSelected(true);
				rbAct.setBackground(new Color(238, 238, 238));
				rbAct.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
				rbAct.addChangeListener(new ChangeListener() {
					@Override
					public void stateChanged(ChangeEvent e) {
						rbActStateChanged(e);
					}
				});
				panel18.add(rbAct, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));

				// ---- rbInact ----
				rbInact.setText("Inactive");
				rbInact.setBackground(new Color(238, 238, 238));
				rbInact.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
				rbInact.addChangeListener(new ChangeListener() {
					@Override
					public void stateChanged(ChangeEvent e) {
						rbInactStateChanged(e);
					}
				});
				panel18.add(rbInact, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));

				// ---- label5 ----
				label5.setText("Acceptability:");
				label5.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
				panel18.add(label5, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));

				// ---- cmbAccep ----
				cmbAccep.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
				cmbAccep.addItemListener(new ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent e) {
						cmbAccepItemStateChanged(e);
					}
				});
				panel18.add(cmbAccep, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));

				// ---- label6 ----
				label6.setText("Lang refset member:");
				label6.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
				panel18.add(label6, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));

				// ---- extActive ----
				extActive.setText("Active");
				extActive.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
				extActive.addChangeListener(new ChangeListener() {
					@Override
					public void stateChanged(ChangeEvent e) {
						extActiveStateChanged(e);
					}
				});
				panel18.add(extActive, new GridBagConstraints(4, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));

				// ---- extInactive ----
				extInactive.setText("Inactive");
				extInactive.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
				extInactive.addChangeListener(new ChangeListener() {
					@Override
					public void stateChanged(ChangeEvent e) {
						extInactiveStateChanged(e);
					}
				});
				panel18.add(extInactive, new GridBagConstraints(5, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));

				// ---- label3 ----
				label3.setText("Is case significant?");
				label3.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
				panel18.add(label3, new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

				// ---- rbYes ----
				rbYes.setText("Yes");
				rbYes.setBackground(new Color(238, 238, 238));
				rbYes.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
				rbYes.addChangeListener(new ChangeListener() {
					@Override
					public void stateChanged(ChangeEvent e) {
						rbYesStateChanged(e);
					}
				});
				panel18.add(rbYes, new GridBagConstraints(4, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

				// ---- rbNo ----
				rbNo.setSelected(true);
				rbNo.setText("No");
				rbNo.setBackground(new Color(238, 238, 238));
				rbNo.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
				rbNo.addChangeListener(new ChangeListener() {
					@Override
					public void stateChanged(ChangeEvent e) {
						rbNoStateChanged(e);
					}
				});
				panel18.add(rbNo, new GridBagConstraints(5, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

				// ---- label13 ----
				label13.setText("text");
				label13.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
				label13.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						label13MouseClicked(e);
					}
				});
				panel18.add(label13, new GridBagConstraints(6, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));
			}
			panel2.add(panel18, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel2, BorderLayout.CENTER);

		// ======== panel19 ========
		{
			panel19.setLayout(new GridBagLayout());
			((GridBagLayout) panel19.getLayout()).columnWidths = new int[] { 77, 172, 0, 0, 0, 0 };
			((GridBagLayout) panel19.getLayout()).rowHeights = new int[] { 0, 0 };
			((GridBagLayout) panel19.getLayout()).columnWeights = new double[] { 0.0, 1.0, 1.0, 0.0, 0.0, 1.0E-4 };
			((GridBagLayout) panel19.getLayout()).rowWeights = new double[] { 0.0, 1.0E-4 };

			// ---- label4 ----
			label4.setText("Action");
			label4.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			panel19.add(label4, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

			// ---- cmbActions ----
			cmbActions.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			panel19.add(cmbActions, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

			// ---- bLaunch ----
			bLaunch.setText("Save");
			bLaunch.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			bLaunch.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					bLaunchActionPerformed();
				}
			});
			panel19.add(bLaunch, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 5), 0, 0));

			// ---- cancelButton ----
			cancelButton.setText("Cancel");
			cancelButton.setFont(new Font("Lucida Grande", Font.PLAIN, 11));
			cancelButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					cancelButtonActionPerformed();
				}
			});
			panel19.add(cancelButton, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 5), 0, 0));

			// ---- label12 ----
			label12.setText("text");
			label12.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					label12MouseClicked(e);
				}
			});
			panel19.add(label12, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		}
		add(panel19, BorderLayout.SOUTH);

		// ======== termZoomDialog ========
		{
			termZoomDialog.setModal(true);
			Container termZoomDialogContentPane = termZoomDialog.getContentPane();
			termZoomDialogContentPane.setLayout(new BorderLayout());

			// ======== panel12 ========
			{
				panel12.setBorder(new EmptyBorder(5, 5, 5, 5));
				panel12.setLayout(new GridBagLayout());
				((GridBagLayout) panel12.getLayout()).columnWidths = new int[] { 0, 0, 0 };
				((GridBagLayout) panel12.getLayout()).rowHeights = new int[] { 0, 0, 0 };
				((GridBagLayout) panel12.getLayout()).columnWeights = new double[] { 1.0, 0.0, 1.0E-4 };
				((GridBagLayout) panel12.getLayout()).rowWeights = new double[] { 1.0, 0.0, 1.0E-4 };

				// ======== scrollPane2 ========
				{
					scrollPane2.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

					// ---- zoomTextArea ----
					zoomTextArea.setLineWrap(true);
					scrollPane2.setViewportView(zoomTextArea);
				}
				panel12.add(scrollPane2, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 0), 0, 0));

				// ---- saveZoomButton ----
				saveZoomButton.setText("Save");
				saveZoomButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						saveZoomButtonActionPerformed(e);
					}
				});
				panel12.add(saveZoomButton, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 5), 0, 0));

				// ---- cancelZoomChange ----
				cancelZoomChange.setText("Cancel");
				cancelZoomChange.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						cancelZoomChangeActionPerformed(e);
					}
				});
				panel12.add(cancelZoomChange, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));
			}
			termZoomDialogContentPane.add(panel12, BorderLayout.CENTER);
			termZoomDialog.pack();
			termZoomDialog.setLocationRelativeTo(termZoomDialog.getOwner());
		}

		// ---- buttonGroup3 ----
		ButtonGroup buttonGroup3 = new ButtonGroup();
		buttonGroup3.add(rbAct);
		buttonGroup3.add(rbInact);

		// ---- inactActRefset ----
		ButtonGroup inactActRefset = new ButtonGroup();
		inactActRefset.add(extActive);
		inactActRefset.add(extInactive);

		// ---- buttonGroup1 ----
		ButtonGroup buttonGroup1 = new ButtonGroup();
		buttonGroup1.add(rbYes);
		buttonGroup1.add(rbNo);
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	private JPanel panel2;
	private JPanel panel15;
	private JSeparator separator1;
	private JLabel label2;
	private JScrollPane scrollPane5;
	private JTextArea targetTextField;
	private JLabel spellcheckok;
	private JPanel panel18;
	private JLabel label1;
	private JComboBox comboBox1;
	private JLabel label7;
	private JRadioButton rbAct;
	private JRadioButton rbInact;
	private JLabel label5;
	private JComboBox cmbAccep;
	private JLabel label6;
	private JRadioButton extActive;
	private JRadioButton extInactive;
	private JLabel label3;
	private JRadioButton rbYes;
	private JRadioButton rbNo;
	private JLabel label13;
	private JPanel panel19;
	private JLabel label4;
	private JComboBox cmbActions;
	private JButton bLaunch;
	private JButton cancelButton;
	private JLabel label12;
	private JDialog termZoomDialog;
	private JPanel panel12;
	private JScrollPane scrollPane2;
	private JTextArea zoomTextArea;
	private JButton saveZoomButton;
	private JButton cancelZoomChange;
	// JFormDesigner - End of variables declaration //GEN-END:variables
	public WorkflowInterpreter workflowInterpreter;
	public WfComponentProvider componentProvider;
	public WfRole wfRole;
	public TranslationProject translationProject;
	private boolean readOnlyMode;

	class UpdateUIWorker extends SwingWorker<String, String> {
		private WorkListMember instance;
		private boolean isOutbox;

		public UpdateUIWorker(WorkListMember instance, boolean isOutbox) {
			super();
			this.instance = instance;
			this.isOutbox = isOutbox;
		}

		@Override
		protected String doInBackground() throws Exception {
			updating = true;
			try {
				targetTextField.setText("");
				WorkList workList = instance.getWfInstance().getWorkList();
				workflowInterpreter = WorkflowInterpreter.createWorkflowInterpreter(workList.getWorkflowDefinition());
				List<WfRole> roles = workflowInterpreter.getNextRole(instance.getWfInstance(), workList);
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
					if (bExists) {
						break;
					}
				}
				if (bExists) {
					roleConcept = Terms.get().getConcept(userRole.getId());
				}

				wfRole = userRole;
				translationProject = (TranslationProject) TerminologyProjectDAO.getProjectForWorklist(workList, config);
				List<WfAction> actions = workflowInterpreter.getPossibleActions(instance.getWfInstance(), user);
				setPossibleActions(actions, isOutbox);
				if (!wfRole.getName().contains("release authority role")) {
					addDefaultActions();
				}
			} finally {
				updating = false;
			}
			return "";
		}

	}

	void clearForm() {
		updating = true;
		descriptionInEditor = null;
		comboBox1.setEnabled(false);
		cmbAccep.setEnabled(false);
		targetTextField.setText("");
		targetTextField.setEnabled(false);
		rbYes.setEnabled(false);
		rbInact.setEnabled(false);
		extInactive.setEnabled(false);
		rbAct.setEnabled(false);
		extActive.setEnabled(false);
		rbNo.setEnabled(false);
		rbYes.setSelected(false);
		rbInact.setSelected(false);
		extInactive.setSelected(false);
		rbAct.setSelected(false);
		extActive.setSelected(false);
		rbNo.setSelected(false);
		updating = false;
	}

	public void setReadOnlyMode(boolean readOnly) {
		updating = true;
		rbInact.setEnabled(true && !readOnly);
		extInactive.setEnabled(true && !readOnly);
		rbAct.setEnabled(true && !readOnly);
		extActive.setEnabled(true && !readOnly);
		rbYes.setEnabled(true && !readOnly);
		rbNo.setEnabled(true && !readOnly);
		comboBox1.setEnabled(true && !readOnly);
		targetTextField.setEnabled(true && !readOnly);
		cmbAccep.setEnabled(true && !readOnly);
		bLaunch.setEnabled(true);
		cancelButton.setEnabled(true);
		updating = false;
	}

	public void setPossibleActions(List<WfAction> actions, boolean isOutbox) {
		cmbActions.removeAllItems();

		for (WfAction action : actions) {
			if (action.getName().contains("Approve")) {
				cmbActions.addItem(action);
				if (action.getName().equals(lastAction)) {
					cmbActions.setSelectedItem(action);
				}
				actions.remove(action);
				break;
			}
		}
		for (WfAction wfAction : actions) {
			cmbActions.addItem(wfAction);
			if (wfAction.getName().equals(lastAction)) {
				cmbActions.setSelectedItem(wfAction);
			}
		}
		if (actions.isEmpty()) {
			cmbActions.addItem(WfAction.SEND_TO_OUTBOX);
			if (WfAction.SEND_TO_OUTBOX.equals(lastAction)) {
				cmbActions.setSelectedItem(WfAction.SEND_TO_OUTBOX);
			}
		}
		cmbActions.addItem(WfAction.NEXT_ITEM);
		if (WfAction.NEXT_ITEM.equals(lastAction)) {
			cmbActions.setSelectedItem(WfAction.NEXT_ITEM);
		}
		cmbActions.addItem(WfAction.NO_ACTION);
		if (WfAction.NO_ACTION.equals(lastAction)) {
			cmbActions.setSelectedItem(WfAction.NO_ACTION);
		}
		if (isOutbox) {
			cmbActions.setSelectedItem(WfAction.NEXT_ITEM);
			if (WfAction.NEXT_ITEM.equals(lastAction)) {
				cmbActions.setSelectedItem(WfAction.NEXT_ITEM);
			}
			cmbActions.setEnabled(false);
		} else {
			cmbActions.setEnabled(true);
		}
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
		if (satdAction.getName().equals(lastAction)) {
			cmbActions.setSelectedItem(satdAction);
		}
	}

	private void setDefaultPreferredTermText(ContextualizedDescription newDesc) throws Exception {
		if (translConfig.getSelectedEditorMode().equals(ConfigTranslationModule.EditorMode.PREFERRED_TERM_EDITOR)) {
			String pref = LanguageUtil.getDefaultPreferredTermText(instance.getConcept(), sourceLangRefset, targetLangRefset, config);
			if (!pref.equals("")) {
				newDesc.setText(pref);
			}
		}
	}
}
