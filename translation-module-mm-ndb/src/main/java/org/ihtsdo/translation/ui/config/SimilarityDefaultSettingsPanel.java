/*
 * Created by JFormDesigner on Fri Jun 18 16:53:24 GMT-03:00 2010
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

public class SimilarityDefaultSettingsPanel extends JPanel {

	private static final long serialVersionUID = 1653665739753572691L;
	private ConfigTranslationModule confTrans;
	final ButtonGroup group = new ButtonGroup();
	I_TermFactory tf = Terms.get();

	public SimilarityDefaultSettingsPanel(I_ConfigAceFrame config, ConfigTranslationModule confTrans) {
		this.confTrans = confTrans;
		initComponents();
		initCustomComponents();
		if (config == null || confTrans == null) {
			error.setText("Problems initializing configuration see the logfile for more details");
		}

	}

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
	private JPanel configContainer;
	private JPanel buttonContainer;
	private JButton applyButton;
	private JButton revertButton;
	private JPanel errorContainer;
	private JLabel error;

	// JFormDesigner - End of variables declaration //GEN-END:variables

	private void setButtonsEnabled(boolean b) {
		applyButton.setEnabled(b);
		revertButton.setEnabled(b);
	}

	private class SimilaritySettingRadioButton extends JRadioButton {

		private static final long serialVersionUID = 6624960715151454927L;
		private DefaultSimilaritySearchOption similarityDefaultSetting;

		public void setSimilarityDefaultSetting(DefaultSimilaritySearchOption similarityDefaultSetting) {
			this.similarityDefaultSetting = similarityDefaultSetting;
		}

		public DefaultSimilaritySearchOption getSimilarityDefaultSetting() {
			return similarityDefaultSetting;
		}

	}

}
