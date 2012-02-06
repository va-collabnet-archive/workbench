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

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.translation.ui.ConfigTranslationModule;

/**
 * The Class SpellCheckerConfigPanel.
 *
 * @author Guillermo Reynoso
 */
public class SpellCheckerConfigPanel extends JPanel{

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1653665739753572691L;
	
	/** The conf trans. */
	private ConfigTranslationModule confTrans;
	
	/** The group. */
	final ButtonGroup group = new ButtonGroup();
	
	/** The tf. */
	I_TermFactory tf = Terms.get();
	
	/**
	 * Instantiates a new spell checker config panel.
	 *
	 * @param config the config
	 * @param confTrans2 the conf trans2
	 */
	public SpellCheckerConfigPanel(I_ConfigAceFrame config, ConfigTranslationModule confTrans2){
		this.confTrans = confTrans2;
		initComponents();
		initCustomComponents();
		if(config == null || confTrans == null){
			error.setText("Problems initializing configuration see the logfile for more details");
		}
		setButtonsEnabled(false);
	}

	/**
	 * Inits the custom components.
	 */
	private void initCustomComponents() {
		
		this.setBorder(new EmptyBorder(new Insets(5, 5, 0, 5)));
		configContainer.setBorder(new BevelBorder(BevelBorder.LOWERED));
		
		ChangeListener cl = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				setButtonsEnabled(true);
			}
		};
		
		spellChecker.addChangeListener(cl);
		
		
		applyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				confTrans.setEnableSpellChecker(spellChecker.isSelected());
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
		//inboxItemCheckbox.setEnabled(inboxItemConfigurationPermission);
		if(confTrans != null){
			spellChecker.setSelected(confTrans.isEnableSpellChecker());
		}else{
			spellChecker.setEnabled(false);
		}
			
	}

	/**
	 * Inits the components.
	 */
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY
		// //GEN-BEGIN:initComponents
		configContainer = new JPanel();
		spellChecker = new JCheckBox();
		buttonContainer = new JPanel();
		applyButton = new JButton();
		revertButton = new JButton();
		errorContainer = new JPanel();
		error = new JLabel();

		//======== this ========
		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(new BorderLayout(5, 5));

		//======== configContainer ========
		{
			configContainer.setLayout(new GridBagLayout());
			((GridBagLayout)configContainer.getLayout()).columnWidths = new int[] {0, 0};
			((GridBagLayout)configContainer.getLayout()).rowHeights = new int[] {0, 0, 0, 0, 0};
			((GridBagLayout)configContainer.getLayout()).columnWeights = new double[] {0.0, 1.0E-4};
			((GridBagLayout)configContainer.getLayout()).rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 1.0E-4};

			//---- spellChecker ----
			spellChecker.setText("Enable spell checker");
			configContainer.add(spellChecker, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 5, 0), 0, 0));
		}
		add(configContainer, BorderLayout.NORTH);

		//======== buttonContainer ========
		{
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
		// //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY
	// //GEN-BEGIN:variables
	/** The config container. */
	private JPanel configContainer;
	
	/** The spell checker. */
	private JCheckBox spellChecker;
	
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
	
}
