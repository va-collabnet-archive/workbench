/*
 * Created by JFormDesigner on Mon Jul 19 19:45:14 GMT-03:00 2010
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
 * @author Guillermo Reynoso
 */
public class TranslatorDefaultEditorModePanel extends JPanel {

	private static final long serialVersionUID = -1739339632756238461L;

	private I_ConfigAceFrame config;
	private ConfigTranslationModule confTrans;
	HashMap<UUID, EditorMode> currentRoleConfiguration;
	Set<I_GetConceptData> roleConcepts = new HashSet<I_GetConceptData>();
	EditorMode[] editModes = EditorMode.values();

	// private boolean translatorDefaultEditorModePanel;

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
				
				if(role == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.TRANSLATION_SME_ROLE.getUids())){
					editorMode.setEnabled(false);
				}else if(role == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.RELEASE_AUTHORITY_ROLE.getUids())){
					editorMode.setEnabled(false);
				}else if(role == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.TRANSLATION_SUPER_SME_ROLE.getUids())){
					editorMode.setEnabled(false);
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
	private JPanel container;
	private JPanel errorContainer;
	private JLabel errorLabel;
	private JPanel buttonContainer;
	private JButton applyButton;
	private JButton revertButton;

	// JFormDesigner - End of variables declaration //GEN-END:variables

	/**
	 * Calculates a set of valid users - a user is valid is they are a child of
	 * the User concept in the top hierarchy, and have a description of type
	 * "user inbox".
	 * 
	 * @return The set of valid users.
	 * @throws IOException 
	 * @throws TerminologyException 
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

	class ComboItem {
		private EditorMode editorMode;
		private UUID uuid;

		ComboItem(EditorMode editorMode, UUID uuid) {
			this.setEditorMode(editorMode);
			this.uuid = uuid;
		}

		public UUID getUuid() {
			return uuid;
		}

		public void setUuid(UUID uuid) {
			this.uuid = uuid;
		}

		public void setEditorMode(EditorMode editorMode) {
			this.editorMode = editorMode;
		}

		public EditorMode getEditorMode() {
			return editorMode;
		}

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