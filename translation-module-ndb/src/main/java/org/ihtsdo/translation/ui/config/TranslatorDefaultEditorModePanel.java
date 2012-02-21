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

package org.ihtsdo.translation.ui.config;

import java.awt.BorderLayout;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.translation.ui.ConfigTranslationModule;
import org.ihtsdo.translation.ui.ConfigTranslationModule.EditorMode;

/**
 * The Class TranslatorDefaultEditorModePanel.
 *
 * @author Guillermo Reynoso
 */
public class TranslatorDefaultEditorModePanel extends JPanel {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -1739339632756238461L;

	/** The config. */
	private I_ConfigAceFrame config;
	
	/** The conf trans. */
	private ConfigTranslationModule confTrans;
	
	/** The current role configuration. */
	HashMap<UUID, EditorMode> currentRoleConfiguration;
	
	/** The role concepts. */
	Set<I_GetConceptData> roleConcepts = new HashSet<I_GetConceptData>();
	
	/** The edit modes. */
	EditorMode[] editModes = EditorMode.values();

	// private boolean translatorDefaultEditorModePanel;

	/**
	 * Instantiates a new translator default editor mode panel.
	 *
	 * @param config the config
	 * @param confTrans the conf trans
	 */
	public TranslatorDefaultEditorModePanel(I_ConfigAceFrame config, ConfigTranslationModule confTrans) {
		try {
			this.config = config;
			this.confTrans = confTrans;
			currentRoleConfiguration = this.confTrans.getTranslatorRoles();
			roleConcepts = getTranslationRoles();
			
			if (currentRoleConfiguration == null) {
				currentRoleConfiguration = new HashMap<UUID, EditorMode>();
				for (I_GetConceptData role : roleConcepts) {
					currentRoleConfiguration.put(role.getUids().iterator().next(), EditorMode.READ_ONLY);
				}
			}

			initComponents();
			initCustomComponents();

		} catch (TerminologyException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Inits the custom components.
	 *
	 * @throws TerminologyException the terminology exception
	 */
	private void initCustomComponents() throws TerminologyException {
		try {
			// Current role configuration
			for (I_GetConceptData role : roleConcepts) {
				List<ComboItem> comboItems = new ArrayList<ComboItem>();

				// to set the current editor mode this role
				ComboItem currentRoleEditorMode = null;
				List<UUID> uidList = role.getUids();
				EditorMode selectedEditorModeForCurrentRole = null;
				for (UUID uuid : uidList) {
					if (currentRoleConfiguration.containsKey(uuid)) {
						selectedEditorModeForCurrentRole = currentRoleConfiguration.get(uuid);
					}
				}

				// Creates the combo items, same uuid for all edior modes.
				for (EditorMode editorMode : editModes) {
					ComboItem ci = new ComboItem(editorMode, role.getUids().get(0));
					comboItems.add(ci);
					if (editorMode.equals(selectedEditorModeForCurrentRole)) {
						currentRoleEditorMode = ci;
					}
				}
				JPanel roleContainer = new JPanel();
				JComboBox editorMode = new JComboBox(comboItems.toArray());
				
				if(role == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.RELEASE_AUTHORITY_ROLE.getUids())){
					//editorMode.setEnabled(false);
				}else if(role == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.TRANSLATION_SME_ROLE.getUids())){
					//editorMode.setEnabled(false);
				}

				editorMode.setSelectedItem(currentRoleEditorMode);

				JLabel translatorRole = new JLabel();

				// ======== this ========
				setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

				// ======== panel2 ========
				{
					roleContainer.setBorder(new EmptyBorder(0, 5, 5, 5));
					roleContainer.setLayout(new GridBagLayout());
					((GridBagLayout) roleContainer.getLayout()).columnWidths = new int[] { 300, 153, 0 };
					((GridBagLayout) roleContainer.getLayout()).rowHeights = new int[] { 0, 0 };
					((GridBagLayout) roleContainer.getLayout()).columnWeights = new double[] { 0.5, 0.0, 1.0E-4 };
					((GridBagLayout) roleContainer.getLayout()).rowWeights = new double[] { 1.0, 1.0E-4 };
					roleContainer.add(editorMode, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

					// ---- label2 ----
					translatorRole.setText(role.getInitialText());
					roleContainer.add(translatorRole, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				}

				container.add(roleContainer);

				roleContainer.setMaximumSize(new Dimension(800, 35));

			}

			applyButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					List<JComboBox> comboBoxes = getCombos(container);
					for (JComboBox comboBox : comboBoxes) {
						ComboItem item = (ComboItem) comboBox.getSelectedItem();
						if (currentRoleConfiguration.containsKey(item.getUuid())) {
							currentRoleConfiguration.remove(item.getUuid());
							currentRoleConfiguration.put(item.getUuid(), item.getEditorMode());
						}
					}
				}

			});

			revertButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					revertSelections();
				}
			});

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	/**
	 * Revert selections.
	 */
	protected void revertSelections() {
		List<JComboBox> comboBoxes = getCombos(container);
		for (JComboBox comboBox : comboBoxes) {
			ComboItem currentSel = (ComboItem) comboBox.getSelectedItem();
			ComboItem oldSelection;
			if(currentSel != null){
				oldSelection = new ComboItem(currentRoleConfiguration.get(currentSel.getUuid()), currentSel.getUuid());
				comboBox.getModel().setSelectedItem(oldSelection);
			}
		}
	}

	/**
	 * Gets the combos.
	 *
	 * @param panel the panel
	 * @return the combos
	 */
	private List<JComboBox> getCombos(JPanel panel) {
		List<JComboBox> result = new ArrayList<JComboBox>();
		Component[] components = panel.getComponents();
		for (int i = 0; i < components.length; i++) {
			if (components[i] instanceof JComboBox) {
				result.add((JComboBox) components[i]);
			} else if (components[i] instanceof JPanel) {
				result.addAll(getCombos((JPanel) components[i]));
			}
		}
		return result;
	}

	/**
	 * Inits the components.
	 */
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		container = new JPanel();
		errorContainer = new JPanel();
		errorLabel = new JLabel();
		buttonContainer = new JPanel();
		applyButton = new JButton();
		revertButton = new JButton();

		// ======== this ========
		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(new BorderLayout());

		// ======== container ========
		{
			container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		}
		add(container, BorderLayout.NORTH);

		// ======== errorContainer ========
		{
			errorContainer.setBorder(new EmptyBorder(5, 5, 5, 5));
			errorContainer.setLayout(new GridBagLayout());
			((GridBagLayout) errorContainer.getLayout()).columnWidths = new int[] { 0, 0 };
			((GridBagLayout) errorContainer.getLayout()).rowHeights = new int[] { 0, 0 };
			((GridBagLayout) errorContainer.getLayout()).columnWeights = new double[] { 1.0, 1.0E-4 };
			((GridBagLayout) errorContainer.getLayout()).rowWeights = new double[] { 1.0, 1.0E-4 };

			// ---- errorLabel ----
			errorLabel.setVerticalAlignment(SwingConstants.BOTTOM);
			errorContainer.add(errorLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		}
		add(errorContainer, BorderLayout.CENTER);

		// ======== buttonContainer ========
		{
			buttonContainer.setBorder(null);
			buttonContainer.setLayout(new FlowLayout(FlowLayout.RIGHT));

			// ---- applyButton ----
			applyButton.setText("Apply");
			buttonContainer.add(applyButton);

			// ---- revertButton ----
			revertButton.setText("Cancel");
			buttonContainer.add(revertButton);
		}
		add(buttonContainer, BorderLayout.SOUTH);
		// JFormDesigner - End of component initialization
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	/** The container. */
	private JPanel container;
	
	/** The error container. */
	private JPanel errorContainer;
	
	/** The error label. */
	private JLabel errorLabel;
	
	/** The button container. */
	private JPanel buttonContainer;
	
	/** The apply button. */
	private JButton applyButton;
	
	/** The revert button. */
	private JButton revertButton;

	// JFormDesigner - End of variables declaration //GEN-END:variables

	/**
	 * Calculates a set of valid users - a user is valid is they are a child of
	 * the User concept in the top hierarchy, and have a description of type
	 * "user inbox".
	 *
	 * @return The set of valid users.
	 * @throws TerminologyException the terminology exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static Set<I_GetConceptData> getTranslationRoles() throws TerminologyException, IOException {
		I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
		
		HashSet<I_GetConceptData> validRoles = new HashSet<I_GetConceptData>();
		I_GetConceptData roleParent = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.TRANSLATOR_ROLE.getUids());

		I_IntSet allowedTypes = Terms.get().getActiveAceFrameConfig().getDestRelTypes();

		Set<? extends I_GetConceptData> allUsers = roleParent.getDestRelOrigins(config.getAllowedStatus(), allowedTypes, config.getViewPositionSetReadOnly(), Precedence.TIME,
				config.getConflictResolutionStrategy());

		for (I_GetConceptData user : allUsers) {
			validRoles.add(user);
		}

		return validRoles;
	}

	/**
	 * The Class ComboItem.
	 */
	class ComboItem {
		
		/** The editor mode. */
		private EditorMode editorMode;
		
		/** The uuid. */
		private UUID uuid;

		/**
		 * Instantiates a new combo item.
		 *
		 * @param editorMode the editor mode
		 * @param uuid the uuid
		 */
		ComboItem(EditorMode editorMode, UUID uuid) {
			this.setEditorMode(editorMode);
			this.uuid = uuid;
		}

		/**
		 * Gets the uuid.
		 *
		 * @return the uuid
		 */
		public UUID getUuid() {
			return uuid;
		}

		/**
		 * Sets the uuid.
		 *
		 * @param uuid the new uuid
		 */
		public void setUuid(UUID uuid) {
			this.uuid = uuid;
		}

		/**
		 * Sets the editor mode.
		 *
		 * @param editorMode the new editor mode
		 */
		public void setEditorMode(EditorMode editorMode) {
			this.editorMode = editorMode;
		}

		/**
		 * Gets the editor mode.
		 *
		 * @return the editor mode
		 */
		public EditorMode getEditorMode() {
			return editorMode;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			if(editorMode != null){
				return editorMode.toString();
			}else{
				return null;
			}
		}

	}

}