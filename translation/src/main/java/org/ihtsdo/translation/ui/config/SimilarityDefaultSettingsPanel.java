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
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.ihtsdo.translation.ui.ConfigTranslationModule;
import org.ihtsdo.translation.ui.ConfigTranslationModule.DefaultSimilaritySearchOption;

/**
 * The Class SimilarityDefaultSettingsPanel.
 */
public class SimilarityDefaultSettingsPanel extends JPanel {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1653665739753572691L;
	
	/** The conf trans. */
	private ConfigTranslationModule confTrans;
	
	/** The group. */
	final ButtonGroup group = new ButtonGroup();
	
	/** The tf. */
	I_TermFactory tf = Terms.get();

	/**
	 * Instantiates a new similarity default settings panel.
	 *
	 * @param config the config
	 * @param confTrans the conf trans
	 */
	public SimilarityDefaultSettingsPanel(I_ConfigAceFrame config, ConfigTranslationModule confTrans) {
		this.confTrans = confTrans;
		initComponents();
		initCustomComponents();
		if (config == null || confTrans == null) {
			error.setText("Problems initializing configuration see the logfile for more details");
		}

	}

	/**
	 * Inits the custom components.
	 */
	private void initCustomComponents() {

		this.setBorder(new EmptyBorder(new Insets(5, 5, 0, 5)));
		configContainer.setBorder(new BevelBorder(BevelBorder.LOWERED));

		error.setBorder(new EmptyBorder(new Insets(0, 5, 0, 0)));

		DefaultSimilaritySearchOption[] similaritySettings = DefaultSimilaritySearchOption.values();
		List<SimilaritySettingRadioButton> radioButtons = new ArrayList<SimilaritySettingRadioButton>();

		ChangeListener cl = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				setButtonsEnabled(true);
			}
		};

		// Create dynamiclly radio buttons
		for (DefaultSimilaritySearchOption loopMode : similaritySettings) {
			SimilaritySettingRadioButton button = new SimilaritySettingRadioButton();
			button.setText(loopMode.toString());
			button.setSimilarityDefaultSetting(loopMode);
			radioButtons.add(button);
			button.addChangeListener(cl);
			if (confTrans != null && confTrans.getDefaultSimilaritySearchOption() != null && confTrans.getDefaultSimilaritySearchOption() == loopMode) {
				button.setSelected(true);
			}
			group.add(button);
		}

		int buttonNum = 1;
		for (SimilaritySettingRadioButton jRadioButton : radioButtons) {
			GridBagConstraints constraint = new GridBagConstraints();
			// constraint.
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
		}

		setButtonsEnabled(false);

		applyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Enumeration<AbstractButton> buttons = group.getElements();
				while (buttons.hasMoreElements()) {
					SimilaritySettingRadioButton button = (SimilaritySettingRadioButton) buttons.nextElement();
					if (button.isSelected()) {
						confTrans.setDefaultSimilaritySearchOption(button.getSimilarityDefaultSetting());
						setButtonsEnabled(false);
					}
				}
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
	public void selectCurrentConfButton() {
		if (confTrans != null) {
			Enumeration<AbstractButton> buttons = group.getElements();
			while (buttons.hasMoreElements()) {
				SimilaritySettingRadioButton button = (SimilaritySettingRadioButton) buttons.nextElement();
				if (button.getSimilarityDefaultSetting() == confTrans.getDefaultSimilaritySearchOption()) {
					button.setSelected(true);
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
		configContainer = new JPanel();
		buttonContainer = new JPanel();
		applyButton = new JButton();
		revertButton = new JButton();
		errorContainer = new JPanel();
		error = new JLabel();

		// ======== this ========
		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(new BorderLayout(5, 5));

		// ======== configContainer ========
		{
			configContainer.setLayout(new GridBagLayout());
			((GridBagLayout) configContainer.getLayout()).columnWidths = new int[] { 0, 0 };
			((GridBagLayout) configContainer.getLayout()).rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0 };
			((GridBagLayout) configContainer.getLayout()).columnWeights = new double[] { 0.0, 1.0E-4 };
			((GridBagLayout) configContainer.getLayout()).rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0E-4 };
		}
		add(configContainer, BorderLayout.NORTH);

		// ======== buttonContainer ========
		{
			buttonContainer.setBorder(null);
			buttonContainer.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 10));

			// ---- applyButton ----
			applyButton.setText("Apply");
			applyButton.setHorizontalAlignment(SwingConstants.LEFT);
			buttonContainer.add(applyButton);

			// ---- revertButton ----
			revertButton.setText("Cancel");
			buttonContainer.add(revertButton);
		}
		add(buttonContainer, BorderLayout.SOUTH);

		// ======== errorContainer ========
		{
			errorContainer.setLayout(new BorderLayout(5, 5));

			// ---- error ----
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
	private void setButtonsEnabled(boolean b) {
		applyButton.setEnabled(b);
		revertButton.setEnabled(b);
	}

	/**
	 * The Class SimilaritySettingRadioButton.
	 */
	private class SimilaritySettingRadioButton extends JRadioButton {

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 6624960715151454927L;
		
		/** The similarity default setting. */
		private DefaultSimilaritySearchOption similarityDefaultSetting;

		/**
		 * Sets the similarity default setting.
		 *
		 * @param similarityDefaultSetting the new similarity default setting
		 */
		public void setSimilarityDefaultSetting(DefaultSimilaritySearchOption similarityDefaultSetting) {
			this.similarityDefaultSetting = similarityDefaultSetting;
		}

		/**
		 * Gets the similarity default setting.
		 *
		 * @return the similarity default setting
		 */
		public DefaultSimilaritySearchOption getSimilarityDefaultSetting() {
			return similarityDefaultSetting;
		}

	}

}
