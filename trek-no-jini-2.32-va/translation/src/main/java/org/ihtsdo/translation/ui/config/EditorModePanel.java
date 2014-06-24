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
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.SoftBevelBorder;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.translation.ui.ConfigTranslationModule;
import org.ihtsdo.translation.ui.ConfigTranslationModule.EditingPanelOpenMode;
import org.ihtsdo.translation.ui.ConfigTranslationModule.EditorMode;

/**
 * The Class EditorModePanel.
 *
 * @author Guillermo Reynoso
 */
public class EditorModePanel extends JPanel{

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1653665739753572691L;
	
	/** The conf trans. */
	private ConfigTranslationModule confTrans;
	
	/** The group. */
	final ButtonGroup group = new ButtonGroup();
	
	/** The group2. */
	final ButtonGroup group2 = new ButtonGroup();
	
	/** The tf. */
	I_TermFactory tf = Terms.get();

	/**
	 * Instantiates a new editor mode panel.
	 *
	 * @param config the config
	 * @param confTrans the conf trans
	 */
	public EditorModePanel(I_ConfigAceFrame config, ConfigTranslationModule confTrans){
		this.confTrans = confTrans;
		initComponents();
		initCustomComponents();
		if(config == null || confTrans == null){
			error.setText("Problems initializing configuration see the logfile for more details");
		} 
	}
	
	/**
	 * Inits the custom components.
	 */
	private void initCustomComponents() {
		openModeContainer.setEnabled(false);
		
		this.setBorder(new EmptyBorder(new Insets(5, 5, 0, 5)));
		configContainer.setBorder(new BevelBorder(BevelBorder.LOWERED));
		
		error.setBorder(new EmptyBorder(new Insets(0, 5, 0, 0)));
		
		EditorMode[] editModes = EditorMode.values();
		List<EditorModeRadioButton> radioButtons = new ArrayList<EditorModeRadioButton>();
		
		EditingPanelOpenMode[] openMode = EditingPanelOpenMode.values();
		List<EditingOpenModeRb> openModeRbs = new ArrayList<EditingOpenModeRb>();
		
		ActionListener al = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object o = e.getSource();
				if(o instanceof EditorModeRadioButton){
					EditorModeRadioButton button = (EditorModeRadioButton)o;
					EditorMode mode = button.getEditorMode();
					if(mode == EditorMode.FULL_EDITOR){
						SwingUtils.disabledAllComponents(openModeContainer, false);
					}else{
						SwingUtils.disabledAllComponents(openModeContainer, true);
					}
				}
				setButtonsEnabled(true);
			}
		};
		
		for (EditingPanelOpenMode om : openMode) {
			EditingOpenModeRb openModeRb = new EditingOpenModeRb();
			openModeRb.setText(om.toString());
			openModeRb.setOpenMode(om);
			openModeRbs.add(openModeRb);
			openModeRb.addActionListener(al);
			System.out.println("EDITING MODE: "+confTrans.getEditingPanelOpenMode());
			if (confTrans != null && confTrans.getEditingPanelOpenMode() != null && confTrans.getEditingPanelOpenMode() == om) {
				openModeRb.setSelected(true);
			}else if(confTrans.getEditingPanelOpenMode() == null && om ==  EditingPanelOpenMode.FSN_TERM_MODE){
				openModeRb.setSelected(true);
			}
			group2.add(openModeRb);
		}

		//Create  dynamiclly radio buttons 
		for (EditorMode loopMode : editModes) {
			EditorModeRadioButton button = new EditorModeRadioButton();
			button.setText(loopMode.toString());
			button.setEditorMode(loopMode);
			radioButtons.add(button);
			button.addActionListener(al);
			if (confTrans != null && confTrans.getSelectedEditorMode() != null && confTrans.getSelectedEditorMode() == loopMode) {
				button.setSelected(true);
			}
			group.add(button);
		}

		int buttonNum = 1;
		for (EditorModeRadioButton jRadioButton : radioButtons) {
			GridBagConstraints constraint = new GridBagConstraints();
			//constraint.
			constraint.gridx = 1;
			constraint.gridy = buttonNum;
			constraint.gridheight = 1;
			constraint.gridwidth = 0;
			constraint.weightx = 0;
			constraint.weighty = 0;
			constraint.fill = GridBagConstraints.HORIZONTAL;
			constraint.anchor = GridBagConstraints.LINE_START;
			constraint.insets = new Insets(0, 0, 0, 0);
			
			configContainer.add(jRadioButton, constraint);
			buttonNum++;
			if(jRadioButton.getEditorMode() == EditorMode.FULL_EDITOR){
				int i = 1;
				for (EditingOpenModeRb editingOpenModeRb : openModeRbs) {
					GridBagConstraints c = new GridBagConstraints();
					//constraint.
					c.gridx = 1;
					c.gridy = i;
					c.gridheight = 1;
					c.gridwidth = 1;
					c.weightx = 0;
					c.weighty = 0;
					c.fill = GridBagConstraints.HORIZONTAL;
					c.anchor = GridBagConstraints.LINE_START;
					c.insets = new Insets(0, 0, 0, 0);
					
					openModeContainer.add(editingOpenModeRb, c);
					i++;
				}
				GridBagConstraints c2 = new GridBagConstraints();
				//constraint.
				c2.gridx = 1;
				c2.gridy = buttonNum;
				c2.gridheight = 1;
				c2.gridwidth = 0;
				c2.weightx = 0;
				c2.weighty = 0;
				c2.fill = GridBagConstraints.HORIZONTAL;
				c2.anchor = GridBagConstraints.LINE_START;
				c2.insets = new Insets(5, 30, 5, 0);
				
				configContainer.add(openModeContainer, c2);
				buttonNum++;
			}
		}
		
		setButtonsEnabled(false);
		
		applyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Enumeration<AbstractButton> buttons = group.getElements();
				while(buttons.hasMoreElements()){
					EditorModeRadioButton button = (EditorModeRadioButton)buttons.nextElement();
					if(button.isSelected()){
						confTrans.setSelectedEditorMode(button.getEditorMode());
					}
				}
				Enumeration<AbstractButton> buttons2 = group2.getElements();
				while (buttons2.hasMoreElements()) {
					EditingOpenModeRb openModeRb = (EditingOpenModeRb) buttons2.nextElement();
					if(openModeRb.isSelected()){
						confTrans.setEditingPanelOpenMode(openModeRb.getOpenMode());
					}
				}
				setButtonsEnabled(false);
			}
		});
		
		revertButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				selectCurrentConfButton();
				setButtonsEnabled(false);
			}
		});
	}
	
	/**
	 * Select current conf button.
	 */
	public void selectCurrentConfButton(){
		if(confTrans != null){
			Enumeration<AbstractButton> buttons = group.getElements();
			while(buttons.hasMoreElements()){
				EditorModeRadioButton button = (EditorModeRadioButton)buttons.nextElement();
				if(button.getEditorMode() == confTrans.getSelectedEditorMode()){
					button.setSelected(true);
				}
			}
			Enumeration<AbstractButton> buttons2 = group2.getElements();
			while (buttons2.hasMoreElements()) {
				EditingOpenModeRb abstractButton = (EditingOpenModeRb) buttons2.nextElement();
				if(abstractButton.getOpenMode() == confTrans.getEditingPanelOpenMode()){
					abstractButton.setSelected(true);
				}
			}
			if(confTrans.getSelectedEditorMode() !=  EditorMode.FULL_EDITOR){
				SwingUtils.disabledAllComponents(openModeContainer, true);
			}
		}
	}

	/**
	 * Inits the components.
	 */
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		configContainer = new JPanel();
		buttonContainer = new JPanel();
		applyButton = new JButton();
		revertButton = new JButton();
		errorContainer = new JPanel();
		error = new JLabel();
		openModeContainer = new JPanel();

		//======== this ========
		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(new BorderLayout(5, 5));

		//======== configContainer ========
		{
			configContainer.setLayout(new GridBagLayout());
			((GridBagLayout)configContainer.getLayout()).columnWidths = new int[] {0, 0};
			((GridBagLayout)configContainer.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0};
			((GridBagLayout)configContainer.getLayout()).columnWeights = new double[] {0.0, 1.0E-4};
			((GridBagLayout)configContainer.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
		}
		add(configContainer, BorderLayout.NORTH);

		//======== buttonContainer ========
		{
			buttonContainer.setBorder(null);
			buttonContainer.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 10));

			//---- applyButton ----
			applyButton.setText("Apply");
			applyButton.setHorizontalAlignment(SwingConstants.LEFT);
			buttonContainer.add(applyButton);

			//---- revertButton ----
			revertButton.setText("Cancel");
			buttonContainer.add(revertButton);
		}
		add(buttonContainer, BorderLayout.SOUTH);

		//======== errorContainer ========
		{
			errorContainer.setLayout(new BorderLayout(5, 5));

			//---- error ----
			error.setForeground(UIManager.getColor("Button.light"));
			errorContainer.add(error, BorderLayout.SOUTH);
		}
		add(errorContainer, BorderLayout.CENTER);

		//======== openModeContainer ========
		{
			openModeContainer.setBorder(new CompoundBorder(
				new EmptyBorder(0, 0, 0, 5),
				new SoftBevelBorder(SoftBevelBorder.LOWERED)));
			openModeContainer.setLayout(new GridBagLayout());
			((GridBagLayout)openModeContainer.getLayout()).columnWidths = new int[] {0, 0};
			((GridBagLayout)openModeContainer.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0};
			((GridBagLayout)openModeContainer.getLayout()).columnWeights = new double[] {0.0, 1.0E-4};
			((GridBagLayout)openModeContainer.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4};
		}
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	/** The config container. */
	private JPanel configContainer;
	
	/** The button container. */
	private JPanel buttonContainer;
	
	/** The apply button. */
	private JButton applyButton;
	
	/** The revert button. */
	private JButton revertButton;
	
	/** The error container. */
	private JPanel errorContainer;
	
	/** The error. */
	private JLabel error;
	
	/** The open mode container. */
	private JPanel openModeContainer;
	// JFormDesigner - End of variables declaration //GEN-END:variables

	/**
	 * Sets the buttons enabled.
	 *
	 * @param b the new buttons enabled
	 */
	private void setButtonsEnabled(boolean b){
		applyButton.setEnabled(b);
		revertButton.setEnabled(b);
	}

	/**
	 * The Class EditorModeRadioButton.
	 */
	private class EditorModeRadioButton extends JRadioButton{
		
		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 6624960715151454927L;
		
		/** The editor mode. */
		private EditorMode editorMode;
		
		/**
		 * Gets the editor mode.
		 *
		 * @return the editor mode
		 */
		public EditorMode getEditorMode() {
			return editorMode;
		}
		
		/**
		 * Sets the editor mode.
		 *
		 * @param editorMode the new editor mode
		 */
		public void setEditorMode(EditorMode editorMode) {
			this.editorMode = editorMode;
		}
		
	}
	
	/**
	 * The Class EditingOpenModeRb.
	 */
	private class EditingOpenModeRb extends JRadioButton{
		
		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 6424049142919640725L;
		
		/** The open mode. */
		private EditingPanelOpenMode openMode;

		/**
		 * Gets the open mode.
		 *
		 * @return the open mode
		 */
		public EditingPanelOpenMode getOpenMode() {
			return openMode;
		}

		/**
		 * Sets the open mode.
		 *
		 * @param openMode the new open mode
		 */
		public void setOpenMode(EditingPanelOpenMode openMode) {
			this.openMode = openMode;
		}
		
	}
	
}
